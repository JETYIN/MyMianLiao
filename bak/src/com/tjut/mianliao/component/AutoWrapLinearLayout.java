package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.tjut.mianliao.R;

public class AutoWrapLinearLayout extends ViewGroup {

    /** 标签之间的间距 px */
    private int mItemMargins = 50;

    /** 标签第一行的间距 px */
    private int mFirstLineMargins = 0;

    /** 标签的行间距 px */
    private int mLineMargins = 10;

    private boolean mIsMatchTopMarginWithItem = true;

    private boolean mDynamicSize;

    public AutoWrapLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AutoWrapLinearLayout);
        mDynamicSize = ta.getBoolean(R.styleable.AutoWrapLinearLayout_dynamic_size, false);
    }

    /**
     * mItemMargins --> default is 50px
     *
     * @param margin
     */
    public void setItemMargins(int margin) {
        mItemMargins = margin;
    }

    /**
     * mLineMargins --> default is 10 px
     *
     * @param lineMargin
     */
    public void setLineMargin(int lineMargin) {
        mLineMargins = lineMargin;
    }

    public void setFirstLineMargin(int lineMargin) {
        mFirstLineMargins = lineMargin;
    }

    public void setMarginTopWithItemMargin(boolean isMatch) {
        mIsMatchTopMarginWithItem = isMatch;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int singleHeight = getChildCount() < 2 ? 150 : 85;
        int measureHeight = getChildCount() * singleHeight;
        for (int index = 0; index < getChildCount(); index++) {
            final View child = getChildAt(index);
            child.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mDynamicSize) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(measureHeight, MeasureSpec.EXACTLY));
        } else {
            getLayoutParams().height = getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        final int count = getChildCount();
        arg1 = arg2 =0;
        int row = 0;// which row lay you view relative to parent
        int lengthX = arg1; // right position of child relative to parent
        int lengthY = arg2; // bottom position of child relative to parent
        for (int i = 0; i < count; i++) {
            final View child = this.getChildAt(i);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();
            lengthX += width + mItemMargins;
            if (row == 0) {
                if (mIsMatchTopMarginWithItem) {
                    lengthY = mItemMargins + mFirstLineMargins + height + arg2;
                } else {
                    lengthY = height + arg2 + mFirstLineMargins;
                }
            } else {
                if (mIsMatchTopMarginWithItem) {
                    lengthY = row * (height + mItemMargins + mLineMargins) + mItemMargins + height + arg2;
                } else {
                    lengthY = row * (height + mLineMargins) + height + arg2;
                }
            }
            // if it can't drawing on a same line , skip to next line
            if (lengthX > arg3) {
                lengthX = width + mItemMargins + arg1;
                row++;
                if (mIsMatchTopMarginWithItem) {
                    lengthY = row * (height + mItemMargins + mLineMargins) + mItemMargins + height + arg2;
                } else {
                    lengthY = row * (height + mLineMargins) + height + arg2;
                }
            }
            child.layout(lengthX - width, lengthY - height, lengthX, lengthY);
        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }
}