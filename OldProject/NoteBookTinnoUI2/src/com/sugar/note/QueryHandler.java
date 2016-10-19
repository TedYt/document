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

import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

//import com.mediatek.notebook.NoteAdapter.NoteItem;
import com.sugar.note.NotePad.Notes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.ParseException;
public class QueryHandler extends AsyncQueryHandler {
    private Context mContext;
    public NoteAdapter nadapter;
    ProgressDialog mPdialog;
    public QueryHandler(ContentResolver cr, Context context) {
        super(cr);
        mContext = context;
    }
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {  
        NotePad.Notes.sNoteCount = cursor.getCount();
        mPdialog = (ProgressDialog)cookie;
        
        if (token == 0) {
            nadapter = ((NotesList) mContext).mNoteAdapter;
            if (nadapter != null) {
                nadapter.cur = cursor;
            }
            setData(cursor, token);
            ((NotesList) mContext).setListAdapter(nadapter);
            ((NotesList) mContext).invalidateOptionsMenu();
        } else if (token == 2) {
            nadapter = ((NoteDelete)mContext).noteadapter;
            if (nadapter != null) {
                nadapter.cur = cursor;
            }
            setData(cursor, token);
            ((NoteDelete)mContext).setListAdapter(nadapter);
        } else if(token ==1) {
            nadapter = ((NotesSearch)mContext).noteadapter;
            if (nadapter != null) {
                nadapter.cur = cursor;
            }
            setData(cursor, token);
            ((NotesSearch)mContext).setListAdapter(nadapter);
        } else if(token == 4) {
            nadapter = ((NotesList)mContext).mNoteAdapter;
            try {
		exportData(cursor,token);
	    } catch (IOException e) {
		e.printStackTrace();
	    }
        }
        
        mPdialog.cancel();
    }
    public void setData(Cursor cursor, int token) {
        NoteItem item;
        String note;
        String createTime;
        String currentTime;
        String notegroup;
        long modifyTime;
	long alerttime;
	int  noteitemcol;
	String notetitle;
        int id;
	int istop;
	int sortindex;
        int i = 0;


        if (nadapter != null && nadapter.list != null) {
            nadapter.list.clear();
        }
        
        if (cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndex(Notes._ID);
            int titleColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_NOTETEXT);
	    int noteColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_NOTE);
            int createColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_CREATE_DATE);
            int groupColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_GROUP);
	    int colorColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_COLOR);
	    int alertColumn = cursor.getColumnIndex(Notes.ALERTED_DATE);
            int modifyColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_MODIFICATION_DATE);
	    int istopColumn = cursor.getColumnIndex(Notes.IS_TOP);
	    int sortindexColumn = cursor.getColumnIndex(Notes.SORT_INDEX);

            do {
                id = cursor.getInt(idColumn);
                note = cursor.getString(noteColumn);
		notetitle =cursor.getString(titleColumn);
		
                createTime = cursor.getString(createColumn);
                notegroup = cursor.getString(groupColumn);
                modifyTime = cursor.getLong(modifyColumn);
				
		noteitemcol = cursor.getInt(colorColumn);
		alerttime = cursor.getLong(alertColumn);

		istop = cursor.getInt(istopColumn);
		sortindex = cursor.getInt(sortindexColumn);
		
                item = new NoteItem();
                item.id = id;
                item.note = note;
		item.notetitle = notetitle;
                i++;
                
		try {
		    currentTime = PublicUtils.FormatTimeToString(mContext,modifyTime);
		    item.create_time = currentTime;
		} catch (ParseException e) {
		    e.printStackTrace();
		}
                item.modify_time = modifyTime;
                item.notegroup = getGroup(notegroup);
				
		item.alertdate = alerttime;
		item.itembgcol = noteitemcol;

		item.is_top = istop;
		item.sort_index = sortindex;
		
                nadapter.addList(item);   
            } while(cursor.moveToNext());
        }
        cursor.close();
    }

    public String getSepLine()
    {
    	String str = "-";
    	for(int i=0; i<5; i++)
    	{
    		str += str;
    	}
    	
    	return str;		
    }
    public void exportData(Cursor cursor, int token) throws IOException {
        NoteItem item;
        String note;
        String createTime;
        String currentTime;
        String notegroup;
        long modifyTime;
        int id;
        File file;
        File fileName;
        
        List<NoteItem> list = new ArrayList<NoteItem>();
        
        if (cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndex(Notes._ID);
            int titleColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_NOTE);
            int createColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_CREATE_DATE);
            int groupColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_GROUP);
            int modifyColumn = cursor.getColumnIndex(Notes.COLUMN_NAME_MODIFICATION_DATE);
            do {
                id = cursor.getInt(idColumn);
                note = cursor.getString(titleColumn);
                createTime = cursor.getString(createColumn);
                notegroup = cursor.getString(groupColumn);
                modifyTime = cursor.getLong(modifyColumn);
                item = new NoteItem();
                item.id = id;
                item.note = note;
                currentTime = currentDay(createTime);
                item.modify_time = modifyTime;
                item.create_time = currentTime;
                item.notegroup = getGroup(notegroup);
                list.add(item);   
            } while(cursor.moveToNext());
        }
        cursor.close();
        
        
        String filePath;
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
        	filePath = Environment.getExternalStorageDirectory().toString();
        	file = new File(filePath,"/NOTE");
        	if(!file.exists()) {
        	    file.mkdir();
        	}
        	fileName = new File(file,"note.txt");
        	if(!fileName.exists()) {
        	    try {
		        fileName.createNewFile();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
        	}
        	String text = getSepLine();
        	text += "\n";
        	for(int i=0; i<list.size(); i++) {
        	    text        += list.get(i).notegroup+"\n";
        	    text        += list.get(i).note+"\n";
        	    text        += list.get(i).create_time+"\n";
        	    text        += getSepLine()+"\n";
        	}
        	
        	FileOutputStream fos;
		try {
		    fos = new FileOutputStream(fileName);
		    fos.write(text.getBytes());
		    fos.close();
		} catch (FileNotFoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
        }
    }
    public String currentDay(String modifyDay) {
        Calendar c = Calendar.getInstance();
        String currentDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String currentYear = String.valueOf(c.get(Calendar.YEAR));
        String currentMonth = String.valueOf(c.get(Calendar.MONTH) + 1);
        String saveYear;
        String saveMonth;
        String saveDay;
        String []mt = modifyDay.split(" ");
        Resources resource = mContext.getResources();

        if (Locale.getDefault().getLanguage().equals("en")) {
            saveMonth = mt[1];
            saveDay = mt[2];
            saveYear = mt[0]; 
            if (currentYear.equals(saveYear)) {
                if (currentMonth.equals(saveMonth) && currentDay.equals(saveDay)) {
                    return mt[3];
                } else {
                    return NotePad.Notes.MONTH[Integer.valueOf(mt[1]) - 1] + " " + mt[2] + " " + mt[3];
                }
            } else {
                return NotePad.Notes.MONTH[Integer.valueOf(mt[1]) - 1] + " " + mt[2] + "," + mt[0] + " " + mt[3];
            }
        } else {
            String displayMonth = (String) resource.getString(R.string.month);
            String displayDay = (String) resource.getString(R.string.day);
            saveYear = mt[0]; 
            saveMonth = mt[1];
            saveDay = mt[2];
if (mt[1].equals("Jan")) {
        		mt[1] = "1";
        	} else if (mt[1].equals("Feb")) {
        		mt[1] = "2";
        	} else if (mt[1].equals("Mar")) {
        		mt[1] = "3";
        	} else if (mt[1].equals("Apr")) {
        		mt[1] = "4";
        	} else if (mt[1].equals("May")) {
        		mt[1] = "5";
        	} else if (mt[1].equals("June")) {
        		mt[1] = "6";
        	} else if (mt[1].equals("July")) {
        		mt[1] = "7";
        	} else if (mt[1].equals("Aug")) {
        		mt[1] = "8";
        	} else if (mt[1].equals("Sep")) {
        		mt[1] = "9";
        	} else if (mt[1].equals("Oct")) {
        		mt[1] = "10";
        	} else if (mt[1].equals("Nov")) {
        		mt[1] = "11";
        	} else if (mt[1].equals("Dec")) {
        		mt[1] = "12";
        	}
            if (currentYear.equals(saveYear)) {
                if (currentMonth.equals(saveMonth) && currentDay.equals(saveDay)) {
                    return mt[3];
                } else {
                    return mt[1] + displayMonth + mt[2] + displayDay + " " + mt[3]; 
                }
            } else {
                return mt[0] + "-" + mt[1] + "-" + mt[2] + " " + mt[3];
            }
        }
    }
    
    public int selectedNumber() {
        int count = nadapter.selectedNumber();
        return count;
    }
    
    public String getGroup(String i) {
        Resources resource;
        if (mContext == null) {
            resource = (Resources) ((NoteDelete)mContext).getResources(); 
        } else {
            resource = (Resources) mContext.getResources(); 
        }
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
}
