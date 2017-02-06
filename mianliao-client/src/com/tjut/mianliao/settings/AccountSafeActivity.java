package com.tjut.mianliao.settings;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.UserExtInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.login.LoginActivity;
import com.tjut.mianliao.login.UserExtLoginManager;
import com.tjut.mianliao.login.UserExtLoginManager.UserBindListListener;
import com.tjut.mianliao.login.UserExtLoginManager.UserExtBindListener;
import com.tjut.mianliao.profile.UpdatePasswordActivity;
import com.tjut.mianliao.util.SnsHelper;
import com.umeng.socialize.bean.SHARE_MEDIA;

public class AccountSafeActivity extends BaseActivity implements OnClickListener,
        UserExtBindListener, UserBindListListener, DialogInterface.OnClickListener {

    private UserExtLoginManager mExtLoginManager;
    private SharedPreferences mPreferences;
    private AccountInfo mAccountInfo;
    private SnsHelper mSnsHelper;

    private UserInfo mUserInfo;
    private TextView mTvAccount;
    private TextView mTvQQAccount, mTvQQStatus;
    private TextView mTvSinaAccount, mTvSinaStatus;
    private TextView mTvRrAccount, mTvRrStatus;

    private ImageView mIvStart1, mIvStart2, mIvStart3, mIvStart4, mIvStart5;

    private List<UserExtInfo> mUserExtInfos;
    private LightDialog mUnbindDialog, mCannotBindDialog;

    private int mColorBinded, mColorUnbind;
    private int mBindCount;
    private int mUnbindType;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_account_safe;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.setting_account_and_safe);
        mColorBinded = 0XFF939292;
        mColorUnbind = 0XFF56C2C6;
        mExtLoginManager = UserExtLoginManager.getInstance(this);
        mAccountInfo = AccountInfo.getInstance(this);
        mExtLoginManager.registerUserBindListListener(this);
        mExtLoginManager.registerUserExtBindListener(this);
        mPreferences = DataHelper.getSpForData(this);
        mSnsHelper = SnsHelper.getInstance();
        mExtLoginManager.setIsLoginByExt(false);
        mUserInfo = mAccountInfo.getUserInfo();
        mTvAccount = (TextView) findViewById(R.id.tv_account);
        mTvAccount.setText(mUserInfo.account);
        initView();
        initExtBindinfo();
        mExtLoginManager.getBindList();
        showStartLevel(3);

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mExtLoginManager.isCancleLoginByExt()) {
            getTitleBar().hideProgress();
        }
    }

    @Override
    protected void onDestroy() {
        mExtLoginManager.unregisterUserBindListListener(this);
        mExtLoginManager.unregisterUserExtBindListener(this);
        super.onDestroy();
    }

    private void initView() {
        mTvQQAccount = (TextView) findViewById(R.id.tv_account_qq);
        mTvSinaAccount = (TextView) findViewById(R.id.tv_account_sina);
        mTvRrAccount = (TextView) findViewById(R.id.tv_account_rr);
        mTvQQStatus = (TextView) findViewById(R.id.tv_qq_status);
        mTvSinaStatus = (TextView) findViewById(R.id.tv_sina_status);
        mTvRrStatus = (TextView) findViewById(R.id.tv_rr_status);
        mIvStart1 = (ImageView) findViewById(R.id.iv_start_1);
        mIvStart2 = (ImageView) findViewById(R.id.iv_start_2);
        mIvStart3 = (ImageView) findViewById(R.id.iv_start_3);
        mIvStart4 = (ImageView) findViewById(R.id.iv_start_4);
        mIvStart5 = (ImageView) findViewById(R.id.iv_start_5);
    }

    private void initExtBindinfo() {
        mTvQQAccount.setText("");
        mTvSinaAccount.setText("");
        mTvRrAccount.setText("");
        mTvQQStatus.setText(R.string.setting_exth_bind);
        mTvQQStatus.setTextColor(mColorUnbind);
        mTvSinaStatus.setText(R.string.setting_exth_bind);
        mTvSinaStatus.setTextColor(mColorUnbind);
        mTvRrStatus.setText(R.string.setting_exth_bind);
        mTvRrStatus.setTextColor(mColorUnbind);
    }

    private void showUnbindDialog(String content) {
        if (mUnbindDialog == null) {
            mUnbindDialog = new LightDialog(this)
                .setTitleLd(R.string.course_time_clear)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.setting_unbind, this);
        }
        mUnbindDialog.setMessage(content);
        mUnbindDialog.show();
    }

    private void showCannotDialog() {
        if (mCannotBindDialog == null) {
            mCannotBindDialog = new LightDialog(this)
            .setMessage(R.string.setting_cannot_bind_msg)
            .setTitleLd(R.string.course_time_clear)
            .setNegativeButton(R.string.setting_cannot_bind_ok, null);
        }
        mCannotBindDialog.show();
    }

    private void showStartLevel(int level) {
        mIvStart1.setImageResource(1 <= level ?
                R.drawable.pic_safe_star_yellow :R.drawable.pic_safe_star_gray);
        mIvStart2.setImageResource(2 <= level ?
                R.drawable.pic_safe_star_yellow : R.drawable.pic_safe_star_gray);
        mIvStart3.setImageResource(3 <= level ?
                R.drawable.pic_safe_star_yellow : R.drawable.pic_safe_star_gray);
        mIvStart4.setImageResource(4 <= level ?
                R.drawable.pic_safe_star_yellow : R.drawable.pic_safe_star_gray);
        mIvStart5.setImageResource(5 <= level ?
                R.drawable.pic_safe_star_yellow : R.drawable.pic_safe_star_gray);
    }

    private void updateBindInfo(UserExtInfo info, boolean isBind) {
        switch (info.type) {
            case UserExtInfo.TYPE_QQ:
                if (isBind) {
                    mTvQQAccount.setText(info.nickName);
                    mTvQQStatus.setText(R.string.setting_exth_unbind);
                    mTvQQStatus.setTextColor(mColorBinded);
                    findViewById(R.id.rl_bind_qq).setTag(info);
                } else {
                    mTvQQAccount.setText("");
                    mTvQQStatus.setText(R.string.setting_exth_bind);
                    mTvQQStatus.setTextColor(mColorUnbind);
                    findViewById(R.id.rl_bind_qq).setTag(null);
                }
                break;
            case UserExtInfo.TYPE_SINA:
                if (isBind) {
                    mTvSinaAccount.setText(info.nickName);
                    mTvSinaStatus.setText(R.string.setting_exth_unbind);
                    mTvSinaStatus.setTextColor(mColorBinded);
                    findViewById(R.id.rl_bind_sina).setTag(info);
                } else {
                    mTvSinaAccount.setText("");
                    mTvSinaStatus.setText(R.string.setting_exth_bind);
                    mTvSinaStatus.setTextColor(mColorUnbind);
                    findViewById(R.id.rl_bind_sina).setTag(null);
                }
                break;
            case UserExtInfo.TYPE_RENREN:
                if (isBind) {
                    mTvRrAccount.setText(info.nickName);
                    mTvRrStatus.setText(R.string.setting_exth_unbind);
                    mTvRrStatus.setTextColor(mColorBinded);
                    findViewById(R.id.rl_bind_rr).setTag(info);
                } else {
                    mTvRrAccount.setText("");
                    mTvRrStatus.setText(R.string.setting_exth_bind);
                    mTvRrStatus.setTextColor(mColorUnbind);
                    findViewById(R.id.rl_bind_rr).setTag(null);
                }
                break;
            default:
                break;
        }
    }

    private void updateBindInfo() {
        if (mUserExtInfos != null && mUserExtInfos.size() > 0) {
            for (UserExtInfo info : mUserExtInfos) {
                updateBindInfo(info, true);
            }
        } else {
            initExtBindinfo();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_pwd:
                startActivity(new Intent(this, UpdatePasswordActivity.class));
                break;
            case R.id.rl_bind_qq:
                mUnbindType = UserExtInfo.TYPE_QQ;
                Object objQQ = v.getTag();
                if (objQQ == null) {
                    startBind();
                    getTitleBar().showProgress();
                    return;
                }
                UserExtInfo extInfo = (UserExtInfo) objQQ;
                mExtLoginManager.setUserExtInfo(extInfo);
                if (canUnbind()) {
                    showUnbindDialog(getContentByType());
                } else {
                    showCannotDialog();
                }
                break;
            case R.id.rl_bind_rr:
                mUnbindType = UserExtInfo.TYPE_RENREN;
                Object objRr = v.getTag();
                if (objRr == null) {
                    startBind();
                    getTitleBar().showProgress();
                    return;
                }
                UserExtInfo extInfoR = (UserExtInfo) objRr;
                mExtLoginManager.setUserExtInfo(extInfoR);
                if (canUnbind()) {
                    showUnbindDialog(getContentByType());
                } else {
                    showCannotDialog();
                }
                break;
            case R.id.rl_bind_sina:
                mUnbindType = UserExtInfo.TYPE_SINA;
                Object objSina = v.getTag();
                if (objSina == null) {
                    startBind();
                    getTitleBar().showProgress();
                    return;
                }
                UserExtInfo extInfoSina = (UserExtInfo) objSina;
                mExtLoginManager.setUserExtInfo(extInfoSina);
                if (canUnbind()) {
                    showUnbindDialog(getContentByType());
                } else {
                    showCannotDialog();
                }
                break;

            default:
                break;
        }
    }

    private void startBind() {
        switch (mUnbindType) {
            case UserExtInfo.TYPE_QQ:
                mSnsHelper.loginByExt(this, SHARE_MEDIA.QQ);
                break;
            case UserExtInfo.TYPE_RENREN:
                mSnsHelper.loginByExt(this, SHARE_MEDIA.RENREN);
                break;
            case UserExtInfo.TYPE_SINA:
                mSnsHelper.loginByExt(this, SHARE_MEDIA.SINA);
                break;
            default:
                break;
        }
    }

    private String getContentByType() {
        switch (mUnbindType) {
            case UserExtInfo.TYPE_QQ:
                return getString(R.string.setting_unbind_msg, getString(R.string.setting_exth_qq));
            case UserExtInfo.TYPE_RENREN:
                return getString(R.string.setting_unbind_msg, getString(R.string.setting_exth_rr));
            case UserExtInfo.TYPE_SINA:
                return getString(R.string.setting_unbind_msg, getString(R.string.setting_exth_sina));
            default:
                return "";
        }
    }

    private boolean canUnbind() {
        boolean loginByExt = mPreferences.getBoolean(LoginActivity.SP_LOGIN_STYLE, false);
        if (loginByExt && mBindCount <= 1) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onGetBindListSuccess(List<UserExtInfo> userExtInfos) {
        mUserExtInfos = userExtInfos;
        mBindCount = mUserExtInfos.size();
        updateBindInfo();
    }

    @Override
    public void onGetBindListFailed() {}

    @Override
    public void onBindSuccess(int type) {
        UserExtInfo extInfo = mExtLoginManager.getUserExtInfo();
        mBindCount++;
        updateBindInfo(extInfo, true);
        getTitleBar().hideProgress();
    }

    @Override
    public void onUnbindSuccess(int type) {
        UserExtInfo extInfo = new UserExtInfo();
        extInfo.type = type;
        mBindCount--;
        updateBindInfo(extInfo, false);
        getTitleBar().hideProgress();
    }

    @Override
    public void onBindFailed(int code) {
        getTitleBar().hideProgress();
        if (code == 213) {
            toast(R.string.setting_binded_msg);
        }
    }

    @Override
    public void onUnbindFailed() {
        getTitleBar().hideProgress();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            mExtLoginManager.userExtUnbind();
            getTitleBar().showProgress();
        }
    }

}
