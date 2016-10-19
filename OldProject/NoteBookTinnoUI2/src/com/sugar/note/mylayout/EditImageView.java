package com.sugar.note.mylayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sugar.note.NoteView;
import com.sugar.note.R;
import com.sugar.note.handing.WriteCanvas;

/**
 * Created by user on 9/19/14.
 */
public class EditImageView extends ViewGroup implements WriteCanvas.AddWordListener {

    private NoteView mNoteView;

    private int colNum;
    private int rowNum;
    private int mLeftCor = 0;//添加手写字时，下一个字的列的坐标
    private int mTopCor = 0;//添加手写字时，下一个字的行的坐标
    private final static int VIEW_MARGIN=2;

    private int mToalHeight = 0;
    private int mLeft = 0;
    private int mTop = 0;
    private int mRight = 0;

    private final int WORD_HEIGHT = 100;
    private final int WORD_WIDTH = 100;

    public EditImageView(Context context) {
        this(context, null);
    }

    public EditImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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

        LayoutInflater inflater = LayoutInflater.from(mNoteView);
        //ImageView imageView = (ImageView)inflater.inflate(R.layout.writen_word_item, null);


        Matrix matrix = new Matrix();
        float hscale = getScale(bitmap.getHeight());
        Log.d("tui", "addWord, scale = " + hscale + ", height = " + bitmap.getHeight());
        matrix.postScale(hscale, hscale);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0,0,
                bitmap.getWidth(),bitmap.getHeight(),matrix, true);
        int index = getChildCount();

        ImageView imageView = new ImageView(mNoteView);
        int w = resizeBmp.getWidth();
        int h = resizeBmp.getHeight();
        /*if (w > WORD_WIDTH) {
            float wscale = WORD_HEIGHT * 1.0f / w ;
            matrix.postScale(wscale, 1.0f);
            resizeBmp = Bitmap.createBitmap(bitmap, 0,0,
                    resizeBmp.getWidth(),resizeBmp.getHeight(),matrix, true);
        }*/

        imageView.setLayoutParams(new EditImageView.LayoutParams(w, h));

        imageView.setImageBitmap(resizeBmp);
        addView(imageView, index);

        mLeftCor += w;
        mTopCor += h;
    }

    private float getScale(int length) {

        return WORD_HEIGHT * 1.0f / length ;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("tui", "onSizeChanged w = "+w+", h = "+h);
    }

    @Override
    public void addView(View child) {

        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
    }


    @Override
    protected void onLayout(boolean arg0, int left, int top, int right, int bottom) {
        Log.d("tui", "onLayout ");
        final int count = getChildCount();
        Log.d("lmj","count = "+count);
        mLeft = left;
        mTop = top;
        mRight = right;

        int row = 1;// which row lay you view relative to parent
        int lengthX = left;    // right position of child relative to parent
        int lengthY = top;    // bottom position of child relative to parent

        for (int i = 0; i < count; i++) {

            final View child = this.getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            lengthX += width + VIEW_MARGIN;
            lengthY = row * (height + VIEW_MARGIN) ;//+ VIEW_MARGIN + height;
            //if it can't drawing on a same line , skip to next line
            if (lengthX > right) {
                lengthX = left + width;
                row++;
                lengthY = row * (WORD_HEIGHT + VIEW_MARGIN) ;//+ VIEW_MARGIN + ROW_HEIGHT;

            }
            child.layout(lengthX - width, lengthY - height, lengthX, lengthY);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("tui", "onMeasure");
        int row = 0;// which row lay you view relative to parent
        int lengthX = mLeft;    // right position of child relative to parent
        int lengthY = mTop;    // bottom position of child relative to parent

        for (int index = 0; index < getChildCount(); index++) {
            final View child = getChildAt(index);

            // measure
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            lengthX += childWidth + VIEW_MARGIN;
            lengthY = row * (WORD_HEIGHT + VIEW_MARGIN) + VIEW_MARGIN + WORD_HEIGHT + mTop;

            //if it can't drawing on a same line , skip to next line
            if (lengthX > mRight) {
                lengthX = childWidth + VIEW_MARGIN + mLeft;
                row++;
                lengthY = row * (WORD_HEIGHT + VIEW_MARGIN) + VIEW_MARGIN + WORD_HEIGHT + mTop;
            }
        }

        mToalHeight = lengthY - mTop;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(mToalHeight, heightMeasureSpec));
    }
}
