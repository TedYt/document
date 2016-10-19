package com.sugar.note;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.view.inputmethod.InputMethodManager;
import android.app.Activity;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

public class NoteScrollView extends ScrollView {

    private int mode = 0;
    private Context mContext;
    private float x = 0;
    private float y = 0;
    private float upx = 0;
    private float upy = 0;

    private OnResizeListener mResizeListener;

    public interface OnResizeListener{
        void OnResize(int w, int h, int oldw, int oldh);
    }

    public NoteScrollView(Context context) {
        super(context);
        mContext = context;
    }

    public NoteScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public NoteScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void setOnResizeListener(OnResizeListener l){
        mResizeListener = l;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mResizeListener != null){
            mResizeListener.OnResize(w,h,oldw,oldh);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

	  switch (ev.getAction()) {

		case MotionEvent.ACTION_DOWN:
		      x = ev.getX();
		      y = ev.getY();
                      break;
		case MotionEvent.ACTION_UP:
		      upx = ev.getX();
		      upy = ev.getY();
		      
		      if (Math.abs(x - upx) < 10 && Math.abs(y - upy) < 10)
		      {
                          InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		          imm.showSoftInput(((Activity) mContext).getCurrentFocus(), InputMethodManager.SHOW_IMPLICIT);
                          PopupWindow note_pop_win = ((NoteView) mContext).getNotePopupWin();
                          if (note_pop_win != null) {
                              note_pop_win.dismiss();
                          }
                          LinearLayout editArea = ((NoteView) mContext).getEditArea();
                          int count = editArea.getChildCount();
                          int i = 0;
                          
                          for (i = 0; i < count; i++) {
                              View v = editArea.getChildAt(i);
                              if (v instanceof ImageEditor) {
                                  ImageView delete = ((ImageEditor) v).getDeleteView();
                                  if (delete.getVisibility() == View.VISIBLE) {
                                      delete.setVisibility(View.INVISIBLE);
                                  }
                              }
                          }
		      }
                      break;
		case MotionEvent.ACTION_MOVE:
                      break;
		case MotionEvent.ACTION_POINTER_DOWN:
                      break;
		case MotionEvent.ACTION_POINTER_UP:
                      break;
	 }
	  
	 return super.onTouchEvent(ev);
    }

}
