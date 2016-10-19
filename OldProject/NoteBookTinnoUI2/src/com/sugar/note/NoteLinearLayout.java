package com.sugar.note;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;



public class NoteLinearLayout extends LinearLayout {

    public NoteLinearLayout(Context context) {
        super(context);
    }

    public NoteLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoteLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int counts = ev.getPointerCount();

	switch (ev.getAction()) {

		case MotionEvent.ACTION_DOWN:
                      break;
		case MotionEvent.ACTION_UP:
                      break;
		case MotionEvent.ACTION_MOVE:
                      break;
		case MotionEvent.ACTION_POINTER_DOWN:
                      break;
		case MotionEvent.ACTION_POINTER_UP:
                      break;
	}

	boolean flag = super.onInterceptTouchEvent(ev);
        if (counts >= 2) {
            flag = true;
	}
	 
        return flag;
    }

}
