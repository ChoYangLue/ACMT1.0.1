package com.tsuchiya.ken.acmt101;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.GestureDetector;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.WindowManager;

import java.io.File;


public class MainActivity extends AppCompatActivity {
    private MyGLSurfaceView glView;
    private GestureDetector gesDetector = null;

    FileListDialog dialog = new FileListDialog(this);

    private int FileSelectNum=0;

    // 通知
    private void toastMake(String message, int x, int y){
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER| Gravity.CENTER, x, y);
        toast.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ツールバー
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("ACMT1.0.1");
        toolbar.setBackgroundColor(Color.parseColor("#353020"));
        setSupportActionBar(toolbar);

        // ボタン
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);  // 非表示
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // openGLのビュー
        glView=(MyGLSurfaceView)findViewById(R.id.id_myGLview);

        gesDetector = new GestureDetector(this, (GestureDetector.OnGestureListener) glView);

        // スリープさせないたくない
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        dialog.setOnFileListDialogListener(new FileListDialog.onFileListDialogListener() {
            @Override
            public void onClickFileList(File file) {
                if(file == null){
                    //not select
                }else{
                    //select file or directory
                    //System.out.println(file);

                    String path = file.getPath();

                    // ファイル読み込み
                    //glView.ReadFile(path);

                    switch (FileSelectNum){
                        case 0:
                            //glView.ReadFile(path);
                            glView.ImportModel(path);
                            break;
                        case 1:
                            glView.ExportModel();
                            break;
                    }

                    //System.out.println("FileSelectNum:"+FileSelectNum);

                    // 第3引数は、表示期間（LENGTH_SHORT、または、LENGTH_LONG）
                    toastMake(path, 0, -200);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // ボタンのIDを取得
        int id = item.getItemId();

        // ボタンの処理
        switch (id){
            case R.id.action_settings:
                System.out.println("import");
                FileSelectNum = 0;
                dialog.show(Environment.getExternalStorageDirectory().getPath(), "select");
                return true;
            case R.id.export:
                System.out.println("export");
                FileSelectNum = 1;
                dialog.show(Environment.getExternalStorageDirectory().getPath(), "select");
                return true;
            case R.id.id_open:
                System.out.println("open");
                FileSelectNum = 2;
                dialog.show("/", "select");
                return true;
            case R.id.id_radio_R:
                item.setChecked(!item.isChecked());
                System.out.println("render");
                glView.SetDisplayMode(0);
                return true;
            case R.id.id_radio_E:
                item.setChecked(!item.isChecked());
                System.out.println("edit");
                glView.SetDisplayMode(1);
                return true;
            case R.id.id_delall:
                System.out.println("DeleteAll");
                glView.DeleteObj(0);
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //glView.requestRender();
        return gesDetector.onTouchEvent(event);
    }

    //アクティビティレジューム時に呼ばれる
    @Override
    public void onResume() {
        super.onResume();
        glView.onResume();
    }

    //アクティビティポーズ時に呼ばれる
    @Override
    public void onPause() {
        super.onPause();
        glView.onPause();
    }

}

// http://coskx.webcrow.jp/mrr/for_students/androidopengles/index.html
// http://d.hatena.ne.jp/poyonshot/20120316/1331906154
// https://techbooster.org/android/application/1629/
// https://akira-watson.com/android/touchevent.html
// http://xiaoxia.exblog.jp/13352288/
//http://mousouprogrammer.blogspot.jp/2013/01/android_21.html
//http://www.adakoda.com/android/000086.html
//https://www.javadrive.jp/start/string/index4.html
//http://chat-messenger.net/blog-entry-38.html
//http://www.hiramine.com/programming/3dmodelfileformat/objfileformat.html
//sinsengumi.net/blog/2009/03/javaでファイルを1行ずつ読み込む/
//https://qiita.com/shunjiro/items/f3bfca727b76350ee23a
//http://www.programmingmat.jp/android_lab/gles20_triangle.html
//http://chemicalfactory.hatenablog.com/entry/2013/08/16/213706
//https://qiita.com/nein37/items/d0c01daf240f3f585092
//http://seesaawiki.jp/w/moonlight_aska/d/%CA%A3%BB%A8%A4%CA%A5%BF%A5%C3%A5%C1%A5%A4%A5%D9%A5%F3%A5%C8%A4%F2%BC%E8%C6%C0%A4%B9%A4%EB


// コントローラー
//https://analogicintelligence.blogspot.jp/2016/09/i2cspil3gd20.html
//http://n.mtng.org/ele/arduino/tutorial012.html
//http://makers-with-myson.blog.so-net.ne.jp/2014-06-21
//https://ameblo.jp/wise-me-362/entry-12215436801.html


// 色
//https://www.colorcodehex.com/8e8356/