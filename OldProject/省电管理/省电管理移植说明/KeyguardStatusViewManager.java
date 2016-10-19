/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.internal.policy.impl;

import com.android.internal.R;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCard.State;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.TransportControlView;
import com.android.internal.policy.impl.KeyguardUpdateMonitor.SimStateCallback;
import com.android.internal.policy.impl.KeyguardUpdateMonitor.phoneStateCallback;
import com.android.internal.policy.impl.KeyguardUpdateMonitor.deviceInfoCallback;
import com.android.internal.policy.impl.KeyguardUpdateMonitor.InfoCallback;

import java.util.ArrayList;
import java.util.Date;

import libcore.util.MutableInt;

import android.content.ContentResolver;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.internal.telephony.Phone;
import com.mediatek.featureoption.FeatureOption;
import android.provider.Telephony.SIMInfo;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.view.inputmethod.InputMethodManager;
import android.app.admin.DevicePolicyManager;
import android.net.ConnectivityManager;
import android.view.Gravity;
import com.mediatek.xlog.Xlog;

/***
 * Manages a number of views inside of LockScreen layouts. See below for a list of widgets
 *
 */
class KeyguardStatusViewManager implements OnClickListener {
    private static final boolean DEBUG = true;
    private static final String TAG = "KeyguardStatusView";

    public static final int LOCK_ICON = 0; // R.drawable.ic_lock_idle_lock;
    public static final int ALARM_ICON = R.drawable.ic_lock_idle_alarm;
    public static final int CHARGING_ICON = 0; //R.drawable.ic_lock_idle_charging;
    public static final int BATTERY_LOW_ICON = 0; //R.drawable.ic_lock_idle_low_battery;
    private static final long INSTRUCTION_RESET_DELAY = 2000; // time until instruction text resets

    private static final int INSTRUCTION_TEXT = 10;
    private static final int CARRIER_TEXT = 11;
    private static final int CARRIER_HELP_TEXT = 12;
    private static final int HELP_MESSAGE_TEXT = 13;
    private static final int OWNER_INFO = 14;
    private static final int BATTERY_INFO = 15;

    private StatusMode mStatus;
    private StatusMode mGeminiStatus;
    private String mDateFormatString;
    private TransientTextManager mTransientTextManager;
    private TransientTextManager mTransientTextManagerGemini;

    // Views that this class controls.
    // NOTE: These may be null in some LockScreen screens and should protect from NPE
    private TextView mCarrierView;
    private TextView mCarrierGeminiView;
    private Drawable mCarrierIcon;
    private Drawable mCarrierGeminiIcon;

    private TextView mDateView;
    private TextView mStatus1View;
    private TextView mOwnerInfoView;
    private TextView mAlarmStatusView;

    //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    private TextView mCalibrationData;
    //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    
    private TransportControlView mTransportView;
    private static boolean phoneAppAlive = false;

    // Top-level container view for above views
    private View mContainer;

    // are we showing battery information?
    private boolean mShowingBatteryInfo = false;

    // last known plugged in state
    private boolean mPluggedIn = false;

    // last known battery level
    private int mBatteryLevel = 100;
    
    private String mChargeTime = ""; //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>

    // last known SIM state
    protected State mSimState;
    protected State mSimGeminiState;
    private LockPatternUtils mLockPatternUtils;
    private KeyguardUpdateMonitor mUpdateMonitor;
    private Button mEmergencyCallButton;
    private boolean mUnlockDisabledDueToSimState;

    // Shadowed text values
    private CharSequence mCarrierText;
    private CharSequence mCarrierGeminiText;
    private CharSequence mCarrierHelpText;
    private String mHelpMessageText;
    private String mInstructionText;
    private CharSequence mOwnerInfoText;
    private boolean mShowingStatus;
    private KeyguardScreenCallback mCallback;
    //private final boolean mShowEmergencyButtonByDefault;
    private boolean mEmergencyButtonEnabledBecauseSimLocked;
    private TextView mDMPrompt;
    //here we don't want the plmn get the
    private final boolean mEmergencyCallButtonEnabledInScreen;
    private CharSequence mPlmn;
    private CharSequence mSpn;
    protected int mPhoneState;

    //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    private boolean mDownloadCalibrationData;
    //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    /**
     * bitmaps of sim card infomation.
     */
    private static final int[] SIM_ICON_IMGS = {
        // color order:
        // Orange blue pink green Yellow ultramarine red purple
        // sim_normal
        com.mediatek.internal.R.drawable.sim_orange_normal,
        com.mediatek.internal.R.drawable.sim_blue_normal,
        com.mediatek.internal.R.drawable.sim_pink_normal,
        com.mediatek.internal.R.drawable.sim_green_normal,
        com.mediatek.internal.R.drawable.sim_yellow_normal,
        com.mediatek.internal.R.drawable.sim_brown_normal,
        com.mediatek.internal.R.drawable.sim_red_normal,
        com.mediatek.internal.R.drawable.sim_purple_normal,
        // sim_no_sign
        com.mediatek.internal.R.drawable.sim_orange_no_sign,
        com.mediatek.internal.R.drawable.sim_blue_no_sign,
        com.mediatek.internal.R.drawable.sim_pink_no_sign,
        com.mediatek.internal.R.drawable.sim_green_no_sign,
        com.mediatek.internal.R.drawable.sim_yellow_no_sign,
        com.mediatek.internal.R.drawable.sim_brown_no_sign,
        com.mediatek.internal.R.drawable.sim_red_no_sign,
        com.mediatek.internal.R.drawable.sim_purple_no_sign,
        // sim_no
        com.mediatek.internal.R.drawable.sim_orange_no,
        com.mediatek.internal.R.drawable.sim_blue_no,
        com.mediatek.internal.R.drawable.sim_pink_no,
        com.mediatek.internal.R.drawable.sim_green_no,
        com.mediatek.internal.R.drawable.sim_yellow_no,
        com.mediatek.internal.R.drawable.sim_brown_no,
        com.mediatek.internal.R.drawable.sim_red_no,
        com.mediatek.internal.R.drawable.sim_purple_no,
        // sim_lock
        com.mediatek.internal.R.drawable.sim_orange_lock,
        com.mediatek.internal.R.drawable.sim_blue_lock,
        com.mediatek.internal.R.drawable.sim_pink_lock,
        com.mediatek.internal.R.drawable.sim_green_lock,
        com.mediatek.internal.R.drawable.sim_yellow_lock,
        com.mediatek.internal.R.drawable.sim_brown_lock,
        com.mediatek.internal.R.drawable.sim_red_lock,
        com.mediatek.internal.R.drawable.sim_purple_lock
    };

