package com.tjut.mianliao.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.tjut.mianliao.settings.Settings;

public class ThemeImageView extends ImageView {

    public ThemeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!Settings.getInstance(context).isNightMode()) {
            return;
        }

        ThemeTool.setBackGroundImage(this, attrs);
        ThemeTool.setSrcImage(this, attrs);

    }

}
