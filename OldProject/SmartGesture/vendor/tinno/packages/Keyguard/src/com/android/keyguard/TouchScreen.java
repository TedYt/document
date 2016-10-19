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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
//add by zzp
import android.gesture.Gesture; 
import android.gesture.GestureLibraries; 
import android.gesture.GestureLibrary; 
import android.gesture.GestureOverlayView; 
import android.gesture.GestureOverlayView.OnGesturePerformedListener; 
import android.gesture.Prediction;
import java.util.ArrayList;
import android.opengl.GLSurfaceView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.ImageButton;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.app.ActivityManagerNative;
import android.os.RemoteException;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.view.MotionEvent;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
//add by zzp end

//aoran add power unlock.
//import com.android.internal.policy.impl.keyguard.KeyguardSecurityModel;
//import com.android.internal.policy.impl.keyguard.KeyguardSecurityModel.SecurityMode;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//import android.app.KeyguardManager.KeyguardLock;
import android.app.KeyguardManager;
import android.app.IActivityManager;
import android.content.ContentResolver;
import android.provider.Settings;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.widget.LockPatternUtils;


public class TouchScreen extends LockScreenLayout implements KeyguardSecurityView/*, MyGestureOverlayView.onTouchInterface*/ {
    private static final boolean DEBUG = KeyguardHostView.DEBUG;
    private static final String TAG = "TouchScreen";
	
	private static final int MSG_UPDATE_RIGHT_RESOURCE = 100;
	private static final int MSG_RUN_ANIMATION = 101;

	private LockPatternUtils mLockPatternUtils;
	//private KeyguardUpdateMonitor mUpdateMonitor;

	//protected OnDismissAction mDismissAction;
	private Context mContext;
	//private Handler mHandler = new Handler();

	//int mBatteryLevel = 0;
	//boolean mPluggedIn = false;
	//boolean isCharging = false;

	//private Calendar mCalendar;
	//private Time TIME = new Time();
	//private static final String mFormat = "HH:mm";

	//private boolean mSilentMode;
	//private AudioManager mAudioManager;

	//private TinnoSlidingTab mSelector;
	//private RelativeLayout mProgressBar;
	
//	private ImageView mMoveToRight = null;
//	private ImageView mMoveToLeft = null;
	
	//private TextView mMessageText = null;
	
	//private TextView weekDayView = null;
	//private TextView ampmView = null;
	private final String DATEFORMAT = "dd/MM/yyyy";
	
	//private RelativeLayout charge_layout;	
	
	//private ImageView mGestureImage;
	//private WindowManager mWindowManager;
	//private WindowManager.LayoutParams mParams;
	//private TelephonyManager mTelephonyManager;
	/// @} end modify
	
	private ImageView mMoveAnimation = null;
	
	private WindowManager wm;
	
	private int mScreenWidth = 1080;
//add by zzp
	private TouchSurfaceView mSurface;
	//private MyGestureOverlayView gestureView;
	//private GestureLibrary gLibrary;
	//private boolean loadState;
	//private MyRenderer mRender;
	//private MyOnGesturePerformedListener mGestureListener;
	private MyRenderer mRenderer;
	/*private int mGestureTimes = 0;
	private double mLength = 0.0;
	private float mPrevX;
	private float mPrevY;
	private boolean mSpeedUnlock = false;
	//private int mWidth;
	//private int mHeight;
	private static boolean slidingDrawerOpened = false;
	
	private final KeyguardActivityLauncher mActivityLauncher = new KeyguardActivityLauncher() {

        @Override
        KeyguardSecurityCallback getCallback() {
            return mCallback;
        }

        @Override
        LockPatternUtils getLockPatternUtils() {
            return mLockPatternUtils;
        }

        @Override
        Context getContext() {
            return mContext;
        }};

	@Override
	public void onTouchInterface() {
		// TODO Auto-generated method stub
		Log.e("interface", "onTouchInterface()");
		if (mCallback != null) {
			mCallback.userActivity(0);
		}
	}

	@Override
	public void onSpeedUnlock(float x, float y) {
		// TODO Auto-generated method stub
		Log.e("interface", "onSpeedUnlock()");
		//mSpeedUnlock = true;
		NativeMethod.setpointup(x, y);
		gestureView.setCanTouch(false);
		mHandler.postDelayed(new Runnable() {
		public void run() {
			if (mCallback != null) {
				mCallback.userActivity(0);
				mCallback.dismiss(false);
			}
		}
		}, 300);
	}*/
//add by zzp end

