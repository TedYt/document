/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.INotificationManager;
import android.app.ITransientNotification;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.graphics.PixelFormat;


import com.android.internal.telephony.ITelephony;
import com.android.internal.widget.LockPatternUtils;

import android.view.Gravity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.telephony.TelephonyManager;
import android.view.WindowManagerImpl;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.EditText;
import com.android.internal.R;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.TelephonyProperties;
import android.util.Log;
import com.mediatek.featureoption.FeatureOption;
import android.content.Intent;
import android.provider.Settings;
import android.graphics.drawable.Drawable;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.policy.impl.KeyguardUpdateMonitor.phoneStateCallback;
import com.mediatek.xlog.Xlog;
/**
 * Displays a dialer like interface to unlock the SIM PIN.
 */
public class SimUnlockScreen extends LinearLayout implements KeyguardScreen, View.OnClickListener,
        KeyguardUpdateMonitor.InfoCallback, KeyguardUpdateMonitor.RadioStateCallback {
    private static final String TAG = "SimUnlockScreen";

    private static final int DIGIT_PRESS_WAKE_MILLIS = 5000;

    private final KeyguardUpdateMonitor mUpdateMonitor;
    private final KeyguardScreenCallback mCallback;

    private TextView mResultPrompt = null;
    private TextView mHeaderText;
    private TextView mTimesLeft = null;
    private TextView mSIMCardName = null;
    private Button mMoreInfoBtn = null;
 
    private TextView mPinText;

    private TextView mOkButton;
    private Button mEmergencyCallButton;
    private TextView mCancelButton = null;
    private View mBackSpaceButton;

    private final int[] mEnteredPin = {0, 0, 0, 0, 0, 0, 0, 0};
    private int mEnteredDigits = 0;

    private ProgressDialog mSimUnlockProgressDialog = null;

    private LockPatternUtils mLockPatternUtils;

    private int mCreationOrientation;

    private int mKeyboardHidden;

    // size limits for the pin.
    private static final int MIN_PIN_LENGTH = 4;
    private static final int MAX_PIN_LENGTH = 8;

    private static final int GET_SIM_RETRY_EMPTY = -1;

    private static final int STATE_ENTER_PIN = 0;
    private static final int STATE_ENTER_PUK = 1;
    private static final int STATE_ENTER_NEW = 2;
    private static final int STATE_REENTER_NEW = 3;
    private static final int STATE_ENTER_FINISH = 4;

    private int mPukEnterState;

    private int mPINRetryCount;
    private int mPUKRetryCount;
    private int mSim2PINRetryCount;
    private int mSim2PUKRetryCount;

    private boolean sim1FirstBoot = false;
    private boolean sim2FirstBoot = false;

    private String mPukText;
    private String mNewPinText;

    public IccCard.State mSimState;
    public IccCard.State mSim2State;

    /**here, for singlesim, mSimId=0;
      *for gemini the first sim, mSimId=1;
      *for gemini the second sim, mSimId=2;
      **/
      
    public int mSimId = 0;
    private static int SIMLOCK_TYPE_PIN = 1;
    private static int SIMLOCK_TYPE_SIMMELOCK = 2;
    static final int VERIFY_TYPE_PIN = 501;

    private static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    /**
     * The Status of this SIM.
     */
    enum SIMStatus {
        /**
         * For sim1 verify both sim inserted
         */
    	SIM1_BOTH_SIM_INSERTED,

        /**
         * For sim1 verify, only SIM1 inserted
         */
    	SIM1_ONLY_SIM1_INSERTED,

        /**
         * For sim2 verify, both sim inserted
         */
    	SIM2_BOTH_SIM_INSERTED,
        /**
         * For sim2 verify, only SIM2 inserted
         */
    	SIM2_ONLY_SIM1_INSERTED,
        }
        

    public SimUnlockScreen(Context context, Configuration configuration,
            KeyguardUpdateMonitor updateMonitor, KeyguardScreenCallback callback,
            LockPatternUtils lockpatternutils, int simId) {
        super(context);
        mUpdateMonitor = updateMonitor;
        mCallback = callback;
        mSimId = simId;
        mCreationOrientation = configuration.orientation;
        Xlog.i(TAG,"SimUnlockScreen Constructor, mSimId="+simId+", mCreationOrientation="+mCreationOrientation);

        mKeyboardHidden = configuration.hardKeyboardHidden;
        mLockPatternUtils = lockpatternutils;

        LayoutInflater inflater = LayoutInflater.from(context);

        //if (Configuration.ORIENTATION_LANDSCAPE == mCreationOrientation) {
           //inflater.inflate(com.mediatek.internal.R.layout.keyguard_screen_sim_pin_landscape, this, true);
       // } else {
            if (FeatureOption.MTK_GEMINI_SUPPORT){
                inflater.inflate(com.mediatek.internal.R.layout.keyguard_screen_sim_pin_portrait_gemini, this, true);
                ((TextView)findViewById(com.mediatek.internal.R.id.ForText)).setText(""); 
                mSIMCardName = (TextView) findViewById(com.mediatek.internal.R.id.SIMCardName);
            } else{
                inflater.inflate(com.mediatek.internal.R.layout.keyguard_screen_sim_pin_portrait, this, true);
            }
            findViewById(com.mediatek.internal.R.id.emergencyCall).setBackgroundDrawable(context.getResources().getDrawable(com.mediatek.internal.R.drawable.eccbtn));
            if (findViewById(com.mediatek.internal.R.id.EnterPassword) != null) {
                findViewById(com.mediatek.internal.R.id.EnterPassword).setBackgroundDrawable(context.getResources().getDrawable(com.mediatek.internal.R.drawable.btndeletenobgb));
            }
            findViewById(com.mediatek.internal.R.id.pinDisplay).setBackgroundDrawable(context.getResources().getDrawable(com.mediatek.internal.R.drawable.edit_text_configure));
            ((ImageButton)findViewById(com.mediatek.internal.R.id.backspace)).setImageDrawable(context.getResources().getDrawable(com.mediatek.internal.R.drawable.phone_dial_delete_button));
            mTimesLeft = (TextView) findViewById(com.mediatek.internal.R.id.TimeLeft);
            mPinText = (TextView) findViewById(com.mediatek.internal.R.id.pinDisplay);
            mPinText.setTextColor(0xff000000);
            mResultPrompt = (TextView)findViewById(com.mediatek.internal.R.id.ResultDisplay);
            if (findViewById(com.mediatek.internal.R.id.keyPad) != null){
                new TouchInput();
            }
        //}

        mHeaderText = (TextView) findViewById(com.mediatek.internal.R.id.headerText);
        mPinText = (TextView) findViewById(com.mediatek.internal.R.id.pinDisplay);
        mPinText.setTextColor(0xff000000);
        mBackSpaceButton = findViewById(com.mediatek.internal.R.id.backspace);
        mBackSpaceButton.setOnClickListener(this);

        mEmergencyCallButton = (Button) findViewById(com.mediatek.internal.R.id.emergencyCall);
        updateEmergencyCallButtonState(mEmergencyCallButton);
        mOkButton = (TextView) findViewById(com.mediatek.internal.R.id.ok);
        mCancelButton = (TextView) findViewById(com.mediatek.internal.R.id.cancel);
        if(mPinText != null)
        	mPinText.setFocusable(true);
            
        if(mEmergencyCallButton != null)
        	mEmergencyCallButton.setFocusable(true);

        mOkButton.setFocusable(true);
        mCancelButton.setFocusable(true);
        updateSimState();

        if (mKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            // landscape mode
            setFocusable(true);
            setFocusableInTouchMode(true);
            mPinText.requestFocus();
            mPinText.setFocusable(true);
        }else {
            // it's for onKeyDown callback
            setFocusable(true);
            setFocusableInTouchMode(true);
        }

        mEmergencyCallButton.setOnClickListener(this);
        mOkButton.setOnClickListener(this);
        mUpdateMonitor.registerInfoCallback(this);
        mUpdateMonitor.registerRadioStateCallback(this);
        mUpdateMonitor.registerPhoneStateCallback(mPhoneCallback);
        if (FeatureOption.MTK_GEMINI_SUPPORT){
           String siminfoupdate = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_INFO_READY, "false");
           if (siminfoupdate.equals("true")){
               Xlog.i(TAG,"siminfo already update, we should read value from the siminfo");
               dealwithSIMInfoChanged(mSimId);
           }
        }
    }

    
    private void displaythesimcardinfo(int slotId){
        if (Phone.GEMINI_SIM_1 == slotId && mUpdateMonitor.IsSIMInserted(Phone.GEMINI_SIM_2)){
            popupSIMInfoDialog(SIMStatus.SIM1_BOTH_SIM_INSERTED);
        }else if (Phone.GEMINI_SIM_1 == slotId && (!mUpdateMonitor.IsSIMInserted(Phone.GEMINI_SIM_2))){
            popupSIMInfoDialog(SIMStatus.SIM1_ONLY_SIM1_INSERTED);
        }else if (Phone.GEMINI_SIM_2 == slotId && mUpdateMonitor.IsSIMInserted(Phone.GEMINI_SIM_1)){
            popupSIMInfoDialog(SIMStatus.SIM2_BOTH_SIM_INSERTED);
        }else{
            popupSIMInfoDialog(SIMStatus.SIM2_ONLY_SIM1_INSERTED);
        }
    }
    
    private void popupSIMInfoDialog(SIMStatus status){
        ImageView View = new ImageView(mContext);
        View.setScaleType(ImageView.ScaleType.FIT_XY);
        switch (status)
        {
        case SIM1_BOTH_SIM_INSERTED:
            View.setBackgroundDrawable(getResources().getDrawable(com.mediatek.internal.R.drawable.sim1_both_sim_inserted));
            break;
    
        case SIM1_ONLY_SIM1_INSERTED:
            View.setBackgroundDrawable(getResources().getDrawable(com.mediatek.internal.R.drawable.sim1_only_sim1_inserted));
            break;
    
        case SIM2_BOTH_SIM_INSERTED:
            View.setBackgroundDrawable(getResources().getDrawable(com.mediatek.internal.R.drawable.sim2_both_sim_inserted));
            break;
    
        case SIM2_ONLY_SIM1_INSERTED:
            View.setBackgroundDrawable(getResources().getDrawable(com.mediatek.internal.R.drawable.sim2_only_sim2_inserted));
            break;
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
        dialogBuilder.setTitle(com.mediatek.internal.R.string.more_info_title);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(View);
        dialogBuilder.setPositiveButton(R.string.ok, null);
        AlertDialog SIMCardDialog = dialogBuilder.create();
        SIMCardDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        SIMCardDialog.show();
    }
    
    
    /** {@inheritDoc} */
    public boolean needsInput() {
        return true;
    }

    /** {@inheritDoc} */
    public void onPause() {

    }

    public void updateSimState() {
        Xlog.i(TAG, "updateSimSate, simId="+mSimId+", sim1FirstBoot="+sim1FirstBoot+",sim2FirstBoot="+sim2FirstBoot);
        if (mTimesLeft != null) {
            mTimesLeft.setVisibility(View.VISIBLE);
        }
        if (mResultPrompt != null) {
            mResultPrompt.setText("");
        }

        if (FeatureOption.MTK_GEMINI_SUPPORT){
            if ((true == sim1FirstBoot || true == sim2FirstBoot)){ 
                if (null != mMoreInfoBtn){
                    mMoreInfoBtn.setText(com.mediatek.internal.R.string.more_siminfo_for_button);
                }else{//need to init mMoreInfoBtn
                    mMoreInfoBtn = (Button)findViewById(com.mediatek.internal.R.id.moresiminfo);
                    if (mMoreInfoBtn != null) {
                        mMoreInfoBtn.setVisibility(View.VISIBLE);
                        mMoreInfoBtn
                                .setText(com.mediatek.internal.R.string.more_siminfo_for_button);
                        mMoreInfoBtn.setOnClickListener(new OnClickListener() {
                            public void onClick(View v) {
                                displaythesimcardinfo(mSimId);
                            }
                        });
                    }
                }
            }
            getSIMCardName(mSimId);
        }
        
        if (Phone.GEMINI_SIM_2 == mSimId){
           mSim2State = mUpdateMonitor.getSimState(Phone.GEMINI_SIM_2);
           if (IccCard.State.PUK_REQUIRED == mSim2State) {
               Xlog.d(TAG, "updateSimState, mSim2State = PUK_REQUIRED");
               mHeaderText.setText(com.mediatek.R.string.keyguard_password_enter_puk_code);
               mTimesLeft.setText(getRetryPuk(Phone.GEMINI_SIM_2));
               mPukEnterState = STATE_ENTER_PUK;
           } else if (IccCard.State.PIN_REQUIRED == mSim2State){
               Xlog.d(TAG, "updateSimState, mSim2State = PIN_REQUIRED");
               mHeaderText.setText(R.string.keyguard_password_enter_pin_code);
               mTimesLeft.setText(getRetryPin(Phone.GEMINI_SIM_2));
               mPukEnterState = STATE_ENTER_PIN;
           }
           if (sim2FirstBoot){
               ((TextView)findViewById(com.mediatek.internal.R.id.ForText)).setText(com.mediatek.internal.R.string.for_second_simcard);
           }else{
               ((TextView)findViewById(com.mediatek.internal.R.id.ForText)).setText("");
           }
        } else if (Phone.GEMINI_SIM_1 == mSimId){
            mSimState = mUpdateMonitor.getSimState(Phone.GEMINI_SIM_1);
           if (mSimState == IccCard.State.PUK_REQUIRED) {
               Xlog.d(TAG, "updateSimState1, mSimState = PUK_REQUIRED");
               mHeaderText.setText(mContext.getText(com.mediatek.R.string.keyguard_password_enter_puk_code));
               mTimesLeft.setText(getRetryPuk(Phone.GEMINI_SIM_1));
               mPukEnterState = STATE_ENTER_PUK;
           } else if (mSimState == IccCard.State.PIN_REQUIRED){
               Xlog.d(TAG, "updateSimState1, mSimState = PIN_REQUIRED");
               mHeaderText.setText(mContext.getText(R.string.keyguard_password_enter_pin_code));
               mTimesLeft.setText(getRetryPin(Phone.GEMINI_SIM_1));
               mPukEnterState = STATE_ENTER_PIN;
           } 
           if (FeatureOption.MTK_GEMINI_SUPPORT){
               if (sim1FirstBoot){
                    ((TextView)findViewById(com.mediatek.internal.R.id.ForText)).setText(com.mediatek.internal.R.string.for_first_simcard);
               } else {
                    ((TextView)findViewById(com.mediatek.internal.R.id.ForText)).setText("");
               }
           }
        }else{//for single SIM
           Log.e(TAG, "updateSimState, wrong simId:"+mSimId);
        }  
    }

    private void getSIMCardName(final int slotId){
        Drawable d = null;
        String s = null;
        try {
            d = mUpdateMonitor.getOptrDrawableBySlot(slotId);
        }catch (IndexOutOfBoundsException e){
            Xlog.w(TAG, "getSIMCardName::getOptrDrawableBySlot exception, slotId="+slotId);
        }
        if (null != d){//need to reset?
            mSIMCardName.setBackgroundDrawable(d);
        }

        try {
            s = mUpdateMonitor.getOptrNameBySlot(slotId);
        }catch (IndexOutOfBoundsException e){
            Xlog.w(TAG, "getSIMCardName::getOptrNameBySlot exception, slotId="+slotId);
        }
        Xlog.i(TAG, "slotId="+slotId+", mSimId="+mSimId+",s="+s);
     
        if (null != s){
            mSIMCardName.setText(s);
        }else if (mSimId == Phone.GEMINI_SIM_2 && sim2FirstBoot 
             || mSimId == Phone.GEMINI_SIM_1 && sim1FirstBoot){
            Xlog.i(TAG, "getSIMCardName for the first reboot");
            TextView forText = (TextView)findViewById(com.mediatek.internal.R.id.ForText);
            if (Phone.GEMINI_SIM_2 == slotId){
                forText.setText(com.mediatek.internal.R.string.for_second_simcard);
            }else{
                forText.setText(com.mediatek.internal.R.string.for_first_simcard);
            }
        }else{
            Xlog.i(TAG, "getSIMCardName for seaching SIM card");
            mSIMCardName.setText(com.mediatek.internal.R.string.searching_simcard);
        }
    }

    private void dealWithEnterMessage(){
        if(FeatureOption.MTK_GEMINI_SUPPORT){
            if (mUpdateMonitor.getSimState(Phone.GEMINI_SIM_1) == IccCard.State.PIN_REQUIRED
                && Phone.GEMINI_SIM_1 == mSimId){
                Xlog.d(TAG, "onClick, check PIN, mSimId="+mSimId);
                checkPin(mSimId);
            }else if (mUpdateMonitor.getSimState(Phone.GEMINI_SIM_2) == IccCard.State.PIN_REQUIRED
                && Phone.GEMINI_SIM_2 == mSimId){
                Xlog.d(TAG, "onClick, check PIN, mSimId="+mSimId);
                checkPin(mSimId);
            }else if (mUpdateMonitor.getSimState(Phone.GEMINI_SIM_1) == IccCard.State.PUK_REQUIRED
                && Phone.GEMINI_SIM_1 == mSimId){
                Xlog.d(TAG, "onClick, check PUK, mSimId="+mSimId);
                checkPuk(mSimId);
            }else if (mUpdateMonitor.getSimState(Phone.GEMINI_SIM_2) == IccCard.State.PUK_REQUIRED
                && Phone.GEMINI_SIM_2 == mSimId){
                Xlog.d(TAG, "onClick, check PUK, mSimId="+mSimId);
                checkPuk(mSimId);
            }else {
                Xlog.d(TAG, "wrong status");
            }
        }
        else{            
            if (mUpdateMonitor.getSimState() == IccCard.State.PIN_REQUIRED)
            {
                Xlog.d(TAG, "onClick, check Pin for single SIM");
                checkPin();
            }
            else if (mUpdateMonitor.getSimState() == IccCard.State.PUK_REQUIRED) 
            {
                Xlog.d(TAG, "onClick, check PUK for single SIM");
                checkPuk();
            }
        }
    }


    /** {@inheritDoc} */
    public void onResume() {

        mCallback.pokeWakelock();
        // start fresh
        updateSimState();

        // make sure that the number of entered digits is consistent when we
        // erase the SIM unlock code, including orientation changes.
        mPinText.setText("");
        mEnteredDigits = 0;
        updateEmergencyCallButtonState(mEmergencyCallButton);
        
        //if has IME, then hide it
        InputMethodManager imm = ((InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (imm.isActive()) {
            Log.i(TAG, "IME is showing, we should hide it");
            imm.hideSoftInputFromWindow(this.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);  
        }
    }

    /** {@inheritDoc} */
    public void cleanUp() {
        // hide the dialog.
        if (mSimUnlockProgressDialog != null) {
            mSimUnlockProgressDialog.hide();
        }
        mUpdateMonitor.removeCallback(this);
        mUpdateMonitor.unRegisterRadioStateCallback();
        mUpdateMonitor.removeCallback(mPhoneCallback);
    }


    /**
     * Since the IPC can block, we want to run the request in a separate thread
     * with a callback.
     */
    private abstract class CheckSimPin extends Thread {

        private final String mPin;
        private final String mPuk;
        //private int mSimId = -1;
        private boolean result;

        protected CheckSimPin(String pin) {
            mPin = pin;
            mPuk = null;
        }
        protected CheckSimPin(String pin, int simId) {
            mPin = pin;
            mPuk = null;
            //mSimId = simId;
        }

        protected CheckSimPin(String puk, String pin, int simId) {
            mPin = pin;
            mPuk = puk;
            //mSimId = simId;
        }

        abstract void onSimLockChangedResponse(boolean success);

        @Override
        public void run() {
            try {
                Log.d(TAG, "CheckSimPin," + "mSimId =" + mSimId);

                if(mSimId == Phone.GEMINI_SIM_1) {
                    Log.d(TAG, "CheckSimPin, check sim 1 or single");
                    if (mUpdateMonitor.getSimState(Phone.GEMINI_SIM_1) == IccCard.State.PIN_REQUIRED)
                    {
                        if(FeatureOption.MTK_GEMINI_SUPPORT){
                            result = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPinGemini(mPin, Phone.GEMINI_SIM_1);
                        }else{
                            result = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPin(mPin);
                        }
                    }
                    else if (mUpdateMonitor.getSimState(Phone.GEMINI_SIM_1) == IccCard.State.PUK_REQUIRED)
                    {
                        if(FeatureOption.MTK_GEMINI_SUPPORT){
                            result = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPukGemini(mPuk, mPin, Phone.GEMINI_SIM_1);
                        }else{
                            result = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPuk(mPuk, mPin);
                        }
                    }
                    post(new Runnable() {
                    	public void run() {
                        	onSimLockChangedResponse(result);
                    	}
                	});
                }else if(FeatureOption.MTK_GEMINI_SUPPORT) {
                    Log.d(TAG, "CheckSimPin, check sim 2");
                    if (mUpdateMonitor.getSimState(Phone.GEMINI_SIM_2) == IccCard.State.PIN_REQUIRED)
                    {
                    	result = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPinGemini(mPin, Phone.GEMINI_SIM_2);
                    }
                    else if (mUpdateMonitor.getSimState(Phone.GEMINI_SIM_2) == IccCard.State.PUK_REQUIRED)
                    {
                    	result = ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).supplyPukGemini(mPuk, mPin, Phone.GEMINI_SIM_2);
                    }
                    post(new Runnable() {
                        public void run() {
                            onSimLockChangedResponse(result);
                        }
                    });
                }
            } catch (RemoteException e) {
                post(new Runnable() {
                    public void run() {
                        onSimLockChangedResponse(false);
                    }
                });
            }
        }
    }

    public void onClick(View v) {
        if (v == mBackSpaceButton) {
            final Editable digits = mPinText.getEditableText();
            final int len = digits.length();
            if (len > 0) {
                digits.delete(len-1, len);
                mEnteredDigits--;
            }
            mCallback.pokeWakelock();
        } else if (v == mEmergencyCallButton) {
            mCallback.takeEmergencyCallAction();
        } else if (v == mOkButton) {
            dealWithEnterMessage();
            mCallback.pokeWakelock(10000);
        }
    }

    private Dialog getSimUnlockProgressDialog() {
        if (mSimUnlockProgressDialog == null) {
            mSimUnlockProgressDialog = new ProgressDialog(mContext);
            mSimUnlockProgressDialog.setMessage(
                    mContext.getString(R.string.lockscreen_sim_unlock_progress_dialog_message));
            mSimUnlockProgressDialog.setIndeterminate(true);
            mSimUnlockProgressDialog.setCancelable(false);
            mSimUnlockProgressDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_sf_slowBlur); /*{
                mSimUnlockProgressDialog.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
            }*/
        }
        return mSimUnlockProgressDialog;
    }
    private void setInputInvalidAlertDialog(CharSequence message, boolean shouldDisplay) {
        StringBuilder sb = new StringBuilder(message);

        if (shouldDisplay) {
            AlertDialog newDialog = new AlertDialog.Builder(mContext)
            .setMessage(sb)
            .setPositiveButton(R.string.ok, null)
            .setCancelable(true)
            .create();

            newDialog.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
            newDialog.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            newDialog.show();
        } else {
             Toast.makeText(mContext,sb).show();
        }
    }

	private int getRetryPukCount(final int simId) {
        if (mSimId == Phone.GEMINI_SIM_2)
			return SystemProperties.getInt("gsm.sim.retry.puk1.2",GET_SIM_RETRY_EMPTY);
		else
			return SystemProperties.getInt("gsm.sim.retry.puk1",GET_SIM_RETRY_EMPTY);
		}

    private int getRetryPinCount(final int simId) {
        if (mSimId == Phone.GEMINI_SIM_2)
		return SystemProperties.getInt("gsm.sim.retry.pin1.2",GET_SIM_RETRY_EMPTY);
	else
		return SystemProperties.getInt("gsm.sim.retry.pin1",GET_SIM_RETRY_EMPTY);
    }

    private String getRetryPuk(final int simId) {
        mPUKRetryCount = getRetryPukCount(simId);
        switch (mPUKRetryCount) {
        case GET_SIM_RETRY_EMPTY:
            return " ";
        //case 1:
        //    return "(" + mContext.getString(com.mediatek.R.string.one_retry_left) + ")";
        default:
            return "(" + mContext.getString(com.mediatek.R.string.retries_left,mPUKRetryCount) + ")";
        }
    }
    private String getRetryPin(final int simId) {
        mPINRetryCount = getRetryPinCount(simId);
        switch (mPINRetryCount) {
        case GET_SIM_RETRY_EMPTY:
            return " ";
        //case 1:
        //    return "(" + mContext.getString(com.mediatek.R.string.one_retry_left) + ")";
        default:
            return "(" + mContext.getString(com.mediatek.R.string.retries_left,mPINRetryCount) + ")";
        }
    }
    private boolean validatePin(String pin, boolean isPUK) {
        // for pin, we have 4-8 numbers, or puk, we use only 8.
        int pinMinimum = isPUK ? MAX_PIN_LENGTH : MIN_PIN_LENGTH;
        // check validity
        if (pin == null || pin.length() < pinMinimum
                || pin.length() > MAX_PIN_LENGTH) {
            return false;
        } else {
            return true;
        }
    }

     private void updatePinEnterScreen() {
        if (FeatureOption.MTK_GEMINI_SUPPORT){
           if (sim2FirstBoot){
               ((TextView)findViewById(com.mediatek.internal.R.id.ForText)).setText(com.mediatek.internal.R.string.for_second_simcard);
           }else if (sim1FirstBoot){
               ((TextView)findViewById(com.mediatek.internal.R.id.ForText)).setText(com.mediatek.internal.R.string.for_first_simcard);
           }else{
               ((TextView)findViewById(com.mediatek.internal.R.id.ForText)).setText(""); 
           }
        }
        switch (mPukEnterState) {
            case STATE_ENTER_PUK:
               mPukText = mPinText.getText().toString();
               if (validatePin(mPukText, true)) {
                  mPukEnterState = STATE_ENTER_NEW;
                  mHeaderText.setText(com.mediatek.R.string.keyguard_password_enter_new_pin_code);
                  mResultPrompt.setText("");
                  mTimesLeft.setVisibility(View.GONE);
               }else {
                  mResultPrompt.setText(com.mediatek.R.string.invalidPuk);
                  mResultPrompt.setFocusable(true);
               }
               break;

             case STATE_ENTER_NEW:
                 mNewPinText = mPinText.getText().toString();
                 if (validatePin(mNewPinText, false)) {
                    mPukEnterState = STATE_REENTER_NEW;
                    mHeaderText.setText(com.mediatek.R.string.keyguard_password_Confirm_pin_code);
                    mResultPrompt.setText("");
                 } else {
                    mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_code_length_prompt);
                    mResultPrompt.setFocusable(true);
                 }
                 break;

             case STATE_REENTER_NEW:
                if (!mNewPinText.equals(mPinText.getText().toString())) {
                    mPukEnterState = STATE_ENTER_NEW;
                    mHeaderText.setText(mContext.getText(com.mediatek.R.string.keyguard_password_enter_new_pin_code));
                    mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_code_donnot_mismatch);
                    mResultPrompt.setFocusable(true);
                } else {
                   mPukEnterState = STATE_ENTER_FINISH;
                   mResultPrompt.setText("");
                }
                break;

                default:
                    break;
        }
        mPinText.setText("");
        mEnteredDigits = 0; 
        mCallback.pokeWakelock();
    }

    private void checkPuk() {
        updatePinEnterScreen();
        if (mPukEnterState != STATE_ENTER_FINISH){
            return;
        }
        getSimUnlockProgressDialog().show();
        new CheckSimPin(mPukText, mNewPinText, Phone.GEMINI_SIM_1) {
            void onSimLockChangedResponse(boolean success) {
                if (mSimUnlockProgressDialog != null) {
                    mSimUnlockProgressDialog.hide();
                }
                if (success) {
                    // before closing the keyguard, report back that
                    // the sim is unlocked so it knows right away
                    mUpdateMonitor.reportSimUnlocked(mSimId);
                    if (true == mUpdateMonitor.DM_IsLocked()){
                        Xlog.i("keyguard","we clicked cancel button");
                        mCallback.goToLockScreen();
                    }else{
                        mCallback.goToUnlockScreen();
                    }
                } else {//failure
                    mTimesLeft.setVisibility(View.VISIBLE);
                    mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_wrong_code_input);
                    mResultPrompt.setFocusable(true);
                    int retryCount = getRetryPukCount(Phone.GEMINI_SIM_1);
                    boolean countChange = (mPUKRetryCount != retryCount);
                    String retryInfo = getRetryPuk(Phone.GEMINI_SIM_1);
                    //add for gemini enhanment
                    mHeaderText.setText(com.mediatek.R.string.keyguard_password_enter_puk_code);
                    mTimesLeft.setText(retryInfo);
                    //end for gemini enhanment
                    mPinText.setText("");
                    mEnteredDigits = 0;
                    mPukEnterState = STATE_ENTER_PUK;
                    if (retryCount != 0) {
                        if (countChange) {
                            setInputInvalidAlertDialog(mContext.getString(com.mediatek.R.string.keyguard_password_wrong_puk_code)+ retryInfo, false);
                        } else {
                            setInputInvalidAlertDialog(mContext.getString(R.string.lockscreen_pattern_wrong), false);
                        }
                    } else {
                        setInputInvalidAlertDialog(mContext.getString(com.mediatek.R.string.sim_permanently_locked), true);
                    }
                    mCallback.pokeWakelock();
                }
            }
        }.start();
        mPinText.setText("");
    }

	private void checkPuk(final int simId) {
        updatePinEnterScreen();
        if (mPukEnterState != STATE_ENTER_FINISH){
            return;
        }
        getSimUnlockProgressDialog().show();
        new CheckSimPin(mPukText, mNewPinText, mSimId) {
            void onSimLockChangedResponse(boolean success) {
                if (mSimUnlockProgressDialog != null) {
                    mSimUnlockProgressDialog.hide();
                }
                if (success) {
                    // before closing the keyguard, report back that
                    // the sim is unlocked so it knows right away

                    //M{
                    // mUpdateMonitor.reportSimPinUnlocked();
                    mUpdateMonitor.reportSimUnlocked(mSimId);
                    //}M

                    if (true == mUpdateMonitor.DM_IsLocked()){
                        mCallback.goToLockScreen();
                    }else{
                        mCallback.goToUnlockScreen();
                    }
                } else {
                    //Xlog.i(TAG,"should reenter puk again");
                    mTimesLeft.setVisibility(View.VISIBLE);
                    getSIMCardName(mSimId);
                    mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_wrong_code_input);
                    mResultPrompt.setFocusable(true);
                    int retryCount = getRetryPukCount(mSimId);
                    boolean countChange = (mPUKRetryCount != retryCount);
                    String retryInfo = getRetryPuk(simId);
                    //add for gemini enhanment
                    mHeaderText.setText(com.mediatek.R.string.keyguard_password_enter_puk_code);
                    mTimesLeft.setText(retryInfo);
                    //end for gemini enhanment
                    mPinText.setText("");
                    mEnteredDigits = 0;
                    mPukEnterState = STATE_ENTER_PUK;
                    if (retryCount != 0) {
                        if (countChange) {
                            setInputInvalidAlertDialog(mContext.getString(com.mediatek.R.string.keyguard_password_wrong_puk_code)+ retryInfo, false);
                        } else {
                            setInputInvalidAlertDialog(mContext.getString(R.string.lockscreen_pattern_wrong), false);
                        }
                    } else {
                        setInputInvalidAlertDialog(mContext.getString(com.mediatek.R.string.sim_permanently_locked), true);
                    }
                    mCallback.pokeWakelock();
                }
            }
        }.start();
        mPinText.setText("");
    }

    private void checkPin() {
        mNewPinText = mPinText.getText().toString();
        mEnteredDigits = mNewPinText.length();
        if (mEnteredDigits == 0){
           mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_code_length_prompt);
           return;
        }
        // make sure that the pin is at least 4 digits long.
        if (mEnteredDigits < 4) {
            // otherwise, display a message to the user, and don't submit.
            //mHeaderText.setText(R.string.invalidPin);
            mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_code_length_prompt);
            mPinText.setText("");
            mEnteredDigits = 0;
            mCallback.pokeWakelock();
            return;
        }
        getSimUnlockProgressDialog().show();

        new CheckSimPin(mNewPinText) {
            void onSimLockChangedResponse(boolean success) {
                Xlog.d(TAG, "onSimLockChangedResponse, success = " + success );
                if (mSimUnlockProgressDialog != null) {
                    mSimUnlockProgressDialog.hide();
                }
                if (success) {
                    // before closing the keyguard, report back that
                    // the sim is unlocked so it knows right away
                    mUpdateMonitor.reportSimUnlocked(mSimId);
                    if (true == mUpdateMonitor.DM_IsLocked()){
                        mCallback.goToLockScreen();
                    }else{
                        mCallback.goToUnlockScreen();
                    }
                } else {
                    if (mPukEnterState == STATE_ENTER_PIN)
                    {
                        mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_wrong_code_input);
                        mResultPrompt.setFocusable(true);
                        int retryCount = getRetryPinCount(Phone.GEMINI_SIM_1);
                        if (retryCount == 0)//goto PUK
                        {
                            mHeaderText.setText(com.mediatek.R.string.keyguard_password_enter_puk_code);
                            mTimesLeft.setText(getRetryPuk(Phone.GEMINI_SIM_1));
                            //getSIMCardName(simId);
                            mPukEnterState = STATE_ENTER_PUK;
                        }
                        else
                        {
                            mHeaderText.setText(R.string.keyguard_password_enter_pin_code);
                            mTimesLeft.setText(getRetryPin(Phone.GEMINI_SIM_1));
                            //getSIMCardName(simId);
                        }
                        mPinText.setText("");
                        mEnteredDigits = 0;

                    }
                    else if (mPukEnterState == STATE_ENTER_PUK)
                    {
                        mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_wrong_code_input);
                        mResultPrompt.setFocusable(true);
                        int retryCount = getRetryPukCount(Phone.GEMINI_SIM_1);
                        if (retryCount == 0)//goto PUK
                        {
                            mHeaderText.setText(com.mediatek.R.string.keyguard_password_enter_puk_code);
                            mTimesLeft.setText(getRetryPuk(Phone.GEMINI_SIM_1));
                            //getSIMCardName(simId);
                            mPukEnterState = STATE_ENTER_PUK;
                        }
                        else
                        {
                            mHeaderText.setText(R.string.keyguard_password_enter_pin_code);
                            mTimesLeft.setText(getRetryPin(Phone.GEMINI_SIM_1));
                         }
                         mPinText.setText("");
                         mEnteredDigits = 0;
                    }
                }
                mCallback.pokeWakelock();
            }
        }.start();
        mPinText.setText("");
    }

    private void checkPin(final int simId) {
        mNewPinText = mPinText.getText().toString();
        mEnteredDigits = mNewPinText.length();
        if (mEnteredDigits == 0){
            mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_code_length_prompt);
            return;
        }
        // make sure that the pin is at least 4 digits long.
        if (mEnteredDigits < 4) {
            // otherwise, display a message to the user, and don't submit.
            //modify for gemini
            //mHeaderText.setText(R.string.invalidPin);
            mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_code_length_prompt);
            mResultPrompt.setFocusable(true);
            mPinText.setText("");
            mEnteredDigits = 0;
            mCallback.pokeWakelock();
            return;
        }
        getSimUnlockProgressDialog().show();

        new CheckSimPin(mNewPinText, simId) {
            void onSimLockChangedResponse(boolean success) {
                Xlog.d(TAG, "onSimLockChangedResponse, simId = " + mSimId + ", success = " + success );
                if (mSimUnlockProgressDialog != null) {
                    mSimUnlockProgressDialog.hide();
                }
                if (success) {
                    // before closing the keyguard, report back that
                    // the sim is unlocked so it knows right away
                    mUpdateMonitor.reportSimUnlocked(mSimId);
                    if (true == mUpdateMonitor.DM_IsLocked()){
                        Xlog.i("keyguard","we clicked cancel button");
                        mCallback.goToLockScreen();
                    }else{
                        mCallback.goToUnlockScreen();
                    }
                } else {
                    //add for gemini
                    getSIMCardName(mSimId);
                    Xlog.d(TAG, "onSimLockChangedResponse, simId = " + mSimId + ", success = " + success
                        +",mPukEnterState="+mPukEnterState);
					if (mPukEnterState == STATE_ENTER_PIN)
					{
                        mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_wrong_code_input);
                        mResultPrompt.setFocusable(true);
                        int retryCount = getRetryPinCount(mSimId);
                        Xlog.d(TAG, "check PIN, retryCount="+retryCount);
                        if (retryCount == 0){
                            mHeaderText.setText(com.mediatek.R.string.keyguard_password_enter_puk_code);
                            mTimesLeft.setText(getRetryPuk(mSimId));
                            mPukEnterState = STATE_ENTER_PUK;
                        }else {
                            mHeaderText.setText(R.string.keyguard_password_enter_pin_code);
                            mTimesLeft.setText(getRetryPin(mSimId));
                        }
                        mPinText.setText("");
                        mEnteredDigits = 0;
					}
					else if (mPukEnterState == STATE_ENTER_PUK)
					{
                        mResultPrompt.setText(com.mediatek.internal.R.string.keyguard_wrong_code_input);
                        mResultPrompt.setFocusable(true);
                        int retryCount = getRetryPukCount(mSimId);
						boolean countChange = (mPUKRetryCount != retryCount);
                        String retryInfo = getRetryPuk(mSimId);
                        mHeaderText.setText(com.mediatek.R.string.keyguard_password_enter_puk_code);
                        mTimesLeft.setText(retryInfo);
                        //getSIMCardName(simId);
						mPinText.setText("");
						mEnteredDigits = 0;
						mPukEnterState = STATE_ENTER_PUK;
						if (retryCount != 0) {
							if (countChange) {
								setInputInvalidAlertDialog(mContext.getString(com.mediatek.R.string.keyguard_password_wrong_puk_code)+ retryInfo, false);
							} else {
								setInputInvalidAlertDialog(mContext.getString(R.string.lockscreen_pattern_wrong), false);
							}
						} else {
							setInputInvalidAlertDialog(mContext.getString(com.mediatek.R.string.sim_permanently_locked), true);
						}
					}
                }
                mCallback.pokeWakelock();
            }
        }.start();
        mPinText.setText("");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch(keyCode){

        case KeyEvent.KEYCODE_BACK:
            break;

        case KeyEvent.KEYCODE_DEL:
            if (mEnteredDigits > 0) {
	        	final Editable digits = mPinText.getEditableText();
            	final int len = digits.length();
                mPinText.onKeyDown(keyCode, event);
                if (mEnteredDigits > 0)
                {
                    digits.delete(len-1, len);
                    mEnteredDigits--;
                }
                mCallback.pokeWakelock();
        }
            break;

        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_DPAD_CENTER:
            dealWithEnterMessage();
            break;

        case KeyEvent.KEYCODE_DPAD_DOWN:
        case KeyEvent.KEYCODE_DPAD_LEFT:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
        case KeyEvent.KEYCODE_DPAD_UP:
            mCallback.pokeWakelock();
        		return false;
            	//break;

        default:
            final char match = event.getMatch(DIGITS);
            if (match != 0) {
                reportDigit(match - '0');
                break;
            } else {
                return false;
            }
        }
        return true;
    }

    private void reportDigit(int digit) {
        if (mEnteredDigits == 0) {
            mPinText.setText("");
        }
        if (mEnteredDigits == 8) {
            return;
        }
        Xlog.v(TAG, "EnterDigits = " + Integer.toString(mEnteredDigits) + " input digit is " + Integer.toString(digit));
        mPinText.append(Integer.toString(digit));
        mEnteredPin[mEnteredDigits++] = digit;
    }

    void updateConfiguration() {
    	
       Xlog.d(TAG, "Call updateSimState in updateConfiguration(), refresh header text.");
		updateSimState();
		
        Configuration newConfig = getResources().getConfiguration();
        if (newConfig.orientation != mCreationOrientation)
        {
            mCallback.recreateMe(newConfig);
        } else if (newConfig.hardKeyboardHidden != mKeyboardHidden) {
            mKeyboardHidden = newConfig.hardKeyboardHidden;
            final boolean isKeyboardOpen = mKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            if (mUpdateMonitor.isKeyguardBypassEnabled() && isKeyboardOpen)
            {
                if (true == mUpdateMonitor.DM_IsLocked()){
                    Xlog.i("keyguard","we clicked cancel button");
                    mCallback.goToLockScreen();
                }else{
                    mCallback.goToUnlockScreen();
                }
            }
            else
            {
				mCallback.recreateMe(newConfig);
            }
        }
    }

    /**
     * Sets the text on the emergency button to indicate what action will be taken.
     * If there's currently a call in progress, the button will take them to the call
     * @param button the button to update
     */
    public void updateEmergencyCallButtonState(Button button) {
        int newState = TelephonyManager.getDefault().getCallState();
        int textId;

        TelephonyManager telephony = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE); 
        boolean isVoiceCapable = (telephony != null && telephony.isVoiceCapable());        

        if (isVoiceCapable)
        {
        if (newState == TelephonyManager.CALL_STATE_OFFHOOK) {
            // show "return to call" text and show phone icon
            textId = R.string.lockscreen_return_to_call;
            int phoneCallIcon = com.mediatek.internal.R.drawable.pin_lock_emgencycall_icon;
            button.setCompoundDrawablesWithIntrinsicBounds(phoneCallIcon, 0, 0, 0);
        } else {
            textId = R.string.lockscreen_emergency_call;
            int emergencyIcon = com.mediatek.internal.R.drawable.pin_lock_emgencycall_icon;
            button.setCompoundDrawablesWithIntrinsicBounds(emergencyIcon, 0, 0, 0);
        }
        button.setText(textId);
    }
        else
        {
           button.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateConfiguration();
    }

    /** {@inheritDoc} */
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateConfiguration();
    }

    /**
     * Helper class to handle input from touch dialer.  Only relevant when
     * the keyboard is shut.
     */
    private class TouchInput implements View.OnClickListener {
        private TextView mZero;
        private TextView mOne;
        private TextView mTwo;
        private TextView mThree;
        private TextView mFour;
        private TextView mFive;
        private TextView mSix;
        private TextView mSeven;
        private TextView mEight;
        private TextView mNine;
        private TextView mCancelButton;

        private TouchInput() {
            mZero = (TextView) findViewById(com.mediatek.internal.R.id.zero);
            mOne = (TextView) findViewById(com.mediatek.internal.R.id.one);
            mTwo = (TextView) findViewById(com.mediatek.internal.R.id.two);
            mThree = (TextView) findViewById(com.mediatek.internal.R.id.three);
            mFour = (TextView) findViewById(com.mediatek.internal.R.id.four);
            mFive = (TextView) findViewById(com.mediatek.internal.R.id.five);
            mSix = (TextView) findViewById(com.mediatek.internal.R.id.six);
            mSeven = (TextView) findViewById(com.mediatek.internal.R.id.seven);
            mEight = (TextView) findViewById(com.mediatek.internal.R.id.eight);
            mNine = (TextView) findViewById(com.mediatek.internal.R.id.nine);
            mCancelButton = (TextView) findViewById(com.mediatek.internal.R.id.cancel);

            mZero.setText("0");
            mOne.setText("1");
            mTwo.setText("2");
            mThree.setText("3");
            mFour.setText("4");
            mFive.setText("5");
            mSix.setText("6");
            mSeven.setText("7");
            mEight.setText("8");
            mNine.setText("9");

            mZero.setOnClickListener(this);
            mOne.setOnClickListener(this);
            mTwo.setOnClickListener(this);
            mThree.setOnClickListener(this);
            mFour.setOnClickListener(this);
            mFive.setOnClickListener(this);
            mSix.setOnClickListener(this);
            mSeven.setOnClickListener(this);
            mEight.setOnClickListener(this);
            mNine.setOnClickListener(this);
            mCancelButton.setOnClickListener(this);
        }


        public void onClick(View v) {
            if (v == mCancelButton) {
               if (IccCard.State.PIN_REQUIRED == mUpdateMonitor.getSimState(mSimId)){
                   mUpdateMonitor.setPINDismiss(mSimId, true, true);
               } else {
                   mUpdateMonitor.setPINDismiss(mSimId, false, true);
               }
               if (FeatureOption.MTK_GEMINI_SUPPORT){
                   setSimLockScreenDone(mSimId, SIMLOCK_TYPE_PIN);
                   Intent t = new Intent("action_pin_dismiss");
                   t.putExtra("simslot", mSimId);
                   mContext.sendBroadcast(t);
               }else{
                   setSimLockScreenDone(Phone.GEMINI_SIM_1, SIMLOCK_TYPE_PIN);
               }
               sendVerifyResult(VERIFY_TYPE_PIN,false);
                mCallback.pokeWakelock();
                if (true == mUpdateMonitor.DM_IsLocked()){
                   Xlog.i("keyguard","we clicked cancel button, DM is locked");
                    mCallback.goToLockScreen();
                }else{
                   Xlog.i("keyguard","we clicked cancel button");
                    mCallback.goToUnlockScreen();
                }
                return;
            }

            final int digit = checkDigit(v);
            if (digit >= 0) {
                mCallback.pokeWakelock(DIGIT_PRESS_WAKE_MILLIS);
                reportDigit(digit);
            }
        }

        private int checkDigit(View v) {
            int digit = -1;
            if (v == mZero) {
                digit = 0;
            } else if (v == mOne) {
                digit = 1;
            } else if (v == mTwo) {
                digit = 2;
            } else if (v == mThree) {
                digit = 3;
            } else if (v == mFour) {
                digit = 4;
            } else if (v == mFive) {
                digit = 5;
            } else if (v == mSix) {
                digit = 6;
            } else if (v == mSeven) {
                digit = 7;
            } else if (v == mEight) {
                digit = 8;
            } else if (v == mNine) {
                digit = 9;
            }
            return digit;
        }
    }

	private phoneStateCallback mPhoneCallback = new phoneStateCallback() {
		public void onPhoneStateChanged(int newState) {
			updateEmergencyCallButtonState(mEmergencyCallButton);
		}
	};
    public void onUnlockKeyguard() {
    }

    public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn, int batteryLevel) {

    }
    
    //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
	public void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn, int batteryLevel, String time) {

    }
    //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
    
    public void onRefreshCarrierInfo(CharSequence plmn, CharSequence spn) {

    }

    public void onRingerModeChanged(int state) {

    }

    public void onTimeChanged() {

    }

