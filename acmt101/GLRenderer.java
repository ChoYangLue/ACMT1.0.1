package com.tsuchiya.ken.acmt101;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import static android.R.attr.id;
import static android.R.attr.textAppearanceLarge;
import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.MODE_WORLD_READABLE;
import static android.provider.Settings.Global.DEVICE_NAME;
import static java.lang.Math.PI;


/**
 * Created by tommy on 2015/06/18.
 */
public class GLRenderer implements GLSurfaceView.Renderer {
    //システム
    private final Context mContext;
    private boolean validProgram=false; //シェーダプログラムが有効
    private float aspect;//アスペクト比
    private float viewlength = 5.0f; //視点距離
    private float   angle=0f; //回転角度

    //視点変更テスト変数
    private float alph=0f,beta=0f;

    //光源の座標　x,y,z
    private  float[] LightPos={0f,1.5f,3f,1f};//x,y,z,1

    //変換マトリックス
    private  float[] pMatrix=new float[16]; //プロジェクション変換マトリックス
    private  float[] mMatrix=new float[16]; //モデル変換マトリックス
    private  float[] cMatrix=new float[16]; //カメラビュー変換マトリックス

    private Axes MyAxes= new Axes();  //原点周囲の軸表示とためのオブジェクトを作成
    private Cube MyCube = new Cube(); //原点に，外接球半径１の立方体オブジェクトを作成
    private Model Mymodel = new Model(); //原点に，外接球半径１の立方体オブジェクトを作成

    private float ModP = 0.0f;
    private float ModR = 0.0f;

    private float ViewWidth, ViewHeight; //画面の縦横

    private float factorx,factory;//サイズ補正

    private float[] tmpPos1v= new float[4]; //同次座標系
    private float[] tmpPos2v= new float[4]; //同次座標系
    private float[] tmpPos3v= new float[4]; //同次座標系
    private float[] ButtonPos1 = new float[2]; //正規xy座標系
    private float[] ButtonPos2 = new float[2]; //正規xy座標系
    private float[] ButtonPos3 = new float[2]; //正規xy座標系
    private float[] ButtonPos4 = new float[2]; //正規xy座標系

    //シェーダのattribute属性の変数に値を設定していないと暴走するのでそのための準備
    private static float[] DummyFloat= new float[1];
    private static final FloatBuffer DummyBuffer=BufferUtil.makeFloatBuffer(DummyFloat);

    // 傾きセンサー用の変数
    private SensorManager mSensorManager;
    private float mPitch;
    private float mRoll;

    private int DisplayMode = 0;
    private boolean moveFlag = true;

    private boolean AccelFlag = false;

    GLRenderer(final Context context) {
        mContext = context;
    }

    //サーフェイス生成時に呼ばれる
    @Override
    public void onSurfaceCreated(GL10 gl10,EGLConfig eglConfig) {
        //プログラムの生成
        validProgram = GLES.makeProgram();

        // SensorManagerを取得
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        // ジャイロセンサのリストを取得
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        //List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if (sensorList.size() <= 0) {
            // 加速度センサーでの回転を有効にする。
            AccelFlag = true;
            //Toast.makeText(mContext, "ジャイロセンサが搭載されていない", Toast.LENGTH_LONG).show();
        }

            // 傾きを初期化
            //roteX = 0;
            mPitch = 0;
            mRoll = 0;

            // イベントハンドラを登録
            mSensorManager.registerListener(
                    new SensorEventListener() {
                        public void onAccuracyChanged(
                                Sensor sensor, int accuracy) {
                        }

                        public void onSensorChanged(SensorEvent event) {
                            // 傾きを更新
                            //roteX = event.values[SensorManager.AXIS_X];
                            mPitch = event.values[SensorManager.DATA_Y];
                            mRoll = event.values[SensorManager.DATA_Z];
                            //System.out.println( roteX+":"+mPitch+":"+mRoll);
                            move(mPitch, mRoll);
                        }
                    },
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);

