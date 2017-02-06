package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.tjut.mianliao.R;

public class PageIndicator extends View {
    private static final int INDICATOR_COLOR = 0x33ffffff;
    private static final int INDICATOR_COLOR_BLACK = 0x32FFFFFF;
    private static final int INDICATOR_COLOR_HL = 0x99ffffff;
    private static final int INDICATOR_COLOR_HL_BLACK = 0x5AFFFFFF;

    private int mIndicatorSize = 30; // pixels
    private int mRadius = mIndicatorSize / 4;
    private int mCenterY = mIndicatorSize / 2;

    private int mDotColor = INDICATOR_COLOR;
    private int mDotColorSelected = INDICATOR_COLOR_HL;

    private int mNumPages;
    private int mCurrentPage;

    private Paint mPaint;
    private Paint mPaintShadow;
    private Paint mPaintCurrent;
    
    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator);
        int count = ta.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = ta.getIndex(i);
            switch (attr) {
                case R.styleable.PageIndicator_dotColor:
                    mDotColor = ta.getColor(attr, INDICATOR_COLOR);
                    break;
                case R.styleable.PageIndicator_dotColorSelected:
                    mDotColorSelected = ta.getColor(attr, INDICATOR_COLOR_HL);
                    break;
                case R.styleable.PageIndicator_dotSize:
                    mIndicatorSize = ta.getDimensionPixelSize(attr, 30);
                    mRadius = mIndicatorSize / 4;
                    mCenterY = mIndicatorSize / 2;
                    break;
                default:
                    break;
            }
        }
        ta.recycle();
    }

    public void setNumPages(int numPages) {
        if (mPaint == null) {
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(mDotColor);
        }
        if (mPaintCurrent == null) {
            mPaintCurrent = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintCurrent.setColor(mDotColorSelected);
        }
        if (mPaintShadow == null) {
            mPaintShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintShadow.setColor(0xff888888);
//            mPaintShadow.setStyle(Paint.Style.STROKE);
        }
        if (mNumPages != numPages) {
            mNumPages = numPages;
            requestLayout();
        }
    }

    public void setCurrentPage(int currentPage) {
        if (mCurrentPage == currentPage) {
            return;
        }
        mCurrentPage = currentPage;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mNumPages * mIndicatorSize, mIndicatorSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mNumPages == 0) {
            return;
        }
        for (int i = 0; i < mNumPages; i++) {
            int x = i * mIndicatorSize + mIndicatorSize / 2;
            canvas.drawCircle(x, mCenterY, mRadius, mPaintShadow);
            if (i == mCurrentPage) {
                canvas.drawCircle(x, mCenterY, mRadius, mPaintCurrent);
            } else {
                canvas.drawCircle(x, mCenterY, mRadius, mPaint);
            }
        }
    }
}
