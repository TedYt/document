#Begin 编译apk
LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

CommonUpgrade := CommonUpgrade

#指明编译的源文件
src_dirs := src \
    $(CommonUpgrade)/src

res_dirs := res 

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 \
								music

#LOCAL_SRC_FILES := $(call all-java-files-under,src) 
LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs)) \
					src/com/ape/music/IMediaPlaybackService.aidl

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_ASSET_DIR := $(LOCAL_PATH)/$ assets


LOCAL_PACKAGE_NAME := TinnoApeMusic
#LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags
#取消代码混淆
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)
#End 编译apk

#Begin 将so库复制到系统目录下
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libaudiocore
LOCAL_SRC_FILES := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libaudiofp
LOCAL_SRC_FILES := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
LOCAL_MODULE := libBDmfemusic_V1
LOCAL_SRC_FILES := libs/armeabi/$(LOCAL_MODULE).so
LOCAL_MODULE_PATH := $(TARGET_OUT_SHARED_LIBRARIES)
include $(BUILD_PREBUILT)
#End 将so库复制到系统目录下

#Begin 引用第三方jar包
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    music:libs/music-sdk-v3.1.0.jar 

#必须写明的一句，否则第三方jar包引用不会生效
include $(BUILD_MULTI_PREBUILT)
#End 引用第三方jar包

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
