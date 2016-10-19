package com.zale.ydan;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class ColoredEggActivity extends Activity {

	private final int MSG_HIDE_FIRST_HINT = 1000;
	private final int MSG_SHOW_SECOND_HINT = 1001;
	
	private final int TIME_MSG = 2000;
	
	private H mH;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ce_main);
		
		mH = new H();
		mH.sendEmptyMessageDelayed(MSG_HIDE_FIRST_HINT, TIME_MSG);
		mH.sendEmptyMessageDelayed(MSG_SHOW_SECOND_HINT, TIME_MSG + 500);
	}
	
	public void hideFirstHint() {
		View view = findViewById(R.id.start_hint);
		view.setVisibility(View.INVISIBLE);
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_out);
		view.startAnimation(anim);
	}
	
	public void showSecondHint() {
		View view = findViewById(R.id.second_hint);
		view.setVisibility(View.VISIBLE);
		
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.anim_fade_in);
		view.startAnimation(anim);
	}
	
	class H extends Handler{
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_HIDE_FIRST_HINT:
				hideFirstHint();
				break;
			case MSG_SHOW_SECOND_HINT:
				showSecondHint();
				break;
			default:
				break;
			}
		}
	}

	

	
}
