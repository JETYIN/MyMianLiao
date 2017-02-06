package com.tjut.mianliao.component;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Show image which fill width of the ImageView, and scale height accordingly.
 */
public class FillWidthImageView extends ProImageView {

    private float mScaleFactor = 1;

    public FillWidthImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdjustViewBounds(true);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        updateMatrix();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * mScaleFactor));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateMatrix();
        }
    }

    private void updateMatrix() {
        Drawable d = getDrawable();
        if (d == null || ScaleType.MATRIX != getScaleType()) {
            return;
        }
        float dWidth = d.getIntrinsicWidth();
        float dHeight = d.getIntrinsicHeight();
        mScaleFactor = dHeight / dWidth;
        float vWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        Matrix matrix = new Matrix();
        float scale = vWidth / dWidth;
        matrix.setScale(scale, scale);
        setImageMatrix(matrix);
    }
}
