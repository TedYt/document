package com.sugar.note.mylayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.sugar.note.NoteView;
import com.sugar.note.R;

import java.util.zip.Inflater;

/**
 * Created by yutao on 9/17/14.
 */
public class WritenWordParentLayout extends LinearLayout {

    private NoteView mContext;

    public WritenWordParentLayout(Context context) {
        this(context, null);
    }

    public WritenWordParentLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WritenWordParentLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (context instanceof NoteView) {
            Log.d("tui", "WritenWordParentLayout, mContext instanceof NoteView");
            mContext = (NoteView)context;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        WritenWordChildLayout childLayout = (WritenWordChildLayout)inflater.inflate(R.layout.writen_word_child_layout, null);
        addView(childLayout);
    }

    /**
     * 用于处理手写文字
     */
    public class AddWritenWordHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bitmap b = (Bitmap)msg.obj;

            Bundle bundle = msg.getData();
            if (bundle != null) {
                int x = bundle.getInt("x");
                int y = bundle.getInt("y");
                int w = bundle.getInt("w");
                int h = bundle.getInt("h");
                Bitmap testb = Bitmap.createBitmap(b, x, y, w, h);
                //savePic(testb, "/storage/sdcard0/test1.png");
//                savePic(testb, pathForNewCameraPhotoEx("test1" + "_thum.jpg"));
//                addImage("test1");
                Log.d("tui", "WriteHandler, x = " + x + ", y = " + y + ", w = " + w + ", h = " + h);
            }
        }
    }
}
