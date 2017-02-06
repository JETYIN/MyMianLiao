package com.tjut.mianliao.theme;

import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class ThemeTool {

    public static void setBackGroundImage(View view, AttributeSet attrs) {

        int bgId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "background", -1);

        if (bgId != -1) {
            String resName = ThemeDrawableSource.getResName(bgId);
            if (resName != null) {
                String blackFileName = resName + "_black";
                if (blackFileName.equals("pic_bg_emo")) {
                    System.out.println("pic_bg_emo");
                }
                int blackResId = ThemeDrawableSource.getResId(blackFileName);
                if (blackResId != -1) {
                    view.setBackgroundResource(blackResId);
                } else {
                    view.setBackgroundResource(bgId);
                }
            }
        }

    }

    public static void setSrcImage(ImageView view, AttributeSet attrs) {

        int sid = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", -1);

        if (sid != -1) {
            String resName = ThemeDrawableSource.getResName(sid);
            if (resName != null) {
                String blackFileName = resName + "_black";
                int blackResId = ThemeDrawableSource.getResId(blackFileName);
                if (blackResId != -1) {
                    view.setImageResource(blackResId);
                } else {
                    view.setImageResource(sid);
                }
            }
        }

    }

}
