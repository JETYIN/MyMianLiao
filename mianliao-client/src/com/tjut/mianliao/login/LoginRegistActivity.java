package com.tjut.mianliao.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.login.UserExtLoginManager.UserExtLoginListener;
import com.tjut.mianliao.register.AccountBasicActivity;
import com.tjut.mianliao.register.ChooseSchoolActivity;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;
import com.umeng.socialize.bean.SHARE_MEDIA;

public class LoginRegistActivity extends BaseActivity implements OnClickListener,
        UserExtLoginListener {
    private ImageView mButtonLogin, mButtonRegister;
    private UserExtLoginManager mUserExtLoginManager;
    private SnsHelper mSnsHelper;
    private LinearLayout mShowProgress;
    private SharedPreferences mPreferences;

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_loginorregister;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtonLogin = (ImageView) findViewById(R.id.imagebutton_login);
        mButtonRegister = (ImageView) findViewById(R.id.imagebutton_register);
        mShowProgress = (LinearLayout) findViewById(R.id.ll_loading_progress);
        mUserExtLoginManager = UserExtLoginManager.getInstance(this);
        mUserExtLoginManager.registerUserExtLoginListener(this);
        mSnsHelper = SnsHelper.getInstance();
        mPreferences = DataHelper.getSpForData(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.imagebutton_login:
                intent.setClass(LoginRegistActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.imagebutton_register:
                intent.setClass(LoginRegistActivity.this, AccountBasicActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_qq_login:
                mSnsHelper.loginByExt(LoginRegistActivity.this, SHARE_MEDIA.QQ);
                mUserExtLoginManager.setIsCancleLoginByExt(false);
                mButtonLogin.setEnabled(false);
                mButtonRegister.setEnabled(false);
                break;
            case R.id.iv_sina_login:
                mSnsHelper.loginByExt(LoginRegistActivity.this, SHARE_MEDIA.SINA);
                mUserExtLoginManager.setIsCancleLoginByExt(false);
                mButtonLogin.setEnabled(false);
                mButtonRegister.setEnabled(false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onExtLoginSuccess(int type) {
        saveLoginType(true);
        mUserExtLoginManager.setIsTaskRunning(false);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onExtLoginFailed(int type) {
        Utils.hidePgressDialog();
        mUserExtLoginManager.setIsTaskRunning(false);
        Intent intent = new Intent(this, ChooseSchoolActivity.class);
        intent.putExtra(LoginActivity.IS_EXT_LOGIN, true);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mUserExtLoginManager.isCancleLoginByExt()) {
            mShowProgress.setVisibility(View.VISIBLE);
            mButtonRegister.setEnabled(false);
            mButtonLogin.setEnabled(false);
        } else {
            mShowProgress.setVisibility(View.GONE);
            mButtonLogin.setEnabled(true);
            mButtonRegister.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserExtLoginManager.unregisterUserExtLoginListener(this);
    }

    @Override
    public void onBackPressed() {
        // Do not quit directly to improve user experience.
        moveTaskToBack(true);
    }

    private void saveLoginType(boolean loginByExt) {
        Editor editor = mPreferences.edit();
        editor.putBoolean(LoginActivity.SP_LOGIN_STYLE, loginByExt);
        editor.commit();
    }

}
