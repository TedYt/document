package com.test.test;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TestBroadcast extends BroadcastReceiver {

	private final static String TAG = "Test/TestBroadcast";
	
	@Override
	public void onReceive(Context c, Intent i) {
		String action = i.getAction();
		
		if (action.equals(Intent.ACTION_PACKAGE_ADDED)){
			String pckname = i.getDataString();
			Intent intent = new Intent("test.kill.process");
			c.startService(intent);
			Log.d(TAG, "install pck " + pckname);
		}else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)){
			String pckname = i.getDataString();
			Intent intent = new Intent("test.kill.process");
			c.stopService(intent);
			Log.d(TAG, "removed pck " + pckname);
		}
	}
}
