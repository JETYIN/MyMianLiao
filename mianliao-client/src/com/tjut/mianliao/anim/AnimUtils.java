package com.tjut.mianliao.anim;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import com.tjut.mianliao.util.Utils;

import org.lasque.tusdk.core.listener.AnimationListenerAdapter;

import java.util.Random;

/**
 * Created by YoopWu on 2016/6/16 0016.
 */
public class AnimUtils {

    private Random mRandom = new Random();

    /**
     * 心跳动画
     * @param view
     */
    public static void playHeartbeatAnimation(final View view, final Animation.AnimationListener listener) {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new ScaleAnimation(1.0f, 5f, 1.0f, 5f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f));
        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.4f));

        animationSet.setDuration(30);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setFillAfter(true);

        animationSet.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                AnimationSet animationSet = getAnimationSet(5f, 0.5f, 5f, 0.5f, 100);
                animationSet.setInterpolator(new DecelerateInterpolator());
                animationSet.addAnimation(new AlphaAnimation(0.4f, 1.0f));
                animationSet.setAnimationListener(new AnimationListenerAdapter(){
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        AnimationSet animationSet = getAnimationSet(0.5f, 1.3f, 0.5f, 1.3f, 100);
                        animationSet.setInterpolator(new AccelerateInterpolator());
                        animationSet.addAnimation(new AlphaAnimation(1.0f, 0.4f));
                        animationSet.setAnimationListener(new AnimationListenerAdapter(){
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                super.onAnimationEnd(animation);
                                AnimationSet animationSet = getAnimationSet(1.3f, 1.0f, 1.3f, 1.0f, 100);
                                animationSet.setInterpolator(new DecelerateInterpolator());
                                animationSet.addAnimation(new AlphaAnimation(0.4f, 1.0f));
                                animationSet.setAnimationListener(new AnimationListenerAdapter(){
                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        super.onAnimationEnd(animation);
                                        view.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                view.setVisibility(View.INVISIBLE);
                                            }
                                        }, 500);
                                        if (listener != null) {
                                            listener.onAnimationEnd(animation);
                                        }
                                    }
                                });
                                view.startAnimation(animationSet);
                            }
                        });
                        view.startAnimation(animationSet);
                    }
                });
                view.startAnimation(animationSet);
            }
        });

        // 实现心跳的View
        view.startAnimation(animationSet);
        view.setVisibility(View.VISIBLE);
    }

    @NonNull
    private static AnimationSet getAnimationSet(float fromX, float toX, float fromY, float toY,
                                                int duration) {
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(new ScaleAnimation(fromX, toX, fromY,
                toY, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f));

        animationSet.setDuration(duration);
        animationSet.setFillAfter(false);
        return animationSet;
    }

    public static final void startSpecialGiftMoveAnim(final View view) {
        AnimationSet set = new AnimationSet(false);
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        final int width = Utils.getDisplayWidth();
        final int height = Utils.getDisplayHeight();
        final float toX = (width - view.getMeasuredWidth()) / 2;
        final float toY = (height - view.getMeasuredHeight()) / 2;
        Animation animation = new TranslateAnimation(0f, toX, 0f, toY);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.6f, 1f);
        ScaleAnimation scaleAnimation = new ScaleAnimation(0.1f, 1f, 0.1f, 1f);
        set.addAnimation(animation);
        set.addAnimation(alphaAnimation);
        set.addAnimation(scaleAnimation);
        set.setDuration(2000);
        set.setFillAfter(true);
        set.setAnimationListener(new AnimationListenerAdapter(){
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Animation animation = new TranslateAnimation(toX, width, toY,
                                toY + view.getMeasuredHeight());
                        animation.setDuration(1000);
                        animation.setAnimationListener(new AnimationListenerAdapter(){
                            @Override
                            public void onAnimationEnd(Animation animation) {
                                super.onAnimationEnd(animation);
                                view.setVisibility(View.GONE);
                            }
                        });
                        view.startAnimation(animation);
                    }
                }, 3500);
            }
        });
        view.setVisibility(View.VISIBLE);
        view.startAnimation(set);
    }

    public static void heartAnim(View view) {
//        PropertyValuesHolder xholder = PropertyValuesHolder.ofFloat("scaleX", 1f, 5f, 0.5f, 1.8f, 1.0f);
//        PropertyValuesHolder yholder = PropertyValuesHolder.ofFloat("scaleY", 1f, 5f, 0.5f, 1.8f, 1.0f);
        ObjectAnimator.ofPropertyValuesHolder(view,
                PropertyValuesHolder.ofFloat("scaleX", 1f, 5f, 0.5f, 1.8f, 1.0f),
                PropertyValuesHolder.ofFloat("scaleY", 1f, 5f, 0.5f, 1.8f, 1.0f)
                )
                .setDuration(800)
                .start();
    }

}
