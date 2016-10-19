/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.sugar.note;

import java.text.DateFormatSymbols;
import java.util.Calendar;

//import android.widget.TinnoNumberPicker.OnChangedListener;
import com.sugar.note.TinnoNumberPicker.OnChangedListener;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
//import com.android.internal.R;
import android.widget.LinearLayout;
import android.widget.FrameLayout;


public class TinnoTimePicker extends FrameLayout {
     /**
     * A no-op callback used in the constructor to avoid null checks
     * later in the code.
     */
    private static final OnTimeChangedListener NO_OP_CHANGE_LISTENER = new OnTimeChangedListener() {
        //@Override
        public void onTimeChanged(TinnoTimePicker view, int hourOfDay, int minute) {
            // TODO Auto-generated method stub
            
        }
    };
    
    // state
    private int mCurrentHour      = 0; // 0-23
    private int mCurrentMinute    = 0; // 0-59
    private String mCurrentFormat; // 0-23

    private Boolean mIs24HourView = false;
    private boolean mIsAm;

    // ui components
    private final TinnoNumberPicker mHourPicker;
    private final TinnoNumberPicker mMinutePicker;
    private TinnoNumberPicker mAmPmPicker = null;

    private LinearLayout mAmPmLayout = null;
    //private TextView mAmPmText= null;
    
    private String[] mText = null;
    //private final String mPmText;
    
    // callbacks
    private OnTimeChangedListener mOnTimeChangedListener;