    private static final int SIM_COLOR_COUNT = 8;
    private static final int SIM_NORMAL_INDEX = 0;
    private static final int SIM_NO_SIGN_INDEX = 1;
    private static final int SIM_NO_INDEX = 2;
    private static final int SIM_LOCK_INDEX = 3;


    private class TransientTextManager {
        private TextView mTextView;
        private class Data {
            final int icon;
            final CharSequence text;
            Data(CharSequence t, int i) {
                text = t;
                icon = i;
            }
        };
        private ArrayList<Data> mMessages = new ArrayList<Data>(5);

        TransientTextManager(TextView textView) {
            mTextView = textView;
        }

        /* Show given message with icon for up to duration ms. Newer messages override older ones.
         * The most recent message with the longest duration is shown as messages expire until
         * nothing is left, in which case the text/icon is defined by a call to
         * getAltTextMessage() */
        void post(final CharSequence message, final int icon, long duration) {
            if (mTextView == null) {
                return;
            }
            mTextView.setText(message);
            mTextView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
            final Data data = new Data(message, icon);
            mContainer.postDelayed(new Runnable() {
                public void run() {
                    mMessages.remove(data);
                    int last = mMessages.size() - 1;
                    final CharSequence lastText;
                    final int lastIcon;
                    if (last > 0) {
                        final Data oldData = mMessages.get(last);
                        lastText = oldData.text;
                        lastIcon = oldData.icon;
                    } else {
                        final MutableInt tmpIcon = new MutableInt(0);
                        lastText = getAltTextMessage(tmpIcon);
                        lastIcon = tmpIcon.value;
                    }
                    mTextView.setText(lastText);
                    mTextView.setCompoundDrawablesWithIntrinsicBounds(lastIcon, 0, 0, 0);
                }
            }, duration);
        }
    };

    public KeyguardStatusViewManager(View view, KeyguardUpdateMonitor updateMonitor,
                LockPatternUtils lockPatternUtils, KeyguardScreenCallback callback,
                boolean emergencyButtonEnabledInScreen) {
        if (DEBUG) Xlog.v(TAG, "KeyguardStatusViewManager()");
        mContainer = view;
        mDateFormatString = getContext().getString(R.string.abbrev_wday_month_day_no_year);
        mLockPatternUtils = lockPatternUtils;
        mUpdateMonitor = updateMonitor;
        mCallback = callback;
				
        mCarrierView = (TextView) findViewById(R.id.carrier);
        mDateView = (TextView) findViewById(R.id.date);
        mStatus1View = (TextView) findViewById(R.id.status1);
        mAlarmStatusView = (TextView) findViewById(R.id.alarm_status);
        mOwnerInfoView = (TextView) findViewById(R.id.propertyOf);

        //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
        mCalibrationData = (TextView) findViewById(R.id.calibrationData);
        //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not

        mTransportView = (TransportControlView) findViewById(R.id.transport);
        mEmergencyCallButton = (Button) findViewById(R.id.emergencyCallButton);
        mEmergencyCallButtonEnabledInScreen = emergencyButtonEnabledInScreen;

        mDMPrompt = (TextView) findViewById(R.id.dm_lock_prompt);
        if (null != mDMPrompt && mUpdateMonitor.DM_IsLocked()){
            mDMPrompt.setVisibility(View.VISIBLE);
            mDMPrompt.setText(com.mediatek.internal.R.string.dm_prompt);
        }

        if (FeatureOption.MTK_GEMINI_SUPPORT){
            mCarrierGeminiView = (TextView) findViewById(R.id.carrierGemini);
            mTransientTextManagerGemini = new TransientTextManager(mCarrierGeminiView);
        }
        // Hide transport control view until we know we need to show it.
        if (mTransportView != null) {
            mTransportView.setVisibility(View.GONE);
        }

        /*** try to get whether or not the phoneApp is ready**/
        if (!phoneAppAlive){
            phoneAppAlive = mUpdateMonitor.isPhoneAppReady();
        }

        if (mEmergencyCallButton != null) {
            mEmergencyCallButton.setText(R.string.lockscreen_emergency_call);
            mEmergencyCallButton.setOnClickListener(this);
            mEmergencyCallButton.setFocusable(false); // touch only!
        }

        mTransientTextManager = new TransientTextManager(mCarrierView);


        mUpdateMonitor.registerInfoCallback(mInfoCallback);
        mUpdateMonitor.registerSimStateCallback(mSimStateCallback);
        mUpdateMonitor.registerPhoneStateCallback(mPhoneCallback);
        mUpdateMonitor.registerDeviceInfoCallback(mDeviceInfoCallback);

        resetStatusInfo();
        refreshDate();
        updateOwnerInfo();

        // Required to get Marquee to work.
        if (FeatureOption.MTK_GEMINI_SUPPORT){
            final View scrollableViews[] = {mCarrierView, mCarrierGeminiView, mDateView, mStatus1View, mOwnerInfoView,
                mAlarmStatusView };
            for (View v : scrollableViews) {
            if (v != null) {
                v.setSelected(true);
            }
        }
        }else{
           final View scrollableViews[] = {mCarrierView, mDateView, mStatus1View, mOwnerInfoView,
                mAlarmStatusView };
           for (View v : scrollableViews) {
              if (v != null) {
                 v.setSelected(true);
              }
           }
        }

    }

