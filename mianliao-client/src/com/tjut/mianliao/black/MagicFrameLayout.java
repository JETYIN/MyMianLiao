package com.tjut.mianliao.black;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tjut.mianliao.R;

public class MagicFrameLayout extends FrameLayout {

    public MagicFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MagicLayout);
        int type = ta.getInteger(R.styleable.MagicLayout_color_magic, -1);
        ta.recycle();
    }

}
