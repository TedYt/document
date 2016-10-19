package com.android.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.mediatek.common.featureoption.FeatureOption;

import com.android.settings.accessibility.ToggleFeaturePreferenceFragment;
import com.android.settings.accessibility.ToggleSwitch;
import com.android.settings.accessibility.ToggleSwitch.OnBeforeCheckedChangeListener;
import android.provider.Settings;
import android.app.ActionBar;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.view.Gravity;
import android.view.View;
import android.app.ActionBar;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class GestureViewPager extends Activity {
	private static final String TAG = "GestureViewPager";
	private static final String STATES_KEY = "states";
	private static final int DEFAULT_TITLE_COLOR = 0xff000000;
	Context context = null;
	LocalActivityManager manager = null;
	ViewPager pager = null;
	TabHost tabHost = null;
	TextView gesturesSystem,gesturesCommunication;
	
	private float offset = 0;
	private int currIndex = 0;
	private float bmpW;
	private ImageView cursor;

	protected ToggleSwitch mToggleSwitch;
	protected String mPreferenceKey;

	protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
		Log.v("GES", "@@@onPreferenceToggled," + enabled);
		Settings.Global.putInt(getContentResolver(),
				Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED, enabled ? 1
						: 0);
		//setPreferenceEnable();
		updatePreference();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_viewpager);
		
