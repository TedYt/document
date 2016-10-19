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
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

//import com.mediatek.notebook.NoteAdapter.NoteItem;

public class NotesSearch extends ListActivity {

    private static final String TAG = "NotesList";
    public static final int REFRESH = 0;
    public TextView countView;
    public NoteAdapter noteadapter;
    ProgressDialog mPdialog;
    
    public ListView lv;
    private int mAllRecordCount = 0;
    private ArrayList<NoteItem> mAllNotelist;
   
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);
        setContentView(R.layout.noteslist_item_search);  
        Log.i(TAG, "onCreate");
        Intent intent = getIntent();
	mAllRecordCount = intent.getIntExtra(PublicUtils.NOTE_RECORD_COUNT, 0);
	mAllNotelist = intent.getParcelableArrayListExtra("notelist");
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
	actionBar.setDisplayShowTitleEnabled(false);
	//actionBar.setDisplayUseLogoEnabled(false);
	//actionBar.setDisplayHomeAsUpEnabled(false);
	actionBar.setDisplayShowHomeEnabled(false);
        ViewGroup view = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.notelist_action_bar, null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(view, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, 
                                                                 ActionBar.LayoutParams.WRAP_CONTENT,
                                                                 Gravity.CENTER_VERTICAL | Gravity.RIGHT));
        countView = (TextView)view.findViewById(R.id.note_count);   
        
        lv = getListView();
        
       
        
        //lv.setTextFilterEnabled(true);
       
    }
    
    public String [] select = {"note","song"};
    protected void onResume() {
        super.onResume();
        String query = getIntent().getExtras().getString("query");
        noteadapter = new NoteAdapter(this, null, 1);
        mPdialog = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        QueryHandler qh = new QueryHandler(this.getContentResolver(), this);
        qh.startQuery(1, 
                      mPdialog, 
                      getIntent().getData(), 
                      NotePad.Notes.PROJECTION, 
                      "notetext like ?", 
                      new String[]{"%"+query+"%"}, 
                      NotePad.Notes.sSortOrder);
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
    	Intent it = new Intent(this, NoteDelete.class);
        it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        this.startActivity(it);
    }
    
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        noteadapter = null;
    }
    
    public void queryUpdateData() {
        noteadapter = new NoteAdapter(this, null, 0);
        mPdialog = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        QueryHandler qh = new QueryHandler(this.getContentResolver(), this);
        qh.startQuery(0, 
                      mPdialog, 
                      getIntent().getData(), 
                      NotePad.Notes.PROJECTION, 
                      null, 
                      null, 
                      NotePad.Notes.sSortOrder);
    }

    

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        NoteItem noteitem = (NoteItem)l.getAdapter().getItem(position);
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), noteitem.id);
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            setResult(RESULT_OK, new Intent().setData(uri));
        } else { 
            Intent it = new Intent(this, NoteView.class);
            it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            it.setData(uri);
            it.putExtra(PublicUtils.NOTE_RECORD_COUNT, mAllRecordCount);
	    it.putParcelableArrayListExtra("notelist", mAllNotelist);
            this.startActivity(it);
        }
    }
}
