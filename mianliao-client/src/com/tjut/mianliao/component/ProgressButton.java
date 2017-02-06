package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class ProgressButton extends FrameLayout {

    private TextView mTvContent;
    private ProgressBar mPbProgress;

    public ProgressButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.comp_progress_button, this, true);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        mPbProgress = (ProgressBar) findViewById(R.id.pb_progress);

        int drawableLeft = 0, drawableTop = 0, drawableRight = 0, drawableBottom = 0;

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextView);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            switch (attr) {
                case R.styleable.TextView_text:
                    setText(ta.getText(attr));
                    break;
                case R.styleable.TextView_textColor:
                    mTvContent.setTextColor(ta.getColorStateList(attr));
                    break;
                case R.styleable.TextView_textSize:
                    mTvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            ta.getDimensionPixelSize(attr, 18));
                    break;
                case R.styleable.TextView_drawableLeft:
                    drawableLeft = ta.getResourceId(attr, drawableLeft);
                    break;
                case R.styleable.TextView_drawableTop:
                    drawableTop = ta.getResourceId(attr, drawableTop);
                    break;
                case R.styleable.TextView_drawableRight:
                    drawableRight = ta.getResourceId(attr, drawableRight);
                    break;
                case R.styleable.TextView_drawableBottom:
                    drawableBottom = ta.getResourceId(attr, drawableBottom);
                    break;
                case R.styleable.TextView_drawablePadding:
                    mTvContent.setCompoundDrawablePadding(ta.getDimensionPixelSize(attr, 0));
                    break;
                default:
                    break;
            }
        }
        ta.recycle();

        mTvContent.setCompoundDrawablesWithIntrinsicBounds(
                drawableLeft, drawableTop, drawableRight, drawableBottom);
    }

    public void setCompoundDrawables(int drawableLeft, int drawableTop,
                                     int drawableRight, int drawableBottom) {
        mTvContent.setCompoundDrawablesWithIntrinsicBounds(
                drawableLeft, drawableTop, drawableRight, drawableBottom);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mTvContent.setEnabled(enabled);
        mPbProgress.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public ProgressButton setInProgress(boolean inProgress) {
        mTvContent.setVisibility(inProgress ? INVISIBLE : VISIBLE);
        mPbProgress.setVisibility(inProgress ? VISIBLE : GONE);
        return this;
    }

    public TextView getTextView() {
        return mTvContent;
    }

    public boolean isInProgress() {
        return mPbProgress.getVisibility() == VISIBLE;
    }

    public ProgressButton setText(CharSequence content) {
        mTvContent.setText(content);
        return this;
    }

    public ProgressButton setText(int content) {
        mTvContent.setText(content);
        return this;
    }

    public ProgressButton setTextColor(int color) {
        mTvContent.setTextColor(color);
        return this;
    }
}
