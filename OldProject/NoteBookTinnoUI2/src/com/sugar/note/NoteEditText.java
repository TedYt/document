package com.sugar.note;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.ImageView;
import android.view.MotionEvent;
import android.widget.PopupWindow;


public class NoteEditText extends EditText {

    private int mode = 0;
    private Context mContext;
    private float x = 0;
    private float y = 0;
    private float upx = 0;
    private float upy = 0;


    public NoteEditText(Context context) {
        super(context);
        mContext = context;
    }

    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    public void exitSelectionActionMode() {
        stopSelectionActionMode();
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

                LinearLayout editArea = ((NoteView) mContext).getEditArea();
                int count = editArea.getChildCount();
                int i = 0;


                if (Math.abs(x - upx) < 10 && Math.abs(y - upy) < 10) {
                    PopupWindow note_pop_win = ((NoteView) mContext).getNotePopupWin();
                    if (note_pop_win != null) {
                        note_pop_win.dismiss();
                    }

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
            default:
                break;
        }

        return super.onTouchEvent(ev);
    }

}
