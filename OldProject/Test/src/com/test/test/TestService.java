package com.test.test;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.EventLog;
import android.util.EventLog.Event;
import android.util.Log;

public class TestService extends Service {

	private static final String TAG = "TestService";
	
	private H mH;
	
	private Timer mTimer; 
	
	private ActivityChangeListener mListener;
	
	public interface ActivityChangeListener{
		public void activityChanged(String topActivity);
	}
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		log("onBind");
		return null;
	}

	@Override
	protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
		// TODO Auto-generated method stub
		super.dump(fd, writer, args);
	}

	@Override
	public void onCreate() {
		log("onCreate");
		mH = new H();
		mTimer = new Timer();
	}
	
	public void setOnActivityChangedListener(ActivityChangeListener listener){
		if (mListener == null){
			mListener = listener;
		}
	}

	private void analysisLog() {
		log("analysisLog");
		Context c = getApplicationContext();
		ActivityManager am = (ActivityManager)c.getSystemService(ACTIVITY_SERVICE);
//		List<ActivityManager.RunningAppProcessInfo> amLists = am.getRunningAppProcesses();
//			log(amLists.get(0).processName.toString());
		List<ActivityManager.RunningTaskInfo> amLists = am.getRunningTasks(1);
		String baseActivityName = amLists.get(0).baseActivity.getClassName();
		int activitiesCount = amLists.get(0).numActivities;
		String topActivity = amLists.get(0).topActivity.getClassName();
		if (mListener != null){
			log("mListener != null");
			mListener.activityChanged(topActivity);
		}
		CharSequence description = amLists.get(0).description;
		log("baseActivityName = " + baseActivityName + ", activitiesCount = " + 
					activitiesCount + ", topActivity = " + topActivity + "\n");
			
			
			
		/*try {
			int tagCode = EventLog.getTagCode("am_proc_start");
			Collection<EventLog.Event> output = new ArrayList<EventLog.Event>();
			EventLog.readEvents(new int[] {tagCode}, output);
		
			if (output.size() <= 0){
				log("output.size() <= 0");
			}
			
			for(Event event : output){
				Object[] objects = (Object[])event.getData();
				ComponentName componentName = ComponentName.unflattenFromString(objects[4].toString());
				
				log("packageName = " + componentName.getPackageName());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log("IOException e " + e.getMessage());
			e.printStackTrace();
		}*/
	}

	@Override
	public void onDestroy() {
		log("onDestroy");
		if (mTimer != null){
			mTimer.cancel();
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		log("onStart");
		
		if (mTimer == null){
			mTimer = new Timer();
		}
		
		mTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				analysisLog();
			}
		}, 0, 500);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		log("onUnbind");
		return super.onUnbind(intent);
	}
	
	private void log(String msg){
		Log.d(TAG, msg);
	}

	class H extends Handler{

		@Override
		public void handleMessage(Message msg) {
			analysisLog();
		}
		
	}
}
