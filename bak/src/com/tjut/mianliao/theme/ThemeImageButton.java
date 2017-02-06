package com.tjut.mianliao.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.tjut.mianliao.settings.Settings;

public class ThemeImageButton extends ImageButton{

    public ThemeImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!Settings.getInstance(context).isNightMode()) {
            return;
        }
        ThemeTool.setSrcImage(this, attrs);
    }

}