    private boolean inWidgetMode() {
        return mTransportView != null && mTransportView.getVisibility() == View.VISIBLE;
    }

    private boolean isAccountMode(){
        if (mContainer instanceof AccountUnlockScreen){
            return true;
        }
        return false;
    }

    void setInstructionText(String string) {
        mInstructionText = string;
        update(INSTRUCTION_TEXT, string);
    }

    void setCarrierText(CharSequence string, int simId) {
        if (Phone.GEMINI_SIM_2 == simId){
            mCarrierGeminiText = string;
        }else {
            mCarrierText = string;
        }
        update(CARRIER_TEXT, string);
    }

    void setOwnerInfo(CharSequence string) {
        mOwnerInfoText = string;
        update(OWNER_INFO, string);
    }

    /**
     * Sets the carrier help text message, if view is present. Carrier help text messages are
     * typically for help dealing with SIMS and connectivity.
     *
     * @param resId resource id of the message
     */
    public void setCarrierHelpText(int resId) {
        mCarrierHelpText = getText(resId);
        update(CARRIER_HELP_TEXT, mCarrierHelpText);
    }

    private CharSequence getText(int resId) {
        return resId == 0 ? null : getContext().getText(resId);
    }

    /**
     * Unlock help message.  This is typically for help with unlock widgets, e.g. "wrong password"
     * or "try again."
     *
     * @param textResId
     * @param lockIcon
     */
    public void setHelpMessage(int textResId, int lockIcon) {
        final CharSequence tmp = getText(textResId);
        mHelpMessageText = tmp == null ? null : tmp.toString();
        update(HELP_MESSAGE_TEXT, mHelpMessageText);
    }

    private void update(int what, CharSequence string) {
        if (inWidgetMode()) {
            if (DEBUG) Log.v(TAG, "inWidgetMode() is true");
            // Use Transient text for messages shown while widget is shown.
            switch (what) {
                case INSTRUCTION_TEXT:
                case CARRIER_HELP_TEXT:
                case HELP_MESSAGE_TEXT:
                case BATTERY_INFO:
                    mTransientTextManager.post(string, 0, INSTRUCTION_RESET_DELAY);
                    break;

                case OWNER_INFO:
                case CARRIER_TEXT:
                default:
                    if (DEBUG) Xlog.w(TAG, "Not showing message id " + what + ", str=" + string);
            }
        } else {
            updateStatusLines(mShowingStatus);
        }
    }

    public void onPause() {
        if (DEBUG) Xlog.v(TAG, "onPause()");
        mUpdateMonitor.removeCallback(mInfoCallback);
        mUpdateMonitor.removeCallback(mSimStateCallback);
        mUpdateMonitor.removeCallback(mPhoneCallback);
        mUpdateMonitor.removeCallback(mDeviceInfoCallback);
    }

    /** {@inheritDoc} */
    public void onResume() {
        if (DEBUG) Xlog.v(TAG, "onResume()");
        mUpdateMonitor.registerInfoCallback(mInfoCallback);
        mUpdateMonitor.registerSimStateCallback(mSimStateCallback);
        mUpdateMonitor.registerPhoneStateCallback(mPhoneCallback);
        mUpdateMonitor.registerDeviceInfoCallback(mDeviceInfoCallback);
        resetStatusInfo();
    }

    void resetStatusInfo() {
        mInstructionText = null;
        mShowingBatteryInfo = mUpdateMonitor.shouldShowBatteryInfo();
        mPluggedIn = mUpdateMonitor.isDevicePluggedIn();
        mBatteryLevel = mUpdateMonitor.getBatteryLevel();
        mChargeTime = mUpdateMonitor.getBatteryChargeTime(); //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        updateStatusLines(true);
    }


    /** {@inheritDoc} */
    public void cleanUp() {
        if (DEBUG) Xlog.v(TAG, "cleanUp");
        mUpdateMonitor.removeCallback(mInfoCallback);
        mUpdateMonitor.removeCallback(mSimStateCallback);
        mUpdateMonitor.removeCallback(mPhoneCallback);
        mUpdateMonitor.removeCallback(mDeviceInfoCallback);
    }
    /**
     * Update the status lines based on these rules:
     * AlarmStatus: Alarm state always gets it's own line.
     * Status1 is shared between help, battery status and generic unlock instructions,
     * prioritized in that order.
     * @param showStatusLines status lines are shown if true
     */
    void updateStatusLines(boolean showStatusLines) {
        if (DEBUG) Xlog.v(TAG, "updateStatusLines(" + showStatusLines + ")");
        mShowingStatus = showStatusLines;
        updateAlarmInfo();
        updateOwnerInfo();
        updateStatus1();
        updateCarrierText();
        //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
        updateCalibrationDataText();
        //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not    
    }

    private void updateAlarmInfo() {
        if (mAlarmStatusView != null) {
            String nextAlarm = mLockPatternUtils.getNextAlarm();
            boolean showAlarm = mShowingStatus && !TextUtils.isEmpty(nextAlarm);
            mAlarmStatusView.setText(nextAlarm);
            mAlarmStatusView.setCompoundDrawablesWithIntrinsicBounds(ALARM_ICON, 0, 0, 0);
            mAlarmStatusView.setVisibility(showAlarm ? View.VISIBLE : View.GONE);
        }
    }

    private void updateOwnerInfo() {
        final ContentResolver res = getContext().getContentResolver();
        final boolean ownerInfoEnabled = Settings.Secure.getInt(res,
                Settings.Secure.LOCK_SCREEN_OWNER_INFO_ENABLED, 1) != 0;
        mOwnerInfoText = ownerInfoEnabled ?
                Settings.Secure.getString(res, Settings.Secure.LOCK_SCREEN_OWNER_INFO) : null;
        if (mOwnerInfoView != null) {
            mOwnerInfoView.setText(mOwnerInfoText);
            mOwnerInfoView.setVisibility(TextUtils.isEmpty(mOwnerInfoText) ? View.GONE:View.VISIBLE);
        }
    }

