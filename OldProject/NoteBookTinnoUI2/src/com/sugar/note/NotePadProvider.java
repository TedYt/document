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

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentProvider.PipeDataWriter;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;

public class NotePadProvider extends ContentProvider implements PipeDataWriter<Cursor> {
    private static final String TAG = "NotePadProvider";
    private static final String DATABASE_NAME = "note_pad.db";
    private static final int DATABASE_VERSION = 2;
    private static HashMap<String, String> sNotesProjectionMap;
    private static HashMap<String, String> sLiveFolderProjectionMap;
    private static final int READ_NOTE_NOTE_INDEX = 1;
    private static final int READ_NOTE_TITLE_INDEX = 2;
    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;
    private static final int LIVE_FOLDER_NOTES = 3;
    private static final UriMatcher S_MATCHER;
    private DatabaseHelper mOpenHelper;

    static {
        S_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        S_MATCHER.addURI(NotePad.AUTHORITY, "notes", NOTES); 
        S_MATCHER.addURI(NotePad.AUTHORITY, "notes/#", NOTE_ID);
        S_MATCHER.addURI(NotePad.AUTHORITY, "live_folders/notes", LIVE_FOLDER_NOTES);
        sNotesProjectionMap = new HashMap<String, String>();
        sNotesProjectionMap.put(NotePad.Notes._ID, NotePad.Notes._ID);
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_NOTETEXT, NotePad.Notes.COLUMN_NAME_NOTETEXT); //mod  by liumoujun at 2014.04.09 for notebook record text
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_NOTE, NotePad.Notes.COLUMN_NAME_NOTE);
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_GROUP, NotePad.Notes.COLUMN_NAME_GROUP);
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_COLOR, NotePad.Notes.COLUMN_NAME_COLOR);//line by yangkui 2014.03.28 for col column
        sNotesProjectionMap.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE,
                NotePad.Notes.COLUMN_NAME_CREATE_DATE);
        sNotesProjectionMap.put(
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE,
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE);
    //add[begin] by liumoujun at 2014.04.03 for notebook alarm
	sNotesProjectionMap.put(
                NotePad.Notes.ALERTED_DATE,
                NotePad.Notes.ALERTED_DATE);
   //add[end] by liumoujun at 2014.04.03 for notebook alarm

        sNotesProjectionMap.put(
                NotePad.Notes.NOTE_ALERT_NUM,
                NotePad.Notes.NOTE_ALERT_NUM);

   //add [begin] by liumoujun at 2014.04.22 for sorting note record
        sNotesProjectionMap.put(
                NotePad.Notes.IS_TOP,
                NotePad.Notes.IS_TOP);
        sNotesProjectionMap.put(
                NotePad.Notes.SORT_INDEX,
                NotePad.Notes.SORT_INDEX);
   //add [begin] by liumoujun at 2014.04.22 for sorting note record
   
        sLiveFolderProjectionMap = new HashMap<String, String>();
    }

   static class DatabaseHelper extends SQLiteOpenHelper {
       DatabaseHelper(Context context) {
           super(context, DATABASE_NAME, null, DATABASE_VERSION);
       }

       @Override
       public void onCreate(SQLiteDatabase db) {
           db.execSQL("CREATE TABLE " + NotePad.Notes.TABLE_NAME + " ("
                   + NotePad.Notes._ID + " INTEGER PRIMARY KEY,"
                   + NotePad.Notes.COLUMN_NAME_NOTETEXT + " TEXT,"  //mod  by liumoujun at 2014.04.09 for notebook record text
                   + NotePad.Notes.COLUMN_NAME_NOTE + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                   + NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER,"
		   + NotePad.Notes.ALERTED_DATE + " INTEGER,"  //add by liumoujun at 2014.04.03 for notebook alarm
		   + NotePad.Notes.NOTE_ALERT_NUM + " INTEGER,"
                   + NotePad.Notes.COLUMN_NAME_GROUP + " TEXT,"
                   + NotePad.Notes.COLUMN_NAME_COLOR+ " INTEGER,"//line by yangkui 2014.03.28 for col column
            //add [begin] by liumoujun at 2014.04.22 for sorting note record
                   + NotePad.Notes.IS_TOP + " INTEGER,"
                   + NotePad.Notes.SORT_INDEX + " INTEGER"
           //add [end] by liumoujun at 2014.04.22 for sorting note record
                   + ");");
       }

       @Override
       public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                   + newVersion + ", which will destroy all old data");
           db.execSQL("DROP TABLE IF EXISTS notes");
           onCreate(db);
       }
   }
   @Override
   public boolean onCreate() {
       mOpenHelper = new DatabaseHelper(getContext());
       return true;
   }
   @Override
   public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
           String sortOrder) {
       SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
       qb.setTables(NotePad.Notes.TABLE_NAME);
       switch (S_MATCHER.match(uri)) {
           case NOTES:
               qb.setProjectionMap(sNotesProjectionMap);
               break;
           case NOTE_ID:
               qb.setProjectionMap(sNotesProjectionMap);
               qb.appendWhere(
                   NotePad.Notes._ID +    
                   "=" +
                   uri.getPathSegments().get(NotePad.Notes.NOTE_ID_PATH_POSITION));
               break;          
           default:
               throw new IllegalArgumentException("Unknown URI " + uri);
       }
       SQLiteDatabase db = mOpenHelper.getReadableDatabase();
       Cursor c = qb.query(
           db,            
           projection,    
           selection,     
           selectionArgs, 
           null,          
           null,          
           sortOrder        
       );
       c.setNotificationUri(getContext().getContentResolver(), uri);
       return c;
   }

   @Override
   public String getType(Uri uri) {
       return NotePad.Notes.CONTENT_ITEM_TYPE;
    }

    static ClipDescription sNoteStreamTypes = new ClipDescription(null,
            new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN });

    public void writeDataToPipe(ParcelFileDescriptor output, Uri uri, String mimeType,
            Bundle opts, Cursor c) {
        FileOutputStream fout = new FileOutputStream(output.getFileDescriptor());
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new OutputStreamWriter(fout, "UTF-8"));
            pw.println(c.getString(READ_NOTE_TITLE_INDEX));
            pw.println("");
            pw.println(c.getString(READ_NOTE_NOTE_INDEX));
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Ooops", e);
        } finally {
            c.close();
            if (pw != null) {
                pw.flush();
            }
            try {
                fout.close();
            } catch (IOException e) {
                Log.i(TAG, "fout.close()", e);
            }
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        String year;
        String month;
        String day;
        String hour;
        String minute;
        String createTime;
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        Long now = Long.valueOf(System.currentTimeMillis());
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
        createTime = String.valueOf(year) +
                             " " + 
                             month +
                             " " + 
                             day + 
                             " " +
                             hour +
                             ":" +
                             minute;
        
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_CREATE_DATE)) {
            values.put(NotePad.Notes.COLUMN_NAME_CREATE_DATE, createTime);
        }

        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE)) {
            values.put(NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
        }


   //mod [begin] by liumoujun at 2014.04.09 for notebook record text
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_NOTETEXT)) {
            Resources r = Resources.getSystem();
            values.put(NotePad.Notes.COLUMN_NAME_NOTETEXT, ""); //r.getString(android.R.string.untitled)
        }
  //mod [end] by liumoujun at 2014.04.09 for notebook record text

        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_NOTE)) {
            values.put(NotePad.Notes.COLUMN_NAME_NOTE, "");
        }

        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_GROUP)) {
            values.put(NotePad.Notes.COLUMN_NAME_GROUP, "");
        }
