/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.sugar.note;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.ContactsContract.DisplayPhoto;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sugar.note.NotePad.Notes;
import com.sugar.note.handing.NoteGraffiti;
import com.sugar.note.handing.PaintSetAreaWindow;
import com.sugar.note.handing.WriteCanvas;
import com.sugar.note.mylayout.EditImageView;
import com.sugar.note.mylayout.WritenWordParentLayout;
import com.sugar.note.picture.ImageDownloader;
import com.sugar.note.picture.PictureSelectActivity;
import com.sugar.note.picture.StorageMountedReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.view.MotionEvent; 
import android.util.FloatMath;
import android.view.View.OnTouchListener;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.text.TextWatcher;
import android.text.Editable;

import android.app.AlarmManager;
import android.app.PendingIntent;

import android.database.ContentObserver;
import android.app.ProgressDialog;

import java.util.ArrayList;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;

import java.text.ParseException;

import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.app.Dialog;
import android.widget.PopupMenu;
import android.view.MenuItem;
import com.sugar.note.picture.SavePreShareTask;


public class NoteView extends Activity implements OnClickListener,
            NoteScrollView.OnResizeListener, StorageMountedReceiver.MountedChangeListener {
    private Context mContext;
    private String mNotegroup;
    private Cursor mCursor;
    private Uri mUri;
    private Toast mMaxNoteToast = null;
    private int mMaxLength = 1501;

    private Button mAlarmBtn;
    private LinearLayout mEditArea;

    private Button mTakePhoto;
    private Button mPickPhoto;

    private LayoutInflater mInflater;
    private Resources mResource;
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private static int mWidth;
    private static int mHeight;
    
    private ImageButton mselectcol;
    public View mNoteBgColorSelector; //the color select view
    private ImageButton mBtnMore;
	public View mNoteShareDelete;
    public static final int BLUE = 0;
    public static final int GREEN = 1;
    public static final int RED = 2;
    public static final int YELLOW = 3;
    public int mCurrentWorkingcol;
    public int mOldWorkingcol;
    private static final Map<Integer, Integer> sBgSelectorBtnsMap = new HashMap<Integer, Integer>();

    static {
        sBgSelectorBtnsMap.put(R.id.iv_bg_blue, BLUE);
        sBgSelectorBtnsMap.put(R.id.iv_bg_green, GREEN);
        sBgSelectorBtnsMap.put(R.id.iv_bg_red, RED);
        sBgSelectorBtnsMap.put(R.id.iv_bg_yellow, YELLOW);
    }

    private static final Map<Integer, Integer> sBgSelectorSelectionMap = new HashMap<Integer, Integer>();

    static {
        sBgSelectorSelectionMap.put(BLUE, R.id.iv_bg_blue_select);
        sBgSelectorSelectionMap.put(GREEN, R.id.iv_bg_green_select);
        sBgSelectorSelectionMap.put(RED, R.id.iv_bg_red_select);
        sBgSelectorSelectionMap.put(YELLOW, R.id.iv_bg_yellow_select);
    }

    private static final Map<Integer, Integer> sBgworkcol = new HashMap<Integer, Integer>();

    static {
        sBgworkcol.put(BLUE, R.drawable.edit_blue);
        sBgworkcol.put(GREEN, R.drawable.edit_green);
        sBgworkcol.put(RED, R.drawable.edit_red);
        sBgworkcol.put(YELLOW, R.drawable.edit_yellow);
    }

    private static final Map<Integer, Integer> sEditAreaBgworkcol = new HashMap<Integer, Integer>();

    static {
        sEditAreaBgworkcol.put(BLUE, R.drawable.edit_area_blue);
        sEditAreaBgworkcol.put(GREEN, R.drawable.edit_area_green);
        sEditAreaBgworkcol.put(RED, R.drawable.edit_area_red);
        sEditAreaBgworkcol.put(YELLOW, R.drawable.edit_area_yellow);
    }

    private RelativeLayout mScroll;
    private NoteLinearLayout center2;

    private final String NBFontsizePrefName = "notebook_fontsize_sharedpreference";
    private SharedPreferences NBFontsizePref = null;
    private final String NBFontsizePrefKey = "notebook_fontsize_prefkey";
    private float curtextSize = 20.0f;

    private NoteScrollView mScrollView;

    private ImageButton mbtnback;
    private long mAlertDate = 0;

    private boolean AlarmSave = false;
    private boolean AlarmSaveFlag = false;

    //private Button mDeleteBtn;
    private Button mNoteBgBtn;
    private ProgressDialog mDeleting;
    private String sWhere;

    private ImageDownloader mImageDownLoader;

    
    private int mRecordCount = 0;
    private int mSortIndex = 0;
    private int mIsTop = 0;
    private int mNoteID = 0;
    private ArrayList<NoteItem> notelist;
    private int mTopNoteCount = 0;
    private int mContentCount = 0;

    private static final String PHOTO_DATE_FORMAT = "'IMG'_yyyyMMdd_HHmmss";
    public static final int REQUEST_CODE_CAMERA_WITH_DATA = 1001;
    public static final int REQUEST_CODE_PHOTO_PICKED_WITH_DATA = 1002;
    public static final int REQUEST_CODE_NOTEALERT_WITH_DATA = 1003;
    public static final int REQUEST_CODE_NOTEALARM_UPDATE = 1004;
    public static final int REQUEST_CODE_TEST = 1005;
    public static final int NOTE_ALARM_ALERT_NUM  = 3;
    private String mCurrentPhotoFile;
    private final int mPhotoPickSize = 260;//50700;// = getPhotoPickSize();
    private String mNote = "";

    private PopupWindow mPopupWin;
    private final int ALARM_FLAG_NEW = 0;
    private final int ALARM_FLAG_UPDATE = 1;

    private StorageMountedReceiver mReceiver;

    private final int TASK_LOAD_CONTENT = 0;
    private final int TASK_LOAD_IMAGE = 1;
    private final int TASK_UPDATE_IMAGE = 2;
    private final int TASK_TOTAL = 3;
    private ArrayList<AsyncTask<String, Object, Boolean>> mTasks;
    private View mWriteCanvasLayout;
    private View mBtnArea;
    private PaintSetAreaWindow mPaintBoldWindow;
    private WritenWordParentLayout mWritenWordParentLayout;
    private Button mNoteShareBtn;
    private Button mNoteDeleteBtn;

    private ContentObserver mCobserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (NotePad.Notes.sDeleteFlag &&
                    mDeleting != null &&
                    NotePad.Notes.sDeleteNum == 1) {

                mDeleting.cancel();
                Intent it = new Intent();
                it.putExtra("UpdateNoteFlag", true);
                setResult(RESULT_OK, it);
                finish();
                NotePad.Notes.sDeleteFlag = false;
            }
        }
    };

    private BroadcastReceiver mNoteAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PublicUtils.ACTION_NOTEALARM_ALERT)) {
                ShowAlertTime(-2);
            }
        }
    };

    public LinearLayout getEditArea() {
        return mEditArea;
    }

    private void init() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        //Display dis = getWindowManager().getDefaultDisplay();
        mWidth = dm.widthPixels;//dis.get
        mHeight = dm.heightPixels;//dis.getHeight();

        mInflater = LayoutInflater.from(mContext);
        mResource = getResources();


        Log.e("note", "@onCreate mWidth = " + mWidth + ", mHeight = " + mHeight);

        mScroll = (RelativeLayout) findViewById(R.id.textnote);

        mScrollView = (NoteScrollView) findViewById(R.id.main_area_scroll);
	    mScrollView.setOnTouchListener(new NoteScrollTouchListenter());
        mScrollView.setOnResizeListener(this);

        mScroll.setBackgroundResource(R.drawable.background);
        center2 = (NoteLinearLayout) findViewById(R.id.center2);

        mEditArea = (LinearLayout) findViewById(R.id.main_area);

        mTakePhoto = (Button) findViewById(R.id.take_photo);
        mTakePhoto.setOnClickListener(this);

        mPickPhoto = (Button) findViewById(R.id.pick_photo);
        mPickPhoto.setOnClickListener(this);

        mbtnback = (ImageButton) findViewById(R.id.btn_back);
        mbtnback.setOnClickListener(this);

        mAlarmBtn = (Button) findViewById(R.id.go_alarm_btn);
        mAlarmBtn.setOnClickListener(this);
   /*
        mDeleteBtn = (Button) findViewById(R.id.go_delete_btn);
        mDeleteBtn.setOnClickListener(this);
   */
        mNoteBgBtn = (Button) findViewById(R.id.go_note_bg_btn);
        mNoteBgBtn.setOnClickListener(this);

        mNoteShareBtn = (Button) findViewById(R.id.btn_note_share);
        mNoteShareBtn.setOnClickListener(this);

        mNoteDeleteBtn = (Button) findViewById(R.id.btn_note_delete);
        mNoteDeleteBtn.setOnClickListener(this);

        mBtnMore = (ImageButton) findViewById(R.id.btn_more);
        mBtnMore.setOnClickListener(this);

        mBtnArea = findViewById(R.id.menu_bar);
        mWriteCanvasLayout = findViewById(R.id.write_canvas_layout);
        initWriteCanvasBtnArea();

        //mWritenWordParentLayout = (WritenWordParentLayout)mInflater.inflate(R.layout.writen_word_parent_layout, null);
        //mEditArea.addView(mWritenWordParentLayout);

        //EditImageView editImageView = (EditImageView)mInflater.inflate(R.layout.writen_word_framlayout, mEditArea, false);
        //mEditArea.addView(editImageView);


        //初始化笔触粗细选择区域
        mPaintBoldWindow = new PaintSetAreaWindow(this);
        WriteCanvas wc = (WriteCanvas)mWriteCanvasLayout.findViewById(R.id.write_single_area);
        wc.setHandler(new WriteHandler());
        mPaintBoldWindow.initWindow(this, wc, getWindowManager(), PaintSetAreaWindow.PAINT_BOLD);

        ((WriteCanvas)findViewById(R.id.write_double_area_write1)).setHandler(new WriteHandler());
        ((WriteCanvas)findViewById(R.id.write_double_area_write2)).setHandler(new WriteHandler());

        mNoteBgColorSelector = (View) findViewById(R.id.note_bg_color_selector);
        for (int id : sBgSelectorBtnsMap.keySet()) {
            ImageView iv = (ImageView) findViewById(id);
            iv.setOnClickListener(this);
        }

        mNoteShareDelete = (View) findViewById(R.id.menu_share_delete);

        center2.setOnTouchListener(new ZoomListenter());

         View vtemp = mEditArea.getChildAt(0);
         if(vtemp instanceof NoteEditText) {
              ((NoteEditText) vtemp).setOnTouchListener(new ScribingListenter());
	      ((NoteEditText) vtemp).addTextChangedListener(mTextWatcher);
	      ((NoteEditText) vtemp).setOnKeyListener(new NBEditTextOnKeyListenter());
         }


        getContentResolver().registerContentObserver(NotePad.Notes.CONTENT_URI,
                true,
                mCobserver);
    }

    private void initWriteCanvasBtnArea() {
        Button b = (Button)findViewById(R.id.wc_btn_collapse);
        b.setOnClickListener(this);

        b = (Button)findViewById(R.id.wc_btn_sd);
        b.setOnClickListener(this);

        b = (Button)findViewById(R.id.wc_btn_space);
        b.setOnClickListener(this);

        b = (Button)findViewById(R.id.wc_btn_bold);
        b.setOnClickListener(this);

        b = (Button)findViewById(R.id.wc_btn_delete);
        b.setOnClickListener(this);
    }

    public WriteCanvas[] getWriteCanvas() {

        WriteCanvas[] canvas = new WriteCanvas[3];
        canvas[0] = (WriteCanvas)findViewById(R.id.write_single_area);
        canvas[1] = (WriteCanvas)findViewById(R.id.write_double_area_write1);
        canvas[2] = (WriteCanvas)findViewById(R.id.write_double_area_write2);

        return canvas;
    }

    @Override
    public void OnResize(int w, int h, int oldw, int oldh) {
        closePopupWin();
    }

    @Override
    public void onMountedChanged(String state) {
        if (state.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            String[] names = new String[mEditArea.getChildCount()];
            for (int i = 0; i < mEditArea.getChildCount(); i++) {
                View child = mEditArea.getChildAt(i);
                if (child instanceof ImageEditor) {
                    ImageEditor editor = (ImageEditor) child;
                    names[i] = editor.getPhoto();
                } else {
                    names[i] = null;
                }
            }
            UpdateImageTask task = new UpdateImageTask();
            task.execute(names);
            mTasks.set(TASK_UPDATE_IMAGE, task);
        } else if (state.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            for (int i = 0; i < mEditArea.getChildCount(); i++) {
                View child = mEditArea.getChildAt(i);
                if (child instanceof ImageEditor) {
                    ImageView image = (ImageView)child.findViewById(R.id.thumb_image);
                    TextView text = (TextView)child.findViewById(R.id.image_hint);
                    text.setVisibility(View.VISIBLE);
                    image.setImageResource(R.drawable.note_image_error);
                }
            }
        }
    }

    public class NoteScrollTouchListenter implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (mWriteCanvasLayout.getVisibility() == View.VISIBLE) {
                closeWriteCanvas();
            }

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_UP:
	            NoteEditText et_focused = (NoteEditText)mEditArea.getFocusedChild();
	            if (et_focused != null && et_focused.hasSelection()) {
		        et_focused.exitSelectionActionMode();
	            }
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
            }
            return false;
        }
    }
    
    public class ZoomListenter implements OnTouchListener {

        private int mode = 0;
        float oldDist = 0;
        float textSize = 0;


        @Override
        public boolean onTouch(View v, MotionEvent event) {


            int count = mEditArea.getChildCount();

            int pCounts = event.getPointerCount();

            if (textSize == 0) {
                textSize = curtextSize;
            }
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:

                    mode = 1;
                    break;
                case MotionEvent.ACTION_UP:
                    oldDist = 0;
                    mode = 0;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    oldDist = 0;
                    mode -= 1;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:

                    oldDist = spacing(event);
                    mode += 1;
                    break;

                case MotionEvent.ACTION_MOVE:
                    //if (mode >= 2) {
                    if (pCounts >= 2) {
                        float newDist = spacing(event);
                        if (oldDist == 0) {
                            oldDist = newDist;
                        }
                        if (newDist > oldDist + 1) {
                            zoom(newDist / oldDist);
                            oldDist = newDist;
                        }
                        if (newDist < oldDist - 1) {
                            zoom(newDist / oldDist);
                            oldDist = newDist;
                        }
                    }
                    break;
            }
            return false; //false
        }

        private void zoom(float f) {
            int count = mEditArea.getChildCount();

            float newtextSize = 0;
            newtextSize = textSize * f;
            for (int j = count - 1; j >= 0; j--) {
                View v = mEditArea.getChildAt(j);
                if (v instanceof NoteEditText) {
                    if (newtextSize > 10 && newtextSize < 60) {
                        ((NoteEditText) v).setTextSize(newtextSize);
                        textSize = newtextSize;
                    } else if (newtextSize <= 10) {
                        ((NoteEditText) v).setTextSize(10);
                        textSize = 10;
                    } else if (newtextSize >= 60) {
                        ((NoteEditText) v).setTextSize(60);
                        textSize = 60;
                    }
                }
            }
            curtextSize = textSize;
        }

        private float spacing(MotionEvent event) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return FloatMath.sqrt(x * x + y * y);
        }
    }


    /**
     *处理加载某个NoteView内容
     */
    public class LoadContentTask extends AsyncTask<String, Object, Boolean>{
        private int loadContent_Count = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            Object v1 = values[0];
            Object v2 = values[1];

            loadContent_Count = loadContent_Count + 1;
            if ((v1 != null && v1 instanceof ImageEditor) &&
                    (v2 != null && v2 instanceof Bitmap)){
                Log.d("tui", "onProgressUpdate, PictureContent");
                ImageEditor layout = (ImageEditor)v1;
                Bitmap bitmap = (Bitmap)v2;

                ImageView image = (ImageView) layout.findViewById(R.id.thumb_image);
                ImageView delete = (ImageView) layout.findViewById(R.id.btn_delete);
                TextView hint = (TextView)layout.findViewById(R.id.image_hint);
                image.setImageBitmap(bitmap);
                delete.setVisibility(View.INVISIBLE);
                if (!PublicUtils.isStorageMounted()) {
                    hint.setVisibility(View.VISIBLE);
                } else {
                    hint.setVisibility(View.INVISIBLE);
                }

                int index = mEditArea.getChildCount();
                mEditArea.addView(layout, index);
            }else if (v1 != null && v1 instanceof String) {
                Log.d("tui", "onProgressUpdate, TextContent");
                String s1 = (String)values[0];
                String s2 = (String)values[1];
                Log.d("tui", "enter onProgressUpdate, s1 = " + s1 +", s2 = "+s2);
                NoteEditText editText = initNoteEditText();

                if (s2.equals("TEXT__SCR__TRUE__")) {
                    editText.setText(s1.substring(17));
                    editText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                } else if (s2.equals("TEXT__SCR__FALSE__")) {
                    editText.setText(s1.substring(18));
                    editText.setPaintFlags(0);
                }

                editText.setTextSize(curtextSize);
            }
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d("tui", "enter doInBackground, count = " + strings.length);

            int length = strings.length;
            for (int i = 1; i < length - 1; i = i + 2) {
                String s1 = strings[i];
                String s2 = strings[i + 1];
                Log.d("tui", "enter doInBackground, s1 = " + s1 +", s2 = "+s2);
                if (isPictureContent(s1, s2)) {
                    Log.d("tui", "isPictureContent");
                    String imageName = s1.substring(7);
                    Bitmap b = getContentImage(imageName);
                    ImageEditor layout = initImageLayout(imageName);

                    publishProgress(layout, b);//参数的顺序不要改变
                }else if (isTextContent(s1, s2)) {
                    Log.d("tui", "isTextContent");
                    publishProgress(s1, s2);
                }
            }

            return null;
        }

        private Bitmap getContentImage(String imageName) {
            Bitmap b = null;

            if (PublicUtils.isStorageMounted()) {
                b = BitmapFactory.decodeFile(pathForNewCameraPhotoEx(imageName + "_thum.jpg"));
                if (b == null) {
                    b = BitmapFactory.decodeFile(pathForNewCameraPhotoEx(imageName + ".jpg"));
                }

            } else {
                b = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.note_image_error);
            }

            return b;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            int etArea_count = mEditArea.getChildCount();
            Log.e("tui", "onPostExecute etArea_count = " + etArea_count);
            View child = mEditArea.getChildAt(etArea_count - 1);
            if (child instanceof ImageEditor) {
                NoteEditText ne = (NoteEditText) mInflater.inflate(
                        R.layout.edit_in_textactivity, null);
                mEditArea.addView(ne, etArea_count);
                ne.setOnTouchListener(new ScribingListenter());
                ne.addTextChangedListener(mTextWatcher);
                ne.setOnKeyListener(new NBEditTextOnKeyListenter());
                ne.setFocusable(true);
                ne.setFocusableInTouchMode(true);
                ne.requestFocus();
                ne.setTextSize(curtextSize);
            } else if (child instanceof NoteEditText) {
                NoteEditText ne = (NoteEditText) child;
                String text = ne.getText().toString();
                if (text != null) {
                    int len = text.length();
                    ne.setFocusable(true);
                    ne.setFocusableInTouchMode(true);
                    ne.requestFocus();
                    ne.setSelection(len);
                    ne.setTextSize(curtextSize);
                }
            }
        }

        private NoteEditText initNoteEditText() {
            int count = mEditArea.getChildCount();

            NoteEditText et = null;
            if (count == 1 && loadContent_Count == 1) {//默认的edittext
                View v = mEditArea.getChildAt(0);
                if (v instanceof NoteEditText) {
                    et = (NoteEditText) v;
                }
            }else {
                et = (NoteEditText) mInflater.inflate(R.layout.edit_in_textactivity, null);
                et.setOnTouchListener(new ScribingListenter());
                et.addTextChangedListener(mTextWatcher);
                et.setOnKeyListener(new NBEditTextOnKeyListenter());
                mEditArea.addView(et, count);
            }

            return et;
        }

        /**
         *
         * @param imageName
         * @return
         */
        private ImageEditor initImageLayout(String imageName) {
            ImageEditor layout = (ImageEditor) mInflater.inflate(R.layout.image_layout, null);
            ImageView image = (ImageView) layout.findViewById(R.id.thumb_image);
            final ImageView delete = (ImageView) layout.findViewById(R.id.btn_delete);

            image.setOnClickListener(new ContentPhotoClickListener(imageName));
            image.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (delete.getVisibility() == View.INVISIBLE) {
                        delete.setVisibility(View.VISIBLE);
                    } else {
                        delete.setVisibility(View.INVISIBLE);
                    }

                    setDeleteImgInvisible(v);
                    return true;
                }
            });

            layout.setPhoto(imageName);
            layout.setDeleteView(delete);

            delete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    del_note_image(delete);
                }
            });

            return layout;
        }

        /**
         *
         * @param s1
         * @param s2
         * @return
         */
        private boolean isPictureContent(String s1, String s2) {
            return s1 != null && s1.startsWith("PHOTO__") && !s1.equals("PHOTO__") && s2.equals("PHOTO__");
        }

        /**
         *
         * @param s1
         * @param s2
         * @return
         */
        private boolean isTextContent(String s1, String s2) {
            return (s1 != null &&
                    (
                     (s1.startsWith("TEXT__SCR__TRUE__") && s2.equals("TEXT__SCR__TRUE__")) ||
                     (s1.startsWith("TEXT__SCR__FALSE__") && s2.equals("TEXT__SCR__FALSE__"))
                    )
                   );
        }
    }

    /**
     * 处理从相册选择图片，向NoteView添加图片的任务
     */
    class LoadImageTask extends AsyncTask<String, Object, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            Object v1 = values[0];
            Object v2 = values[1];

            if (v1 instanceof ImageEditor && v2 instanceof Bitmap) {
                ImageEditor layout = (ImageEditor) v1;
                Bitmap bitmap = (Bitmap) v2;

                ImageView image = (ImageView) layout.findViewById(R.id.thumb_image);
                ImageView delete = (ImageView) layout.findViewById(R.id.btn_delete);
                image.setImageBitmap(bitmap);
                delete.setVisibility(View.INVISIBLE);

                int index = mEditArea.indexOfChild(mEditArea.getFocusedChild());

                View child = mEditArea.getChildAt(index);
                int newIndex = 0;
                if (child instanceof NoteEditText) {
                    String text = ((NoteEditText) child).getText().toString();
                    Log.e("notedb", "andy339 addImage text = " + text);
                    mEditArea.addView(layout, index + 1);
                    newIndex = index + 2;
                }

                NoteEditText et = initEdiText();
                mEditArea.addView(et, newIndex);

                initOther();
            } else if (v2 == null) {
                Toast.makeText(NoteView.this,
                        R.string.picture_is_error_picture,
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            for (int i = 0; i< strings.length; i ++){
                String path = strings[i];
                Log.d("tui", "LoadImageTask, doInBackground, " + path);

                Bitmap b = getCompressBitmap(path);
                savePicToStorage(b, path);

                String name = getFileNameFromPath(path);
                ImageEditor layout = initImageLayout(name);

                publishProgress(layout, b);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            String text = mContext.getString(R.string.picture_load_image_done);
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT)
                 .show();
        }

        private Bitmap getCompressBitmap(String path) {
            int reqWidth = getResources().getDimensionPixelSize(R.dimen.noteview_image_view_width);
            int reqHeight = getResources().getDimensionPixelSize(R.dimen.noteview_image_view_height);

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = PublicUtils.getInSampleSize(options,reqWidth,reqHeight);
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);

            return bitmap;
        }

        private void savePicToStorage(Bitmap b, String path) {
            String fileName = getFileNameFromPath(path);
            String savePath = pathForNewCameraPhotoEx(fileName + "_thum.jpg");
            savePic(b, savePath);
        }

        private ImageEditor initImageLayout(String imageName) {
            ImageEditor layout = (ImageEditor) mInflater.inflate(R.layout.image_layout, null);
            ImageView image = (ImageView) layout.findViewById(R.id.thumb_image);
            final ImageView delete = (ImageView) layout.findViewById(R.id.btn_delete);

            image.setOnClickListener(new ContentPhotoClickListener(imageName));
            image.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (delete.getVisibility() == View.INVISIBLE) {
                        delete.setVisibility(View.VISIBLE);
                    } else {
                        delete.setVisibility(View.INVISIBLE);
                    }

                    setDeleteImgInvisible(v);
                    return true;
                }
            });

            layout.setPhoto(imageName);
            layout.setDeleteView(delete);

            delete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    del_note_image(delete);
                }
            });

            return layout;
        }

        private void initOther() {
            if (getNoteContentStatus() == false) {
                mAlarmBtn.setEnabled(false);
            } else {
                mAlarmBtn.setEnabled(true);
            }
            AlarmSave = false;
            NotePad.Notes.sSaveNoteFlag = false;
        }

        private NoteEditText initEdiText() {
            NoteEditText et = (NoteEditText) mInflater.inflate(R.layout.edit_in_textactivity, null);

            et.setOnTouchListener(new ScribingListenter());
            et.addTextChangedListener(mTextWatcher);
            et.setOnKeyListener(new NBEditTextOnKeyListenter());
            et.setTextSize(curtextSize);
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();

            return et;
        }
    }

    /**
     * 处理存储器挂载和卸载时，图片的不同处理
     */
    class UpdateImageTask extends AsyncTask<String, Object, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            int index = (Integer)values[0];
            Bitmap b = (Bitmap)values[1];

            ImageEditor editor = (ImageEditor)mEditArea.getChildAt(index);
            ImageView image = (ImageView)editor.findViewById(R.id.thumb_image);
            TextView text = (TextView)editor.findViewById(R.id.image_hint);

            image.setImageBitmap(b);
            text.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            for (int i = 0; i < strings.length; i++) {
                if (strings[i] == null){
                    continue;
                }

                Bitmap b = BitmapFactory.decodeFile(pathForNewCameraPhotoEx(strings[i] + "_thum.jpg"));
                publishProgress(i, b);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
        }
    }

    /**
     * 用于处理手写文字
     */
    class WriteHandler extends Handler{
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
                savePic(testb, pathForNewCameraPhotoEx("test1" + "_thum.jpg"));
                addImage("test1");
                Log.d("tui", "WriteHandler, x = " + x + ", y = " + y + ", w = " + w + ", h = " + h);
            }
        }
    }

    class ContentPhotoClickListener implements OnClickListener{

        private String mImageName;

        public ContentPhotoClickListener(String mImageName) {
            this.mImageName = mImageName;
        }

        @Override
        public void onClick(View view) {
            setDeleteImgInvisible(view);
            File file = new File(pathForNewCameraPhotoEx(mImageName + ".jpg"));
            if (file.exists()) {
                Intent intent = new Intent(NoteView.this, NoteImageView.class);
                String img_path = pathForNewCameraPhotoEx(mImageName + ".jpg");
                intent.putExtra("note_img_path", img_path);
                startActivity(intent);
                return;
            }
            file = new File(pathForNewCameraPhotoEx(mImageName + "_thum.jpg"));
            if (file.exists()) {
                Intent intent = new Intent(NoteView.this, NoteImageView.class);
                String img_path = pathForNewCameraPhotoEx(mImageName + "_thum.jpg");
                intent.putExtra("note_img_path", img_path);
                startActivity(intent);
                return;
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        int position = 0;
        super.onCreate(savedInstanceState);
        mReceiver = new StorageMountedReceiver(this);
        mReceiver.registerMountReceiver(this);

        mTasks = new ArrayList<AsyncTask<String, Object, Boolean>>();
        initTasks();

        setContentView(R.layout.notelist_item_edit_main);
        mContext = this;
        init();
        mImageDownLoader = new ImageDownloader(this);

        IntentFilter intentFilter = new IntentFilter(PublicUtils.ACTION_NOTEALARM_ALERT);
        registerReceiver(mNoteAlarmReceiver, intentFilter);
        NBFontsizePref = this.getSharedPreferences(NBFontsizePrefName, Context.MODE_PRIVATE);
        curtextSize = NBFontsizePref.getFloat(NBFontsizePrefKey, 20.0f);

        if (savedInstanceState != null) {
            mCurrentPhotoFile = savedInstanceState.getString("NotePhotoFile");
        }

        mUri = getIntent().getData();

        mRecordCount = getIntent().getIntExtra(PublicUtils.NOTE_RECORD_COUNT, 0);
        notelist = getIntent().getParcelableArrayListExtra("notelist");

        loadContent();
		
        mTopNoteCount = PublicUtils.getTopNoteCount(mContext.getContentResolver());
        center2.setBackgroundResource(sBgworkcol.get(mCurrentWorkingcol));
        mEditArea.setBackgroundResource(sEditAreaBgworkcol.get(mCurrentWorkingcol));
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("NotePhotoFile", mCurrentPhotoFile);
    }

    private void loadContent() {
        if (mUri != null) {
            sWhere = NotePad.Notes._ID +
                    " = " +
                    mUri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION);

            mCursor = managedQuery(
                    mUri,
                    NotePad.Notes.PROJECTION,
                    null,
                    null,
                    null
            );
            mCursor.moveToFirst();

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            int colAlertDateIndex = mCursor.getColumnIndex(NotePad.Notes.ALERTED_DATE);
            int colSortIndex = mCursor.getColumnIndex(NotePad.Notes.SORT_INDEX);
            int colIsTopIndex = mCursor.getColumnIndex(NotePad.Notes.IS_TOP);
            int colNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_NOTE);
            //int groupNoteIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_GROUP);
            int colNoteIDIndex = mCursor.getColumnIndex(NotePad.Notes._ID);
            mAlertDate = mCursor.getLong(colAlertDateIndex);
            ShowAlertTime(mAlertDate);
            mSortIndex = mCursor.getInt(colSortIndex);
            mIsTop = mCursor.getInt(colIsTopIndex);
            mNoteID = mCursor.getInt(colNoteIDIndex);

            int bgcolIndex = mCursor.getColumnIndex(NotePad.Notes.COLUMN_NAME_COLOR);
            mCurrentWorkingcol = mCursor.getInt(bgcolIndex);
            mOldWorkingcol = mCurrentWorkingcol;

            String note = mCursor.getString(colNoteIndex);
            mNote = note;
            //String group = mCursor.getString(groupNoteIndex);

            if (note != null) {
                String[] items = note.split("__END_OF_PART__");
                int num = items.length;
                for (int i = 0; i < num; i++) {
                    Log.d("tui", "items[" + i + "]: " + items[i]);
                }

                LoadContentTask task = new LoadContentTask();
                task.execute(items);
                mTasks.set(TASK_LOAD_CONTENT, task);
            }
        } else {
            mCurrentWorkingcol = 0;
            int count = mEditArea.getChildCount();
            for (int j = count - 1; j >= 0; j--) {
                View v = mEditArea.getChildAt(j);
                if (v instanceof NoteEditText) {
                    ((NoteEditText) v).setTextSize(curtextSize);
                    break;
                }
            }

            //mDeleteBtn.setEnabled(false);
            mNoteDeleteBtn.setEnabled(false);
            mNoteDeleteBtn.setTextColor(0xffc0c0c0);
            mAlarmBtn.setEnabled(false);
        }
    }

    private boolean getNoteContentStatus() {
        int count = mEditArea.getChildCount();
        String temp_str = null;

        if (count == 1) {
            View v = mEditArea.getChildAt(0);
            if (v instanceof NoteEditText) {
                temp_str = ((NoteEditText) v).getText().toString();
                if ((temp_str == null) || (temp_str != null && temp_str.equals(""))) {
                    return false;
                }
            } else if (v instanceof ImageEditor) {
                return true;
            }
        }

        return true;
    }


    public void onClickTakePhoto(final View v) {
        takePhoto();
    }


    public Bitmap convertToBitmap(String path, int w, int h) {
        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 设置为ture只获取图片大小
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 返回为空
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        float scaleWidth = 0.f, scaleHeight = 0.f;
        if (width > w || height > h) {
            // 缩放
            scaleWidth = ((float) width) / w;
            scaleHeight = ((float) height) / h;
        }

        opts.inJustDecodeBounds = false;
        float scale = Math.max(scaleWidth, scaleHeight);
        //opts.inSampleSize = (int)scale;
        opts.inSampleSize = Math.round(scale);
        Bitmap temp1 = BitmapFactory.decodeFile(path, opts);
        int width_temp1 = temp1.getWidth();
        int height_temp1 = temp1.getHeight();
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(temp1);
        return weak.get();
    }

    private void setDeleteImgInvisible(View imgView) {
        int count = mEditArea.getChildCount();
        ImageEditor curImgEditor = (ImageEditor) imgView.getParent();
        
        for (int i = 0; i < count; i++) {
            View v = mEditArea.getChildAt(i);
            if (v instanceof ImageEditor) {
                ImageEditor tempImgEditor = (ImageEditor) v;

                if (tempImgEditor != curImgEditor) {
                    ImageView delete = tempImgEditor.getDeleteView();
                    if (delete.getVisibility() == View.VISIBLE) {
                        delete.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    private void addImage(final String image) {
        ImageEditor layout = (ImageEditor) mInflater.inflate(R.layout.image_layout, null);
        ImageView im = (ImageView) layout.findViewById(R.id.thumb_image);
        im.setOnClickListener(new ContentPhotoClickListener(image));

        Bitmap b = BitmapFactory.decodeFile(pathForNewCameraPhotoEx(image + "_thum.jpg"));
        if (b == null) {
            b = BitmapFactory.decodeFile(pathForNewCameraPhotoEx(image + ".jpg"));
        }

        im.setImageBitmap(b);
        layout.setPhoto(image);
        final ImageView delete = (ImageView) layout.findViewById(R.id.btn_delete);
        layout.setDeleteView(delete);
        delete.setVisibility(View.INVISIBLE);
        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                del_note_image(delete);
            }
        });

        OnLongClickListener longClickListener = new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (delete.getVisibility() == View.INVISIBLE) {
                    delete.setVisibility(View.VISIBLE);
                } else {
                    delete.setVisibility(View.INVISIBLE);
                }
                
                setDeleteImgInvisible(v);
                return true;
            }
        };

        im.setOnLongClickListener(longClickListener);

        int index = mEditArea.indexOfChild(mEditArea.getFocusedChild());

        View vChild = mEditArea.getChildAt(index);
        int iNewIndex = 0;
        if (vChild instanceof NoteEditText) {
            String text = ((NoteEditText) vChild).getText().toString();
            mEditArea.addView(layout, index + 1);
            iNewIndex = index + 2;
        }


        NoteEditText et = (NoteEditText) mInflater.inflate(R.layout.edit_in_textactivity, null);
        mEditArea.addView(et, iNewIndex);

        et.setOnTouchListener(new ScribingListenter());
        et.addTextChangedListener(mTextWatcher);
        et.setOnKeyListener(new NBEditTextOnKeyListenter());
        et.setTextSize(curtextSize);
        et.setFocusable(true);
        et.setFocusableInTouchMode(true);
        et.requestFocus();


        if (getNoteContentStatus() == false) {
            mAlarmBtn.setEnabled(false);
        } else {
            mAlarmBtn.setEnabled(true);
        }
        AlarmSave = false;
        NotePad.Notes.sSaveNoteFlag = false;
    }

    private void del_note_image(ImageView del_image) {
        ImageEditor imageEditor = (ImageEditor) del_image.getParent();
        int del_index = mEditArea.indexOfChild(imageEditor);
        int count = mEditArea.getChildCount();

        if (count >= del_index + 1) {
            View vtemp = mEditArea.getChildAt(del_index + 1);
            if (vtemp instanceof NoteEditText) {
                String temp_str = ((NoteEditText) vtemp).getText().toString();
                if ((temp_str == null) || (temp_str != null && temp_str.equals(""))) {
                    mEditArea.removeView(vtemp);
                }
            }
        }
        mEditArea.removeView((ImageEditor) del_image.getParent());

        File file = new File(pathForNewCameraPhotoEx(imageEditor.getPhoto() + ".jpg"));
        if (file.exists()) {
            file.delete();
        }

        file = new File(pathForNewCameraPhotoEx(imageEditor.getPhoto() + "_thum.jpg"));
        if (file.exists()) {
            file.delete();
        }

        if (getNoteContentStatus() == false) {
            mAlarmBtn.setEnabled(false);
        } else {
            mAlarmBtn.setEnabled(true);
        }
    }

    
    private final TextWatcher mTextWatcher = new TextWatcher() {
        private String foc_before_str = null;

        private String foc_after_str = null;
        private int foc_et_str_len = 0;
        private int before_focused_index = 0;

        @Override
        public void afterTextChanged(Editable s) {
            int length = s.length();
            AlarmSave = false;
            NotePad.Notes.sSaveNoteFlag = false;
            if (getNoteContentStatus() == false) {
                mAlarmBtn.setEnabled(false);
            } else {
                mAlarmBtn.setEnabled(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            NoteEditText et_focused = (NoteEditText) mEditArea.getFocusedChild();
            if (et_focused == null) {
                return;
            }
            String foc_et_str = et_focused.getText().toString();
            foc_et_str_len = foc_et_str.length();
            before_focused_index = et_focused.getSelectionStart();
            foc_before_str = foc_et_str.substring(0, before_focused_index);
            foc_after_str = foc_et_str.substring(before_focused_index);
            int index = mEditArea.indexOfChild(mEditArea.getFocusedChild());

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (count == 1) {
                if (s.charAt(start) == '\n') {
                    NoteEditText et_focused = (NoteEditText) mEditArea.getFocusedChild();
                    if (et_focused == null) {
                        return;
                    }
                    int index = mEditArea.indexOfChild(mEditArea.getFocusedChild());
                    int focused_index = et_focused.getSelectionStart();


                    NoteEditText et = (NoteEditText) mInflater.inflate(
                            R.layout.edit_in_textactivity, null);
                    mEditArea.addView(et, index + 1);
                    NoteEditText prev = (NoteEditText) mEditArea.getChildAt(index);

                    if (focused_index > foc_et_str_len) {
                        et.setFocusable(true);
                        et.setFocusableInTouchMode(true);
                        et.requestFocus();
                        prev.setText(foc_before_str);
                    } else {
                        if (before_focused_index == 0) {
                            prev.setText("");
                            et.setText(foc_after_str);
                            if (prev.getPaintFlags() == (Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG)) {
                                prev.setPaintFlags(0);
                                et.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                            }
                        } else {
                            prev.setText(foc_before_str);
                            et.setText(foc_after_str);

                            et.setFocusable(true);
                            et.setFocusableInTouchMode(true);
                            et.requestFocus();
                        }
                    }
                    prev.setTextSize(curtextSize);
                    et.setTextSize(curtextSize);
                    et.setOnTouchListener(new ScribingListenter());
                    et.addTextChangedListener(mTextWatcher);
                    et.setOnKeyListener(new NBEditTextOnKeyListenter());
                }
            }
        }
    };


    public class NBEditTextOnKeyListenter implements View.OnKeyListener {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DEL) {


                if (v instanceof NoteEditText) {
                    //String text = ((NoteEditText)v).getText().toString();
                    int index = ((NoteEditText) v).getSelectionStart();
                    
                    if (index == 0) {

                        int FoucsET_Index = mEditArea.indexOfChild(mEditArea.getFocusedChild());
                        if (FoucsET_Index > 0) {
                            View vPrevChild = mEditArea.getChildAt(FoucsET_Index - 1);
                            if (vPrevChild instanceof NoteEditText) {
                                String text1 = ((NoteEditText) vPrevChild).getText().toString();
                                int len1 = text1.length();
                                String text_temp = ((NoteEditText) v).getText().toString();
                                int len_temp = text_temp.length();

                                mEditArea.removeView(v);
                                if (len_temp > 0) {
                                    StringBuilder sb = new StringBuilder(text1);
                                    sb.append(text_temp);
                                    ((NoteEditText) vPrevChild).setText(sb.toString());
                                }
                                ((NoteEditText) vPrevChild).setFocusable(true);
                                ((NoteEditText) vPrevChild).setFocusableInTouchMode(true);
                                ((NoteEditText) vPrevChild).requestFocus();
                                ((NoteEditText) vPrevChild).setSelection(len1);
                            } else if (vPrevChild instanceof ImageEditor) {
                                String text_temp = ((NoteEditText) v).getText().toString();
                                int len_temp = text_temp.length();
                                if (len_temp == 0) {
                                    mEditArea.removeView(v);
                                }
                                mEditArea.removeView(vPrevChild);
                                File file = new File(pathForNewCameraPhotoEx(((ImageEditor) vPrevChild).getPhoto() + ".jpg"));
                                if (file.exists()) {
                                    file.delete();
                                }
                                file = new File(pathForNewCameraPhotoEx(((ImageEditor) vPrevChild).getPhoto() + "_thum.jpg"));
                                if (file.exists()) {
                                    file.delete();
                                }

                                if (FoucsET_Index - 2 >= 0 && len_temp == 0) {
                                    View vTempChild = mEditArea.getChildAt(FoucsET_Index - 2);
                                    if (vTempChild instanceof NoteEditText) {
                                        String text2 = ((NoteEditText) vTempChild).getText().toString();
                                        int len2 = text2.length();

                                        ((NoteEditText) vTempChild).setFocusable(true);
                                        ((NoteEditText) vTempChild).setFocusableInTouchMode(true);
                                        ((NoteEditText) vTempChild).requestFocus();
                                        ((NoteEditText) vTempChild).setSelection(len2);
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_DEL) {
                if (v instanceof NoteEditText) {
                    int index2 = ((NoteEditText) v).getSelectionStart();
                    
                    if (index2 == 0) {
                        int FoucsET_Index2 = mEditArea.indexOfChild(mEditArea.getFocusedChild());
                        
                        if (FoucsET_Index2 == 0) {
                            if (((NoteEditText) v).getPaintFlags() == (Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG)) {
                                ((NoteEditText) v).setPaintFlags(0);
                            }
                        }
                    }
                }
                if (getNoteContentStatus() == false) {
                    mAlarmBtn.setEnabled(false);
                } else {
                    mAlarmBtn.setEnabled(true);
                }
            }
            return false;
        }
    }



    public class ScribingListenter implements OnTouchListener {
        float x = 0, y = 0, upx = 0, upy = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            NoteEditText et = (NoteEditText) v;
            Log.d("tui", "ScribingListenter onTouch");

            if (mWriteCanvasLayout.getVisibility() == View.VISIBLE) {
                closeWriteCanvas();
            }

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    x = event.getX();
                    y = event.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    upx = event.getX();
                    upy = event.getY();
                    if (Math.abs(x - upx) > 80 && Math.abs(y - upy) < 40) {
                        if (et.getPaintFlags() == (Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG)) {
                            et.setPaintFlags(0);
                        } else {
                            et.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
                        }

                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
            }
            return false; //false
        }
    }


    public static void addGalleryIntentExtras(Intent intent, Uri croppedPhotoUri, int photoSize) {
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectX", mWidth);
        intent.putExtra("aspectY", mHeight);
        intent.putExtra("outputX", mWidth / 2);//photoSize);
        intent.putExtra("outputY", mHeight / 2);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, croppedPhotoUri);
    }

    private void savePic(Bitmap b, String strPath) {
        if (b == null){
            return;
        }

        FileOutputStream fos = null;
        File file = new File(strPath);

        if (file.exists()) {
            file.delete();
//            return;
        }
        
        try {
            fos = new FileOutputStream(strPath);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mNoteBgColorSelector.getVisibility() == View.VISIBLE
                && !inRangeOfView(mNoteBgColorSelector, ev)) {
            closeNotebgColorSeletor();
            if (inRangeOfView(mAlarmBtn, ev) || inRangeOfView(mTakePhoto, ev) 
                || inRangeOfView(mPickPhoto, ev) || inRangeOfView(mBtnMore, ev)) {
                return super.dispatchTouchEvent(ev);
            } else {
                return true;
            }
        } else if (mNoteShareDelete.getVisibility() == View.VISIBLE
                && !inRangeOfView(mNoteShareDelete, ev)) {
            closeNoteShareDeleteMenu();
            if (inRangeOfView(mAlarmBtn, ev) || inRangeOfView(mTakePhoto, ev) 
                || inRangeOfView(mPickPhoto, ev) || inRangeOfView(mNoteBgBtn, ev)) {
                return super.dispatchTouchEvent(ev);
            } else {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x
                || ev.getX() > (x + view.getWidth())
                || ev.getY() < y
                || ev.getY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }

    @SuppressLint("SimpleDateFormat")
    public static String generateTempNoteShareFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(PHOTO_DATE_FORMAT);
        return "NoteShare-" + dateFormat.format(date);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.btn_more:
                //showNotbgColorSeleor();
                //findViewById(sBgSelectorSelectionMap.get(mCurrentWorkingcol)).setVisibility(View.VISIBLE);
                showNoteShareDeleteMenu();
                closePopupWin();
                break;
            case R.id.go_alarm_btn:
                showAlarmDialog(v);
                break;
            case R.id.modify_btn:
                showUpdateAlarmDialog();
                break;
            case R.id.delete_btn:
                DeleteNoteAlarm();
                ShowAlertTime(0);
                //PublicUtils.updateNoteAlertNum(mContext.getContentResolver(),0,mUri);
                AlarmSaveFlag = true;
                closePopupWin();
                break;
     /*
            case R.id.go_delete_btn:
                closePopupWin();
                deleteNoteItem();
                break;
     */
            case R.id.go_note_bg_btn:
                showNotbgColorSeleor();
                findViewById(sBgSelectorSelectionMap.get(mCurrentWorkingcol)).setVisibility(View.VISIBLE);
                closePopupWin();
                break;
            case R.id.btn_note_share:
                //Bitmap noteBitmap = PublicUtils.convertViewToBitmapEx(mEditArea);
                String noteShareFilePath = PublicUtils.FOLD_SHARE_PIC + "/" + generateTempNoteShareFileName() + ".jpg";
                SavePreShareTask task1 = new SavePreShareTask(this);
                task1.execute(null, noteShareFilePath,true);
                break;
            case R.id.btn_note_delete:
                deleteNoteItem();
                break;
            case R.id.btn_back:
                closePopupWin();
                if (NotePad.Notes.sSaveNoteFlag == false) {
                    long alarmdate = 0;
                    if (mUri != null) {
                        alarmdate = PublicUtils.getAlarmDateByUri(mContext.getContentResolver(), mUri);
                    }
                    doSave(alarmdate);   //mAlertDate
                }
                break;
            case R.id.take_photo:
                closePopupWin();
                takePhoto();
//                switchToWriteCanvas();
                break;
            case R.id.pick_photo:
                closePopupWin();
                pickPhoto();
                break;
            case R.id.wc_btn_bold:
                togglePaintBoldArea(v);
                break;
            case R.id.wc_btn_collapse:
                closeWriteCanvas();
                break;
            case R.id.wc_btn_delete:
                break;
            case R.id.wc_btn_sd:
                toggleWriteArea();
                break;
            case R.id.wc_btn_space:
                break;
            default:
                if (sBgSelectorBtnsMap.containsKey(id)) {
                    int i = sBgSelectorBtnsMap.get(id);
                    findViewById(sBgSelectorSelectionMap.get(mCurrentWorkingcol)).setVisibility(View.GONE);
                    center2.setBackgroundResource(sBgworkcol.get(i));
                    mEditArea.setBackgroundResource(sEditAreaBgworkcol.get(i));
                    mCurrentWorkingcol = i;
                    closeNotebgColorSeletor();
                }
                break;
        }
    }

    private void toggleWriteArea() {

        mPaintBoldWindow.close();

        Button b = (Button)findViewById(R.id.wc_btn_sd);
        WriteCanvas singleWC =  (WriteCanvas)findViewById(R.id.write_single_area);
        if (singleWC.getVisibility() == View.VISIBLE) {
            b.setText(R.string.canvas_single);
            singleWC.setVisibility(View.GONE);
            findViewById(R.id.write_double_area).setVisibility(View.VISIBLE);
        }else if (singleWC.getVisibility() == View.GONE) {
            b.setText(R.string.canvas_double);
            singleWC.setVisibility(View.VISIBLE);
            findViewById(R.id.write_double_area).setVisibility(View.GONE);
        }
    }

    private void togglePaintBoldArea(View anchor) {
        mPaintBoldWindow.toggle(anchor);
    }

    private void switchToWriteCanvas() {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getWindow() != null && getWindow().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        mBtnArea.setVisibility(View.GONE);
        mWriteCanvasLayout.setVisibility(View.VISIBLE);
    }

    private void closeWriteCanvas() {
        mPaintBoldWindow.close();

        mBtnArea.setVisibility(View.VISIBLE);
        mWriteCanvasLayout.setVisibility(View.GONE);
    }

    private void deleteNoteItemImageFile() {
        int count = mEditArea.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mEditArea.getChildAt(i);
            if (v instanceof ImageEditor) {
                String photoName = ((ImageEditor) v).getPhoto();
                File file = new File(pathForNewCameraPhotoEx(photoName + ".jpg"));
                if (file.exists()) {
                    file.delete();
                }

                file = new File(pathForNewCameraPhotoEx(photoName + "_thum.jpg"));
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void deleteNoteItem() {
        AlertDialog.Builder bld = new AlertDialog.Builder(NoteView.this);
        bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mDeleting = ProgressDialog.show(NoteView.this, "", getString(R.string.delete_progress), true);
                if (mUri != null) {
                    NoteView.this.getContentResolver().delete(mUri, sWhere, null);
                    deleteNoteItemImageFile();
                    PublicUtils.cancelNoteAlarm(mContext, mUri);
                }
                NotePad.Notes.sDeleteFlag = true;
            }
        });
        bld.setNegativeButton(getString(R.string.delete_confirm_cancel), null);
        bld.setCancelable(true);
        bld.setMessage(getString(R.string.delete_note_confirm));
        bld.setTitle(getString(R.string.delete_confirm_title));
        bld.create().show();
        NotePad.Notes.sDeleteNum = 1;
    }

    /**
     *
     */
    private void DeleteNoteAlarm() {
        updateNote(null, null, null, 0);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setData(mUri);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
        alarmManager.cancel(pendingIntent);
    }

    /**
     *
     */
    private void showUpdateAlarmDialog() {
        long alarmdate = PublicUtils.getAlarmDateByUri(mContext.getContentResolver(), mUri);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        NoteAlarmSetView view = (NoteAlarmSetView)inflater.inflate(R.layout.notebook_alarm_set, null);

        
        Dialog dialog = new Dialog(mContext,R.style.Note_Dialog);
        dialog.setContentView(view);

        view.setDialog(dialog);
        view.setAlarmFlag(ALARM_FLAG_UPDATE);

        dialog.show();
        closePopupWin();
    }

    public long getAlertOldTime(){
        if (mUri != null){
            long alarmdate = PublicUtils.getAlarmDateByUri(mContext.getContentResolver(), mUri);
            return alarmdate;
        }

        return 0;
    }

    /**
     *
     * @param anchor
     */
    private void showAlarmDialog(View anchor) {

        long alarmdate = PublicUtils.getAlarmDateByUri(mContext.getContentResolver(), mUri);
        if (alarmdate == 0) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            NoteAlarmSetView view = (NoteAlarmSetView)inflater.inflate(R.layout.notebook_alarm_set, null);

            
            Dialog dialog = new Dialog(mContext,R.style.Note_Dialog);
            dialog.setContentView(view);
            
            view.setDialog(dialog);
            view.setAlarmFlag(ALARM_FLAG_NEW);
            dialog.show();
        }else {
            initPopupWindow();
            if (mPopupWin.isShowing()){
                mPopupWin.dismiss();
            }else {
                int yOff = getAnchorYoff(anchor);
                mPopupWin.showAtLocation(anchor, Gravity.NO_GRAVITY, 155, yOff - 11);
            }
        }
    }

    /**
     *
     * @param anchor
     * @return
     */
    private int getAnchorYoff(View anchor) {
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        Log.d("tui", "location[0] = " + location[0] + ", location[1] = " + location[1]);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int popH = mPopupWin.getHeight();
        Log.d("tui", "popH = " + popH);
        return location[1] - popH;
    }

    /**
     *
     */
    private void initPopupWindow() {
        if (mPopupWin != null){
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.note_update_alarm_btn, null);
        Button btn = (Button)layout.findViewById(R.id.modify_btn);
        btn.setOnClickListener(this);
        btn = (Button)layout.findViewById(R.id.delete_btn);
        btn.setOnClickListener(this);

        int w = getResources().getDimensionPixelOffset(R.dimen.note_update_alarm_btn_area_width);
        int h = getResources().getDimensionPixelSize(R.dimen.note_update_alarm_btn_area_height);

        mPopupWin = new PopupWindow(layout, w, h);
        mPopupWin.setAnimationStyle(R.style.PopupWindowAnim);
    }

    public PopupWindow getNotePopupWin() {
        return mPopupWin;
    }

    private void closePopupWin() {
        if (mPopupWin != null && mPopupWin.isShowing()){
            mPopupWin.dismiss();
        }
    }

    /**
     * added by yutao 2014.06.06
     */
    private void showNotbgColorSeleor() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_bg_chooser_show);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mNoteBgColorSelector.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mNoteBgColorSelector.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mNoteBgColorSelector.startAnimation(anim);
        mNoteBgColorSelector.setVisibility(View.VISIBLE);

    }

    /**
     * added by yutao 2014.06.06
     */
    private void closeNotebgColorSeletor() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_bg_chooser_disappear);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mNoteBgColorSelector.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mNoteBgColorSelector.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mNoteBgColorSelector.startAnimation(anim);
        mNoteBgColorSelector.setVisibility(View.GONE);

    }


     
    private void showNoteShareDeleteMenu() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_menu_share_delete_show);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mNoteShareDelete.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mNoteShareDelete.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mNoteShareDelete.startAnimation(anim);
        mNoteShareDelete.setVisibility(View.VISIBLE);

    }

    
    private void closeNoteShareDeleteMenu() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_menu_share_delete_disappear);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mNoteShareDelete.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mNoteShareDelete.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mNoteShareDelete.startAnimation(anim);
        mNoteShareDelete.setVisibility(View.GONE);

    }

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CAMERA_WITH_DATA: {
                    final String path = pathForNewCameraPhotoEx(mCurrentPhotoFile + ".jpg");
                    Bitmap bitmap = compressPicture(path);
                    
                    WeakReference<Bitmap> weak = new WeakReference<Bitmap>(bitmap);
                    savePic(weak.get(), pathForNewCameraPhotoEx(mCurrentPhotoFile + "_thum.jpg"));
                    addImage(mCurrentPhotoFile);
                    break;
                }
                case REQUEST_CODE_NOTEALERT_WITH_DATA: {
                    if (data != null && data.hasExtra("NoteAlertDate")) {
                        mAlertDate = data.getLongExtra("NoteAlertDate", 0);

                        if (mAlertDate > 0) {
                            AlarmSave = true;
                            mAlertDate = PublicUtils.getAlertTime(mAlertDate);
                            doSave(mAlertDate);


                            long triggerAtTime = SystemClock.elapsedRealtime() + 15 * 1000;
                            Intent intent = new Intent(this, AlarmReceiver.class);
                            intent.setData(mUri);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                            AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
                            alarmManager.set(AlarmManager.RTC_WAKEUP, mAlertDate, pendingIntent);
                            ShowAlertTime(mAlertDate);
			    //PublicUtils.updateNoteAlertNum(mContext.getContentResolver(),NOTE_ALARM_ALERT_NUM,mUri);
                        }
                    }
                    break;
                }
                case REQUEST_CODE_NOTEALARM_UPDATE: {
                    if (data != null && data.hasExtra("DeleteNoteAlert")) {
                        long delNote = data.getLongExtra("DeleteNoteAlert", -1);
                        if (delNote == 0) {
                            ShowAlertTime(delNote);
			    //PublicUtils.updateNoteAlertNum(mContext.getContentResolver(),0,mUri);
                        }
                    }
                    break;
                }
            }
        } else if (resultCode == Activity.RESULT_FIRST_USER && requestCode == REQUEST_CODE_NOTEALARM_UPDATE) {
            if (data != null && data.hasExtra("NoteAlertDateUpdate")) {
                mAlertDate = data.getLongExtra("NoteAlertDateUpdate", 0);

                if (mAlertDate > 0) {
                    AlarmSave = true;
                    mAlertDate = PublicUtils.getAlertTime(mAlertDate);
                    doSave(mAlertDate);


                    long triggerAtTime = SystemClock.elapsedRealtime() + 15 * 1000;
                    Intent intent = new Intent(this, AlarmReceiver.class);
                    intent.setData(mUri);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
                    alarmManager.set(AlarmManager.RTC_WAKEUP, mAlertDate, pendingIntent);

                    ShowAlertTime(mAlertDate);
		    //PublicUtils.updateNoteAlertNum(mContext.getContentResolver(),NOTE_ALARM_ALERT_NUM,mUri);
                }
            }
        }

    }

    public void setAlarmTime(int type, long date){
        Log.d("tui", "setAlarmTime date = "+date);
        mAlertDate = date;

        if (mAlertDate > 0) {
            AlarmSave = true;
            mAlertDate = PublicUtils.getAlertTime(mAlertDate);
            doSave(mAlertDate);

            long triggerAtTime = SystemClock.elapsedRealtime() + 15 * 1000;
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.setData(mUri);   //mWorkingNote.getNoteId()
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
            alarmManager.set(AlarmManager.RTC_WAKEUP, mAlertDate, pendingIntent); //RTC_WAKEUP  mAlertDate
            ShowAlertTime(mAlertDate);
            //PublicUtils.updateNoteAlertNum(mContext.getContentResolver(),NOTE_ALARM_ALERT_NUM,mUri);
        }
    }

    
    private Bitmap compressPicture(String path) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        BitmapFactory.decodeFile(path, options);

        int image_view_width = getResources().getDimensionPixelSize(R.dimen.noteview_image_view_width);
        int image_view_height = getResources().getDimensionPixelSize(R.dimen.noteview_image_view_height);

        int sampleSize = PublicUtils.getInSampleSize(options, image_view_width, image_view_height);

        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return bitmap;
    }

    /**
     * yutao
     * @param path
     * @return
     */
    private String getFileNameFromPath(String path) {
        String[] p = path.split("/");
        String name = p[p.length - 1];
        Log.d("tui", "mame = " + name);

        String[] p1 = name.split("\\.");

        return p1[0];
    }

    private void ShowAlertTime(long alerttime) {
        long Currtime = System.currentTimeMillis();

        if (alerttime == 0) {
            mAlarmBtn.setText("");
        } else if (alerttime == -2 || Currtime > alerttime) {
            mAlarmBtn.setText(R.string.note_alerted);
        } else {
            try {
                String atime = PublicUtils.FormatTimeToString(mContext, alerttime);
                mAlarmBtn.setText(atime);
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public static String pathForCroppedPhoto(Context context, String fileName) {
        final File dir = new File(context.getExternalCacheDir() + "/tmp");
        dir.mkdirs();
        final File f = new File(dir, fileName);
        return f.getAbsolutePath();
    }

    @SuppressLint("SdCardPath")
    public static String pathForNewCameraPhotoEx(String fileName) {
        String path = "/sdcard";
        final File dir = new File(path + "/.TinnoNote");
        dir.mkdirs();
        final File f = new File(dir, fileName);
        return f.getAbsolutePath();
    }

    @SuppressLint("SdCardPath")
    public String pathForNewAudioEx(String fileName) {
        String path = "/sdcard";//mSM.getDefaultPath();
        //Log.i("note", "path : " + path);
        final File dir = new File(path + "/.TinnoNote");
        dir.mkdirs();
        final File f = new File(dir, fileName);
        return f.getAbsolutePath();
    }

    private Intent getTakePhotoIntent(String fileName) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE, null);
        
        final String newPhotoPath = pathForNewCameraPhotoEx(fileName);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(newPhotoPath)));
        return intent;
    }

    private void startPhotoActivity(Intent intent, int requestCode, String photoFile) {
        mCurrentPhotoFile = photoFile;
        startActivityForResult(intent, requestCode);
    }

    private void startTakePhotoActivity(String photoFile) {
        final Intent intent = getTakePhotoIntent(photoFile + ".jpg");
        startPhotoActivity(intent, REQUEST_CODE_CAMERA_WITH_DATA, photoFile);
    }

    @SuppressLint("SimpleDateFormat")
    public static String generateTempPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(PHOTO_DATE_FORMAT);
        return "NotePhoto-" + dateFormat.format(date);
    }

    private void takePhoto() {
        try {
            // Launch camera to take photo for selected contact
            startTakePhotoActivity(generateTempPhotoFileName());
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    private void pickPhoto() {
        try {
            // Launch picker to choose photo for selected contact
            startPickFromNoteGallery(generateTempPhotoFileName());
        } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    mContext, R.string.photoPickerNotFoundText, Toast.LENGTH_LONG).show();
        }
    }

    private Intent getPhotoPickIntent(String photoFile) {
        final String croppedPhotoPath = pathForCroppedPhoto(mContext, photoFile + ".jpg");
        final Uri croppedPhotoUri = Uri.fromFile(new File(croppedPhotoPath));
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        addGalleryIntentExtras(intent, croppedPhotoUri, mPhotoPickSize);
        return intent;
    }

    private void startPickFromNoteGallery(String photoFile) {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getWindow() != null && getWindow().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        Intent intent = new Intent(this, PictureSelectActivity.class);
        mCurrentPhotoFile = photoFile;
        startActivity(intent);
        /*Intent intent = new Intent(this, NoteGraffiti.class);
        startActivity(intent);*/
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        closePopupWin();
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            for (int i = 0; i < mTasks.size(); i++) {
                AsyncTask<String, Object, Boolean> task = mTasks.get(i);
                if (task == null) continue;

                AsyncTask.Status state = task.getStatus();
                if (state == AsyncTask.Status.RUNNING){
                    String text = getString(R.string.picture_loading);
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                    return true;
                }
            }

            if (NotePad.Notes.sSaveNoteFlag == false) {
                long alarmdate = 0;
                if (mUri != null) {
                    alarmdate = PublicUtils.getAlarmDateByUri(mContext.getContentResolver(), mUri);
                }
                doSave(alarmdate);   //mAlertDate
            }

            if (mWriteCanvasLayout.getVisibility() == View.VISIBLE) {
                mWriteCanvasLayout.setVisibility(View.GONE);
                Log.d("tui", "onKeyDown, mWriteCanvasLayout");
                return false;
            }
        }

        return super.onKeyDown(keyCode, event);
    }


    @SuppressLint("ShowToast")
    class MaxLengthFilter implements InputFilter {
        private int mMaxLength;

        public MaxLengthFilter(int max) {
            mMaxLength = max - 1;
            mMaxNoteToast = Toast.makeText(NoteView.this, R.string.editor_full,
                    Toast.LENGTH_SHORT);
        }

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            int keep = mMaxLength - (dest.length() - (dend - dstart));
            if (keep < (end - start)) {
                mMaxNoteToast.show();
            }
            if (keep <= 0) {
                return "";
            } else if (keep >= end - start) {
                return null;
            } else {
                return source.subSequence(start, start + keep);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleAddImage();
        ShowAlertTime(mAlertDate);
    }

    private void handleAddImage() {

        ArrayList<String> paths = NotePad.mPaths;
        if (paths.size() <= 0){
            return;
        }

        LoadImageTask task = new LoadImageTask();
        String[] imageNames = new String[paths.size()];

        for(int i = 0; i < paths.size(); i++){
            imageNames[i] = paths.get(i);
        }

        task.execute(imageNames);
        mTasks.set(TASK_LOAD_IMAGE, task);

        NotePad.mPaths.clear();
    }

    private void initTasks() {
        for (int i = 0; i < TASK_TOTAL; i++) {
            mTasks.add(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mWriteCanvasLayout.setVisibility(View.GONE);
        mBtnArea.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImageDownLoader.OnDestroy();
        mPaintBoldWindow.close();

        clearTasks();

        mReceiver.unRegisterMountReceiver(this);
        getContentResolver().unregisterContentObserver(mCobserver);
        unregisterReceiver(mNoteAlarmReceiver);
    }

    private void clearTasks(){
        for (int i = 0; i < mTasks.size(); i++) {
            AsyncTask<String, Object, Boolean> task = mTasks.get(i);
            if (task == null) continue;

            AsyncTask.Status state = task.getStatus();
            if (state == AsyncTask.Status.RUNNING){
                task.cancel(true);
            }
        }
        mTasks.clear();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    class SpinnerSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int which, long arg3) {
            String noteData[] = getData();
            mNotegroup = noteData[which];
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    public String getGroup(String i) {
        Resources resource = (Resources) this.getResources();
        String groupWork = (String) resource.getString(R.string.menu_work);
        String groupPersonal = (String) resource.getString(R.string.menu_personal);
        String groupFamily = (String) resource.getString(R.string.menu_family);
        String groupStudy = (String) resource.getString(R.string.menu_study);
        if (i.equals("1")) {
            return groupWork;
        } else if (i.equals("2")) {
            return groupPersonal;
        } else if (i.equals("3")) {
            return groupFamily;
        } else if (i.equals("4")) {
            return groupStudy;
        } else {
            return "";
        }
    }


    private void doSave(long AlertDate) {
        int count = mEditArea.getChildCount();
        int i = 0;
        String text = "";
        String note_text = "";
        Intent it = new Intent();
        
        for (i = 0; i < count; i++) {
            View v = mEditArea.getChildAt(i);
            if (v instanceof NoteEditText) {
                String edit = ((NoteEditText) v).getText().toString();
                note_text = note_text + edit;
                if (count == 1) {
                    if ((edit == null) || (edit != null && edit.equals(""))) {
                        break;
                    }
                }

                if (((NoteEditText) v).getPaintFlags() == (Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG)) {
                    text += "__END_OF_PART__TEXT__SCR__TRUE__";
                    text += edit;
                    text += "__END_OF_PART__TEXT__SCR__TRUE__";
                } else {
                    text += "__END_OF_PART__TEXT__SCR__FALSE__";
                    text += edit;
                    text += "__END_OF_PART__TEXT__SCR__FALSE__";
                }
            } else if (v instanceof ImageEditor) {
                String photo = ((ImageEditor) v).getPhoto();
                if (photo != null) {
                    text += "__END_OF_PART__PHOTO__";
                    text += photo;
                    text += "__END_OF_PART__PHOTO__";
                }
            }
        }

        if (mUri == null) {
            ContentValues values = new ContentValues();
            if (text.equals("")) {
                if (AlarmSave == false) {
                    it.putExtra("UpdateNoteFlag", false);
                    setResult(RESULT_OK, it);
                    finish();
                }
                NotePad.Notes.sSaveNoNote = true;
                return;
            } else {
                int NoteID = -1;

                if (mTopNoteCount > 0) {
                    for (int j = mTopNoteCount + 1; j <= mRecordCount; j++){
                        NoteID = notelist.get(j - 1).id;
                        ContentValues sort_values = new ContentValues();
                        sort_values.put(NotePad.Notes.SORT_INDEX, j + 1);
                        getContentResolver().update(
                                NotePad.Notes.CONTENT_URI,
                                sort_values,
                                NotePad.Notes._ID + "=?",
                                new String[]{String.valueOf(NoteID)}
                        );
                    }

                } else {
                    for (int j = 1; j <= mRecordCount; j++){
                        NoteID = notelist.get(j - 1).id;
                        ContentValues sort_values = new ContentValues();
                        sort_values.put(NotePad.Notes.SORT_INDEX, j + 1);

                        getContentResolver().update(
                                NotePad.Notes.CONTENT_URI,
                                sort_values,
                                NotePad.Notes._ID + "=?",
                                new String[]{String.valueOf(NoteID)}
                        );
                    }
                }

                values.put(NotePad.Notes.COLUMN_NAME_COLOR, mCurrentWorkingcol);
                //values.put(NotePad.Notes.COLUMN_NAME_GROUP, String.valueOf(i));
                values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
                values.put(NotePad.Notes.COLUMN_NAME_NOTETEXT, note_text);
                values.put(NotePad.Notes.ALERTED_DATE, AlertDate);
                values.put(NotePad.Notes.SORT_INDEX, mTopNoteCount + 1);
            }

            mUri = Notes.CONTENT_URI;
            Uri retrunUri = getContentResolver().insert(mUri, values);
            if (retrunUri == null) {
                mUri = null;
                Toast.makeText(this, R.string.sdcard_full, Toast.LENGTH_LONG).show();
            } else {
                this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


                if (AlarmSave == false) {
                    it.putExtra("UpdateNoteFlag", true);
                    setResult(RESULT_OK, it);
                    NotePad.Notes.sSaveNoteFlag = true;
                    finish();
                } else {
                    //mDeleteBtn.setEnabled(true);
                    mNoteDeleteBtn.setEnabled(true);
                    mNoteDeleteBtn.setTextColor(0xff000000);
                    mNote = text;
                    AlarmSave = false;
                    AlarmSaveFlag = true;
                    Toast.makeText(this, R.string.note_saved, Toast.LENGTH_LONG).show();
                }
                mUri = retrunUri;
            }
        } else {
            if ((mNote != null && !mNote.equals(text)) || (AlarmSave == true) || (mCurrentWorkingcol != mOldWorkingcol)) {
                updateNote(text, note_text, String.valueOf(i), AlertDate);
                if (AlarmSave == false) {
                    NotePad.Notes.sSaveNoteFlag = true;
                }
            }
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            if (AlarmSave == false) {
                if (AlarmSaveFlag == true || NotePad.Notes.sSaveNoteFlag == true) {
                    it.putExtra("UpdateNoteFlag", true);
                } else {
                    it.putExtra("UpdateNoteFlag", false);
                }
                setResult(RESULT_OK, it);
                finish();
            }
            else {
                mNote = text;
                AlarmSave = false;
                AlarmSaveFlag = true;
                Toast.makeText(this, R.string.note_saved, Toast.LENGTH_LONG).show();
            }
        }

        NBFontsizePref.edit().putFloat(NBFontsizePrefKey, curtextSize).commit();
    }


    private String[] getData() {
        Resources resource = (Resources) getBaseContext().getResources();
        String groupWork = (String) resource.getString(R.string.menu_work);
        String groupNone = (String) resource.getString(R.string.menu_none);
        String groupPersonal = (String) resource.getString(R.string.menu_personal);
        String groupFamily = (String) resource.getString(R.string.menu_family);
        String groupStudy = (String) resource.getString(R.string.menu_study);
        String[] noteData = {groupNone, groupWork, groupPersonal, groupFamily, groupStudy};
        return noteData;
    }


    private void updateNote(String text, String NoteText, String group, long AlertDate) {
        String year;
        String month;
        String day;
        String hour;
        String minute;
        int i = 0;
        ContentValues values = new ContentValues();
        values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, System.currentTimeMillis());
        Calendar c = Calendar.getInstance();
        year = String.valueOf(c.get(Calendar.YEAR));
        month = String.valueOf(c.get(Calendar.MONTH) + 1);
        day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        minute = String.valueOf(c.get(Calendar.MINUTE));
        if (c.get(Calendar.HOUR_OF_DAY) < 10) {
            hour = "0" + hour;
        }
        if (c.get(Calendar.MINUTE) < 10) {
            minute = "0" + minute;
        }
        String modifyTime = String.valueOf(year) +
                " " +
                month +
                " " +
                day +
                " " +
                hour +
                ":" +
                minute;
        values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, modifyTime);
        values.put(NotePad.Notes.COLUMN_NAME_NOTE, text);
        values.put(NotePad.Notes.ALERTED_DATE, AlertDate);
        values.put(NotePad.Notes.COLUMN_NAME_NOTETEXT, NoteText);

        int NoteID = -1;
        if (mIsTop == 0) {
            values.put(NotePad.Notes.SORT_INDEX, mTopNoteCount + 1);
            if (mSortIndex > mTopNoteCount + 1) {
                for (int j = mTopNoteCount + 1; j < mSortIndex; j++) {
                    NoteID = notelist.get(j - 1).id;
                    ContentValues sort_values = new ContentValues();
                    sort_values.put(NotePad.Notes.SORT_INDEX, j + 1);
                    getContentResolver().update(
                            NotePad.Notes.CONTENT_URI,
                            sort_values,
                            NotePad.Notes._ID + "=?",
                            new String[]{String.valueOf(NoteID)}
                    );

                }
            }
        } else if (mIsTop == 1) {
            values.put(NotePad.Notes.SORT_INDEX, 1);
            if (mSortIndex != 1) {
                for (int j = 1; j < mSortIndex; j++) {
                    NoteID = notelist.get(j - 1).id;
                    ContentValues sort_values = new ContentValues();
                    sort_values.put(NotePad.Notes.SORT_INDEX, j + 1);
                    getContentResolver().update(
                            NotePad.Notes.CONTENT_URI,
                            sort_values,
                            NotePad.Notes._ID + "=?",
                            new String[]{String.valueOf(NoteID)}
                    );
                }
            }
        }
        String noteData[] = getData();
        for (i = 0; i < 5; i++) {
            if (mNotegroup == noteData[i]) {
                break;
            }
        }
        values.put(NotePad.Notes.COLUMN_NAME_COLOR, mCurrentWorkingcol);
        getContentResolver().update(
                mUri,
                values,
                null,
                null
        );
    }


}