    private void updateStatus1() {
        if (mStatus1View != null) {
            MutableInt icon = new MutableInt(0);
            CharSequence string = getPriorityTextMessage(icon);
            mStatus1View.setText(string);
            mStatus1View.setCompoundDrawablesWithIntrinsicBounds(icon.value, 0, 0, 0);
            mStatus1View.setVisibility(mShowingStatus ? View.VISIBLE : View.INVISIBLE);
        }
    }


   private void showOrHideCarrier(){
      boolean SIM1Missing = false;
      boolean SIM2Missing = false;
      TextView carrierDivider = (TextView)findViewById(R.id.carrierDivider);

	  ConnectivityManager cm = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
      boolean bIsWifiOnly = (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);

      if (bIsWifiOnly)
      	{
      	   carrierDivider.setVisibility(View.GONE);
          mCarrierView.setVisibility(View.GONE);
          mCarrierGeminiView.setVisibility(View.GONE);
      	   return;
      	}

      if (mStatus == StatusMode.SimMissing || mStatus == StatusMode.SimMissingLocked) {
          SIM1Missing = true;
      }
      if (mGeminiStatus == StatusMode.SimMissing || mGeminiStatus == StatusMode.SimMissingLocked) {
          SIM2Missing = true;
      }
      Log.i(TAG,"mSIMOneMissing="+SIM1Missing+",mSIMTwoMissing="+SIM2Missing);
      if (SIM1Missing && SIM2Missing){//both of the two sim missing, only to display SIM1
          carrierDivider.setVisibility(View.GONE);
          mCarrierView.setVisibility(View.VISIBLE);
          mCarrierGeminiView.setVisibility(View.GONE);
          mCarrierView.setGravity(Gravity.CENTER);
      }else if (SIM1Missing && !SIM2Missing){//only sim one missing
          carrierDivider.setVisibility(View.GONE);
          mCarrierView.setVisibility(View.GONE);
          mCarrierGeminiView.setVisibility(View.VISIBLE);
          mCarrierGeminiView.setGravity(Gravity.CENTER);
      }else if (!SIM1Missing && SIM2Missing){//only sim two missing
          carrierDivider.setVisibility(View.GONE);
          mCarrierView.setVisibility(View.VISIBLE);
          mCarrierGeminiView.setVisibility(View.GONE);
          mCarrierView.setGravity(Gravity.CENTER);
      }else{//both of them are not missing
          carrierDivider.setVisibility(View.VISIBLE);
          //Drawable divider = getContext().getResources().getDrawable(R.drawable.fastscroll_thumb_default_holo);
          //carrierDivider.setCompoundDrawablesWithIntrinsicBounds(divider, null, null, null);
          carrierDivider.setText("|");
          mCarrierView.setVisibility(View.VISIBLE);
          mCarrierGeminiView.setVisibility(View.VISIBLE);
          mCarrierView.setGravity(Gravity.RIGHT);
          mCarrierGeminiView.setGravity(Gravity.LEFT);
       }

       if (mStatus == StatusMode.SimUnknown){
           carrierDivider.setVisibility(View.GONE);
           mCarrierView.setVisibility(View.GONE);
           mCarrierGeminiView.setGravity(Gravity.CENTER);
       }
       if (mGeminiStatus == StatusMode.SimUnknown){
           carrierDivider.setVisibility(View.GONE);
           mCarrierGeminiView.setVisibility(View.GONE);
           mCarrierView.setGravity(Gravity.CENTER);
       }
    }


    private void updateCarrierText() {
        //firstly, we should know which one operator we should show
        if (FeatureOption.MTK_GEMINI_SUPPORT && !isAccountMode()){
            showOrHideCarrier();
            Xlog.i(TAG, "updateCarrierText, mCarrierText="+mCarrierText+", mCarrierGeminiText="+mCarrierGeminiText);
        }
        if (!inWidgetMode() && mCarrierView != null) {
            if (FeatureOption.MTK_GEMINI_SUPPORT){
                //mCarrierView.setCompoundDrawablesWithIntrinsicBounds(mCarrierIcon, null, null, null);
            }
            mCarrierView.setText(mCarrierText);
        }
        if (!inWidgetMode() && (FeatureOption.MTK_GEMINI_SUPPORT)
            && mCarrierGeminiView != null && !isAccountMode()) {
            //mCarrierGeminiView.setCompoundDrawablesWithIntrinsicBounds(mCarrierGeminiIcon, null, null, null);
            mCarrierGeminiView.setText(mCarrierGeminiText);
        }
    }

    //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    private void updateCalibrationDataText(){
        if (mCalibrationData == null)
        	return;
        