//MTK-START [mtk80950][120410][ALPS00266631]check whether download calibration data or not
    public void onDownloadCalibrationDataUpdate(boolean mClibrationData){

    }
//MTK-END [mtk80950][120410][ALPS00266631]check whether download calibration data or not

    private void dealwithSIMInfoChanged(int slotId){
        String operName = null;
        Drawable bkground = null;
        if (null != mUpdateMonitor && FeatureOption.MTK_GEMINI_SUPPORT){
            try{
               bkground = mUpdateMonitor.getOptrDrawableBySlot(slotId);
            }catch (IndexOutOfBoundsException e){
               Xlog.w(TAG, "getOptrDrawableBySlot exception, slotId="+slotId);
            }
            
            try{
               operName = mUpdateMonitor.getOptrNameBySlot(slotId);
            }catch (IndexOutOfBoundsException e){
               Xlog.w(TAG, "getOptrNameBySlot exception, slotId="+slotId);
            }
        }
        if (null == operName){//this is the new SIM card inserted
            LayoutParams lp = (LayoutParams)mSIMCardName.getLayoutParams();
            lp.width = 0;
            mSIMCardName.setLayoutParams(lp);
            TextView forText = (TextView)findViewById(com.mediatek.internal.R.id.ForText);
            if (Phone.GEMINI_SIM_2 == mSimId){
                Xlog.i(TAG,"SIM2 is first reboot");
                sim2FirstBoot = true;
                forText.setText(com.mediatek.internal.R.string.for_second_simcard);
            }else{
                Xlog.i(TAG,"SIM1 is first reboot");
                sim1FirstBoot = true;
                forText.setText(com.mediatek.internal.R.string.for_first_simcard);
            }
            if (null != mMoreInfoBtn){
                mMoreInfoBtn.setText(com.mediatek.internal.R.string.more_siminfo_for_button);
            }else{//need to init mMoreInfoBtn
                mMoreInfoBtn = (Button)findViewById(com.mediatek.internal.R.id.moresiminfo);
                if (mMoreInfoBtn != null) {
                    mMoreInfoBtn.setVisibility(View.VISIBLE);
                    mMoreInfoBtn
                            .setText(com.mediatek.internal.R.string.more_siminfo_for_button);
                    mMoreInfoBtn.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            displaythesimcardinfo(mSimId);
                        }
                    });
                }
            }
        }else if (mSimId== slotId){
            Xlog.i(TAG, "dealwithSIMInfoChanged, we will refresh the SIMinfo");
            mSIMCardName.setText(operName);
            if (null != bkground){
                mSIMCardName.setBackgroundDrawable(bkground);
            }
        }
    }

    public void onSIMInfoChanged(int slotId){
        Xlog.i(TAG,"onSIMInfoChanged");
        dealwithSIMInfoChanged(slotId);
    }

    public void onLockScreenUpdate(int slotId){
        Xlog.i(TAG, "onLockScreenUpdate name update, slotId="+slotId+", mSimId="+mSimId);
        if (FeatureOption.MTK_GEMINI_SUPPORT && (mSimId == slotId)){
            //refresh the name for the SIM Card
            getSIMCardName(slotId);
        }
    }

    public void onRadioStateChanged(int slotId){
        Xlog.i(TAG, "onRadioStateChanged, slotId="+slotId+", mSimId="+mSimId);
        if (mSimId == slotId){
            //here we should destroy this PIN interface
            mCallback.pokeWakelock();
            mCallback.goToUnlockScreen();
        }
    }

     public void onMissedCallChanged(int missedCall){};
     public void onWallpaperSetComplete() {};
     public void onSearchNetworkUpdate(int simId, boolean switchOn){};
        
