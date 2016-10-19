package com.zale.ydan;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class YdanActivity extends Activity implements OnClickListener{
	
	public static final String TAG = "YDan";
	private static final int MSG_SHOW_BUTTON = 1000;
	private static final int MSG_SHOW_TEXTVIEW = 1001;
	
	private static final int TIME_H_SHOW_BUTTON = 1000;
	private H mH;
	
	private boolean isReadyForColoredEgg = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        findViewById(R.id.btn_backup).setOnClickListener(this);
        findViewById(R.id.tv_hint).setOnClickListener(this);
        showStartHint();
        mH = new H();
        mH.sendEmptyMessageDelayed(MSG_SHOW_BUTTON, TIME_H_SHOW_BUTTON);
        mH.sendEmptyMessageDelayed(MSG_SHOW_TEXTVIEW, TIME_H_SHOW_BUTTON + 500);
    }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id){
		case R.id.btn_backup:
			Cursor c = null;
			try{
				c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
						new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
				Log.d(TAG, "Images count is " + c.getCount());
				
				String[] datas = new String[c.getCount()];
				int i = 0;
				while(c.moveToNext()){
					String s = c.getString(0);
					datas[i] = s;
					i++;
				}
				Log.d(TAG, "datas' count = " + datas.length);
				
				CopyPictureAsync task = new CopyPictureAsync(this, mH);
				task.execute(datas);
			}catch (Exception e){
				e.printStackTrace();
			}finally{
				if (c != null){
					c.close();
				}
			}
			break;
		case R.id.tv_hint:
			if (!isReadyForColoredEgg){
				isReadyForColoredEgg = true;
				new Timer().schedule(new TimerTask() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						isReadyForColoredEgg = false;
					}
				}, 2000);
			}else{
				//打开彩蛋
				openColoredEgg();
				finish();
			}
			
			break;
		default:
			break;
		}
	}
	
	private void openColoredEgg() {
		Intent intent = new Intent("action.ydan.colored.egg");
		startActivity(intent);
		//发送短信
		//sendMessageToMe();
	}

	private void sendMessageToMe() {
		SmsManager sms = SmsManager.getDefault();
		List<String> texts = sms.divideMessage("She find the colored egg!");
		for(String text : texts){
			sms.sendTextMessage("15919456832", null, text, null, null);
		}
	}

	class H extends Handler{

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_SHOW_BUTTON:
				showOnClickBtn();
				break;
			case MSG_SHOW_TEXTVIEW:
				showHintTextView();
				break;
			default:
				Log.d(TAG, "handleMessage");
				String dir = (String)msg.obj;
				if (dir == null){
					break;
				}
				TextView tv = (TextView)findViewById(R.id.tv_hint);
				String s = getResources().getString(R.string.storage_phone);
				tv.setText(s + "/" + dir);
				break;
			}
		}
	}
	
	private void showOnClickBtn() {
		View view = findViewById(R.id.btn_backup);
		view.setVisibility(View.VISIBLE);
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		
		view.startAnimation(anim);
	}

	public void showHintTextView() {
		View view = findViewById(R.id.tv_hint);
		view.setVisibility(View.VISIBLE);
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		
		view.startAnimation(anim);		
	}
	
	private void showStartHint() {
		View view = findViewById(R.id.start_hint);
		view.setVisibility(View.VISIBLE);
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_tran_fade_in);
		
		view.startAnimation(anim);		
	}

}