package com.sugar.note;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import android.app.Dialog;

/**
 * Created by user on 7/25/14.
 */
public class NoteAlarmSetView extends LinearLayout implements View.OnClickListener
                    ,TinnoDatePicker.OnDateChangedListener
                    ,TinnoTimePicker.OnTimeChangedListener{

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

    private Context mContext;

    private int start_alarm_set_flag = -1;
    private TinnoDatePicker mDatePicker;
    private TinnoTimePicker mTimePikcer;

    //private AlertDialog mDialog;
    private Dialog mDialog;

    public NoteAlarmSetView(Context context) {
        this(context, null);
    }

    public NoteAlarmSetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoteAlarmSetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initResource();
        registerListener();
        updateDisplayDateAndTIme(mYear,mMonth,mDay,mHour,mMinute);
    }

    private void initResource() {

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

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(mContext);

    }

    public void registerListener() {
        final Calendar mCalendar = Calendar.getInstance();

        long oldtime = ((NoteView)mContext).getAlertOldTime();
        if (oldtime > 0) {
            mCalendar.setTimeInMillis(oldtime);
        }

        mYear = mCalendar.get(Calendar.YEAR);
        mMonth = mCalendar.get(Calendar.MONTH) + 1;
        mDay = mCalendar.get(Calendar.DAY_OF_MONTH);
        mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = mCalendar.get(Calendar.MINUTE);

        mDatePicker.init(mYear, mMonth - 1, mDay, this);

        mTimePikcer.setOnTimeChangedListener(this);
        mTimePikcer.setCurrentTime(mHour, mMinute);

        final int colorSel = mContext.getResources().getColor(R.color.alarm_time_selected);
        final int colorUnsel = mContext.getResources().getColor(R.color.alarm_time_unselected);

        mDateDispaly.setTextColor(colorUnsel);
        mTimeDisplay.setTextColor(colorSel);

        mLinearLayout_date.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mDateDispaly.setTextColor(colorSel);
                mTimeDisplay.setTextColor(colorUnsel);
                mTimePikcer.setVisibility(View.INVISIBLE);
                mDatePicker.setVisibility(View.VISIBLE);
            }
        });
        mLinearLayout_time.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mDateDispaly.setTextColor(colorUnsel);
                mTimeDisplay.setTextColor(colorSel);
                mTimePikcer.setVisibility(View.VISIBLE);
                mDatePicker.setVisibility(View.INVISIBLE);
            }
        });

        mButtonCancel.setOnClickListener(this);
        mButtonSend.setOnClickListener(this);
    }

    public void setAlertDate(long date, boolean set) {
        /*if (start_alarm_set_flag == -1) {
            return;
        }*/

        NoteView c = (NoteView)mContext;
        c.setAlarmTime(start_alarm_set_flag, date);
    }

    private boolean checkSet() {
        Calendar cal = Calendar.getInstance();
        int y = cal.get(Calendar.YEAR);
        int m = cal.get(Calendar.MONTH) + 1;
        int d = cal.get(Calendar.DAY_OF_MONTH);
        int h = cal.getTime().getHours();
        int mi = cal.getTime().getMinutes();


        if (mYear > y) {
            return true;
        }
        if (mYear == y && mMonth > m) {
            return true;
        }
        if (mYear == y && mMonth == m && mDay > d) {
            return true;
        }
        if (mYear == y && mMonth == m && mDay == d) {
            if ((mHour * 60 + mMinute) > (h * 60 + mi)) {
                return true;
            }
        }
        return false;
    }

    private int getDateFormat() {
        int ret = 0;
        String fmt = Settings.System.getString(mContext.getContentResolver(),
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

    private void updateDisplayDateAndTIme(int year,int month,int day,int hour,int minute) {
        if ((mDateDispaly == null) || (mTimeDisplay == null)) {
            return;
        }

        String y = year + "";
        String m = (month <= 9) ? ("0" + month) : ("" + month);
        String d = (day <= 9) ? ("0" + day) : ("" + day);

        String h;
        String mi = (minute <= 9) ? ("0" + minute) : ("" + minute);
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

        switch (getDateFormat()) {
            case 0: //MM-dd-yyyy
                date = m + "/" + d + "/" + y;
                break;
            case 1: //dd-MM-yyyy
                date = d + "/" + m + "/" + y;
                break;
            default: //yyyy-MM-dd
                date = y + "/" + m + "/" + d;
                break;
        }

        mTimeDisplay.setText(time);
        mDateDispaly.setText(date);

        if (checkSet()) {
            mButtonSend.setEnabled(true);
            mButtonSend.setTextColor(0xff000000);
        } else {
            mButtonSend.setEnabled(false);
            //mButtonSend.setTextColor(0xffc0c0c0);
            mButtonSend.setTextColor(0xff898989);
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.id_button_send:
                Calendar mDate = Calendar.getInstance();

                mDate.set(Calendar.YEAR, mYear);
                mDate.set(Calendar.MONTH, mMonth - 1);
                mDate.set(Calendar.DAY_OF_MONTH, mDay);
                mDate.set(Calendar.HOUR_OF_DAY, mHour);
                mDate.set(Calendar.MINUTE, mMinute);
                setAlertDate(mDate.getTimeInMillis(),true);
                mDialog.dismiss();
                break;
            case R.id.id_button_cancel:
                mDialog.dismiss();
                break;
        }
    }

    //public void setDialog(AlertDialog dialog){
    public void setDialog(Dialog dialog){
        mDialog = dialog;
    }

    public void setAlarmFlag(int flag){
        start_alarm_set_flag = flag;
    }

    @Override
    public void onDateChanged(TinnoDatePicker view, int year, int monthOfYear, int dayOfMonth) {
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
        Log.e("tui", "NoteAlarmSetView  onDateChanged date = "+date);
    }

    @Override
    public void onTimeChanged(TinnoTimePicker view, int hourOfDay, int minute) {
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
        Log.e("tui", "NoteAlarmSetView  onTimeChanged time = "+time);
    }
}