        // イベントハンドラを登録(加速度センサー)
        if (AccelFlag){
            mSensorManager.registerListener(
                    new SensorEventListener() {
                        public void onAccuracyChanged(
                                Sensor sensor, int accuracy) {
                        }

                        public void onSensorChanged(
                                SensorEvent event) {
                            // 傾きを更新
                            //roteX = event.values[SensorManager.AXIS_X];
                            mPitch = -event.values[SensorManager.DATA_Y];
                            mRoll = event.values[SensorManager.DATA_X];
                            //System.out.println( roteX+":"+mPitch+":"+mRoll);
                            move(mPitch, mRoll);
                        }
                    },
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_GAME);
        }


            //頂点配列の有効化
            GLES20.glEnableVertexAttribArray(GLES.positionHandle);
            GLES20.glEnableVertexAttribArray(GLES.normalHandle);

            //デプスバッファの有効化
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            // カリングの有効化
            GLES20.glEnable(GLES20.GL_CULL_FACE); //裏面を表示しないチェックを行う

            // 裏面を描画しない
            GLES20.glFrontFace(GLES20.GL_CCW); //表面のvertexのindex番号はCCWで登録
            GLES20.glCullFace(GLES20.GL_BACK); //裏面は表示しない

            //光源色の指定 (r, g, b,a)
            GLES20.glUniform4f(GLES.lightAmbientHandle, 0.15f, 0.15f, 0.15f, 1.0f); //周辺光
            GLES20.glUniform4f(GLES.lightDiffuseHandle, 0.5f, 0.5f, 0.5f, 1.0f); //乱反射光
            GLES20.glUniform4f(GLES.lightSpecularHandle, 0.9f, 0.9f, 0.9f, 1.0f); //鏡面反射光

            //背景色の設定
            //GLES20.glClearColor(0f, 0f, 0.2f, 1.0f);
            GLES20.glClearColor(0.65f, 0.62f, 0.44f, 1.0f);     // 168,157,112

