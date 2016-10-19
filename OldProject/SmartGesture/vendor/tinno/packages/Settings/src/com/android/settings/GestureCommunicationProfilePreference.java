/**
 * 
 */
package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.content.res.TypedArray;

import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.audioprofile.AudioProfileManager;
import com.mediatek.audioprofile.AudioProfileManager.Scenario;
import com.mediatek.xlog.Xlog;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.widget.CheckBox;

/**
 * @author mtk80800
 *
 */
public class GestureCommunicationProfilePreference extends Preference implements CompoundButton.OnCheckedChangeListener{
	
    private static final String XLogTAG = "Settings/AudioP";
    private static final String TAG = "GestureCommunicationProfilePreference:";
    
	private static CompoundButton mCurrentChecked = null;
	private static String activeKey = null;
	
	private String mPreferenceTitle = null;
	private String mPreferenceSummary = null;
	
	private TextView mTextView = null;
	private TextView mSummary = null;
	private CheckBox mCheckboxButton = null;
	
	//private AudioProfileManager mProfileManager;
	private Context             mContext;
    private String              mKey;


	
	
    public GestureCommunicationProfilePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mContext = context;
        
        //get the title from audioprofile_settings.xml
        if(super.getTitle() != null){
        	mPreferenceTitle = super.getTitle().toString();
        }
        
        //get the summary from audioprofile_settings.xml
        if(super.getSummary() != null){
        	mPreferenceSummary = super.getSummary().toString();
        }
        
        //mProfileManager = (AudioProfileManager)context.getSystemService(Context.AUDIOPROFILE_SERVICE);
        
        mKey = getKey();
        
            setLayoutResource(R.layout.gesture_profile_item);//aoran
            
