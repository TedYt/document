#include <jni.h>

#include "utils/Log.h"  //这个是在android源码环境使用
#include <android/log.h>//这个在NDK编译时使用
#include <tfa9890_interface.h>


#define LOG_TAG "jniAudioTest"
#undef LOG
//#define  ALOGI(...)  __android_log_print(ANDROID_LOG_INFO,"tao",__VA_ARGS__)

jintArray manual_calbration(JNIEnv *env){

	jintArray array;
	int i = 1;
	
	jint result = 0;
	jint data1 = 0;
	jint data2 = 0;
	
	float re25=0.0;
	float tCoefA=0.0000;
	//do someting prepare
	tfa9890_manualCalibationSet(1);
	system("tinymix -D 0 600 1");
	if(!tfa9890_check_tfaopen()) {
		ALOGI("enable out--speaker-of-pa-1");
		tfa9890_init();
	}
	
	ALOGI("enable out--speaker-of-pa-2");
    tfa9890_setSamplerate(48000);
	tfa9890_EQset(0);
	tfa9890_SpeakerOn();
	
	tfa9890_reCalibration(&re25, &tCoefA);
	
	array = (*env)->NewIntArray(env,3);
	for (;i <= 3; i ++){
		(*env)->SetIntArrayRegion(env, array, i-1, 1, &i);
	}
	
	data1 = (int)(re25*10000);
	(*env)->SetIntArrayRegion(env, array, 0, 1, &data1);
	data2 = (int)(tCoefA*10000);
	(*env)->SetIntArrayRegion(env, array, 1, 1, &data2);
	
	
	if(((re25) >= 4.8 && (re25) <= 7.2)){
		if((tCoefA) >= 0.0038 && (tCoefA) <= 0.0040){
			ALOGI("manualcalibration is very good");
			result = 1;
		}
	}else {
		ALOGI("manualcalibration is very bad");
		result = -1;
	}
	(*env)->SetIntArrayRegion(env, array, 2, 1, &result);
	
	return array;
}

JNIEXPORT void JNICALL Java_com_android_settings_TestAudioCalibrationLib_goBack(JNIEnv *env, jobject obj)
{
	ALOGI("Test AC goBack 1");
	tfa9890_SpeakerOff();
	ALOGI("Test AC goBack 2");
	tfa9890_manualCalibationSet(0);
	ALOGI("Test AC goBack 3");
}

/* Native interface, it will be call in java code */
JNIEXPORT jintArray JNICALL Java_com_android_settings_TestAudioCalibrationLib_tfa9890reCalibration(JNIEnv *env, jobject obj, jfloat f1, jfloat f2)
{
    //LOGI("Hello World From libhelloworld.so!");
	jintArray array = manual_calbration(env);

    return array;//(*env)->NewStringUTF(env, "Hello World!");
}

/* This function will be call when the library first be load.
 * You can do some init in the libray. return which version jni it support.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    void *venv;
    ALOGI("JNI_OnLoad!");

    if ((*vm)->GetEnv(vm, (void**)&venv, JNI_VERSION_1_4) != JNI_OK) {
        ALOGI("ERROR: GetEnv failed");
        return 1;
    }

     return JNI_VERSION_1_4;
}

