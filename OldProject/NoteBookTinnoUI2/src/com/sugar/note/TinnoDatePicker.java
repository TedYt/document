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
import java.text.SimpleDateFormat;
import java.util.Calendar;

//import android.widget.TinnoNumberPicker.OnChangedListener;
import com.sugar.note.TinnoNumberPicker.OnChangedListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
//import com.android.internal.R;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Arrays;
import java.text.ParseException;

import android.text.TextUtils;
import android.widget.FrameLayout;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;

public class TinnoDatePicker extends FrameLayout {
	
    private static final int DEFAULT_START_YEAR = 1970;
    private static final int DEFAULT_END_YEAR   = 2036;
	
    private static final String DATE_FORMAT = "MM/dd/yyyy";
	
    private static final boolean DEFAULT_CALENDAR_VIEW_SHOWN = false;
    private static final boolean DEFAULT_SPINNERS_SHOWN = true;
	
    private final java.text.DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);
    
    // This ignores Undecimber, but we only support real Gregorian calendars.
    private static final int NUMBER_OF_MONTHS = 12;

    /* UI Components */
    private final TinnoNumberPicker mDayPicker;
    private final TinnoNumberPicker mMonthPicker;
    private final TinnoNumberPicker mYearPicker;
    private final CalendarView mCalendarView;
	
    private final LinearLayout mDayLayout;
    private final LinearLayout mMonthLayout;
    private final LinearLayout mYearLayout;

    private final TextView mDayText;
    private final TextView mMonthText;
    private final TextView mYearText;

    private final ImageView mImgGap1;
    private final ImageView mImgGap2;
    /**
     * How we notify users the date has changed.
     */
    private OnDateChangedListener mOnDateChangedListener;
    
    private int mDay;
    private int mMonth;
    private int mYear;

    private Object mMonthUpdateLock = new Object();
    private volatile Locale mMonthLocale;
    private String[] mShortMonths;
	
    private Calendar mTempDate;

    private Calendar mMinDate;

    private Calendar mMaxDate;

    private Calendar mCurrentDate;
	
    private Locale mCurrentLocale;

    private int mNumberOfMonths;	
	
    private static String TAG = "tinnodatepicker";
    /**
     * The callback used to indicate the user changes the date.
     */
    public interface OnDateChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param year The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *  with {@link java.util.Calendar}.
         * @param dayOfMonth The day of the month that was set.
         */
        void onDateChanged(TinnoDatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    public TinnoDatePicker(Context context) {
        this(context, null);
    }
    
    public TinnoDatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TinnoDatePicker(Context context, AttributeSet attrs, int defSytle) {
        super(context, attrs, defSytle);

        setCurrentLocale(Locale.getDefault());

        TypedArray attributesArray = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.DatePicker,
                defSytle, 0);
        boolean spinnersShown = attributesArray.getBoolean(com.android.internal.R.styleable.DatePicker_spinnersShown,
                DEFAULT_SPINNERS_SHOWN);
        boolean calendarViewShown = false;//attributesArray.getBoolean(
        //R.styleable.DatePicker_calendarViewShown, DEFAULT_CALENDAR_VIEW_SHOWN);
        int startYear = attributesArray.getInt(com.android.internal.R.styleable.DatePicker_startYear,
                DEFAULT_START_YEAR);
        int endYear = attributesArray.getInt(com.android.internal.R.styleable.DatePicker_endYear, DEFAULT_END_YEAR);
        String minDate = attributesArray.getString(com.android.internal.R.styleable.DatePicker_minDate);
        String maxDate = attributesArray.getString(com.android.internal.R.styleable.DatePicker_maxDate);
        // int layoutResourceId = attributesArray.getResourceId(com.android.internal.R.styleable.DatePicker_internalLayout,
        //        R.layout.tinno_date_picker);
        attributesArray.recycle();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.tinno_date_picker, this, true);
        //inflater.inflate(layoutResourceId, this, true);

        OnChangedListener OnChangedListener = new OnChangedListener() {
            public void onChanged(TinnoNumberPicker picker, int oldVal, int newVal) {

                mTempDate.setTimeInMillis(mCurrentDate.getTimeInMillis());
                // take care of wrapping of days and months to update greater fields
                if (picker == mDayPicker) {
                    int maxDayOfMonth = mTempDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (oldVal == maxDayOfMonth && newVal == 1) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, 1);
                    } else if (oldVal == 1 && newVal == maxDayOfMonth) {
                        mTempDate.add(Calendar.DAY_OF_MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.DAY_OF_MONTH, newVal - oldVal);
                    }
                } else if (picker == mMonthPicker) {
                    if (oldVal == 11 && newVal == 0) {
                        mTempDate.add(Calendar.MONTH, 1);
                    } else if (oldVal == 0 && newVal == 11) {
                        mTempDate.add(Calendar.MONTH, -1);
                    } else {
                        mTempDate.add(Calendar.MONTH, newVal - oldVal);
                    }
                } else if (picker == mYearPicker) {
                    mTempDate.set(Calendar.YEAR, newVal);
                } else {
                    throw new IllegalArgumentException();
                }
                // now set the date to the adjusted one
                setDate(mTempDate.get(Calendar.YEAR), mTempDate.get(Calendar.MONTH),
                        mTempDate.get(Calendar.DAY_OF_MONTH));
                updateSpinners();
                updateCalendarView();
                notifyDateChanged();
            }
        };

        //DayPicker
        mDayPicker = (TinnoNumberPicker) findViewById(R.id.day);
        mDayLayout = (LinearLayout) findViewById(R.id.DayLayout);
        mDayText = (TextView) findViewById(R.id.DayText);

        mDayPicker.Init(TinnoNumberPicker.NUMBER_TYPE.TYPE_WHEEL);
        mDayPicker.setOnChangeListener(OnChangedListener);

        //MonthPicker
        mMonthPicker = (TinnoNumberPicker) findViewById(R.id.month);
        mMonthLayout = (LinearLayout) findViewById(R.id.MonthLayout);
        mMonthText = (TextView) findViewById(R.id.MonthText);

        mMonthPicker.Init(TinnoNumberPicker.NUMBER_TYPE.TYPE_WHEEL);


        mMonthPicker.setRange(0, mNumberOfMonths - 1, mShortMonths);

        mMonthPicker.setOnChangeListener(OnChangedListener);

        //YearPicker
        mYearPicker = (TinnoNumberPicker) findViewById(R.id.year);
        mYearLayout = (LinearLayout) findViewById(R.id.YearLayout);
        mYearText = (TextView) findViewById(R.id.YearText);

        mYearPicker.Init(TinnoNumberPicker.NUMBER_TYPE.TYPE_WHEEL);
        mYearPicker.setOnChangeListener(OnChangedListener);

        mImgGap1 = (ImageView) findViewById(R.id.img_gap1);
        mImgGap2 = (ImageView) findViewById(R.id.img_gap2);
		

        // calendar view day-picker
        mCalendarView = (CalendarView) findViewById(R.id.calendar_view);
        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            public void onSelectedDayChange(CalendarView view, int year, int month, int monthDay) {
                setDate(year, month, monthDay);
                updateSpinners();
                notifyDateChanged();
            }
        });

        setCalendarViewShown(false);

        // set the min date giving priority of the minDate over startYear
        mTempDate.clear();
        if (!TextUtils.isEmpty(minDate)) {
            if (!parseDate(minDate, mTempDate)) {
                mTempDate.set(startYear, 0, 1);
            }
        } else {
            mTempDate.set(startYear, 0, 1);
        }
        setMinDate(mTempDate.getTimeInMillis());

        // set the max date giving priority of the maxDate over endYear
        mTempDate.clear();
        if (!TextUtils.isEmpty(maxDate)) {
            if (!parseDate(maxDate, mTempDate)) {
                mTempDate.set(endYear, 11, 31);
            }
        } else {
            mTempDate.set(endYear, 11, 31);
        }
        setMaxDate(mTempDate.getTimeInMillis());

        mCurrentDate.setTimeInMillis(System.currentTimeMillis());
        init(mCurrentDate.get(Calendar.YEAR), mCurrentDate.get(Calendar.MONTH), mCurrentDate
                .get(Calendar.DAY_OF_MONTH), null);

        setDate(mTempDate.get(Calendar.YEAR), mTempDate.get(Calendar.MONTH),
                mTempDate.get(Calendar.DAY_OF_MONTH));
        // re-order the number pickers to match the current date format
        reorderPickers(mShortMonths);

    }

    private void addGapImg(LinearLayout pView, int addIndex) {
        if (addIndex == 1)
        {
            pView.addView(mImgGap1);
        }
        else if (addIndex == 2)
        {
            pView.addView(mImgGap2);
        }
        else
        {
            Log.e("tui", "addGapImg the value of addIndex must be 1 or 2 addIndex = "+addIndex);
        }
    }

    private void reorderPickers(String[] months) {
        /* Remove the 3 pickers from their parent and then add them back in the
         * required order.
         */
        LinearLayout parent = (LinearLayout) findViewById(R.id.parent);
        parent.removeAllViews();
	mDayLayout.removeAllViews();
	mMonthLayout.removeAllViews();
	mYearLayout.removeAllViews();

        boolean didDay = false, didMonth = false, didYear = false;
        int add_index = 0;

        char[] order = DateFormat.getDateFormatOrder(getContext());
        final int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            add_index++;
            
            switch (order[i]) {
                case DateFormat.DATE:
                    mDayLayout.addView(mDayPicker);
		    mDayLayout.addView(mDayText);
		    parent.addView(mDayLayout);
                    Log.e("tui", "reorderPickers 111 ");
                    didDay = true;
                    break;
                case DateFormat.MONTH:
                    mMonthLayout.addView(mMonthPicker);
		    mMonthLayout.addView(mMonthText);              
                    parent.addView(mMonthLayout);
                    //parent.addView(mImgGap1);
                    Log.e("tui", "reorderPickers 222 ");
                    didMonth = true;
                    break;
                case DateFormat.YEAR:
                    mYearLayout.addView (mYearPicker);
		    mYearLayout.addView(mYearText);                      
                    parent.addView(mYearLayout);
                    //parent.addView(mImgGap1);
                    Log.e("tui", "reorderPickers 333 ");
                    didYear = true;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            addGapImg(parent, add_index);
        }

        Log.e("tui", "reorderPickers  didMonth = "+didMonth + "  didDay = "+didDay +" didYear = "+didYear);
        // Shouldn't happen, but just in case.
        if (!didMonth) {
	    mMonthLayout.addView(mMonthPicker);
	    mMonthLayout.addView(mMonthText); 
            parent.addView(mMonthLayout);
            add_index++;
            addGapImg(parent, add_index);
        }
        if (!didDay) {
	    mDayLayout.addView(mDayPicker);
	    mDayLayout.addView(mDayText); 
            parent.addView(mDayLayout);
            add_index++;
            addGapImg(parent, add_index);
        }
        if (!didYear) {
	    mYearLayout.addView(mYearPicker);
	    mYearLayout.addView(mYearText); 
            parent.addView(mYearLayout);
            add_index++;
            addGapImg(parent, add_index);
        }
    }

    public void updateDate(int year, int monthOfYear, int dayOfMonth) {

       if (!isNewDate(year, monthOfYear, dayOfMonth)) {
            return;
        }
        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners();
        updateCalendarView();
        notifyDateChanged();
    }

    private String[] getShortMonths() {
        final Locale currentLocale = Locale.getDefault();
        if (currentLocale.equals(mMonthLocale) && mShortMonths != null) {
            return mShortMonths;
        } else {
            synchronized (mMonthUpdateLock) {
                if (!currentLocale.equals(mMonthLocale)) {
                    mShortMonths = new String[NUMBER_OF_MONTHS];
                    for (int i = 0; i < NUMBER_OF_MONTHS; i++) {
                        mShortMonths[i] = DateUtils.getMonthString(Calendar.JANUARY + i,
                                DateUtils.LENGTH_MEDIUM);
                    }
                    mMonthLocale = currentLocale;
                }
            }
            return mShortMonths;
        }
    }
	
    public CalendarView getCalendarView () {
        return mCalendarView;
    }
    /**
     * Sets the current locale.
     *
     * @param locale The current locale.
     */
    private void setCurrentLocale(Locale locale) {
        if (locale.equals(mCurrentLocale)) {
            return;
        }

        mCurrentLocale = locale;

        mTempDate = getCalendarForLocale(mTempDate, locale);
        mMinDate = getCalendarForLocale(mMinDate, locale);
        mMaxDate = getCalendarForLocale(mMaxDate, locale);
        mCurrentDate = getCalendarForLocale(mCurrentDate, locale);

        mNumberOfMonths = mTempDate.getActualMaximum(Calendar.MONTH) + 1;
        mShortMonths = new String[mNumberOfMonths];
        for (int i = 0; i < mNumberOfMonths; i++) {
/*            mShortMonths[i] = DateUtils.getMonthString(Calendar.JANUARY + i,
                    DateUtils.LENGTH_MEDIUM);*/
            mShortMonths[i] = "" + (Calendar.JANUARY + i + 1);//<TUI_SETTINGS><日期控件月份修改成数字显示>XUSONG
        }
    }
	
    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }	