public static class Toast {
	static final String TAG = "Toast";
	static final boolean localLOGV = false;

	final Handler mHandler = new Handler();
	final Context mContext;
	final TN mTN;
	int mGravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
	int mY;
	View mView;

	public Toast(Context context) {
		mContext = context;
		mTN = new TN();
		mY = context.getResources().getDimensionPixelSize(
				com.android.internal.R.dimen.toast_y_offset);
	}

	public static Toast makeText(Context context, CharSequence text) {
		Toast result = new Toast(context);

		LayoutInflater inflate = (LayoutInflater)
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflate.inflate(com.android.internal.R.layout.transient_notification, null);
		TextView tv = (TextView)v.findViewById(com.android.internal.R.id.message);
		tv.setText(text);

		result.mView = v;

		return result;
	}
	/**
	 * Show the view for the specified duration.
	 */
	public void show() {
		if (mView == null) {
			throw new RuntimeException("setView must have been called");
		}
		INotificationManager service = getService();
		String pkg = mContext.getPackageName();
		TN tn = mTN;
		try {
			service.enqueueToast(pkg, tn, 0);
		} catch (RemoteException e) {
			// Empty
		}
	}
	/**
	 * Close the view if it's showing, or don't show it if it isn't showing yet.
	 * You do not normally have to call this.  Normally view will disappear on its own
	 * after the appropriate duration.
	 */
	public void cancel() {
		mTN.hide();
	}

