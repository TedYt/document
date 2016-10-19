package com.test.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class BootUpBroadcast extends BroadcastReceiver {
	private final static String TAG = "Test/BootUpBroadcast";
	
	private final static String SERVER_ACTION = "test.kill.process";
	
	@Override
	public void onReceive(Context c, Intent i) {
		String action = i.getAction();
		Log.d(TAG, "BootUpBroadcast");
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
			PackageManager pm = c.getPackageManager();
			try {
				pm.getPackageInfo("com.linecorp.LGCOOKIE", PackageManager.GET_ACTIVITIES);
				Log.d(TAG, "found pck com.linecorp.LGCOOKIE");
				Intent intent = new Intent(SERVER_ACTION);
    			c.startService(intent);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				Log.d(TAG, " !!! NOT found pck com.linecorp.LGCOOKIE");
			}
		}
	}
}
