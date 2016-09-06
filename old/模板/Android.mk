LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

CommonUpgrade := CommonUpgrade

src_dirs := src \
    $(CommonUpgrade)/src

res_dirs := res 

#加载工程中已编译好的jar包，要使用LOCAL_JAVA_LIBRARIES
LOCAL_JAVA_LIBRARIES := framework \
                    android-support-v7-appcompat \
                    android-support-v7-recyclerview \
                    android-support-v13 \
                    android-support-v4 
                    
#加载第三方的jar包，要用LOCAL_STATIC_JAVA_LIBRARIES
LOCAL_STATIC_JAVA_LIBRARIES := music

#必须有包含下面的字段，否则会出现JAVA文件的修改不能被编译到的情况
LOCAL_SRC_FILES := $(call all-java-files-under, src) 
LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))

LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))

LOCAL_ASSET_DIR := $(LOCAL_PATH)/$ assets


LOCAL_PACKAGE_NAME := ApeMusic
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

#不使用混淆
#出现下面提示可以使用：
#there were 604 unresolved references to classes or interfaces.
#         You may need to add missing library jars or update their versions.
#         If your code works fine without the missing classes, you can suppress
#         the warnings with '-dontwarn' options.
LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

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

#导入第三方模板的方法
#注意，必须在include $(BUILD_PREBUILT)之后，并且必须要包含下面两个include
#{
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
    music:libs/music-sdk-v3.1.0.jar 

include $(BUILD_MULTI_PREBUILT)
#}

# Use the following include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
