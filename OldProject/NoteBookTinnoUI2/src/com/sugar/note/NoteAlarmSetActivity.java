package com.sugar.note;

import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.Handler;
import android.os.Message;

import android.text.format.DateFormat;

import com.sugar.note.TinnoDatePicker.OnDateChangedListener;


public class NoteAlarmSetActivity extends Activity {
    /** Called when the activity is first created. */
	private static final String TAG = "SendTimingMsgActivity";
	private static final String NETWORK_STATS_UPDATED = "com.android.server.action.NETWORK_STATS_UPDATED";
	private static final boolean dbg = true;
	private static final int CHOOSE_SIM_TO_SEND = 0;
	private TextView mDateDispaly;
	private TextView mTimeDisplay;
	private LinearLayout mLinearLayout_date;
	private LinearLayout mLinearLayout_time;
	private Button mButtonCancel;
	private Button mButtonSend;

	private int mYear;
	private int mMonth;
	private int mDay;
	private int mHour;
	private int mMinute;


	//private boolean isInit = false;
	private Context mCtx;
	private String msgContent;
	private int mSelectedSimId;
	private static final int MSG_UPDATA_UI = 0;
	private static final int MSG_NETWORK_STATS_UPDATED = 1;
	private static final int MSG_RUN_INIT = 2;
	private NewworkChangeReceiver mReceiver;

