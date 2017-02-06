package com.tjut.mianliao.component;

import ru.truba.touchgallery.GalleryWidget.GalleryViewPager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * This class is a workaround to solve the Exception in view pager:
 * java.lang.IllegalArgumentException: pointerIndex out of range
 */
public class GalleryViewPagerW extends GalleryViewPager {

    public GalleryViewPagerW(Context context) {
        super(context);
    }

    public GalleryViewPagerW(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (IllegalArgumentException e) {
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            return super.onTouchEvent(event);
        } catch (IllegalArgumentException e) {
        }
        return false;
    }
}
