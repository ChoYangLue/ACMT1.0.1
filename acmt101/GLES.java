package com.tsuchiya.ken.acmt101;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;


/**
 * Created by tommy on 2015/06/18.
 */
//シェーダ操作
public class GLES {

    //頂点シェーダのコード
    private final static String VERTEX_CODE=
        //shadingを使用するflag 1の時使用する，0の時使用しない（単色にする）
        "uniform int u_EnableShading;"+
        //shadingを使用しない時の色の設定（単色）
        "uniform vec4 u_ObjectColor;"+

        //光源
        "uniform vec4 u_LightAmbient;"+ //光源の環境光色
        "uniform vec4 u_LightDiffuse;"+ //光源の拡散光色
        "uniform vec4 u_LightSpecular;"+//光源の鏡面光色
        "uniform vec4 u_LightPos;"+     //光源の位置（カメラビュー座標系）

        //マテリアル
        "uniform vec4 u_MaterialAmbient;"+   //マテリアルの環境光色
        "uniform vec4 u_MaterialDiffuse;"+   //マテリアルの拡散光色
        "uniform vec4 u_MaterialSpecular;"+  //マテリアルの鏡面光色
        "uniform float u_MaterialShininess;"+//マテリアルの鏡面指数

        //行列
        "uniform mat4 u_MMatrix;" +       //モデルビュー行列
        "uniform mat4 u_PMMatrix;" +       //射影行列×モデルビュー行列

        //頂点情報
        //"attribute vec4 a_Position;"+  //位置
        "attribute vec3 a_Position;"+  //位置
        "attribute vec3 a_Normal;"+     //法線ベクトル
        "attribute float a_Index;"+         // 頂点インデックス
        "uniform int u_IndexNumber;"+

        //出力
        "varying vec4 v_Color;"+ "" +

        "void main(){"+
            "if (u_EnableShading==1) {"+
                //環境光の計算
                "vec4 ambient=u_LightAmbient*u_MaterialAmbient;"+

                //拡散光の計算
                //"vec3 P=vec3(u_MMatrix*a_Position);"+
                "vec3 P=vec3(u_MMatrix*vec4(a_Position,1.0));"+
                "vec3 L=normalize(vec3(u_LightPos)-P);"+  //光源方向単位ベクトル
                "vec3 N=normalize(mat3(u_MMatrix)*a_Normal);"+  //法線単位ベクトル
                "float dotLN=max(dot(L,N),0.0);"+
                "vec4 diffuseP=vec4(dotLN);"+
                "vec4 diffuse=diffuseP*u_LightDiffuse*u_MaterialDiffuse;"+

                //鏡面光の計算
                "vec3 V=normalize(-P);"+  //視点方向単位ベクトル
                "float dotNLEffect=ceil(dotLN);"+
                "vec3 R=2.*dotLN*N-L;"+
                "float specularP=pow(max(dot(R,V),0.0),u_MaterialShininess)*dotNLEffect;"+

                "vec4 specular=specularP*u_LightSpecular*u_MaterialSpecular;"+

                //色の指定
                "v_Color=ambient+diffuse+specular;"+
            "} else {"+
                "int idx = int(a_Index);"+
                "if (idx==u_IndexNumber){"+
                    "v_Color=vec4(1.0, 0.0, 0.0, 1.0);"+
                "} else {"+
                    "v_Color=u_ObjectColor;}"+
            "}"+
            "gl_PointSize = 20.0;" +
            //位置の指定
           // "gl_Position=u_PMMatrix*a_Position;"+
            "gl_Position=u_PMMatrix*vec4(a_Position,1.0);"+
        "}";

    //フラグメントシェーダのコード
    private final static String FRAGMENT_CODE=
            "precision mediump float;"+
             "varying vec4 v_Color;"+
              "void main(){"+
                    "gl_FragColor=v_Color;"+
             "}";
    /*
    private final static String FRAGMENT_CODE=
        "precision mediump float;"+
        "varying vec4 v_Color;"+
        "void main(){"+
            "gl_FragColor=v_Color;"+
            "if(length(gl_PointCoord-vec2(0.5)) > 0.5) discard;"+
        "}";
     */

