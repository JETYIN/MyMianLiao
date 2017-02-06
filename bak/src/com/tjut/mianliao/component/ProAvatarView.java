package com.tjut.mianliao.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * The main purpose of ProAvatarView is to draw avatar as a round image with a
 * border, and even a cover.
 * In order to draw the border, it should set padding.
 */
public class ProAvatarView extends AvatarView {

    private RectF mBorderRect;
    private Path mBorderPath;
    private Paint mBorderPaint;

    public ProAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mBorderRect = new RectF();
        mBorderPath = new Path();
        mBorderPath.setFillType(Path.FillType.EVEN_ODD);
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setColor(0x4DFFFFFF);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int padding = getPaddingLeft();
        mBorderRect.set(mViewRect);
        mBorderRect.inset(-padding, -padding);

        mBorderPath.reset();
        if (isRoundRect()) {
            mBorderPath.addRoundRect(mViewRect,
                    mCornerRadius, mCornerRadius, Path.Direction.CW);
            mBorderPath.addRoundRect(mBorderRect,
                    mCornerRadius, mCornerRadius, Path.Direction.CW);
        } else {
            mBorderPath.addCircle(mViewRect.centerX(), mViewRect.centerY(),
                    mViewRect.width() / 2, Path.Direction.CW);
            mBorderPath.addCircle(mViewRect.centerX(), mViewRect.centerY(),
                    mViewRect.width() / 2 + padding, Path.Direction.CW);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int sl = canvas.saveLayer(mBorderRect, null, Canvas.ALL_SAVE_FLAG);
        super.onDraw(canvas);
        canvas.drawPath(mBorderPath, mBorderPaint);
        canvas.restoreToCount(sl);
    }
}
