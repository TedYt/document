package com.android.settings;

import android.app.Activity;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

public class TestAudioCalibration extends Activity {
	
	TextView mData1;
	TextView mData2;
	TextView mResult;
	Resources mRes;
	
	TestAudioCalibrationLib mTacLib;
	
	float mD1 = 1.0f;
	float mD2 = 2.0f;
	
	private MediaPlayer mMediaPlayer;
	
	private static boolean isRunning = false;
	
	private Runnable myRunable = new Runnable() {
		
		public void run() {
			isRunning = true;
			int[] datas = mTacLib.tfa9890reCalibration(mD1, mD2);//call native method
			Message msg = myHandler.obtainMessage();
			msg.obj = datas;
			msg.sendToTarget();
			isRunning = false;
		}
	};
	
	private Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			int[] datas = (int[])msg.obj;
			
			for (int i=0; i < datas.length; i++){
				Log.d("tui", "data_" + i + "= " + datas[i]);
			}
			
			float data1 = datas[0] * 1.0f / 10000;
			mData1.setText(mRes.getString(R.string.test_audio_calibration_data1, data1 +""));// 
			float data2 = datas[1] * 1.0f / 10000;
			mData2.setText(mRes.getString(R.string.test_audio_calibration_data2, data2 +""));//
			
			if (datas[2] == 1){
				mResult.setText("Result : Pass");
			}else {
				mResult.setText("Result : Fail");
			}
		}
		
	};
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.test_audio_calibration);
		mData1 = (TextView)findViewById(R.id.test_ac_data1);
		mData2 = (TextView)findViewById(R.id.test_ac_data2);
		mResult = (TextView)findViewById(R.id.test_ac_result);
		mRes = getResources();
		
		mTacLib = new TestAudioCalibrationLib();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("tao","onresume...");
		
		mData1.setText(mRes.getString(R.string.test_audio_calibration_data1, "..."));// 
		mData2.setText(mRes.getString(R.string.test_audio_calibration_data2, "..."));//
		
		mResult.setText("Result : ...");
		mResult.postDelayed(myRunable, 300);
		playRingtone();
	}
	
	private void playRingtone() {
		try{
			Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(this, alert);
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			mMediaPlayer.setLooping(true);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if (isRunning){
				Toast.makeText(this, "Task is running, pleas wait...", Toast.LENGTH_SHORT);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		
		mTacLib.goBack();//call native method
		
		try{
			if (mMediaPlayer != null){
				Log.d("tui", "mMediaPlayer != null");
				if(mMediaPlayer.isPlaying()){
					mMediaPlayer.stop();
					Log.d("tui", "mMediaPlayer stop");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		super.onDestroy();
	}
	
	
}
