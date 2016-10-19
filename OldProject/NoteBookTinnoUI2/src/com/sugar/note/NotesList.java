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

/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sugar.note;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.app.Activity;
import android.view.View.OnFocusChangeListener;
import android.widget.RelativeLayout;

public class NotesList extends ListActivity implements SearchView.OnQueryTextListener,
        AdapterView.OnItemLongClickListener,OnClickListener, OnScrollListener,
        OnFocusChangeListener{

    private static final String TAG = "NotesList";
    public static final int REFRESH = 0;
    public TextView countView;
    public NoteAdapter mNoteAdapter;
    ProgressDialog mPdialog;
    
    public ListView mList;
    private LinearLayout mListHeaderView;
    private SearchView mSearchView;
    public Button mAddNewNote;
    private int headerContentHeight;
    private Context mContext;
    
    public static final int NOTE_LIST = 0;
    public static final int NOTE_SETTOP_DELETE = 1;
    public static final int NOTE_SEARCH = 2;
    private int mEditMode = NOTE_LIST;
    private int mOldEditMode = NOTE_LIST;
    private ActionBar mActionBar;
    private View mEditArea;
    private View mTitle;
    private View mSearchTitle;
    private Boolean isSelectAll = false;
    private boolean mDbkground = false;
    public ProgressDialog mDeletingDialog = null;
    private int mFlag = 0;
    private QueryHandler mQuery;

    private final int MSG_REQUERY = 1;
    

    private LinearLayout mNoteListView;
    private Button mSetTopBt;
    private Button mDeleteBt;
    private Button mCancelBt;
    private Button mSelectAllBt;
    private String mQueryString = "";

    public static final int REQUEST_CODE_NOTEVIEW = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        setContentView(R.layout.noteslist_item_main);
	mContext = this;
        Log.i(TAG, "onCreate");
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        getContentResolver().registerContentObserver(NotePad.Notes.CONTENT_URI, true, mCobserver);

        mList= getListView();
        mList.setOnScrollListener(this);

        mAddNewNote = (Button)findViewById(R.id.btn_new_note);
        mAddNewNote.setOnClickListener(this);

        mList.setOnTouchListener(listTouchListen);
        mList.setOnItemLongClickListener(this);
        LayoutInflater inflater = getLayoutInflater();
        mListHeaderView = (LinearLayout) inflater.inflate(R.layout.noteslist_headview, null);
        
        measureView(mListHeaderView);
        headerContentHeight = mListHeaderView.getMeasuredHeight();
        
        mListHeaderView.setPadding(0, -1 * headerContentHeight, 0, 0);
        mListHeaderView.invalidate();
        
        mList.addHeaderView(mListHeaderView, null, true);

        mSearchView = (SearchView)mListHeaderView.findViewById(R.id.search);
        mSearchView.setFocusable(false);
        
        setupSearchView();
        mSearchView.setOnQueryTextFocusChangeListener(this);

        state = HIDE;

        mActionBar = getActionBar();
        if (mActionBar != null){
            mActionBar.hide();
        }
        mTitle = findViewById(R.id.title);
        mEditArea = findViewById(R.id.edit_delete_area);
        mSearchTitle = findViewById(R.id.title_search);

        mCancelBt = (Button)findViewById(R.id.cancle);
        mCancelBt.setOnClickListener(this);
        mSelectAllBt = (Button)findViewById(R.id.selectall);
        mSelectAllBt.setOnClickListener(this);
        mSetTopBt = (Button)findViewById(R.id.settop);
        mSetTopBt.setOnClickListener(this);
        mDeleteBt = (Button)findViewById(R.id.delete);
        mDeleteBt.setOnClickListener(this);

        mQuery = new QueryHandler(getContentResolver(), this);
	mNoteListView = (LinearLayout)findViewById(R.id.notelistview);
        mNoteAdapter = new NoteAdapter(this, null, 0);
        updateNoteList();
    }
    
    protected void onResume() {
        super.onResume();
	
        if (NotePad.Notes.sSaveNoteFlag) {
            Toast.makeText(this, R.string.note_saved, Toast.LENGTH_LONG).show();
            NotePad.Notes.sSaveNoteFlag = false;
        } else if (NotePad.Notes.sSaveNoNote) {
            Toast.makeText(this, R.string.save_none, Toast.LENGTH_LONG).show();
            NotePad.Notes.sSaveNoNote = false;
        }
        
    }
    
    public void intoModify()
    {
        if (mEditMode == NOTE_LIST){
            changeEditMode(NOTE_SETTOP_DELETE);
        }
    }

    private void setNoteListLayoutParams(int mode) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mNoteListView.getLayoutParams();
        
        switch (mode){
            case NOTE_SETTOP_DELETE:
		params.addRule(RelativeLayout.ABOVE, R.id.edit_delete_area);
                params.addRule(RelativeLayout.BELOW, R.id.title);
                break;
            case NOTE_LIST:
		params.addRule(RelativeLayout.ABOVE, R.id.btn_new_note);
            case NOTE_SEARCH:
		params.addRule(RelativeLayout.BELOW, R.id.title_search);
                break;
        }

        mNoteListView.setLayoutParams(params);
    }

    public void addNoteHeadView() {
        mList.addHeaderView(mListHeaderView);
    }

    
    public void changeEditMode(int mode){

        setNoteListLayoutParams(mode);
        mOldEditMode = mEditMode;
        switch (mode){
            case NOTE_SETTOP_DELETE:
                if (mActionBar != null){
                    mActionBar.show();
                }
                setAdapterMode(NOTE_SETTOP_DELETE);

                mEditMode = NOTE_SETTOP_DELETE;
                mList.removeHeaderView(mListHeaderView);
                mAddNewNote.setVisibility(View.GONE);

                mTitle.setVisibility(View.VISIBLE);
                mList.setPadding(0,0,0,0);

                showAnimation(mode);
                break;
            case NOTE_LIST:
                if (mActionBar != null){
                    mActionBar.hide();
                }
                setAdapterMode(NOTE_LIST);

                showAnimation(mode);
                if (mOldEditMode == NOTE_SEARCH) {
                    setListAdapter(null);
                }
                mEditMode = NOTE_LIST;
                mListHeaderView.invalidate();
                mList.addHeaderView(mListHeaderView);

                mTitle.setVisibility(View.GONE);
                mSearchTitle.setVisibility(View.GONE);
                mEditArea.setVisibility(View.GONE);
                mList.setPadding(0,38,0,0);
                break;
            case NOTE_SEARCH:
                setAdapterMode(NOTE_SEARCH);
                mEditMode = NOTE_SEARCH;
                
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(((Activity) mContext).getWindow() != null && ((Activity) mContext).getWindow().getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(((Activity) mContext).getWindow().getCurrentFocus().getWindowToken(), 0);
                    mSearchView.clearFocus();
                }

                mList.removeHeaderView(mListHeaderView);
                mAddNewNote.setVisibility(View.GONE);
                mSearchTitle.setVisibility(View.VISIBLE);
                mList.setPadding(0,0,0,0);
                showAnimation(mode);
                break;
            default:
                break;
        }
    }

    public int getEditMode(){
        return mEditMode;
    }

    
    public void setAdapterMode(int mode){
        NoteAdapter adapter = (NoteAdapter)getListAdapter();
        adapter.setModeState(mode);
    }

    public void showAnimation(int mode){
        Animation upperIn = AnimationUtils.loadAnimation(this,R.anim.anim_edit_upper_in);
        Animation upperOut = AnimationUtils.loadAnimation(this, R.anim.anim_edit_upper_out);
        Animation bottomOut = AnimationUtils.loadAnimation(this,R.anim.anim_edit_bottom_out);
        final Animation bottomIn = AnimationUtils.loadAnimation(this, R.anim.anim_edit_bottom_in);
        switch(mode){
            case NOTE_SETTOP_DELETE:
                mTitle.startAnimation(upperIn);
                bottomOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEditArea.setVisibility(View.VISIBLE);
                        mEditArea.startAnimation(bottomIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mAddNewNote.startAnimation(bottomOut);
                break;
            case NOTE_LIST:
                mTitle.startAnimation(upperOut);
                bottomOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mAddNewNote.setVisibility(View.VISIBLE);
                        mAddNewNote.startAnimation(bottomIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                mEditArea.startAnimation(bottomOut);
                break;
            case NOTE_SEARCH:
                mAddNewNote.startAnimation(bottomOut);
                mSearchTitle.startAnimation(upperIn);
                
                break;
            default:
                break;
        }
    }

    public void showSearchBar()
    {
    	state = SHOW;
        updateHeaderView(SHOW);
        mSearchView.setFocusable(true);
        mSearchView.requestFocus();
        setSelection(0);
    }
    
    public void exportNote()
    {
        mPdialog = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        QueryHandler qh = new QueryHandler(this.getContentResolver(), this);
        qh.startQuery(4, 
                      mPdialog, 
                      getIntent().getData(), 
                      NotePad.Notes.PROJECTION, 
                      null, 
                      null, 
                      NotePad.Notes.sSortOrder);
    }

    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	return super.onKeyDown(keyCode, event);
    }


    private void measureView(View child) {
        ViewGroup.LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0,
                params.width);
        int lpHeight = params.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }
    
    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(true);
        mSearchView.onActionViewExpanded();

        if(mSearchView != null){
	     mSearchView.clearFocus();
        }
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getString(R.string.app_name));
    }
    
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            mList.clearTextFilter();
        } else {
            mList.setFilterText(newText.toString());
        }
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        mQueryString = query;
        querySearchData(query);
        changeEditMode(NOTE_SEARCH);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (mEditMode == NOTE_SETTOP_DELETE){
            changeEditMode(NOTE_LIST);
            mNoteAdapter.cancelSelected();
            
            if (isSelectAll) {
                isSelectAll = false;
                mSelectAllBt.setText(R.string.menu_select_all_button);
            }
            
        }else if (mEditMode == NOTE_SEARCH){
                Message msg = mHandler.obtainMessage(MSG_REQUERY);
                msg.sendToTarget();
        } else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNoteAdapter = null;
    }
    
    public void queryUpdateData() {
        mPdialog = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        mQuery.startQuery(0, 
                      mPdialog, 
                      getIntent().getData(), 
                      NotePad.Notes.PROJECTION, 
                      null, 
                      null, 
                      NotePad.Notes.sSortOrder);
    }

    private void querySearchData(String query) {
        mPdialog = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        mQuery.startQuery(0, 
                      mPdialog, 
                      getIntent().getData(), 
                      NotePad.Notes.PROJECTION, 
                      "notetext like ?", 
                      new String[]{"%"+query+"%"}, 
                      NotePad.Notes.sSortOrder);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_options_menu, menu);   
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NotesList.class), null, intent, 0, null);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mEditMode != NOTE_LIST) {
            menu.findItem(R.id.menu_muti_search).setVisible(false);
        } else if (NotePad.Notes.sNoteCount == 0 || state == SHOW) {
            menu.findItem(R.id.menu_muti_search).setVisible(false);
        } else {
            menu.findItem(R.id.menu_muti_search).setVisible(true);
        }
   
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent it;
        switch (item.getItemId()) {		
        case R.id.menu_muti_search:
        	showSearchBar();
            return true;
 
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        if (mEditMode == NOTE_SETTOP_DELETE){
            CheckBox cb = (CheckBox)v.findViewById(R.id.checkbox);
            mNoteAdapter.checkboxClickAction(position);
            if (cb.isChecked()) {
                cb.setChecked(false);
            } else {
                cb.setChecked(true);
            }
            return;
        }

        NoteItem noteitem = (NoteItem)l.getAdapter().getItem(position);
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), noteitem.id);
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            setResult(RESULT_OK, new Intent().setData(uri));
        } else { 
            Intent it = new Intent(this, NoteView.class);
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.setData(uri);

	    int iCount = mNoteAdapter.getCount();
	    it.putExtra(PublicUtils.NOTE_RECORD_COUNT, iCount);
	    it.putParcelableArrayListExtra("notelist", mNoteAdapter.list);
            
            startActivityForResult(it, REQUEST_CODE_NOTEVIEW);
        }
    }

    private void updateNoteList() {
        if (mEditMode == NOTE_LIST)
        {
            mPdialog = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
            mQuery.startQuery(0,
                      mPdialog, 
                      getIntent().getData(), 
                      NotePad.Notes.PROJECTION, 
                      null, 
                      null, 
                      NotePad.Notes.sSortOrder);
        
            
        }
        else if (mEditMode == NOTE_SEARCH)
        {
            querySearchData(mQueryString);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_NOTEVIEW) {
            if (data != null && data.hasExtra("UpdateNoteFlag")) {
                boolean update_flag = data.getBooleanExtra("UpdateNoteFlag", false);
                if (update_flag == true) {
                    updateNoteList();
                }
            }
        }

    }

    private final static int RELEASE_To_REFRESH = 0;
    private final static int PULL_To_REFRESH = 1; 
    private final static int SHOW = 2;
    private final static int HIDE = 3;
    private final static int LOADING = 4;
    
    private boolean isFirstVisible = true;
    private boolean isRecored;
    private int startY;
    private int state;
    
    private boolean isBack;
    
    private final static int RATIO = 2;
    
    private OnTouchListener listTouchListen = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (mEditMode != NOTE_LIST) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (((Activity) mContext).getWindow() != null && ((Activity) mContext).getWindow().getCurrentFocus() != null) {
                        imm.hideSoftInputFromWindow(((Activity) mContext).getWindow().getCurrentFocus().getWindowToken(), 0);
                        mSearchView.clearFocus();
                    }
                    
                    isRecored = true;
                    startY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_UP: {
                    int tempY = (int) event.getY();
                    int yDelta = tempY - startY;
                    //Log.d("tui", "ACTION_UP, yDelta = " + yDelta);
                    if (Math.abs(yDelta) < 15) {//最少移动15个单位，否则不做处理
                        break;
                    }

                    if (isShowHeadView(yDelta)) {
                        state = SHOW;
                        updateHeaderView(SHOW);
                    } else {
                        state = HIDE;
                        updateHeaderView(HIDE);
                    }

                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    int tempY = (int) event.getY();
                    int yDelta = tempY - startY;
                    
                    if (Math.abs(yDelta) < 15) {//最少移动15个单位，否则不做处理
                        break;
                    }

                    if (yDelta > 0 && state == HIDE) { //向下滑
                        //Log.d("tui", "move down");
                        int top = -1 * headerContentHeight + yDelta / RATIO;
                        if (top <= 0) {
                            mListHeaderView.setPadding(0, top, 0, 0);
                        }
                    } else if (yDelta < 0) {//向上滑
                        //Log.d("tui", "move up");
                        int top = yDelta / RATIO;
                        if (state == SHOW) {
                            //Log.d("tui", "move up, state == SHOW");
                            mListHeaderView.setPadding(0, top, 0, 0);
                        }
                    }
                    break;
                }
            }
            return false;
        }
    };

    private boolean isShowHeadView(int yDelta) {
        int top = mListHeaderView.getPaddingTop();
        
        if (!isFirstVisible){
            return false;
        }
        int delta = yDelta / RATIO - headerContentHeight;
        
        if (delta >= 0){
            return true;
        }

        return false;
    }


    private void updateHeaderView(int state) {
        switch (state) {
            case RELEASE_To_REFRESH:
                break;
            case PULL_To_REFRESH:
                break;
            case SHOW:
                mListHeaderView.setPadding(0, 0, 0, 0);
                break;
            case HIDE:
                mListHeaderView.setPadding(0, -1 * headerContentHeight, 0, 0);
                mSearchView.clearFocus();
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long rowid) {
        Log.d("tui", "i = " + position + ", l = " + rowid);
        //长按搜索框时，不切换状态
        if (position >= 0 && rowid >= 0 ){
            intoModify();
            return true;
        }

        return false;
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        Log.d("tui", "onClick");
        switch (id){
            case R.id.btn_new_note:
                createNewNote();
                break;
            case R.id.cancle:
                cancleEdit();
                break;
            case R.id.selectall:
                selectAllItem();
                break;
            case R.id.settop:
                setItemTop();
                break;
            case R.id.delete:
                deleteItem();
                break;
            default:
                break;
        }
    }

    private void deleteItem() {
        if (mNoteAdapter.selectedNumber() > 0) {
            AlertDialog.Builder bld = new AlertDialog.Builder(this);
            bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    mDbkground = true;
                    mDeletingDialog = ProgressDialog.show(NotesList.this, "", getString(R.string.delete_progress), true);
                    new Thread(new Runnable() {
                        public void run() {
                            mNoteAdapter.deleteSelectedNote();
                            NotePad.Notes.sDeleteFlag = true;
                            mDeletingDialog.dismiss();
                        }
                    }).start();

                }
            });
            bld.setNegativeButton(getString(R.string.delete_confirm_cancel), null);
            bld.setCancelable(true);
            bld.setMessage(getString(R.string.delete_confirm));
            bld.setTitle(getString(R.string.delete_confirm_title));
            AlertDialog dlg = bld.create();
            dlg.show();
        } else {
            Toast.makeText(this, R.string.no_selected, Toast.LENGTH_LONG).show();
        }

    }

    private void setItemTop() {
        final Handler handle = new Handler() {
            public void handleMessage(Message msg) {
                Log.d("tui", "setItemTop, Handler");
                queryUpdateData();
                changeEditMode(NOTE_LIST);
            }
        };

        if (mNoteAdapter.selectedNumber() > 0) {
            new Thread(new Runnable() {
                public void run() {
                    if (mNoteAdapter.getSelectedIsAllTop()) {
                        mNoteAdapter.NoteCancelTop(getContentResolver());
                    } else {
                        mNoteAdapter.NoteSetTop(getContentResolver());
                    }
                    handle.sendEmptyMessage(0);
                }
            }).start();
        } else {
            Toast.makeText(this, R.string.no_selected, Toast.LENGTH_LONG).show();
        }
    }

    private void selectAllItem() {
        if (isSelectAll == false) {
            setCheckBoxStatus(true);
            mNoteAdapter.selectAllOrNoCheckbox(true);
            isSelectAll = true;
        } else {
            setCheckBoxStatus(false);
            mNoteAdapter.selectAllOrNoCheckbox(false);
            isSelectAll = false;
        }

        setBottomButtonStatus();
    }

    public void setBottomButtonStatus() {
        if (NotePad.Notes.sNoteCount == NotePad.Notes.sDeleteNum) {
            mSelectAllBt.setText(R.string.menu_deselect_all_button);
            if (isSelectAll == false) {
                isSelectAll = true;
            }
        } else {
            mSelectAllBt.setText(R.string.menu_select_all_button);
            if (isSelectAll == true) {
                isSelectAll = false;
            }
        }

        if (mNoteAdapter.getSelectedIsAllTop())
        {
              mSetTopBt.setText(R.string.menu_cancel_settop); 
        }
	else
	{
              mSetTopBt.setText(R.string.menu_set_settop);
	}
    }

    private void cancleEdit() {
        onBackPressed();
    }

    private void setCheckBoxStatus(boolean status) {
        ListView listView = (ListView)findViewById(android.R.id.list);
        for (int i = 0; i < listView.getChildCount(); i ++) {
            View view = listView.getChildAt(i);
            CheckBox cb = (CheckBox)view.findViewById(R.id.checkbox);
            cb.setChecked(status);
        }
    }

    private void createNewNote() {
        Intent it;
        it = new Intent(NotesList.this, NoteView.class);
        int iCount = mNoteAdapter.getCount();
        it.putExtra(PublicUtils.NOTE_RECORD_COUNT, iCount);
        it.putParcelableArrayListExtra("notelist", mNoteAdapter.list);
        startActivityForResult(it, REQUEST_CODE_NOTEVIEW);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Log.d("tui", "onScroll, firstVisibleItem = "  +firstVisibleItem);
        if (firstVisibleItem == 0) {
            isFirstVisible = true;
        } else {
            isFirstVisible = false;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        Log.d("tui", "onFocusChange");
    }

    private ContentObserver mCobserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (mDbkground) {
                mFlag ++;
                if (mFlag == NotePad.Notes.sDeleteNum) {
                    mFlag = 0;
                    mDeletingDialog.cancel();
                    mDbkground = false;

                    Message msg = mHandler.obtainMessage(MSG_REQUERY);
                    msg.sendToTarget();
                }
            }
        }
    };

    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what){
                case MSG_REQUERY:
                    Log.d("tui", "MSG_REQUERY");
                    queryUpdateData();
		    changeEditMode(NOTE_LIST);
                    break;
                default:
                    break;
            }
        }
    };
}
