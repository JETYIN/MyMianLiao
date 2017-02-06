package com.tjut.mianliao.component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by YoopWu on 2016/7/7 0007.
 */
public class VerticalPager extends ViewGroup {


    private Scroller mScroller;  //滚动条

    private Context mContext;

    private final static int RATE = 5; //速率标准

    private final static int DISTANCE = 300;//需要滚动的距离


    private VelocityTracker mVelocityTracker;//通过此类可以计算速度


    public VerticalPager(Context context, AttributeSet attrs) {

        super(context, attrs);

        this.mContext = context;

        mScroller = new Scroller(context);


    }


    @Override

    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int totalHeight = 0;

        int count = getChildCount();


        for (int i = 0; i < count; i++) {

            View childView = getChildAt(i);

            childView.layout(l, totalHeight, r, totalHeight + b);

            totalHeight += b;

        }

    }


    @Override

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = MeasureSpec.getSize(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);


        int count = getChildCount();

        for (int i = 0; i < count; i++) {

            getChildAt(i).measure(width, height);

        }

        setMeasuredDimension(width, height);

    }


    private int mLastMotionY;

    @Override

    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {

            mVelocityTracker = VelocityTracker.obtain();

        }

        mVelocityTracker.addMovement(event);


        int action = event.getAction();


        float y = event.getY();


        switch (action) {

            case MotionEvent.ACTION_DOWN:

                if (!mScroller.isFinished()) {

                    mScroller.abortAnimation();

                }

                mLastMotionY = (int) y;

                Log.d("montion", "" + getScrollY());

                break;

            case MotionEvent.ACTION_MOVE:

                int deltaY = (int) (mLastMotionY - y);

                scrollBy(0, deltaY);

                invalidate();

                mLastMotionY = (int) y;

                break;

            case MotionEvent.ACTION_UP:


                mVelocityTracker.computeCurrentVelocity(1, 1000); //单位为1说明，一秒一个像素，最大值为1000

                float vy = mVelocityTracker.getYVelocity();  //vy代表Y轴方向的速率

                Log.i("test", "velocityTraker : " + mVelocityTracker.getYVelocity());

                if (getScrollY() < 0) {

                    mScroller.startScroll(0, -DISTANCE, 0, DISTANCE);

                } else if (getScrollY() > (getHeight() * (getChildCount() - 1))) {

                    View lastView = getChildAt(getChildCount() - 1);

                    mScroller.startScroll(0, lastView.getTop() + DISTANCE, 0, -DISTANCE);

                } else {

                    int position = getScrollY() / getHeight();

                    View positionView = null;

                    if (vy < -RATE) {  //下滑

                        positionView = getChildAt(position + 1);

                        mScroller.startScroll(0, positionView.getTop() - DISTANCE, 0, +DISTANCE);

                    } else if (vy > RATE) {//上滑

                        positionView = getChildAt(position);

                        mScroller.startScroll(0, positionView.getTop() - DISTANCE, 0, +DISTANCE);

                    } else {

                        int mod = getScrollY() % getHeight();

                        if (mod > getHeight() / 2) {

                            positionView = getChildAt(position + 1);

                            mScroller.startScroll(0, positionView.getTop() - DISTANCE, 0, +DISTANCE);

                        } else {

                            positionView = getChildAt(position);

                            mScroller.startScroll(0, positionView.getTop() + DISTANCE, 0, -DISTANCE);

                        }

                    }

                }

                invalidate();

                break;

        }

        return true; //返回true表示事件由本View消费掉

    }


    @Override
    public void computeScroll() {

        super.computeScroll();

        if (mScroller.computeScrollOffset()) {

            scrollTo(0, mScroller.getCurrY());

        }

    }


}