/*	
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }	
*/
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(event);

        final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR;
        String selectedDateUtterance = DateUtils.formatDateTime(mContext,
                mCurrentDate.getTimeInMillis(), flags);
        event.getText().add(selectedDateUtterance);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }
	
    public boolean getCalendarViewShown() {
        return mCalendarView.isShown();
    }
	
    public void setCalendarViewShown(boolean shown) {
        mCalendarView.setVisibility(shown ? VISIBLE : GONE);
    }	
	
    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, getYear(), getMonth(), getDayOfMonth());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setDate(ss.mYear, ss.mMonth, ss.mDay);
        updateSpinners();
        updateCalendarView();
    }

	
    /**
     * Initialize the state.
     * @param year The initial year.
     * @param monthOfYear The initial month.
     * @param dayOfMonth The initial day of the month.
     * @param onDateChangedListener How user is notified date is changed by user, can be null.
     */
    public void init(int year, int monthOfYear, int dayOfMonth,
            OnDateChangedListener onDateChangedListener) {

        setDate(year, monthOfYear, dayOfMonth);
        updateSpinners();
        updateCalendarView();
        mOnDateChangedListener = onDateChangedListener;
    }

    private void updateSpinners() {
        if (mCurrentDate.equals(mMinDate)) {
            mDayPicker.setRange(mCurrentDate.get(Calendar.DAY_OF_MONTH),
                    mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            mMonthPicker.setRange(mCurrentDate.get(Calendar.MONTH),
                    mCurrentDate.getActualMaximum(Calendar.MONTH));
        } else if (mCurrentDate.equals(mMaxDate)) {
            mDayPicker.setRange(mCurrentDate.getActualMinimum(Calendar.DAY_OF_MONTH),
                    mCurrentDate.get(Calendar.DAY_OF_MONTH));
            mMonthPicker.setRange(mCurrentDate.getActualMinimum(Calendar.MONTH),
                    mCurrentDate.get(Calendar.MONTH));
        } else {
            mDayPicker.setRange(1, mCurrentDate.getActualMaximum(Calendar.DAY_OF_MONTH));
            mMonthPicker.setRange(0, 11);
        }


        String[] displayedValues = Arrays.copyOfRange(mShortMonths,
                mMonthPicker.getBeginRange(), mMonthPicker.getEndRange() + 1);
        mMonthPicker.setRange(mMonthPicker.getBeginRange(), mMonthPicker.getEndRange(), displayedValues);


        mYearPicker.setRange(mMinDate.get(Calendar.YEAR), mMaxDate.get(Calendar.YEAR));

        mYearPicker.setCurrent(mCurrentDate.get(Calendar.YEAR), false);
        mMonthPicker.setCurrent(mCurrentDate.get(Calendar.MONTH), false);
        mDayPicker.setCurrent(mCurrentDate.get(Calendar.DAY_OF_MONTH), false);

    }

    private void adjustMaxDay(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, mYear);
        cal.set(Calendar.MONTH, mMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (mDay > max) {
            mDay = max;
        }
        Log.i(TAG, "adjustMaxDay, mYear ="+Integer.toString(mYear)
                    +"mMonth ="+Integer.toString(mMonth)
                    +"day is"+Integer.toString(max));
    }
	

    private void updateDaySpinner() {
        Calendar cal = Calendar.getInstance();
        cal.set(mYear, mMonth, mDay);
        int max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        mDayPicker.setRange(1, max);
        mDayPicker.setCurrent(mDay, false);
    }

    public int getYear() {
        return mCurrentDate.get(Calendar.YEAR);
    }

    /**
     * @return The selected month.
     */
    public int getMonth() {
        return mCurrentDate.get(Calendar.MONTH);
    }

    /**
     * @return The selected day of month.
     */
    public int getDayOfMonth() {
        return mCurrentDate.get(Calendar.DAY_OF_MONTH);
    }
	
    public void setYearRange(int startYear,int endYear){
        mYearPicker.setRange(startYear, endYear); 
        if((mYear>=startYear)&&(mYear<=endYear))
        {
            mYearPicker.setCurrent(mYear, true);
        }
        else
        {
            mYear = startYear;
        }
    }
	
    private boolean isNewDate(int year, int month, int dayOfMonth) {
        return (mCurrentDate.get(Calendar.YEAR) != year
                || mCurrentDate.get(Calendar.MONTH) != dayOfMonth
                || mCurrentDate.get(Calendar.DAY_OF_MONTH) != month);
    }

    private void setDate(int year, int month, int dayOfMonth) {
        mCurrentDate.set(year, month, dayOfMonth);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
        } else if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
        }
    }	

    public long getMinDate() {
        return mCalendarView.getMinDate();
    }
	
    /**
     * Updates the calendar view with the current date.
     */
    private void updateCalendarView() {
         mCalendarView.setDate(mCurrentDate.getTimeInMillis(), false, false);
    }

    public void setMinDate(long minDate) {
        mTempDate.setTimeInMillis(minDate);
        if (mTempDate.get(Calendar.YEAR) == mMinDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMinDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMinDate.setTimeInMillis(minDate);
        mCalendarView.setMinDate(minDate);
        if (mCurrentDate.before(mMinDate)) {
            mCurrentDate.setTimeInMillis(mMinDate.getTimeInMillis());
            updateCalendarView();
        }
        updateSpinners();
    }
    /**
    
     * Parses the given <code>date</code> and in case of success sets the result
     * to the <code>outDate</code>.
     *
     * @return True if the date was parsed.
     */
    private boolean parseDate(String date, Calendar outDate) {
        try {
            outDate.setTime(mDateFormat.parse(date));
            return true;
        } catch (ParseException e) {
            Log.w(TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
            return false;
        }
    }
	
    public long getMaxDate() {
        return mCalendarView.getMaxDate();
    }

    public void setMaxDate(long maxDate) {
        mTempDate.setTimeInMillis(maxDate);
        if (mTempDate.get(Calendar.YEAR) == mMaxDate.get(Calendar.YEAR)
                && mTempDate.get(Calendar.DAY_OF_YEAR) != mMaxDate.get(Calendar.DAY_OF_YEAR)) {
            return;
        }
        mMaxDate.setTimeInMillis(maxDate);
        mCalendarView.setMaxDate(maxDate);
        if (mCurrentDate.after(mMaxDate)) {
            mCurrentDate.setTimeInMillis(mMaxDate.getTimeInMillis());
            updateCalendarView();
        }
        updateSpinners();
    }
    
    public int getDisplayYear()
    {
        return mYearPicker.getCurrent();
    }
    
    public int getDisplayMonth()
    {
        return mMonthPicker.getCurrent();
    }
    
    public int getDisplayDayofMonth()
    {
        return mDayPicker.getCurrent();
    }

    private void notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (mOnDateChangedListener != null) {
            mOnDateChangedListener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
        }  	
    }  
	
    /**
     * Class for managing state storing/restoring.
     */
    private static class SavedState extends BaseSavedState {

        private final int mYear;

        private final int mMonth;

        private final int mDay;

        private SavedState(Parcelable superState, int year, int month, int day) {
            super(superState);
            mYear = year;
            mMonth = month;
            mDay = day;
        }

        /**
         * Constructor called from {@link #CREATOR}
         */
        private SavedState(Parcel in) {
            super(in);
            mYear = in.readInt();
            mMonth = in.readInt();
            mDay = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mYear);
            dest.writeInt(mMonth);
            dest.writeInt(mDay);
        }

        @SuppressWarnings("all")
        // suppress unused and hiding
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
	
}

