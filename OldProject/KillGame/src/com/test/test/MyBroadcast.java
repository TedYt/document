package com.test.test;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class MyBroadcast extends BroadcastReceiver {

	private final static String TAG = "Test/TestBroadcast";
	
	private final static String TARGET_PACKAGE = "package:com.linecorp.LGCOOKIE";
	
	private final static String SERVER_ACTION = "test.kill.process";
	
	@Override
	public void onReceive(Context c, Intent i) {
		String action = i.getAction();
		
		if (action.equals(Intent.ACTION_PACKAGE_ADDED)){
			String pckname = i.getDataString();
			if (TARGET_PACKAGE.equals(pckname)){
    			Intent intent = new Intent(SERVER_ACTION);
    			c.startService(intent);
			}
			Log.d(TAG, "install pck " + pckname);
		}else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)){
			String pckname = i.getDataString();
			if (TARGET_PACKAGE.equals(pckname)){
    			Intent intent = new Intent(SERVER_ACTION);
    			c.stopService(intent);
			}
			Log.d(TAG, "removed pck " + pckname);
		}
	}
}
