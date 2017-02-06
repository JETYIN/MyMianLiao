package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ExpandableGridView extends GridView {
    public ExpandableGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST));
        getLayoutParams().height = getMeasuredHeight();
    }
}