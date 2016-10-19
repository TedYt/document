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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

//import net.micode.notes.R;
//import net.micode.notes.data.Notes;
//import net.micode.notes.tool.DataUtils;

import java.io.IOException;
import android.util.Log;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class AlarmAlertActivity extends Activity {  //implements OnClickListener, OnDismissListener
    private long mNoteId;
    private String mSnippet = "";
    private static final int SNIPPET_PREW_MAX_LEN = 60;
    MediaPlayer mPlayer;
    private Button mButtonOK;
    private Button mButtonEnter;
    private Context mCtx;
    private TextView mAlarmText;
    private static final int EVENT_NOTE_ALARMALERT = 3000;
    private static final int NOTE_ALARMALERT_TIME = 300000;
    private static final int NOTE_ALARMALERT_INTERVAL_TIME = 300000;
    private NoteHomeReceiver mReceiverHome = new NoteHomeReceiver();
    private IntentFilter mIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    

    private Handler mAlarmAlertHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            
            switch (msg.what) {
            case EVENT_NOTE_ALARMALERT:
           /*
		int alert_num = PublicUtils.getAlarmAlertNum(mCtx.getContentResolver(), mNoteId);
		if (alert_num >= 1)
		{
	              long AlarmAlertTime = 0;
		      long Currtime = System.currentTimeMillis();
		      AlarmAlertTime = Currtime + NOTE_ALARMALERT_INTERVAL_TIME;
                      Intent intent = new Intent(mCtx, AlarmReceiver.class);
	              Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, mNoteId);
                      intent.setData(noteUri);   //mWorkingNote.getNoteId()
                      PendingIntent pendingIntent = PendingIntent.getBroadcast(mCtx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                      AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
                      alarmManager.set(AlarmManager.RTC_WAKEUP, AlarmAlertTime, pendingIntent);
		      
		      stopAlarmSound();
                      finish();
		}
		else
		{
                      closeNoteAlarmDialog();
		}
	  */
	        stopAlarmSound();
                final Window win = getWindow();
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
            default:
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.note_alarm_dialog);
	mCtx = this;
        registerReceiver(mReceiverHome, mIntentFilter);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    //| WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        

        Intent intent = getIntent();

	mButtonOK = (Button)findViewById(R.id.id_button_ok);
	mButtonEnter = (Button)findViewById(R.id.id_button_enter);

	mButtonOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			        Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, mNoteId);
				closeNoteAlarmDialog();
				//PublicUtils.updateNoteAlertNum(mCtx.getContentResolver(), 0, noteUri);
			}
		});

       
       mButtonEnter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			      closeNoteAlarmDialog();
			      Intent intent = new Intent(mCtx, NoteView.class);
	                      Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, mNoteId);
			      //PublicUtils.updateNoteAlertNum(mCtx.getContentResolver(), 0, noteUri);
                              intent.setData(noteUri);
                              startActivity(intent);
			}
		});

        mAlarmText = (TextView)findViewById(R.id.note_alarm_dialog_text);

        try {
            mNoteId = Long.valueOf(intent.getData().getPathSegments().get(1));
            mSnippet = PublicUtils.getSnippetById(this.getContentResolver(), mNoteId);
            mSnippet = mSnippet.length() > SNIPPET_PREW_MAX_LEN ? mSnippet.substring(0,
                    SNIPPET_PREW_MAX_LEN) + getResources().getString(R.string.notelist_string_info)
                    : mSnippet;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

	
	if (!mSnippet.equals(""))
	{
              mAlarmText.setText(mSnippet);
	}
	else
	{
              mAlarmText.setText(R.string.default_note_text);
	}

        mPlayer = new MediaPlayer();
        playAlarmSound();
       
        //Uri noteUri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_ID_URI_BASE, mNoteId);
	//int alert_num = PublicUtils.getAlarmAlertNum(mCtx.getContentResolver(), mNoteId);
        //PublicUtils.updateNoteAlertNum(getContentResolver(), alert_num - 1, noteUri);
	mAlarmAlertHandler.sendEmptyMessageDelayed(EVENT_NOTE_ALARMALERT, NOTE_ALARMALERT_TIME);
    }


    

    public void closeNoteAlarmDialog() {
	
        Log.e("nb alarm", "AlarmAlertActivity andy3344  closeNoteAlarmDialog");
	Intent intent = new Intent(PublicUtils.ACTION_NOTEALARM_ALERT);
	sendBroadcast(intent);
        stopAlarmSound();
        finish();
    }

    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    private void playAlarmSound() {
        Uri url = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);

        int silentModeStreams = Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

        if ((silentModeStreams & (1 << AudioManager.STREAM_ALARM)) != 0) {
            mPlayer.setAudioStreamType(silentModeStreams);
        } else {
            mPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
        }
        try {
            mPlayer.setDataSource(this, url);
            mPlayer.prepare();
            mPlayer.setLooping(true);
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stopAlarmSound() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeNoteAlarmDialog();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiverHome);
        super.onDestroy();
    }

    private class NoteHomeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if (reason != null && reason.equals("homekey")) {
                    stopAlarmSound();
                }
            }
       
        }
    }

}
