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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_FULL;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
import static android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN;
import static android.os.BatteryManager.EXTRA_STATUS;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_HEALTH;
import android.media.AudioManager;
import android.media.IRemoteControlClient;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.provider.Telephony;
import android.os.SystemProperties;

import static android.provider.Telephony.Intents.EXTRA_PLMN;
import static android.provider.Telephony.Intents.EXTRA_SHOW_PLMN;
import static android.provider.Telephony.Intents.EXTRA_SHOW_SPN;
import static android.provider.Telephony.Intents.EXTRA_SPN;
import static android.provider.Telephony.Intents.SPN_STRINGS_UPDATED_ACTION;
import static android.provider.Telephony.Intents.ACTION_DUAL_SIM_MODE_SELECT;
import static android.provider.Telephony.Intents.ACTION_GPRS_CONNECTION_TYPE_SELECT;
import static android.provider.Telephony.Intents.ACTION_UNLOCK_KEYGUARD;


import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;

import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.TelephonyIntents;
import static com.android.internal.telephony.TelephonyIntents.EXTRA_CALIBRATION_DATA;
import static com.android.internal.telephony.TelephonyIntents.ACTION_DOWNLOAD_CALIBRATION_DATA;

import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.R;
import com.google.android.collect.Lists;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.content.DialogInterface;
import com.mediatek.featureoption.FeatureOption;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.gemini.GeminiNetworkSubUtil;
import com.android.internal.telephony.gemini.GeminiPhone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.ITelephony;
import android.os.ServiceManager;
import android.os.RemoteException;
// Added by OMA DM
import com.mediatek.dmagent.DMAgent;
import android.os.IBinder;

import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import android.widget.TextView;
import android.telephony.ServiceState;
import android.telephony.PhoneStateListener;
import android.widget.Button;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.ComponentName;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import java.util.List;
import com.mediatek.xlog.Xlog;
import com.android.internal.widget.LockPatternUtils;

//Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.android.powersaving.*;
//End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>

/**
 * Watches for updates that may be interesting to the keyguard, and provides
 * the up to date information as well as a registration for callbacks that care
 * to be updated.
 *
 * Note: under time crunch, this has been extended to include some stuff that
 * doesn't really belong here.  see {@link #handleBatteryUpdate} where it shutdowns
 * the device, and {@link #getFailedAttempts()}, {@link #reportFailedAttempt()}
 * and {@link #clearFailedAttempts()}.  Maybe we should rename this 'KeyguardContext'...
 */
public class KeyguardUpdateMonitor {

    static private final String TAG = "KeyguardUpdateMonitor";
    private boolean DEBUG = true;

    /* package */ static final int LOW_BATTERY_THRESHOLD = 16;

    private final Context mContext;

    private IccCard.State mSimState = IccCard.State.UNKNOWN;
    private IccCard.State mSim2State = IccCard.State.UNKNOWN;
    
    private IccCard.State mSimLastState = IccCard.State.UNKNOWN;
    private IccCard.State mSim2LastState = IccCard.State.UNKNOWN;
    
    private boolean mInPortrait;
    private boolean mKeyboardOpen;
    

    private BatteryStatus mBatteryStatus;

    // used by slide lock screen to check wheather user has changed wallpaper when it dismissed
    private boolean mWallpaperUpdate;

    private boolean mKeyguardBypassEnabled;

    private boolean mDevicePluggedIn;

    private boolean mDeviceProvisioned;

    private CharSequence mTelephonyPlmn;
    private CharSequence mTelephonySpn;
    private CharSequence mTelephonyPlmnGemini;
    private CharSequence mTelephonySpnGemini;

    private int mFailedAttempts = 0;

    private boolean mClockVisible;

    private Handler mHandler;
    private PhoneStateListener mPhoneStateListener;
    private PhoneStateListener mPhoneStateListenerGemini;
    private AlertDialog mDialog = null;
    private AlertDialog mSIMCardDialog = null;
    private View mPromptView = null;
    private boolean mSIMRemoved = false;
    private int mCardTotal = 0;
    private AlertDialog mGPRSDialog1 = null;
    private AlertDialog mGPRSDialog2 = null;
    private int mMissedCall = 0;
    private boolean mWallpaperSetComplete = true;//this flag should be true when device reboot
    private SIMStatus mSimStatus;
    private boolean shouldPopup = false;
    protected static final boolean mIsCMCC = SystemProperties.get("ro.operator.optr").equals("OP01");
    /**
     * dual_sim_setting is flag used for GlobalActions to check if user has clicked
     * the dual_sim_mode_setting dialog
     * -1: dialog has not been created;
     * 0: dialog has been created but has not been clicked;
     * 1: dialog has been created and has been clicked;
     */
    public static int dual_sim_setting = -1; 
    private ArrayList<InfoCallback> mInfoCallbacks = Lists.newArrayList();
    private ArrayList<SimStateCallback> mSimStateCallbacks = Lists.newArrayList();
    private ArrayList<phoneStateCallback> mPhoneCallbacks = Lists.newArrayList();
    private ArrayList<deviceInfoCallback> mDeviceInfoCallbacks = Lists.newArrayList();
    
    private SystemStateCallback mSystemStateCallback; // ALPS00264727: system notification change for KeyguardViewManager
    private ContentObserver mContentObserver;
    private RadioStateCallback mRadioStateCallback;
    private PackageManager mPm;
    private ComponentName mComponentName;
    private int mRingMode;
    private int mPhoneState;
    
    // messages for the handler
    private static final int MSG_TIME_UPDATE = 301;
    private static final int MSG_BATTERY_UPDATE = 302;
    private static final int MSG_CARRIER_INFO_UPDATE = 303;
    private static final int MSG_SIM_STATE_CHANGE = 304;
    private static final int MSG_RINGER_MODE_CHANGED = 305;
    private static final int MSG_PHONE_STATE_CHANGED = 306;
    private static final int MSG_CLOCK_VISIBILITY_CHANGED = 307;
    private static final int MSG_DEVICE_PROVISIONED = 308;
    private static final int MSG_UNLOCK_KEYGUARD = 309;
    private static final int MSG_LOCK_SCREEN_MISSED_CALL = 310;
    private static final int MSG_LOCK_SCREEN_WALLPAPER_SET = 311;
    private static final int MSG_DM_KEYGUARD_UPDATE = 312;
    private static final int MSG_SIM_DETECTED = 313;
    private static final int MSG_DELAY_SIM_DETECTED = 314;
    private static final int MSG_CONFIGURATION_CHANGED = 315;
    private static final int MSG_SIMINFO_CHANGED = 316;
    private static final int MSG_KEYGUARD_RESET_DISMISS = 317;
    private static final int MSG_KEYGUARD_UPDATE_LAYOUT = 318;
    private static final int MSG_KEYGUARD_SIM_NAME_UPDATE = 319;
    private static final int MSG_MODEM_RESET = 320;
    private static final int MSG_PRE_3G_SWITCH = 321;
    private static final int MSG_BOOTUP_MODE_PICK = 322;
    private static final int MSG_GPRS_TYPE_SELECT = 323;
    private static final int MSG_SYSTEM_STATE = 324;    // ALPS00264727

    //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    private static final int MSG_DOWNLOAD_CALIBRATION_DATA_UPDATE = 325;
    //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not

    // ALPS00264727 begin
    private static final int SYSTEM_STATE_SHUTDOWN = 0;
    private static final int SYSTEM_STATE_BOOTUP = 1;
    // ALPS00264727 end

    //DM Begin
    private static boolean KEYGUARD_DM_LOCKED = false;//Default is false, now for test
    // OMADM TEST INTENT
    public static final String OMADM_LAWMO_LOCK = "com.mediatek.dm.LAWMO_LOCK";
    public static final String OMADM_LAWMO_UNLOCK = "com.mediatek.dm.LAWMO_UNLOCK";
    //DM end

    //SlideLockScreen Wallpaper set
    private static final String ACTION_WALLPAPER_SET = "com.mediatek.lockscreen.action.WALLPAPER_SET";
    private static final String EXTRA_COMPLETE = "com.mediatek.lockscreen.extra.COMPLETE";

    //Add for service state
    boolean mNetSearching = false;
    boolean mNetSearchingGemini = false;

    private int mPINFlag = 0x0;
    
    private static final int NEWSIMINSERTED    = 2;
    private static final int DEFAULTSIMREMOVED = 1;

    //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    boolean mCalibrationData = true;
    //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not

    private LockPatternUtils mLockPatternUtils;
    
    //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
    private IPowSavChargeTime mBinder;
	private ServiceConnection mConn = new ServiceConnection(){
			
			@Override
			public void onServiceDisconnected(ComponentName name) {
				
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mBinder = IPowSavChargeTime.Stub.asInterface(service);
			}
	};
	private boolean isPowSavServiceBoot = false;
    //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
    
    
    /**
     * When we receive a
     * {@link com.android.internal.telephony.TelephonyIntents#ACTION_SIM_STATE_CHANGED} broadcast,
     * and then pass a result via our handler to {@link KeyguardUpdateMonitor#handleSimStateChange},
     * we need a single object to pass to the handler.  This class helps decode
     * the intent and provide a {@link SimCard.State} result.
     */

    /**
     * Either a lock screen (an informational keyguard screen), or an unlock
     * screen (a means for unlocking the device) is shown at any given time.
     */

    private class SIMStatus{
        private int _total = 0;
        private int _dialogType = 0;

        public SIMStatus(int dialogType, int total){
            _dialogType = dialogType;
            _total = total;
        }

        public int getSimType(){
            return _dialogType;
        }

        public int getSIMCardCount(){
            return _total;
        }
    }

    private static class SimArgs {

        public final IccCard.State simState;
        int simId = 0;

        SimArgs(IccCard.State state) {
            simState = state;
        }

        //need add patch for Android4.0.4
        SimArgs(IccCard.State state, int SimId){
           simState = state;
           simId = SimId;
        }