	private INotificationManager sService;

	private INotificationManager getService() {
		if (sService != null) {
			return sService;
		}
		sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
		return sService;
	}

	private class TN extends ITransientNotification.Stub {
		final Runnable mShow = new Runnable() {
			public void run() {
				handleShow();
			}
		};

		final Runnable mHide = new Runnable() {
			public void run() {
				handleHide();
			}
		};

		private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();

		WindowManagerImpl mWM;

		TN() {
			final WindowManager.LayoutParams params = mParams;
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			params.width = WindowManager.LayoutParams.WRAP_CONTENT;
			params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
					| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
					| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
			params.format = PixelFormat.TRANSLUCENT;
			params.windowAnimations = com.android.internal.R.style.Animation_Toast;
			params.type = WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
			params.setTitle("Toast");
		}

		/**
		 * schedule handleShow into the right thread
		 */
		public void show() {
			if (localLOGV) Xlog.v(TAG, "SHOW: " + this);
			mHandler.post(mShow);
		}

		/**
		 * schedule handleHide into the right thread
		 */
		public void hide() {
			if (localLOGV) Xlog.v(TAG, "HIDE: " + this);
			mHandler.post(mHide);
		}

		public void handleShow() {
			if (localLOGV) Xlog.v(TAG, "HANDLE SHOW: " + this + " mView=" + mView);

			mWM = WindowManagerImpl.getDefault();
			final int gravity = mGravity;
			mParams.gravity = gravity;
			if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
				mParams.horizontalWeight = 1.0f;
			}
			if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
				mParams.verticalWeight = 1.0f;
			}
			mParams.y = mY;
			if (mView != null) {
				if (mView.getParent() != null) {
					if (localLOGV) Xlog.v(
							TAG, "REMOVE! " + mView + " in " + this);
					mWM.removeView(mView);
				}
				if (localLOGV) Xlog.v(TAG, "ADD! " + mView + " in " + this);
				mWM.addView(mView, mParams);
			}
		}

		public void handleHide() {
			if (localLOGV) Xlog.v(TAG, "HANDLE HIDE: " + this + " mView=" + mView);
			if (mView != null) {
				// note: checking parent() just to make sure the view has
				// been added...  i have seen cases where we get here when
				// the view isn't yet added, so let's try not to crash.
				if (mView.getParent() != null) {
					if (localLOGV) Xlog.v(
							TAG, "REMOVE! " + mView + " in " + this);
					mWM.removeView(mView);
				}

				mView = null;
			}
		}
	}
    }

    private boolean isSimLockDisplay(int slot, int type) {
    	if (slot < 0) {
    		return false;
    	}
    	
    	Long simLockState = Settings.System.getLong(mContext.getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, 0);
    	Long bitSet = simLockState;
    	
    	bitSet = bitSet>>>2*slot;
    	if (SIMLOCK_TYPE_PIN == type) {
	    if (0x1L == (bitSet & 0x1L)) {
		   return true;
	    } else {
		   return false;
	    }
    	} else if (SIMLOCK_TYPE_SIMMELOCK == type) {
	        bitSet = bitSet>>>1;
	    if (0x1L == (bitSet & 0x1L)) {
		   return true;
	    } else {
		   return false;
	    }
    	}
    	
    	return true;
    }

    private void setSimLockScreenDone(int slot, int type) {
    	if (slot < 0) {
    		return ;
    	}
    	
    	if (isSimLockDisplay(slot, type)) {
    		Xlog.d(TAG, "setSimLockScreenDone the SimLock display is done");
    		return;
    	}
    	
    	Long simLockState = Settings.System.getLong(mContext.getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, 0);
    	
    	Long bitSet = 0x1L;
    	
    	bitSet = bitSet<<2*slot;
    	Xlog.d(TAG, "setSimLockScreenDone1 bitset = " + bitSet);
	    if (SIMLOCK_TYPE_SIMMELOCK == type) {
		    bitSet = bitSet << 1;
	    }
	    Xlog.d(TAG, "setSimLockScreenDone2 bitset = " + bitSet);
	
	    simLockState += bitSet;
	    Settings.System.putLong(mContext.getContentResolver(), Settings.System.SIM_LOCK_STATE_SETTING, simLockState);
    }

    public void sendVerifyResult(int verifyType, boolean bRet) {
	Xlog.d(TAG, "sendVerifyResult verifyType = " + verifyType
			+ " bRet = " + bRet);
	Intent retIntent = new Intent("android.intent.action.CELLCONNSERVICE").putExtra("start_type", "response");

	if (null == retIntent) {
		Xlog.e(TAG, "sendVerifyResult new retIntent failed");
		return;
	}
	retIntent.putExtra("verfiy_type", verifyType);
	retIntent.putExtra("verfiy_result", bRet);

	mContext.startService(retIntent);
    }   
}

