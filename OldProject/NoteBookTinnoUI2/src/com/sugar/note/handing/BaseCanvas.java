package com.sugar.note.handing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;


/**
 * Created by user on 8/28/14.
 */
public class BaseCanvas extends View {

    public ArrayList<DrawPath> mDrawPaths;
    public ArrayList<DrawPath> mEraserDrawPaths;

    public Paint mPaint;

    private Path mPath;

    public Canvas mCanvas = null;

    private DrawPath mDrawPath;

    private float mx;

    private float my;

    private final float MIN_DESTENSE = 4;

    private int mCanvasViewWidth = 0;
    private int mCanvasViewHeight = 0;
    private Paint mBitmapPaint;
    public Bitmap mBitmap;
    private boolean isInitFlag = false;
    public static final int CANVAS_PAINT = 0;
    public static final int CANVAS_ERASER = 1;
    private int mCanvasMode = CANVAS_PAINT;
    //private Canvas mCircleCanvas = null;
    private Paint mCirclePaint;
    public ArrayList<Integer> mGraffitiOperation;
    

    /**
     *
      * @param context
     */
    public BaseCanvas(Context context) {
        this(context, null);
    }

    public BaseCanvas(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void intCirclePaint() {
        mCirclePaint = new Paint();
        mCirclePaint.setStrokeWidth(8);
        mCirclePaint.setColor(Color.BLACK);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
        mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
    }
    
    public void initCanvas(int canvasViewWidth, int canvasViewHeight) {
        mDrawPaths = new ArrayList<DrawPath>();
        mEraserDrawPaths = new ArrayList<DrawPath>();

        initPaint();
        mBitmap = Bitmap.createBitmap(canvasViewWidth, canvasViewHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        //mCircleCanvas = new Canvas(mBitmap);//
        intCirclePaint();
        mGraffitiOperation = new ArrayList<Integer>();
    }

    public void initPaint() {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(PaintSetAreaWindow.BASE_BOLD);
        mPaint.setColor(Color.BLUE);
        mPaint.setXfermode(null);
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);
        if (mBitmap != null && mBitmapPaint != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        }
        
        
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
        
        super.onDraw(canvas);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mCanvasViewWidth != w || mCanvasViewHeight != h) {
            mCanvasViewWidth = w;
            mCanvasViewHeight = h;
            if (isInitFlag == false) {
                initCanvas(w, h);
                isInitFlag = true;
            }
        }
    }

    public void clearEraserDrawPaths() {
        if (mEraserDrawPaths != null && mEraserDrawPaths.size() > 0) {
            mEraserDrawPaths.clear();
        }
    }

    public void eraserPaint() {
        Log.d("tui", "eraserPaint");

        //mPaint.setAlpha(0);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(80);
        mPaint.setAntiAlias(true);
    }

    private void drawEraserPath() {
        if (mEraserDrawPaths == null) {
            return;
        }

        eraserPaint();
        Iterator<DrawPath> iterator = mEraserDrawPaths.iterator();
        while (iterator.hasNext()) {
            DrawPath drawPath = iterator.next();
            mCanvas.drawPath(drawPath.path,drawPath.paint);
        }
    }

    public void clear() {
        if (mDrawPaths != null && mDrawPaths.size() > 0) {
            mDrawPaths.clear();
            clearEraserDrawPaths();
            redrawCanvas();
            clearGraffitiOperation();
        }
        
    }

    public void redrawCanvas() {
        int canvasViewWidth = getCanvasViewWidth();
        int canvasViewHeight = getCanvasViewHeight();

        if (mDrawPaths == null) {
            return;
        }
        
        mBitmap = Bitmap.createBitmap(canvasViewWidth, canvasViewHeight, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
            
        Iterator<DrawPath> iterator = mDrawPaths.iterator();
        while (iterator.hasNext()) {
            DrawPath drawPath = iterator.next();
            mCanvas.drawPath(drawPath.path,drawPath.paint);
        }

        if (getCanvasModeValue() == CANVAS_ERASER) {
            drawEraserPath();
        }

        if (mDrawPaths != null && mDrawPaths.size() == 0) {
            clearEraserDrawPaths();
            clearGraffitiOperation();
        }
        invalidate();
    }

    public int getCanvasViewWidth() {
        return mCanvasViewWidth;
    }

    public int getCanvasViewHeight() {
        return mCanvasViewHeight;
    }

    public void clearGraffitiOperation() {
        if (mGraffitiOperation != null && mGraffitiOperation.size() > 0) {
            mGraffitiOperation.clear();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                mDrawPath = new DrawPath();
                
                mDrawPath.path = mPath;
                mDrawPath.paint = mPaint;

                TouchDown(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                TouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                TouchUp(x,y);
                invalidate();
                break;
            default:
                break;
        }

        return true;
    }

    private void TouchDown(float x, float y) {
        mPath.moveTo(x,y);
        mx = x;
        my = y;

    }

    private Rect TouchMove(float x, float y) {
        Rect areaToRefresh = null;

        final float previousX = mx;
        final float previousY = my;

        float dx = Math.abs(x - mx);
        float dy = Math.abs(y - my);

        if (dx >= MIN_DESTENSE || dy >= MIN_DESTENSE) {

            float cX = (x + previousX) / 2;
            float cY = (y + previousY) / 2;
            mPath.quadTo(previousX, previousY, cX, cY);

            mx = x;
            my = y;
        }

        return areaToRefresh;
    }

    private void TouchUp(float x, float y) {
        mPath.lineTo(mx, my);
        mCanvas.drawPath(mPath,mPaint);
        if (getCanvasModeValue() == CANVAS_PAINT) {
            mDrawPaths.add(mDrawPath);
            mGraffitiOperation.add(CANVAS_PAINT);
        } else {
            if (mDrawPaths != null && mDrawPaths.size() > 0) {
                mEraserDrawPaths.add(mDrawPath);
                mGraffitiOperation.add(CANVAS_ERASER);
            }
        }
        mPath = null;
    }

    public float getPaintBoldValue() {
        return mPaint.getStrokeWidth();
    }

    public void setPaintBoldValue(int boldValue) {
        mPaint.setStrokeWidth(boldValue);
    }

    public int getCanvasModeValue() {
        return mCanvasMode;
    }

    public void setCanvasModeValue(int modeValue) {
        mCanvasMode = modeValue;
    }

    public int getPaintColor() {
        return mPaint.getColor();
    }

    public void setPaintColor(int color) {
        mPaint.setColor(color);
    }

    public class DrawPath{
        public Path path;
        public Paint paint;
    }
}