        static SimArgs fromIntent(Intent intent) {
            IccCard.State state;
            int SimId = 0;
            if (!TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())) {
                throw new IllegalArgumentException("only handles intent ACTION_SIM_STATE_CHANGED");
            }
            String stateExtra = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
            if(FeatureOption.MTK_GEMINI_SUPPORT) {
                SimId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
            }
            if (IccCard.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)) {
                final String absentReason = intent
                    .getStringExtra(IccCard.INTENT_KEY_LOCKED_REASON);

                if (IccCard.INTENT_VALUE_ABSENT_ON_PERM_DISABLED.equals(
                        absentReason)) {
                    state = IccCard.State.PERM_DISABLED;
                } else {
                    state = IccCard.State.ABSENT;
                }
            } else if (IccCard.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
                state = IccCard.State.READY;
            } else if (IccCard.INTENT_VALUE_ICC_LOCKED.equals(stateExtra)) {
                final String lockedReason = intent
                        .getStringExtra(IccCard.INTENT_KEY_LOCKED_REASON);
                if (IccCard.INTENT_VALUE_LOCKED_ON_PIN.equals(lockedReason)) {
                    state = IccCard.State.PIN_REQUIRED;
                } else if (IccCard.INTENT_VALUE_LOCKED_ON_PUK.equals(lockedReason)) {
                    state = IccCard.State.PUK_REQUIRED;
                } else {
                    state = IccCard.State.UNKNOWN;
                }
            } else if (IccCard.INTENT_VALUE_LOCKED_NETWORK.equals(stateExtra)) {
                state = IccCard.State.NETWORK_LOCKED;
            } else if (IccCard.INTENT_VALUE_ICC_NOT_READY.equals(stateExtra)) {
                state = IccCard.State.NOT_READY;
            } else {
                state = IccCard.State.UNKNOWN;
            }
            return new SimArgs(state, SimId);
        }

        public String toString() {
            return simState.toString();
        }
    }

    private static class BatteryStatus {
        public final int status;
        public final int level;
        public final int plugged;
        public final int health;
        public final String chargeTime; //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        public BatteryStatus(int status, int level, int plugged, int health) {
            this.status = status;
            this.level = level;
            this.plugged = plugged;
            this.health = health;
            this.chargeTime = ""; //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        }
        //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
		public BatteryStatus(int status, int level, int plugged, int health, String chargeTime){
			this.status = status;
			this.level = level;
			this.plugged = plugged;
			this.health = health;
			this.chargeTime = chargeTime;
		}
		//End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
    }

    public KeyguardUpdateMonitor(Context context) {
        mContext = context;

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_TIME_UPDATE:
                        handleTimeUpdate();
                        break;
                    case MSG_BATTERY_UPDATE:
                        handleBatteryUpdate((BatteryStatus) msg.obj);
                        break;
                    case MSG_CARRIER_INFO_UPDATE:
                        handleCarrierInfoUpdate();
                        break;
                    case MSG_SIM_STATE_CHANGE:
                        handleSimStateChange((SimArgs) msg.obj);
                        break;
                    case MSG_CONFIGURATION_CHANGED:
                        updateResources();
                        break;
                    case MSG_RINGER_MODE_CHANGED:
                        handleRingerModeChange(msg.arg1);
                        break;
                    case MSG_PHONE_STATE_CHANGED:
                        handlePhoneStateChanged((String)msg.obj);
                        break;
                    case MSG_CLOCK_VISIBILITY_CHANGED:
                        handleClockVisibilityChanged();
                        break;
                    case MSG_DEVICE_PROVISIONED:
                        handleDeviceProvisioned();
                        break;
                    case MSG_BOOTUP_MODE_PICK:
                        /* bootup mode picker */
                        handleBootupModePick();
                        break;

                    case MSG_SIMINFO_CHANGED:
                        handleSIMInfoChanged(msg.arg1);
                        break;
                    case MSG_UNLOCK_KEYGUARD:
                        /* unlock keyguard */
                        if (DEBUG) Xlog.d(TAG, "handleUNLOCK_KEYGUARD");
                        for (int i = 0; i < mInfoCallbacks.size(); i++) {
                           mInfoCallbacks.get(i).onUnlockKeyguard();
                        }
                        break;
                        
                    case MSG_GPRS_TYPE_SELECT:
                        /* GPRS type picker */
                        Xlog.d(TAG, "msg.arg1 = " + msg.arg1);
                        Xlog.d(TAG, "msg.arg2 = " + msg.arg2);

                        if(msg.arg1 == Phone.GEMINI_SIM_1) {
                            handleGprsTypePickSim1();
                        }else{
                            handleGprsTypePickSim2();
                        }
                        break;

                    case MSG_KEYGUARD_RESET_DISMISS:
                        mPINFlag = 0x0;
                        break;

                    case MSG_LOCK_SCREEN_MISSED_CALL:
                        handleMissedCall(msg.arg1);
                        break;
                    case MSG_LOCK_SCREEN_WALLPAPER_SET:
                        handleWallpaperSet();
                        break;

                    case MSG_DM_KEYGUARD_UPDATE:
                        handleDMKeyguardUpdate();
                        break;

                    case MSG_SIM_DETECTED:
                        handleSIMCardChanged(msg.arg1, msg.arg2);
                        break;

                    case MSG_DELAY_SIM_DETECTED:
                        handleDelaySIMCardChanged();
                        break;
                    /** add this for IPO workaround**/
                    case MSG_KEYGUARD_UPDATE_LAYOUT:
                        Xlog.i(TAG, "MSG_KEYGUARD_UPDATE_LAYOUT, msg.arg1="+msg.arg1);
                        handleLockScreenUpdateLayout(msg.arg1);
                        break;

                     /** add this for sim name workaround for framework**/
                    case MSG_KEYGUARD_SIM_NAME_UPDATE:
                        Xlog.i(TAG, "MSG_KEYGUARD_SIM_NAME_UPDATE, msg.arg1="+msg.arg1);
                        handleSIMNameUpdate(msg.arg1);
                        break;

                    case MSG_MODEM_RESET:
                        Xlog.i(TAG, "MSG_MODEM_RESET, msg.arg1="+msg.arg1);
                        handleRadioStateChanged(msg.arg1);
                        break;

                    case MSG_PRE_3G_SWITCH:
                        handle3GSwitchEvent();
                        break;
                    
                    /** add this for ALPS00264727 **/
                    case MSG_SYSTEM_STATE:
                        Xlog.i(TAG, "MSG_SYSTEM_STATE, msg.arg1=" + msg.arg1);
                        handleSystemStateChanged(msg.arg1);
                        break;

                    //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
                    case MSG_DOWNLOAD_CALIBRATION_DATA_UPDATE:
                        handleDownloadCalibrationDataUpdate();
                        break;
                    //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
                }
            }
        };

        DM_Check_Locked();
        mLockPatternUtils = new LockPatternUtils(mContext);
        if (isGMSRunning()) {
            Xlog.i(TAG, "first reboot, GMS is running");
            mLockPatternUtils.setLockScreenDisabled(true);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_DELAY_SIM_DETECTED));
        }
        mPhoneStateListener = new PhoneStateListener(){// for SIM1 and single
           @Override
           public void onServiceStateChanged(ServiceState state) {
                if (state != null) {
                   int regState = state.getRegState();
                   if (true==mNetSearching && (regState != ServiceState.REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING)) {
                       Xlog.d(TAG, "PhoneStateListener, sim1 searching finished");
                       mNetSearching = false;
                       for (int i=0; i< mInfoCallbacks.size(); i++){ 
                           mInfoCallbacks.get(i).onSearchNetworkUpdate(Phone.GEMINI_SIM_1, false);
                       }
                   }
                   Xlog.d(TAG, "PhoneStateListener, sim1 on service state changed before.state="+regState);
                   if (regState == ServiceState.REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING) {
                        mNetSearching = true;
                        for (int i=0; i< mInfoCallbacks.size(); i++){
                           mInfoCallbacks.get(i).onSearchNetworkUpdate(Phone.GEMINI_SIM_1, true); 
                        }
                   }
                }
            }
        };
        
        mPhoneStateListenerGemini = new PhoneStateListener() {//only for SIM2
           @Override
           public void onServiceStateChanged(ServiceState state) {
              if (state != null) {
                  int regState = state.getRegState(); 
                  if (true==mNetSearchingGemini && (regState != ServiceState.REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING)){
                      Xlog.d(TAG, "PhoneStateListener, sim2 searching finished");
                      mNetSearchingGemini = false;
                      for (int i=0; i< mInfoCallbacks.size(); i++){
                          mInfoCallbacks.get(i).onSearchNetworkUpdate(Phone.GEMINI_SIM_2, false);
                      }
                  }
                  Xlog.d(TAG, "PhoneStateListener, sim2 on service state changed before.state="+regState);
                  if (regState == ServiceState.REGISTRATION_STATE_NOT_REGISTERED_AND_SEARCHING){
                      mNetSearchingGemini = true;
                      for (int i=0; i< mInfoCallbacks.size(); i++){
                          mInfoCallbacks.get(i).onSearchNetworkUpdate(Phone.GEMINI_SIM_2, true);
                      }
                  }
               }
           }
        };

       if (FeatureOption.MTK_GEMINI_SUPPORT) {
           ((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE))
                    .listenGemini(mPhoneStateListener,PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_1);   
           ((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE))
                    .listenGemini(mPhoneStateListenerGemini,PhoneStateListener.LISTEN_SERVICE_STATE, Phone.GEMINI_SIM_2);
       } else {
           ((TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE))
                .listen(mPhoneStateListener,PhoneStateListener.LISTEN_SERVICE_STATE);
       }


        mKeyguardBypassEnabled = context.getResources().getBoolean(
                com.android.internal.R.bool.config_bypass_keyguard_if_slider_open);

        mDeviceProvisioned = Settings.Secure.getInt(
                mContext.getContentResolver(), Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
		//M{
		mDeviceProvisioned = true;
		//}M
        // Since device can't be un-provisioned, we only need to register a content observer
        // to update mDeviceProvisioned when we are...
        if (!mDeviceProvisioned) {
            mContentObserver = new ContentObserver(mHandler) {
                @Override
                public void onChange(boolean selfChange) {
                    super.onChange(selfChange);
                    mDeviceProvisioned = Settings.Secure.getInt(mContext.getContentResolver(),
                        Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
                    if (mDeviceProvisioned && mContentObserver != null) {
                        // We don't need the observer anymore...
                        mContext.getContentResolver().unregisterContentObserver(mContentObserver);
                        mContentObserver = null;
                    }
                    if (DEBUG) Xlog.d(TAG, "DEVICE_PROVISIONED state = " + mDeviceProvisioned);
                }
            };

            mContext.getContentResolver().registerContentObserver(
                    Settings.Secure.getUriFor(Settings.Secure.DEVICE_PROVISIONED),
                    false, mContentObserver);

            // prevent a race condition between where we check the flag and where we register the
            // observer by grabbing the value once again...
            boolean provisioned = Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.DEVICE_PROVISIONED, 0) != 0;
            if (provisioned != mDeviceProvisioned) {
                mDeviceProvisioned = provisioned;
                if (mDeviceProvisioned) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_DEVICE_PROVISIONED));
                }
            }
        }

        // take a guess to start
        //mSimState = IccCard.State.READY;
        //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        //mBatteryStatus = new BatteryStatus(BATTERY_STATUS_UNKNOWN, 100, 0, 0);
        Log.d("tinno_powersaving", "updateMonitor is created");
        mBatteryStatus = new BatteryStatus(BATTERY_STATUS_UNKNOWN, 100, 0, 0, "");
        Intent intent = new Intent("powersaving.action.getchargtime");
		mContext.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
		//End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        mDevicePluggedIn = true;
        
		
		
        mTelephonyPlmn = getDefaultPlmn();
        if(FeatureOption.MTK_GEMINI_SUPPORT == true) {
            mTelephonyPlmnGemini = getDefaultPlmn();
        }

        // setup receiver
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        //Gemini Enhancement Begin
        filter.addAction(TelephonyIntents.ACTION_NEW_SIM_DETECTED);
        filter.addAction(TelephonyIntents.ACTION_DEFAULT_SIM_REMOVED);
        filter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        filter.addAction(TelephonyIntents.ACTION_SIM_INSERTED_STATUS);
        filter.addAction("android.intent.action.SIM_NAME_UPDATE");
        filter.addAction(TelephonyIntents.ACTION_RADIO_OFF);
        filter.addAction(GeminiPhone.EVENT_3G_SWITCH_START_MD_RESET);
        //Gemini Enhancement End
        filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        filter.addAction(SPN_STRINGS_UPDATED_ACTION);
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
        filter.addAction(ACTION_DUAL_SIM_MODE_SELECT);
        filter.addAction(ACTION_GPRS_CONNECTION_TYPE_SELECT);
        filter.addAction(ACTION_UNLOCK_KEYGUARD);
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        filter.addAction("com.android.phone.NotificationMgr.MissedCall_intent");
        filter.addAction(ACTION_WALLPAPER_SET);
        //for shutdow alarm
        filter.addAction("android.intent.action.normal.boot");
        //end for shutdown alarm
        //DM Begin
	filter.addAction(OMADM_LAWMO_LOCK);
	filter.addAction(OMADM_LAWMO_UNLOCK);
	//DM end
        // ALPS00264727 begin
        filter.addAction(Intent.ACTION_SHUTDOWN);
        filter.addAction("android.intent.action.ACTION_PREBOOT_IPO");
        // ALPS00264727 end

	 //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
	 filter.addAction(TelephonyIntents.ACTION_DOWNLOAD_CALIBRATION_DATA);
	 //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
		
		filter.addAction("powersaving.action.isbootup"); //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
		
        context.registerReceiver(new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (DEBUG) Xlog.d(TAG, "received broadcast " + action);

                if (Intent.ACTION_TIME_TICK.equals(action)
                        || Intent.ACTION_TIME_CHANGED.equals(action)
                        || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_TIME_UPDATE));
                } else if (SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                    if(intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1) == Phone.GEMINI_SIM_1){
                        mTelephonyPlmn = getTelephonyPlmnFrom(intent);
                        mTelephonySpn = getTelephonySpnFrom(intent);
                        Log.d(TAG, "SPN_STRINGS_UPDATED_ACTION, update sim1, plmn="+mTelephonyPlmn
                            +", spn="+mTelephonySpn);
                    }else{
                        mTelephonyPlmnGemini = getTelephonyPlmnFrom(intent);
                        mTelephonySpnGemini = getTelephonySpnFrom(intent);
                        Log.d(TAG, "SPN_STRINGS_UPDATED_ACTION, update sim2, plmn="+mTelephonyPlmnGemini
                            +", spn="+mTelephonySpnGemini);
                    }
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_CARRIER_INFO_UPDATE));
                } else if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    final int status = intent.getIntExtra(EXTRA_STATUS, BATTERY_STATUS_UNKNOWN);
                    final int plugged = intent.getIntExtra(EXTRA_PLUGGED, 0);
                    final int level = intent.getIntExtra(EXTRA_LEVEL, 0);
                    final int health = intent.getIntExtra(EXTRA_HEALTH, BATTERY_HEALTH_UNKNOWN);
                    Log.i(TAG, "ACTION_BATTERY_CHANGED, status="+status+",plugged="+plugged
                        +", level="+level+", health="+health);
                      
                     //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                     final Message msg;
                     if (isPowSavServiceBoot){
                     	 String chargetime = " " ;
		                 try{
		                 	Log.d("tinno_powersaving", "action battery changed");
		                  	String time = mBinder.getChargeTime(intent);
		                  	chargetime += time;
		                  }catch(Exception e){
		                  	Log.d("tinno_powersaving", e.getMessage());
		                  }
                        
		               msg = mHandler.obtainMessage(
		                		MSG_BATTERY_UPDATE, new BatteryStatus(status, level, plugged, health, chargetime));
                     }else {
                     	msg = mHandler.obtainMessage(
                            MSG_BATTERY_UPDATE, new BatteryStatus(status, level, plugged, health));
                     }
                    
                   //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                   
                    mHandler.sendMessage(msg);
                }else if(action.equals("powersaving.action.isbootup")){  //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                	isPowSavServiceBoot = true;
                }                
                else if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                    int simId = 0;
                    String stateExtra = intent.getStringExtra(IccCard.INTENT_KEY_ICC_STATE);
                    if(FeatureOption.MTK_GEMINI_SUPPORT == true) {
                       simId = intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1);
                    }
                    Log.i(TAG, "ACTION_SIM_STATE_CHANGED, stateExtra="+stateExtra+",simId="+simId);
                    if (IccCard.INTENT_VALUE_ICC_IMSI != stateExtra 
                        && (IccCard.INTENT_VALUE_ICC_LOADED != stateExtra)){
                          mHandler.sendMessage(mHandler.obtainMessage(
                                               MSG_SIM_STATE_CHANGE,
                                               SimArgs.fromIntent(intent)));
                    }
                }
                /*Gemini Enhancement begin*/
                else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)){
                     Xlog.i(TAG, "received the IPO shutdown message");
                     mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_KEYGUARD_RESET_DISMISS));
                     
                     // ALPS00264727: post a message
                     Message m = mHandler.obtainMessage(MSG_SYSTEM_STATE);
                     m.arg1 = SYSTEM_STATE_SHUTDOWN;
                     mHandler.sendMessage(m);
                }
                else if (TelephonyIntents.ACTION_RADIO_OFF.equals(action)){ 
                   int slotId = intent.getIntExtra("slotId", 0);
                   Xlog.i(TAG, "received ACTION_RADIO_OFF message, slotId="+slotId);
                   mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_MODEM_RESET,slotId, 0));
                }else if (GeminiPhone.EVENT_3G_SWITCH_START_MD_RESET.equals(action)){
                   Xlog.i(TAG, "received EVENT_3G_SWITCH_START_MD_RESET message");
                   mHandler.sendMessage(mHandler.obtainMessage(MSG_PRE_3G_SWITCH));
                }else if (TelephonyIntents.ACTION_SIM_INSERTED_STATUS.equals(action)){
                    int slotId = intent.getIntExtra("slotId", 0); 
                    Xlog.i(TAG, "SIM_INSERTED_STATUS, slotId="+slotId);
                    mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_KEYGUARD_UPDATE_LAYOUT, slotId, 0));
                }else if ("android.intent.action.SIM_NAME_UPDATE".equals(action)){
                    int slotId = intent.getIntExtra("slotId", 0);
                    Xlog.i(TAG, "SIM_NAME_UPDATE, slotId="+slotId);
                    mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_KEYGUARD_SIM_NAME_UPDATE, slotId, 0));
                }
                else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                     mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_CONFIGURATION_CHANGED));
                }
                else if (TelephonyIntents.ACTION_NEW_SIM_DETECTED.equals(action)){
                    int total = intent.getIntExtra("newSIMSlot", 0);
                    if (isGMSRunning()){
                        mSimStatus = new SIMStatus(NEWSIMINSERTED, total);
                        shouldPopup = true;
                    }else {//we can go the normal process
                        boolean bootReason = SystemProperties.get("sys.boot.reason").equals("1");
                        Xlog.i(TAG, "new_sim_detected, total="+total+", bootReason="+bootReason);
                        if (bootReason){
                            //save the information, and popup until alarm dialog dismiss
                            mSimStatus = new SIMStatus(NEWSIMINSERTED, total);
                            shouldPopup = true;
                        }else {
                            mHandler.sendMessage(mHandler.obtainMessage(
                                                 MSG_SIM_DETECTED,
                                                 NEWSIMINSERTED, total));
                        }
                    }
                } else if (TelephonyIntents.ACTION_DEFAULT_SIM_REMOVED.equals(action)){
                    int total = intent.getIntExtra("simCount", 0);
                    if (isGMSRunning()){
                        shouldPopup = true;
                        mSimStatus = new SIMStatus(DEFAULTSIMREMOVED, total);
                    }else {//we can go the normal process
                        boolean bootReason = SystemProperties.get("sys.boot.reason").equals("1");
                        Xlog.i(TAG, "default_sim_removed, total="+total+", bootReason="+bootReason);
                        if (bootReason){
                            //mSIMDialog the information, and popup until alarm dialog dismiss
                            mSimStatus = new SIMStatus(DEFAULTSIMREMOVED, total);
                            shouldPopup = true;
                        }else {
                            mHandler.sendMessage(mHandler.obtainMessage(
                                                 MSG_SIM_DETECTED,
                                                 DEFAULTSIMREMOVED, total));
                        }
                    }
                } else if ("android.intent.action.normal.boot".equals(action)){
                    Log.i(TAG, "received normal boot, shouldPop="+shouldPopup);
                    if (null != mSimStatus && shouldPopup){
                        shouldPopup = false;
                        mHandler.sendMessage(mHandler.obtainMessage(
                                             MSG_SIM_DETECTED,
                                             mSimStatus.getSimType(), mSimStatus.getSIMCardCount())); 
                    }
                }else if (TelephonyIntents.ACTION_SIM_INFO_UPDATE.equals(action)){
                    int slotId = intent.getIntExtra("slotId", 0); 
                    Xlog.i(TAG, "sim info update, slotId="+slotId);
                    mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_SIMINFO_CHANGED, slotId, 0));
                } else if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(action)) {
                    if (DEBUG) Xlog.i(TAG, "RINGER_MODE_CHANGED_ACTION received");
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_RINGER_MODE_CHANGED,
                            intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1), 0));
                } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                    String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_PHONE_STATE_CHANGED, state));
                } else if (ACTION_DUAL_SIM_MODE_SELECT.equals(action)) {
                    Xlog.i(TAG, "ACTION_DUAL_SIM_MODE_SELECT, received");
                    mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_BOOTUP_MODE_PICK));
                } else if (ACTION_UNLOCK_KEYGUARD.equals(action)) {
                    Xlog.i(TAG, "ACTION_UNLOCK_KEYGUARD, received");
                    mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_UNLOCK_KEYGUARD));
                } else if (ACTION_GPRS_CONNECTION_TYPE_SELECT.equals(action)) {
                    Xlog.d(TAG, "ACTION_GPRS_CONNECTION_TYPE_SELECT, simId =  " + intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, -1));
                    mHandler.sendMessage(mHandler.obtainMessage(
                            MSG_GPRS_TYPE_SELECT, intent.getIntExtra(Phone.GEMINI_SIM_ID_KEY, Phone.GEMINI_SIM_1), 0));

                }
                else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                    Xlog.d(TAG, "ACTION_AIRPLANE_MODE_CHANGED, received");
                    if (mDialog != null && intent.getExtras().getBoolean("state")) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
                else if ("com.android.phone.NotificationMgr.MissedCall_intent".equals(action)) {
                   mMissedCall = intent.getExtras().getInt("MissedCallNumber"); 
                   Xlog.i(TAG, "MissedCall_intent received, mMissedCall="+mMissedCall);
                   mHandler.sendMessage(mHandler.obtainMessage(MSG_LOCK_SCREEN_MISSED_CALL,mMissedCall, 0));
                }
                else if (ACTION_WALLPAPER_SET.equals(action)) {
                    mWallpaperSetComplete = intent.getBooleanExtra(EXTRA_COMPLETE, false);
                    Xlog.d(TAG, "WALLPAPER_SET:" + mWallpaperSetComplete);
                    if(mWallpaperSetComplete){
                        mHandler.sendMessage(mHandler.obtainMessage(MSG_LOCK_SCREEN_WALLPAPER_SET, 0));
                    }
                }
                //DM Begin
                else if(OMADM_LAWMO_LOCK.equals(action)){
                    KEYGUARD_DM_LOCKED = true;
                    Log.i(TAG, "OMADM_LAWMO_LOCK received, KEYGUARD_DM_LOCKED="+KEYGUARD_DM_LOCKED);
                    mHandler.sendMessage((mHandler.obtainMessage(MSG_DM_KEYGUARD_UPDATE)));
                }
                else if(OMADM_LAWMO_UNLOCK.equals(action)){
                    KEYGUARD_DM_LOCKED = false;
                    Log.i(TAG, "OMADM_LAWMO_UNLOCK received, KEYGUARD_DM_LOCKED="+KEYGUARD_DM_LOCKED);
                    mHandler.sendMessage((mHandler.obtainMessage(MSG_DM_KEYGUARD_UPDATE)));
                }
                //DM end
                // ALPS00264727 begin
                else if (Intent.ACTION_SHUTDOWN.equals(action)) {
                     Message m = mHandler.obtainMessage(MSG_SYSTEM_STATE);
                     m.arg1 = SYSTEM_STATE_SHUTDOWN;
                     mHandler.sendMessage(m);
                } else if ("android.intent.action.ACTION_PREBOOT_IPO".equals(action)) {
                     // ALPS00264727: post a message
                     Message m = mHandler.obtainMessage(MSG_SYSTEM_STATE);
                     m.arg1 = SYSTEM_STATE_BOOTUP;
                     mHandler.sendMessage(m);
                }
                // ALPS00264727 end
                //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
                else if(TelephonyIntents.ACTION_DOWNLOAD_CALIBRATION_DATA.equals(action)){
                    mCalibrationData = intent.getBooleanExtra(EXTRA_CALIBRATION_DATA, true);
                    Xlog.i(TAG, "mCalibrationData = "+mCalibrationData);
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_DOWNLOAD_CALIBRATION_DATA_UPDATE));
                }
                //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
            }
        }, filter);
        
        mWallpaperUpdate = true;
    }

    /**
     * Reload some of our resources when the configuration changes.
     *
     * We don't reload everything when the configuration changes -- we probably
     * should, but getting that smooth is tough.  Someday we'll fix that.  In the
     * meantime, just update the things that we know change.
     */

    boolean isGMSRunning(){
        boolean running = false;
        boolean isExist = true;
        mPm = mContext.getPackageManager();
        mComponentName = new ComponentName(
            "com.google.android.setupwizard", "com.google.android.setupwizard.SetupWizardActivity");

        try {
            mPm .getInstallerPackageName("com.google.android.setupwizard");
        } catch (IllegalArgumentException e) {
           isExist = false;
        }
        if (isExist && (PackageManager.COMPONENT_ENABLED_STATE_ENABLED == 
            mPm.getComponentEnabledSetting(mComponentName)
            || PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ==
            mPm.getComponentEnabledSetting(mComponentName))){
            running = true;
        }
        Xlog.i(TAG, "isGMSRunning, isGMSExist = "+isExist+", running = "+running);
        return running;
    }
    
    void updateResources() {
        if (null != mSIMCardDialog) {
            if (mSIMCardDialog.isShowing() && mSIMRemoved) {//default sim removed
                Xlog.i(TAG,"updateResources, default sim removed");
                mSIMCardDialog.setTitle(com.mediatek.internal.R.string.default_sim_removed);
                Button nagbtn = mSIMCardDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (null != nagbtn){
                    nagbtn.setText(com.mediatek.internal.R.string.keyguard_close);
                }
                Button posbtn = mSIMCardDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (null != posbtn){
                    posbtn.setText(com.mediatek.internal.R.string.change_settings);
                }
                String msg = mContext.getResources().getString(com.mediatek.internal.R.string.change_setting_for_removesim);
                InitView(mPromptView, msg);
            }else if (mSIMCardDialog.isShowing() && !mSIMRemoved){//new sim inserted
                Xlog.i(TAG,"updateResources, new sim inserted");
                mSIMCardDialog.setTitle(com.mediatek.internal.R.string.new_sim_inserted);
                Button nagbtn = mSIMCardDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                if (null != nagbtn) {
                    nagbtn.setText(com.mediatek.internal.R.string.keyguard_close);
                }
                Button posbtn = mSIMCardDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                if (null != posbtn) {
                    posbtn.setText(com.mediatek.internal.R.string.change_settings);
                }
                String msg = null;
                if (3 == mCardTotal) {
                    msg = mContext.getResources().getString(com.mediatek.internal.R.string.change_setting_for_twonewsim);
                }else{
                    msg = mContext.getResources().getString(com.mediatek.internal.R.string.change_setting_for_onenewsim);
                }
                InitView(mPromptView, msg);
            }
        }
    }

    public boolean getSearchingFlag(int simId){
       if(FeatureOption.MTK_GEMINI_SUPPORT == true
           && Phone.GEMINI_SIM_2 == simId){
              return mNetSearchingGemini;
       }else{
          return mNetSearching;
       }
    }

    protected void handleLockScreenUpdateLayout(int slotId){
        if(FeatureOption.MTK_GEMINI_SUPPORT == true) {
           for (int i=0; i< mInfoCallbacks.size(); i++){
              mInfoCallbacks.get(i).onLockScreenUpdate(slotId);
           }
        }
    }

    private void handleSIMNameUpdate(int slotId){
       if(FeatureOption.MTK_GEMINI_SUPPORT == true) {
           for (int i=0; i< mInfoCallbacks.size(); i++){
              mInfoCallbacks.get(i).onLockScreenUpdate(slotId);
           }
           updateResources();//update the new sim detected or default sim removed
       } 
    }

    private void handleSIMInfoChanged(int slotId){//update the siminfo
        if(FeatureOption.MTK_GEMINI_SUPPORT == true) {
           for (int i = 0; i < mInfoCallbacks.size(); i++) {
              mInfoCallbacks.get(i).onSIMInfoChanged(slotId);
            }
        } 
    }

    private void handleDelaySIMCardChanged(){
        new Thread(){
            @Override
            public void run(){
                while (PackageManager.COMPONENT_ENABLED_STATE_ENABLED == 
                        mPm.getComponentEnabledSetting(mComponentName)
                        || PackageManager.COMPONENT_ENABLED_STATE_DEFAULT ==
                        mPm.getComponentEnabledSetting(mComponentName)){
                     try {
                         Thread.sleep(500);
                     } catch (InterruptedException e){
                         //e.printStackTrace();
                     }
                }
                Xlog.i(TAG, "handleDelaySIMCardChanged, exit, shouldPopup="+shouldPopup);
                //restore the flag
                mLockPatternUtils.setLockScreenDisabled(false);
                if (shouldPopup){
                    mHandler.sendMessage(mHandler.obtainMessage(
                       MSG_SIM_DETECTED,
                       mSimStatus.getSimType(), mSimStatus.getSIMCardCount())); 
                }
            }
       }.start();
    }
    
    private void handleSIMCardChanged(int CardState, int CardTotalNumber){
        switch (CardState){
            case NEWSIMINSERTED:{ 
                mSIMRemoved = false;
                mCardTotal = CardTotalNumber;
                Xlog.i(TAG,"SIMCardInserted, CardTotalNumber="+CardTotalNumber+", mSIMRemoved="+mSIMRemoved
                           +", mCardTotal="+mCardTotal);    
                LayoutInflater factory=LayoutInflater.from(mContext);
                //mPromptView =factory.inflate(com.mediatek.internal.R.layout.prompt, null);
                //BEGIN <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
                if(FeatureOption.MTK_VT3G324M_SUPPORT == true) {
                    mPromptView =factory.inflate(com.mediatek.internal.R.layout.prompt, null);
                } else {
                    mPromptView =factory.inflate(com.mediatek.internal.R.layout.prompt_no_videocall, null);
                }
                //END <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
                String msg = null;
                if (2 == CardTotalNumber){
                    msg = mContext.getResources().getString(com.mediatek.internal.R.string.change_setting_for_twonewsim);
                }else{
                    msg = mContext.getResources().getString(com.mediatek.internal.R.string.change_setting_for_onenewsim);
                }
                InitView(mPromptView, msg);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                dialogBuilder.setTitle(com.mediatek.internal.R.string.new_sim_inserted);
                dialogBuilder.setCancelable(false);
                dialogBuilder.setView(mPromptView);
                //if (!mIsCMCC || IsSIMInserted(Phone.GEMINI_SIM_1) && IsSIMInserted(Phone.GEMINI_SIM_2)){
                    dialogBuilder.setPositiveButton(com.mediatek.internal.R.string.change_settings,
                                            new AlertDialog.OnClickListener() {
                                                public void onClick(DialogInterface arg0, int arg1){
                                                    //begin to call setting interface    
                                                    Intent t = new Intent("android.settings.GEMINI_MANAGEMENT");
                                                    t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    mContext.startActivity(t);
                                                }
                                            });
                // }
                dialogBuilder.setNegativeButton(com.mediatek.internal.R.string.keyguard_close, null);
                mSIMCardDialog = dialogBuilder.create();
                mSIMCardDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                mSIMCardDialog.show();
                break;
            }

            case DEFAULTSIMREMOVED:{
                mSIMRemoved = true;
                mCardTotal = CardTotalNumber;
                Xlog.i(TAG,"SIMCardRemoved, CardTotalNumber="+CardTotalNumber+", mSIMRemoved="+mSIMRemoved
                           +", mCardTotal="+mCardTotal); 
                String msg = mContext.getResources().getString(com.mediatek.internal.R.string.change_setting_for_removesim);
                LayoutInflater factory=LayoutInflater.from(mContext);
                mPromptView =factory.inflate(com.mediatek.internal.R.layout.sim_removed_prompt, null);
                ((TextView)mPromptView.findViewById(com.mediatek.internal.R.id.stringprompt)).setText(msg);
                InitView(mPromptView, msg);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                dialogBuilder.setTitle(com.mediatek.internal.R.string.default_sim_removed);
                dialogBuilder.setCancelable(false);
                dialogBuilder.setView(mPromptView);
                //if (!mIsCMCC){
                    dialogBuilder.setPositiveButton(com.mediatek.internal.R.string.change_settings,
                                            new AlertDialog.OnClickListener() {
                                                public void onClick(DialogInterface arg0, int arg1){
                                                    //begin to call setting interface    
                                                    Intent t = new Intent("android.settings.GEMINI_MANAGEMENT");
                                                    t.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    mContext.startActivity(t);
                                                }
                                            });
                // }
                dialogBuilder.setNegativeButton(com.mediatek.internal.R.string.keyguard_close, null);
                mSIMCardDialog = dialogBuilder.create();
                mSIMCardDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                mSIMCardDialog.show();
                break;
            }

            default:
                throw new IllegalStateException("Unknown SIMCard Changed:" + CardState);
        }
    }



    protected void handleDeviceProvisioned() {
        for (int i = 0; i < mDeviceInfoCallbacks.size(); i++) {
            mDeviceInfoCallbacks.get(i).onDeviceProvisioned();
        }
        if (mContentObserver != null) {
            // We don't need the observer anymore...
            mContext.getContentResolver().unregisterContentObserver(mContentObserver);
            mContentObserver = null;
        }
    }

    protected void handlePhoneStateChanged(String newState) {
        if (DEBUG) Xlog.d(TAG, "handlePhoneStateChanged(" + newState + ")");
        if (mGPRSDialog1 != null) {
            mGPRSDialog1.dismiss();
    	}
        if (mGPRSDialog2 != null) {
            mGPRSDialog2.dismiss();
    	}
        if (mSIMCardDialog != null) {
            mSIMCardDialog.dismiss();
        }
        if (TelephonyManager.EXTRA_STATE_IDLE.equals(newState)) {
            mPhoneState = TelephonyManager.CALL_STATE_IDLE;
        } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(newState)) {
            mPhoneState = TelephonyManager.CALL_STATE_OFFHOOK;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(newState)) {
            mPhoneState = TelephonyManager.CALL_STATE_RINGING;
        }
        for (int i = 0; i < mPhoneCallbacks.size(); i++) {
            mPhoneCallbacks.get(i).onPhoneStateChanged(mPhoneState);
        }
    }

    protected void handleRingerModeChange(int mode) {
        if (DEBUG) Xlog.d(TAG, "handleRingerModeChange(" + mode + ")");
        mRingMode = mode;
        for (int i = 0; i < mInfoCallbacks.size(); i++) {
            mInfoCallbacks.get(i).onRingerModeChanged(mode);
        }
    }

    /**
     * Handle {@link #MSG_TIME_UPDATE}
     */
    private void handleTimeUpdate() {
        if (DEBUG) Xlog.d(TAG, "handleTimeUpdate");
        for (int i = 0; i < mInfoCallbacks.size(); i++) {
            mInfoCallbacks.get(i).onTimeChanged();
        }
    }

    /**
     * Handle {@link #MSG_BATTERY_UPDATE}
     */
    private void handleBatteryUpdate(BatteryStatus batteryStatus) {
        if (DEBUG) Log.d(TAG, "handleBatteryUpdate");
        final boolean batteryUpdateInteresting =
                isBatteryUpdateInteresting(mBatteryStatus, batteryStatus);
        mBatteryStatus = batteryStatus;
        if (batteryUpdateInteresting) {
            for (int i = 0; i < mInfoCallbacks.size(); i++) {
                // TODO: pass BatteryStatus object to onRefreshBatteryInfo() instead...
                //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
                /*mInfoCallbacks.get(i).onRefreshBatteryInfo(
                    shouldShowBatteryInfo(),isPluggedIn(batteryStatus), batteryStatus.level);*/
                 mInfoCallbacks.get(i).onRefreshBatteryInfo( 
                    shouldShowBatteryInfo(),isPluggedIn(batteryStatus), batteryStatus.level, batteryStatus.chargeTime);
                //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
            }
        }
    }

    /**
     * Handle {@link #MSG_CARRIER_INFO_UPDATE}
     */
    private void handleCarrierInfoUpdate() {
        if (DEBUG) Xlog.d(TAG, "handleCarrierInfoUpdate: plmn = " + mTelephonyPlmn
            + ", spn = " + mTelephonySpn);

        for (int i = 0; i < mInfoCallbacks.size(); i++) {
            mInfoCallbacks.get(i).onRefreshCarrierInfo(mTelephonyPlmn, mTelephonySpn);
        }
    }

    /**
     * Handle {@link #MSG_SIM_STATE_CHANGE}
     */
    private void handleSimStateChange(SimArgs simArgs) {
        final IccCard.State state = simArgs.simState;

        if (DEBUG) {
            Log.d(TAG, "handleSimStateChange: intentValue = " + simArgs + " "
                    + "state resolved to " + state.toString());
        }

        IccCard.State tempState;//the previous state
        if(simArgs.simId == Phone.GEMINI_SIM_1) {
            tempState = mSimState;
            mSimLastState = mSimState;
        }else {
            tempState = mSim2State;
            mSim2LastState = mSim2State;
        }

        if (state != IccCard.State.UNKNOWN && state != tempState) {
            if (state == IccCard.State.NOT_READY) {
               // DON'T CHECK tempstate to update the sim state
               //here, we can't return, otherwise, we dismiss PIN when boot up,when we turned radio off and turnedon,
               //we can't show the PIN interface again.
               //return;
            }
            if (simArgs.simId == Phone.GEMINI_SIM_1) {
                mSimState = state;
                Xlog.d(TAG, "handleSimStateChange: mSimState = " + mSimState);
            } else {
                mSim2State = state;
                Xlog.d(TAG, "handleSimStateChange: mSim2State = " + mSim2State);
            }
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                for (int i = 0; i < mSimStateCallbacks.size(); i++) {
                    mSimStateCallbacks.get(i).onSimStateChangedGemini(state, simArgs.simId);
                }
            } else {
                for (int i = 0; i < mSimStateCallbacks.size(); i++) {
                   mSimStateCallbacks.get(i).onSimStateChanged(state);
                }
            }
        }
    }

    private void handle3GSwitchEvent(){
        //only for gemini 
        mPINFlag = 0x0;
    }

    private void handleRadioStateChanged(int slotId){
       Xlog.d(TAG, "handleRadioStateChanged, slotId="+slotId+", mSimState="+mSimState
                        +", mSim2State="+mSim2State);
       setPINDismiss(slotId, true, false);
       setPINDismiss(slotId, false, false);
       if (Phone.GEMINI_SIM_1 == slotId
           && IccCard.State.PIN_REQUIRED == mSimState || IccCard.State.PUK_REQUIRED == mSimState){
           if (null != mRadioStateCallback){
              mRadioStateCallback.onRadioStateChanged(slotId);
           }
       } else if (Phone.GEMINI_SIM_2 == slotId 
           && IccCard.State.PIN_REQUIRED == mSim2State || IccCard.State.PUK_REQUIRED == mSim2State){
           if (null != mRadioStateCallback){
              mRadioStateCallback.onRadioStateChanged(slotId);
           }
       }
    }

    // ALPS00264727: handle system shutdown or bootup intent
    private void handleSystemStateChanged(int state) {
        if (null == mSystemStateCallback) {
            if (DEBUG) Xlog.d(TAG, "mSystemStateCallback is null, skipped!");
            return;
        }

        switch (state) {
            case SYSTEM_STATE_BOOTUP:
                mSystemStateCallback.onSysBootup();
                break;

            case SYSTEM_STATE_SHUTDOWN:
                mSystemStateCallback.onSysShutdown();
                break;

            default:
                if (DEBUG) Xlog.e(TAG, "received unknown system state change event");
                break;
        }
    }

    /**
     * Handle {@link #MSG_BOOTUP_MODE_PICK}
     */
    private void handleBootupModePick() {
        /*if (DEBUG) Xlog.d(TAG, "handleBootupModePick");

        //String[] simname = mContext.getResources().getStringArray(com.mediatek.R.array.bootup_mode);
        String[] simname = new String[2];
        simname[0] = getOptrNameBySlotForCTA(Phone.GEMINI_SIM_1);
        simname[1] = getOptrNameBySlotForCTA(Phone.GEMINI_SIM_2);
        mDialog = new AlertDialog.Builder(mContext)
                .setTitle(com.mediatek.R.string.choose_bootup_mode)
                .setCancelable(false)
                .setItems(simname,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDialog != null)
                        {
                        	mDialog.dismiss();
                        	mDialog = null;
                        }
                        dual_sim_setting = 1; // The dialog item has been selected.
                        Intent intent = new Intent(Intent.ACTION_DUAL_SIM_MODE_CHANGED);
                        switch(which)
                        {
                            //Dual Sim
                            case 0:
                                if (DEBUG) Log.d(TAG, "handleBootupModePick, mode = dual sim");
                                intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, GeminiNetworkSubUtil.MODE_DUAL_SIM);
                                mContext.sendBroadcast(intent);
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING,
                                                        3);

                                break;
                            // Sim 1
                            case 1:
                                if (DEBUG) Xlog.d(TAG, "handleBootupModePick, mode = sim 1");
                                intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, GeminiNetworkSubUtil.MODE_SIM1_ONLY);
                                mContext.sendBroadcast(intent);
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING,
                                                        1);
                                break;
                            // Sim 2
                            case 1:
                                if (DEBUG) Xlog.d(TAG, "handleBootupModePick, mode = sim 2");
                                intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, GeminiNetworkSubUtil.MODE_SIM2_ONLY);
                                mContext.sendBroadcast(intent);
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING,
                                                        2);
                                break;
                            //Dont remind me 
                            case 3:
                                if (DEBUG) Xlog.d(TAG, "handleBootupModePick, mode = don't remind me");
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.BOOT_UP_SELECT_MODE, 0);

                                break;

                            default:
                                if (DEBUG) Xlog.d(TAG, "handleBootupModePick, default, mode = dual sim");
                                intent.putExtra(Intent.EXTRA_DUAL_SIM_MODE, GeminiNetworkSubUtil.MODE_DUAL_SIM);
                                mContext.sendBroadcast(intent);
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.DUAL_SIM_MODE_SETTING,
                                                        3);

                                break;
                        }
                    }
            })
                .create();
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        if (!mContext.getResources().getBoolean(
            com.android.internal.R.bool.config_sf_slowBlur)) {
            mDialog.getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
            WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
       //when dual sim mode setting dialog shows, dual_sim_setting be initialized to 0,
       // which is used to check if user has clicked the items or not, if user has not clicked the items,
       // the airplane mode should be grayed or disabled. see ALPS00131386
        dual_sim_setting = 0;
        mDialog.show();*/
    }


    /**
     * Handle {@link #MSG_GPRS_TYPE_SELECT}
     */
    private void handleGprsTypePickSim1() {
        if (DEBUG) Xlog.d(TAG, "handleGprsTypePickSim1");
        mGPRSDialog1 = new AlertDialog.Builder(mContext)
                .setTitle(com.mediatek.R.string.choose_gprs_mode)
                .setCancelable(false)
                .setItems(com.mediatek.R.array.gprs_mode_1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mGPRSDialog1 != null) {
                        mGPRSDialog1.dismiss();
						mGPRSDialog1 = null;
                        }
                        switch(which)
                        {
                            /* SIM 1*/
                            case 0:
                                if (DEBUG) Xlog.d(TAG, "handleGPRSTypePick, mode = SIM 1");
                                try{
                                    ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                                    if(phone != null){
                                        phone.setGprsConnType(GeminiNetworkSubUtil.CONN_TYPE_WHEN_NEEDED, Phone.GEMINI_SIM_2);
                                        phone.setGprsConnType(GeminiNetworkSubUtil.CONN_TYPE_ALWAYS, Phone.GEMINI_SIM_1);
                                    }else{
                                        Xlog.d(TAG, "Connect to phone service error");
                                        return;
                                    }
                                }catch(RemoteException e){
                                    Xlog.d(TAG, "Connect to phone service error");
                                }

                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING,
                                                        1);

                                break;
                            /* DO NOT CONNECT*/
                            case 1:
                                if (DEBUG) Xlog.d(TAG, "handleGPRSTypePick, mode = NONE");
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING,
                                                        0);

                                break;
                           /* Cancel*/
                           case 2:
									//do nothing
									break;

                            default:
                                if (DEBUG) Xlog.d(TAG, "handleGPRSTypePick, default, mode = NONE");
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING,
                                                        0);
                                break;
                        }
                    }
                })
                .create();


        mGPRSDialog1.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        if (!mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            mGPRSDialog1.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        mGPRSDialog1.show();
    }


    /**
     * Handle {@link #MSG_GPRS_TYPE_SELECT}
     */
    public void handleGprsTypePickSim2() {
        if (DEBUG) Xlog.d(TAG, "handleGprsTypePickSim2");
        mGPRSDialog2 = new AlertDialog.Builder(mContext)
                .setTitle(com.mediatek.R.string.choose_gprs_mode)
                .setCancelable(false)
                .setItems(com.mediatek.R.array.gprs_mode_2,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(mGPRSDialog2 != null) {
                        mGPRSDialog2.dismiss();
                        mGPRSDialog2 = null;
                        }
                        switch(which)
                        {
                            /* SIM 2*/
                            case 0:
                                if (DEBUG) Xlog.d(TAG, "handleGPRSTypePick, mode = SIM 2");
                                try{
                                    ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                                    if(phone != null){
                                        phone.setGprsConnType(GeminiNetworkSubUtil.CONN_TYPE_WHEN_NEEDED, Phone.GEMINI_SIM_1);
                                        phone.setGprsConnType(GeminiNetworkSubUtil.CONN_TYPE_ALWAYS, Phone.GEMINI_SIM_2);
                                    }else{
                                        Xlog.d(TAG, "Connect to phone service error");
                                        return;
                                    }
                                }catch(RemoteException e){
                                    Xlog.d(TAG, "Connect to phone service error");
                                }
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING,
                                                        2);

                                break;
                            /* DO NOT CONNECT*/
                            case 1:
                                if (DEBUG) Xlog.d(TAG, "handleGPRSTypePick, mode = NONE");
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING,
                                                        0);

                                break;
							/* Cancel*/
                            case 2:
                                //do nothing
                                break;

                            default:
                                if (DEBUG) Xlog.d(TAG, "handleGPRSTypePick, default, mode = NONE");
                                // Change the system setting
                                Settings.System.putInt(mContext.getContentResolver(), Settings.System.GPRS_CONNECTION_SETTING,
                                                        0);

                                break;
                         }
                     }
                })
                .create();


        mGPRSDialog2.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        if (!mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_sf_slowBlur)) {
            mGPRSDialog2.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        }
        mGPRSDialog2.show();
    }

    public void handleMissedCall(int missedCall){
       for (int i = 0; i < mInfoCallbacks.size(); i++){
           mInfoCallbacks.get(i).onMissedCallChanged(missedCall);
       }
    }

    public void handleWallpaperSet(){
    	mWallpaperUpdate = true;
        for (int i = 0; i < mInfoCallbacks.size(); i++){
            mInfoCallbacks.get(i).onWallpaperSetComplete();
        }
    }
    
    public boolean getWallpaperUpdate() {
    	return mWallpaperUpdate;
    }
    
    public void reportWallpaperSet() {
    	mWallpaperUpdate = false;
    }

    public void handleDMKeyguardUpdate(){
        Log.d(TAG, "handleDMKeyguardUpdate: flag = " + KEYGUARD_DM_LOCKED);

        for (int i = 0; i < mDeviceInfoCallbacks.size(); i++) {
            mDeviceInfoCallbacks.get(i).onDMKeyguardUpdate();
        }
    }

    private void handleClockVisibilityChanged() {
        if (DEBUG) Log.d(TAG, "handleClockVisibilityChanged()");
        for (int i = 0; i < mDeviceInfoCallbacks.size(); i++) {
            mDeviceInfoCallbacks.get(i).onClockVisibilityChanged();
        }
    }

   private static boolean isPluggedIn(BatteryStatus status) {
        return status.plugged == BatteryManager.BATTERY_PLUGGED_AC
                || status.plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

//MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    private void handleDownloadCalibrationDataUpdate(){
        Log.d(TAG, "handleDownloadCalibrationDataUpdate");
	for (int i = 0; i < mInfoCallbacks.size(); i++){
            mInfoCallbacks.get(i).onDownloadCalibrationDataUpdate(mCalibrationData);
        }
    }

    private boolean isBatteryUpdateInteresting(BatteryStatus old, BatteryStatus current) {
        // change in plug is always interesting
        final boolean nowPluggedIn = isPluggedIn(current);
        final boolean wasPluggedIn = isPluggedIn(old);
        final boolean stateChangedWhilePluggedIn =
            wasPluggedIn == true && nowPluggedIn == true && (old.status != current.status);
        // change in plug state is always interesting
        if (wasPluggedIn != nowPluggedIn || stateChangedWhilePluggedIn) {
            return true;
        }

        // change in battery level while plugged in
        if (old.level != current.level) {
            return true;
        }

        // change where battery needs charging
        if (!nowPluggedIn && isBatteryLow(current) && current.level != old.level) {
            return true;
        }
        
        //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        //chargeTime changed is interesting 
        if (!old.chargeTime.equals(current.chargeTime)){
        	return true;
        }
        //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        
        return false;
    }

    private boolean isBatteryLow(BatteryStatus status) {
        return status.level < LOW_BATTERY_THRESHOLD;
    }

    /**
     * @param intent The intent with action {@link Telephony.Intents#SPN_STRINGS_UPDATED_ACTION}
     * @return The string to use for the plmn, or null if it should not be shown.
     */
    private CharSequence getTelephonyPlmnFrom(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_SHOW_PLMN, false)) {
            final String plmn = intent.getStringExtra(EXTRA_PLMN);
            if (plmn != null) {
                return plmn;
            } else {
                return getDefaultPlmn();
            }
        }
        return null;
    }

    /**
     * @return The default plmn (no service)
     */
    private CharSequence getDefaultPlmn() {
        return mContext.getResources().getText(
                        R.string.lockscreen_carrier_default);
    }

    /**
     * @param intent The intent with action {@link Telephony.Intents#SPN_STRINGS_UPDATED_ACTION}
     * @return The string to use for the plmn, or null if it should not be shown.
     */
    private CharSequence getTelephonySpnFrom(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_SHOW_SPN, false)) {
            final String spn = intent.getStringExtra(EXTRA_SPN);
            if (spn != null) {
                return spn;
            }
        }
        return null;
    }

    /**
     * Remove the given observer from being registered from any of the kinds
     * of callbacks.
     * @param observer The observer to remove (an instance of {@link ConfigurationChangeCallback},
     *   {@link InfoCallback} or {@link SimStateCallback}
     */
    public void removeCallback(Object observer) {
        mInfoCallbacks.remove(observer);
        mSimStateCallbacks.remove(observer);
        mPhoneCallbacks.remove(observer);
        mDeviceInfoCallbacks.remove(observer);
    }

    /**
     * Callback for phoneStateCallback.
     */
    interface phoneStateCallback{
        /**
               * Called when the phone state changes. String will be one of:
               * {@link TelephonyManager#EXTRA_STATE_IDLE}
              * {@link TelephonyManager@EXTRA_STATE_RINGING}
              * {@link TelephonyManager#EXTRA_STATE_OFFHOOK
             */
        void onPhoneStateChanged(int phoneState);
    }

    /**
       * Callback for new music item.
       **/
    interface deviceInfoCallback{
        /**
         * Called when visibility of lockscreen clock changes, such as when
         * obscured by a widget.
         */
        void onClockVisibilityChanged();
        /**
         * Called when the device becomes provisioned
         */
        void onDeviceProvisioned();

        /**
         * Called when the device becomes provisioned
            */
        void onDMKeyguardUpdate();
    }
    
    /**
     * Callback for general information relevant to lock screen.
     */
    interface InfoCallback {
        void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn, int batteryLevel);
        void onRefreshBatteryInfo(boolean showBatteryInfo, boolean pluggedIn, int batteryLevel, String time); //Line <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
        void onTimeChanged();
        void onUnlockKeyguard();


        /**
         * @param plmn The operator name of the registered network.  May be null if it shouldn't
         *   be displayed.
         * @param spn The service provider name.  May be null if it shouldn't be displayed.
         */
        void onRefreshCarrierInfo(CharSequence plmn, CharSequence spn);

        /**
         * Called when the ringer mode changes.
         * @param state the current ringer state, as defined in
         * {@link AudioManager#RINGER_MODE_CHANGED_ACTION}
         */
        void onRingerModeChanged(int state);

        /***when the siminfo changed***/
        void onSIMInfoChanged(int slotId);

        /**
              *because android4.0, the lockscreen and unlockscreen are the same screen, so, we 
              *move the lockscreen infocallback to infocallback
              */
        void onMissedCallChanged(int missedCall);
        void onWallpaperSetComplete();
        void onSearchNetworkUpdate(int simId, boolean switchOn);
        /*** in order to refresh slidelockscreen, lockscreen and PINScreen***/
        void onLockScreenUpdate(int slotId);
        
        //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
        void onDownloadCalibrationDataUpdate(boolean calibrationData);
        //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
    }

    /**
     * Callback to notify of sim state change.
     */
    interface SimStateCallback {
        void onSimStateChanged(IccCard.State simState);
        void onSimStateChangedGemini(IccCard.State simState, int simId);
    }

    /**
       *Callback to notify the modem state change.
       */
    interface RadioStateCallback{
        void onRadioStateChanged(int slotId);
    }

    /**
     * ALPS00264727: Callback to notify the system state change.
     */
    interface SystemStateCallback {
        void onSysShutdown();
        void onSysBootup();
    }

    public void registerPhoneStateCallback(phoneStateCallback callback){
        if (!mPhoneCallbacks.contains(callback)) {
            mPhoneCallbacks.add(callback);
            // Notify listener of the current state
            callback.onPhoneStateChanged(mPhoneState);
        } else {
            if (DEBUG) Log.e(TAG, "Object tried to add another Phone callback");
        }
    }

    public void registerDeviceInfoCallback(deviceInfoCallback callback){
        if (!mDeviceInfoCallbacks.contains(callback)) {
            mDeviceInfoCallbacks.add(callback);
            // Notify listener of the current state
            callback.onClockVisibilityChanged();
            callback.onDeviceProvisioned();
        } else {
            if (DEBUG) Log.e(TAG, "Object tried to add another Device callback");
        }
    }
            
    public void registerRadioStateCallback(RadioStateCallback callback) {
        mRadioStateCallback = callback;
    }

    /**
     * Register to receive notifications about general keyguard information
     * (see {@link InfoCallback}.
     * @param callback The callback.
     */
    public void registerInfoCallback(InfoCallback callback) {
        if (!mInfoCallbacks.contains(callback)) {
            mInfoCallbacks.add(callback);
            // Notify listener of the current state
            
            //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
            /*callback.onRefreshBatteryInfo(shouldShowBatteryInfo(),isPluggedIn(mBatteryStatus),
                    mBatteryStatus.level);*/
            callback.onRefreshBatteryInfo(shouldShowBatteryInfo(),isPluggedIn(mBatteryStatus),
                    mBatteryStatus.level, mBatteryStatus.chargeTime);
            //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
            
            callback.onTimeChanged();
            callback.onRingerModeChanged(mRingMode);
            //callback.onPhoneStateChanged(mPhoneState);
            callback.onRefreshCarrierInfo(mTelephonyPlmn, mTelephonySpn);
            //callback.onClockVisibilityChanged();
            //MTK-START [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
            callback.onDownloadCalibrationDataUpdate(mCalibrationData);
            //MTK-END [mtk80950][1204010][ALPS00266631]check whether download calibration data or not
        } else {
            if (DEBUG) Log.e(TAG, "Object tried to add another INFO callback");
        }
    }

    /**
     * ALPS00264727: register to receive notification about system state change (shutdown, bootup)
     * for KeyguardViewManager only
     */
    public void registerSystemStateCallback(SystemStateCallback callback) {
        mSystemStateCallback = callback;
    }

    public void unRegisterRadioStateCallback(){
        mRadioStateCallback = null;
    }
    /**
     ** get the flag which records whether or not the user touch dismiss button
     * PIN flag if true, that is PIN requried, else puk required.
     */
    public void setPINDismiss(int slotId, boolean PINFlag, boolean flag) {
        int bitSet = 0x1;
        bitSet <<= PINFlag?slotId:2+slotId;
        if (flag) {
            mPINFlag |= bitSet;
        }else {
            mPINFlag &= ~bitSet;
        }
    }

    public boolean getPINDismissFlag(int slotId, boolean PINFlag){
        Log.i(TAG, "getPINDismissFlag, slotId="+slotId+", PINFlag="+PINFlag);
        int bitSet = 0x1;
        int result = 0x0;
        if (PINFlag){
            result = bitSet << slotId;
        }else {
            result = bitSet << 2+slotId;
        }
        return  0 == (mPINFlag & result)?false:true;
    }

    /**
     * Register to be notified of sim state changes.
     * @param callback The callback.
     */
    public void registerSimStateCallback(SimStateCallback callback) {
        if (!mSimStateCallbacks.contains(callback)) {
            mSimStateCallbacks.add(callback);
            // Notify listener of the current state
            if (FeatureOption.MTK_GEMINI_SUPPORT){
                callback.onSimStateChangedGemini(mSimState, Phone.GEMINI_SIM_1);
                callback.onSimStateChangedGemini(mSim2State, Phone.GEMINI_SIM_2);
            }else {
                callback.onSimStateChanged(mSimState);
            }
        } else {
            if (DEBUG) Xlog.e(TAG, "Object tried to add another SIM callback");
        }
    }

    public void reportClockVisible(boolean visible) {
        mClockVisible = visible;
        mHandler.obtainMessage(MSG_CLOCK_VISIBILITY_CHANGED).sendToTarget();
    }


    public IccCard.State getSimState() {
       return mSimState;
    }
    
    public IccCard.State getSimState(int simId) {
        if(simId == Phone.GEMINI_SIM_2){
            Xlog.d(TAG, "mSim2State = "+ mSim2State);
            return mSim2State;
        }else{
            Xlog.d(TAG, "mSimState = "+ mSimState);
            return mSimState;
        }
    }

    public IccCard.State getLastSimState(int simId) {
        if(simId == Phone.GEMINI_SIM_2){
            Xlog.d(TAG, "mSim2LastState = "+ mSim2LastState);
            return mSim2LastState;
        }else{
            Xlog.d(TAG, "mSimLastState = "+ mSimLastState);
            return mSimLastState;
        }
    }
    

    /**
     * Report that the user succesfully entered the sim pin so we
     * have the information earlier than waiting for the intent
     * broadcast from the telephony code.
     */
    public void reportSimUnlocked() {
	if(mSimState!= IccCard.State.NETWORK_LOCKED){
            mSimState = IccCard.State.READY;
        }
        //mergrate from Android4.0.4, but we don't need it 
        //handleSimStateChange(new SimArgs(mSimState));
    }

    public void reportSimUnlocked(int simId) {
        //if (DEBUG) Xlog.d(TAG, "mSimState = "+ IccCard.State.READY + ", simId = " + simId);
        if(simId == Phone.GEMINI_SIM_2) {
            if(mSim2State!= IccCard.State.NETWORK_LOCKED)
                mSim2State = IccCard.State.READY;
        } else {
            if(mSimState!= IccCard.State.NETWORK_LOCKED)
                mSimState = IccCard.State.READY;
        }
    }

    public boolean isKeyguardBypassEnabled() {
        return mKeyguardBypassEnabled;
    }

    public boolean isDevicePluggedIn() {
        return isPluggedIn(mBatteryStatus);
    }

    public boolean isDeviceCharged() {
        return mBatteryStatus.status == BATTERY_STATUS_FULL
                || mBatteryStatus.level >= 100; // in case particular device doesn't flag it
    }

    public boolean isDeviceCharging() {
        return mBatteryStatus.status != BatteryManager.BATTERY_STATUS_DISCHARGING
                && mBatteryStatus.status != BatteryManager.BATTERY_STATUS_NOT_CHARGING;
    }
    
    //Begin <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
    public String getBatteryChargeTime(){
    	return mBatteryStatus.chargeTime;
    }
    //End <PowerSaving> <20120905> <add Power Saving Appliction> yutao>
    
    public int getBatteryLevel() {
        return mBatteryStatus.level;
    }

    public boolean shouldShowBatteryInfo() {
        return isPluggedIn(mBatteryStatus) || isBatteryLow(mBatteryStatus);
    }

    public CharSequence getTelephonyPlmn() {
        return mTelephonyPlmn;
    }

    public CharSequence getTelephonySpn() {
        return mTelephonySpn;
    }
    
    public CharSequence getTelephonyPlmn(int simId) {
        if(simId == Phone.GEMINI_SIM_2) {
            return mTelephonyPlmnGemini;
        }else{
            return mTelephonyPlmn;
        }
    }

    public CharSequence getTelephonySpn(int simId) {
        if(simId == Phone.GEMINI_SIM_2){
            return mTelephonySpnGemini;
        }else{
            return mTelephonySpn;
        }
    }


    /**
     * @return Whether the device is provisioned (whether they have gone through
     *   the setup wizard)
     */
    public boolean isDeviceProvisioned() {
        return mDeviceProvisioned;
    }

    public int getFailedAttempts() {
        return mFailedAttempts;
    }

    public void clearFailedAttempts() {
        mFailedAttempts = 0;
    }

    public void reportFailedAttempt() {
        mFailedAttempts++;
    }

    public boolean isClockVisible() {
        return mClockVisible;
    }

    public int getPhoneState() {
        return mPhoneState;
    }
    public int getMissedCall() {
       return mMissedCall;
    }

    public boolean getWallpaperStatus() {
       return mWallpaperSetComplete;
    }

    public boolean isPhoneAppReady() {
        final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        
        boolean ready = false;
        List<RunningAppProcessInfo> runningAppInfo = am.getRunningAppProcesses();   
        if (runningAppInfo == null){
        	  Log.i(TAG, "runningAppInfo == null");
        	  return ready;
        }        
        for (RunningAppProcessInfo app : runningAppInfo) {
            if (app.processName.equals("com.android.phone")){
                ready = true;
                break;
            }
        }
        return ready;
    }

    //DM begin
    public boolean DM_IsLocked()
    {
        //if (DEBUG) Xlog.i (TAG,"DM_IsLocked, lockflag is "+KEYGUARD_DM_LOCKED);
        return KEYGUARD_DM_LOCKED;
    }

    //DM Begin
    private void DM_Check_Locked()
    {
        try {
            //for OMA DM
            IBinder binder = ServiceManager.getService("DMAgent");
            if (binder != null) {
                DMAgent agent = DMAgent.Stub.asInterface(binder);
                boolean flag = agent.isLockFlagSet();
                Log.i(TAG,"DM_Check_Locked, the lock flag is:"+flag);
                KEYGUARD_DM_LOCKED = flag;
            }else{
                Log.i(TAG,"DM_Check_Locked, DMAgent doesn't exit");
            }
        } catch(Exception e){
            Log.e(TAG,"get DM status failed!");
        }   
    }
    //DM end

   public String getOptrNameById(long simId){
        if (simId > 0){
            Xlog.i(TAG, "getOptrNameById, xxsimId="+simId);
            SIMInfo info = SIMInfo.getSIMInfoById(mContext, (int)simId);
            if (null == info){
               Xlog.i(TAG, "getOptrNameBySlotId, return null");
               return null;
            }else{
               Xlog.i(TAG, "info="+info.mDisplayName);
               return info.mDisplayName; 
            }
        }else if (-1 == simId){
            return mContext.getResources().getString(com.mediatek.internal.R.string.keyguard_alwaysask);
        }else if (-2 == simId){
            return mContext.getResources().getString(com.mediatek.internal.R.string.keyguard_internal_call);
        }else if (0 == simId){
            return mContext.getResources().getString(com.mediatek.internal.R.string.keyguard_data_none);
        }else{
            return mContext.getResources().getString(com.mediatek.internal.R.string.keyguard_not_set);
        }
    }
    
    public Drawable getOptrDrawableById(long simId){
        if (simId > 0){
            Xlog.i(TAG, "getOptrDrawableById, xxsimId="+simId);
            SIMInfo info = SIMInfo.getSIMInfoById(mContext, (int)simId); 
            if (null == info){
               Xlog.i(TAG, "getOptrDrawableBySlotId, return null");
               return null;
            }else{
               return mContext.getResources().getDrawable(info.mSimBackgroundRes);
            }
        }else{
            return null;
        }
    }
   public String getOptrNameBySlot(long Slot){
        if (Slot >= 0){
            Xlog.i(TAG, "getOptrNameBySlot, xxSlot="+Slot);
            SIMInfo info = SIMInfo.getSIMInfoBySlot(mContext, (int)Slot);
            if (null == info){
               Xlog.i(TAG, "getOptrNameBySlotId, return null");
               return null;
            }else{
               Xlog.i(TAG, "info="+info.mDisplayName);
               return info.mDisplayName; 
            }
        }else {
            throw new IndexOutOfBoundsException();
        }
    }

    
   public String getOptrNameBySlotForCTA(long Slot){
       if (Slot >= 0){
           Xlog.i(TAG, "getOptrNameBySlot, xxSlot="+Slot);
           SIMInfo info = SIMInfo.getSIMInfoBySlot(mContext, (int)Slot);
           if (null == info || info.mDisplayName == null){
              Xlog.i(TAG, "getOptrNameBySlotId, return null");
              if (Phone.GEMINI_SIM_2 == Slot){
            	  return mContext.getResources().getString(com.mediatek.internal.R.string.new_sim) + " 02";
              }else{
            	  return mContext.getResources().getString(com.mediatek.internal.R.string.new_sim) + " 01";
              }
           }else{
              Xlog.i(TAG, "info="+info.mDisplayName);
              return info.mDisplayName; 
           }
       }else {
           return mContext.getResources().getString(com.mediatek.internal.R.string.keyguard_not_set);
       }
   }
  
   
    
    public Drawable getOptrDrawableBySlot(long Slot){
        if (Slot >= 0){
            Xlog.i(TAG, "getOptrDrawableBySlot, xxslot="+Slot);
            SIMInfo info = SIMInfo.getSIMInfoBySlot(mContext, (int)Slot); 
            if (null == info){
               Xlog.i(TAG, "getOptrDrawableBySlotId, return null");
               return null;
            }else{
               return mContext.getResources().getDrawable(info.mSimBackgroundRes);
            }
        }else{
            throw new IndexOutOfBoundsException();
        }
    }

    public void InitView(View v, String s){
        long voicecallSlot = Settings.System.getLong(mContext.getContentResolver(), 
                          Settings.System.VOICE_CALL_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
        long smsSlot = Settings.System.getLong(mContext.getContentResolver(), 
                          Settings.System.SMS_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
        long dateSlot = Settings.System.getLong(mContext.getContentResolver(), 
                          Settings.System.GPRS_CONNECTION_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
        long videoSlot = Settings.System.getLong(mContext.getContentResolver(), 
                          Settings.System.VIDEO_CALL_SIM_SETTING, Settings.System.DEFAULT_SIM_NOT_SET);
        TelephonyManager telephony = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        boolean isVoiceCapable = (telephony != null && telephony.isVoiceCapable());
        boolean isSmsCapable = (telephony != null && telephony.isSmsCapable());        
        if (DEBUG) Log.i(TAG, "InitView, isVoiceCapable="+isVoiceCapable+" isSmsCapable "+isSmsCapable);
        
        Xlog.i(TAG, "voicecallSlot="+voicecallSlot+",smsSlot="+smsSlot+",dateSlot="+dateSlot+", videoSlot="+videoSlot);

        //for the newsiminserted/defaultsimremoved
        ((TextView)v.findViewById(com.mediatek.internal.R.id.stringprompt)).setText(s);
        if (false == mSIMRemoved){//here we goto the new sim inserted dialog, but it has the different UI layout from the default sim removed
            TextView firstName = (TextView)v.findViewById(com.mediatek.internal.R.id.first_sim_name);
            TextView secondName = (TextView)v.findViewById(com.mediatek.internal.R.id.second_sim_name);
            ((TextView) v.findViewById(com.mediatek.internal.R.id.sim_setting_prompt))
               .setText(com.mediatek.internal.R.string.new_sim_setting_prompt);
            if (1 == mCardTotal){// only SIM1 inserted
                secondName.setVisibility(View.GONE);
                    addNameForSIMDetectDialog(firstName, Phone.GEMINI_SIM_1);
            }else if (2 == mCardTotal){// only SIM2 inserted
                secondName.setVisibility(View.GONE);
                    addNameForSIMDetectDialog(firstName, Phone.GEMINI_SIM_2);
            }else {//both SIM inserted
                addNameForSIMDetectDialog(firstName, Phone.GEMINI_SIM_1);
                addNameForSIMDetectDialog(secondName, Phone.GEMINI_SIM_2);
            }
        }

        TextView voicecall = (TextView) v.findViewById(com.mediatek.internal.R.id.voicecall);
        //TextView videocall = (TextView) v.findViewById(com.mediatek.internal.R.id.videocall);
        //BEGIN <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
        TextView videocall = null;
        if(FeatureOption.MTK_VT3G324M_SUPPORT == true) {
            videocall = (TextView) v.findViewById(com.mediatek.internal.R.id.videocall);
        }
        //END <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
        if (isVoiceCapable)
        {
          voicecall.setText(com.mediatek.internal.R.string.keyguard_voicecall);
          //videocall.setText(com.mediatek.internal.R.string.keyguard_videocall);
          //BEGIN <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
          if(FeatureOption.MTK_VT3G324M_SUPPORT == true) {
            videocall.setText(com.mediatek.internal.R.string.keyguard_videocall);
          }
          //END <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
        }
        else
        {
          voicecall.setVisibility(View.GONE);
          //videocall.setVisibility(View.GONE);
          //BEGIN <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
          if(FeatureOption.MTK_VT3G324M_SUPPORT == true) {
            videocall.setVisibility(View.GONE);
          }
          //END <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
        }
        
        TextView sms = (TextView) v.findViewById(com.mediatek.internal.R.id.sms);
        if (isSmsCapable)
        {
          sms.setText(com.mediatek.internal.R.string.keyguard_sms);
        }
        else
        {
          sms.setVisibility(View.GONE);
        }
        
        
        TextView data = (TextView) v.findViewById(com.mediatek.internal.R.id.data);
        data.setText(com.mediatek.internal.R.string.keyguard_data);
        
        TextView voicecalloptr = (TextView) v.findViewById(com.mediatek.internal.R.id.voicecallopr);
        //TextView videcallooptr = (TextView) v.findViewById(com.mediatek.internal.R.id.videocallopr);
        //BEGIN <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
        TextView videcallooptr = null;
        if(FeatureOption.MTK_VT3G324M_SUPPORT == true) {
            videcallooptr = (TextView) v.findViewById(com.mediatek.internal.R.id.videocallopr);
        }
        //END <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry

				if (isVoiceCapable)
				{
					voicecalloptr.setBackgroundDrawable(getOptrDrawableById(voicecallSlot));
					String voicecalloptrname = getOptrNameById(voicecallSlot);
					if (null == voicecalloptrname){
						voicecalloptr.setText(com.mediatek.internal.R.string.searching_simcard);
					}else{
						voicecalloptr.setText(voicecalloptrname);
					}
		                        
                                        //BEGIN <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
				        if(FeatureOption.MTK_VT3G324M_SUPPORT == true) {
					    videcallooptr.setBackgroundDrawable(getOptrDrawableById(videoSlot));
					    String videooptnamestring = getOptrNameById(videoSlot);
					    if (null == videooptnamestring){
					        videcallooptr.setText(com.mediatek.internal.R.string.searching_simcard);
					    }else{
					        videcallooptr.setText(videooptnamestring);
					    }
                                        }
				        //END <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
				}
				else
				{
				  voicecalloptr.setVisibility(View.GONE);
				  //videcallooptr.setVisibility(View.GONE);
				  //BEGIN <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
				  if(FeatureOption.MTK_VT3G324M_SUPPORT == true) {
				      videcallooptr.setVisibility(View.GONE);
				  }
				  //END <SNOFT-149><2012-06-12><S9052 does not support videocall> Jerry
				}
		
        TextView smsoptr = (TextView) v.findViewById(com.mediatek.internal.R.id.smsopr);
        if (isSmsCapable)
        {
	        smsoptr.setBackgroundDrawable(getOptrDrawableById(smsSlot));
	        String smsoptrname = getOptrNameById(smsSlot);
	        if (null == smsoptrname){
	            smsoptr.setText(com.mediatek.internal.R.string.searching_simcard);
	        }else{
	            smsoptr.setText(smsoptrname);
	        }
        }
        else
        {
          smsoptr.setVisibility(View.GONE);
        }
        
        TextView dataoptr = (TextView) v.findViewById(com.mediatek.internal.R.id.dataopr);
        dataoptr.setBackgroundDrawable(getOptrDrawableById(dateSlot));
        String dataoptnamestring = getOptrNameById(dateSlot);
        if (null == dataoptnamestring){
            dataoptr.setText(com.mediatek.internal.R.string.searching_simcard);
        }else{
            dataoptr.setText(dataoptnamestring);
        }
    }

    public void addNameForSIMDetectDialog(TextView v, int slotId){
        v.setBackgroundDrawable(getOptrDrawableBySlot(slotId));
        String optrname = getOptrNameBySlot(slotId);
        if (null == optrname){
            v.setText(com.mediatek.internal.R.string.searching_simcard);
        }else{
            v.setText(optrname);
        }
    }

    /**
     * Whether or not exist SIM card in device.
     * 
     * @return
     */
    public boolean IsSIMInserted(int slotId) {
        try {
            final ITelephony phone = 
                ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null && !phone.isSimInsert(slotId)) {
                return false;
            }
        } catch (RemoteException ex) {
            Xlog.e(TAG, "Get sim insert status failure!");
            return false;
        }
        return true;
    }

    public void setDebugFilterStatus(boolean debugFlag){
        DEBUG = debugFlag;
    }
}
