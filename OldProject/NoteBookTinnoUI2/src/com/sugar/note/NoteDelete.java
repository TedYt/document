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

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

//import com.mediatek.notebook.NoteAdapter.NoteItem;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

//add [begin] by yangkui for  the delete layout
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.LayoutInflater;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
//add [begin] by yangkui for the delete layout
public class NoteDelete extends ListActivity{
    private QueryHandler mQueryhandler;
    public ProgressDialog mLoading;
    public ProgressDialog mDeleting = null;
    private boolean mDbkground;
    public NoteAdapter noteadapter;
    private int mFlag = 0;
    public List<NoteItem> list = new ArrayList<NoteItem>(); 
//add [begin] by yangkui for  the delete layout
    public   Boolean SelectAllFlag;
    public   Button bt_selectall;
    public   Button bt_cancel;
    public   Button bt_settop;
    public   Button  bt_delete;
//add [begin] by yangkui for the delete layout
    private Context mContext;

    private ContentObserver mCobserver = new ContentObserver(new Handler()) {   
        @Override 
        public void onChange(boolean selfChange) { 
            super.onChange(selfChange);
            if (mDbkground) {
                mFlag ++;
                if (mFlag == NotePad.Notes.sDeleteNum) {
                    mFlag = 0;
                    mDeleting.cancel();
                    finish();
                    mDbkground = false;
                }
            }
        } 
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        NotePad.Notes.sNoteDelete = this;
        NotePad.Notes.sDeleteNum = 0;
	SelectAllFlag = false;
	mContext = this;
//remove [begin] by yangkui 20140412 remove actionbar 
/*
        ActionBar ab = getActionBar();
        ab.setHomeButtonEnabled(true);
        NotePad.Notes.sActionbar = ab;
        ab.setIcon(R.drawable.ic_title_bar_done);
        ab.setTitle("0" + "  " + getString(R.string.title_bar_selected));
 */
//remove [end] by yangkui 20140412 remove actionbar 		
        setContentView(R.layout.noteslist_item_delete_settop);
	setActionBarLayout(R.layout.actionbar_layout);//add [line] by yangkui for actionbar layout 20140414

        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        noteadapter = new NoteAdapter(this, null, 2);
        getContentResolver().registerContentObserver(getIntent().getData(), true, mCobserver);

	InitView();//add [line] by yangkui for the view init 20140114

    }
//add [begin] by yangkui for the view init 20140114
	public void InitView()
	{
		bt_selectall = (Button)findViewById(R.id.selectall);
		bt_selectall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(SelectAllFlag ==false)
				{
           		       		setCheckBoxStatus(true);
            		       		noteadapter.selectAllOrNoCheckbox(true);
                               		invalidateOptionsMenu();
					SelectAllFlag = true;
				}else{
	 			           setCheckBoxStatus(false);
	 			           noteadapter.selectAllOrNoCheckbox(false);
	 			           invalidateOptionsMenu();
					   SelectAllFlag =false;
				}
				
			}
		});
		
		bt_delete =(Button)findViewById(R.id.delete);
		bt_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				OnDeletemmenu();
			}
		});

		bt_settop =(Button)findViewById(R.id.settop);
		bt_settop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (noteadapter.getSelectedIsAllTop())
                               {
                                      noteadapter.NoteCancelTop(mContext.getContentResolver());
                               }
	                       else
	                       {
                                      noteadapter.NoteSetTop(mContext.getContentResolver());
	                       }

	                       finish();
			}
		});
		
		bt_cancel=(Button)findViewById(R.id.cancle);
		bt_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onBackPressed();
			}
		});
			
	}
//add [end] by yangkui for the view init 20140114
    @Override
	public void onBackPressed() {
	// TODO Auto-generated method stub
			super.onBackPressed();
	}
//add [begin] by yangkui for actionbar layout 20140414
	public void setActionBarLayout( int layoutId ){
		ActionBar actionBar = getActionBar( );
		if( null != actionBar ){
			actionBar.setDisplayShowHomeEnabled( false );
			actionBar.setDisplayShowCustomEnabled(true);

			LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflator.inflate(layoutId, null);
			ActionBar.LayoutParams layout = new ActionBar.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			actionBar.setCustomView(v,layout);
		}
	}
