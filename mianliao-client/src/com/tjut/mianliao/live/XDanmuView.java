package com.tjut.mianliao.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import com.tjut.mianliao.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by YoopWu on 2016/6/16 0016.
 */
public class XDanmuView extends RelativeLayout {
    private int mScreenWidth;
    private List<View> mChildList;
    private int mRowNum = 3;
    private int[] mSpeeds = {
            6000, 6200, 6400, 6600
    };
    private int[] mRowPos = {
            20, 25, 30
    };
    private Random mRandom;

    public static enum XDirection {
        FROM_RIGHT_TO_LEFT,
        FORM_LEFT_TO_RIGHT
    }

    public enum XCAction {
        SHOW, HIDE
    }

    private XDirection mDirection = XDirection.FROM_RIGHT_TO_LEFT;

    public XDanmuView(Context context) {
        this(context, null, 0);
    }

    public XDanmuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XDanmuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScreenWidth = getScreenWidth();
        mChildList = new ArrayList<>();
        mRandom = new Random();
    }

    public void setDirection(XDirection direction) {
        mDirection = direction;
    }

    int lastRow = 0;

    public void addDanmuItemViews(List<View> danmuView) {
        for (View view : danmuView) {
            createDanmuView(view);
        }
    }

    public void addDanmuItemView(View danmuView) {
        createDanmuView(danmuView);
    }

    public void createDanmuView(final View view) {
        Utils.logD("XDanmuView", "create danmu view start");
        RelativeLayout.LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        int row = mRandom.nextInt(100) % mRowNum;
        while (row == lastRow) {
            row = mRandom.nextInt(100) % mRowNum;
        }
        int pos = mRandom.nextInt(100) % mRowNum;
        lp.topMargin = row * mRowPos[pos];
        lastRow = row;
        view.setLayoutParams(lp);
        view.setPadding(40, 2, 40, 2);
        view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        this.addView(view);
        Utils.logD("XDanmuView", "measure view width :" + view.getMeasuredWidth()
                + ",height : " + view.getMeasuredHeight());
        ViewPropertyAnimator animator;
        if (mDirection == XDirection.FROM_RIGHT_TO_LEFT) {
            animator = view.animate().translationXBy(-(mScreenWidth + view.getMeasuredWidth()));
        } else {
            animator = view.animate().translationXBy(mScreenWidth + view.getMeasuredWidth());
        }
        animator.setDuration(mSpeeds[new Random().nextInt(mSpeeds.length)]);
        animator.setInterpolator(new LinearInterpolator());
        animator.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                removeView(view);
            }
        });
        animator.start();
        Utils.logD("XDanmuView", "create danmu view end");
    }

    boolean isFirst = true;

    public void start() {
        switchAnimation(XCAction.SHOW);
        if (isFirst) {
            isFirst = false;
        }
    }

    public void hide() {
        switchAnimation(XCAction.HIDE);
    }

    public void stop() {
        this.setVisibility(View.GONE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int childCount = this.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            RelativeLayout.LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.leftMargin <= 0) {
                if (mDirection == XDirection.FORM_LEFT_TO_RIGHT) {
                    view.layout(-view.getMeasuredWidth(), lp.topMargin,
                            0, lp.topMargin + view.getMeasuredHeight());
                } else {
                    view.layout(mScreenWidth, lp.topMargin + view.getMeasuredHeight(), mScreenWidth + view.getMeasuredWidth(),
                            lp.topMargin + view.getMeasuredHeight() * 2);
                }

            } else {
                continue;
            }
        }
    }


    private void switchAnimation(final XCAction action) {
        AlphaAnimation animation;
        if (action == XCAction.HIDE) {
            animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(400);
        } else {
            animation = new AlphaAnimation(0.0f, 1.0f);
            animation.setDuration(1000);
        }
        XDanmuView.this.startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (action == XCAction.HIDE) {
                    XDanmuView.this.setVisibility(View.GONE);
                } else {
                    XDanmuView.this.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private int getScreenWidth() {
        WindowManager mWm = (WindowManager) this.getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        mWm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
