/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.android.keyguard;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import com.mediatek.common.featureoption.FeatureOption;

public class PowerOffAlarmView extends RelativeLayout implements
        KeyguardSecurityView, MediatekGlowPadView.OnTriggerListener {
    private static final String TAG = "PowerOffAlarm";
    private static final boolean DEBUG = false;
    private final int DELAY_TIME_SECONDS = 7;
    private int mFailedPatternAttemptsSinceLastTimeout = 0;
    private int mTotalFailedPatternAttempts = 0;
    private LockPatternUtils mLockPatternUtils;
    private Button mForgotPatternButton;
    private TextView mVcTips, titleView = null;
    private LinearLayout mVcTipsContainer;
    private KeyguardSecurityCallback mCallback;
    private boolean isRegistered = false;
    private boolean mEnableFallback;
    private Context mContext;

    // These defaults must match the values in res/xml/settings.xml
    private static final String DEFAULT_SNOOZE = "10";
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";
    protected static final String SCREEN_OFF = "screen_off";

    protected Alarm mAlarm;
    private int mVolumeBehavior;
    boolean mFullscreenStyle;
    private MediatekGlowPadView mGlowPadView;
    private boolean mIsDocked = false;
    private static final int UPDATE_LABEL = 99;
    // Parameters for the GlowPadView "ping" animation; see triggerPing().
    private static final int PING_MESSAGE_WHAT = 101;
    private static final boolean ENABLE_PING_AUTO_REPEAT = true;
    private static final long PING_AUTO_REPEAT_DELAY_MSEC = 1200;

    private boolean mPingEnabled = true;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PING_MESSAGE_WHAT:
                    triggerPing();
                    break;
                case UPDATE_LABEL:
                    if(titleView != null){
                        titleView.setText(msg.getData().getString("label"));
                    }
                    break;
            }
        }
    };

    //Begin yutao add  2014.6.23 SmartGesture
    private SensorManager mSensorManager;
    private Sensor AccelerometerSensor;
    private float z = 0;
    private float az = 0;
    private boolean faceUp = false;
    private ContentResolver cr;
    private boolean isOpenProSensor = false;

    private final SensorEventListener AccelerometerSnoozeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            az = event.values[SensorManager.DATA_Z];
            if (z > 0 && az > 0) {
                faceUp = true;
            }
            if (z > -7 && z < 12 && az < -7 && az > -16  && faceUp) {
                faceUp = false;
                snooze();
            }
            z = az;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not used.
        }
    };
    //End yutao add end 2014.6.23 SmartGesture

    public PowerOffAlarmView(Context context) {
        this(context, null);
    }

    public PowerOffAlarmView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        //Begin yutao add 2014.6.23
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        AccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        isOpenProSensor = false;
        //End yutao add 2014.6.23
    }

    //Begin yutao add 2014.6.23 SmartGesture
    private void registerSensor(){
        Log.d(TAG, "tui, register AccelerometerSnoozeListener for PowerOffAlarm");
        cr = mContext.getContentResolver();
        boolean bUpsetSnoozeOn = 1== Settings.Secure.getInt(cr,
                Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED,
                Settings.Secure.UPSET_SNOOZE_ALARM_ENABLED, 0) ;

        if (bUpsetSnoozeOn){
            mSensorManager.registerListener(AccelerometerSnoozeListener,
                    AccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            isOpenProSensor = true;
        }else{
            isOpenProSensor = false;
        }
    }

    private void unregisterSensor(){
        if (isOpenProSensor){
            Log.d(TAG, "tui, unregister AccelerometerSnoozeListener for PowerOffAlarm");
            mSensorManager.unregisterListener(AccelerometerSnoozeListener);
        }
    }
    //End yutao add 2014.6.23 SmartGesture


    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        mCallback = callback;
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        mLockPatternUtils = utils;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.w(TAG, "onFinishInflate ... ");
        setKeepScreenOn(true);
        titleView = (TextView) findViewById(R.id.alertTitle);
        mGlowPadView = (MediatekGlowPadView) findViewById(R.id.glow_pad_view);
        mGlowPadView.setOnTriggerListener(this);
        setFocusableInTouchMode(true);
        triggerPing();

        // Check the docking status , if the device is docked , do not limit rotation
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
        Intent dockStatus = mContext.registerReceiver(null, ifilter);
        if (dockStatus != null) {
            mIsDocked = dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1)
                    != Intent.EXTRA_DOCK_STATE_UNDOCKED;
        }

        // Register to get the alarm killed/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
        filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
        filter.addAction(Alarms.ALARM_DISMISS_ACTION);
        filter.addAction(UPDATE_LABEL_ACTION);
        mContext.registerReceiver(mReceiver, filter);

        mLockPatternUtils = mLockPatternUtils == null ? new LockPatternUtils(
                mContext) : mLockPatternUtils;
        enableEventDispatching(true);
    }

    @Override
    public void onTrigger(View v, int target) {
        final int resId = mGlowPadView.getResourceIdForTarget(target);
        switch (resId) {
            case R.drawable.mtk_ic_alarm_alert_snooze:
                snooze();
                break;

            case R.drawable.mtk_ic_alarm_alert_dismiss_pwroff:
                powerOff();
                break;

            case R.drawable.mtk_ic_alarm_alert_dismiss_pwron:
                powerOn();
                break;

            default:
                // Code should never reach here.
                Log.e(TAG, "Trigger detected on unhandled resource. Skipping.");
        }
    }

    private void triggerPing() {
        if (mPingEnabled) {
            mGlowPadView.ping();

            if (ENABLE_PING_AUTO_REPEAT) {
                mHandler.sendEmptyMessageDelayed(PING_MESSAGE_WHAT, PING_AUTO_REPEAT_DELAY_MSEC);
            }
        }
    }

    // Attempt to snooze this alert.
    private void snooze() {
        Log.d(TAG, "snooze selected");
	sendBR(SNOOZE);
    }

    // power on the device
    private void powerOn() {
        enableEventDispatching(false);
        Log.d(TAG, "powerOn selected");
        sendBR(DISMISS_AND_POWERON);
        sendBR(NORMAL_BOOT_ACTION);
    }

    // power off the device
    private void powerOff() {
        Log.d(TAG, "powerOff selected");
        sendBR(DISMISS_AND_POWEROFF);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        //TODO: if we need to add some logic here ?
        return result;
    }

    @Override
    public void showUsabilityHint() {
    }

    /** TODO: hook this up */
    public void cleanUp() {
        if (DEBUG)
            Log.v(TAG, "Cleanup() called on " + this);
        mLockPatternUtils = null;
    }

    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume(int reason) {
        reset();
        Log.v(TAG, "onResume");
        registerSensor();//yutao add 2014.6.23 SmartGesture
    }

    @Override
    public KeyguardSecurityCallback getCallback() {
        return mCallback;
    }

    @Override
    public void onDetachedFromWindow() {
        Log.v(TAG, "onDetachedFromWindow ....");
        mContext.unregisterReceiver(mReceiver);
        unregisterSensor();//yutao add 2014.6.23 SmartGesture
    }

    @Override
    public void showBouncer(int duration) {
    }

    @Override
    public void hideBouncer(int duration) {
    }

    private void enableEventDispatching(boolean flag) {
        try {
            final IWindowManager wm = IWindowManager.Stub
                    .asInterface(ServiceManager
                            .getService(Context.WINDOW_SERVICE));
            if(wm != null){
                wm.setEventDispatching(flag);
            }
        } catch (RemoteException e) {
            Log.w(TAG, e.toString());
        }
    }

    private void sendBR(String action){
        Log.w(TAG, "send BR: " + action);
        mContext.sendBroadcast(new Intent(action));
    }

    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other
    // applications
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();
          Log.v(TAG, "receive action : " + action);
          if(UPDATE_LABEL_ACTION.equals(action)){
              Message msg = new Message();
              msg.what = UPDATE_LABEL;
              Bundle data = new Bundle();
              data.putString("label", intent.getStringExtra("label"));
              msg.setData(data);
              mHandler.sendMessage(msg);
          }else if (PowerOffAlarmManager.isAlarmBoot()) {
              snooze();
          }
       }
    };

    @Override
    public void onGrabbed(View v, int handle) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onReleased(View v, int handle) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onFinishFinalAnimation() {
        // TODO Auto-generated method stub
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
    }

    private static final String SNOOZE = "com.android.deskclock.SNOOZE_ALARM";
    private static final String DISMISS_AND_POWEROFF = "com.android.deskclock.DISMISS_ALARM";
    private static final String DISMISS_AND_POWERON = "com.android.deskclock.POWER_ON_ALARM";
    private static final String UPDATE_LABEL_ACTION = "update.power.off.alarm.label";
    private static final String NORMAL_BOOT_ACTION = "android.intent.action.normal.boot";
    private static final String NORMAL_BOOT_DONE_ACTION = "android.intent.action.normal.boot.done";
    private static final String DISABLE_POWER_KEY_ACTION = "android.intent.action.DISABLE_POWER_KEY";

}
