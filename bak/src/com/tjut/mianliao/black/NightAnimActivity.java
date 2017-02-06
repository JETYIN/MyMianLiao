package com.tjut.mianliao.black;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.cocos2dx.CocosAvatarView;
import com.tjut.mianliao.cocos2dx.CocosAvatarView.OnAvatarLoadedListener;
import com.tjut.mianliao.cocos2dx.CocosAvatarView.OnNightAnimFinishListener;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.im.IMResourceManager;

public class NightAnimActivity extends Activity implements OnAvatarLoadedListener, OnNightAnimFinishListener {

    private CocosAvatarView mCocosAvatarView;

    private IMResourceManager mIMResManager;

    private UserInfoManager mUserInfoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_night_anim);
        IMResourceManager.getInstance(this);
        mCocosAvatarView = (CocosAvatarView) findViewById(R.id.cav_avatar);

        CocosAvatarView.setOnAvatarLoadedListener(this);
        CocosAvatarView.setOnNightAnimFinishListener(this);
    }

    private void setAppCache() {
        mUserInfoManager = UserInfoManager.getInstance(this);
        // save your-self info
        UserInfo mUserInfo = mUserInfoManager.getUserInfo(AccountInfo.getInstance(this).getUserId());
        UserEntryManager.getInstance(this).clear();
        mUserInfoManager.clear();
        UserRemarkManager.getInstance(this).clear();
        mUserInfoManager.addUserInfo(mUserInfo);
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (CocosAvatarView.isAvatarLoaded()) {
            mCocosAvatarView.onResume(this);
            mCocosAvatarView.ShowDayNightAnim();
        }
        // TODO Auto-generated method stub

    }

    @Override
    public void onNightAnimFinish() {
        // TODO Auto-generated method stub
        setAppCache();
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
    }

    @Override
    public void onAvatarLoaded() {

        mCocosAvatarView.onResume(this);
        mCocosAvatarView.ShowDayNightAnim();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return true;
    }

    // ----------------------------------------------change day anim action

    private static boolean s_IsNightAnimActive = false, s_IsDayAnimActive = false;

    public static void setAnimFlag(boolean isNight) {
        s_IsNightAnimActive = isNight;
        s_IsDayAnimActive = !isNight;
    }

    public static void playChangeDayAnim(Context context) {
        if (s_IsNightAnimActive) {
            Intent intent = new Intent(context, NightAnimActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (s_IsDayAnimActive) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        s_IsNightAnimActive = false;
        s_IsDayAnimActive = false;

    }

}
