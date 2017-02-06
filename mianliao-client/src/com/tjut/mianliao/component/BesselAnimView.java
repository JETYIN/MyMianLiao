package com.tjut.mianliao.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore.Audio.Media;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tjut.mianliao.R;
import com.tjut.mianliao.live.BessalEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by YoopWu on 2016/6/15 0015.
 */
public class BesselAnimView extends RelativeLayout {

	private int[] mDrawableResIds = new int[] { R.drawable.ic_rating_bad,
			R.drawable.ic_rating_good, R.drawable.ic_rating_normal};

	public static final int END_Y_RANDOM = 0x00;
	public static final int END_Y_TOP = 0x01;
	public static final int END_Y_WITH_HALF = 0x02;
	public static final int END_X_RANDOM = 0x03;
	public static final int END_X_RIGHT = 0x04;
	public static final int END_X_LEFT = 0x05;
	public static final int END_X_CENTER_ONLY = 0x06;
	
	private Drawable[] mAnimViews;

	private List<Interpolator> mInterpolators;

	private LayoutParams mLayoutParms;

	private int mDrawbleWidth;

	private int mDrawbleHeight;

	private int mLayoutWidth;

	private int mLayoutHeight;

	private int mEndYPosition = END_Y_TOP;

	private int mEndXPosition = END_X_RANDOM;
	
	private Random mRandom;

	public BesselAnimView(Context context) {
		this(context, null);
	}

	public BesselAnimView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mAnimViews = new Drawable[mDrawableResIds.length];
		for (int i = 0; i < mDrawableResIds.length; i++) {
			Drawable drawable = getResources().getDrawable(mDrawableResIds[i]);
			mAnimViews[i] = drawable;
			if (mDrawbleWidth == 0 || mDrawbleHeight == 0) {
				mDrawbleWidth = drawable.getIntrinsicWidth();
				mDrawbleHeight = drawable.getIntrinsicHeight();
			}
		}
		mLayoutParms = new LayoutParams(mDrawbleWidth, mDrawbleHeight);
		mRandom = new Random();
		addInterpolators();
	}

	private void addInterpolators() {
		mInterpolators = new ArrayList<Interpolator>();
		mInterpolators.add(new AccelerateInterpolator(2));
		mInterpolators.add(new OvershootInterpolator());
		mInterpolators.add(new AccelerateDecelerateInterpolator());
		mInterpolators.add(new LinearInterpolator());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mLayoutWidth = getMeasuredWidth();
		mLayoutHeight = getMeasuredHeight();
		if (mLayoutParms.rightMargin == 0 && mDrawbleWidth > 0) {
			mLayoutParms.addRule(ALIGN_PARENT_BOTTOM);
			mLayoutParms.addRule(ALIGN_PARENT_RIGHT);
			mLayoutParms.rightMargin = mLayoutWidth / 5 - mDrawbleWidth;
			mLayoutParms.bottomMargin = mDrawbleHeight;
		}
	}

	public void addView() {
		if (mLayoutWidth == 0 || mLayoutHeight == 0) {
			return;
		}
		final ImageView imageView = new ImageView(getContext());
		imageView.setImageResource(mDrawableResIds[mRandom
				.nextInt(mDrawableResIds.length)]);
		imageView.setLayoutParams(mLayoutParms);
		addView(imageView);
		AnimatorSet set = getAnimatorSet(imageView);
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				removeView(imageView);
			}
		});
		set.start();
	}
	
	public void setViewEndYPosition(int pos) {
		mEndYPosition = pos;
	}

	public void setViewEndXPosition(int pos) {
		mEndXPosition = pos;
	}
	
	private AnimatorSet getAnimatorSet(ImageView iv) {
		// alpha animation
		ObjectAnimator aplhaAnimator = ObjectAnimator.ofFloat(iv, "alpha", 0.3f, 1.0f);
		// scale animation
		ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(iv, "scaleX", 0.1f, 1.0f);
		ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(iv, "scaleY", 0.1f, 1.0f);
		AnimatorSet set = new AnimatorSet();
		set.setDuration(1000);
		set.playTogether(aplhaAnimator, scaleXAnim, scaleYAnim);
		set.setTarget(iv);
		// Bessel animation
		ValueAnimator bessalAnimator = getBessalAnimator(iv);
		AnimatorSet bessalSet = new AnimatorSet();
		bessalSet.playTogether(bessalAnimator, set);
		bessalSet.setTarget(iv);
		return bessalSet;
	}

	private ValueAnimator getBessalAnimator(final ImageView iv) {
		PointF p0 = new PointF(mLayoutWidth - mLayoutWidth / 5, mLayoutHeight - mDrawbleHeight * 2);
		PointF p1 = getPointF(1);
		PointF p2 = getPointF(2);
		int endY = 0;
		int endX = 0;
		if (mEndXPosition == END_Y_RANDOM) {
			endY = mLayoutHeight - mRandom.nextInt(mLayoutHeight / 2);
		} else if (mEndYPosition == END_Y_TOP) {
			endY = 0;
		} else if (mEndYPosition == END_Y_WITH_HALF) {
			endY = mLayoutHeight / 2;
		}
		if (mEndXPosition == END_X_RANDOM) {
			endX = mRandom.nextInt(mLayoutWidth);
		} else if (mEndXPosition == END_X_RIGHT) {
			endX = mLayoutWidth / 2 + mRandom.nextInt(mLayoutWidth / 2);
		} else if (mEndXPosition == END_X_LEFT) {
			endX = mRandom.nextInt(mLayoutWidth / 2);
		} else if (mEndXPosition == END_X_CENTER_ONLY) {
			endX = mRandom.nextInt(mLayoutWidth / 2) + mLayoutWidth / 4;
		}
		PointF p3 = new PointF(endX, endY);
		BessalEvaluator evaluator = new BessalEvaluator(p1);
		ValueAnimator animator = ValueAnimator.ofObject(evaluator, p0, p3);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				PointF pointF = (PointF) animation.getAnimatedValue();
				iv.setX(pointF.x);
				iv.setY(pointF.y);
				iv.setAlpha(1 - animation.getAnimatedFraction());
			}
		});
		animator.setInterpolator(new AccelerateDecelerateInterpolator());
		animator.setDuration(4000);
		animator.setTarget(iv);
		return animator;
	}

	private PointF getPointF(int i) {
		PointF pointF = new PointF();
		pointF.x = mLayoutWidth - mRandom.nextInt(mLayoutWidth / 5);
		if (i == 2) {
			pointF.y = mRandom.nextInt(mLayoutHeight / 2) + mLayoutHeight / 4;
		} else {
			pointF.y = mRandom.nextInt(mLayoutHeight / 2) + mLayoutHeight / 3;
		}
		if(pointF.y > mLayoutParms.topMargin){
			pointF.y = pointF.y - mRandom.nextInt(mLayoutHeight / 5);
		}
		return pointF;
	}

}
