package com.tsuchiya.ken.acmt101;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.widget.Toast;

//public class MyGLSurfaceView extends GLSurfaceView {
public class MyGLSurfaceView extends GLSurfaceView implements GestureDetector.OnGestureListener {
    private final Context mContext;
    private GLRenderer renderer;

    private ScaleGestureDetector gesDetect = null;
    private GestureDetector GesDetect = null;

    private float scale = 1f;

    int oldX = 0, oldY = 0;
    //スクロールイベントが起こる前の座標
    int originX, originY;
    //スクロールイベントの発生座標

    // サーフェースビューのコンストラクタ
    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context,attrs);
        mContext=context;
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderer = new GLRenderer(context);
        setRenderer(renderer);

        // ScaleGestureDetecotorクラスのインスタンス生成
        gesDetect = new ScaleGestureDetector(mContext, onScaleGestureListener);

        // GestureDetecotorクラスのインスタンス生成
        GesDetect = new GestureDetector(mContext,onGestureListener);
    }

    boolean boolval = false;
    @Override
    public boolean onTouchEvent(MotionEvent ev1){
        switch (ev1.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolval = true;
                System.out.println("ACTION_DOWN");
                oldX = (int) ev1.getX();
                oldY = (int) ev1.getY();
                break;
            case MotionEvent.ACTION_UP:
                System.out.println("ACTION_UP");
                boolval = false;
                break;
            case MotionEvent.ACTION_MOVE:
                originX = (int) ev1.getX();
                originY = (int) ev1.getY();
                //renderer.setScrollValue(oldX, oldY);
                oldX += originX;
                oldY += originY;
                break;
        }
        renderer.SetDownEvent(boolval);
        gesDetect.onTouchEvent(ev1);
        GesDetect.onTouchEvent(ev1);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) { return false; }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) { return false; }

    @Override
    public void onLongPress(MotionEvent arg0) {}

    @Override
    public void onShowPress(MotionEvent arg0) {}

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent arg0) { return true; }


    // スケールジェスチャーイベントを取得
    private final SimpleOnScaleGestureListener onScaleGestureListener = new SimpleOnScaleGestureListener(){
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //System.out.println("ScaleGesture"+ "onScale");
            scale = detector.getScaleFactor();
            System.out.println(scale);
            renderer.UpdateScale(scale);
            return super.onScale(detector);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            System.out.println("ScaleGesture"+"onScaleBegin");
            //scale = 1f;
            return super.onScaleBegin(detector);
        }
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            System.out.println("ScaleGesture"+ "onScaleEnd");
            super.onScaleEnd(detector);
        }
    };

    // 複雑なタッチイベントを取得
    private final GestureDetector.SimpleOnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            System.out.println("Gesture"+"onDoubleTap");
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            System.out.println("Gesture"+"onDoubleTapEvent");
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            System.out.println("Gesture"+"onDown");
            //renderer.singleshorttap(e.getX(),e.getY());
            return super.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            System.out.println("Gesture"+"onFling");
            return super.onFling(e1, e2, velocityX, velocityY);
        }


        @Override
        public void onLongPress(MotionEvent e) {
            System.out.println("Gesture"+"onLongPress");
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            System.out.println("Gesture"+"onScroll");
            renderer.setScrollValue(distanceX, distanceY);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public void onShowPress(MotionEvent e) {
            System.out.println("Gesture"+"onShowPress");
            super.onShowPress(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            System.out.println("Gesture"+"onSingleTapConfirmed");
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            System.out.println("Gesture"+"onSingleTapUp");
            return super.onSingleTapUp(e);
        }

    };

    public void ReadFile(String Model_Path){
        renderer.ReadFile(Model_Path);
    }

    public void DeleteObj(int objID){ renderer.DeleteObj(objID); }

    public void SetDisplayMode(int dm){ renderer.SetDisplauMode(dm); }

    public void ExportModel(){ renderer.ExportModel(); }

    public void ImportModel(String Model_Path){
        renderer.ImportModel(Model_Path);
    }
}

