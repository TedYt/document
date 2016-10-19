package com.sugar.note.mylayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sugar.note.NoteView;
import com.sugar.note.R;
import com.sugar.note.handing.WriteCanvas;

/**
 * Created by yutao on 9/17/14.
 */
public class WritenWordChildLayout extends LinearLayout implements WriteCanvas.AddWordListener{

    private WriteCanvas mWriteCanvas;

    private NoteView mNoteView;

    public WritenWordChildLayout(Context context) {
        this(context, null);
    }

    public WritenWordChildLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WritenWordChildLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d("tui", "WritenWordChildLayout");
        if (context instanceof NoteView) {
            Log.d("tui", "WritenWordChildLayout, mContext instanceof NoteView");
            mNoteView = (NoteView)context;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("tui", "WritenWordChildLayout, onFinishInflate");
        WriteCanvas[] canvas = mNoteView.getWriteCanvas();
        for (WriteCanvas c : canvas) {
            c.setAddWordListener(this);
        }
    }

    @Override
    public void addWord(Bitmap bitmap) {
        Log.d("tui", "WritenWordChildlayout, addWord");
        LayoutInflater inflater = LayoutInflater.from(mNoteView);
        ImageView imageView = (ImageView)inflater.inflate(R.layout.writen_word_item, null);

        Matrix matrix = new Matrix();
        matrix.postScale(0.2f, 0.2f);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0,0,
                bitmap.getWidth(),bitmap.getHeight(),matrix, true);
        int index = getChildCount();

        imageView.setImageBitmap(resizeBmp);
        addView(imageView, index);
    }
}
