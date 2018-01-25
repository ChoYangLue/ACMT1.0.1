package com.tsuchiya.ken.acmt101;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class Cube {
    // 頂点数の初期化
    private int numIndexs = 0;
    private int numVerts = 0;

    // 頂点データ格納用バッファ
    private FloatBuffer mVertexBuffer;

    // 法線データ格納用バッファ
    private FloatBuffer mNormalBuffer;

    // indexデータ格納用バッファ
    private FloatBuffer mIndexBuffer;

    // 色と反射
    private float R = 0.82f;
    private float G = 0.88f;
    private float B = 0.87f;
    private float A = 1f;
    private float Sh = 20.f;

    // 描画するか
    public boolean DrawFlag;

    // コンストラクタ
    Cube() {
        makeCube(1f);
    }
    Cube(float r) { //r rudius of circumsphere
        makeCube(r);
    }

    // cubeを作る
    public void makeCube(float r) {
        float orig_vertex_positions[][] = {
                {-1.0f, -1.0f, 1.0f},
                {-1.0f, 1.0f, 1.0f},
                {-1.0f, -1.0f, -1.0f},
                {-1.0f, 1.0f, -1.0f},
                {1.0f, -1.0f, 1.0f},
                {1.0f, 1.0f, 1.0f},
                {1.0f, -1.0f, -1.0f},
                {1.0f, 1.0f, -1.0f},
        };

        int[] face_index = {
                1, 2, 0, 3, 6, 2,
                7, 4, 6, 5, 0, 4,
                6, 0, 2, 3, 5, 7,
                1, 3, 2, 3, 7, 6,
                7, 5, 4, 5, 1, 0,
                6, 4, 0, 3, 1, 5,
        };

        float orig_vertex_normals[][] = {
                {-1.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, -1.0f},
                {1.0f, 0.0f, 0.0f},
                {0.0f, 0.0f, 1.0f},
                {0.0f, -1.0f, 0.0f},
                {0.0f, 1.0f, 0.0f},
        };

        int[] norm_index = {
                0, 0, 0, 1, 1, 1,
                2, 2, 2, 3, 3, 3,
                4, 4, 4, 5, 5, 5,
                0, 0, 0, 1, 1, 1,
                2, 2, 2, 3, 3, 3,
                4, 4, 4, 5, 5, 5,
        };

        float[] index = new float[orig_vertex_positions.length];
        for(int i=0;i<orig_vertex_positions.length;i++){
            index[i] = (float) i;
            //System.out.println("ind:"+index[i]);
        }

        float[] vertex_positions = new float[face_index.length*3];
        float[] vertex_normals = new float[norm_index.length*3];
        int j=0;
        for(int i=0;i<face_index.length;i++){
            vertex_positions[j] = orig_vertex_positions[face_index[i]][0];
            vertex_positions[j+1] = orig_vertex_positions[face_index[i]][1];
            vertex_positions[j+2] = orig_vertex_positions[face_index[i]][2];

            vertex_normals[j]=orig_vertex_normals[norm_index[i]][0];
            vertex_normals[j+1]=orig_vertex_normals[norm_index[i]][1];
            vertex_normals[j+2]=orig_vertex_normals[norm_index[i]][2];
            j+=3;
        }
        //System.out.println(Arrays.toString(vertex_positions));

        ByteBuffer bb = ByteBuffer.allocateDirect(vertex_positions.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(vertex_positions);
        mVertexBuffer.position(0);


        ByteBuffer bbb = ByteBuffer.allocateDirect(vertex_normals.length * 4);
        bbb.order(ByteOrder.nativeOrder());
        mNormalBuffer = bbb.asFloatBuffer();
        mNormalBuffer.put(vertex_normals);
        mNormalBuffer.position(0);

        ByteBuffer bbbb = ByteBuffer.allocateDirect(index.length * 4);
        bbbb.order(ByteOrder.nativeOrder());
        mIndexBuffer = bbbb.asFloatBuffer();
        mIndexBuffer.put(index);
        mIndexBuffer.position(0);


        numIndexs = (vertex_positions.length + 1) / 3;
        numVerts = (vertex_positions.length + 1);

        System.out.println("numVert:" + numVerts);
        System.out.println("numIndexs:" + numIndexs);

        // ポイントスプライトの設定
        //GLES20.glEnable(GLES20.GL_VERTEX_P);

        DrawFlag = true;
    }

    // 描画
    public void draw(int DispMode, int index_num) {
        // Vertexシェーダーコードのnormal変数の番号を取得
        GLES20.glEnableVertexAttribArray(GLES.normalHandle);
        // バッファとvPosを結びつける
        GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, mNormalBuffer);

        // 選択中の頂点のインデックス
        GLES20.glUniform1i(GLES.indexNumber, index_num);

        // Vertexシェーダーコードのvertex変数の番号を取得
        int vPos = GLES.positionHandle;
        GLES20.glEnableVertexAttribArray(vPos);
        // バッファとvPosを結びつける
        GLES20.glVertexAttribPointer(vPos, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

        //周辺光反射
        GLES20.glUniform4f(GLES.materialAmbientHandle, R, G, B, A);

        //拡散反射
        GLES20.glUniform4f(GLES.materialDiffuseHandle, R, G, B, A);

        //鏡面反射
        GLES20.glUniform4f(GLES.materialSpecularHandle, 1f, 1f, 1f, A);
        GLES20.glUniform1f(GLES.materialShininessHandle, Sh);

        //shadingを使わない時に使う単色の設定 (r, g, b,a)
        GLES20.glUniform4f(GLES.objectColorHandle, R, G, B, A);

        // Vertexシェーダーコードのindex変数の番号を取得
        GLES20.glEnableVertexAttribArray(GLES.indexHandle);
        // バッファとvIndexを結びつける
        GLES20.glVertexAttribPointer(GLES.indexHandle, 1, GLES20.GL_FLOAT, false, 0, mIndexBuffer);

        if (DrawFlag) {
            switch (DispMode) {
                case 0:
                    // 描画する頂点をVertexシェーダーに指定
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numIndexs);
                    break;
                case 1:
                    //GLES20.glDrawArrays(GLES20.GL_LINES, 0, numIndexs);
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numIndexs);

                    //shadingを使わない時に使う単色の設定 (r, g, b,a)
                    GLES20.glUniform4f(GLES.objectColorHandle, 0.959f, 0.560f, 0.368f, 1.0f);
                    GLES.disableShading(); //シェーディング機能を無向にする
                    GLES20.glDrawArrays(GLES20.GL_POINTS, 0, numIndexs);
                    GLES.enableShading(); //シェーディング機能を使う設定に戻す
                    break;
                default:
                    break;
            }
        }


    }

    // 削除と非表示
    public void DeleteAll(){

        // バッファのクリア
        mVertexBuffer.clear();
        mNormalBuffer.clear();
        mIndexBuffer.clear();

        DrawFlag = false;
    }
}
