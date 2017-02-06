package com.tjut.mianliao.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class CardItemBase extends LinearLayout {

    protected TextView mTvTitle;

    public CardItemBase(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.ci_title, this, true);
        mTvTitle = (TextView) findViewById(R.id.tv_title);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CardItem);
        for (int i = 0; i < ta.getIndexCount(); i++) {
            int attr = ta.getIndex(i);
            if (attr == R.styleable.CardItem_cardTitle) {
                setTitle(ta.getText(attr));
            }
        }
        ta.recycle();

        setBackgroundResource(getBackgroundResId());

        int padding = getResources().getDimensionPixelOffset(R.dimen.ci_padding_horizontal);
        setPadding(padding, 0, padding, 0);
    }

    protected int getBackgroundResId() {
        return 0;
    }

    public void setTitle(int resId) {
        mTvTitle.setText(resId);
    }

    public void setTitle(CharSequence text) {
        mTvTitle.setText(text);
    }
    public void setTitleSize(int textSize){
//        mTvTitle.setTextSize(R.dimen.card_content_txt_size);
    }

}
