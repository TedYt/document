/**
 * @author aoran
 * Gesture Settings
 */
package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.preference.PreferenceActivity;
import android.content.Context;

import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;


import com.android.settings.accessibility.ToggleFeaturePreferenceFragment;
import com.android.settings.accessibility.ToggleSwitch;
import com.android.settings.accessibility.ToggleSwitch.OnBeforeCheckedChangeListener;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;
import android.app.AlertDialog;
import android.app.Dialog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.widget.CheckBox;

public class GestureProfileSettingsSystem extends PreferenceActivity
{
    private static final String LOG_TAG = "GestureSettingsSystem";
    private static Context mContext;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        getFragmentManager().beginTransaction().replace(android.R.id.content, new GestureSettingsSystemPreferenceFragment()).commit(); 
        
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



public class GestureSettingsSystemPreferenceFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
	private static final String EXTRA_CHECKED = "checked";
	private static final String LOW_SENSITIVE_DEGREE = "1";
	private static final String HEIGH_SENSITIVE_DEGREE = "2";

	protected ToggleSwitch mToggleSwitch;
	protected String mPreferenceKey;

	private GestureSystemProfilePreference tutorial_upset_snooze_alarm;
//	private GestureSystemProfilePreference tutorial_doubleclick_screen; //yutao 2014.6.12 delete the doubleclick
	private GestureSystemProfilePreference tutorial_doubleclick_homekey;
	private GestureSystemProfilePreference tutorial_promixy_powerkey_unlock;
 private GestureSystemProfilePreference tutorial_gesture_browser_screen_on;
        private GestureSystemProfilePreference tutorial_gestrue_video_face_play;
        private GestureSystemProfilePreference tutorial_gesture_gallery_swipe;

	private CheckBoxPreference toggle_upset_snooze_alarm;
	private CheckBoxPreference toggle_doubleclick_screen;
	private CheckBoxPreference toggle_doubleclick_homekey;
	private CheckBoxPreference toggle_promixy_powerkey_unlock;
	 private CheckBoxPreference toggle_gesture_browser_on;
        private CheckBoxPreference toggle_gesture_video_face_play;
        private CheckBoxPreference toggle_gesture_gallery_swipe;

	private ListPreference mGestureSensitive;
        private BroadcastReceiver mGestureSettingsSystemUpdateReceiver;
        private List<GestureSystemProfilePreference> mGestureProfilePrefList = 
    	new ArrayList<GestureSystemProfilePreference>();

	TextView mtextview;
	ImageView mimageview;


       private String[]  mkeyList =new String[] {"tutorial_upset_snooze_alarm",/*"tutorial_doubleclick_screen",*/
       									"tutorial_doubleclick_homekey","tutorial_promixy_powerkey_unlock",
       									"tutorial_gesture_browser_screen_on","tutorial_gestrue_video_face_play","tutorial_gesture_gallery_swipe"};
	   
	protected void onPreferenceToggled(String preferenceKey, boolean enabled) {
		Log.v("GES", "@@@onPreferenceToggled," + enabled);
		Settings.Global.putInt(getContentResolver(),
				Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED, enabled ? 1
						: 0);
		setPreferenceEnable();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Log.v("GES", "@@@onViewCreated,activity" + getActivity());
		//onInstallActionBarToggleSwitch();
		//final boolean gestureSettingsEnabled = Settings.Global.getInt(
		//		getContentResolver(),
		//		Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED, 0) == 1;
		//mToggleSwitch.setCheckedInternal(gestureSettingsEnabled);
		// getArguments().putBoolean(EXTRA_CHECKED, gestureSettingsEnabled);
		//getListView().setDivider(null);
		// getListView().setEnabled(false);
	}

	@Override
	public void onDestroyView() {
		Log.v("GES", "@@@onDestroyView");
		getActivity().getActionBar().setCustomView(null);
		mToggleSwitch.setOnBeforeCheckedChangeListener(null);
		if(mGestureSettingsSystemUpdateReceiver != null)
		unregisterReceiver(mGestureSettingsSystemUpdateReceiver);
		
		super.onDestroyView();
	}

