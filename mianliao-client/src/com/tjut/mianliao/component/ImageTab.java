package com.tjut.mianliao.component;

import android.widget.ImageView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.tab.Tab;

public class ImageTab extends Tab {

    private int mBgId = R.drawable.pic_bg_emo;
    private int mBgChosenId = R.drawable.pic_bg_emo_choose;
    
    private ImageView mButton;
    
    public ImageTab(ImageView button) {
        mButton = button;
        setButton(button);
    }

    @Override
    public void setChosen(boolean chosen) {
        mButton.setBackgroundResource(mBgId);
    }
    
    public void setBackgroundResource(int bgId, int bgChosenId) {
        mBgId = bgId;
        mBgChosenId = bgChosenId;
    }

}
