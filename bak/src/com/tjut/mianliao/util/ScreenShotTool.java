package com.tjut.mianliao.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;

public class ScreenShotTool {

    public static Bitmap snapShotBitmap;
    public static int appContentHeight;
    public static int appOtherHeight;

    public static int getOtherHeight(Activity activity) {
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int contentTop = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight = contentTop - statusBarHeight;
        return statusBarHeight + titleBarHeight;
    }

    public static void process(Activity activity) {

        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache(true);

        Bitmap bmp1 = view.getDrawingCache();
        int height = getOtherHeight(activity);

        Bitmap bmp2 = Bitmap.createBitmap(bmp1, 0, height, bmp1.getWidth(), bmp1.getHeight() - height);

        snapShotBitmap = bmp2;

    }
}
