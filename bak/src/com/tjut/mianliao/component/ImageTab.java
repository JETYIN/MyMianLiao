package com.tjut.mianliao.component;

import android.widget.ImageView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.tab.Tab;

public class ImageTab extends Tab {

    private int mBgId = R.drawable.pic_bg_emo;
    private int mBgIdBlack = R.drawable.pic_bg_emo_black;
    private int mBgChosenId = R.drawable.pic_bg_emo_choose;
    private int mBgChosenIdBlack = R.drawable.pic_bg_emo_choose_black;
    
    private ImageView mButton;
    private boolean mIsNightMode;
    
    public ImageTab(ImageView button) {
        mButton = button;
        setButton(button);
    }
    
    public void setNightMode(boolean nightMode) {
        mIsNightMode = nightMode;
    }
    
    @Override
    public void setChosen(boolean chosen) {
        mButton.setBackgroundResource(chosen ? mIsNightMode ? mBgChosenIdBlack : mBgChosenId
                : mIsNightMode ? mBgIdBlack : mBgId);
    }
    
    public void setBackgroundResource(int bgId, int bgChosenId) {
        mBgId = bgId;
        mBgChosenId = bgChosenId;
    }

}
