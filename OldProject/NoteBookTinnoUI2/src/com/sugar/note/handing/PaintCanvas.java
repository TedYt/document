package com.sugar.note.handing;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

/**
 * Created by user on 8/28/14.
 */
public class PaintCanvas extends BaseCanvas {
    public PaintCanvas(Context context) {
        super(context);
    }

    public PaintCanvas(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaintCanvas(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