//		ActionBar ab = getActionBar();
//        ab.hide();

		context = GestureViewPager.this;
		manager = new LocalActivityManager(this , false);
		Bundle states = (savedInstanceState != null) ? (Bundle) savedInstanceState.getBundle(STATES_KEY) : null;
		manager.dispatchCreate(states);
		
		onInstallActionBarToggleSwitch();
		
		final boolean gestureSettingsEnabled = Settings.Global.getInt(
				getContentResolver(),
				Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED, 0) == 1;
		mToggleSwitch.setCheckedInternal(gestureSettingsEnabled);
		

		InitImageView();
		initTextView();
		initPagerViewer();

	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Bundle state = manager.saveInstanceState();
	    if (state != null)
	    {
	    	outState.putBundle(STATES_KEY, state);
        }
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onResume() {
		manager.dispatchResume();
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		manager.dispatchPause(false);
		super.onPause();
	}
	
	private void initTextView() {
		int defaultTitleColor = DEFAULT_TITLE_COLOR;
		gesturesSystem = (TextView) findViewById(R.id.gesturesSystem);
		gesturesCommunication = (TextView) findViewById(R.id.gesturesCommunication);
		/*
		int playedColor = getResources().getThemeColor("theme_title_text_color");
		quickSettings.setTextColor(playedColor == 0 ? 0xff000000 : playedColor);
		generalSettings.setTextColor(playedColor == 0 ? 0xff000000 : playedColor);
        */
		gesturesSystem.setOnClickListener(new MyOnClickListener(0));
		gesturesCommunication.setOnClickListener(new MyOnClickListener(1));
		if (FeatureOption.MTK_THEMEMANAGER_APP) {
            defaultTitleColor = getResources().getThemeColor("theme_title_text_color");
            if (defaultTitleColor == 0) {
                defaultTitleColor = DEFAULT_TITLE_COLOR;
            }
        }
		gesturesSystem.setTextColor(defaultTitleColor);
		gesturesCommunication.setTextColor(defaultTitleColor);
	}
	
	private void initPagerViewer() {
		pager = (ViewPager) findViewById(R.id.viewpage);
		final ArrayList<View> list = new ArrayList<View>();
		//Intent intent = new Intent(context, GestureSettingsSystem.class);
		Intent intent = new Intent(context, GestureProfileSettingsSystem.class);
		list.add(getView("GestureProfileSettingsSystem", intent));//xitong   GestureSettingsCommunication
		//Intent intent2 = new Intent(context, GestureSettingsCommunication.class);
		Intent intent2 = new Intent(context, GestureProfileSettingsCommunication.class);
		list.add(getView("GestureSettingsCommunication", intent2));//tongxun  GestureSettingsCommunication

		pager.setAdapter(new MyPagerAdapter(list));
		pager.setCurrentItem(0);
		pager.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	
	private void InitImageView() {
		cursor = (ImageView) findViewById(R.id.cursor);
		
		Bitmap sourceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.roller);
		bmpW = sourceBitmap.getWidth();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		float screenW = dm.widthPixels;
		
		float sx = screenW/2/bmpW;
		Bitmap destBitmap = getScaleBitmap(sourceBitmap, sx, 1.0f);
		cursor.setImageBitmap(destBitmap);
		
		bmpW = screenW/2;
		offset = (screenW / 2 - bmpW) / 2;
		Matrix matrix = new Matrix();
		matrix.postTranslate(offset, 0);
		cursor.setImageMatrix(matrix);
	}

	private View getView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView(); 
	}

	public class MyPagerAdapter extends PagerAdapter{
		List<View> list =  new ArrayList<View>();
		public MyPagerAdapter(ArrayList<View> list) {
			this.list = list;
		}

		@Override
		public void destroyItem(ViewGroup container, int position,
				Object object) {
			ViewPager pViewPager = ((ViewPager) container);
			pViewPager.removeView(list.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			return list.size();
		}
		@Override
		public Object instantiateItem(View arg0, int arg1) {
			ViewPager pViewPager = ((ViewPager) arg0);
			pViewPager.addView(list.get(arg1));
			return list.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}
	
	public class MyOnPageChangeListener implements OnPageChangeListener {

		float one = offset * 2 + bmpW;
		float two = one * 2;

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = null;
			switch (arg0) {
			case 0:
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
				} 
				break;
			case 1:
				if (currIndex == 0) {
					animation = new TranslateAnimation(offset, one, 0, 0);
				} 
				break;
			}
			currIndex = arg0;
			animation.setFillAfter(true);
			animation.setDuration(200);
			cursor.startAnimation(animation);
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}
	}
	
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			pager.setCurrentItem(index);
		}
	};
	
	//getScaleBitmap
    public Bitmap getScaleBitmap(Bitmap sourceBitmap,float sx,float sy) {
    	int width = sourceBitmap.getWidth();
    	int height = sourceBitmap.getHeight();
    	
    	Matrix matrix = new Matrix();
    	matrix.preScale(sx, sy);
    	Bitmap mScaleBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, width, height, matrix, true);
    	
    	return mScaleBitmap;
    }


	protected void onInstallActionBarToggleSwitch() {
		mToggleSwitch = createAndAddActionBarToggleSwitch(this);
		
		mToggleSwitch
				.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
					@Override
					public boolean onBeforeCheckedChanged(
							ToggleSwitch toggleSwitch, boolean checked) {
						toggleSwitch.setCheckedInternal(checked);
						 //getArguments().putBoolean(EXTRA_CHECKED, checked);
						onPreferenceToggled(mPreferenceKey, checked);
						return false;
					}
				});
		
	}
	private ToggleSwitch createAndAddActionBarToggleSwitch(Activity activity) {
		ToggleSwitch toggleSwitch = new ToggleSwitch(activity);
		final int padding = activity.getResources().getDimensionPixelSize(
				R.dimen.action_bar_switch_padding);
		toggleSwitch.setPadding(0, 0, padding, 0);
		activity.getActionBar().setDisplayOptions(
				ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		activity.getActionBar().setCustomView(
				toggleSwitch,
				new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
						ActionBar.LayoutParams.WRAP_CONTENT,
						Gravity.CENTER_VERTICAL | Gravity.END));
		return toggleSwitch;
	}

	private void updatePreference()
	{
	Intent broadcast = new Intent("com.android.settings.GESTURE_VIEWPAGER_UPDATE");
        sendBroadcast(broadcast);
	}
	
}