	protected void onInstallActionBarToggleSwitch() {
		mToggleSwitch = createAndAddActionBarToggleSwitch(getActivity());
		mToggleSwitch
				.setOnBeforeCheckedChangeListener(new OnBeforeCheckedChangeListener() {
					@Override
					public boolean onBeforeCheckedChanged(
							ToggleSwitch toggleSwitch, boolean checked) {
						toggleSwitch.setCheckedInternal(checked);
						// getArguments().putBoolean(EXTRA_CHECKED, checked);
						onPreferenceToggled(mPreferenceKey, checked);
						return false;
					}
				});
	}

	private void setPreferenceEnable() {
		final boolean gestureSettingsEnabled = Settings.Global.getInt(
				getContentResolver(),
				Settings.Global.ENABLE_GESTURE_SETTINGS_ENABLED, 0) == 1;
		if (gestureSettingsEnabled) {
			tutorial_upset_snooze_alarm.setEnabled(true);
//			tutorial_doubleclick_screen.setEnabled(true);
			tutorial_doubleclick_homekey.setEnabled(true);
			tutorial_promixy_powerkey_unlock.setEnabled(true);
			tutorial_gesture_browser_screen_on.setEnabled(true);
tutorial_gestrue_video_face_play.setEnabled(true);
tutorial_gesture_gallery_swipe.setEnabled(true);
		} else {
			tutorial_upset_snooze_alarm.setEnabled(false);
//			tutorial_doubleclick_screen.setEnabled(false);
			tutorial_doubleclick_homekey.setEnabled(false);
			tutorial_promixy_powerkey_unlock.setEnabled(false);
			tutorial_gesture_browser_screen_on.setEnabled(false);
tutorial_gestrue_video_face_play.setEnabled(false);
tutorial_gesture_gallery_swipe.setEnabled(false);
		}
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("GES", "@@@onCreate");
		//addPreferencesFromResource(R.xml.gesture_settings);
		createPreferenceHierarchy();
		initializeAllPreferences();
		setPreferenceEnable();

	        mGestureSettingsSystemUpdateReceiver  = new GestureSettingsSystemUpdateReceiver();
	        IntentFilter filter = new IntentFilter();
	        filter.addAction("com.android.settings.GESTURE_VIEWPAGER_UPDATE");
	        registerReceiver(mGestureSettingsSystemUpdateReceiver, filter);
	}


    private void createPreferenceHierarchy(){
    	
    	PreferenceScreen root = getPreferenceScreen();
        if(root != null){
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.gesture_system_settings);
        
        root = getPreferenceScreen();
        
    }
	

	@Override
	public void onResume() {
		super.onResume();
		updateAllPreferences();
		setPreferenceEnable();
		Log.v("GES", "@@@onResume");
		
		 //add by zjw
        ListView listView = this.getListView();
        listView.setDivider(null);
        listView.setDrawSelectorOnTop(false);
        listView.setSelector(R.drawable.listItemSelector);
	}

	private void updateAllPreferences() {
		Log.v("GES", "@@@updateAllPreferences");

	for(String profileKey: mkeyList)
	{
		GestureSystemProfilePreference  listitemPreference = (GestureSystemProfilePreference)findPreference(profileKey);
	        if(listitemPreference != null){
	            listitemPreference.loadSettingChecked();
	        } 
	}

		
	}

	private void initializeAllPreferences() {

		tutorial_upset_snooze_alarm= (GestureSystemProfilePreference) findPreference("tutorial_upset_snooze_alarm");
//		tutorial_doubleclick_screen= (GestureSystemProfilePreference) findPreference("tutorial_doubleclick_screen");
		tutorial_doubleclick_homekey= (GestureSystemProfilePreference) findPreference("tutorial_doubleclick_homekey");
		tutorial_promixy_powerkey_unlock= (GestureSystemProfilePreference) findPreference("tutorial_promixy_powerkey_unlock");
		tutorial_gesture_browser_screen_on = (GestureSystemProfilePreference) findPreference("tutorial_gesture_browser_screen_on");
tutorial_gestrue_video_face_play = (GestureSystemProfilePreference) findPreference("tutorial_gestrue_video_face_play");
tutorial_gesture_gallery_swipe = (GestureSystemProfilePreference) findPreference("tutorial_gesture_gallery_swipe");

	
		mGestureSensitive = (ListPreference)findPreference("sensitive_degree");
		if(mGestureSensitive != null){
			mGestureSensitive.setOnPreferenceChangeListener(this);	
		}	
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

/*
		String key = preference.getKey();
		String title = ((GestureSystemProfilePreference)preference).getTitle();;
		if(preference == tutorial_promixy_unlock)
		{
			Dialog mdialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.really_remove_account_title)
                .setPositiveButton(R.string.ok, null)
                .setMessage(title)
                .create();
			mdialog.show();
		}
*/

		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.xml.tutorial_dialog, null);

