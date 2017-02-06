package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridView;
import android.widget.ScrollView;

import com.tjut.mianliao.util.Utils;

import java.util.logging.LogManager;

/**
 * Created by YoopWu on 2016/7/7 0007.
 */
public class ExpandGridView extends GridView {

    private ScrollView mParentScrollView;

    public ExpandGridView(Context context) {
        super(context);
    }

    public ExpandGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setParentScrollView(ScrollView scrollView) {
        mParentScrollView = scrollView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,

                MeasureSpec.AT_MOST);
        Utils.logD("Live", "onMesure called");
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
