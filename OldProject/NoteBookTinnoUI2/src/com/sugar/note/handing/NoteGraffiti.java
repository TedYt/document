package com.sugar.note.handing;

import android.app.Activity;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.net.Uri;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.os.Bundle;

import com.sugar.note.R;
import com.sugar.note.handing.GraffitiCanvas;
import android.app.ActionBar;
import android.view.LayoutInflater;
import com.sugar.note.handing.BaseCanvas;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;


public class NoteGraffiti extends Activity implements OnClickListener {
    private Context mContext;
    private Uri mUri;
    private Button mUndoBtn;
    private Button mColorBtn;
    private Button mThicknessBtn;
    private Button mPaintEraserBtn;
    private Button mClearBtn;
    //private LinearLayout mColorLinear;
    //private LinearLayout mThicknessLinear;
    private GraffitiCanvas mGraffitiCanvas;
    private ImageButton mBtnBack;
    private ImageButton mBtnSaveShare;
    private boolean isPaint = true;
    private FrameLayout mGraffitiFrame;
    private ImageView mGraffitiImage;
    private PaintSetAreaWindow mPaintBoldWindow;
    private PaintSetAreaWindow mPaintColorWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_graffiti);
        mContext = this;

        mUndoBtn = (Button) findViewById(R.id.btn_undo);
        mUndoBtn.setOnClickListener(this);
        
        mColorBtn = (Button) findViewById(R.id.btn_color);
        mColorBtn.setOnClickListener(this);

        mThicknessBtn = (Button) findViewById(R.id.btn_thickness);
        mThicknessBtn.setOnClickListener(this);

        mPaintEraserBtn = (Button) findViewById(R.id.btn_paint_eraser);
        mPaintEraserBtn.setOnClickListener(this);
 
        mClearBtn = (Button) findViewById(R.id.btn_clear);
        mClearBtn.setOnClickListener(this);

        //mColorLinear = (LinearLayout) findViewById(R.id.menu_bar_color);
        //mThicknessLinear = (LinearLayout) findViewById(R.id.menu_bar_thickness);
        mGraffitiCanvas = (GraffitiCanvas) findViewById(R.id.note_graffiti_canvas);
        
        mGraffitiFrame = (FrameLayout) findViewById(R.id.graffiti_frame);

        mGraffitiImage = (ImageView) findViewById(R.id.graffiti_circle);
        
        initActionBar();
        
        mPaintBoldWindow = new PaintSetAreaWindow(this);
        mPaintBoldWindow.initWindow(this, mGraffitiCanvas, getWindowManager(), PaintSetAreaWindow.PAINT_BOLD);

        mPaintColorWindow = new PaintSetAreaWindow(this);
        mPaintColorWindow.initWindow(this, mGraffitiCanvas, getWindowManager(), PaintSetAreaWindow.PAINT_COLOR);
    }

    public ImageView getGraffitiImage() {
        return mGraffitiImage;
    }

    public PaintSetAreaWindow getPaintBoldWindow() {
        return mPaintBoldWindow;
    }

    private void togglePaintBoldArea(View anchor) {
        mPaintBoldWindow.toggle(anchor);
        mPaintColorWindow.close();
    }

    public PaintSetAreaWindow getPaintColorWindow() {
        return mPaintColorWindow;
    }

    private void togglePaintColorArea(View anchor) {
        mPaintColorWindow.toggle(anchor);
        mPaintBoldWindow.close();
    }

    private void initActionBar(){
        ActionBar bar = getActionBar();
        
        if (bar != null) {
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(R.layout.graffiti_actionbar, null);
           

            mBtnBack = (ImageButton)view.findViewById(R.id.btn_back);
            mBtnBack.setOnClickListener(this);

            mBtnSaveShare = (ImageButton)view.findViewById(R.id.btn_save_share);
            mBtnSaveShare.setOnClickListener(this);
            bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(view);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("notedb", "andy3344 NoteGraffiti onResume  ");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("notedb", "andy3344 NoteGraffiti onDestroy  ");
    }

    private void setBtnEnabled(boolean isEnable) {
        //mUndoBtn.setEnabled(isEnable);
        mColorBtn.setEnabled(isEnable);
        mThicknessBtn.setEnabled(isEnable);
        mClearBtn.setEnabled(isEnable);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Log.e("tui", "NoteGraffiti  onClick id = "+id);

        switch (id){
            case R.id.btn_undo:
                mGraffitiCanvas.undo();
                break;
            case R.id.btn_color:
                togglePaintColorArea(v);
                break;
            case R.id.btn_thickness:
                togglePaintBoldArea(v);
                break;
            case R.id.btn_paint_eraser:
                if (isPaint) {
                    mGraffitiCanvas.eraserPaint();
                    mGraffitiCanvas.setCanvasModeValue(BaseCanvas.CANVAS_ERASER);
                    mPaintEraserBtn.setText(R.string.menu_paint);
                    //mGraffitiCanvas.clearEraserDrawPaths();
                    setBtnEnabled(false);
                    isPaint = false;
                }
                else {
                    mGraffitiCanvas.initPaint();
                    mGraffitiCanvas.setCanvasModeValue(BaseCanvas.CANVAS_PAINT);
                    mPaintEraserBtn.setText(R.string.menu_eraser);
                    setBtnEnabled(true);
                    isPaint = true;
                }
                break;
            case R.id.btn_clear:
                Log.e("tui", "NoteGraffiti  onClick btn_undo ");
                mGraffitiCanvas.clear();
                break;
            
            default:
                
                break;
        }
    }

}
