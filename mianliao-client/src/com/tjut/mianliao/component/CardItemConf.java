package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class CardItemConf extends CardItemBase {

    protected TextView mTvContent;

    public CardItemConf(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.ci_content_conf, this, true);
        mTvContent = (TextView) findViewById(R.id.tv_content);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CardItem);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            switch (attr) {
                case R.styleable.CardItem_cardContent:
                    setContent(ta.getText(attr));
                    break;

                case R.styleable.CardItem_cardHint:
                    setHint(ta.getText(attr));
                    break;

                case R.styleable.CardItem_cardSingleLine:
                    setSingleLine(ta.getBoolean(attr, false));
                    break;

                default:
                    break;
            }
        }
        ta.recycle();

        int padding = getResources().getDimensionPixelOffset(R.dimen.cif_padding_vertical);
        setPadding(getPaddingLeft(), padding, getPaddingRight(), padding);
    }

    @Override
    protected int getBackgroundResId() {
        return R.drawable.selector_bg_card_item;
    }

    public void setContent(int resId) {
        mTvContent.setText(resId);
    }

    public void showDrawImg(boolean show) {
        if(!show){
            mTvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    public void setContent(CharSequence text) {
        mTvContent.setText(text);
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
}
