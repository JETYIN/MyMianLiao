LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := mianliao_avatar_shared

LOCAL_MODULE_FILENAME := libmianliao_avatar

LOCAL_SRC_FILES := main.cpp \
                   Java_com_tjut_mianliao_cocos2dx_CocosAvatarView.cpp \
                   ../../Classes/AppDelegate.cpp \
                   ../../Classes/AvatarScene.cpp

LOCAL_C_INCLUDES := $(LOCAL_PATH) \
                    $(LOCAL_PATH)/../../Classes \
                    $(LOCAL_PATH)/../../../cocos2d/extensions \
                    $(LOCAL_PATH)/../../../cocos2d/cocos/platform/android/jni \
                    $(LOCAL_PATH)/../../../cocos2d

LOCAL_STATIC_LIBRARIES := cocos2dx_static

include $(BUILD_SHARED_LIBRARY)

$(call import-module,.)