    //aoran add power unlock.@{
    private float mPromixyValueNow = 1.0f;
    private SensorManager mSensorManager;
    private Sensor mPromixySensor;
    private Handler mHandler = new Handler();
    private ContentResolver cr;
    PowerManager pm;
    boolean bPowerKeyUnlockOn;
    private final SensorEventListener mProximitySensorListener;
    /// end@}

	public TouchScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;

		//wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE); 
		//mScreenWidth = wm.getDefaultDisplay().getWidth();
		//Log.d(TAG, "mScreenWidth = "+mScreenWidth);
        /// M: yuanyuan.wang modify for Proximity Unlock @{

		//mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		


	/*// �������ο����GestureLibrary 
        gLibrary = GestureLibraries.fromRawResource(mContext, R.raw.gestures); 
       // �������ƿ���Դ 
       loadState = gLibrary.load(); 
	 mGestureListener = new MyOnGesturePerformedListener();*/

	 mRenderer = new MyRenderer(mContext);
        //aoran add power unlock.@{
        cr = mContext.getContentResolver();
        pm =(PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        bPowerKeyUnlockOn = 1==Settings.Secure.getInt(cr,
                Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED,
                Settings.Secure.PROMIXY_POWERKEY_UNLOCK_ENABLED, 0) ;
        mProximitySensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    Log.v("aoran", "distance centimeters=" + event.values[0]);
                    mPromixyValueNow = event.values[0];
                    Log.d("TAG_PRM","***aoran***onSensorChaged mPromixyValueNow="+mPromixyValueNow);
                    if (mPromixyValueNow == 0.0f) {
                        Log.d("aoran","mPromixyValueNow locked");
                        return;
                    }
                    else{
                        if (mCallback != null) {
                            mCallback.userActivity(0);
                            mCallback.dismiss(false);
                            Log.d("aoran","mPromixyValueNow unlock OK");
                        }
                    }

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not used.
            }
        };
        /// end@}

	//surface = new TouchSurfaceView(mContext);
        //surface.setRenderer(new MyRenderer(mContext));
	//WindowManager.LayoutParams lparams;
	//lparams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    	
    	//mWindowManager.addView(surface, mParams);
		/// @} end modify
	}

	public TouchScreen(Context context) {
		this(context, null);
	}


	/*boolean startActivity(Intent intent, Object tag) {

	        intent.setFlags(
	                    Intent.FLAG_ACTIVITY_NEW_TASK
	                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
	                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);

	        try {
	            mContext.startActivity(intent);
		     mHandler.postDelayed(new Runnable() {
				public void run() {
					if (mCallback != null) {
						mCallback.userActivity(0);
						mCallback.dismiss(false);
					}
				}
			}, 300);
	            return true;
	        } catch (SecurityException e) {
	            Log.e(TAG, "Does not have the permission to launch " + intent +
	                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
	                    "or use the exported attribute for this activity. "
	                    + "tag=" + tag + " intent=" + intent, e);
	        }
	        return false;
    	}
	
	public boolean startActivitySafely(Intent intent, Object tag) {
	        boolean success = false;
	        try {
	            success = startActivity(intent, tag);
	        } catch (ActivityNotFoundException e) {
	            Log.e(TAG, "Unable to launch. tag=" + tag + " intent=" + intent, e);
	        }
	        return success;
    	}
    	
    	//private float mGestureEndX, mGestureEndY;
	private final class MyOnGestureListener implements OnGestureListener{

		@Override
		public void onGesture(GestureOverlayView arg0, MotionEvent event) {
			// TODO Auto-generated method stub
			//Log.e("gesture", "onGesture");
			mGestureTimes++;
			float x,y;
			x = event.getX();
			y = event.getY();
			//double d 
			mLength += Math.sqrt((x-mPrevX)*(x-mPrevX)+(y-mPrevY)*(y-mPrevY));
			mPrevX = x;
			mPrevY = y;
			Log.e("gesture", "onGesture mGestureTimes = "+mGestureTimes+", mLength = "+mLength);
			//mPrevX = event.getX();
		}

		@Override
		public void onGestureCancelled(GestureOverlayView arg0, MotionEvent arg1) {
			// TODO Auto-generated method stub
			//Log.e("gesture", "onGestureCancelled");
		}

		@Override
		public void onGestureEnded(GestureOverlayView gesture, MotionEvent event) {
			// TODO Auto-generated method stub
			//mGestureTimes++;
			float x,y;
			x = event.getX();
			y = event.getY();
			//double d 
			mLength += Math.sqrt((x-mPrevX)*(x-mPrevX)+(y-mPrevY)*(y-mPrevY));
			Log.e("gesture", "onGestureEnded mGestureTimes = "+mGestureTimes+", mLength = "+mLength+", gestureView mWidth = "+mScreenWidth);
			if(mLength >= 600.0) {//mGestureTimes >= 5) {
				//float x,y;
				//mGestureEndX = event.getX();
				//mGestureEndY = event.getY();
				NativeMethod.setpointup(x, y);
				gestureView.setCanTouch(false);
				mGestureListener.onGesturePerformed(gesture, gesture.getGesture());
			}
			else {
				NativeMethod.hidesomething();
			}
		}

		@Override
		public void onGestureStarted(GestureOverlayView arg0, MotionEvent event) {
			mGestureTimes = 0;
			mLength = 0.0;
			//mWidth = gestureView.getWidth();
			//mHeight = gestureView.getHeight();
			// TODO Auto-generated method stub
			//Log.e("gesture", "onGestureStarted");
			mPrevX = event.getX();
			mPrevY = event.getY();
		}
		 
	 }
	private final class MyOnGesturePerformedListener implements
                        OnGesturePerformedListener { 
  
                public void onGesturePerformed(GestureOverlayView overlay, 
                                Gesture gesture) { 
                        if (loadState) {//����������Դ�ɹ� 
                                // ��ȡ����ͼ�ν���ƥ�䣬ƥ��̶Ⱦ���Prediction�е�score 
                                ArrayList<Prediction> predictions = gLibrary.recognize(gesture); 
  
                                if (!predictions.isEmpty()) {// ����û�����ͼ�Σ��ͻ�ƥ�� 
  
                                        Prediction prediction = predictions.get(0); 
                                        Log.i("gesture", String.valueOf(prediction.score)); 
                                        if (prediction.score > 3) {// �ж����ƶȴ���1������������߽���ƥ�� 
                                                if ("phone".equals(prediction.name) || "phone2".equals(prediction.name)) {//�ر� 
                                                	Log.e("gesture", "phone phone phone");
								//NativeMethod.setpointup(mGestureEndX, mGestureEndY);
								Intent intent = new Intent();
						            	intent.setComponent(new ComponentName("com.android.dialer","com.android.dialer.DialtactsActivity"));
								startActivitySafely(intent, "phone");
						            	
                                                } else if ("msg".equals(prediction.name) || "msg2".equals(prediction.name) || "msg3".equals(prediction.name)) {
			                            	Log.e("gesture", "msg msg msg");
								//NativeMethod.setpointup(mGestureEndX, mGestureEndY);
								Intent intent = new Intent();
						            	intent.setComponent(new ComponentName("com.android.mms","com.android.mms.ui.ConversationList"));
								startActivitySafely(intent, "msg");
								//mActivityLauncher.launchActivity(intent, false, true, null, null);
						            	

			                            } else if ("music".equals(prediction.name)) {
			                          		Log.e("gesture", "music music music");
								//NativeMethod.setpointup(mGestureEndX, mGestureEndY);
								Intent intent = new Intent();
						            	intent.setComponent(new ComponentName("com.android.music","com.android.music.MusicBrowserActivity"));
								startActivitySafely(intent, "music");
			                            } else if ("camera".equals(prediction.name)) {
								//intent.setComponent(new ComponentName("com.android.gallery3d", "com.android.camera.CameraLaunch"));
								//NativeMethod.setpointup(mGestureEndX, mGestureEndY);
								mActivityLauncher.launchCamera(null, null);
								mHandler.postDelayed(new Runnable() {
									public void run() {
										if (mCallback != null) {
											mCallback.userActivity(0);
											mCallback.dismiss(false);
										}
									}
								}, 250);
                                                	Log.e("gesture", "camera camera camera");
                                                } else if ("web".equals(prediction.name) || "web2".equals(prediction.name)) {
                                                	//NativeMethod.setpointup(mGestureEndX, mGestureEndY);
                                                	Intent intent = new Intent();
						            	intent.setComponent(new ComponentName("com.android.browser","com.android.browser.BrowserActivity"));
								startActivitySafely(intent, "web");
			                          		Log.e("gesture", "web web web");
			                            } else {//if (mGestureTimes >= 20){
                                                	Log.e("gesture", "no match unlock screen");
								//NativeMethod.setpointup(mGestureEndX, mGestureEndY);
								mHandler.postDelayed(new Runnable() {
									public void run() {
										if (mCallback != null) {
											mCallback.userActivity(0);
											mCallback.dismiss(false);
										}
									}
								}, 250);
                                                }// else {
                                                //	Log.e("gesture", "hidesomething11111111111");
                                                //	NativeMethod.hidesomething();
                                                //}
                                        } else{// if (mGestureTimes >= 20)  {// ���ƶ�С��1����ʶ�� 
                                                Log.e("gesture", "no prediction !!!");
							//NativeMethod.setpointup(mGestureEndX, mGestureEndY);
							mHandler.postDelayed(new Runnable() {
								public void run() {
									if (mCallback != null) {
										mCallback.userActivity(0);
										mCallback.dismiss(false);
									}
								}
							}, 250);
                                        }// else {
                                        //	Log.e("gesture", "hidesomething22222222");
                                        //	NativeMethod.hidesomething();
                                        //}
                                } else {//û�л�ͼ��  
                                        Log.e("gesture", "not recognize@@@");
                                } 
                        } else { 
                                Log.e("gesture", "no loading$$$");
                        } 
                } 
        }*/
	

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Log.d(TAG,"onAttachedToWindow@@@@@@@@@@@@zzp");
		if(mSurface != null) {
			mSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);//add by zzp
		}
		//KeyguardUpdateMonitor.getInstance(mContext).registerCallback(
		//		mUpdateMonitorCallbacks);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Log.d(TAG,"onDetachedFromWindow@@@@@@@@@@@@zzp");
		//KeyguardUpdateMonitor.getInstance(mContext).removeCallback(
		//		mUpdateMonitorCallbacks);
	}

	@Override
	protected void onFinishInflate() {
		Log.d(TAG,"onFinishInflate------------------BEGIN");
		initView();
		Log.d(TAG,"onFinishInflate------------------END");
	}

	private void initView() {

//add by zzp
		mSurface = (TouchSurfaceView)findViewById(R.id.surface);
		if(mSurface != null) {
			mSurface.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
			mSurface.setRenderer(mRenderer);
			//mSurface.setZOrderMediaOverlay(true);
			mSurface.setZOrderOnTop(true);
			 
			mSurface.setWindowFlag(mSurface.getWindowFlag() & (~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM) );
			
			mSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
			mSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		}
		/*gestureView = (MyGestureOverlayView)findViewById(R.id.myGesture);
		if(gestureView != null) {
			//gestureView.addOnGesturePerformedListener(mGestureListener);
			gestureView.addOnGestureListener(new MyOnGestureListener());
			gestureView.setTouchInterfaceLisen(this);
		}
		//gestureView.setTouchInterfaceLisen(this);

		final ImageButton ib = (ImageButton)findViewById(R.id.handle_arrow);
		SlidingDrawer sd = (SlidingDrawer)findViewById(R.id.drawer);
		if(slidingDrawerOpened) {
			sd.open();
			ib.setBackgroundResource(R.drawable.gesturearrow);
		}
		sd.setOnDrawerCloseListener(new OnDrawerCloseListener() {
			
			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				if (mCallback != null) {
					mCallback.userActivity(0);
				}
				slidingDrawerOpened = false;
				ib.setBackgroundResource(R.drawable.gesturearrow2);
			}
		});
		sd.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			
			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				if (mCallback != null) {
					mCallback.userActivity(0);
				}
				slidingDrawerOpened = true;
				ib.setBackgroundResource(R.drawable.gesturearrow);
			}
		});*/
//add by zzp end
	}


	/*private KeyguardUpdateMonitorCallback mUpdateMonitorCallbacks = new KeyguardUpdateMonitorCallback() {
		void onRefreshBatteryInfo(BatteryStatus status) {
			mBatteryLevel = status.level;
			mPluggedIn = status.isPluggedIn();
			isCharging = status.isDeviceCharging();
			Log.d("TinLock", TAG
					+ ",onRefreshBatteryInfo------>mBatteryLevel = "
					+ mBatteryLevel + ", mPluggedIn = " + mPluggedIn
					+ ", isCharging = " + isCharging);
			updateChargeStatus();
		}

		void onTimeChanged() {
			Log.d("TinLock", TAG + ", onTimeChanged-------------updateTime");
			upateTime();
		}

	};*/

	private static final String IS_NOW_ON_WORKSPACE = "is_now_on_workspace";
	
	private KeyguardSecurityCallback mCallback;/* = new KeyguardSecurityCallback() {

		@Override
		public void dismiss(boolean securityVerified) {
			
			//  修改解锁完瞬间到桌面钱，StatusBAR会闪一下黑色的问题
            Settings.System.putInt(mContext.getContentResolver(),IS_NOW_ON_WORKSPACE,1);
            //  END .
            
			// TODO Auto-generated method stub
			KeyguardUpdateMonitor.getInstance(mContext)
					.setAlternateUnlockEnabled(true);
			boolean deferKeyguardDone = false;
			if (mDismissAction != null) {
				deferKeyguardDone = mDismissAction.onDismiss();
				mDismissAction = null;
			}
			if (mViewMediatorCallback != null) {
				if (deferKeyguardDone) {
					mViewMediatorCallback.keyguardDonePending();
				} else {
					mViewMediatorCallback.keyguardDone(true);
				}
			}
		}

		@Override
		public void userActivity(long timeout) {
			// TODO Auto-generated method stub
			if (mViewMediatorCallback != null) {
				mViewMediatorCallback.userActivity(timeout);
			}
		}

		@Override
		public boolean isVerifyUnlockOnly() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void reportSuccessfulUnlockAttempt() {
			// TODO Auto-generated method stub

		}

		@Override
		public void reportFailedUnlockAttempt() {
			// TODO Auto-generated method stub

		}

		@Override
		public int getFailedAttempts() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void showBackupSecurity() {
			// TODO Auto-generated method stub

		}

		@Override
		public void setOnDismissAction(OnDismissAction action) {
			//TouchScreen.this.setOnDismissAction(action);
		}

		@Override
		public boolean hasOnDismissAction() {
			//return TouchScreen.this.hasOnDismissAction();
			return false;
		}

		@Override
		public void updateKeyguardLayerVisibility(boolean visible) {
			// TODO Auto-generated method stub

		}

		@Override
		public void updateClipChildren(boolean clipChildren) {
			// TODO Auto-generated method stub

		}

		@Override
		public void updateWidgetContainerInteractive(boolean interactive) {
			// TODO Auto-generated method stub

		}
	};*/

	/**
	 * Sets an action to perform when keyguard is dismissed.
	 * 
	 * @param action
	 */
	/*protected void setOnDismissAction(OnDismissAction action) {
		mDismissAction = action;
	}

	protected boolean hasOnDismissAction() {
		return mDismissAction != null ? true : false;
	}*/

	/*public void updateChargeStatus() {
		if (mProgressBar != null) {
			if (mPluggedIn && isCharging) {
				mProgressBar.setVisibility(View.VISIBLE);
//				mProgressBar.setProgress(mBatteryLevel);
				updateProgressBar(mBatteryLevel * mScreenWidth / 100);
				if (mBatteryLevel == 100) {
					mMessageText.setText(R.string.lockscreen_charged);
				}else {
					String chargeLevel = mContext.getString(R.string.slider_screen_plugged_in, mBatteryLevel);
					mMessageText.setText((CharSequence)chargeLevel);
				}
			}else {
				mProgressBar.setVisibility(View.GONE);
				mMessageText.setText(R.string.slider_screen_slide);
			}
		}
		
	}*/
	

	/*@Override
	public void reset() {
		Log.d(TAG,"reset");
	}*/

	/*@Override
	public void onScreenTurnedOff() {
		Log.d(TAG,"onScreenTurnedOff");

		//mSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		mRenderer.setScreenOn(false);
		/// @} end modify
	}

	@Override
	public void onScreenTurnedOn() {
		Log.d(TAG+", Liang","onScreenTurnedOn");

		mSurface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		mRenderer.setScreenOn(true);
	}*/

	/*@Override
	public void show() {
		Log.d(TAG,"show");
		System.out.println("show-->");
	}*/

	/*@Override
	public void wakeWhenReadyTq(int keyCode) {
		Log.d(TAG,"wakeWhenReadyTq");
		if (mViewMediatorCallback != null) {
			mViewMediatorCallback.wakeUp();
		}
	}*/

	/*@Override
	public void verifyUnlock() {
		Log.d(TAG,"verifyUnlock");
		//JUNMING.LIANG ADD FOR BUG:SWLAL-935
        if (mViewMediatorCallback != null) {
            mViewMediatorCallback.keyguardDone(true);
        }
        //JUNMING.LIANG END
	}

	@Override
	public void cleanUp() {
		Log.d(TAG,"cleanUp");
		mLockPatternUtils = null;
		mUpdateMonitor = null;
		mCallback = null;

	NativeMethod.clearScreen();
	if(mSurface != null) {
		mSurface.destroySurfaceview();
	}
	}

	@Override
	public long getUserActivityTimeout() {
		Log.d(TAG,"getUserActivityTimeout");
		return 0;
	}*/

	/*@Override
	public boolean isAlarmUnlockScreen() {
		Log.d(TAG,"isAlarmUnlockScreen");
		return false;
	}*/

	private static final String LOW_SENSITIVE_DEGREE = "1";

	public void setKeyguardCallback(KeyguardSecurityCallback callback) {
	        mCallback = callback;
	        /// M: update visibility immediately after set callback
	        mCallback.updateKeyguardLayerVisibility(true);
    	}
	
	@Override
    	public void hideBouncer(int duration) {
		
    	}
	@Override
    	public void showBouncer(int duration) {
    		
    	}

	@Override
    	public void showUsabilityHint() {
		
    	}

	@Override
    	public KeyguardSecurityCallback getCallback() {
        	return mCallback;
    	}

	@Override
    	public boolean needsInput() {
        	return false;
    	}

    	@Override
    	public void onPause() {
            KeyguardUpdateMonitor.getInstance(mContext).removeCallback(mUpdateCallback);

            Log.d(TAG,"onPause");
		if(mSurface != null) {
			mSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);//add by zzp
		}
		//mRenderer.setScreenOn(false);
     	}

    @Override
    public void onResume(int reason) {
        Log.d(TAG,"onResume");
	    if(mSurface != null) {
		    mSurface.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
	    }

        ///aoran add promixySensor for unlock.{
        KeyguardUpdateMonitor.getInstance(mContext).registerCallback(mUpdateCallback);
        boolean screen = pm.isScreenOn();
        Log.d("aoran","onResume screen="+screen);

        if(bPowerKeyUnlockOn && screen){
            Log.d("aoran","registerListener");
            mSensorManager.registerListener(mProximitySensorListener, mPromixySensor, SensorManager.SENSOR_DELAY_NORMAL);
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    Log.d("aoran","unregisterPromixyListener!!!!!");
                    mSensorManager.unregisterListener(mProximitySensorListener);
                }
            },500);
        }
        /// end @}

	//mRenderer.setScreenOn(true);
    }

    @Override
    public void reset() {
    	Log.d(TAG,"reset");
    }
	
    public void setLockPatternUtils(LockPatternUtils utils) {
        mLockPatternUtils = utils;
    }

    ///aoran add promixySensor for unlock.{
    KeyguardUpdateMonitorCallback mUpdateCallback = new KeyguardUpdateMonitorCallback() {

        @Override
        public void onDevicePolicyManagerStateChanged() {
            updateTargets();
        }

        @Override
        public void onSimStateChanged(State simState, int simId) {
            updateTargets();
        }

        @Override
        public void onDockStatusUpdate(int dockState) {
            if(dockState == 1) {
                mCallback.dismiss(false);
            }
        }
    };

    private void updateTargets(){
        //aoran add power unlock
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mPromixySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    }

    /// end @}

}