    private static String TAG = "timepicker";
    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnTimeChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         */
        void onTimeChanged(TinnoTimePicker view, int hourOfDay, int minute);
    }

    public TinnoTimePicker(Context context) {
        this(context, null);
    }
    
    public TinnoTimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TinnoTimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.tinno_time_picker,this, true);

        // hour
        mHourPicker = (TinnoNumberPicker) findViewById(R.id.hour);
        mHourPicker.Init(TinnoNumberPicker.NUMBER_TYPE.TYPE_WHEEL);
        mHourPicker.setOnChangeListener(new OnChangedListener() {
            public void onChanged(TinnoNumberPicker spinner, int oldVal, int newVal) {
                mCurrentHour = newVal;
                if (!mIs24HourView){
                    if (mCurrentHour == 12){
                        mCurrentHour = 0;
                    }
                    if (!mIsAm){
                       mCurrentHour += 12;
                    }
                }
                Log.i(TAG, "the hour change to "+Integer.toString(mCurrentHour));
                onTimeChanged();
            }
        });
        
        // digits of minute
        mMinutePicker = (TinnoNumberPicker) findViewById(R.id.minute);
        mMinutePicker.Init(TinnoNumberPicker.NUMBER_TYPE.TYPE_WHEEL);
        mMinutePicker.setRange(0, 59);
        mMinutePicker.setOnChangeListener(new OnChangedListener() {
            public void onChanged(TinnoNumberPicker spinner, int oldVal, int newVal) {
                mCurrentMinute = newVal;
                Log.i(TAG, "the hour minite to "+Integer.toString(mCurrentMinute));
                onTimeChanged();
            }
        });

        // am/pm
        mAmPmPicker = (TinnoNumberPicker)findViewById(R.id.date_format);
	  mAmPmLayout= (LinearLayout)findViewById(R.id.AmPmLayout);
	  //mAmPmText = (TextView)findViewById(R.id.AmPmText);

        // now that the hour/minute picker objects have been initialized, set
        // the hour range properly based on the 12/24 hour display mode.
        configurePickerRanges();
        
        if (!mIs24HourView){
            mIsAm = (mCurrentHour < 12);
            mAmPmPicker.Init(TinnoNumberPicker.NUMBER_TYPE.TYPE_ELASTIC);//TYPE_WHEEL);
            mAmPmPicker.setOnChangeListener(new OnChangedListener() {
            public void onChanged(TinnoNumberPicker spinner, int oldVal, int newVal) {
                    if (oldVal != newVal){
                        Log.i(TAG,"oldVal="+Integer.toString(oldVal)+", newVal="+Integer.toString(newVal));
                        onFormatChanged();
                    }
                }
            });
            /* Get the localized am/pm strings and use them in the spinner */
            DateFormatSymbols dfs = new DateFormatSymbols();
            String[] dfsAmPm = dfs.getAmPmStrings();
            mText = new String[2];
            mText[0] = dfsAmPm[Calendar.AM];
            mText[1] = dfsAmPm[Calendar.PM];
            mAmPmPicker.setRange(0, mText.length, mText);
            mAmPmPicker.setCurrent(mIsAm ? mText[0] : mText[1], false);
        }
        
        // initialize to current time
        Calendar cal = Calendar.getInstance();
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER);
        // by default we're not in 24 hour mode?
        Log.e("note", "lmj5588 TinnoTimePicker Hour == "+cal.get(Calendar.HOUR_OF_DAY));
	Log.e("note", "lmj5588 TinnoTimePicker Minute == "+cal.get(Calendar.MINUTE));
        setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(cal.get(Calendar.MINUTE));
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;
        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            mHour = hour;
            mMinute = minute;
        }
        
        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentHour, mCurrentMinute);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     * @param onTimeChangedListener the callback, should not be null.
     */
    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        mOnTimeChangedListener = onTimeChangedListener;
    }

    /**
     * @return The current hour (0-23).
     */
    public Integer getCurrentHour() {
        return mCurrentHour;
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        this.mCurrentHour = currentHour;
	Log.e("note", "lmj5588 TinnoTimePicker setCurrentHour this.mCurrentHour == "+this.mCurrentHour);
	Log.e("note", "lmj5588 TinnoTimePicker setCurrentHour mCurrentHour == "+mCurrentHour);
        updateHourDisplay();
    }

    public void setCurrentTime(Integer currentHour,Integer currentMinute) {
        this.mCurrentHour = currentHour;
        this.mCurrentMinute = currentMinute;
        updateHourDisplay();
	updateMinuteDisplay();
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    public void setIs24HourView(Boolean is24HourView) {
        //if (mIs24HourView != is24HourView) {
        mIs24HourView = is24HourView;
        configurePickerRanges();
        updateHourDisplay();
        //}
    }

    /**
     * @return true if this is in 24 hour view else false.
     */
    public boolean is24HourView() {
        return mIs24HourView;
    }
    
    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mCurrentMinute;
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinute = currentMinute;
        updateMinuteDisplay();
    }

    @Override
    public int getBaseline() {
        return mHourPicker.getBaseline(); 
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateHourDisplay() {
        int currentHour = mCurrentHour;
        if (!mIs24HourView) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour > 12) currentHour -= 12;
            else if (currentHour == 0) currentHour = 12;
        }
        mHourPicker.setCurrent(currentHour, true);
        mIsAm = mCurrentHour < 12;
        //mAmPmButton.setText(mIsAm ? mAmText : mPmText);
        //now, we write it as below:
        if (!mIs24HourView) {
            mAmPmPicker.setCurrent(mIsAm ? mText[0] : mText[1], false);
        }
	Log.e("note", "lmj5588 TinnoTimePicker updateHourDisplay aaa ");
        onTimeChanged();
    }

    private void configurePickerRanges() {
        Log.i(TAG, "configurePickerRanges, mIs24HourView="+Boolean.toString(mIs24HourView));
        if (mIs24HourView) {
            mHourPicker.setRange(0, 23);
            mAmPmPicker.setVisibility(View.GONE);
		//mAmPmText.setVisibility(View.GONE);
	      mAmPmLayout.setVisibility(View.GONE);
        } else {
            mHourPicker.setRange(1, 12);
            mAmPmPicker.setVisibility(View.VISIBLE);
		//mAmPmText.setVisibility(View.VISIBLE);
	      mAmPmLayout.setVisibility(View.VISIBLE);

        }
    }

    private void onTimeChanged() {
	Log.e("note", "lmj5588 TinnoTimePicker onTimeChanged bbb ");
	if (mOnTimeChangedListener == null)
	{
              Log.e("note", "lmj5588 TinnoTimePicker onTimeChanged bbb mOnTimeChangedListener is null");
	}
	else
	{
              Log.e("note", "lmj5588 TinnoTimePicker onTimeChanged bbb mOnTimeChangedListener is not null");
	      Log.e("note", "lmj5588 TinnoTimePicker onTimeChanged bbb mOnTimeChangedListener = "+mOnTimeChangedListener);
	}
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute());
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateMinuteDisplay() {
        Log.i(TAG,"curentMinutes is"+Integer.toString(mCurrentMinute));
        mMinutePicker.setCurrent(mCurrentMinute, true);
        mOnTimeChangedListener.onTimeChanged(this, getCurrentHour(), getCurrentMinute());
    }


    private void onFormatChanged(){
        if (mIsAm) {
            // Currently AM switching to PM
            if (mCurrentHour < 12) {
                mCurrentHour += 12;
            }                
        } else { 
            // Currently PM switching to AM
            if (mCurrentHour >= 12) {
                mCurrentHour -= 12;
            }
        }
        mIsAm = !mIsAm;
        onTimeChanged();
    }
}

