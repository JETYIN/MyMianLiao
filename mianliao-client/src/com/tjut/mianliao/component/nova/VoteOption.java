package com.tjut.mianliao.component.nova;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class VoteOption extends FrameLayout {

    private TextView mTvContent;
    private TextView mTvProgress;
    private RectF mArcRect;
    private RectF mOvalRect;
    private Paint mPaint;
    private float mProgress;
    private int mStrokeWidth;
    private int mOvalMargin;
    private boolean mProgressShown;
    private int mCircleColor;
    private int mContentColor;

    public VoteOption(Context context) {
        super(context);
        init(context);
    }

    public VoteOption(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int delta = mStrokeWidth / 2;
        mArcRect.set(0, 0, w, h);
        mArcRect.inset(delta, delta);
        delta = mOvalMargin;
        mOvalRect.set(0, 0, w, h);
        mOvalRect.inset(delta, delta);
    }

    public void setContent(String content) {
        mTvContent.setText(content);
    }

    public void setColor(int color) {
        mContentColor = color;
        mPaint.setColor(color);
        invalidate();
    }

    public void setProgress(float progress) {
        progress = Math.max(0, Math.min(100, progress));
        if (mProgress != progress) {
//            mTvProgress.setText(String.format("%.0f%%", progress));
            mProgress = progress;
            invalidate();
        }
    }

    public void setProgressTextColor(int color) {
        mTvProgress.setTextColor(color);
    }

    public void setProgressShown(boolean shown) {
        mProgressShown = shown;
        mTvProgress.setVisibility(shown ? VISIBLE : GONE);
        invalidate();
    }
    
    public void setCircleColor(int color) {
        mCircleColor = color;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mProgressShown) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(mCircleColor);
            canvas.drawArc(mArcRect, -90, mProgress * 360 / 100, false, mPaint);
        }
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mContentColor);
        canvas.drawOval(mOvalRect, mPaint);
        super.dispatchDraw(canvas);
    }

    @SuppressLint("NewApi")
    private void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int pad = context.getResources().getDimensionPixelSize(R.dimen.cf_reply_padding_bottom);
        mStrokeWidth = (int) (1 * metrics.density);
        mOvalMargin = (int) (4 * metrics.density);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(mStrokeWidth);
        mArcRect = new RectF();
        mOvalRect = new RectF();

        LayoutParams params = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.setMargins(mOvalMargin, mOvalMargin, mOvalMargin, mOvalMargin);
        params.gravity = Gravity.CENTER;

        mTvContent = new TextView(context);
        mTvContent.setGravity(Gravity.CENTER);
        mTvContent.setTextColor(Color.WHITE);
        mTvContent.setTextSize(10);
        mTvContent.setPadding(pad, pad, pad, pad);
        addView(mTvContent, params);

        mTvProgress = new TextView(context);
        mTvProgress.setGravity(Gravity.CENTER);
        mTvProgress.setTextColor(Color.WHITE);
        mTvProgress.setTextSize(18);
//        mTvProgress.setText("0%");
        ShapeDrawable d = new ShapeDrawable(new OvalShape());
        d.setAlpha(0x00);
     // judge the version_code, avoid NoSuchMethodError
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mTvProgress.setBackground(d);
        } else {
            mTvProgress.setBackgroundDrawable(d);
        }
//        mTvProgress.setBackground(d);
        addView(mTvProgress, params);
    }
}