            // 背景とのブレンド方法を設定します。
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);    // 単純なアルファブレンド
        }

    private final double Acceleration = 0.05;    // 加速量
    private double Dx=0;    // 速度
    private double Dy=0;    // 速度
    private float StartRote = 1.2f;  // 回転開始値
    private float ModelScale = 0.6f; // モデルの拡大率
    private float ScaleRate = 0.03f; // 拡大スピード

    public void move(float Pitch, float Roll) {
        // 傾きによって移動量を修正
        Dx = 0;
        Dy = 0;

        //System.out.println(Roll);
        //System.out.println(RoteFlag);

        // 傾きから移動量の変動幅を計算
        if (Math.abs(Roll)>StartRote) Dx = -Math.toRadians(Roll)* Acceleration;
        if (Math.abs(Pitch)>StartRote) Dy = -Math.toRadians(Pitch)* Acceleration;

        if (RoteFlag && moveFlag) beta-=Dx;   // 横方向回転
        else if (moveFlag) cam_posX -=Dx;     // 横方向平行移動
        if (beta> PI) beta = -3.14f;
        else if (beta<-PI) beta = 3.14f;

        if (RoteFlag && moveFlag) alph+=Dy;  // 縦方向回転
        else if (moveFlag) cam_posY+=Dy;      // 縦方向平行移動
        if (PI/2<alph) alph=1.57f;
        if (alph<-PI/2) alph=-1.57f;

    }

    //画面サイズ変更時に呼ばれる
    @Override
    public void onSurfaceChanged(GL10 gl10,int w,int h) {
        //ビューポート変換
        GLES20.glViewport(0,0,w,h);
        ViewWidth=w;
        ViewHeight=h;
        aspect=(float)w/(float)h;
        factory=(float)Math.sqrt(aspect);
        factorx=1f/factory;
    }

    private float cam_posX = 0;
    private float cam_posY = 0;

    //毎フレーム描画時に呼ばれる
    @Override
    public void onDrawFrame(GL10 glUnused) {
        if (!validProgram) return;
        //シェーダのattribute属性の変数に値を設定していないと暴走するのでここでセットしておく。この位置でないといけない
        GLES20.glVertexAttribPointer(GLES.positionHandle, 3, GLES20.GL_FLOAT, false, 0, DummyBuffer);
        GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, DummyBuffer);
        GLES20.glVertexAttribPointer(GLES.indexHandle, 1, GLES20.GL_FLOAT, false, 0, DummyBuffer);

        GLES.enableShading();   //シェーディング機能を有効にする。（デフォルト）

        //画面のクリア
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT |
                GLES20.GL_DEPTH_BUFFER_BIT);

        //プロジェクション変換（射影変換）--------------------------------------
        //透視変換（遠近感を作る）
        //カメラは原点に有り，z軸の負の方向を向いていて，上方向はy軸＋方向である。
        GLES.gluPerspective(pMatrix,
                45.0f,  //Y方向の画角
                aspect, //アスペクト比
                1.0f,   //ニアクリップ　　　z=-1から
                100.0f);//ファークリップ　　Z=-100までの範囲を表示することになる
        GLES.setPMatrix(pMatrix);

        //カメラビュー変換（視野変換）-----------------------------------
        //カメラ視点が原点になるような変換

        Matrix.setLookAtM(cMatrix, 0,
                (float) (viewlength * Math.sin(beta) * Math.cos(alph) + cam_posX*Math.cos(beta) ),  //カメラの視点 x
                (float) (viewlength * Math.sin(alph) + cam_posY*Math.cos(alph)),                    //カメラの視点 y
                (float) (viewlength * Math.cos(beta) * Math.cos(alph) - cam_posX*Math.sin(beta) ),  //カメラの視点 z
                (float) (cam_posX*Math.cos(beta)),(float) (cam_posY*Math.cos(alph)), (float) (cam_posX*Math.sin(beta)), //カメラの視線方向の代表点
                0.0f, 1.0f, 0.0f);//カメラの上方向

        //Matrix.translateM(cMatrix, 0, (float)(cam_posX*Math.sin(beta)*Math.cos(alph)), (float)(cam_posY*Math.sin(alph)), (float)(cam_posZ*Math.cos(beta)*Math.cos(alph)));
        /*
        Matrix.setLookAtM(cMatrix, 0,
                (float) (viewlength * Math.sin(0) * Math.cos(0)),  //カメラの視点 x
                (float) (viewlength * Math.sin(0)),                    //カメラの視点 y
                (float) (viewlength * Math.cos(0) * Math.cos(0)),  //カメラの視点 z
                0.0f,0.0f,0.0f, //カメラの視線方向の代表点
                0.0f, 1.0f, 0.0f);//カメラの上方向

        Matrix.translateM(cMatrix, 0, (float)(cam_posX*Math.sin(0)*Math.cos(0)), (float)(cam_posY*Math.sin(0)), (float)(cam_posZ*Math.cos(0)*Math.cos(0)));
        */

        //カメラ視点ビュー変換はこれで終わり。
        GLES.setCMatrix(cMatrix);

        //cMatrixをセットしてから光源位置をセット
        GLES.setLightPosition(LightPos);

        //座標軸の描画
        GLES.disableShading(); //シェーディング機能は使わない
        Matrix.setIdentityM(mMatrix, 0);//モデル変換行列mMatrixを単位行列にする。
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        //座標軸の描画本体
        //引数 r, g, b, a, shininess(1以上の値　大きな値ほど鋭くなる), linewidth
        //shininessは使用していない
        MyAxes.draw(1f, 1f, 1f, 1f, 10.f, 2f);//座標軸の描画本体
        GLES.enableShading(); //シェーディング機能を使う設定に戻す

        //MyCubeの描画
        Matrix.setIdentityM(mMatrix, 0);  //ここではすでに設定されているので省略可
        //Matrix.rotateM(mMatrix, 0, angle * 2, 0, 1, 0);
        //Matrix.rotateM(mMatrix, 0, -ModR, 0, 1, 0);
        //Matrix.rotateM(mMatrix, 0, ModP, 1, 0, 0);
        //Matrix.translateM(mMatrix, 0, (float)(-cam_posX), (float)(-cam_posY), (float)(-cam_posZ));
        Matrix.scaleM(mMatrix, 0, ModelScale, ModelScale, ModelScale);
        //Matrix.scaleM(mMatrix, 0, 0.1f, 0.1f, 0.1f);
        GLES.updateMatrix(mMatrix);//現在の変換行列をシェーダに指定
        //MyCubeの描画本体
        // r, g, b, a, shininess(1以上の値　大きな値ほど鋭くなる)
        //MyCube.draw(0f, 1f, 0f, 1f, 20.f);
        MyCube.draw(DisplayMode, 3);

        //angle+=0.5;

        if (DisplayMode == 0){
            moveFlag = true;
        }else{
            moveFlag = false;
        }

        Mymodel.draw(0.82f, 0.88f, 0.87f, 1f, 20.f, DisplayMode);

        ButtonPos1[0] = -factorx*.3f/4f;
        ButtonPos1[1] = -1f + 0.2f+factory*.3f/4f;
        ButtonPos2[0] = factorx*.3f/4f;
        ButtonPos2[1] = -1f + 0.2f+factory*.3f/4f;
        ButtonPos3[0] = -factorx*.3f/4f;
        ButtonPos3[1] = -1f + 0.2f-factory*.3f/4f;
        ButtonPos4[0] = +factorx*.3f/4f;
        ButtonPos4[1] = -1f + 0.2f-factory*.3f/4f;

    }

    private float Scroll[] = {0f, 0f}; //１本指のドラッグ[rad]
    public void setScrollValue(float DeltaX, float DeltaY) {
        Scroll[0] += DeltaX * 0.01;
        if (3.14f<Scroll[0]) Scroll[0]=3.14f;
        if (Scroll[0]<-3.14) Scroll[0]=-3.14f;
        Scroll[1] -= DeltaY * 0.01;
        if (1.57f<Scroll[1]) Scroll[1]=1.57f;
        if (Scroll[1]<-1.57) Scroll[1]=-1.57f;
        if (moveFlag) ModP=Scroll[1];
        if (moveFlag) ModR=Scroll[0];
        System.out.println(ModR+":"+ModP);
    }

    // タッチしているかで回転するか決める
    boolean RoteFlag = true;
    public void SetDownEvent(boolean UPDOWN) {
        //System.out.println(UPDOWN);
        if (UPDOWN) RoteFlag = false;
        else RoteFlag = true;
    }

    public void UpdateScale(float scalevalue){
        if (scalevalue>1.0f){
            ModelScale += ScaleRate;
        }else{
            if (ModelScale > 0.2f) ModelScale -= ScaleRate;
        }
        //ModelScale *= scalevalue;
    }

    public void singleshorttap(float DeviceX, float DeviceY) {
        final float tolerance=0.02f; //2乗誤差許容値
        final float tolerance2=0.005f; //2乗誤差許容値
        float xpos=DeviceX/ViewWidth*2f-1f;
        float ypos=-(DeviceY/ViewHeight*2f-1f);
        float d1=(xpos-tmpPos1v[0])*(xpos-tmpPos1v[0])+(ypos-tmpPos1v[1])*(ypos-tmpPos1v[1]);
        float d2=(xpos-tmpPos2v[0])*(xpos-tmpPos2v[0])+(ypos-tmpPos2v[1])*(ypos-tmpPos2v[1]);
        float d3=(xpos-tmpPos3v[0])*(xpos-tmpPos3v[0])+(ypos-tmpPos3v[1])*(ypos-tmpPos3v[1]);
        float b1=(xpos-ButtonPos1[0])*(xpos-ButtonPos1[0])+(ypos-ButtonPos1[1])*(ypos-ButtonPos1[1]);
        float b2=(xpos-ButtonPos2[0])*(xpos-ButtonPos2[0])+(ypos-ButtonPos2[1])*(ypos-ButtonPos2[1]);
        float b3=(xpos-ButtonPos3[0])*(xpos-ButtonPos3[0])+(ypos-ButtonPos3[1])*(ypos-ButtonPos3[1]);
        float b4=(xpos-ButtonPos4[0])*(xpos-ButtonPos4[0])+(ypos-ButtonPos4[1])*(ypos-ButtonPos4[1]);
        int done=0;
        Log.i("singleshorttap", "LEarth:" + tmpPos1v[0] + "," + tmpPos1v[1]);
        Log.i("singleshorttap", "SEarth:" + tmpPos2v[0] + "," + tmpPos2v[1]);
        Log.i("singleshorttap", "TapPos:" + xpos + "," + ypos);
        if (d1<tolerance) {
            Toast.makeText(mContext, "大きな地球タップされました", Toast.LENGTH_SHORT).show();
            done=1;
        }
        if (d2<tolerance) {
            Toast.makeText(mContext, "小さな地球タップされました", Toast.LENGTH_SHORT).show();
            done=1;
        }
        if (d3<tolerance) {
            Toast.makeText(mContext, "光源タップされました", Toast.LENGTH_SHORT).show();
            done=1;
        }

        if (b1<tolerance2) {
            Toast.makeText(mContext, "ボタン赤タップされました", Toast.LENGTH_SHORT).show();
            done=1;
        }
        if (b2<tolerance2) {
            Toast.makeText(mContext, "ボタン黄タップされました", Toast.LENGTH_SHORT).show();
            done=1;
        }
        if (b3<tolerance2) {
            Toast.makeText(mContext, "ボタン青タップされました", Toast.LENGTH_SHORT).show();
            done=1;
        }
        if (b4<tolerance2) {
            Toast.makeText(mContext, "ボタン白タップされました", Toast.LENGTH_SHORT).show();
            done=1;
        }
        if (done==0) {
            //viewmode += 1;
            //if (viewmode == 3) viewmode = 0;
        }
    }

    // 指定のオブジェクトを削除
    public void DeleteObj(int objID){
        if (objID == 0){
            MyCube.DeleteAll();
            Mymodel.DeleteAll();
        }
    }

    // 描画モード切替
    public void SetDisplauMode(int dm){
        DisplayMode = dm;
    }

    // モデルファイル読み込み
    public void ReadFile(String Model_Path){
        System.out.println(Model_Path);
        try{
            OutputStream out = mContext.openFileOutput("test.obj", MODE_WORLD_READABLE);
            //OutputStream out = mContext.openFileOutput(Model_Path, MODE_PRIVATE);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out,"UTF-8"));
            writer.append("v 0 0 0;");
            writer.close();

            FileInputStream fileInputStream;
            fileInputStream = mContext.openFileInput("test.obj" );
            //fileInputStream = mContext.openFileInput( Model_Path );
            BufferedReader reader = new BufferedReader( new InputStreamReader( fileInputStream , "UTF-8") );
            String str = "";
            String tmp;
            while( (tmp = reader.readLine()) != null ){
                str = str + tmp + "\n";
                System.out.print(str);
            }
            reader.close();

        }catch( IOException e ){
            e.printStackTrace();
        }

        System.out.println(Environment.getExternalStorageDirectory().getPath());
    }

    // 3Dモデルファイル書き出し
    public void ExportModel(){
        try{
            File dir = new File(Environment.getExternalStorageDirectory().getPath());
            // 外部ストレージにファイルを作成し、書き込む。
            File file = new File(Environment.getExternalStorageDirectory().getPath(), "myfile.txt");
            FileOutputStream outputStream;
            outputStream = new FileOutputStream(file);

            outputStream.write("test 0 0 0".getBytes());
            // ちなみにDDMSで確認したところ、確認時の環境下では
            // "/mnt/sdcard/Android/data/[パッケージ名]/files/myfile.txt"
            // に書き込まれた。
            outputStream.close();

            Toast.makeText(mContext, file.getPath(), Toast.LENGTH_LONG).show();

        }catch( IOException e ){
            e.printStackTrace();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // 3Dモデルファイル読み込み
    public void ImportModel(String Model_Path) {
        System.out.println(Model_Path);
        FileReader fr = null;
        BufferedReader br1 = null;
        try {
            // ファイルオブジェクトに変換
            File file = new File(Model_Path);

            // 指定したファイルが存在したら
            if (file.exists()) {
                // OBJファイルだったら
                if (getSuffix(Model_Path).equals("obj")) {
                    fr = new FileReader(file);
                    br1 = new BufferedReader(fr);

                    String lines;
                    while ((lines = br1.readLine()) != null) {
                        System.out.println(lines);
                        Mymodel.makeObjModel(lines,Model_Path);
                    }
                    Mymodel.makeObjModel("END",Model_Path);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br1.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Mymodel.makeObjModel("END");
    }

    // 拡張子を調べて返す。
    public static String getSuffix(String fileName) {
        if (fileName == null)
            return null;
        int point = fileName.lastIndexOf(".");
        if (point != -1) {
            return fileName.substring(point + 1);
        }
        return fileName;
    }
}
