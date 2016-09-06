package com.android.settings;

//import android.util.Log;

public class TestAudioCalibrationLib {
	static {
		System.loadLibrary("jni_tfa9890");
		//Log.d("tao","loadlib ok");
	}
	
	public native int[] tfa9890reCalibration(float data1, float data2);
	public native void goBack();
}