        if (mDownloadCalibrationData){
            mCalibrationData.setVisibility(View.GONE);
        }else{
            Xlog.i(TAG, "updateCalibrationDataText");
            mCalibrationData.setText(R.string.calibration_data);
            mCalibrationData.setVisibility(View.VISIBLE);
        }
    }
    //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not

    private CharSequence getAltTextMessage(MutableInt icon) {
        // If we have replaced the status area with a single widget, then this code
        // prioritizes what to show in that space when all transient messages are gone.
        CharSequence string = null;
        if (mShowingBatteryInfo) {
            // Battery status
            if (mPluggedIn) {
                // Charging or charged
                if (mUpdateMonitor.isDeviceCharged()) {
                    string = getContext().getString(R.string.lockscreen_charged);
                } else {
                    string = getContext().getString(R.string.lockscreen_plugged_in, mBatteryLevel);
                }
               
                icon.value = CHARGING_ICON;
            } else if (mBatteryLevel < KeyguardUpdateMonitor.LOW_BATTERY_THRESHOLD) {
                // Battery is low
                string = getContext().getString(R.string.lockscreen_low_battery);
                icon.value = BATTERY_LOW_ICON;
            }
        } else {
            string = mCarrierText;
          
        }
        return string;
    }

    private CharSequence getPriorityTextMessage(MutableInt icon) {
        CharSequence string = null;
        String time; //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        if (!TextUtils.isEmpty(mInstructionText)) {
            // Instructions only
            string = mInstructionText;
            icon.value = LOCK_ICON;
        } else if (mShowingBatteryInfo) {
            // Plugged in and charging now.
            if (mPluggedIn && mUpdateMonitor.isDeviceCharging()) {
                // Charging or charged
                if (mUpdateMonitor.isDeviceCharged()) {
                	//Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                    //string = getContext().getString(R.string.lockscreen_charged);
                    time = getContext().getString(R.string.lockscreen_charged);
                    //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                } else {
                	//Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                    //string = getContext().getString(R.string.lockscreen_plugged_in, mBatteryLevel);
                    time = getContext().getString(R.string.lockscreen_plugged_in_charging);
                    //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                }
                //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                time += mChargeTime; 
                string = time; 
                //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                icon.value = CHARGING_ICON;
            } else if (mBatteryLevel < KeyguardUpdateMonitor.LOW_BATTERY_THRESHOLD) {
                // Battery is low
                string = getContext().getString(R.string.lockscreen_low_battery);
                icon.value = BATTERY_LOW_ICON;
            }
        } else if (!inWidgetMode() && mOwnerInfoView == null && mOwnerInfoText != null) {
            // OwnerInfo shows in status if we don't have a dedicated widget
            string = mOwnerInfoText;
        }
        return string;
    }

    void refreshDate() {
        if (mDateView != null) {
            String s = DateFormat.getDateFormat(getContext()).format(new Date());
            Log.i(TAG, "refreshDate, s="+s);
            mDateView.setText(s);
        }
    }

    /**
     * Determine the current status of the lock screen given the sim state and other stuff.
     */
    public StatusMode getStatusForIccState(IccCard.State simState) {
        // Since reading the SIM may take a while, we assume it is present until told otherwise.
        if (simState == null) {
            return StatusMode.SimUnknown;
        }

        final boolean missingAndNotProvisioned = (!mUpdateMonitor.isDeviceProvisioned()
                && (simState == IccCard.State.ABSENT || simState == IccCard.State.PERM_DISABLED));

        // Assume we're NETWORK_LOCKED if not provisioned
        simState = missingAndNotProvisioned ? State.NETWORK_LOCKED : simState;
        switch (simState) {
            case ABSENT:
                return StatusMode.SimMissing;
            case NETWORK_LOCKED:
                return StatusMode.NetworkLocked;
            case NOT_READY:
                return StatusMode.SimNotReady;
            case PIN_REQUIRED:
                return StatusMode.SimLocked;
            case PUK_REQUIRED:
                return StatusMode.SimPukLocked;
            case READY:
                return StatusMode.Normal;
            case PERM_DISABLED:
                return StatusMode.SimPermDisabled;
            case UNKNOWN:
                return StatusMode.SimUnknown;
        }
        return StatusMode.SimMissing;
    }

    private Context getContext() {
        return mContainer.getContext();
    }

    /**
     * Update carrier text, carrier help and emergency button to match the current status based
     * on SIM state.
     *
     * @param simState
     */
    private void updateCarrierTextWithSimStatus(State simState, int simId) {
        CharSequence carrierText = null;
        int carrierHelpTextId = 0;
        mUnlockDisabledDueToSimState = true;
        CharSequence plmn;
        CharSequence spn;
        StatusMode mode = StatusMode.Normal;

        if (StatusMode.NetworkSearching == mStatus && Phone.GEMINI_SIM_1 == simId
            || StatusMode.NetworkSearching == mGeminiStatus && Phone.GEMINI_SIM_2 == simId){
            Xlog.i(TAG, "updateCarrierTextWithSimStatus, searching network now, don't interrupt it, simState="
                +simState+", simId="+simId);
            return ;
        }
        
        if (Phone.GEMINI_SIM_2 == simId){
           mGeminiStatus = getStatusForIccState(simState);
           mSimGeminiState = simState;
           plmn = mUpdateMonitor.getTelephonyPlmn(Phone.GEMINI_SIM_2);
           spn = mUpdateMonitor.getTelephonySpn(Phone.GEMINI_SIM_2);
           mode = mGeminiStatus;
           //mCarrierGeminiIcon = querySIMIcon(Phone.GEMINI_SIM_2);
        }else {
           mStatus = getStatusForIccState(simState);
           mSimState = simState;
           plmn = mUpdateMonitor.getTelephonyPlmn(Phone.GEMINI_SIM_1);
           spn = mUpdateMonitor.getTelephonySpn(Phone.GEMINI_SIM_1);
           mode = mStatus;
           if (FeatureOption.MTK_GEMINI_SUPPORT){
               //mCarrierIcon = querySIMIcon(Phone.GEMINI_SIM_1);
           }
        }

        if (DEBUG) Xlog.d(TAG, "updateCarrierTextWithSimStatus(), simState = " + simState+", simId="+simId
            +", plmn="+plmn+", spn="+spn);
        switch (mode) {
            case SimUnknown:
            case Normal:
                carrierText = makeCarierString(plmn, spn);
                break;

            case NetworkLocked:
                carrierText = makeCarierString(plmn,
                        getContext().getText(R.string.lockscreen_network_locked_message));
                carrierHelpTextId = R.string.lockscreen_instructions_when_pattern_disabled;
                break;

            case SimNotReady:
                carrierText = makeCarierString(plmn, spn);
                break;

            case SimMissing:
                // Shows "No SIM card | Emergency calls only" on devices that are voice-capable.
                // This depends on mPlmn containing the text "Emergency calls only" when the radio
                // has some connectivity. Otherwise, it should be null or empty and just show
                // "No SIM card"
                carrierText =  makeCarrierStringOnEmergencyCapable(
                        getContext().getText(R.string.lockscreen_missing_sim_message_short),
                        plmn);
                carrierHelpTextId = R.string.lockscreen_missing_sim_instructions_long;
                break;

            case SimPermDisabled:
                carrierText = getContext().getText(R.string.lockscreen_missing_sim_message_short);
                carrierHelpTextId = R.string.lockscreen_permanent_disabled_sim_instructions;
                mEmergencyButtonEnabledBecauseSimLocked = true;
                break;

            case SimMissingLocked:
                carrierText =  makeCarrierStringOnEmergencyCapable(
                        getContext().getText(R.string.lockscreen_missing_sim_message_short),
                        plmn);
                carrierHelpTextId = R.string.lockscreen_missing_sim_instructions;
                mEmergencyButtonEnabledBecauseSimLocked = true;
                break;

            case SimLocked:
                carrierText = makeCarrierStringOnEmergencyCapable(
                        getContext().getText(R.string.lockscreen_sim_locked_message),
                        plmn);
                mEmergencyButtonEnabledBecauseSimLocked = true;
                break;

            case SimPukLocked:
                carrierText = makeCarrierStringOnEmergencyCapable(
                        getContext().getText(R.string.lockscreen_sim_puk_locked_message),
                        plmn);
                if (!mLockPatternUtils.isPukUnlockScreenEnable()) {
                    // This means we're showing the PUK unlock screen
                    mEmergencyButtonEnabledBecauseSimLocked = true;
                }
                break;
        }

        setCarrierText(carrierText, simId);

        if (StatusMode.Normal == mGeminiStatus || StatusMode.Normal == mStatus){
             mUnlockDisabledDueToSimState = false;
        }

        if (DEBUG) Xlog.d(TAG, "updateCarrierTextWithSimStatus(), simState = " + simState+", simId="+simId
            +", plmn="+plmn+", spn="+spn+", mUnlockDisabledDueToSimState="+mUnlockDisabledDueToSimState);

        setCarrierHelpText(carrierHelpTextId);
        //only mUnlock set true but button is gone or mUnlock is false but the button is visisble, we need
        //refresh, other wise, skip it.
        if (mUnlockDisabledDueToSimState && mEmergencyCallButton.getVisibility() != View.VISIBLE
            || !mUnlockDisabledDueToSimState && mEmergencyCallButton.getVisibility() == View.VISIBLE){
            updateEmergencyCallButtonState(mPhoneState);
        }
    }


    /*
     * Add emergencyCallMessage to carrier string only if phone supports emergency calls.
     */
    private CharSequence makeCarrierStringOnEmergencyCapable(
            CharSequence simMessage, CharSequence emergencyCallMessage) {
        if (mLockPatternUtils.isEmergencyCallCapable()) {
            return makeCarierString(simMessage, emergencyCallMessage);
        }
        return simMessage;
    }

    private Drawable querySIMIcon(int simId){
        if (!phoneAppAlive){
            phoneAppAlive = mUpdateMonitor.isPhoneAppReady();
        }
        Xlog.i(TAG, "querySIMIcon , phoneAppAlive="+phoneAppAlive+", simdId="+simId);
        if (phoneAppAlive){
            return mUpdateMonitor.getOptrDrawableBySlot(simId);
        }
        return null;
    }

   /**
     * get the sim icon background by colorIndex and sim card status.
     *
     * @param colorIndex
     * @param status
     * @return
     */
    private int getResourceID(int colorIndex, StatusMode status) {
        int resId = -1;
        Xlog.i(TAG, "getResourceID, colorIndex=:"+colorIndex+", status="+status);
        if (colorIndex < 0 || colorIndex >= SIM_COLOR_COUNT) {
            Xlog.e(TAG, "Invalid color index:" + colorIndex);
            return resId;
        }

        switch (status) {
            case Normal:
                resId = SIM_ICON_IMGS[SIM_COLOR_COUNT * SIM_NORMAL_INDEX + colorIndex];
                break;

            case SimNotReady:
            case NetworkLocked:
            case NetworkSearching:
                resId = SIM_ICON_IMGS[SIM_COLOR_COUNT * SIM_NO_SIGN_INDEX + colorIndex];
                break;

            case SimMissingLocked:
                resId = SIM_ICON_IMGS[SIM_COLOR_COUNT * SIM_NO_INDEX + colorIndex];
                break;
            case SimLocked:
            case SimPukLocked:
                resId = SIM_ICON_IMGS[SIM_COLOR_COUNT * SIM_LOCK_INDEX + colorIndex];
                break;
            case SimMissing:
                if (mUpdateMonitor.getSimState() == IccCard.State.UNKNOWN) {
                    resId = SIM_ICON_IMGS[SIM_COLOR_COUNT * SIM_NO_SIGN_INDEX + colorIndex];
                } else {
                    resId = SIM_ICON_IMGS[SIM_COLOR_COUNT * SIM_NO_INDEX + colorIndex];
                }
                break;
        }
        return resId;
    }


    private View findViewById(int id) {
        return mContainer.findViewById(id);
    }

    /**
     * The status of this lock screen. Primarily used for widgets on LockScreen.
     */
    enum StatusMode {
        /**
         * Normal case (sim card present, it's not locked)
         */
        Normal(true),

        /**
         * The sim card is 'network locked'.
         */
        NetworkLocked(true),

        /**
         * The sim card is missing.
         */
        SimMissing(false),

        /**
            * This time sim card is not ready, and this should be a temporay status.
            */
        SimNotReady(false),


        /**
         * The sim card is missing, and this is the device isn't provisioned, so we don't let
         * them get past the screen.
         */
        SimMissingLocked(false),

        NetworkSearching(true),
        /**
         * The sim card is PUK locked, meaning they've entered the wrong sim unlock code too many
         * times.
         */
        SimPukLocked(false),

        /**
         * The sim card is locked.
         */
        SimLocked(true),

        /**
         * The sim card is permanently disabled due to puk unlock failure
         */
        SimPermDisabled(false),

        /**
           * Add new SIM status for keyguard
           **/
        SimUnknown(false);
        private final boolean mShowStatusLines;

        StatusMode(boolean mShowStatusLines) {
            this.mShowStatusLines = mShowStatusLines;
        }

        /**
         * @return Whether the status lines (battery level and / or next alarm) are shown while
         *         in this state.  Mostly dictated by whether this is room for them.
         */
        public boolean shouldShowStatusLines() {
            return mShowStatusLines;
        }
    }

    private void updateEmergencyCallButton(int  phoneState, boolean shown) {

      TelephonyManager telephony = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
      boolean isVoiceCapable = (telephony != null && telephony.isVoiceCapable());
      if (DEBUG) Xlog.i(TAG, "updateEmergencyCallButton, isVoiceCapable="+isVoiceCapable);
      if (isVoiceCapable)
      	{
        if (mUpdateMonitor.DM_IsLocked() || KeyguardUpdateMonitor.mIsCMCC || (mLockPatternUtils.isEmergencyCallCapable() && shown)) {
            mEmergencyCallButton.setVisibility(View.VISIBLE);
        } else {
            mEmergencyCallButton.setVisibility(View.GONE);
            return;
        }

        int textId;
        if (phoneState == TelephonyManager.CALL_STATE_OFFHOOK) {
            // show "return to call" text and show phone icon
            textId = R.string.lockscreen_return_to_call;
            int phoneCallIcon = R.drawable.stat_sys_phone_call;
            mEmergencyCallButton.setCompoundDrawablesWithIntrinsicBounds(phoneCallIcon, 0, 0, 0);
        } else {
            textId = R.string.lockscreen_emergency_call;
            int emergencyIcon = R.drawable.ic_emergency;
            mEmergencyCallButton.setCompoundDrawablesWithIntrinsicBounds(emergencyIcon, 0, 0, 0);
        }
        mEmergencyCallButton.setText(textId);
      }
      else
      	{
      	     mEmergencyCallButton.setVisibility(View.GONE);
            return;
      	}
    }

    private void updateEmergencyCallButtonState(int phoneState) {
        //merge from Android 4.0, but we don't need it.
        /*boolean enabledBecauseSimLocked =
                    mLockPatternUtils.isEmergencyCallEnabledWhileSimLocked()
                    && mEmergencyButtonEnabledBecauseSimLocked;*/

        if (mEmergencyCallButton != null) {
            //merge from Android4.0, but we don't need it.
            //boolean showIfCapable = mShowEmergencyButtonByDefault || enabledBecauseSimLocked;
            boolean showIfCapable = mEmergencyCallButtonEnabledInScreen || mUnlockDisabledDueToSimState;
            Xlog.i(TAG, "updateEmergencyCallButtonState, phoneState="+phoneState+", showIfCapable="+showIfCapable
                +", mEmergencyCallButtonEnabledInScreen="+mEmergencyCallButtonEnabledInScreen
                +", mUnlockDisabledDueToSimState="+mUnlockDisabledDueToSimState);
            updateEmergencyCallButton(phoneState, showIfCapable);
        }
    }

    private KeyguardUpdateMonitor.InfoCallback mInfoCallback
            = new KeyguardUpdateMonitor.InfoCallback() {

        public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn,
                int batteryLevel) {
            mShowingBatteryInfo = showBatteryInfo;
            mPluggedIn = pluggedIn;
            mBatteryLevel = batteryLevel;
            mChargeTime = ""; //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
            final MutableInt tmpIcon = new MutableInt(0);
            update(BATTERY_INFO, getAltTextMessage(tmpIcon));
        }
        //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
		public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn,
                int batteryLevel, String time) {
            mShowingBatteryInfo = showBatteryInfo;
            mPluggedIn = pluggedIn;
            mBatteryLevel = batteryLevel;
            mChargeTime = time; 
            final MutableInt tmpIcon = new MutableInt(0);
            update(BATTERY_INFO, getAltTextMessage(tmpIcon));
        }
        //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        public void onTimeChanged() {
            refreshDate();
        }

        public void onRefreshCarrierInfo(CharSequence plmn, CharSequence spn) {
            mPlmn = plmn;
            mSpn = spn;

            if (FeatureOption.MTK_GEMINI_SUPPORT){
                updateCarrierTextWithSimStatus(mSimGeminiState, Phone.GEMINI_SIM_2);
                updateCarrierTextWithSimStatus(mSimState, Phone.GEMINI_SIM_1);
            }else{
                updateCarrierTextWithSimStatus(mSimState, Phone.GEMINI_SIM_1);
            }
        }

        public void onRingerModeChanged(int state) {

        }

        /***when the siminfo changed***/
        public void onSIMInfoChanged(int slotId){
             //only need to update the icon info
            boolean account = isAccountMode();
            Xlog.i(TAG, "onSIMInfoChanged, account="+account);
            if (account) {
                return;
            }
            if (Phone.GEMINI_SIM_2 == slotId){
                updateCarrierTextWithSimStatus(mSimGeminiState, Phone.GEMINI_SIM_2);
            } else {
                updateCarrierTextWithSimStatus(mSimState, Phone.GEMINI_SIM_1);
            }
        }

        public void onLockScreenUpdate(int slotId){
           //only need to update the icon info
            boolean account = isAccountMode();
            Xlog.i(TAG, "onLockScreenUpdate, account="+account);
            if (account){
                return;
            }
            if (Phone.GEMINI_SIM_2 == slotId){
                updateCarrierTextWithSimStatus(mSimGeminiState,Phone.GEMINI_SIM_2);
            } else {
                updateCarrierTextWithSimStatus(mSimState,Phone.GEMINI_SIM_1);
            }
        }

        public void onMissedCallChanged(int missedCall){
        }

        public void onWallpaperSetComplete(){
        }

        public void onSearchNetworkUpdate(int simId, boolean switchOn) {
            boolean account = isAccountMode();
            Xlog.i(TAG, "onSearchNetworkUpdate, account="+account+",simId = " + simId+", switchOn="+switchOn);
            if (account){
                return;
            }
            if (switchOn){
                if (FeatureOption.MTK_GEMINI_SUPPORT){
                   if (Phone.GEMINI_SIM_1 == simId){
                       mStatus = StatusMode.NetworkSearching;
                       mCarrierText = getContext().getString(com.mediatek.internal.R.string.network_searching);
                   }else{
                       mGeminiStatus = StatusMode.NetworkSearching;
                       mCarrierGeminiText = getContext().getString(com.mediatek.internal.R.string.network_searching);
                   }
                   updateCarrierText();
                }else{
                   mStatus = StatusMode.NetworkSearching;
                   mCarrierText = mCarrierText = getContext().getString(com.mediatek.internal.R.string.network_searching);
                   updateCarrierText();
                }
            }else{
                if (FeatureOption.MTK_GEMINI_SUPPORT){
                    IccCard.State status = mUpdateMonitor.getSimState(simId);
                    if (Phone.GEMINI_SIM_2 == simId){
                        mGeminiStatus = getStatusForIccState(status);
                    } else {
                        mStatus = getStatusForIccState(status);
                    }
                    updateCarrierTextWithSimStatus(status,simId);
                }else{
                    IccCard.State status = mUpdateMonitor.getSimState();
                    mStatus = getStatusForIccState(status);
                    updateCarrierTextWithSimStatus(status,Phone.GEMINI_SIM_1);
                }
            }
        }

        public void onUnlockKeyguard(){
        }

        //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
        public void onDownloadCalibrationDataUpdate(boolean DownloadCalibrationData){
            mDownloadCalibrationData = DownloadCalibrationData;
            updateCalibrationDataText();
        }		
        //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    };

    private deviceInfoCallback mDeviceInfoCallback = new deviceInfoCallback(){
        /** {@inheritDoc} */
        public void onClockVisibilityChanged() {
            // ignored
        }

        public void onDeviceProvisioned() {
            // ignored
        }

        public void onDMKeyguardUpdate(){
            View unlockWidget = (View) findViewById(R.id.unlock_widget);
            if (null != unlockWidget){
                unlockWidget.setVisibility(mUpdateMonitor.DM_IsLocked()?View.GONE : View.VISIBLE);
            }
            if (null != mEmergencyCallButton){
                mEmergencyCallButton.setVisibility(mUpdateMonitor.DM_IsLocked()?View.VISIBLE : View.GONE);
            }
            if (null != mDMPrompt){
                mDMPrompt.setVisibility(mUpdateMonitor.DM_IsLocked()?View.VISIBLE : View.GONE);
                mDMPrompt.setText(com.mediatek.internal.R.string.dm_prompt);
            }

            if (mContainer instanceof PasswordUnlockScreen){
                final int mode = mLockPatternUtils.getKeyguardStoredPasswordQuality();
                switch (mode){
                   case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
                       updatePINWhenDMChanged();
                       break;

                   default:
                       updatePasswordWhenDMChanged(unlockWidget);
                       break;
               }
            }
        }
    };

    private void updatePINWhenDMChanged(){
        int visible = mUpdateMonitor.DM_IsLocked()?View.GONE:View.VISIBLE;
        View keypadView = findViewById(R.id.keypad);
        if (null != keypadView){
            keypadView.setVisibility(visible);
        }
    }

    private void updatePasswordWhenDMChanged(View unlockView){
       InputMethodManager imm = ((InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
       if (mUpdateMonitor.DM_IsLocked()) {
           if (imm.isActive()) {
               Xlog.i(TAG, "IME is showing, we should hide it");
               imm.hideSoftInputFromWindow(unlockView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
           }
       }else{
           Xlog.i(TAG, "IME is hide, we should show it");
           mContainer.invalidate();
       }
    }

    private phoneStateCallback mPhoneCallback = new phoneStateCallback(){
        public void onPhoneStateChanged(int phoneState) {
            mPhoneState = phoneState;
            updateEmergencyCallButtonState(phoneState);
        }
    };

    private SimStateCallback mSimStateCallback = new SimStateCallback() {

        public void onSimStateChanged(State simState) {
            boolean account = isAccountMode();
            Xlog.i(TAG, "mSimStateCallback, account="+account+", simState="+simState);
            if (account){
                return;
            }
            updateCarrierTextWithSimStatus(simState, Phone.GEMINI_SIM_1);
        }

        public void onSimStateChangedGemini(IccCard.State simState, int simId) {
            boolean account = isAccountMode();
            Xlog.i(TAG, "mSimStateCallback, account="+account+", simState="+simState+", simId="+simId);
            if (account){
                return;
            }
            updateCarrierTextWithSimStatus(simState, simId);
        }
    };

    public void onClick(View v) {
        if (v == mEmergencyCallButton) {
            mCallback.takeEmergencyCallAction();
        }
    }

    /**
     * Performs concentenation of PLMN/SPN
     * @param plmn
     * @param spn
     * @return
     */
    private static CharSequence makeCarierString(CharSequence plmn, CharSequence spn) {
        final boolean plmnValid = !TextUtils.isEmpty(plmn);
        final boolean spnValid = !TextUtils.isEmpty(spn);
        if (plmnValid && spnValid) {
            return plmn + "|" + spn;
        } else if (plmnValid) {
            return plmn;
        } else if (spnValid) {
            return spn;
        } else {
            return "";
        }
    }
}
