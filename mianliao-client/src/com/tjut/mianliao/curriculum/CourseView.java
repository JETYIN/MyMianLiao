package com.tjut.mianliao.curriculum;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class CourseView extends TextView {
    private boolean mShowFold;
    private boolean mCourseClosed;
    private boolean mAuthUser = true;

    private Drawable mFoldDrawable;
    private Drawable mClosedDrawable;
    private Drawable mAuthUserDrawable;
    private Drawable mFootDrawable;

    private static int sFoldIconSize;
    private static int sFootHeight;
    private static int sFootIconSize;
    private static int sPaddingBottom;

    public CourseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private static synchronized void init(Context context) {
        if (sFoldIconSize == 0) {
            Resources res = context.getResources();
            sFoldIconSize = res.getDimensionPixelSize(R.dimen.cur_cell_fold_size);
            sFootIconSize = res.getDimensionPixelSize(R.dimen.cur_cell_foot_icon_size);
            sFootHeight = res.getDimensionPixelSize(R.dimen.cur_cell_foot_height);
            sPaddingBottom = res.getDimensionPixelSize(R.dimen.cur_cell_padding_bottom);
        }
    }

    public void setShowFold(boolean showFold) {
        if (mShowFold == showFold) {
            return;
        }
        mShowFold = showFold;
        mFoldDrawable = mShowFold ? getResources().getDrawable(R.drawable.ic_course_fold) : null;
        updateDrawableBounds();
    }

    public void setCourseClosed(boolean closed) {
        if (mCourseClosed == closed) {
            return;
        }
        mCourseClosed = closed;
        mClosedDrawable = mCourseClosed ?
                getResources().getDrawable(R.drawable.ic_course_closed) : null;
        updateDrawableBounds();
    }

    public void setAuthUser(boolean authUser) {
        if (mAuthUser == authUser) {
            return;
        }
        mAuthUser = authUser;
        mAuthUserDrawable = mAuthUser ? null :
                getResources().getDrawable(R.drawable.ic_course_extra);
        updateDrawableBounds();
    }

    private void updateDrawableBounds() {
        if (mFoldDrawable != null) {
            mFoldDrawable.setBounds(getWidth() - sFoldIconSize, 0, getWidth(), sFoldIconSize);
        }
        if (mClosedDrawable != null) {
            mClosedDrawable.setBounds(getWidth() - sFootIconSize,
                    getHeight() - sFootIconSize, getWidth(), getHeight());
        }
        if (mAuthUserDrawable != null) {
            int shift = mClosedDrawable == null ? 0 : sFootIconSize;
            mAuthUserDrawable.setBounds(getWidth() - sFootIconSize - shift,
                    getHeight() - sFootIconSize, getWidth() - shift, getHeight());
        }

        if (showFootBar()) {
            if (mFootDrawable == null) {
                mFootDrawable = getResources().getDrawable(R.drawable.bg_course_item_foot);
            }
            mFootDrawable.setBounds(0, getHeight() - sFootIconSize, getWidth(), getHeight());
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), sFootHeight);
        } else {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), sPaddingBottom);
        }
    }

    private boolean showFootBar() {
        return mClosedDrawable != null || mAuthUserDrawable != null;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateDrawableBounds();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFoldDrawable != null) {
            mFoldDrawable.draw(canvas);
        }
        if (showFootBar()) {
            mFootDrawable.draw(canvas);
        }
        if (mClosedDrawable != null) {
            mClosedDrawable.draw(canvas);
        }
        if (mAuthUserDrawable != null) {
            mAuthUserDrawable.draw(canvas);
        }
    }
}
