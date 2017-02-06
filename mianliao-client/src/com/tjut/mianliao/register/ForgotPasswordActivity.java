package com.tjut.mianliao.register;

import java.util.regex.Pattern;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class ForgotPasswordActivity extends BaseActivity {

	private static final Pattern USER_NAME_PATTERN = Pattern
			.compile("^\\w{3,31}$");

	private Button mBtnSubmit;

	private GetPasswordTask mLastTask;
	private String mUserName;
	private String mEmail;

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_forgot_password;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getTitleBar().showTitleText(R.string.fpwd_title, null);
		getTitleBar().setBackgroundColor(getResources().getColor(R.color.white));
		mBtnSubmit = (Button) findViewById(R.id.btn_submit);
	}

	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_submit:
			mUserName = ((EditText) findViewById(R.id.et_login_name)).getText()
					.toString();
			mEmail = ((EditText) findViewById(R.id.et_email)).getText()
					.toString();

			if (mLastTask == null && isLoginNameValid() && isEmailValid()) {
				new GetPasswordTask().executeLong();
				Intent intent = new Intent(ForgotPasswordActivity.this,
						EmailActivity.class);
				intent.putExtra("mEmail", mEmail);
				intent.putExtra("mUserName", mUserName);
				startActivity(intent);
			}
			break;
		case R.id.tv_contact_us:
			Utils.actionSendTo(this, getString(R.string.service_email));
			break;
		default:
			break;
		}
	}

	private boolean isLoginNameValid() {
		mUserName = ((EditText) findViewById(R.id.et_login_name)).getText()
				.toString();
		if (USER_NAME_PATTERN.matcher(mUserName).matches()) {
			return true;
		} else {
			showResult(R.string.fpwd_login_name_invalid);
			return false;
		}
	}

	private boolean isEmailValid() {
		mEmail = ((EditText) findViewById(R.id.et_email)).getText().toString();
		if (Utils.EMAIL_PATTERN.matcher(mEmail).matches()) {
			return true;
		} else {
			showResult(R.string.reg_email_invalid);
			return false;
		}
	}

	private void showResult(int resId) {
		showResult(getString(resId));
	}

	private void showResult(String info) {
		((TextView) findViewById(R.id.tv_info)).setText(info);
	}

	private class GetPasswordTask extends AdvAsyncTask<Void, Void, MsResponse> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			getTitleBar().showProgress();
			mBtnSubmit.setEnabled(false);
			showResult("");
			mLastTask = this;
		}

		@Override
		protected MsResponse doInBackground(Void... params) {
			return HttpUtil.msRequest(getApplicationContext(),
					MsRequest.RESET_PASSWORD,
					"username=" + Utils.urlEncode(mUserName) + "&email="
							+ Utils.urlEncode(mEmail));
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			super.onPostExecute(response);
			getTitleBar().hideProgress();
			mBtnSubmit.setEnabled(true);
			mLastTask = null;
			if (MsResponse.isSuccessful(response)
					|| response.code == MsResponse.HTTP_TIMEOUT) {
				showResult(getString(R.string.fpwd_reset_success));
			} else {
				showResult(MsResponse.getFailureDesc(getApplicationContext(),
						R.string.fpwd_reset_failed, response.code));
			}
		}
	}
}