package com.sugar.note.handing;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.sugar.note.PublicUtils;

/**
 * Created by user on 8/28/14.
 */
public class WriteCanvas extends BaseCanvas {

    private final int MSG_ADD_TO_VIEW = 100;

    private final int WORD_PADDING = 50;

    private Handler mNoteViewHandler;//用于向NoteView发送消息添加文字图片
    private MyHandler mMyHandler; //用于延迟处理

    //记录手写文字的区域大小
    private int minX = 0, minY = 0;
    private int maxX = 0, maxY = 0;

    private AddWordListener mAddWordListener;

    public interface AddWordListener{
        public void addWord(Bitmap bitmap);
        //public void addWordDone();
    }

    public WriteCanvas(Context context) {
        this(context, null);
    }

    public WriteCanvas(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WriteCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.GONE) {
            clear();
        }

        if (mMyHandler != null) {
            mMyHandler.removeMessages(MSG_ADD_TO_VIEW);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        int action = event.getAction();

        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (minX == 0 && minY == 0) {
                    minX = x;
                    minY = y;
                } else {
                    if (x < minX) {
                        minX = x;
                    }

                    if (y < minY) {
                        minY = y;
                    }
                }

                mMyHandler.removeMessages(MSG_ADD_TO_VIEW);
                break;
            case MotionEvent.ACTION_UP:
                Bitmap b = PublicUtils.convertViewToBitmap(this);
                Message msg = mMyHandler.obtainMessage();
                msg.what = MSG_ADD_TO_VIEW;
                msg.obj = b;
                //Log.d("tui", "minx = " + minX + ", miny = " + minY + ", maxx = " + maxX + ", maxy = " + maxY);

                mMyHandler.sendMessageDelayed(msg, 500);
                break;
            case MotionEvent.ACTION_MOVE:

                if (x < minX) {
                    minX = x;
                }

                if (y < minY) {
                    minY = y;
                }

                if (x > maxX) {
                    maxX = x;
                }

                if (y > maxY) {
                    maxY = y;
                }
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * 调整手写字的区域，在字的周围留出一些间隔
     */
    private void adjustXY() {
        if (minX - WORD_PADDING < 0) {
            minX = 0;
        } else {
            minX -= WORD_PADDING;
        }

        if (minY - WORD_PADDING < 0) {
            minY = 0;
        } else {
            minY -= WORD_PADDING;
        }

        if (maxX + WORD_PADDING > getCanvasViewWidth()) {
            maxX = getCanvasViewWidth();
        } else {
            maxX += WORD_PADDING;
        }

        if (maxY + WORD_PADDING > getCanvasViewHeight()) {
            maxY = getCanvasViewHeight();
        } else {
            maxY += WORD_PADDING;
        }
    }

    private void reset() {
        minX = 0;
        minY = 0;
        maxX = 0;
        maxY = 0;
    }

    public void setHandler(Handler handler) {
        mNoteViewHandler = handler;
        if (mMyHandler == null) {
            mMyHandler = new MyHandler();
        }
    }

    public void setAddWordListener(AddWordListener l) {
        Log.d("tui", "WriteCanvas, setAddWordListener");
        mAddWordListener = l;
    }

    class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Message m = mNoteViewHandler.obtainMessage();
            m.what = msg.what;
            m.obj = msg.obj;
            m.setData(msg.getData());
            if (mAddWordListener != null) {
                Log.d("tui", "mAddWordListener != null");
                Bitmap b = (Bitmap) m.obj;
                adjustXY();
                Bitmap testb = Bitmap.createBitmap(b, minX, minY, maxX - minX, maxY - minY);
                mAddWordListener.addWord(testb);
            } else {
                Log.d("tui", "Handler, mAddWordListener == null");
            }
            /*adjustXY();
            Bundle bundle = new Bundle();
            bundle.putInt("x", minX);
            bundle.putInt("y", minY);
            bundle.putInt("w", maxX - minX);
            bundle.putInt("h", maxY - minY);
            m.setData(bundle);*/

            //mNoteViewHandler.sendMessage(m);

            clear();

            removeMessages(MSG_ADD_TO_VIEW);
            reset();
        }
    }
}
