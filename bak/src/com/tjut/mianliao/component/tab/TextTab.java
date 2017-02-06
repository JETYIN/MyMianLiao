package com.tjut.mianliao.component.tab;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.tjut.mianliao.R;

public class TextTab extends Tab {

    private int mBgId = R.drawable.selector_bg_card_item;
    private int mBgChosenId = R.drawable.bg_bottom_line_green;
    private int mTxtColor;
    private int mTxtColorChosen;
    private int mTxtViewId;
    private boolean mIsNight;

    public TextTab(TextView button) {
        this(button, 0);
    }

    public TextTab(View button, int txtViewId) {
        mTxtViewId = txtViewId;
        setButton(button);
        setBackgroundAndTextColor();
    }

    private void setBackgroundAndTextColor() {
        if (mIsNight) {
            setBackgroundResource(
                    R.drawable.selector_bg_card_item, R.drawable.bottom_bg_red_black);
            setTextColorResource(
                    R.color.txt_tab_item_grey, R.color.txt_tab_item_purple);
        } else {
            setBackgroundResource(
                    R.drawable.selector_bg_card_item, R.drawable.bg_bottom_line_green);
            setTextColorResource(
                    R.color.txt_tab_item_grey, R.color.txt_tab_item_forum);
        }
    }

    public void setNightMode(boolean isNightMode) {
        this.mIsNight = isNightMode;
        setBackgroundAndTextColor();
    }

    @Override
    public void setChosen(boolean chosen) {
        View v = getButton();
        if (mBgId > 0 || mBgChosenId > 0) {
            v.setBackgroundResource(chosen ? mBgChosenId : mBgId);
        }
        TextView tv;
        if (mTxtViewId == 0) {
            tv = (TextView) v;
        } else {
            tv = (TextView) v.findViewById(mTxtViewId);
        }
        tv.setTextColor(chosen ? mTxtColorChosen : mTxtColor);
    }

    public void setBackgroundResource(int bgId, int bgChosenId) {
        mBgId = bgId;
        mBgChosenId = bgChosenId;
    }

    public void setTextColorResource(int colorId, int colorChosenId) {
        Resources res = getButton().getResources();
        mTxtColor = res.getColor(colorId);
        mTxtColorChosen = res.getColor(colorChosenId);
    }
}
