package com.tjut.mianliao.cocos2dx;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.HashMap;
import java.util.Map.Entry;

import org.cocos2dx.lib.Cocos2dxActivity.Cocos2dxEGLConfigChooser;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxHelper;
import org.cocos2dx.lib.Cocos2dxHelper.Cocos2dxHelperListener;
import org.cocos2dx.lib.Cocos2dxRenderer;

public class CocosAvatarView extends Cocos2dxGLSurfaceView implements Cocos2dxHelperListener {

    static {
        System.loadLibrary("mianliao_avatar");
    }

    private static OnAvatarLoadedListener sOnAvatarLoadedListener;
    private static boolean sAvatarLoaded;

    public CocosAvatarView(Context context) {
        super(context);
        init();
    }

    public CocosAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent pMotionEvent) {
        return false;
    }

    @Override
    public void showDialog(String pTitle, String pMessage) {
    }

    @Override
    public void showEditTextDialog(String pTitle, String pMessage,
            int pInputMode, int pInputFlag, int pReturnType, int pMaxLength) {
    }

    @Override
    public void runOnGLThread(Runnable pRunnable) {
        queueEvent(pRunnable);
    }

    public void onResume(Activity activity) {
        Cocos2dxHelper.setActivity(activity);
        Cocos2dxHelper.setListener(this);
        Cocos2dxHelper.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        Cocos2dxHelper.onPause();
        super.onPause();
    }

    public static void setOnAvatarLoadedListener(OnAvatarLoadedListener listener) {
        sOnAvatarLoadedListener = listener;
    }

    public static boolean isAvatarLoaded() {
        return sAvatarLoaded;
    }

    public static void loadArmatureData(final boolean isSandBoxVersionExists) {
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeLoadArmatureData(isSandBoxVersionExists);
            }
        });
    }

    public static void setAvatarsHeight(final float height) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeSetAvatarsHeight(height);
                }
            });
        }
    }

    public static void setAvatarHeight(final int side, final float height) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeSetAvatarHeight(side, height);
                }
            });
        }
    }

    public static void playArmatureByIndex(final int side, final int armatureIndex) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativePlayArmatureByIndex(side, armatureIndex);
                }
            });
        }
    }

    public static void playArmatureByName(final int side, final String animationName) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativePlayArmatureByName(side, animationName);
                }
            });
        }
    }

    public static void changeSuit(final int side, final HashMap<String, String> suit) {
        if (sAvatarLoaded && suit != null) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    for (Entry<String, String> entry : suit.entrySet()) {
                        nativeChangeSuit(side, entry.getKey(), entry.getValue());
                    }
                }
            });
        }
    }

    public static void changeSuit(final int side,
            final String originalLayer, final String replaceImageName) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeChangeSuit(side, originalLayer, replaceImageName);
                }
            });
        }
    }

    public static void hideArmature(final int side) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeHideArmature(side);
                }
            });
        }
    }

    public static void midXArmature(final int side) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeMidXArmature(side);
                }
            });
        }
    }

    public static void showMyAvatar(final float y, final float height) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeShowMyAvatar(y, height);
                }
            });
        }
    }

    public static void locateAvatars(final float y, final float height) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeLocateAvatars(y, height);
                }
            });
        }
    }

    public static void setAvatarsWithPosY(final float y) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeSetAvatarsWithPosY(y);
                }
            });
        }
    }

    public static void setAvatarWithPosY(final int side, final float y) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeSetAvatarWithPosY(side, y);
                }
            });
        }
    }

    public static void stopAction(final int side) {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeStopAction(side);
                }
            });
        }
    }

    public static void loadGenderClothesData(final boolean isSandBoxVersionExists) {
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeLoadGenderClothesData(isSandBoxVersionExists);
            }
        });
    }

    public static void loadClothesData(
            final String suitName, final boolean isSandBoxVersionExists) {
        Cocos2dxHelper.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                nativeLoadClothesData(suitName, isSandBoxVersionExists);
            }
        });
    }

    public static boolean checkDataLoaded() {
        return nativeCheckDataLoaded();
    }

    public static void addLeftAvatar() {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeAddLeftAvatar();
                }
            });
        }
    }

    public static void addRightAvatar() {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeAddRightAvatar();
                }
            });
        }
    }

    public static void removeLeftAvatar() {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeRemoveLeftAvatar();
                }
            });
        }
    }

    public static void removeRightAvatar() {
        if (sAvatarLoaded) {
            Cocos2dxHelper.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    nativeRemoveRightAvatar();
                }
            });
        }
    }

    private void init() {
        int[] attrs = Cocos2dxHelper.getGLContextAttrs();
        // this line is need on some device if we specify an alpha bits
        if (attrs[3] > 0) {
            getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
        setEGLConfigChooser(new Cocos2dxEGLConfigChooser(attrs));
        setCocos2dxRenderer(new Cocos2dxRenderer());
        setZOrderOnTop(true);
    }

    private static void onAvatarLoaded() {
        sAvatarLoaded = true;
        if (sOnAvatarLoadedListener != null) {
            sOnAvatarLoadedListener.onAvatarLoaded();
        }
    }

    private static native void nativeLoadArmatureData(boolean isSandBoxVersionExists);
    private static native void nativeSetAvatarsHeight(float height);
    private static native void nativeSetAvatarHeight(int side, float height);
    private static native void nativePlayArmatureByIndex(int side, int armatureIndex);
    private static native void nativePlayArmatureByName(int side, String animationName);
    private static native void nativeChangeSuit(int side, String originalLayer, String replaceImageName);
    private static native void nativeHideArmature(int side);
    private static native void nativeMidXArmature(int side);
    private static native void nativeShowMyAvatar(float y, float height);
    private static native void nativeLocateAvatars(float y, float height);
    private static native void nativeSetAvatarsWithPosY(float y);
    private static native void nativeSetAvatarWithPosY(int side, float y);
    private static native void nativeStopAction(int side);
    private static native void nativeLoadGenderClothesData(boolean isSandBoxVersionExists);
    private static native void nativeLoadClothesData(String suitName, boolean isSandBoxVersionExists);
    private static native boolean nativeCheckDataLoaded();
    private static native boolean nativeAddLeftAvatar();
    private static native boolean nativeAddRightAvatar();
    private static native boolean nativeRemoveLeftAvatar();
    private static native boolean nativeRemoveRightAvatar();

    public interface OnAvatarLoadedListener {
        public void onAvatarLoaded();
    }
}
