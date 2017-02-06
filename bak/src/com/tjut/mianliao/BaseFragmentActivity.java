package com.tjut.mianliao;

import android.support.v4.app.FragmentActivity;

import com.tjut.mianliao.black.NightAnimActivity;

public class BaseFragmentActivity extends FragmentActivity {

    @Override
    protected void onResume() {
        super.onResume();
        NightAnimActivity.playChangeDayAnim(this);
    }

}
