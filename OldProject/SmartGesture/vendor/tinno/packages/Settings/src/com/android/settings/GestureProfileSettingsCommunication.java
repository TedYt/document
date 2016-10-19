/**
 * @author wyy
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

public class GestureProfileSettingsCommunication extends PreferenceActivity
{
    private static final String LOG_TAG = "GestureSettingsCommunication";
    private static Context mContext;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        getFragmentManager().beginTransaction().replace(android.R.id.content, new GestureSettingsCommunicationPreferenceFragment()).commit(); 
        
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



public class GestureSettingsCommunicationPreferenceFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
	private static final String EXTRA_CHECKED = "checked";
	private static final String LOW_SENSITIVE_DEGREE = "1";
	private static final String HEIGH_SENSITIVE_DEGREE = "2";

	protected ToggleSwitch mToggleSwitch;
	protected String mPreferenceKey;

	private GestureCommunicationProfilePreference tutorial_upset_silent;
	private GestureCommunicationProfilePreference tutorial_promixy_answer_phone;
	private GestureCommunicationProfilePreference tutorial_promixy_dial_phone;
	private GestureCommunicationProfilePreference tutorial_promixy_speaker;
	private GestureCommunicationProfilePreference tutorial_promixy_incoming_call;
//    private GestureCommunicationProfilePreference tutorial_quick_camera;

	private CheckBoxPreference toggle_upset_silent;
	private CheckBoxPreference toggle_promixy_answer_phone;
	private CheckBoxPreference toggle_promixy_dial_phone;
	private CheckBoxPreference toggle_promixy_speaker;
	private CheckBoxPreference toggle_promixy_incoming_call;
	
	private ListPreference mGestureSensitive;
        private BroadcastReceiver mGestureSettingsCommunicationUpdateReceiver;
        private List<GestureCommunicationProfilePreference> mGestureProfilePrefList = 
    	new ArrayList<GestureCommunicationProfilePreference>();

	TextView mtextview;
	ImageView mimageview;
		

       private String[]  mkeyList =new String[] {"tutorial_upset_silent","tutorial_promixy_answer_phone","tutorial_promixy_dial_phone",
       									"tutorial_promixy_speaker","tutorial_promixy_incoming_call"}; //, "tutorial_quick_camera"
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
		if(mGestureSettingsCommunicationUpdateReceiver != null)
		unregisterReceiver(mGestureSettingsCommunicationUpdateReceiver);
		
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
			tutorial_upset_silent.setEnabled(true);
			tutorial_promixy_answer_phone.setEnabled(true);
			tutorial_promixy_dial_phone.setEnabled(true);
			tutorial_promixy_speaker.setEnabled(true);
			tutorial_promixy_incoming_call.setEnabled(true);
//            tutorial_quick_camera.setEnabled(true);
		} else {
			tutorial_upset_silent.setEnabled(false);
			tutorial_promixy_answer_phone.setEnabled(false);
			tutorial_promixy_dial_phone.setEnabled(false);
			tutorial_promixy_speaker.setEnabled(false);
			tutorial_promixy_incoming_call.setEnabled(false);
//            tutorial_quick_camera.setEnabled(false);
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

	        mGestureSettingsCommunicationUpdateReceiver  = new GestureSettingsCommunicationUpdateReceiver();
	        IntentFilter filter = new IntentFilter();
	        filter.addAction("com.android.settings.GESTURE_VIEWPAGER_UPDATE");
	        registerReceiver(mGestureSettingsCommunicationUpdateReceiver, filter);
	}


    private void createPreferenceHierarchy(){
    	
    	PreferenceScreen root = getPreferenceScreen();
        if(root != null){
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.gesture_communication_settings);
        
        root = getPreferenceScreen();
        
        //List<String> profileKeys = mProfileManager.getAllProfileKeys();
       

        
       // for(String profileKey:profileKeys){
       //     addPreference(root, profileKey);
        //}
        //setHasOptionsMenu(true);
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
		GestureCommunicationProfilePreference  listitemPreference = (GestureCommunicationProfilePreference)findPreference(profileKey);
	        if(listitemPreference != null){
	            listitemPreference.loadSettingChecked();
	        } 
	}

		
	}

	private void initializeAllPreferences() {

		tutorial_upset_silent = (GestureCommunicationProfilePreference) findPreference("tutorial_upset_silent");
		tutorial_promixy_answer_phone = (GestureCommunicationProfilePreference) findPreference("tutorial_promixy_answer_phone");
		tutorial_promixy_dial_phone = (GestureCommunicationProfilePreference) findPreference("tutorial_promixy_dial_phone");
		tutorial_promixy_speaker = (GestureCommunicationProfilePreference) findPreference("tutorial_promixy_speaker");		
		tutorial_promixy_incoming_call = (GestureCommunicationProfilePreference) findPreference("tutorial_promixy_incoming_call");
//	    tutorial_quick_camera = (GestureCommunicationProfilePreference)findPreference("tutorial_quick_camera");

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

		if (tutorial_upset_silent == preference) {
			mtextview.setText(R.string.gesture_tutorial_upset_silent);
			//aoran add for test.
			//mtextview.setBackgroundColor(mContext.getResources().getColor(R.color.white));
			animationDialog.setTitle(R.string.gesture_setttings_upset_silent);
			mimageview.setBackgroundResource(R.drawable.animation_upset_silent);
		} else if (tutorial_promixy_answer_phone == preference) {
			mtextview.setText(R.string.gesture_tutorial_promixy_answer_phone);
			animationDialog
					.setTitle(R.string.gesture_setttings_promixy_answer_phone);
			mimageview
					.setBackgroundResource(R.drawable.animation_promixy_answer_phone);
		} else if (tutorial_promixy_dial_phone == preference) {
			mtextview.setText(R.string.gesture_tutorial_promixy_dial_phone);
			animationDialog
					.setTitle(R.string.gesture_setttings_promixy_dial_phone);
			mimageview
					.setBackgroundResource(R.drawable.animation_promixy_dial_phone);
		} else if (tutorial_promixy_speaker== preference) {
			mtextview.setText(R.string.gesture_tutorial_promixy_speaker);
			animationDialog
					.setTitle(R.string.gesture_setttings_promixy_speaker);
			mimageview
					.setBackgroundResource(R.drawable.animation_promixy_speaker);
		} else if (tutorial_promixy_incoming_call == preference) {
			mtextview.setText(R.string.gesture_tutorial_promixy_incoming_call);
			animationDialog
					.setTitle(R.string.gesture_setttings_promixy_incoming_call);
			mimageview
					.setBackgroundResource(R.drawable.animation_promixy_incoming_call);
		}/* else if (tutorial_quick_camera == preference){
            mtextview.setText(R.string.gesture_tutorial_quick_camera);
            animationDialog.setTitle(R.string.gesture_settings_quick_camera);
            mimageview.setBackgroundResource(R.drawable.animation_quick_camera);
        }*/

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

	private class GestureSettingsCommunicationUpdateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {            
 			setPreferenceEnable();
        }
    }
}

}
