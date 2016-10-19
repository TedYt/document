/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.deskclock.alarms;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextClock;
import android.widget.TextView;

import com.android.deskclock.Log;
import com.android.deskclock.R;
import com.android.deskclock.SettingsActivity;
import com.android.deskclock.Utils;
import com.android.deskclock.provider.AlarmInstance;
import com.android.deskclock.widget.multiwaveview.GlowPadView;

//aoran add
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.content.ContentResolver;

/**
 * Alarm activity that pops up a visible indicator when the alarm goes off.
 */
public class AlarmActivity extends Activity {
    // AlarmActivity listens for this broadcast intent, so that other applications
    // can snooze the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";

    // AlarmActivity listens for this broadcast intent, so that other applications
    // can dismiss the alarm (after ALARM_ALERT_ACTION and before ALARM_DONE_ACTION).
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";

    //aoran add
    private SensorManager mSensorManager;
    private Sensor AccelerometerSensor;
    private Sensor mPromixySensor; //yutao
    private static final float SETZ_SILENT = -7;
    private static final boolean USE_GRAVITY_SENSOR = false;
    private float z = 0;
    private float az = 0;
    private boolean faceUp = false;
    private int   iFaceUpCnt = 0;
    private ContentResolver cr;
    private boolean isOpenProSensor = false;

