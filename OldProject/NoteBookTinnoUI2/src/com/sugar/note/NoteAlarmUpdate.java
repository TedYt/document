package com.sugar.note;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.io.IOException;
import android.util.Log;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.database.Cursor;
import java.util.Calendar;
import android.widget.Button;
import android.view.View;
import android.content.ContentValues;
import android.view.View.OnClickListener;

import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import java.text.ParseException;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;


public class NoteAlarmUpdate extends Activity {  // implements OnClickListener

    private Button mModifyAlarm;
    private Button mDeleteAlarm;
    public static final int REQUEST_CODE_NOTEALARM_SET  = 1005;
    private long mAlarmDate = 0;
    private Uri mUri;
    private Button mAlarmBtn;
    private Context mContext;
    private long mAlarmTime = 0;
    private LinearLayout mAlarmUpdate;
    private RelativeLayout mRelaLayout;

    private BroadcastReceiver mNoteAlarmUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(PublicUtils.ACTION_NOTEALARM_ALERT)) {
                  ShowAlertUpdateTime(-2);
            }
        }
    };

    private OnTouchListener RelaLayoutTouchListen = new OnTouchListener()
    {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
		      // TODO Auto-generated method stub
		      Log.e("notedb", "andy3344 NoteAlarmUpdate RelaLayoutTouchListen OnTouchListener() event.getAction() = "+event.getAction());
	              switch (event.getAction()) {
	                    case MotionEvent.ACTION_DOWN: {
		                  Log.e("notedb", "andy3344 NoteAlarmUpdate RelaLayoutTouchListen OnTouchListener() ACTION_DOWN ");
	                          finish();
	                          break;
	                    }
	                    case MotionEvent.ACTION_UP: {
				  Log.e("notedb", "andy3344 NoteAlarmUpdate RelaLayoutTouchListen OnTouchListener() ACTION_UP ");
	                          //finish();
	                          break;
	                    }
	                    case MotionEvent.ACTION_MOVE: {
	            
	                          break;
	                    }
	                   default: {
			   
	                         break;
	                   }
	             }
	        
	            return false;
	    }
    	
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.note_alarm_update);
	mContext = this;

        mUri = getIntent().getData();
	Log.e("notedb", "andy3344 NoteAlarmUpdate onCreate mUri = "+mUri);
	mAlarmTime = getIntent().getLongExtra(PublicUtils.NOTE_ALARM_DATETIME, 0);
	mAlarmUpdate = (LinearLayout)findViewById(R.id.alarm_btns);
	mRelaLayout = (RelativeLayout)findViewById(R.id.alarm_update);
	mRelaLayout.setOnTouchListener(RelaLayoutTouchListen);
	
	mModifyAlarm = (Button)findViewById(R.id.modify_btn);
        mModifyAlarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			//add[begin] by liumoujun at 2014.04.11 for notebook alarm
			        Log.e("notedb", "andy3344 NoteAlarmUpdate start NoteAlarmSetActivity Activity ");
				Intent intent = new Intent("com.sugar.note.NoteAlarmSetActivity");
				int start_alarm_set_flag = 1;
				intent.putExtra(PublicUtils.START_ALARM_SET_FLAG, start_alarm_set_flag);
				intent.putExtra(PublicUtils.NOTE_ALARM_DATETIME, mAlarmTime);
				startActivityForResult(intent,REQUEST_CODE_NOTEALARM_SET);
		      //add[end] by liumoujun at 2014.04.11 for notebook alarm
		                mAlarmUpdate.setVisibility(View.INVISIBLE);
			}
		});
        mDeleteAlarm = (Button)findViewById(R.id.delete_btn);
        mDeleteAlarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.e("notedb", "andy3344 DeleteNoteAlarm onClick");
				long alerttime = 0;
				Intent i = new Intent();
				DeleteNoteAlarm();
				i.putExtra("DeleteNoteAlert", alerttime);
                                setResult(RESULT_OK, i);
				finish();
			}
		});

	mAlarmBtn = (Button)findViewById(R.id.alarm_update_btn);
        mAlarmBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                              finish();
			}
		});

	IntentFilter intentFilter = new IntentFilter(PublicUtils.ACTION_NOTEALARM_ALERT);
        registerReceiver(mNoteAlarmUpdateReceiver, intentFilter);
        ShowAlertUpdateTime(mAlarmTime);
    }


    private void ShowAlertUpdateTime(long alerttime) {
          long Currtime = System.currentTimeMillis();

	  if (alerttime == 0)
	  {
                mAlarmBtn.setText("");
	  }
	  else if (alerttime == -2 || Currtime > alerttime)
	  {
                mAlarmBtn.setText(R.string.note_alerted);
	  }
	  else
	  {
	        try {
	              String atime = PublicUtils.FormatTimeToString(mContext,alerttime);
	              mAlarmBtn.setText(atime);
		} catch (ParseException e) {
		      // TODO Auto-generated catch block
		      e.printStackTrace();
		}
	  }
    }

    //add [begin] by liumoujun at 2014.04.12 for notebook alarm
    private void DeleteNoteAlarm() {
        String year;
        String month;
        String day;
        String hour;
        String minute;

        Log.e("notedb", "andy3344 DeleteNoteAlarm mUri = " + mUri);
        ContentValues values = new ContentValues();
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
        values.put(NotePad.Notes.ALERTED_DATE, 0);
        getContentResolver().update(
                mUri,
                values,
                null,
                null
        );

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setData(mUri);   //mWorkingNote.getNoteId()
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
        alarmManager.cancel(pendingIntent);
    }
//add [end] by liumoujun at 2014.04.12 for notebook alarm

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
          if (requestCode == REQUEST_CODE_NOTEALARM_SET)
          {
                if (resultCode == Activity.RESULT_CANCELED)
                {
                      finish();
		      Log.e("notedb", "andy3344 NoteAlarmUpdate Activity.RESULT_CANCELED ");
		      return;
                }
				
                if (resultCode == Activity.RESULT_FIRST_USER && data != null)
                {
                      Log.e("notedb", "andy3344 NoteAlarmUpdate Activity.RESULT_FIRST_USER ");
                      if (data.hasExtra("NoteAlertDateUpdate"))
                      {
                            mAlarmDate = data.getLongExtra("NoteAlertDateUpdate", 0);
		            Log.e("notedb", "andy3344 NoteAlarmUpdate onActivityResult mAlarmDate = "+mAlarmDate);
                      }
		      //Intent i = new Intent();
                      //i.putExtra("NoteAlertDate", mAlarmDate);
                      setResult(RESULT_FIRST_USER, data);
		      finish();
                }
          }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("notedb", "andy3344 NoteAlarmUpdate onResume  ");
    }


    @Override
    protected void onDestroy(){
	super.onDestroy();
	unregisterReceiver(mNoteAlarmUpdateReceiver);
	Log.e("notedb", "andy3344 NoteAlarmUpdate onDestroy  ");
    }

}