    //システム
    public static int enableShadingHandle; //shadingを行うflagのハンドル
    public static int objectColorHandle;   //shadingを行わない時に使う単色ハンドル
    public static int indexNumber;

    //光源のハンドル
    public static int lightAmbientHandle; //光源の環境光色ハンドル
    public static int lightDiffuseHandle; //光源の拡散光色ハンドル
    public static int lightSpecularHandle;//光源の鏡面光色ハンドル
    public static int lightPosHandle;     //光源の位置ハンドル


    //マテリアルのハンドル
    public static int materialAmbientHandle;  //マテリアルの環境光色ハンドル
    public static int materialDiffuseHandle;  //マテリアルの拡散光色ハンドル
    public static int materialSpecularHandle; //マテリアルの鏡面光色ハンドル
    public static int materialShininessHandle;//マテリアルの鏡面指数ハンドル

    //行列のハンドル
    public static int mMatrixHandle;     //モデルビュー行列ハンドル（カメラビュー行列×モデル変換行列）
    public static int pmMatrixHandle;     //(射影行列×モデルビュー行列)ハンドル

    //頂点のハンドル
    public static int positionHandle;//位置ハンドル
    public static int normalHandle;  //法線ハンドル
    public static int indexHandle;  //インデックスハンドル

    //行列
    public static float[] cMatrix=new float[16];//カメラビュー変換行列
    public static float[] mvMatrix=new float[16];//モデルビュー変換行列
    public static float[] pMatrix=new float[16];//プロジェクション行列（射影行列）
    public static float[] pmMatrix=new float[16];//pMatrix*mvMatrix

    //光源
    private static float[] LightPos = new float[4];    //光源の座標　x,y,z　（ワールド座標）
    private static float[] CVLightPos = new float[4];  //光源の座標　x,y,z　（カメラビュー座標）

    //プログラムの生成
    public static boolean makeProgram() {
        int myProgram;//プログラムオブジェクト
        //シェーダーオブジェクトの生成
        int vertexShader=loadShader(GLES20.GL_VERTEX_SHADER,VERTEX_CODE);
        if (vertexShader==-1) return false;
        int fragmentShader=loadShader(GLES20.GL_FRAGMENT_SHADER,FRAGMENT_CODE);
        if (fragmentShader==-1) return false;

        //プログラムオブジェクトの生成
        myProgram=GLES20.glCreateProgram();
        GLES20.glAttachShader(myProgram, vertexShader);
        GLES20.glAttachShader(myProgram, fragmentShader);
        GLES20.glLinkProgram(myProgram);

        // リンクエラーチェック
        int[] linked = new int[1];
        GLES20.glGetProgramiv(myProgram, GLES20.GL_LINK_STATUS, linked, 0);
        if (linked[0] <= 0) {
            Log.e(" makeProgram", "Failed in Linking");
            Log.e(" makeProgram", GLES20.glGetProgramInfoLog(myProgram));
            return false;
        }

        //shading可否ハンドルの取得
        enableShadingHandle=GLES20.glGetUniformLocation(myProgram,"u_EnableShading");

        //光源のハンドルの取得
        lightAmbientHandle=GLES20.glGetUniformLocation(myProgram,"u_LightAmbient");
        lightDiffuseHandle=GLES20.glGetUniformLocation(myProgram,"u_LightDiffuse");
        lightSpecularHandle=GLES20.glGetUniformLocation(myProgram,"u_LightSpecular");
        lightPosHandle=GLES20.glGetUniformLocation(myProgram,"u_LightPos");

        //マテリアルのハンドルの取得
        materialAmbientHandle=GLES20.glGetUniformLocation(myProgram,"u_MaterialAmbient");
        materialDiffuseHandle=GLES20.glGetUniformLocation(myProgram,"u_MaterialDiffuse");
        materialSpecularHandle=GLES20.glGetUniformLocation(myProgram,"u_MaterialSpecular");
        materialShininessHandle=GLES20.glGetUniformLocation(myProgram,"u_MaterialShininess");
        //光源を使わない時のマテリアルの色のハンドルの取得
        objectColorHandle=GLES20.glGetUniformLocation(myProgram,"u_ObjectColor");

        //行列のハンドルの取得
        mMatrixHandle=GLES20.glGetUniformLocation(myProgram,"u_MMatrix");
        pmMatrixHandle = GLES20.glGetUniformLocation(myProgram, "u_PMMatrix");

        //頂点とその法線ベクトルのハンドルの取得
        positionHandle=GLES20.glGetAttribLocation(myProgram, "a_Position");
        normalHandle=GLES20.glGetAttribLocation(myProgram, "a_Normal");
        indexHandle=GLES20.glGetAttribLocation(myProgram, "a_Index");

        indexNumber=GLES20.glGetUniformLocation(myProgram,"u_IndexNumber");

        //プログラムオブジェクトの利用開始
        GLES20.glUseProgram(myProgram);
        enableShading();

        return true;
    }

