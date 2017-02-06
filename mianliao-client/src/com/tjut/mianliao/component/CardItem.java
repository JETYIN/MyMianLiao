package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class CardItem extends LinearLayout {

    private static final int[] VISIBILITY_FLAGS = {VISIBLE, INVISIBLE, GONE};

    private TextView mTvTitle;
    private TextView mTvContent;
    private ImageView mIvButton;

    public CardItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.card_item, this, true);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        mIvButton = (ImageView) findViewById(R.id.iv_button);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardItem);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.CardItem_cardTitle:
                    setTitle(a.getText(attr));
                    break;

                case R.styleable.CardItem_cardContent:
                    setContent(a.getText(attr));
                    break;

                case R.styleable.CardItem_cardContentIsSelectable:
                    setContentIsSelectable(a.getBoolean(attr, false));
                    break;

                case R.styleable.CardItem_cardHint:
                    setHint(a.getText(attr));
                    break;

                case R.styleable.CardItem_cardSingleLine:
                    setSingleLine(a.getBoolean(attr, false));
                    break;

                case R.styleable.CardItem_cardBtnSrc:
                    setButtonSrc(a.getDrawable(attr));
                    break;

                case R.styleable.CardItem_cardBtnBackground:
                    setButtonBackground(a.getDrawable(attr));
                    break;

                case R.styleable.CardItem_cardBtnVisibility:
                    int visibility = a.getInt(attr, 0);
                    if (visibility != 0) {
                        setButtonVisibility(VISIBILITY_FLAGS[visibility]);
                    }
                    break;

                default:
                    break;
            }
        }
        a.recycle();
    }

    public void setTitle(int resId) {
        mTvTitle.setText(resId);
    }

    public void setTitle(CharSequence text) {
        mTvTitle.setText(text);
    }

    public void setContent(int resId) {
        mTvContent.setText(resId);
    }

    public void setContent(CharSequence text) {
        mTvContent.setText(text);
    }

    public void setContentIsSelectable(boolean selectable) {
        mTvContent.setTextIsSelectable(selectable);
    }

    public void setHint(int resId) {
        mTvContent.setHint(resId);
    }

    public void setHint(CharSequence text) {
        mTvContent.setHint(text);
    }

    public void setSingleLine(boolean singleLine) {
        mTvContent.setSingleLine(singleLine);
    }

    public void setButtonSrc(int resId) {
        mIvButton.setImageResource(resId);
    }

    public void setButtonSrc(Drawable drawable) {
        mIvButton.setImageDrawable(drawable);
    }

    public void setButtonBackground(int resId) {
        mIvButton.setBackgroundResource(resId);
    }

    @SuppressWarnings("deprecation")
    public void setButtonBackground(Drawable background) {
        mIvButton.setBackgroundDrawable(background);
    }

    public void setButtonVisibility(int visibility) {
        mIvButton.setVisibility(visibility);
    }

    public void setButtonListener(OnClickListener l) {
        mIvButton.setOnClickListener(l);
    }

    public ImageView getButton() {
        return mIvButton;
    }
}
