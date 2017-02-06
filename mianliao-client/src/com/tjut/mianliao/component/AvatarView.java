package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.tjut.mianliao.R;

/**
 * The main purpose of AvatarView is to draw avatar as a round image.
 */
public class AvatarView extends ProImageView {

    private static final int TYPE_CIRCLE = 0;
    private static final int TYPE_ROUND_CORNER = 1;

    protected int mType;
    protected int mCornerRadius;
    protected RectF mViewRect;

    private Path mPurgePath;
    private Paint mPurgePaint;

    private Path mCoverPath;
    private Paint mCoverPaint;

    private boolean mCoverVisible;

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mViewRect = new RectF();

        mPurgePath = new Path();
        mPurgePath.setFillType(Path.FillType.INVERSE_WINDING);
        mPurgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPurgePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mCoverPath = new Path();
        mCoverPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCoverPaint.setColor(0x77000000);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AvatarView);
        mType = ta.getInt(R.styleable.AvatarView_borderType, TYPE_CIRCLE);
        mCornerRadius = ta.getDimensionPixelSize(R.styleable.AvatarView_cornerRadius, 0);
        ta.recycle();
    }

    public boolean isRoundRect() {
        return mType == TYPE_ROUND_CORNER;
    }

    public void setCoverVisible(boolean visible) {
        if (mCoverVisible != visible) {
            mCoverVisible = visible;
            invalidate();
        }
    }

    public void setCoverColor(int color) {
        mCoverPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mViewRect.set(getPaddingLeft(), getPaddingTop(),
                getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());

        mPurgePath.reset();
        if (isRoundRect()) {
            mPurgePath.addRoundRect(mViewRect,
                    mCornerRadius, mCornerRadius, Path.Direction.CW);
        } else {
            mPurgePath.addCircle(mViewRect.centerX(), mViewRect.centerY(),
                    mViewRect.width() / 2, Path.Direction.CW);
        }

        mCoverPath.reset();
        mCoverPath.addRect(mViewRect.left, mViewRect.bottom * 2 / 3,
                mViewRect.right, mViewRect.bottom, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int sl = canvas.saveLayer(mViewRect, null, Canvas.ALL_SAVE_FLAG);
        super.onDraw(canvas);
        if (mCoverVisible) {
            canvas.drawPath(mCoverPath, mCoverPaint);
        }
        canvas.drawPath(mPurgePath, mPurgePaint);
        canvas.restoreToCount(sl);
    }
}
