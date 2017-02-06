package com.tjut.mianliao.profile;

import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class UpdatePasswordActivity extends BaseActivity implements OnClickListener{

    private TextView mTvInfo;
    private EditText mEtOldPassword;
    private EditText mEtNewPassword;
    private boolean mIsOldPwdDisplay, mIsNewPwdDisplay;
    private ImageView mIvOldEye, mIvNewEye;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_update_password;
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.prof_update_password, null);

        mTvInfo = (TextView) findViewById(R.id.tv_info);
        mEtOldPassword = (EditText) findViewById(R.id.et_old_password);
        mEtNewPassword = (EditText) findViewById(R.id.et_new_password);
        mIvOldEye = (ImageView) findViewById(R.id.iv_view_old_pwd);
        mIvNewEye = (ImageView) findViewById(R.id.iv_view_new_pwd);
    }

    private void updatePassword() {
        mTvInfo.setVisibility(View.GONE);
        String oldPwd = mEtOldPassword.getText().toString();
        if (oldPwd.length() < 4) {
            showMessage(R.string.lgi_password_too_short);
            return;
        }

        String newPwd = mEtNewPassword.getText().toString();
        if (newPwd.length() < 6) {
            showMessage(R.string.prof_new_password_too_short);
            return;
        }

        new UpdatePwdTask(oldPwd, newPwd).executeLong();
    }

    private void showMessage(int msgId) {
        if (mTvInfo.getVisibility() != View.VISIBLE) {
            mTvInfo.setVisibility(View.VISIBLE);
        }
        mTvInfo.setText(msgId);
    }

    private class UpdatePwdTask extends MsTask {
        private String mOldPwd;
        private String mNewPwd;

        public UpdatePwdTask(String oldPwd, String newPwd) {
            super(getApplicationContext(), MsRequest.UPDATE_PASSWORD);
            mOldPwd = oldPwd;
            mNewPwd = newPwd;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("old_pwd=").append(mOldPwd)
                    .append("&new_pwd=").append(mNewPwd)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            getTitleBar().setRightButtonEnabled(false);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            getTitleBar().setRightButtonEnabled(true);
            if (response.isSuccessful()) {
//                response.showInfo(getRefContext(), R.string.prof_update_password_success);
                toast(getString(R.string.prof_update_password_success));
                finish();
            } else {
//                response.showFailInfo(getRefContext(), R.string.prof_update_password_failed);
                if (response.code == response.MS_USER_WRONG_PASSWORD) {
                    toast(UpdatePasswordActivity.this.getString(R.string.prof_failed_change_password));
                } else {
                    response.showInfo(getRefContext(), response.getFailureDesc(getRefContext(), R.string.prof_update_password_failed, response.code));
                } 

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_finish:
                updatePassword();
                break;

            case R.id.iv_view_old_pwd:
                if (mIsOldPwdDisplay) {
                    mEtOldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mIvOldEye.setImageResource(R.drawable.bottom_eyes_close);
                } else {
                    mEtOldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mIvOldEye.setImageResource(R.drawable.botton_bg_preview);
                }
                mIsOldPwdDisplay = !mIsOldPwdDisplay;
                break;

            case R.id.iv_view_new_pwd:
                if (mIsNewPwdDisplay) {
                    mEtNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mIvNewEye.setImageResource(R.drawable.bottom_eyes_close);
                } else {
                    mEtNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mIvNewEye.setImageResource(R.drawable.botton_bg_preview);
                }
                mIsNewPwdDisplay = !mIsNewPwdDisplay;
                break;
            default:
                break;
        }
    }
}
