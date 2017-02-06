package com.tjut.mianliao.component;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;

import com.nineoldandroids.animation.IntEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.tjut.mianliao.profile.ProfileFragment;
import com.tjut.mianliao.profile.ProfileFragment.StickyScrollCallBack;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class StickyScrollView extends ScrollView {
    
    private int lastTouchAction;
    private StickyScrollCallBack scrollCallBack;

    private DetectThread detectThread;
    private boolean needDetect = false;
    private int lastDetectScrollY = DETECT_SCROLL_INVALID;
    private static final int DETECT_SCROLL_INIT = -1;
    private static final int DETECT_SCROLL_INVALID = -2;

    public StickyScrollView(Context context) {
        this(context, null);
    }

    public StickyScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StickyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOverScrollMode(OVER_SCROLL_ALWAYS);

        needDetect = true;
        detectThread = new DetectThread();
        detectThread.start();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (scrollCallBack != null && scrollCallBack.getCurrentViewpagerItem() > 1 ) {
            return;
        }

        if (null != scrollCallBack) {
            int stickyTranslate = t;
            if (t > ProfileFragment.sStickyTopToTab) {
                stickyTranslate = ProfileFragment.sStickyTopToTab;
            }
            scrollCallBack.onScrollChanged(-stickyTranslate);
        }
    }

    public void setScrollCallBack(StickyScrollCallBack scrollCallBack) {
        this.scrollCallBack = scrollCallBack;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        lastTouchAction = ev.getAction() & MotionEvent.ACTION_MASK;

        if (lastTouchAction == MotionEvent.ACTION_DOWN) {
            lastDetectScrollY = DETECT_SCROLL_INIT;
            animScrollY();
        }

        return super.dispatchTouchEvent(ev);
    }
    
    

    private void animScrollY() {
        int thisScrollY = getScrollY();
        if (thisScrollY == 0 || thisScrollY >= ProfileFragment.sStickyTopToTab) {
            return;
        }

        int scrollY = thisScrollY;
//        if (thisScrollY > ProfileFragment.sStickyTopToTab / 2) {
//            scrollY = ProfileFragment.sStickyTopToTab;
//        }

        ValueAnimator scrollAnim = ObjectAnimator.ofInt(this, "scrollY", scrollY);
        scrollAnim.setDuration(200);
        scrollAnim.setEvaluator(new IntEvaluator());
        scrollAnim.start();
    }

    class DetectThread extends Thread {
        private Message msg;

        @Override
        public void run() {
            while (needDetect) {
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                msg = handler.obtainMessage();
                msg.sendToTarget();
            }
        };
    };

    @Override
    protected void onDetachedFromWindow() {
        needDetect = false;
        detectThread = null;
    };

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            int thisScrollY = getScrollY();
            if (lastDetectScrollY != DETECT_SCROLL_INVALID && lastTouchAction == MotionEvent.ACTION_UP
                    && lastDetectScrollY == thisScrollY) {
                animScrollY();
                lastDetectScrollY = DETECT_SCROLL_INVALID;
            } else if (lastDetectScrollY != DETECT_SCROLL_INVALID) {
                lastDetectScrollY = thisScrollY;
            }
        };
    };

    public void invalidScroll() {
        lastDetectScrollY = DETECT_SCROLL_INVALID;
    }
    
    public int getContentHeight(){
        return getChildAt(0).getHeight();
    }
}
