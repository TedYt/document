package com.test.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class KillProcessService extends Service {

	private static final String TAG = "Test/KillProcessService";
	
	private Timer mTimer;
	public MyHandler myHandler;
	
	private boolean mFlag = false;
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
		if (mTimer == null){
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new MyTimerTask(), 0, 3000);
		}
		
		if (myHandler == null){
			myHandler = new MyHandler();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		
		if (mTimer != null){
			mTimer.cancel();
		}
		if (myHandler != null){
			myHandler.removeMessages(1000);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG, "onStart");
		mFlag = false;
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
	}
	
	public void killCertainProcess() {
    	Context c = getApplicationContext();
    	ActivityManager am = (ActivityManager)c.getSystemService(ACTIVITY_SERVICE);
    	List<ActivityManager.RunningAppProcessInfo> runAppList = am.getRunningAppProcesses();
		String processName = runAppList.get(0).processName;
		int pid = runAppList.get(0).pid;
		log("RunningAppProcess name " + processName + ", pid " + pid);
		if (processName!=null && processName.equals("com.linecorp.LGCOOKIE")){
		    log("!!! It's going to kill " + processName);
			Message msg = myHandler.obtainMessage();
			msg.arg1 = pid;
			msg.obj = processName;
			msg.what = 1000;
			myHandler.sendMessageDelayed(msg, 1500);
		}
	}
    
    private String killProcess(int pid, String pkgname) {
    	String result = null;
    	//mFlag = false;
    	Context c = getApplicationContext();
    	ActivityManager am = (ActivityManager)c.getSystemService(ACTIVITY_SERVICE);
    	try {
			Log.w(TAG, "!!! kill " + pkgname + "!!!!!");
			Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
			forceStopPackage.setAccessible(true);
			forceStopPackage.invoke(am, pkgname);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    	
    	return result;
	}
    
    private boolean isNetworkConnected(){
        Log.d(TAG," isNetworkConnected enter");
        ConnectivityManager connMgr =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        TelephonyManager mTelephony =
                (TelephonyManager)getSystemService(TELEPHONY_SERVICE); 
        
        NetworkInfo info = connMgr.getActiveNetworkInfo();
        if (info == null || !connMgr.getBackgroundDataSetting()){
            Log.d(TAG," isNetworkConnected 0");
            return false;
        }
        
        int netType = info.getType();
        int netSubtype = info.getSubtype();
        
        if (netType == ConnectivityManager.TYPE_WIFI) {
            Log.d(TAG," isNetworkConnected TYPE_WIFI");
            return info.isConnected();
        }else if (netType == ConnectivityManager.TYPE_MOBILE
                    && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS
                    && !mTelephony.isNetworkRoaming()){
            Log.d(TAG," isNetworkConnected TYPE_MOBILE");
            return info.isConnected();
        }else {
            Log.d(TAG," isNetworkConnected null");
            return false;
        }
    }
    
	
	private void log(String msg){
		Log.d(TAG, msg);
	}

	class MyHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
    		int pid = (int)msg.arg1;
			String pkgname = (String)msg.obj;
			if (!isNetworkConnected()){
			    //mFlag = true;
				String result = killProcess(pid, pkgname);
				log("result = " + result);
				
				Toast toast = Toast.makeText(getApplicationContext(), 
						R.string.no_network_hint, Toast.LENGTH_SHORT);
				toast.show();
			}else {
			    log("Network was connected, not to kill " + pkgname);
			}
		}
	}
	
	class MyTimerTask extends TimerTask{

		@Override
		public void run() {
			killCertainProcess();
		}
	}
}
