package com.tjut.mianliao.black;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tjut.mianliao.R;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.theme.ThemeTool;

public class MagicRelativeLayout extends RelativeLayout {

    public MagicRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MagicLayout);
        int type = ta.getInteger(R.styleable.MagicLayout_color_magic, -1);
        ta.recycle();

        if (!Settings.getInstance(context).isNightMode()) {
            return;
        }
        if (type == 0) {
            this.setBackgroundResource(0);
            this.setBackgroundColor(Color.parseColor("#4D000000"));
        }
        else
        {
            ThemeTool.setBackGroundImage(this, attrs);
        }
    }

}