		mtextview = (TextView) view.findViewById(R.id.textview_tutorial);
		mimageview = (ImageView) view.findViewById(R.id.imageview_tutorial);
		mimageview.setVisibility(ImageView.VISIBLE);

		AlertDialog.Builder animationDialog = new AlertDialog.Builder(
				getActivity());
		animationDialog.setView(view);
		animationDialog.setPositiveButton(android.R.string.ok, null);

		//aoran add animation.
		if (tutorial_upset_snooze_alarm== preference) {
			mtextview.setText(R.string.gesture_tutorial_upset_snooze_alarm);
			animationDialog.setTitle(R.string.gesture_setttings_upset_snooze_alarm);
			mimageview
					.setBackgroundResource(R.drawable.animation_upset_snooze_alarm);
			mimageview
					.setOverScrollMode(ImageView.OVER_SCROLL_IF_CONTENT_SCROLLS);
			
		/*} else if (tutorial_doubleclick_screen== preference) {
			mtextview.setText(R.string.gesture_tutorial_doubleclick_screen);
			animationDialog.setTitle(R.string.gesture_setttings_doubleclick_screen);
			mimageview
					.setBackgroundResource(R.drawable.animation_double_screen);*/
		} else if (tutorial_doubleclick_homekey== preference) {
			mtextview.setText(R.string.gesture_tutorial_doubleclick_homekey);
			animationDialog
					.setTitle(R.string.gesture_setttings_doubleclick_homekey);
			mimageview
					.setBackgroundResource(R.drawable.animation_double_homekey);
		} else if (tutorial_promixy_powerkey_unlock== preference) {
			mtextview.setText(R.string.gesture_tutorial_promixy_powerkey_unlock);
			animationDialog
					.setTitle(R.string.gesture_setttings_promixy_powerkey_unlock);
			mimageview
					.setBackgroundResource(R.drawable.animation_promixy_powerunlock);
		}
		else if (tutorial_gesture_browser_screen_on == preference) {
			mtextview.setText(R.string.gesture_tutorial_browser_on);
			animationDialog
					.setTitle(R.string.gesture_setttings_browser_on);
			mimageview
					.setBackgroundResource(R.drawable.animation_gesture_browser_on);
				}
		 else if (tutorial_gestrue_video_face_play == preference) {
			mtextview.setText(R.string.gesture_tutorial_video_face_play);
			animationDialog
					.setTitle(R.string.gesture_setttings_video_face_play);
			mimageview
					.setBackgroundResource(R.drawable.animation_gesture_video_face_play);
				}
		 else if (tutorial_gesture_gallery_swipe == preference) {
			mtextview.setText(R.string.gesture_tutorial_gallery_swipe);
			animationDialog
					.setTitle(R.string.gesture_setttings_gallery_swipe);
			mimageview
					.setBackgroundResource(R.drawable.animation_gesture_gallery_swipe);
				}
		view.setOverScrollMode(ImageView.OVER_SCROLL_IF_CONTENT_SCROLLS);

		final AlertDialog animationTutorial = animationDialog.setView(view)
				.create();
		animationTutorial.show();

		AnimationDrawable frameAnimation = (AnimationDrawable) mimageview
				.getBackground();
		if (!frameAnimation.isRunning()) {
			frameAnimation.stop();
			frameAnimation.start();
		}
			
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	private void handleGestureSetttingsPreferenceClick(String settings_key,
			boolean on) {
		Log.v("GES", "@@@handleGestureSetttingsPreferenceClick," + on);
		Settings.Secure
				.putInt(getContentResolver(), settings_key, (on ? 1 : 0));
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		if(preference == mGestureSensitive){
			SystemProperties.set("persist.ps.threshold", String.valueOf(newValue));
			return true;
		}
		return true;
	}

	private class GestureSettingsSystemUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {            
 			setPreferenceEnable();
        }
    }
}

}
