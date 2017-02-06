package com.tjut.mianliao.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.DataHelper;

public class MsgNotifySetActivity extends BaseActivity implements OnClickListener {

    private static final String SOUND_KEY = Settings.KEY_NEW_MESSAGE_SOUND;
    private static final String VIBRATE_KEY = Settings.KEY_NEW_MESSAGE_VIBRATE;

    private SharedPreferences mSharedPrefs;

    private ImageView mIvMsgSound, mIvMsgVibrate;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_msg_notify;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.setting_msg_notify);
        mSharedPrefs = DataHelper.getSpForData(this);
        mIvMsgSound = (ImageView) findViewById(R.id.iv_msg_sound);
        mIvMsgVibrate = (ImageView) findViewById(R.id.iv_msg_vibrate);
        updateInfo();
    }

    private void updateInfo() {
        mIvMsgSound.setImageResource(getPersistedBoolean(SOUND_KEY) ?
                R.drawable.switch_on : R.drawable.switch_off);
        mIvMsgVibrate.setImageResource(getPersistedBoolean(VIBRATE_KEY) ?
                R.drawable.switch_on : R.drawable.switch_off);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_msg_sound:
                setPersistedBoolean(SOUND_KEY);
                updateInfo();
                break;
            case R.id.rl_msg_vibrate:
                setPersistedBoolean(VIBRATE_KEY);
                updateInfo();
                break;
            default:
                break;
        }
    }

    private void setPersistedBoolean(String key) {
        if (key != null) {
            mSharedPrefs.edit().putBoolean(key, !getPersistedBoolean(key)).commit();
        }
    }

    private boolean getPersistedBoolean(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

}
