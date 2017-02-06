package com.tjut.mianliao.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;

import com.tjut.mianliao.data.DataHelper;

public class ScreenShotTool {

    public static final String SP_APP_CONTENT_HEIGHT = "sp_app_content_height";
    public static final String SP_APP_OTHER_HEIGHT = "sp_app_other_height";
    
    public static Bitmap snapShotBitmap;
    public static int appContentHeight;
    public static int appOtherHeight;

    public static int getAppContentHeight(Context context){
        if (appContentHeight > 0) {
            return appContentHeight;
        }
        return DataHelper.getSpForData(context).getInt(SP_APP_CONTENT_HEIGHT, 0);
    }
    
    public static int getAppOtherHeight(Context context){
        if (appOtherHeight > 0) {
            return appOtherHeight;
        }
        return DataHelper.getSpForData(context).getInt(SP_APP_OTHER_HEIGHT, 0);
    }
    
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
