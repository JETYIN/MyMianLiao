package com.tjut.mianliao.component.nova;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;

public class DanmakuLayout extends FrameLayout {

    private static final int MAX_COUNT = 5;
    private static final long STEP_MILLIS = 5;
    private static final long ADD_MILLIS = 1000;
    private static final long MIN_DURATION_MILLIS = 6000;
    private static final long MAX_DURATION_MILLIS = 8000;
    private static final long CLICK_DURATION_MILLIS = 400;
    private static final long REPLY_DURATION_MILLIS = 700;

    private ArrayList<Danmaku> mRunningDanmakus, mPendingDanmakus;
    private Danmaku mClickedDanmaku;
    private DanmakuHandler mHandler;
    private int mWidth, mHeight;
    private boolean mStarted, mPaused;
    public View mCurrentView;

    private FrameLayout mBlackView;
    private Context mContext;

    public DanmakuLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public DanmakuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public DanmakuLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public boolean isPaused() {
        return mPaused;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        for (int i = 0; i < mRunningDanmakus.size(); i++) {
            Danmaku danmaku = mRunningDanmakus.get(i); 
            danmaku.layout();
        }
        for (int i = 0; i < mPendingDanmakus.size(); i++) {
            Danmaku danmaku = mPendingDanmakus.get(i); 
            danmaku.layout();
        }
    }
    

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    public Danmaku getDanmaku(View view) {
        for (int i = 0; i < mRunningDanmakus.size(); i++) {
            Danmaku danmaku = mRunningDanmakus.get(i); 
            if (danmaku.view == view) {
                return danmaku;
            }
        }
        for (int i = 0; i < mPendingDanmakus.size(); i++) {
            Danmaku danmaku = mPendingDanmakus.get(i); 
            if (danmaku.view == view) {
                return danmaku;
            }
        }
        return null;
    }

    public boolean addDanmaku(View view) {

        return mPendingDanmakus.add(new Danmaku(view));
    }

    public Danmaku removeDanmaku(View view) {
        for (int i = 0; i < mRunningDanmakus.size(); i++) {
            if (mRunningDanmakus.get(i).view == view) {
                removeView(view);
                return mRunningDanmakus.remove(i);
            }
        }
        for (int i = 0; i < mPendingDanmakus.size(); i++) {
            if (mPendingDanmakus.get(i).view == view) {
                removeView(view);
                return mPendingDanmakus.remove(i);
            }
        }
        return null;
    }

    public void clickDanmaku(View view) {
        if (!mPaused) {
            for (int i = 0; i < mRunningDanmakus.size(); i++) {
                Danmaku danmaku = mRunningDanmakus.get(i); 
                if (danmaku.view == view) {
                    this.addBlackView();
                    this.removeView(view);
                    mBlackView.addView(view);
                    mCurrentView = view;
                    pause();
                    mClickedDanmaku = new Danmaku(danmaku);
                    Message msg = Message.obtain();
                    msg.what = DanmakuHandler.CLICK;
                    msg.obj = danmaku;
                    mHandler.sendMessage(msg);
                    break;
                }
            }
        }
    }

    public void replyDanmaku(View view) {
        if (mHandler.hasMessages(DanmakuHandler.REPLY) || mHandler.hasMessages(DanmakuHandler.CLICK)) {
            return;
        }
        // addView(view);
        Danmaku danmaku = new Danmaku(view);
        if (mClickedDanmaku == null) {
            mPendingDanmakus.add(0, danmaku);
            if (mStarted && !mHandler.hasMessages(DanmakuHandler.ADD)) {
                mHandler.sendEmptyMessage(DanmakuHandler.ADD);
            }
        } else {
            mRunningDanmakus.add(0, danmaku);
            Message msg = Message.obtain();
            msg.what = DanmakuHandler.REPLY;
            msg.obj = danmaku;
            mHandler.sendMessage(msg);
        }
    }

    private void addBlackView() {
        FrameLayout.LayoutParams relLayoutParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mBlackView = new FrameLayout(mContext);
        mBlackView.setBackgroundColor(0x88000000);
        this.addView(mBlackView, relLayoutParams);
    }

    private void removeBlackView() {
        if (mBlackView != null) {
            mBlackView.removeAllViews();
            this.removeView(mBlackView);
        }
    }

    public void clear() {
        mStarted = false;
        mPaused = false;
        for (int i = 0; i < mRunningDanmakus.size(); i++) {
            Danmaku danmaku = mRunningDanmakus.get(i); 
            removeView(danmaku.view);
        }
        for (int i = 0; i < mPendingDanmakus.size(); i++) {
            Danmaku danmaku = mPendingDanmakus.get(i); 
            removeView(danmaku.view);
        }
        mRunningDanmakus.clear();
        mPendingDanmakus.clear();
        mHandler.removeCallbacksAndMessages(null);
    }