    //シェーダーオブジェクトの生成
    private static int loadShader(int type,String shaderCode) {
        int shader=GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        // コンパイルチェック
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(" loadShader", "Failed in Compilation");
            Log.e(" loadShader", GLES20.glGetShaderInfoLog(shader));
            return -1;
        }
        return shader;
    }

    //透視変換の指定
    public static void gluPerspective(float[] pm, float angle, float aspect, float near, float far) {
        float top,bottom,left,right;
        if(aspect<1f) {
            top = near * (float) Math.tan(angle * (Math.PI / 360.0));
            bottom = -top;
            left = bottom * aspect;
            right = -left;
        } else {
            right = 1.1f*near * (float) Math.tan(angle * (Math.PI / 360.0));
            left = -right;
            bottom = left / aspect;
            top = -bottom;
        }
        Matrix.frustumM(pm, 0, left, right, bottom, top, near, far);
    }

    //ワールド座標系のLightPosを受け取る
    public static void setLightPosition(float[] lp) {
        System.arraycopy(lp, 0, LightPos, 0, 4);
    }

    //プロジェクション行列（射影行列）を受け取る
    public static void setPMatrix(float[] pm) {
        System.arraycopy(pm, 0, pMatrix, 0, 16);
    }

    //カメラビュー変換行列を受け取る
    public static void setCMatrix(float[] cm) {
        System.arraycopy(cm, 0, cMatrix, 0, 16);
    }

    //カメラビュー変換行列×モデル変換行列 = モデルビュー行列をシェーダに指定
    public static void updateMatrix(float[] mm) {
        Matrix.multiplyMM(mvMatrix, 0, cMatrix, 0, mm, 0);       //mvMatrix = cMatrix * mm
        Matrix.multiplyMM(pmMatrix, 0, pMatrix, 0, mvMatrix, 0); //pmMatrix = pMatrix * mvMatrix
        //モデルビュー行列をシェーダに指定
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mvMatrix, 0);

        //プロジェクション行列（射影行列）×モデルビュー行列をシェーダに指定
        GLES20.glUniformMatrix4fv(pmMatrixHandle, 1, false, pmMatrix, 0);

        //シェーダはカメラビュー座標系の光源位置を使う
        //ワールド座標系のLightPosを，カメラビュー座標系に変換してシェーダに送る
        Matrix.multiplyMV(CVLightPos, 0, cMatrix, 0, LightPos, 0);
        GLES20.glUniform4f(GLES.lightPosHandle, CVLightPos[0], CVLightPos[1], CVLightPos[2], 1.0f);
    }

    public static void transformPCM(float[] result, float[] source ) {
        Matrix.multiplyMV(result, 0, GLES.pmMatrix, 0, source, 0);
        result[0]/=result[3];
        result[1]/=result[3];
        result[2]/=result[3];
        //result[3]にはsourceのz要素が符号反転されて入っている
    }

    public static void enableShading() {
        GLES20.glUniform1i(enableShadingHandle, 1);
    }
    public static void disableShading() {
        GLES20.glUniform1i(enableShadingHandle,0);
    }

}
