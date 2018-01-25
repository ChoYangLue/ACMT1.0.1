package com.tsuchiya.ken.acmt101;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static android.opengl.GLES20.GL_TRIANGLES;

/**
 * Created by admin on 2017/10/20.
 */
public class Model {
    // 頂点数の初期化
    private int numIndexs = 0;
    private int numVerts = 0;

    // 描画フラグ
    public boolean DrawFlag = false;

    //頂点座標
    ArrayList<ArrayList<Float>> VERT = new ArrayList<>();

    //頂点座標番号列
    ArrayList<Integer> FACE = new ArrayList<>();

    //拡頂点の法線ベクトル
    ArrayList<ArrayList<Float>> NORM = new ArrayList<>();

    //拡頂点の法線ベクトル番号列
    ArrayList<Integer> NORMIND = new ArrayList<>();

    // UV座標インデックス
    ArrayList<Integer> TEXIND = new ArrayList<>();

    // 頂点データ格納用バッファ
    private FloatBuffer mVertexBuffer;

    // 法線データ格納用バッファ
    private FloatBuffer mNormalBuffer;

    // indexデータ格納用バッファ
    private FloatBuffer mIndexBuffer;

    // マテリアルファイルフラグ
    boolean mtlFlag = false;

    // マテリアルファイルパス
    String mtlPath;

    public void DeleteAll(){
        if (DrawFlag){
            // ArrayListのクリア
            VERT.clear();
            FACE.clear();
            NORM.clear();
            NORMIND.clear();
            TEXIND.clear();

            // バッファのクリア
            mVertexBuffer.clear();
            mNormalBuffer.clear();
            mIndexBuffer.clear();
        }

        DrawFlag = false;
    }

    // objファイルの１行から情報を引き出す
    public void makeObjModel(String LineString, String ModelPath){
        // 空白で区切る
        String[] LineElement = LineString.split(" ");
        if (LineElement[0].equals("v")){
            // 頂点
            ArrayList<Float> tVERT = new ArrayList<>();
            tVERT.add(Float.parseFloat(LineElement[1]));
            tVERT.add(Float.parseFloat(LineElement[2]));
            tVERT.add(Float.parseFloat(LineElement[3]));
            VERT.add(tVERT);
            //System.out.println("tVERT:"+tVERT.toString());
            //System.out.println("VERT:"+VERT.toString());
        }
        else if (LineElement[0].equals("f")){
            for (int i=1;i<4;i++){
                // インデックス
                String[] FaceTemp = LineElement[i].split("/");
                switch (FaceTemp.length){
                    case 1:
                        // 頂点インデックス
                        FACE.add(Integer.parseInt(FaceTemp[0])-1);
                        break;
                    case 2:
                        // 頂点インデックス
                        FACE.add(Integer.parseInt(FaceTemp[0])-1);
                        TEXIND.add(Integer.parseInt(FaceTemp[1])-1);
                        break;
                    case 3:
                        // 頂点インデックス + 法線インデックス
                        FACE.add(Integer.parseInt(FaceTemp[0])-1);
                        //TEXIND.add(Integer.parseInt(FaceTemp[1])-1);
                        NORMIND.add(Integer.parseInt(FaceTemp[2])-1);
                        break;
                    default:
                        break;
                }

            }
        }
        else if (LineElement[0].equals("vn")){
            // 法線ベクトル
            ArrayList<Float> tNORM = new ArrayList<>();
            tNORM.add(Float.parseFloat(LineElement[1]));
            tNORM.add(Float.parseFloat(LineElement[2]));
            tNORM.add(Float.parseFloat(LineElement[3]));
            NORM.add(tNORM);
        }
        else if (LineElement[0].equals("vt")){
            // UV座標
        }
        else if (LineElement[0].equals("mtllib")){
            // マテリアルファイル
            mtlFlag = true;
            //mtlPath = ModelPath.split(".")[0]+".mtl";
            //System.out.println(mtlPath);
        }
        else if (LineString.equals("END")) {
            // ArrayListから普通の配列に変換
            float[] vertexs = new float[FACE.size() * 3];
            float[] normals = new float[NORMIND.size() * 3];
            int j = 0;
            //byte[] indexs = new byte[FACE.size()];
            for (int i = 0; i < FACE.size(); i++) {
                //indexs[i] = FACE.get(i).byteValue();

                vertexs[j] = VERT.get(FACE.get(i)).get(0);
                vertexs[j + 1] = VERT.get(FACE.get(i)).get(1);
                vertexs[j + 2] = VERT.get(FACE.get(i)).get(2);

                normals[j] = NORM.get(NORMIND.get(i)).get(0);
                normals[j + 1] = NORM.get(NORMIND.get(i)).get(1);
                normals[j + 2] = NORM.get(NORMIND.get(i)).get(2);
                j += 3;
            }

            float[] index = new float[VERT.size()];
            for(int i=0;i<VERT.size();i++){
                index[i] = (float) i;
                //System.out.println("ind:"+index[i]);
            }

            // 頂点用のバッファーを作成
            ByteBuffer bb = ByteBuffer.allocateDirect(vertexs.length * 4);
            bb.order(ByteOrder.nativeOrder());
            mVertexBuffer = bb.asFloatBuffer();
            mVertexBuffer.put(vertexs);
            mVertexBuffer.position(0);

            // 法線用のバッファーを作成
            ByteBuffer Nbb = ByteBuffer.allocateDirect(normals.length * 4);
            Nbb.order(ByteOrder.nativeOrder());
            mNormalBuffer = Nbb.asFloatBuffer();
            mNormalBuffer.put(normals);
            mNormalBuffer.position(0);

            // インデックス用のバッファーを作成
            ByteBuffer bbbb = ByteBuffer.allocateDirect(index.length * 4);
            bbbb.order(ByteOrder.nativeOrder());
            mIndexBuffer = bbbb.asFloatBuffer();
            mIndexBuffer.put(index);
            mIndexBuffer.position(0);

            numIndexs = (vertexs.length + 1) / 3;
            numVerts = (vertexs.length + 1);

            //System.out.println(Arrays.toString(vertexs));
            //System.out.println(VERT.toString());
            //System.out.println("numVerts:" + numVerts);
            //System.out.println("numIndexs:" + numIndexs);
            //System.out.println("FACE>SIze:"+FACE.size());

            DrawFlag = true;
        }

    }