    private final SensorEventListener AccelerometerSnoozeListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
                    //snooze();
            az = event.values[SensorManager.DATA_Z];
                if (z > 0 && az > 0) {
                    faceUp = true;
                }
                if (z > SETZ_SILENT && z < 12 && az < SETZ_SILENT && az > -16  && faceUp) {
                    faceUp = false;
                    //broadcastRingerSilentToPhone();
                    snooze();
                }
                z = az;
            // Jiangde END --, tilt angle will be better
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Not used.
        }
    };

    private final SensorEventListener mPromixySensorAnswerListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.values[0] == 0){
                android.util.Log.d("tui", "Alarm, PromixySensorAnswer");
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    // Controller for GlowPadView.
    private class GlowPadController extends Handler implements GlowPadView.OnTriggerListener {
        private static final int PING_MESSAGE_WHAT = 101;
        private static final long PING_AUTO_REPEAT_DELAY_MSEC = 1200;

        public void startPinger() {
            sendEmptyMessage(PING_MESSAGE_WHAT);
        }

        public void stopPinger() {
            removeMessages(PING_MESSAGE_WHAT);
        }

        @Override
        public void handleMessage(Message msg) {
            ping();
            sendEmptyMessageDelayed(PING_MESSAGE_WHAT, PING_AUTO_REPEAT_DELAY_MSEC);
        }

        @Override
        public void onGrabbed(View v, int handle) {
            stopPinger();
        }

        @Override
        public void onReleased(View v, int handle) {
            startPinger();

        }

        @Override
        public void onTrigger(View v, int target) {
		//aoran add
	android.util.Log.d("aoran","AlarmActivity onTrigger");		
            switch (mGlowPadView.getResourceIdForTarget(target)) {
                case R.drawable.ic_alarm_alert_snooze:
                    Log.v("AlarmActivity - GlowPad snooze trigger");
	android.util.Log.d("aoran","snooze");		
                    snooze();
                    break;

                case R.drawable.ic_alarm_alert_dismiss:
                    Log.v("AlarmActivity - GlowPad dismiss trigger");
	android.util.Log.d("aoran","dismiss");		
                    dismiss();
                    break;
                default:
                    // Code should never reach here.
                    Log.e("Trigger detected on unhandled resource. Skipping.");
            }
        }

        @Override
        public void onGrabbedStateChange(View v, int handle) {
            Log.v("AlarmActivity onGrabbedStateChange");
        }

        @Override
        public void onFinishFinalAnimation() {
            Log.v("AlarmActivity onFinishFinalAnimation");
        }
    }

    private AlarmInstance mInstance;
    private int mVolumeBehavior;
    private GlowPadView mGlowPadView;
    private GlowPadController glowPadController = new GlowPadController();
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("AlarmActivity - Broadcast Receiver - " + action);
            if (action.equals(ALARM_SNOOZE_ACTION)) {
                snooze();
            } else if (action.equals(ALARM_DISMISS_ACTION)) {
                dismiss();
            } else if (action.equals(AlarmService.ALARM_DONE_ACTION)) {
                finish();
            } else {
                Log.i("Unknown broadcast in AlarmActivity: " + action);
            }
        }
    };

    private void snooze() {
        AlarmStateManager.setSnoozeState(this, mInstance);
    }

    private void dismiss() {
        AlarmStateManager.setDismissState(this, mInstance);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

	//aoran add
        cr = getContentResolver();
            boolean bUpsetSnoozeOn = 1==Settings.Secure.getInt(cr, 
                                            Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED,
                                            Settings.Secure.UPSET_SNOOZE_ALARM_ENABLED, 0) ;

        long instanceId = AlarmInstance.getId(getIntent().getData());
        mInstance = AlarmInstance.getInstance(this.getContentResolver(), instanceId);
        Log.v("Displaying alarm for instance: " + mInstance);
        if (mInstance == null) {
            // The alarm got deleted before the activity got created, so just finish()
            Log.v("Error displaying alarm for intent: " + getIntent());
            finish();
            return;
        }

        // Get the volume/camera button behavior setting
        final String vol =
                PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsActivity.KEY_VOLUME_BEHAVIOR,
                        SettingsActivity.DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        ///M: Don't show the wallpaper when the alert arrive. @{
        win.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        ///@}

        // In order to allow tablets to freely rotate and phones to stick
        // with "nosensor" (use default device orientation) we have to have
        // the manifest start with an orientation of unspecified" and only limit
        // to "nosensor" for phones. Otherwise we get behavior like in b/8728671
        // where tablets start off in their default orientation and then are
        // able to freely rotate.
        if (!getResources().getBoolean(R.bool.config_rotateAlarmAlert)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        }
        updateLayout();

        //aoran add
        if (bUpsetSnoozeOn) {
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            AccelerometerSensor = mSensorManager.getDefaultSensor(USE_GRAVITY_SENSOR
                    ? Sensor.TYPE_GRAVITY : Sensor.TYPE_ACCELEROMETER);
            mPromixySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            mSensorManager.registerListener(AccelerometerSnoozeListener, AccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(mPromixySensorAnswerListener, mPromixySensor, SensorManager.SENSOR_DELAY_NORMAL);
            isOpenProSensor = true;
        } else {
            isOpenProSensor = false;
        }

        // Register to get the alarm done/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(AlarmService.ALARM_DONE_ACTION);
        filter.addAction(ALARM_SNOOZE_ACTION);
        filter.addAction(ALARM_DISMISS_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void updateTitle() {
        final String titleText = mInstance.getLabelOrDefault(this);
        TextView tv = (TextView)findViewById(R.id.alertTitle);
        tv.setText(titleText);
        super.setTitle(titleText);
    }

    private void updateLayout() {
        final LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.alarm_alert, null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        setContentView(view);
        updateTitle();
        Utils.setTimeFormat((TextClock)(view.findViewById(R.id.digitalClock)),
                (int)getResources().getDimension(R.dimen.bottom_text_size));

        // Setup GlowPadController
        mGlowPadView = (GlowPadView) findViewById(R.id.glow_pad_view);
        mGlowPadView.setOnTriggerListener(glowPadController);
        glowPadController.startPinger();
    }

    private void ping() {
        mGlowPadView.ping();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateLayout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glowPadController.startPinger();
    }

    @Override
    protected void onPause() {
        glowPadController.stopPinger();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mInstance == null) {
            super.onDestroy();

	//aoran add
	if(isOpenProSensor)
		{
	        mSensorManager.unregisterListener(AccelerometerSnoozeListener);
            mSensorManager.unregisterListener(mPromixySensorAnswerListener);
		resetZ();
		}
	
            return;
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();

	//aoran add
	if(isOpenProSensor)
		{
	        mSensorManager.unregisterListener(AccelerometerSnoozeListener);
            mSensorManager.unregisterListener(mPromixySensorAnswerListener);
		resetZ();
		}
    }

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss.
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.
        Log.v("AlarmActivity - dispatchKeyEvent - " + event.getKeyCode());
        switch (event.getKeyCode()) {
            // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    switch (mVolumeBehavior) {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss();
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

	//aoran add
        public void resetZ() {
        	z = 0;
        	az = 0;
        	faceUp = false;
        }

}
