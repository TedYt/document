/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.deskclock;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//aoran add
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.content.ContentResolver;

import com.android.deskclock.widget.multiwaveview.GlowPadView;

import java.util.Calendar;

/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm
 * tone. This activity is the full screen version which shows over the lock
 * screen with the wallpaper as the background.
 */
public class AlarmAlertFullScreen extends Activity implements GlowPadView.OnTriggerListener {
    private final String ALARM_PHONE_LISTENER = "com.android.deskclock.ALARM_PHONE_LISTENER";
    private final boolean LOG = true;
    // These defaults must match the values in res/xml/settings.xml
    private static final String DEFAULT_SNOOZE = "10";
    // the priority for receiver to receive the kill alarm broadcast first
    private static final int PRIORITY = 100;
    private static final String DEFAULT_VOLUME_BEHAVIOR = "1";
    private static final String KEY_VOLUME_BEHAVIOR = "power_on_volume_behavior";
    protected static final String SCREEN_OFF = "screen_off";
    protected Alarm mAlarm;
    private int mVolumeBehavior;
    boolean mFullscreenStyle;
    private GlowPadView mGlowPadView;
    private boolean mIsDocked = false;

    // Parameters for the GlowPadView "ping" animation; see triggerPing().
    private static final int PING_MESSAGE_WHAT = 101;
    private static final boolean ENABLE_PING_AUTO_REPEAT = true;
    private static final long PING_AUTO_REPEAT_DELAY_MSEC = 1200;

    private boolean mPingEnabled = true;

	//aoran add
    private SensorManager mSensorManager;
    private Sensor AccelerometerSensor;
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
    //aoran add end
    
