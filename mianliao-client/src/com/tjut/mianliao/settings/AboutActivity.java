package com.tjut.mianliao.settings;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.update.UpdateManager;
import com.tjut.mianliao.update.UpdateManager.OnCheckFinishedListener;
import com.tjut.mianliao.util.Utils;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    private static final String URL_CHANGE_LOG = "http://52mianliao.com/wap/blog.php";
    private static final String URL_USER_AGREEMENT = "http://52mianliao.com/wap/agreement.php";

    private UpdateManager mUpdateManager;

    private TextView mTvInformation;
    private View mPbLoading;
    private View mBtnCheckUpdate;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.setting_about, null);

        PackageInfo pkgInfo = Utils.getPackageInfo(this);
        StringBuilder appInfo = new StringBuilder(getString(R.string.app_name));
        if (pkgInfo != null) {
            appInfo.append(" ").append(getString(R.string.version))
                    .append(" ").append(pkgInfo.versionName);
        }
        TextView tvAppInfo = (TextView) findViewById(R.id.tv_app_info);
        tvAppInfo.setText(appInfo);

        mTvInformation = (TextView) findViewById(R.id.tv_information);
        mPbLoading = findViewById(R.id.pb_loading);
        mBtnCheckUpdate = findViewById(R.id.ll_update);
    }

    @Override
    protected void onStart() {
        mCheckUpdateListener.isAlive = true;
        super.onStart();
    }

    @Override
    protected void onStop() {
        mCheckUpdateListener.isAlive = false;
        super.onStop();
    }

    public void checkUpdate() {
        if (!Utils.isNetworkAvailable(this)) {
            showMessage(R.string.no_network);
            return;
        }

        if (mUpdateManager == null) {
            mUpdateManager = new UpdateManager(this);
        }

        mTvInformation.setVisibility(View.INVISIBLE);
        mBtnCheckUpdate.setEnabled(false);
        mPbLoading.setVisibility(View.VISIBLE);
        mUpdateManager.checkForUpdate(true, mCheckUpdateListener);
    }

    private OnCheckFinishedListener mCheckUpdateListener = new OnCheckFinishedListener() {
        @Override
        public void onCheckFinished(boolean success, boolean hasUpdate) {
            if (!success) {
                showMessage(R.string.upd_failed_check_new_version);
            } else if (!hasUpdate) {
                showMessage(R.string.upd_no_new_version);
            }
            mBtnCheckUpdate.setEnabled(true);
            mPbLoading.setVisibility(View.INVISIBLE);
        }
    };

    private void showMessage(int msg) {
        mTvInformation.setVisibility(View.VISIBLE);
        mTvInformation.setText(msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_change_log:
                Intent icl = new Intent(this, BrowserActivity.class);
                icl.putExtra(BrowserActivity.URL, URL_CHANGE_LOG);
                icl.putExtra(BrowserActivity.TITLE, getString(R.string.change_log));
                startActivity(icl);
                break;
            case R.id.ll_agreement:
                Intent iua = new Intent(getApplicationContext(), BrowserActivity.class);
                iua.putExtra(BrowserActivity.URL, URL_USER_AGREEMENT);
                iua.putExtra(BrowserActivity.TITLE, getString(R.string.user_agreement));
                startActivity(iua);
                break;
            case R.id.ll_update:
                checkUpdate();
                break;
            case R.id.ll_invite:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String inviteFriends = getString(R.string.invite_friends);
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, inviteFriends);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        getString(R.string.invite_friends_desc) + getString(R.string.app_url));
                startActivity(Intent.createChooser(sharingIntent, inviteFriends));
                break;
            default:
                break;
        }
    }
}