	//private Uri mUri;
	private int start_alarm_set_flag = -1;
	private TinnoDatePicker mDatePicker;
	private TinnoTimePicker mTimePikcer;
	private long mOldAlarmTime = 0;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.notebook_alarm_set);
		mCtx = this;
		//DataManager.init(mCtx);
		Intent intent = getIntent();
		start_alarm_set_flag = intent.getIntExtra(PublicUtils.START_ALARM_SET_FLAG, -1);
		mOldAlarmTime = intent.getLongExtra(PublicUtils.NOTE_ALARM_DATETIME, 0);
		//mUri = intent.getData();
		registerBroadcastReceiver();
		initResource();
		registerListener();
		//initWheelViewRes();
		sendMsg(MSG_RUN_INIT);
	}

	private void registerBroadcastReceiver(){
		if(dbg)Log.d(TAG, "registerBroadcastReceiver()!");
		mReceiver = new NewworkChangeReceiver();
		registerReceiver(mReceiver, new IntentFilter(NETWORK_STATS_UPDATED));
	}
	
	public class NewworkChangeReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(NETWORK_STATS_UPDATED)){
				if(dbg)Log.d(TAG, "broadcastReceiver:"+NETWORK_STATS_UPDATED);
				sendMsg(MSG_NETWORK_STATS_UPDATED);
			}
		}
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(dbg)Log.d(TAG, "msg.what:"+msg.what);
			switch(msg.what){
				case MSG_UPDATA_UI:
					updateDisplayDateAndTIme(mYear,mMonth,mDay,mHour,mMinute);
					break;
				case MSG_NETWORK_STATS_UPDATED:
					sendMsg(MSG_UPDATA_UI);
					break;
				case MSG_RUN_INIT:
					startRun();
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	};
	
	private void sendMsg(int msg_what){
		Message m = new Message();
		m.what = msg_what;
		mHandler.sendMessage(m);
	}

   
	private void startRun() {
		new Thread(new Runnable() {
		    public void run() {
			//initData();
			sendMsg(MSG_UPDATA_UI);
		    }
		}, "INIT DATA..").start();
	}


	private void initResource(){
		mDateDispaly = (TextView)findViewById(R.id.id_set_year_moth_day);
		mTimeDisplay = (TextView)findViewById(R.id.id_set_time);
		mButtonCancel = (Button)findViewById(R.id.id_button_cancel);
		mButtonSend = (Button)findViewById(R.id.id_button_send);
		mLinearLayout_date = (LinearLayout)findViewById(R.id.linerlayout_date);
		mLinearLayout_time = (LinearLayout)findViewById(R.id.linerlayout_time);
		mDatePicker = (TinnoDatePicker)findViewById(R.id.date_picker);
		mTimePikcer = (TinnoTimePicker)findViewById(R.id.time_picker);
		mTimePikcer.setIs24HourView(is24Hour());
	}
	private void registerListener() {
		final Calendar mCalendar = Calendar.getInstance();
		Log.e("note", "lmj5588 NoteAlarmSetActivity registerListener mOldAlarmTime == "+mOldAlarmTime);
        if (mOldAlarmTime > 0) {
            mCalendar.setTimeInMillis(mOldAlarmTime);
        }
        mYear = mCalendar.get(Calendar.YEAR);
		mMonth = mCalendar.get(Calendar.MONTH) +1; 
		mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		//mHour = mCalendar.getTime().getHours();
		//mMinute = mCalendar.getTime().getMinutes();
		mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
		mMinute = mCalendar.get(Calendar.MINUTE);

		mDatePicker.init(mYear, mMonth - 1, mDay, new OnDateChangedListener() {
			
			@Override
			public void onDateChanged(TinnoDatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				mYear = year;
				mMonth = monthOfYear + 1;
				mDay = dayOfMonth;
				if (checkSet()) {
					mButtonSend.setEnabled(true);
					mButtonSend.setTextColor(0xff000000);
				} else {
					mButtonSend.setEnabled(false);
					mButtonSend.setTextColor(0xff898989);
				}
				monthOfYear++;
				String monthstr = (monthOfYear <= 9) ? ("0"+monthOfYear) : (""+monthOfYear);
		                String daystr = (dayOfMonth <= 9) ? ("0"+dayOfMonth) : (""+dayOfMonth);
				String date;
				switch (getDateFormat()) {
				case 0: // MM-dd-yyyy
					date = monthstr
							+ "/"
							+ daystr
							+ "/"
							+ year;
					break;
				case 1: // dd-MM-yyyy
					date = daystr
							+ "/"
							+ monthstr
							+ "/"
							+ year;
					break;
				default: // yyyy-MM-dd
					date = year 
						        + "/"
							+ monthstr
							+ "/"
							+ daystr;
					break;
				}
				mDateDispaly.setText(date);
			}
		});
		
		mTimePikcer.setOnTimeChangedListener(new TinnoTimePicker.OnTimeChangedListener() {

			@Override 
			public void onTimeChanged(TinnoTimePicker view, int hourOfDay,
					int minute) {
				mHour = hourOfDay;
				mMinute = minute;
				if (checkSet()) {
					mButtonSend.setEnabled(true);
					mButtonSend.setTextColor(0xff000000);
				} else {
					mButtonSend.setEnabled(false);
					mButtonSend.setTextColor(0xff898989);
				}
				String h;
				String time;
				if (is24Hour()) {  //is24Hour()
					h = (hourOfDay <= 9) ? ("0" + hourOfDay)
							: ("" + hourOfDay);
					time = h + ":" + ((minute <= 9) ? ("0" + minute) : (minute));
				} else {
					if (hourOfDay == 0) {
						h = "12";
						time = h + ":" + ((minute <= 9) ? ("0" + minute + " AM") : (minute + " AM"));
					} else {
						int nhourOfDay = hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
						String amPm = hourOfDay >= 12 ? "PM" : "AM";
						h = (nhourOfDay <= 9) ? ("0" + nhourOfDay)
								: ("" + nhourOfDay);
						time = h + ":" + ((minute <= 9) ? ("0" + minute + " " + amPm) : (minute + " " + amPm));
					}
				}
				mTimeDisplay.setText(time);
			}
			
		});

                
		mTimePikcer.setCurrentTime(mHour, mMinute);

		mDateDispaly.setTextColor(0xff949494);
		mTimeDisplay.setTextColor(0xff0bada5);
		
		mLinearLayout_date.setOnClickListener(new OnClickListener() {
		public void onClick(View v) {
		                mDateDispaly.setTextColor(0xff0bada5);
				mTimeDisplay.setTextColor(0xff949494);
				mTimePikcer.setVisibility(View.INVISIBLE);
		                mDatePicker.setVisibility(View.VISIBLE);
			}
		});
		mLinearLayout_time.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
			        mDateDispaly.setTextColor(0xff949494);
		                mTimeDisplay.setTextColor(0xff0bada5);
				mTimePikcer.setVisibility(View.VISIBLE);
			        mDatePicker.setVisibility(View.INVISIBLE);
			}
		});

		mButtonCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			        Intent i = new Intent();
				setResult(RESULT_CANCELED, i);
				finish();
			}
		});

       
		mButtonSend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			       Calendar mDate = Calendar.getInstance();

			       mDate.set(Calendar.YEAR, mYear);
                               mDate.set(Calendar.MONTH, mMonth - 1);
                               mDate.set(Calendar.DAY_OF_MONTH, mDay);
                               mDate.set(Calendar.HOUR_OF_DAY, mHour);
                               mDate.set(Calendar.MINUTE, mMinute);
			       Log.e(TAG, "nb alarm set andy1122 set datetime = "+mDate.getTime().toString());
			       setAlertDate(mDate.getTimeInMillis(),true);
			}
		});
	}

    public void setAlertDate(long date, boolean set) {
        if (start_alarm_set_flag == -1) {
            return;
        }
        Intent i = new Intent();

        if (start_alarm_set_flag == 0) {
            i.putExtra("NoteAlertDate", date);
            setResult(RESULT_OK, i);
        } else if (start_alarm_set_flag == 1) {
            i.putExtra("NoteAlertDateUpdate", date);
            setResult(RESULT_FIRST_USER, i);
        }
        finish();

    }

	private boolean checkSet(){
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH) +1; 
		int d = cal.get(Calendar.DAY_OF_MONTH);
		int h = cal.getTime().getHours();
		int mi = cal.getTime().getMinutes();
		
		
		if(mYear > y){
			return true;
		}
		if(mYear == y && mMonth > m){
			return true;
		}
		if(mYear == y && mMonth == m && mDay > d){
			return true;
		}
		if(mYear == y && mMonth == m && mDay == d){
			if((mHour*60+mMinute) > (h*60+mi)){
				return true;
			}
		}
		return false;
	}

	private void updateDisplayDateAndTIme(int year,int month,int day,int hour,int minute){
		Log.e(TAG, "andy3344 updateDisplayDateAndTIme: begin 111");
		if((mDateDispaly == null) || (mTimeDisplay == null)){
			return;
		}
	
		String y = year +"";
		String m = (month <= 9) ? ("0"+month) : (""+month);
		String d = (day <= 9) ? ("0"+day) : (""+day);

//		String h = (hour <= 9) ? ("0"+hour) : (""+hour);
		String h ;

		String mi = (minute <= 9) ? ("0"+minute) : (""+minute);

//		String time = h+":"+mi;
		String time;

		String date = "2012-12-01";

        if (is24Hour()) //is24Hour()
        {
            h = (hour <= 9) ? ("0" + hour) : ("" + hour);
            time = h + ":" + mi;
        } else {

            if (hour >= 12) {
                h = ((hour - 12) <= 9) ? ("0" + (hour - 12)) : ("" + (hour - 12));

                time = h + ":" + mi + " PM";

            } else {
                h = (hour <= 9) ? ("0" + hour) : ("" + hour);

                time = h + ":" + mi + " AM";

            }
        }

        switch(getDateFormat()){
			case 0: //MM-dd-yyyy
				date = m+"/"+d+"/"+y;
				break;
			case 1: //dd-MM-yyyy
               date = d+"/"+m+"/"+y;
				break;
			default: //yyyy-MM-dd
               date = y+"/"+m+"/"+d;
				break;
		}

		mTimeDisplay.setText(time); //[TUI_MSG] johnlee2898 valid
		mDateDispaly.setText(date);

        if (checkSet()) {
            mButtonSend.setEnabled(true);
            mButtonSend.setTextColor(0xff000000);
        } else {
            mButtonSend.setEnabled(false);
            //mButtonSend.setTextColor(0xffc0c0c0);
            mButtonSend.setTextColor(0xff898989);
        }

        if(dbg)Log.e(TAG, "andy3344 updateDisplayDateAndTIme:"+date+" "+time);
	}

    private boolean is24Hour() {
           return DateFormat.is24HourFormat(this);
    }

	private int getDateFormat() {
		int ret = 0;
		String fmt = Settings.System.getString(getContentResolver(),
	            Settings.System.DATE_FORMAT);
		
		if(fmt == null){
			return ret;
		}

		if(fmt.equals("MM-dd-yyyy") || fmt.equals("EE-MMM-d-yyyy")){
			ret = 0;
		}
		else if(fmt.equals("dd-MM-yyyy") || fmt.equals("EE-d-MMM-yyyy")){
			ret = 1;
		}
		else{
			//yyyy-MM-dd  && yyyy-MMM-d-EE
			ret = 2;
		}
		
		if(dbg)Log.d(TAG, "getDateFormat()->fmt:"+fmt+" ret="+ret);
	    return ret;
	}

	public void onDestroy(){
		super.onDestroy();
		if(mReceiver != null){
			unregisterReceiver(mReceiver);
			mReceiver = null;
			if(dbg)Log.d(TAG, "unregisterReceiver(mReceiver) ");
		}
	}
}
