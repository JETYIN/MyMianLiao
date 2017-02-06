package com.tjut.mianliao.forum.nova;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.profile.IdVerifyActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class CreateChannelActivity extends BaseActivity implements OnClickListener {

    private static final int REQUEST_CODE = 100;


    private TextView mTvCreate, mTvCanCreate, mTvIdOk, mTvGoIdverify;
    private boolean mIsIdOk, mCanCreateToday;
    private LightDialog mDialog;
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_create_channel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showProgress();
        getTitleBar().setTitle("创建频道");
        mTvCreate = (TextView) findViewById(R.id.tv_create_start);
        mTvCanCreate = (TextView) findViewById(R.id.tv_can_create);
        mTvIdOk = (TextView) findViewById(R.id.tv_is_id);
        mTvGoIdverify = (TextView) findViewById(R.id.tv_go_idverfy);
        mTvCreate.setOnClickListener(this);
        mTvGoIdverify.setOnClickListener(this);
        mIsIdOk = UserInfoManager.getInstance(this).getUserInfo(
                AccountInfo.getInstance(this).getUserId()).isVerified();
        new CreateChannelCheckTask().executeLong();
        mIsIdOk = UserInfoManager.getInstance(this).getUserInfo(
                AccountInfo.getInstance(this).getUserId()).isVerified();
        mTvCanCreate.setCompoundDrawablesWithIntrinsicBounds(
                mIsIdOk ? R.drawable.redcheckbox : R.drawable.redcheckbox_warning, 0, 0, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_create_start:
                if (isReady()) {
                    Intent intent = new Intent(this, ChoseChannelAttrActivity.class);
                    startActivityForResult(intent, REQUEST_CODE);
                }
                break;
            case R.id.tv_go_idverfy:
            	if (!mIsIdOk) {
            		Intent intent = new Intent(CreateChannelActivity.this, IdVerifyActivity.class);
            		startActivity(intent);
            	}
            default:
                break;
        }
    }

    private boolean isReady() {
        if (!mIsIdOk) {
        	showMssageDialog(1);
            return false;
        } else if (!mCanCreateToday) {
        	showMssageDialog(2);
            return false;
        } else {
        	return true;
        }
    }

    private class CreateChannelCheckTask extends MsTask {

        public CreateChannelCheckTask() {
            super(CreateChannelActivity.this, MsRequest.CHECK_CREATE);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                mTvCanCreate.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.redcheckbox, 0, 0, 0);
                mCanCreateToday = true;
                if (mIsIdOk) {
                	mTvIdOk.setText(R.string.channel_create_get_userinfo_success);
                	mTvIdOk.setCompoundDrawablesWithIntrinsicBounds(R.drawable.redcheckbox, 0, 0, 0);
                	mTvGoIdverify.setVisibility(View.INVISIBLE);
                	if (mCanCreateToday) {
                		mTvCreate.setEnabled(true);
                		mTvCreate.setText(R.string.channel_create);
                	}
                } else {
                	mTvIdOk.setText(R.string.channel_create_get_userinfo_fail);
                	mTvIdOk.setCompoundDrawablesWithIntrinsicBounds(R.drawable.redcheckbox_warning, 0, 0, 0);
                	mTvGoIdverify.setVisibility(View.VISIBLE);
                }
               
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_DELETED) {
            this.finish();
        }
    }
    private void showMssageDialog(int type) {
		if (mDialog == null) {
			switch (type) {
			case 1:
				mDialog = new LightDialog(this).setTitleLd(
						R.string.cf_create_channel_fail).setMessage(
						R.string.cf_create_channel_fail_noid);
				break;
			case 2:
				mDialog = new LightDialog(this).setTitleLd(
						R.string.cf_create_channel_fail).setMessage(
						R.string.cf_create_channel_fail_toomore);
			default:
				break;
			}

		}
		mDialog.show();
    }

}
