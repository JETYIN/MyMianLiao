#include "JniHelper.h"
#include <jni.h>
#include "AvatarScene.h"
#include "Java_com_tjut_mianliao_cocos2dx_CocosAvatarView.h"

#define  CLASS_NAME "com/tjut/mianliao/cocos2dx/CocosAvatarView"

using namespace cocos2d;

extern "C" {

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeSetAvatarsHeight(JNIEnv* env, jobject thiz, jfloat height) {
        AvatarScene::getInstance()->setAvatarsHeight(height);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeSetAvatarHeight(JNIEnv* env, jobject thiz, jint side, jfloat height) {
        AvatarScene::getInstance()->setAvatarHeight(side, height);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativePlayArmatureByIndex(JNIEnv* env, jobject thiz, jint side, jint armatureIndex) {
        AvatarScene::getInstance()->playArmature(side, armatureIndex);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativePlayArmatureByName(JNIEnv* env, jobject thiz, jint side, jstring animationName) {
        const char* pszAnimationName = env->GetStringUTFChars(animationName, NULL);
        AvatarScene::getInstance()->playArmature(side, pszAnimationName);
        env->ReleaseStringUTFChars(animationName, pszAnimationName);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeLoadArmatureData(JNIEnv* env, jobject thiz, jboolean isSandBoxVersionExists) {
        AvatarScene::getInstance()->loadArmatureData(isSandBoxVersionExists);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeChangeSuit(JNIEnv* env, jobject thiz, jint side, jstring originalLayer, jstring replaceImageName) {
        const char* pszOriginalLayer = env->GetStringUTFChars(originalLayer, NULL);
        const char* pszReplaceImageName = env->GetStringUTFChars(replaceImageName, NULL);
        AvatarScene::getInstance()->changeSuit(side, pszOriginalLayer, pszReplaceImageName);
        env->ReleaseStringUTFChars(originalLayer, pszOriginalLayer);
        env->ReleaseStringUTFChars(replaceImageName, pszReplaceImageName);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeHideArmature(JNIEnv* env, jobject thiz, jint side) {
        AvatarScene::getInstance()->hideArmature(side);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeMidXArmature(JNIEnv* env, jobject thiz, jint side) {
        AvatarScene::getInstance()->midXArmature(side);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeShowMyAvatar(JNIEnv* env, jobject thiz, jfloat y, jfloat height) {
        AvatarScene::getInstance()->showMyAvatar(y, height);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeLocateAvatars(JNIEnv* env, jobject thiz, jfloat y, jfloat height) {
        AvatarScene::getInstance()->locateAvatars(y, height);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeSetAvatarsWithPosY(JNIEnv* env, jobject thiz, jfloat y) {
        AvatarScene::getInstance()->setAvatarsWithPosY(y);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeSetAvatarWithPosY(JNIEnv* env, jobject thiz, jint side, jfloat y) {
        AvatarScene::getInstance()->setAvatarWithPosY(side, y);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeStopAction(JNIEnv* env, jobject thiz, jint side) {
        AvatarScene::getInstance()->stopAction(side);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeLoadGenderClothesData(JNIEnv* env, jobject thiz, jboolean isSandBoxVersionExists) {
        AvatarScene::getInstance()->loadGenderClothesData(isSandBoxVersionExists);
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeLoadClothesData(JNIEnv* env, jobject thiz, jstring suitName, jboolean isSandBoxVersionExists) {
        const char* pszSuitName = env->GetStringUTFChars(suitName, NULL);
        AvatarScene::getInstance()->loadClothesData(pszSuitName, isSandBoxVersionExists);
        env->ReleaseStringUTFChars(suitName, pszSuitName);
    }

    JNIEXPORT jboolean JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeCheckDataLoaded(JNIEnv* env, jobject thiz) {
        return AvatarScene::getInstance()->checkDataLoaded();
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeAddLeftAvatar(JNIEnv* env, jobject thiz) {
        AvatarScene::getInstance()->addLeftAvatar();
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeAddRightAvatar(JNIEnv* env, jobject thiz) {
        AvatarScene::getInstance()->addRightAvatar();
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeRemoveLeftAvatar(JNIEnv* env, jobject thiz) {
        AvatarScene::getInstance()->removeLeftAvatar();
    }

    JNIEXPORT void JNICALL Java_com_tjut_mianliao_cocos2dx_CocosAvatarView_nativeRemoveRightAvatar(JNIEnv* env, jobject thiz) {
        AvatarScene::getInstance()->removeRightAvatar();
    }
}

void onAvatarLoadedJNI() {
    JniMethodInfo t;
    if (JniHelper::getStaticMethodInfo(t, CLASS_NAME, "onAvatarLoaded", "()V")) {
        t.env->CallStaticVoidMethod(t.classID, t.methodID);
        t.env->DeleteLocalRef(t.classID);
    }
}
