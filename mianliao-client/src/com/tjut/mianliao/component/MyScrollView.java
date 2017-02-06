package com.tjut.mianliao.component;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.tjut.mianliao.util.Utils;


public class MyScrollView extends ScrollView{

    private GestureDetector mGestureDetector;

    private OnScrollListener onScrollListener;
    private boolean mScrollable;
    private boolean mIsCirticalPoint;

    public MyScrollView(Context context) {  
        this(context, null);  
    }  
      
    public MyScrollView(Context context, AttributeSet attrs) {  
        this(context, attrs, 0);  
    }  
  
    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);
        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);
    }  
      
    public void setOnScrollListener(OnScrollListener onScrollListener) {  
        this.onScrollListener = onScrollListener;  
    }

    public void setIsCirticalPoint (boolean isCirticalPoint) {
        mIsCirticalPoint = isCirticalPoint;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean superValue = super.onInterceptTouchEvent(ev);
        Utils.logD("Live", "onInterceptTouchEvent superValue is " + superValue);
        return mGestureDetector.onTouchEvent(ev) && superValue;
    }

    @Override  
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(onScrollListener != null){
            onScrollListener.onScroll(t);
        }
    }

    public void setScrollable (boolean srcollable) {
        mScrollable = srcollable;
    }
  
    public interface OnScrollListener{  
        void onScroll(int scrollY);
    }

    class YScrollDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Utils.logD("Live", "distanceX=" + distanceX + ",distanceY=" + distanceY + ", scrollable=" + mScrollable);
            //方案一：(代码最简)
            if (mIsCirticalPoint) {
                return distanceY > 0;
            } else {
                if (distanceY < 0) {
                    return !mScrollable;
                }
                return mScrollable;
            }
            // 方案二
//            if (distanceY < 0) {
//                if (mIsCirticalPoint) {
//                    return true;
//                }
////                return Math.abs(distanceY) > Math.abs(distanceX) && !mScrollable;
//                return !mScrollable;
//            } else {
//                if (mIsCirticalPoint) {
//                    return true;
//                }
////                return Math.abs(distanceY) > Math.abs(distanceX) && mScrollable;
//                return mScrollable;
//            }
        }
    }


}