    public void MakeCube(){
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

            float[] vertex_positions = new float[face_index.length*3];
            int j=0;
            for(int i=0;i<face_index.length;i++){
                vertex_positions[j] = orig_vertex_positions[face_index[i]][0];
                vertex_positions[j+1] = orig_vertex_positions[face_index[i]][1];
                vertex_positions[j+2] = orig_vertex_positions[face_index[i]][2];
                j+=3;
            }
            //System.out.println(Arrays.toString(vertex_positions));

            ByteBuffer bb = ByteBuffer.allocateDirect(vertex_positions.length * 4);
            bb.order(ByteOrder.nativeOrder());
            mVertexBuffer = bb.asFloatBuffer();
            mVertexBuffer.put(vertex_positions);
            mVertexBuffer.position(0);

            numIndexs = (vertex_positions.length + 1) / 3;
            numVerts = (vertex_positions.length + 1);

            System.out.println("numVerts:" + numVerts);
            System.out.println("numIndexs:" + numIndexs);
    }

    public void draw(float r,float g,float b,float a, float shininess, int DispMode) {
        // シェーダーに送る
        if (DrawFlag){

            // Vertexシェーダーコードのnormal変数の番号を取得
            GLES20.glEnableVertexAttribArray(GLES.normalHandle);
            // バッファとvPosを結びつける
            GLES20.glVertexAttribPointer(GLES.normalHandle, 3, GLES20.GL_FLOAT, false, 0, mNormalBuffer);
            //GLES20.glDisableVertexAttribArray(GLES.normalHandle);

            // Vertexシェーダーコードのvertex変数の番号を取得
            int vPos = GLES.positionHandle;
            GLES20.glEnableVertexAttribArray(vPos);
            // バッファとvPosを結びつける
            GLES20.glVertexAttribPointer(vPos, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);

            // 選択中の頂点のインデックス
            GLES20.glUniform1i(GLES.indexNumber, 3);

            // Vertexシェーダーコードのindex変数の番号を取得
            GLES20.glEnableVertexAttribArray(GLES.indexHandle);
            // バッファとvIndexを結びつける
            GLES20.glVertexAttribPointer(GLES.indexHandle, 1, GLES20.GL_FLOAT, false, 0, mIndexBuffer);

            //周辺光反射
            GLES20.glUniform4f(GLES.materialAmbientHandle, r, g, b, a);

            //拡散反射
            GLES20.glUniform4f(GLES.materialDiffuseHandle, r, g, b, a);

            //鏡面反射
            GLES20.glUniform4f(GLES.materialSpecularHandle, 1f, 1f, 1f, a);
            GLES20.glUniform1f(GLES.materialShininessHandle, shininess);

            //shadingを使わない時に使う単色の設定 (r, g, b,a)
            GLES20.glUniform4f(GLES.objectColorHandle, r, g, b, a);

            // 描画する頂点をVertexシェーダーに指定
            //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, numIndexs);
            //GLES20.glDisableVertexAttribArray(vPos);

            switch (DispMode){
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
}