//begin by yangkui 2014.03.28 for col column		
        if (!values.containsKey(NotePad.Notes.COLUMN_NAME_COLOR)) {
            values.put(NotePad.Notes.COLUMN_NAME_COLOR, "0");
        }
//begin by yangkui 2014.03.28 for col column

   //add [begin] by liumoujun at 2014.04.04 for notebook alarm
	if (!values.containsKey(NotePad.Notes.ALERTED_DATE)) {
            values.put(NotePad.Notes.ALERTED_DATE, 0);
        }
   //add [end] by liumoujun at 2014.04.04 for notebook alarm

   //add [begin] by liumoujun at 2014.04.22 for sorting note record
        if (!values.containsKey(NotePad.Notes.IS_TOP)) {
            values.put(NotePad.Notes.IS_TOP, 0);
        }
	if (!values.containsKey(NotePad.Notes.SORT_INDEX)) {
            values.put(NotePad.Notes.SORT_INDEX, 1);
        }
   //add [end] by liumoujun at 2014.04.22 for sorting note record

        if (!values.containsKey(NotePad.Notes.NOTE_ALERT_NUM)) {
            values.put(NotePad.Notes.NOTE_ALERT_NUM, 0);
        }
		
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insert(
            NotePad.Notes.TABLE_NAME,        
            NotePad.Notes.COLUMN_NAME_NOTE,                                  
            values                           
        );

        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        NotePad.Notes.sSdcardFull = true;
        return null;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
	String finalWhere = null;
    /*
        count = db.delete(
            NotePad.Notes.TABLE_NAME,  
            where,                
            whereArgs                  
        );   
    */
	if (uri == null)
        {
              return count;
        }

	switch(S_MATCHER.match(uri))
	{
		case NOTES:
			count = db.delete(NotePad.Notes.TABLE_NAME, where, whereArgs);
			break;
				
		case NOTE_ID:
			long id = ContentUris.parseId(uri);
			finalWhere = NotePad.Notes._ID + " = " + id;
		     /*
			if((where != null) && (!where.equals("")))
			{
				finalWhere += " AND " + where;
			}
		    */
			count = db.delete(NotePad.Notes.TABLE_NAME, finalWhere, null);
			break;
				
		default:
			throw new IllegalArgumentException("unkonw Uri:"+uri);
	}
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;
        String finalWhere = null;
/*
	if (uri != null)
	{
              finalWhere =
                      NotePad.Notes._ID +                              
                      " = " +                                          
                      uri.getPathSegments().                           
                          get(NotePad.Notes.NOTE_ID_PATH_POSITION);
	}

	if (finalWhere != null && !finalWhere.equals(""))
	{
              if (where != null && !where.equals("")) {
                    finalWhere = finalWhere + " AND " + where;
              }
	}
	else
	{
              finalWhere = where;
	}
*/

        if (uri == null)
        {
              return count;
        }

	switch(S_MATCHER.match(uri))
	{
		case NOTES:
			count = db.update(NotePad.Notes.TABLE_NAME, values, where, whereArgs);
			break;
				
		case NOTE_ID:
			long id = ContentUris.parseId(uri);
			finalWhere = NotePad.Notes._ID + " = " + id;
		    /*
			if((where != null) && (!where.equals("")))
			{
				finalWhere += " AND " + where;
			}
		   */
			count = db.update(NotePad.Notes.TABLE_NAME, values, finalWhere, null);
			break;
				
		default:
			throw new IllegalArgumentException("unkonw Uri:"+uri);
	}

    /*
        count = db.update(
            NotePad.Notes.TABLE_NAME, 
            values,                   
            finalWhere,                                      
            whereArgs                                           
        );
   */
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    DatabaseHelper getOpenHelperForTest() {
        return mOpenHelper;
    }
}
