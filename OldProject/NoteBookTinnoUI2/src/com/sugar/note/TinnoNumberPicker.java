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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
//import android.widget.AbsDateView;
//import android.widget.AbsDateView.OnScrollListener;
//import com.android.internal.R;
import com.sugar.note.AbsDateView.OnScrollListener;



import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.content.res.TypedArray;
import android.widget.FrameLayout;
//import android.widget.WheelView;
//import android.widget.ElasticView;






public class TinnoNumberPicker extends FrameLayout {

    /**
     * The callback interface used to indicate the number value has been adjusted.
     */
    public interface OnChangedListener {//This is public to Datepicker or TimePicker
        /**
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        void onChanged(TinnoNumberPicker picker, int oldVal, int newVal);
    }

    /**
     * Interface used to format the number into a string for presentation
     */
    public interface Formatter {
        String toString(int value);
    }

    /**
     * Lower value of the range of numbers allowed for the TinnoNumberPicker
     */
    private int mStart;

    /**
     * Upper value of the range of numbers allowed for the TinnoNumberPicker
     */
    private int mEnd;

    /**
     * Current value of this TinnoNumberPicker
     */
    private int mCurrent;

    /**
     * Previous value of this TinnoNumberPicker.
     */
    private int mPrevious;
    private OnChangedListener mListener;
    private Formatter mFormatter;
    /**
     * This will listen to the change of wheelView when we scroll this view
     */
    private AbsDateView        mAbsView;
    private static final String TAG = "mtknumberpicker";

    private String[] mDisplayedValues = null;
    private final int INVALID_INDEX= -1;
    /***
    * Callback function which used to listen to the datechange in the wheel view
    **/
    private WheelView.OnScrollListener mSrollListener;
    private WheelView.OnItemClickListener mClickListener;
    
    private ImageView mNumberPickerView;
    private String mWheelText;

     	
    /**
     * Create a new number picker
     * @param context the application environment
     */
    public TinnoNumberPicker(Context context) {
        this(context, null);
    }

    /**
     * Create a new number picker
     * @param context the application environment
     * @param attrs a collection of attributes
     */
    public TinnoNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        //setOrientation(VERTICAL);
        LayoutInflater inflater =
                   (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.tinno_number_picker, this, true);
        
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TinnoNumberPicker, 0, 0);
        if(array != null){
        	mWheelText = array.getString(R.styleable.TinnoNumberPicker_markText);
        	Log.e("wenjie", "andy3344 TinnoNumberPicker mWheelText111  = " + mWheelText);
    	}
	else
	{
                Log.e("wenjie", "andy3344 TinnoNumberPicker array  is null ");
	}

