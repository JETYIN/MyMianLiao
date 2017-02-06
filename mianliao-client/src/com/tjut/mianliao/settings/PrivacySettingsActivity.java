package com.tjut.mianliao.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.ContactsBlacklistActivity;
import com.tjut.mianliao.data.DataHelper;

public class PrivacySettingsActivity extends BaseActivity implements OnClickListener {

    private static final String TEMPORARY_CHAT_KEY = Settings.KEY_TEMPORARY_CHAT;

    private SharedPreferences mSharedPrefs;
    private ImageView mIvCheck;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_privacy_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.setting_privacy, null);
        mSharedPrefs = DataHelper.getSpForData(this);
        mIvCheck = (ImageView) findViewById(R.id.iv_check);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_blacklist:
                startActivity(new Intent(this, ContactsBlacklistActivity.class));
                break;
            case R.id.rl_temp_chat:
                setPersistedBoolean(TEMPORARY_CHAT_KEY);
                updateInfo();
                break;

            default:
                break;
        }
    }
/**可以在selec中处理 **/
    private void updateInfo() {
        mIvCheck.setImageResource(getPersistedBoolean(TEMPORARY_CHAT_KEY) ?
                R.drawable.switch_on : R.drawable.switch_off);
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
