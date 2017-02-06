package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Silva on 2016/7/14.
 */
public class MyStickyPtrGridView extends StickyPtrGridView{
    public MyStickyPtrGridView(Context context) {
        this(context, null);
    }

    public MyStickyPtrGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