        mSrollListener = new OnScrollListener(){

            //@Override
            public void onScroll(AbsDateView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                
            }

            //@Override
            public void onScrollStateChanged(AbsDateView view, int scrollState) {
                // TODO Auto-generated method stub
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && (null != view)){
                    int current = getSelectedPos(view.getCenterViewValue().toString());
                    Log.i(TAG,"onScrollStateChanged,The current is"+Integer.toString(current));
                    changeCurrent(current);
                }
            }
            
        };
       
        mClickListener = new WheelView.OnItemClickListener(){

            //@Override
            public void onItemClick(AbsDateView view, int postion) {
                // TODO Auto-generated method stub
                if (null != view){
                    int current = getSelectedPos(view.getCenterViewValue().toString());
                    Log.i(TAG,"onItemClick,The current is"+Integer.toString(current));
                    changeCurrent(current);
                }
            }
        };
    }

    public boolean Init(NUMBER_TYPE type){
        boolean bRet = false;
        switch (type){
            case TYPE_WHEEL:{
                ViewStub stub = (ViewStub)findViewById(R.id.wheelstub);
                mAbsView = (WheelView)stub.inflate();
                ((WheelView)mAbsView).setText(mWheelText);
		Log.e("wenjie", "andy3344 Init mWheelText222  = " + mWheelText);
                break;
            }

            case TYPE_ELASTIC:{
                 ViewStub stub = (ViewStub)findViewById(R.id.elasticstub);
                 mAbsView = (ElasticView)stub.inflate();
                 break;
            }

            default:
                break;
        }
        if (null != mAbsView){
            mAbsView.setOnItemClickListener(mClickListener);
            mAbsView.setOnScrollListener(mSrollListener);
            bRet = true;
         }
         return bRet;
    }

    /**
     * Set the callback that indicates the number has been adjusted by the user.
     * @param listener the callback, should not be null.
     */
    public void setOnChangeListener(OnChangedListener listener) {
        mListener = listener;
    }

     /**
     * Set the formatter that will be used to format the number for presentation
     * @param formatter the formatter object.  If formatter is null, String.valueOf()
     * will be used
     */
    public void setFormatter(Formatter formatter) {
        mFormatter = formatter;
    }

    /**
     * Set the range of numbers allowed for the number picker. The current
     * value will be automatically set to the start.
     *
     * @param start the start of the range (inclusive)
     * @param end the end of the range (inclusive)
     */
    public void setRange(int start, int end) {
        setRange(start, end, null/*displayedValues*/);
        Log.i(TAG, "setRange, start="+Integer.toString(start)
                   +", end="+Integer.toString(end));
    }

    /**
     * Set the range of numbers allowed for the number picker. The current
     * value will be automatically set to the start. Also provide a mapping
     * for values used to display to the user.
     *
     * @param start the start of the range (inclusive)
     * @param end the end of the range (inclusive)
     * @param displayedValues the values displayed to the user.
     */
    public void setRange(int start, int end, String[] displayedValues) {
        mDisplayedValues = displayedValues;
        mStart = start;
        mEnd = end;
        mCurrent = start;
        if (null != mAbsView){
            mAbsView.initDateTime(mStart, mEnd, mDisplayedValues);
        }else{
            Log.w(TAG, "TinnoNumberPicker::setRange, mView is null pointer");
        }
    }

    /**
     * Set the current value for the number picker.
     *
     * @param current the current value the start of the range (inclusive)
     * @throws IllegalArgumentException when current is not within the range
     *         of of the number picker
     */
     // datepicker/Timepicker ---> WheelView
    public void setCurrent(int current, boolean centerflag) {
        if (current < mStart || current > mEnd) {
            throw new IllegalArgumentException(
                    "current should be >= start and <= end");
        }
        mCurrent = current;
        if (null != mAbsView){
            //here because we set it as the index of array, 
            //so, we should set it as current - 1;
        /*
            if(mAbsView instanceof ElasticView)
            {
                  ((ElasticView)mAbsView).setCenterItem(current-mStart, centerflag);
            }
	    else{
		  ((WheelView)mAbsView).setCenterItem(current-mStart, centerflag);
	    }
       */
            mAbsView.setCenterItem(current-mStart, centerflag);
        }else{
            Log.w(TAG, "TinnoNumberPicker::setCurrent, mView is null pointer");
        }
    }

    public void setCurrent(String current, boolean centerflag){
        if (null == mDisplayedValues){
            throw new IllegalArgumentException(
                    "current should be >= start and <= end");
        }else{
            int index = findValueIndex(current);
            setCurrent(index, centerflag);
        }
    }

    private int findValueIndex(String value){
        for (int i=0; i<= mDisplayedValues.length;i++){
            if (value.equals(mDisplayedValues[i]))
                return i;
        }
        return INVALID_INDEX;
    }

    public boolean getScrolling(){
     /*
	if(mAbsView instanceof ElasticView)
	{
              return ((ElasticView)mAbsView).getViewScrolling();
	}
	else
	{
              return ((WheelView)mAbsView).getViewScrolling();
	}
    */
         return mAbsView.getViewScrolling();
    }

    /**
     * Sets the current value of this NumberPicker, and sets mPrevious to the previous
     * value.  If current is greater than mEnd less than mStart, the value of mCurrent
     * is wrapped around.
     *
     * Subclasses can override this to change the wrapping behavior
     *
     * @param current the new value of the NumberPicker
     */
     //WheelView ---> datepicker/Timepicker
    protected void changeCurrent(int current) {
        // Wrap around the values if we go past the start or end
        if (current > mEnd) {
            current = mStart;
        } else if (current < mStart) {
            current = mEnd;
        }
        Log.i(TAG, "ChangeCurrent, mStart="+Integer.toString(mStart)
                   +", mEnd="+Integer.toString(mEnd)
                   +", mPrevious="+Integer.toString(mPrevious)
                   +", mCurrent="+Integer.toString(mCurrent)
                   +", current="+Integer.toString(current));
        mPrevious = mCurrent;
        mCurrent = current;
        notifyChange();
        //updateView();
    }

    private int getSelectedPos(String str){
        if (mDisplayedValues == null) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                /* Ignore as if it's not a number we don't care */
            }
        } else {
            for (int i = 0; i < mDisplayedValues.length; i++) {
                /* Don't force the user to type in jan when ja will do */
                str = str.toLowerCase();
                if (mDisplayedValues[i].toLowerCase().startsWith(str)) {
                    return mStart + i;
                }
            }

            /* The user might have typed in a number into the month field i.e.
             * 10 instead of OCT so support that too.
             */
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {

                /* Ignore as if it's not a number we don't care */
            }
        }
        return mStart;
    }
    /**
     * Notifies the listener, if registered, of a change of the value of this
     * NumberPicker.
     */
    private void notifyChange() {
        if (mListener != null) {
            mListener.onChanged(this, mPrevious, mCurrent);
        }
    }



    /**
     * Returns the current value of the NumberPicker
     * @return the current value.
     */
    public int getCurrent() {
        return mCurrent;
    }

    /**
     * Returns the upper value of the range of the NumberPicker
     * @return the uppper number of the range.
     */
    public int getEndRange() {
        return mEnd;
    }

    /**
     * Returns the lower value of the range of the NumberPicker
     * @return the lower number of the range.
     */
    public int getBeginRange() {
        return mStart;
    }

    public enum NUMBER_TYPE{ 
        TYPE_WHEEL,
        TYPE_ELASTIC,
        NONE
    }
}

