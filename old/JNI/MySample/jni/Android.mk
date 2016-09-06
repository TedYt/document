LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:=com_android_settings_TestAudioCalibrationLib.c
LOCAL_LDLIBS := -llog
LOCAL_C_INCLUDES := $(JNI_H_INCLUDE)

LOCAL_MODULE := libjni_tfa9890

LOCAL_C_INCLUDES += vendor/tinno/tfa9890/interface
                    
LOCAL_SHARED_LIBRARIES += libtfa9890 \
                       liblog \
                       libcutils

#LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)