//add [begin] by yangkui for actionbar layout 20140414
    public void tmpFunc()
    {
    	mLoading = ProgressDialog.show(this, "",  getString(R.string.title_loading), true);
        mQueryhandler = new QueryHandler(this.getContentResolver(), this);
        mQueryhandler.startQuery(2, 
                      mLoading, 
                      getIntent().getData(), 
                      NotePad.Notes.PROJECTION, 
                      null, 
                      null, 
                      NotePad.Notes.sSortOrder);
    }
    
    public Handler mMyHandler = new Handler() 
    {

    	public void handleMessage(Message msg) 
    	{

    		super.handleMessage(msg);
    		switch (msg.what) 
    		{
    		case 1:
    			
    			tmpFunc();
    			break;
    		case 2:
    			
    			break;
    			
    		case 3:
    			
    			break;
    		case 4:
    			;
    			break;
    		
    		default:
    	        break;
    	    }
    	}
    };
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mDeleting != null && mDeleting.isShowing()) {
        	return;
        }
       
//        Thread task = new Thread()
//        {
//        	public void run()
//        	{
        		 mMyHandler.sendEmptyMessageDelayed(1,5);
//        	}
//        };
//        task.start();
       
        
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        noteadapter = null;
        getContentResolver().unregisterContentObserver(mCobserver);
    }
  //remove [begin] by yangkui 20140412 remove actionbar 

    public boolean onCreateOptionsMenu(Menu menu) {
        /* 
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_delete_menu, menu);
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, NoteDelete.class), null, intent, 0, null);*/
        return super.onCreateOptionsMenu(menu);
    }

//remove [end] by yangkui 20140412 remove actionbar
    public boolean onPrepareOptionsMenu(Menu menu) { 
        if (NotePad.Notes.sDeleteNum == 0) {
	//remove [begin] by yangkui 20140412 remove actionbar 
            /*menu.findItem(R.id.menu_no_select).setIcon(R.drawable.ic_clear_select_disable);
            menu.findItem(R.id.menu_no_select).setEnabled(false);
            menu.findItem(R.id.menu_delete).setIcon(R.drawable.ic_menu_delete_disable);
            menu.findItem(R.id.menu_delete).setEnabled(false);*/
	//remove [end] by yangkui 20140412 remove actionbar 
	   SelectAllFlag= false;
	   bt_selectall.setText(R.string.menu_select_all_button);
	   bt_delete.setEnabled(false);
	   bt_selectall.setText(R.string.menu_select_all_button);
        } else {
          //remove [begin] by yangkui 20140412 remove actionbar 
            /*
            menu.findItem(R.id.menu_no_select).setIcon(R.drawable.ic_clear_select);
            menu.findItem(R.id.menu_no_select).setEnabled(true);
            menu.findItem(R.id.menu_delete).setIcon(R.drawable.ic_menu_delete_selected);
            menu.findItem(R.id.menu_delete).setEnabled(true);
            */
	//remove [end] by yangkui 20140412 remove actionbar 
             SelectAllFlag= false;
	    bt_delete.setEnabled(true);
	    bt_selectall.setText(R.string.menu_select_all_button);
        }
        if (NotePad.Notes.sNoteCount == NotePad.Notes.sDeleteNum) {
	     //remove [begin] by yangkui 20140412 remove actionbar 
            /*
            menu.findItem(R.id.menu_all_select).setIcon(R.drawable.ic_select_all_disable);
            menu.findItem(R.id.menu_all_select).setEnabled(false);
              */
	   //remove [end] by yangkui 20140412 remove actionbar 
	    SelectAllFlag= true;
	    bt_selectall.setText(R.string.menu_deselect_all_button);
        } else {
            SelectAllFlag= false;
	    bt_selectall.setText(R.string.menu_select_all_button);
	   //remove [begin] by yangkui 20140412 remove actionbar 
            /*menu.findItem(R.id.menu_all_select).setIcon(R.drawable.ic_select_all);
            menu.findItem(R.id.menu_all_select).setEnabled(true);*/
	   //remove [end] by yangkui 20140412 remove actionbar 
        }

        Log.e("yangkui","NoteDelete onPrepareOptionsMenu  -------noteadapter.list.size() = "+noteadapter.list.size());
        
	if (noteadapter.getSelectedIsAllTop())
        {
              bt_settop.setText(R.string.menu_cancel_settop); 
        }
	else
	{
              bt_settop.setText(R.string.menu_set_settop);
	}

	if (NotePad.Notes.sDeleteNum == 0)
	{
              bt_settop.setEnabled(false);
	}
	else
	{
              bt_settop.setEnabled(true);
	}
	
        return true;
    }


    public  void  OnDeletemmenu(){
            if (noteadapter.selectedNumber() > 0) {
                AlertDialog.Builder bld = new AlertDialog.Builder(this);
                bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mDbkground = true;
                        mDeleting = ProgressDialog.show(NoteDelete.this,"", getString(R.string.delete_progress), true);
                        new Thread(new Runnable() 
                        { 
                            public void run() { 
                               noteadapter.deleteSelectedNote();
                                NotePad.Notes.sDeleteFlag = true;
                                mLoading.dismiss(); 
                            } 
                        }).start(); 
                        Handler handle = new Handler() { 
                            public void handleMessage(Message msg) { 
                                super.handleMessage(msg); 
                            }
                     };}
                });
                bld.setNegativeButton(getString(R.string.delete_confirm_cancel),null);
                bld.setCancelable(true);
                bld.setMessage(getString(R.string.delete_confirm));
                bld.setTitle(getString(R.string.delete_confirm_title));
                AlertDialog dlg = bld.create();
                dlg.show();
            } else {
                Toast.makeText(this, R.string.no_selected, Toast.LENGTH_LONG).show();
            }

	}
