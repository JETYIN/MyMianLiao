package com.tjut.mianliao.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTaskListener;

public class RemarkActivity extends BaseActivity implements MsTaskListener {

    private EditText mEtRemark;
    private Button mBtnSubmit;
    private UserRemarkManager mURM;
    private UserInfo mUserInfo;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_remark;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserInfo = getIntent().getParcelableExtra(UserInfo.INTENT_EXTRA_INFO);
        if (mUserInfo == null || mUserInfo.userId == 0) {
            toast(R.string.prof_user_not_exist);
            finish();
            return;
        }

        getTitleBar().showTitleText(mUserInfo.getNickname(), null);

        mURM = UserRemarkManager.getInstance(this);
        mEtRemark = (EditText) findViewById(R.id.et_remark);
        mEtRemark.setText(mURM.getRemark(mUserInfo.userId, ""));
        mBtnSubmit = (Button) findViewById(R.id.btn_submit);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_submit) {
            String remark = mEtRemark.getText().toString();
            mURM.update(mUserInfo.userId, mUserInfo.account, remark, this);
        }
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        getTitleBar().showProgress();
        mBtnSubmit.setEnabled(false);
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        getTitleBar().hideProgress();
        mBtnSubmit.setEnabled(true);
        if (MsResponse.isSuccessful(response)) {
            toast(R.string.prof_remark_updated);
            setResult(RESULT_UPDATED);
            finish();
        } else {
            response.showFailInfo(this, R.string.prof_remark_update_failed);
        }
    }
}