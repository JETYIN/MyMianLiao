package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class BlockButton extends LinearLayout {

    TextView mTvLeft, mTvRight;

    BlockButtonClickListener mBlockButtonListener;

    public BlockButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.button_block, this);
        this.initComponents();
        this.fillProperties(context, attrs);
    }

    public void setClicklistener(BlockButtonClickListener listener) {
        mBlockButtonListener = listener;
    }

    public void initComponents() {
        mTvLeft = (TextView) this.findViewById(R.id.tv_left);
        mTvRight = (TextView) this.findViewById(R.id.tv_right);

        mTvLeft.setOnClickListener(listener);
        mTvRight.setOnClickListener(listener);

    }

    public void fillProperties(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BlockButton);
        int leftImageResId = ta.getResourceId(R.styleable.BlockButton_block_button_left_image, 0);
        int rightImageResId = ta.getResourceId(R.styleable.BlockButton_block_button_right_image, 0);
        int leftText = ta.getResourceId(R.styleable.BlockButton_block_button_left_text, 0);
        int rightText = ta.getResourceId(R.styleable.BlockButton_block_button_right_text, 0);
        int bgResId = ta.getResourceId(R.styleable.BlockButton_block_button_background, 0);

        ta.recycle();

        mTvLeft.setCompoundDrawablesWithIntrinsicBounds(leftImageResId, 0, 0, 0);
        mTvRight.setCompoundDrawablesWithIntrinsicBounds(rightImageResId, 0, 0, 0);
        mTvLeft.setText(leftText);
        mTvRight.setText(rightText);
        mTvLeft.setBackgroundResource(bgResId);
        mTvRight.setBackgroundResource(bgResId);

    }

    public void setNightModeTextColor() {
        mTvLeft.setTextColor(0xFF959595);
        mTvRight.setTextColor(0xFF959595);
    }

    OnClickListener listener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.tv_left) {
                mBlockButtonListener.onClickLeftButton();

            } else if (view.getId() == R.id.tv_right) {
                mBlockButtonListener.onClickRightButton();
            }

        }

    };

    public interface BlockButtonClickListener {
        void onClickLeftButton();

        void onClickRightButton();
    }

}
