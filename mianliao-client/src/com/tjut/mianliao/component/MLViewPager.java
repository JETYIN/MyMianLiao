package com.tjut.mianliao.component;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class MLViewPager extends ViewPager {
    private ViewGroup parent;
    ViewPagerTouchListener touchListener;

    public void setTouchListener(ViewPagerTouchListener listener) {
        touchListener = listener;
    }

    public MLViewPager(Context context) {
        super(context);
    }

    public MLViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setNestedpParent(ViewGroup parent) {
        this.parent = parent;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(arg0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (touchListener != null) {
            if (arg0.getAction() == MotionEvent.ACTION_MOVE) {
                touchListener.onTouchMove();
            } else if (arg0.getAction() == MotionEvent.ACTION_UP) {
                touchListener.onTouchEnd();
            }
        }
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        return super.onTouchEvent(arg0);
    }

}

interface ViewPagerTouchListener {
    void onTouchMove();

    void onTouchEnd();
}
