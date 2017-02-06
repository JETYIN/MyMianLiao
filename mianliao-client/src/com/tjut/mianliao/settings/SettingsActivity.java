package com.tjut.mianliao.settings;

import java.io.File;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.hyphenate.chat.EMClient;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.LoginStateHelper;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CheckBoxSetting;
import com.tjut.mianliao.component.DialogSetting;
import com.tjut.mianliao.component.IntentSetting;
import com.tjut.mianliao.component.IntentSetting.HighPriorityClickListener;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.feedback.FeedbackActivity;
import com.tjut.mianliao.login.LoginActivity;
import com.tjut.mianliao.login.UserExtLoginManager;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.Utils;

public class SettingsActivity extends BaseActivity implements DialogSetting.Listener, OnClickListener {

    private static final String URL_HELP = "assets/pages/help_menu.html";
    public static final int RESULT_RELOGIN = 100;

    private LightDialog mLogoutDialog;
    private UserExtLoginManager mUserExtLoginManager;
    private String mCacheFolder;
    private SharedPreferences mPreferences;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = DataHelper.getSpForData(this);

        getTitleBar().showTitleText(R.string.setting_title, null);
        mUserExtLoginManager = UserExtLoginManager.getInstance(this);

        CheckBoxSetting cbs = (CheckBoxSetting) findViewById(R.id.setting_new_message_sound);
        cbs.setTitle(R.string.setting_new_message_sound);
        cbs.setKey(Settings.KEY_NEW_MESSAGE_SOUND);

        cbs = (CheckBoxSetting) findViewById(R.id.setting_new_message_vibrate);
        cbs.setTitle(R.string.setting_new_message_vibrate);
        cbs.setKey(Settings.KEY_NEW_MESSAGE_VIBRATE);

        cbs = (CheckBoxSetting) findViewById(R.id.setting_night_mode);
        cbs.setTitle(R.string.setting_night_mode);
        cbs.setKey(Settings.KEY_NIGHT_MODE);

        cbs = (CheckBoxSetting) findViewById(R.id.setting_download_pictures_with_wifi);
        cbs.setTitle(R.string.setting_only_download_pictures_with_wifi);
        cbs.setKey(Settings.KEY_ONLY_DOWNLOAD_PICTURES_WITH_WIFI);

        cbs = (CheckBoxSetting) findViewById(R.id.setting_enter_send_message);
        cbs.setTitle(R.string.setting_enter_send_message);
        cbs.setKey(Settings.KEY_ENTER_SEND_MESSAGE);

        DialogSetting ds = (DialogSetting) findViewById(R.id.setting_default_tab);
        ds.setListener(this);
        ds.setTitle(R.string.setting_default_tab);
        ds.setKey(Settings.KEY_DEFAULT_TAB);
        setDefaultTabSummary(ds, mSettings.getDefaultTabIndex());

        IntentSetting is = (IntentSetting) findViewById(R.id.setting_privacy);
        is.setTitle(R.string.setting_privacy);
        is.setClass(PrivacySettingsActivity.class);

        is = (IntentSetting) findViewById(R.id.setting_help);
        is.setTitle(R.string.help);
        is.setClass(BrowserActivity.class);
        is.getIntent().putExtra(BrowserActivity.URL, Utils.getServerAddress() + URL_HELP);
        is.getIntent().putExtra(BrowserActivity.TITLE, getString(R.string.help));

        is = (IntentSetting) findViewById(R.id.setting_about);
        is.setTitle(R.string.setting_about);
        is.setClass(AboutActivity.class);

        is = (IntentSetting) findViewById(R.id.setting_feedback);
        is.setTitle(R.string.fbk_title);
        is.setClass(FeedbackActivity.class);

        TextView tvLevel = (TextView) findViewById(R.id.tv_safe_level);
        tvLevel.setText(getString(R.string.setting_safe_level));

        is = (IntentSetting) findViewById(R.id.setting_clear_cache);
        is.setTitle(R.string.setting_clear_cache);
        is.registerHighProiorityListener(new HighPriorityClickListener() {

            @Override
            public void onClick() {
                clearCache();
            }
        });

        is = (IntentSetting) findViewById(R.id.setting_notify);
        is.setTitle(R.string.setting_msg_notify);
        is.setClass(MsgNotifySetActivity.class);
    }

    @Override
    public void onCreateDialog(DialogSetting ds, LightDialog dialog) {
        switch (ds.getId()) {
            case R.id.setting_default_tab:
                dialog.setItems(R.array.available_tabs, ds);
                break;

            default:
                break;
        }
    }

    @Override
    public void onBindDialog(DialogSetting ds, LightDialog dialog) { }

    @Override
    public void onCloseDialog(DialogSetting ds, int which) {
        switch (ds.getId()) {
            case R.id.setting_default_tab:
                setDefaultTabSummary(ds, which);
                mSettings.setDefaultTabIndex(which);
                break;

            default:
                break;
        }
    }

    private void setDefaultTabSummary(DialogSetting ds, int index) {
        String[] tabs = getResources().getStringArray(R.array.available_tabs);
        if (index >= 0 && index < tabs.length) {
            ds.setSummary(tabs[index]);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_account_safe:
                startActivity(new Intent(SettingsActivity.this, AccountSafeActivity.class));
                break;
            case R.id.tv_logout:
                showLogoutDialog();
                break;
            default:
                break;
        }
    }

    private void clearCache() {
        mCacheFolder = getCacheDir().getAbsolutePath() + FileDownloader.IMAGE_CACHE;
        File folder = new File(mCacheFolder);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
        toast(this.getString(R.string.setting_clean_succuess));
    }

    private void showLogoutDialog() {
        if (mLogoutDialog == null) {
            mLogoutDialog = new LightDialog(this);
            mLogoutDialog.setTitle(R.string.prof_log_out);
            mLogoutDialog.setMessage(R.string.more_description_logout)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mUserExtLoginManager.setIsCancleLoginByExt(true);
                            LoginStateHelper.accountLogout(getApplicationContext());
                            setResult(RESULT_RELOGIN);
                            resetLoginType();
                            new LogoutThread().start();
                            finish();
                        }
                    });
        }
        mLogoutDialog.show();
    }

    private class LogoutThread extends Thread{
        @Override
        public void run() {
            super.run();
            EMClient.getInstance().logout(true);
        }
    }

    private void resetLoginType() {
        Editor editor = mPreferences.edit();
        editor.putBoolean(LoginActivity.SP_LOGIN_STYLE, false);
        editor.commit();
    }
}