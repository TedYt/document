package com.sugar.note.handing;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import java.util.Iterator;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.view.View;


/**
 * Created by user on 8/28/14.
 */
public class GraffitiCanvas extends BaseCanvas {
    private int lastX = 0;
    private int lastY = 0;
    private Context mContext;
    //private int paintMode = 0;
    
    public GraffitiCanvas(Context context) {
        this(context, null);
    }

    public GraffitiCanvas(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GraffitiCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void undo() {
        if (mGraffitiOperation != null) {
            if (mGraffitiOperation.size() > 0) {
                if (mGraffitiOperation.get(mGraffitiOperation.size() - 1) == CANVAS_PAINT) {
                    if (mDrawPaths != null && mDrawPaths.size() > 0) {
                        mDrawPaths.remove(mDrawPaths.size() - 1);
                    }
                } else {
                    if (mEraserDrawPaths != null && mEraserDrawPaths.size() > 0) {
                        mEraserDrawPaths.remove(mEraserDrawPaths.size() - 1);
                    }
                }

                mGraffitiOperation.remove(mGraffitiOperation.size() - 1);
                if (getCanvasModeValue() == CANVAS_ERASER) {
                    initPaint();
                }
                redrawCanvas();
            }
        }
    }

    private void moveGraffitiImage(MotionEvent event) {
        int dx = (int) event.getX() - lastX; 
        int dy = (int) event.getY() - lastY;
        ImageView graffitiImage = ((NoteGraffiti)mContext).getGraffitiImage();

        int left = graffitiImage.getLeft() + dx; 
        int top = graffitiImage.getTop() + dy; 
        int right = graffitiImage.getRight() + dx; 
        int bottom = graffitiImage.getBottom() + dy;
        Log.e("tui", "GraffitiCanvas moveGraffitiImage  lastX = "+lastX+"  lastY = "+lastY);
        Log.e("tui", "GraffitiCanvas moveGraffitiImage  getWidth() = "+getWidth()+"  getHeight() = "+getHeight());
        Log.e("tui", "GraffitiCanvas moveGraffitiImage  graffitiImage.getWidth() = "+graffitiImage.getWidth()+"  graffitiImage.getHeight() = "+graffitiImage.getHeight());

        if (left < 0) { 
            left = 0; 
            right = left + graffitiImage.getWidth(); 
        }

        if (right > getWidth()) {
            right = getWidth();
            left = right - graffitiImage.getWidth();
        }

        if (top < 0) {
            top = 0;
            bottom = top + graffitiImage.getHeight();
        }

        if (bottom > getHeight()) {
            bottom = getHeight();
            top = bottom - graffitiImage.getHeight();
        }

        graffitiImage.layout(left, top, right, bottom);
    }

    private void setImagePosDown(int xPos, int yPos) {
        ImageView graffitiImage = ((NoteGraffiti)mContext).getGraffitiImage();
        
        int left = 0; 
        int top = 0; 
        int right = 0; 
        int bottom = 0;

        left = xPos - 80;
        if (left < 0) {
            left = 0;
        }

        top = yPos - 40;
        if (top < 0) {
            top = 0;
        }

        right = left + graffitiImage.getWidth();
        bottom = top + graffitiImage.getHeight();
        graffitiImage.layout(left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        
        if (getCanvasModeValue() == CANVAS_ERASER) {
            ImageView graffitiImage = ((NoteGraffiti)mContext).getGraffitiImage();
            eraserPaint();
        
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    Log.e("tui", "GraffitiCanvas onTouchEvent ACTION_DOWN  ");
                
                    lastX = (int) event.getX(); 
                    lastY = (int) event.getY();
                    setImagePosDown(lastX, lastY);
                    graffitiImage.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveGraffitiImage(event);
                    lastX = (int) event.getX();
                    lastY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    graffitiImage.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            PaintSetAreaWindow paintBoldWindow = ((NoteGraffiti)mContext).getPaintBoldWindow();
            paintBoldWindow.close();

            PaintSetAreaWindow paintColorWindow = ((NoteGraffiti)mContext).getPaintColorWindow();
            paintColorWindow.close();
        }
        
        return super.onTouchEvent(event);
    }
}
