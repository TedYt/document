/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sugar.note;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;


import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import android.net.Uri;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.graphics.BitmapFactory;
import android.content.Intent;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.view.View;
import android.graphics.Canvas;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;


public class PublicUtils {
    public static final String TAG = "DataUtils";
    public static final String START_ALARM_SET_FLAG = "start_alarm_set_flag";
    public static final String NOTE_RECORD_COUNT = "note_record_count";
    public static final String ACTION_NOTEALARM_ALERT = "com.sugar.note.action.ALERT";
    //public static final String START_ALARM_UPDATE_DATETIME = "start_alarm_update_datetime";
    public static final String NOTE_ALARM_DATETIME = "note_alarm_datetime";

    public static final String FOLD_SHARE_PIC = "/storage/sdcard0/.TinnoNote/share_pic";

/**
     * Returns the Notebook record Text Context
     * By the Parameters record ID.
     */
    public static String getSnippetById(ContentResolver resolver, long noteId) {
        Cursor cursor = resolver.query(NotePad.Notes.CONTENT_URI,
                new String[]{NotePad.Notes.COLUMN_NAME_NOTETEXT},
                NotePad.Notes._ID + "=?",
                new String[]{String.valueOf(noteId)},
                null);

        String snippet = "";

        try {
            if (cursor != null && cursor.moveToFirst()) {
                snippet = cursor.getString(0);
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "get folder count failed:" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return snippet;
    }

    public static long getAlertTime(long alarmtime) {
	  long note_alerttime = 0;
	  
          Calendar c = Calendar.getInstance();
	  c.setTimeInMillis(alarmtime);
	  Log.e("note", "andy3344 PublicUtils getAlertTime alarmtime = "+alarmtime);

	  int time_sec = c.get(Calendar.SECOND);
	  if (time_sec > 0)
	  {
                time_sec = 0;
		c.set(Calendar.SECOND,time_sec);
	  }

	  note_alerttime = c.getTimeInMillis();
	  Log.e("note", "andy3344 PublicUtils getAlertTime note_alerttime = "+note_alerttime);
	  return note_alerttime;
    }

	
    public static void updateNoteAlertNum(ContentResolver resolver,int alert_num,Uri curNoteUri) {
	  ContentValues values = new ContentValues();
	  values.put(NotePad.Notes.NOTE_ALERT_NUM, alert_num);
	  resolver.update(
                curNoteUri,
                values,
                null,
                null
            );
    }
    

//add [begin] by yangkui at 20140421 for notebook time
	public static String FormatTimeToString (Context mContext,long time) throws ParseException {
        long Currtime;
        String time_year;
        String time_month;
        String time_day;
        String time_hour;
        String time_minute;
        String time_str;

        int time_weekday;
        long time_day_starttime;
        long time_day_endtime;
        boolean is24h = DateFormat.is24HourFormat(mContext);


        String[] weekday_name = mContext.getResources().getStringArray(R.array.weekday_name);
        Calendar c = Calendar.getInstance();

        Currtime = System.currentTimeMillis();

        c.setTimeInMillis(time);


        time_year = String.valueOf(c.get(Calendar.YEAR));
        time_month = String.valueOf(c.get(Calendar.MONTH) + 1);
        time_day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        time_weekday = c.get(Calendar.DAY_OF_WEEK);
        time_hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        time_minute = String.valueOf(c.get(Calendar.MINUTE));
        int hour = c.get(Calendar.HOUR_OF_DAY);

        if (c.get(Calendar.DAY_OF_MONTH) < 10) {
            time_day = "0" + time_day;
        }
        if (c.get(Calendar.MONTH) + 1 < 10) {
            time_month = "0" + time_month;
        }
         /*
	        if (c.get(Calendar.HOUR_OF_DAY) < 10) {
	        	time_hour = "0" + time_hour;
	        }
	    */

        if (c.get(Calendar.MINUTE) < 10) {
            time_minute = "0" + time_minute;
        }

        if (is24h == true) {
            time_hour = (hour <= 9) ? ("0" + hour) : ("" + hour);
            time_str = time_hour + ":" + time_minute;
        } else {

            if (hour >= 12) {
                time_hour = ((hour - 12) <= 9) ? ("0" + (hour - 12)) : ("" + (hour - 12));
                time_str = time_hour + ":" + time_minute + " PM";
            } else {
                time_hour = (hour <= 9) ? ("0" + hour) : ("" + hour);
                time_str = time_hour + ":" + time_minute + " AM";
            }
        }


        String str_time_day_starttime = time_year + "-" + time_month + "-" + time_day + " " + "00:00:00";
        String str_time_day_endtime = time_year + "-" + time_month + "-" + time_day + " " + "23:59:59";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time_day_starttime = sdf.parse(str_time_day_starttime).getTime();
        time_day_endtime = sdf.parse(str_time_day_endtime).getTime();

        if (Currtime > time) {
            if (Currtime > time_day_starttime && Currtime <= time_day_endtime) {
                return time_str;
            } else if ((Currtime > time_day_endtime) && (Currtime <= time_day_endtime + 24 * 60 * 60 * 1000)) {
                return mContext.getResources().getString(R.string.str_yesterday) + " " + time_str;
            } else if ((Currtime > time_day_endtime + 24 * 60 * 60 * 1000)
                    && (Currtime <= time_day_endtime + (7 - time_weekday) * 24 * 60 * 60 * 1000)) {
                return weekday_name[time_weekday - 1] + " " + time_str;
            }
        } else {
            if (Currtime > time_day_starttime && Currtime <= time_day_endtime) {
                return time_str;
            } else if ((Currtime > time_day_starttime - 24 * 60 * 60 * 1000) && (Currtime < time_day_starttime)) {
                return mContext.getResources().getString(R.string.str_tomorrow) + " " + time_str;
            } else if ((Currtime > time_day_starttime - (time_weekday - 1) * 24 * 60 * 60 * 1000) && (Currtime <= time_day_starttime - 24 * 60 * 60 * 1000)) {
                return weekday_name[time_weekday - 1] + " " + time_str;
            }
        }
        return time_year + "-" + time_month + "-" + time_day;
    }

/**
     * Returns the Notebook record Alarm Date
     * By the Parameters Uri.
     */
    public static long getAlarmDateByUri(ContentResolver resolver, Uri curNoteUri) {
        long alarmdate = 0;

        if (resolver == null) {
            Log.e("notedb", "getAlarmDateByUri  resolver is null!!!");
            return alarmdate;
        }

        if (curNoteUri == null) {
            Log.e("notedb", "getAlarmDateByUri  curNoteUri is null!!!");
            return alarmdate;
        }

        Cursor cursor = resolver.query(curNoteUri,
                new String[]{NotePad.Notes.ALERTED_DATE},
                null,
                null,
                null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                alarmdate = cursor.getLong(0);
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "get folder count failed:" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return alarmdate;

    }


    public static int getAlarmAlertNum(ContentResolver resolver, long noteId) {
        int alert_num = 0;

        if (resolver == null) {
            Log.e("notedb", "getAlarmDateByUri  resolver is null!!!");
            return alert_num;
        }


        Cursor cursor = resolver.query(NotePad.Notes.CONTENT_URI,
                new String[]{NotePad.Notes.NOTE_ALERT_NUM},
                NotePad.Notes._ID + "=?",
                new String[]{String.valueOf(noteId)},
                null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                alert_num = cursor.getInt(0);
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "get folder count failed:" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return alert_num;

    }


    public static int getTopNoteCount(ContentResolver resolver) {
        int iCount = 0;
        long isTop = 1;

        if (resolver == null) {
            Log.e("notedb", "getTopNoteCount  resolver is null!!!");
            return iCount;
        }


        Cursor cursor = resolver.query(NotePad.Notes.CONTENT_URI,
                new String[]{NotePad.Notes.SORT_INDEX},
                NotePad.Notes.IS_TOP + "=?",
                new String[]{String.valueOf(isTop)},
                NotePad.Notes.sSortOrder);


        try {
            if (cursor != null && cursor.moveToLast()) {
                iCount = cursor.getCount();
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "get folder count failed:" + e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return iCount;

    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }

        return inSampleSize;
    }

    public static int getInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }

        if (inSampleSize < 2) {
            inSampleSize = 1;
        } else if (inSampleSize >= 2 && inSampleSize < 4) {
            inSampleSize = 2;
        } else if (inSampleSize >= 4 && inSampleSize < 7) {
            inSampleSize = 4;
        } else if (inSampleSize >= 7 && inSampleSize < 14) {
            inSampleSize = 8;
        } else {
            return inSampleSize;
        }
        
        return inSampleSize;
    }

    public static boolean isStorageMounted() {
        String state = Environment.getExternalStorageState();
        Log.d("tui", "getStorageState, state = " + state);

        if (state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }

        return false;
    }

    public static void cancelNoteAlarm(Context curContext, Uri noteUri) {
         Intent intent = new Intent(curContext, AlarmReceiver.class);
         intent.setData(noteUri);
         PendingIntent pendingIntent = PendingIntent.getBroadcast(curContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
         AlarmManager alarmManager = ((AlarmManager) curContext.getSystemService(Context.ALARM_SERVICE));
         alarmManager.cancel(pendingIntent);
     }

    /**
     * 将View转换为Bitmap
     */
    public static Bitmap convertViewToBitmap(View v) {
        if (v == null) {
            return null;
        }

        v.buildDrawingCache();
        Bitmap bitmap = v.getDrawingCache();

        return bitmap;
    }

    private static Bitmap compressImage(Bitmap image) {  
  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;
        while ((baos.toByteArray().length / 1024 > 500) && (options >= 0)) {
            baos.reset();
            options -= 10;
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            //options -= 10;
        }
        
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public static Bitmap convertViewToBitmapEx(View v) {
        if (v == null) {
            return null;
        }
        
        int w = v.getWidth();
        int h = v.getHeight();
        Log.d("tui", "convertViewToBitmapEx, w = " + w + "  h = "+h);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas= new Canvas(bitmap);       
        v.draw(canvas);

        bitmap = compressImage(bitmap);
        return bitmap;
    }
}
