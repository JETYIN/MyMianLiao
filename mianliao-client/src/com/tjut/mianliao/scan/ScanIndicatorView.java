package com.tjut.mianliao.scan;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ScanIndicatorView extends View {
    //    private static String TAG = "ScanIndicatorView";

    private static final int ANIM_INTERVAL = 30; // milliseconds

    private float mIndicatorHeight = 4; // dp
    private float mSpeed = 70; // dp/sec

    private long mAnimStart;

    private RectF mRect = new RectF();
    private RectF mDirtyRect = new RectF();
    private static Paint sPaint = new Paint();

    static {
        sPaint.setColor(0xFF38D3A9);
    }

    public ScanIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        float density = res.getDisplayMetrics().density;
        mSpeed = density * mSpeed;
        mIndicatorHeight = density * mIndicatorHeight;
        setVisibility(GONE);
    }

    public void start() {
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
            mAnimStart = System.currentTimeMillis();
            nextFrame();
        }
    }

    public void stop() {
        setVisibility(INVISIBLE);
    }

    private void nextFrame() {
        if (getVisibility() != VISIBLE) {
            return;
        }

        long timeElapsed = System.currentTimeMillis() - mAnimStart;

        mDirtyRect.set(mRect);

        mRect.top = timeElapsed * mSpeed / 1000;
        mRect.bottom = mRect.top + mIndicatorHeight;
        if (mRect.bottom > getHeight()) {
            mAnimStart = System.currentTimeMillis();
            mRect.top = 0;
            mRect.bottom = mRect.top + mIndicatorHeight;
            mDirtyRect.union(mRect);
        }

        invalidate((int) mDirtyRect.left, (int) (mDirtyRect.top - mIndicatorHeight / 2),
                (int) mDirtyRect.right, (int) (mDirtyRect.bottom + mIndicatorHeight / 2));

        postDelayed(new Runnable() {
            @Override
            public void run() {
                nextFrame();
            }
        }, ANIM_INTERVAL);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mRect.set(0, 0, getWidth(), mIndicatorHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRect != null) {
            canvas.drawRect(mRect, sPaint);
        }
    }
}
