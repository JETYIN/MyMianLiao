package com.tjut.mianliao.live;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by YoopWu on 2016/7/20 0020.
 */
public class LiveViewPager extends ViewPager {

    public LiveViewPager(Context context) {
        this(context, null);
    }

    public LiveViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = super.onInterceptTouchEvent(ev);
        if (ret)
            getParent().requestDisallowInterceptTouchEvent(true);
        return ret;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean ret = super.onTouchEvent(ev);
        if (ret)
            getParent().requestDisallowInterceptTouchEvent(true);
        return ret;
    }
}