    public void start() {
        if (mStarted || mPendingDanmakus.isEmpty()) {
            return;
        }
        mStarted = true;
        mPaused = false;
        mHandler.sendEmptyMessage(DanmakuHandler.ADD);
        mHandler.sendEmptyMessage(DanmakuHandler.UPDATE);
    }

    public void end() {
        this.removeBlackView();
        mStarted = false;
        mPaused = false;
        mClickedDanmaku = null;
        mPendingDanmakus.addAll(mRunningDanmakus);
        mRunningDanmakus.clear();
        for (int i = 0; i < mPendingDanmakus.size(); i++) {
            Danmaku danmaku = mPendingDanmakus.get(i); 
            danmaku.endUpdate();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public void resume() {
        if (!mStarted || !mPaused || mHandler.hasMessages(DanmakuHandler.CLICK)
                || mHandler.hasMessages(DanmakuHandler.REPLY)) {
            return;
        }
        this.removeBlackView();

        mClickedDanmaku = null;
        mPaused = false;
        mHandler.sendEmptyMessageDelayed(DanmakuHandler.ADD, ADD_MILLIS);
        mHandler.sendEmptyMessageDelayed(DanmakuHandler.UPDATE, STEP_MILLIS);
        if (mCurrentView != null) {
            mCurrentView.findViewById(R.id.dv_avatar).setBackgroundResource(R.drawable.danmu_avatar_bg);
            mCurrentView.findViewById(R.id.dv_content).setBackgroundResource(R.drawable.danmu_text_bg);
            this.addView(mCurrentView);
            mCurrentView = null;
        }
        ForumPostDetailActivity.mViewBg.setVisibility(View.GONE);
    }

    public void pause() {
        mPaused = true;
        mHandler.removeMessages(DanmakuHandler.ADD);
        mHandler.removeMessages(DanmakuHandler.UPDATE);
        if (mCurrentView != null) {
            mCurrentView.findViewById(R.id.dv_avatar).setBackgroundResource(R.drawable.danmu_avatar_bg_chosed);
            mCurrentView.findViewById(R.id.dv_content).setBackgroundResource(R.drawable.danmu_text_bg_chosed);
        }
        ForumPostDetailActivity.mViewBg.setVisibility(View.VISIBLE);
    }

    private void init() {
        mRunningDanmakus = new ArrayList<Danmaku>();
        mPendingDanmakus = new ArrayList<Danmaku>();
        mHandler = new DanmakuHandler(this);
    }

    private class DanmakuHandler extends Handler {
        private static final int UPDATE = 1;
        private static final int ADD = 2;
        private static final int CLICK = 3;
        private static final int REPLY = 4;

        private WeakReference<DanmakuLayout> mWeakRef;

        public DanmakuHandler(DanmakuLayout layout) {
            mWeakRef = new WeakReference<DanmakuLayout>(layout);
        }

        @Override
        public void handleMessage(Message msg) {
            DanmakuLayout layout = mWeakRef.get();
            if (layout == null) {
                return;
            }

            Danmaku danmaku;
            switch (msg.what) {
                case UPDATE:
                    int i = 0;
                    while (i < mRunningDanmakus.size()) {
                        danmaku = mRunningDanmakus.get(i);
                        if (danmaku.step()) {
                            i++;
                        } else {
                            mPendingDanmakus.add(mRunningDanmakus.remove(i));
                            removeView(danmaku.view);

                            if (!hasMessages(ADD)) {
                                sendEmptyMessage(ADD);
                            }
                        }

                    }

                    invalidate();
                    sendEmptyMessageDelayed(UPDATE, STEP_MILLIS);
                    break;

                case ADD:
                    if (!mPendingDanmakus.isEmpty() && mRunningDanmakus.size() < MAX_COUNT) {
                        danmaku = mPendingDanmakus.remove(0);
                        mRunningDanmakus.add(danmaku);
                        if (danmaku.view.getParent() == null) {
                            addView(danmaku.view);
                        }

                        danmaku.startUpdate();
                        sendEmptyMessageDelayed(ADD, ADD_MILLIS);
                    }
                    break;

                case CLICK:
                    danmaku = (Danmaku) msg.obj;
                    if (danmaku.clicking) {
                        if (danmaku.step()) {
                            sendMessageDelayed(Message.obtain(msg), STEP_MILLIS);
                        } else {
                            danmaku.endClick(mClickedDanmaku);
                            mClickedDanmaku = danmaku;
                        }
                    } else {
                        danmaku.startClick();
                        sendMessage(Message.obtain(msg));
                    }
                    break;

                case REPLY:
                    danmaku = (Danmaku) msg.obj;
                    if (danmaku.replying) {
                        if (danmaku.step()) {
                            sendMessageDelayed(Message.obtain(msg), STEP_MILLIS);
                        } else {
                            danmaku.endReply(mClickedDanmaku);
                            resume();
                        }
                    } else {
                        addView(danmaku.view);
                        danmaku.startReply(mClickedDanmaku);
                        sendMessage(Message.obtain(msg));
                    }
                    break;

                default:
                    danmaku = null;
                    break;
            }
        }
    }

    public class Danmaku {
        private View view;
        private Point from, to;
        private float fraction, step;
        private Interpolator interpolator;
        private boolean clicking, replying;

        public Danmaku(View view) {
            this.view = view;
            from = new Point();
            to = new Point();
        }

        public Danmaku(Danmaku d) {
            view = d.view;
            from = new Point(d.from);
            to = new Point(d.to);
            fraction = d.fraction;
            step = d.step;
            interpolator = d.interpolator;
        }

        public void setInterpolator(Interpolator interpolator) {
            this.interpolator = interpolator;
        }

        public void setFraction(float fraction) {
            this.fraction = fraction;
        }

        public void setStep(float step) {
            this.step = step * 4;
        }

        public void setRangeX(int fromX, int toX) {
            from.x = fromX;
            to.x = toX;
        }

        public void setRangeY(int fromY, int toY) {
            from.y = fromY;
            to.y = toY;
        }

        public void startUpdate() {
            double random = Math.random();
            long duration = MIN_DURATION_MILLIS + (long) (random * (MAX_DURATION_MILLIS - MIN_DURATION_MILLIS) / 1000)
                * 1000;
            setStep(STEP_MILLIS / (float) duration);
            int x = (int) (random * (mWidth)) / 4;
            setRangeX(x, x);
            setRangeY(mHeight, -300);
            setFraction(0);
            moveTo(from.x, from.y);
        }

        public void endUpdate() {
            setRangeX(0, 0);
            setRangeY(mHeight, mHeight);
            setFraction(1f);
            moveTo(to.x, to.y);
        }

        public void startClick() {
            setStep(STEP_MILLIS / (float) CLICK_DURATION_MILLIS);
            setInterpolator(null);
            setRangeX(view.getLeft(), (mWidth - view.getMeasuredWidth()) / 2);
            setRangeY(view.getTop(), mHeight / 2 - view.getMeasuredHeight() * 2);
            setFraction(0);
            clicking = true;
        }

        public void endClick(Danmaku danmaku) {
            double random = Math.random();
            long duration = MIN_DURATION_MILLIS + (long) (random * (MAX_DURATION_MILLIS - MIN_DURATION_MILLIS) / 1000)
                    * 1000;
            setStep(STEP_MILLIS / (float) duration);
            setRangeX(to.x, to.x);
            setRangeY(danmaku.from.y, danmaku.to.y);
            setInterpolator(danmaku.interpolator);
            setFraction((view.getTop() - from.y) / (float) (-300 - from.y));
            clicking = false;
        }

        public void startReply(Danmaku danmaku) {
            setStep(STEP_MILLIS / (float) REPLY_DURATION_MILLIS);
            int x = danmaku.view.getLeft();// + (danmaku.view.getMeasuredWidth() - view.getMeasuredWidth()) / 2;
            setRangeX(x, x);
            setRangeY(mHeight, danmaku.view.getBottom());
            setInterpolator(null);
            setFraction(0);
            replying = true;
        }

        public void endReply(Danmaku danmaku) {

            double random = Math.random();
            long duration = MIN_DURATION_MILLIS + (long) (random * (MAX_DURATION_MILLIS - MIN_DURATION_MILLIS) / 1000)
                    * 1000;
            setStep(STEP_MILLIS / (float) duration);

            setRangeY(danmaku.from.y, danmaku.to.y);
            setInterpolator(danmaku.interpolator);
            setFraction((view.getTop() - from.y) / (float) (-300 - from.y));
            replying = false;
        }

        public boolean step() {
            // fraction = Math.min(fraction + step, 1f);
            fraction += step;
            float interpolation = interpolator == null ? fraction : interpolator.getInterpolation(fraction);
            moveTo(from.x + (int) ((to.x - from.x) * fraction), from.y + (int) ((to.y - from.y) * fraction));
            return fraction < 1f;
        }

        public void moveTo(int x, int y) {
            // y=4*y;
            view.offsetLeftAndRight(x - view.getLeft());
            view.offsetTopAndBottom(y - view.getTop());
        }

        public void layout() {
            int x = from.x + (int) ((to.x - from.x) * fraction);
            int y = from.y + (int) ((to.y - from.y) * fraction);
            if (y == 0 && fraction == 0f) {
                y = mHeight;
            }
            // y=4*y;
            view.layout(x, y, x + view.getMeasuredWidth(), y + view.getMeasuredHeight());
        }
    }
}