        Xlog.d(XLogTAG, TAG + "new GestureProfilePreference setLayoutResource");
    }  
    
    public GestureCommunicationProfilePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }    
    
    public GestureCommunicationProfilePreference(Context context) {
        this(context, null);
    }
    
     public void setProfileKey(String key) {
    	 setKey(key);
    	 mKey = key;
     }
     
    @Override
    public View getView(View convertView, ViewGroup parent) {
    	Xlog.d(XLogTAG, TAG + "getView from " + getKey());
        View view = super.getView(convertView, parent);

        mCheckboxButton = (CheckBox)view.findViewById(R.id.mcheckbox);
        loadSettingChecked();
        if (mCheckboxButton != null){
            mCheckboxButton.setOnCheckedChangeListener(this);
        	//mCheckboxButton.setOnClickListener(new OnClickListener() {
				
				//public void onClick(View v) {
					// TODO Auto-generated method stub
					//Xlog.d(XLogTAG, TAG + "onClick " + getKey());
					//if(!mCheckboxButton.equals(mCurrentChecked)) {
				       // if (mCurrentChecked != null) {
				            //mCurrentChecked.setChecked(false);


					/*	if(mCheckboxButton.isChecked())
						{
					            mCheckboxButton.setChecked(false);
						    updateSettingChecked(false);
						}
					        else 
					        {
						   mCheckboxButton.setChecked(true);
						   updateSettingChecked(true);
					        } 

					        mCurrentChecked = mCheckboxButton;
				        //} 
				       //     } else if(mCheckboxButton.equals(mCurrentChecked)){
				        //    	Xlog.d(XLogTAG, TAG + "Click the active profile, do nothing return" );
				       //     }

					}
			});*/
        	
        	//mCheckboxButton.setChecked(isChecked());
        	//if(isChecked()) {
        	//	setChecked();
        	//}
        }

        mTextView = (TextView) view.findViewById(R.id.profiles_text);
        if(mPreferenceTitle != null){
            mTextView.setText(mPreferenceTitle);
        } else {
            Xlog.d(XLogTAG, TAG + "PreferenceTitle is null");
        }
        
        mSummary = (TextView) view.findViewById(R.id.profiles_summary);
        dynamicShowSummary();        	

        return view;
    }

    public void dynamicShowSummary() {
        Xlog.d(XLogTAG, TAG + mKey + " dynamicShowSummary");
        
    	if(mSummary != null) {

    	        if(mPreferenceSummary != null){
    			    mSummary.setText(mPreferenceSummary);
    		    }		
    	} else {
            if( mSummary != null){
            	Xlog.d(XLogTAG, TAG + "summary object is null");
            }
    	}
    }
    
    public void onClick() {
    	
    }
    
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Xlog.v(XLogTAG, TAG + "onCheckedChanged  isChecked =  " + isChecked + getKey());
		if(isChecked)
			        Xlog.v(XLogTAG, TAG + "onCheckedChanged  isChecked = true");
	else
		        Xlog.v(XLogTAG, TAG + "onCheckedChanged  isChecked = false " );

	            mCheckboxButton.setChecked(isChecked);
		    updateSettingChecked(isChecked);
        
    }
    
    public boolean isChecked() {
        if(activeKey != null){
    	    return getKey().equals(activeKey);
        }
        return false;
    }

    public void loadSettingChecked() {
    	activeKey = getKey();

	Xlog.d(XLogTAG, TAG + "loadSettingChecked     activeKey  " + activeKey);

		final int toggle_upset_silent_value = Settings.Secure.getInt(
				mContext.getContentResolver(), Settings.Secure.UPSET_SILENT_ENABLED, 0);

		final int toggle_promixy_answer_phone_value = Settings.Secure.getInt(
				mContext.getContentResolver(),
				Settings.Secure.PROMIXY_ANSWER_PHONE_ENABLED, 0);


		final int toggle_promixy_dial_phone_value = Settings.Secure.getInt(
				mContext.getContentResolver(),
				Settings.Secure.PROMIXY_DIAL_PHONE_ENABLED, 0);

		//aoran add promixy speaker.{
		final int toggle_settings_promixy_speaker_value = Settings.Secure.getInt(
				mContext.getContentResolver(), Settings.Secure.PROMIXY_SPEAKER_ENABLED, 0);
		//}

		final int toggle_promixy_incoming_call_value = Settings.Secure.getInt(
				mContext.getContentResolver(),
				Settings.Secure.PROMIXY_INCOMING_CALL_ENABLED, 0);

        final int toggle_promixy_quick_camera_value = Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.GESTURE_QUICK_CAMERA_ENABLED, 0);

		if(mCheckboxButton == null)
			return;
			if(activeKey.equals("tutorial_upset_silent"))
			mCheckboxButton.setChecked(toggle_upset_silent_value == 1);
		else if(activeKey.equals("tutorial_promixy_answer_phone"))
			mCheckboxButton.setChecked(toggle_promixy_answer_phone_value == 1);
		else if(activeKey.equals("tutorial_promixy_dial_phone"))
			mCheckboxButton.setChecked(toggle_promixy_dial_phone_value == 1);
		else if(activeKey.equals("tutorial_promixy_speaker"))
			mCheckboxButton.setChecked(toggle_settings_promixy_speaker_value == 1);
		else if(activeKey.equals("tutorial_promixy_incoming_call"))
			mCheckboxButton.setChecked(toggle_promixy_incoming_call_value == 1);
            /*else if (activeKey.equals("tutorial_quick_camera"))
                mCheckboxButton.setChecked(toggle_promixy_quick_camera_value == 1);*/
    	}
    
    public void setTitle(String title, boolean setToProfile){
        mPreferenceTitle = title;
        
        if(mTextView!=null){
            mTextView.setText(title);
        }
    }

    public String getTitle(){
        return mPreferenceTitle;
    }

    public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
    	}
  public void updateSettingChecked(boolean enable) {
    	activeKey = getKey();
	Xlog.d(XLogTAG, TAG + "updateSettingChecked     activeKey   =  " + activeKey);

		if(activeKey.equals("tutorial_upset_silent"))
			handleGestureSetttingsPreferenceClick(
					Settings.Secure.UPSET_SILENT_ENABLED,
					enable);
		else if(activeKey.equals("tutorial_promixy_answer_phone"))
			handleGestureSetttingsPreferenceClick(
					Settings.Secure.PROMIXY_ANSWER_PHONE_ENABLED,
					enable);
		else if(activeKey.equals("tutorial_promixy_dial_phone"))
			handleGestureSetttingsPreferenceClick(
					Settings.Secure.PROMIXY_DIAL_PHONE_ENABLED,
					enable);
		//aoran add promixy speaker.{
		else if(activeKey.equals("tutorial_promixy_speaker"))
			handleGestureSetttingsPreferenceClick(
					Settings.Secure.PROMIXY_SPEAKER_ENABLED,
					enable);
		//}
		else if(activeKey.equals("tutorial_promixy_incoming_call"))
			handleGestureSetttingsPreferenceClick(
					Settings.Secure.PROMIXY_INCOMING_CALL_ENABLED,
					enable);

        /*else if(activeKey.equals("tutorial_quick_camera"))
            handleGestureSetttingsPreferenceClick(
                    Settings.Secure.GESTURE_QUICK_CAMERA_ENABLED,
                    enable);*/

        /*if(mCheckboxButton != null){
            if(!mCheckboxButton.equals(mCurrentChecked)) {
    	        if (mCurrentChecked != null) {
    	            mCurrentChecked.setChecked(false);
    	        } 
                Xlog.d(XLogTAG, TAG + "setChecked" + getKey());
            	mCheckboxButton.setChecked(true);
    	        mCurrentChecked = mCheckboxButton;
            }

        } else {
            Xlog.d(XLogTAG, TAG + "mCheckboxButton is null");
        }*/
    }

  private void handleGestureSetttingsPreferenceClick(String settings_key,
			boolean on) {
		Log.v("GES", "@@@handleGestureSetttingsPreferenceClick," + on);
			Xlog.d(XLogTAG, TAG + "handleGestureSetttingsPreferenceClick   set   settings_key   =  " + settings_key +" , on = " +on);
final int value = Settings.Secure.getInt(
				mContext.getContentResolver(),
				settings_key, 0);
	Xlog.d(XLogTAG, TAG + "handleGestureSetttingsPreferenceClick     read 1      settings_key   = " + value);

		Settings.Secure
				.putInt(mContext.getContentResolver(), settings_key, (on ? 1 : 0));

		final int value2 = Settings.Secure.getInt(
				mContext.getContentResolver(),
				settings_key, 0);
	Xlog.d(XLogTAG, TAG + "handleGestureSetttingsPreferenceClick     read 2      settings_key   = " + value2);

	}
}