    // Receives the ALARM_KILLED action from the AlarmKlaxon,
    // and also ALARM_SNOOZE_ACTION / ALARM_DISMISS_ACTION from other applications
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (LOG) {
                Log.v("AlarmAlertFullScreen - onReceive  action = " + action);
            }
            if (action.equals(Alarms.ALARM_SNOOZE_ACTION)) {
                Alarms.snooze(context, mAlarm);
                finish();
            } else if (action.equals(Alarms.ALARM_DISMISS_ACTION)) {
                Alarms.dismiss(context, mAlarm, false);
                finish();
            } else {
                Alarm alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
                boolean replaced = intent.getBooleanExtra(Alarms.ALARM_REPLACED, false);
                if (alarm != null && mAlarm.id == alarm.id) {
                    Alarms.dismiss(context, mAlarm, true);
                    if (!replaced) {
                        finish();
                    }
                }
            }
        }
    };

    private final Handler mPingHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case PING_MESSAGE_WHAT:
                triggerPing();
                break;
            }
        }
    };

    private String[] mKeywordArray;
    private Context mContext;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        //aoran add
        cr = getContentResolver();
        boolean bUpsetSnoozeOn = 1==Settings.Secure.getInt(cr, 
                                Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED,
                                Settings.Secure.UPSET_SNOOZE_ALARM_ENABLED, 0) ;
                                            
        // Register to get the alarm killed/snooze/dismiss intent.
        IntentFilter filter = new IntentFilter(Alarms.ALARM_KILLED);
        filter.addAction(Alarms.ALARM_SNOOZE_ACTION);
        filter.addAction(Alarms.ALARM_DISMISS_ACTION);
        filter.setPriority(PRIORITY);
        registerReceiver(mReceiver, filter);

        if (Alarms.bootFromPoweroffAlarm()) {
            finish();
            return;
        }
        mAlarm = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        if (LOG) {
            if (mAlarm != null) {
                Log.v("AlarmAlertFullScreen - Alarm Id " + mAlarm.toString());
            }
        }

        // Get the volume/camera button behavior setting
        final String vol = PreferenceManager.getDefaultSharedPreferences(this).getString(
                SettingsActivity.KEY_VOLUME_BEHAVIOR, SettingsActivity.DEFAULT_VOLUME_BEHAVIOR);
        mVolumeBehavior = Integer.parseInt(vol);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        // Turn on the screen unless we are being launched from the AlarmAlert
        // subclass as a result of the screen turning off.
        if (!getIntent().getBooleanExtra(SCREEN_OFF, false)) {
            win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }

        updateLayout();

 		//aoran add
		if(bUpsetSnoozeOn)
		{
        	mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        	AccelerometerSensor = mSensorManager.getDefaultSensor(USE_GRAVITY_SENSOR
                         ? Sensor.TYPE_GRAVITY : Sensor.TYPE_ACCELEROMETER);
        	mSensorManager.registerListener(AccelerometerSnoozeListener, AccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
			isOpenProSensor = true;
		}
		else
		{
			isOpenProSensor = false;
		}
		//aoran add end

        // Check the docking status , if the device is docked , do not limit rotation
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
        Intent dockStatus = registerReceiver(null, ifilter);
        if (dockStatus != null) {
            mIsDocked = dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1) != Intent.EXTRA_DOCK_STATE_UNDOCKED;
        }
    }

    private void setTitle() {
        final String titleText = mAlarm.getLabelOrDefault(this);

        TextView tv = (TextView) findViewById(R.id.alertTitle);
        if (tv != null) {
            tv.setText(titleText);
        }
        setTitle(titleText);
    }

    protected int getLayoutResId() {
        return R.layout.alarm_alert;
    }

    private void updateLayout() {
        if (LOG) {
            Log.v("AlarmAlertFullScreen - updateLayout");
        }

        final LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(getLayoutResId(), null);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        setContentView(view);

        /* Set the title from the passed in alarm */
        setTitle();

        mGlowPadView = (GlowPadView) findViewById(R.id.glow_pad_view);
        mGlowPadView.setOnTriggerListener(this);
        triggerPing();
    }

    private void triggerPing() {
        if (mPingEnabled) {
            mGlowPadView.ping();

            if (ENABLE_PING_AUTO_REPEAT) {
                mPingHandler.sendEmptyMessageDelayed(PING_MESSAGE_WHAT, PING_AUTO_REPEAT_DELAY_MSEC);
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Log.LOGV) {
            Log.v("AlarmAlert.OnNewIntent()");
        }
        mAlarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        setTitle();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (LOG) {
            Log.v("AlarmAlertFullScreen - onConfigChanged");
        }
        updateLayout();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (LOG) {
            Log.v("AlarmAlertFullScreen - onResume");
        }
        // If the alarm was deleted at some point, disable snooze.
        if (Alarms.getAlarm(getContentResolver(), mAlarm.id) == null) {
            Log.v("AlarmAlertFullScreen alarm was null or been delete");
            mGlowPadView.setTargetResources(R.array.dismiss_drawables);
            mGlowPadView.setTargetDescriptionsResourceId(R.array.dismiss_descriptions);
            mGlowPadView.setDirectionDescriptionsResourceId(R.array.dismiss_direction_descriptions);
        }
        // The activity is locked to the default orientation as a default set in the manifest
        // Override this settings if the device is docked or config set it differently
        if (getResources().getBoolean(R.bool.config_rotateAlarmAlert) || mIsDocked) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Log.LOGV) {
            Log.v("AlarmAlert.onPause()");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.LOGV) {
            Log.v("AlarmAlert.onDestroy()");
        }
        // No longer care about the alarm being killed.
        unregisterReceiver(mReceiver);
        //aoran add
		if(isOpenProSensor)
		{
	        mSensorManager.unregisterListener(AccelerometerSnoozeListener);
			resetZ();
		}
		//aoran add end
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Do this on key down to handle a few of the system keys.eeeeeee
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        if (LOG) {
            Log.v("AlarmAlertFullScreen - dispatchKeyEvent " + event.getKeyCode());
        }
        switch (event.getKeyCode()) {
        // Volume keys and camera keys dismiss the alarm
        case KeyEvent.KEYCODE_POWER:
        case KeyEvent.KEYCODE_VOLUME_UP:
        case KeyEvent.KEYCODE_VOLUME_DOWN:
        case KeyEvent.KEYCODE_VOLUME_MUTE:
        case KeyEvent.KEYCODE_CAMERA:
        case KeyEvent.KEYCODE_FOCUS:
            if (up) {
                switch (mVolumeBehavior) {
                case 1:
                    Alarms.snooze(this, mAlarm);
                    finish();
                    break;

                case 2:
//                    if (IS_SUPPORT_VOICE_COMMAND_UI) {
//                        getNotificationManager().cancel("voiceui", 100);
//                    }
                    Alarms.dismiss(this, mAlarm, false);
                    finish();
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

    @Override
    public void onBackPressed() {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        if (LOG) {
            Log.v("AlarmAlertFullScreen - onBackPressed");
        }
        return;
    }

    @Override
    public void onGrabbed(View v, int handle) {
        mPingEnabled = false;
    }

    @Override
    public void onReleased(View v, int handle) {
        mPingEnabled = true;
        triggerPing();
    }

    @Override
    public void onTrigger(View v, int target) {
        final int resId = mGlowPadView.getResourceIdForTarget(target);
        Log.v("onTrigger Alarms snooze or dismiss");
        switch (resId) {
        case R.drawable.ic_alarm_alert_snooze:
            Alarms.snooze(this, mAlarm);
            finish();
            break;

        case R.drawable.ic_alarm_alert_dismiss:
//            if (IS_SUPPORT_VOICE_COMMAND_UI) {
//                getNotificationManager().cancel("voiceui", 100);
//            }
            Alarms.dismiss(this, mAlarm, false);
            finish();
            break;
        default:
            // Code should never reach here.
            Log.e("Trigger detected on unhandled resource. Skipping.");
        }
    }

    @Override
    public void onGrabbedStateChange(View v, int handle) {
    }

    @Override
    public void onFinishFinalAnimation() {
    }

    private void displayIndicator() {
        Log.v("AlarmFullScreen displayIndicator");
        ImageView icon = (ImageView) findViewById(R.id.indicator_icon);
        TextView ticker = (TextView) findViewById(R.id.indicator_text);
        icon.setVisibility(View.VISIBLE);
        ticker.setVisibility(View.VISIBLE);
        Configuration conf = getResources().getConfiguration();
        if (conf.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ticker.setText(mContext.getString(R.string.alarm_command_summary_format_land,
                    mKeywordArray[0], mKeywordArray[1]));
        } else {
            ticker.setText(mContext.getString(R.string.alarm_command_summary_format,
                    mKeywordArray[0], mKeywordArray[1]));
        }
    }
    
    //aoran add
    public void resetZ() {
    	z = 0;
    	az = 0;
    	faceUp = false;
    }
    private void snooze() {
        Alarms.snooze(this, mAlarm);
        finish();
    }
    //aoran add end
}