//remove [begin] by yangkui 20140412 remove actionbar 
/*	
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_delete:
            if (noteadapter.selectedNumber() > 0) {
                AlertDialog.Builder bld = new AlertDialog.Builder(this);
                bld.setPositiveButton(getString(R.string.delete_confirm_ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mDbkground = true;
                        mDeleting = ProgressDialog.show(NoteDelete.this,"", getString(R.string.delete_progress), true);
                        new Thread(new Runnable() 
                        { 
                            public void run() { 
                               noteadapter.deleteSelectedNote();
                                NotePad.Notes.sDeleteFlag = true;
                                mLoading.dismiss(); 
                            } 
                        }).start(); 
                        Handler handle = new Handler() { 
                            public void handleMessage(Message msg) { 
                                super.handleMessage(msg); 
                            }
                     };}
                });
                bld.setNegativeButton(getString(R.string.delete_confirm_cancel),null);
                bld.setCancelable(true);
                bld.setMessage(getString(R.string.delete_confirm));
                bld.setTitle(getString(R.string.delete_confirm_title));
                AlertDialog dlg = bld.create();
                dlg.show();
            } else {
                Toast.makeText(this, R.string.no_selected, Toast.LENGTH_LONG).show();
            }
            return true;
        case R.id.menu_all_select:
            setCheckBoxStatus(true);
            noteadapter.selectAllOrNoCheckbox(true);
            invalidateOptionsMenu();
            return true;
        case R.id.menu_no_select:
            setCheckBoxStatus(false);
            noteadapter.selectAllOrNoCheckbox(false);
            invalidateOptionsMenu();
            return true;
        default:
            finish();
            return super.onOptionsItemSelected(item);
        }
    }
   */
//remove [end] by yangkui 20140412 remove actionbar  

    private void setCheckBoxStatus(boolean status) {
        ListView listView = (ListView)findViewById(android.R.id.list);  
        for (int i = 0; i < listView.getChildCount(); i ++) {
            View view = listView.getChildAt(i);  
            CheckBox cb = (CheckBox)view.findViewById(R.id.isdelete);  
            cb.setChecked(status); 
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
         CheckBox cb = (CheckBox)v.findViewById(R.id.isdelete);
         noteadapter.checkboxClickAction(position);
         if (cb.isChecked()) {
             cb.setChecked(false);
         } else {
             cb.setChecked(true);
         }

         
    }
}
