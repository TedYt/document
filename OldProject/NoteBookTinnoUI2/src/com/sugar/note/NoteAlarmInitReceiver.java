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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

//import net.micode.notes.data.Notes;
//import net.micode.notes.data.Notes.NoteColumns;

import android.provider.MediaStore;
import android.util.Log;

import java.io.File;


public class NoteAlarmInitReceiver extends BroadcastReceiver {

    private static final String [] PROJECTION = new String [] {
        NotePad.Notes._ID,
        NotePad.Notes.ALERTED_DATE
    };

    private static final int COLUMN_ID                = 0;
    private static final int COLUMN_ALERTED_DATE      = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
            long currentDate = System.currentTimeMillis();
            Cursor c = context.getContentResolver().query(NotePad.Notes.CONTENT_URI,
                    PROJECTION,
                    NotePad.Notes.ALERTED_DATE + ">?" ,  //+ NoteColumns.TYPE + "=" + Notes.TYPE_NOTE
                    new String[] { String.valueOf(currentDate) },
                    null);

            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        long alertDate = c.getLong(COLUMN_ALERTED_DATE);
                        Intent sender = new Intent(context, AlarmReceiver.class);
                        sender.setData(ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, c.getLong(COLUMN_ID)));
                        Log.e("nb alarm", "NoteAlarmInitReceiver andy1122  noteUri = "+ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, c.getLong(COLUMN_ID)));
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, sender, 0);
                        AlarmManager alermManager = (AlarmManager) context
                                .getSystemService(Context.ALARM_SERVICE);
                        alermManager.set(AlarmManager.RTC_WAKEUP, alertDate, pendingIntent);
                    } while (c.moveToNext());
                }
                c.close();
            }
        }else if (action.equals(Intent.ACTION_SHUTDOWN) || action.equals(Intent.ACTION_REBOOT)){
            Log.d("tui", "NoteAlarmInitReceiver, action = " + action);
            ContentResolver cr = context.getContentResolver();
            Cursor cursor = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, null,null,null,null);
            if (cursor != null && cursor.getCount() > 0){
                Log.d("tui", "count = " + cursor.getCount());
                int cIndex = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                while(cursor.moveToNext()){
                    String path = cursor.getString(cIndex);
                    File file = new File(path);
                    if (file.exists()){
                        file.delete();
                    }
                }

                cr.delete(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,null,null);
                cursor.close();
            }

            //删除分享时，临时保存的图片
            File dir = new File(PublicUtils.FOLD_SHARE_PIC);
            if (!dir.exists()) {
                return;
            }

            File[] files = dir.listFiles();
            for (File f : files) {
                Log.d("tui", " file's path " + f.getAbsolutePath());
                if (f.isFile()) {
                    f.delete();
                }
            }
        }
    }
}
