package com.tjut.mianliao.login;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.CheckinHelper;
import com.tjut.mianliao.LoginStateHelper;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.UserState;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.register.ForgotPasswordActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    public static final String EXTRL_USER_EXT_INFO = "user_ext_info";
    public static final String SP_LOGIN_STYLE = "sp_login_style";
    public static final String IS_EXT_LOGIN = "is_login";

    private String mAccount;
    private String mPassword;

    private EditText mEtAccount;
    private EditText mEtPassword;
    private View mButtonLogin;

    private TextView mLoginHint, mTvForget;
    private LoginTask mLoginTask;

    private LightDialog mUpdateAppDialog;
    private SnsHelper mSnsHelper;
    private int mShowflag = 0;
    private ImageView mElephant, mImgCheck;

    private SharedPreferences mPreferences;
    private boolean mCanBack = true;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.lgi_login);
        mEtAccount = (EditText) findViewById(R.id.et_account);
        mEtAccount.setText(AccountInfo.getInstance(this).getAccount());
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mTvForget = (TextView) findViewById(R.id.tv_forget_password);
        mButtonLogin = findViewById(R.id.btn_login);
        mElephant = (ImageView) findViewById(R.id.iv_change_elephant);
        mLoginHint = (TextView) findViewById(R.id.tv_login_hint);
        mImgCheck = (ImageView) findViewById(R.id.iv_pass_check);

        mTvForget.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mEtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || (event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEtPassword.getWindowToken(), 0);
                    login();
                    return true;
                }
                return false;
            }
        });
        mEtPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mElephant.setBackgroundResource(R.drawable.pic_bg_keep_out);
                } else {
                    mElephant.setBackgroundResource(R.drawable.pic_bg_look);
                }
            }
        });

        mSnsHelper = SnsHelper.getInstance();
        mPreferences = DataHelper.getSpForData(this);
    }

    @Override
    protected void onDestroy() {
        if (mLoginTask != null) {
            mLoginTask.cancel(false);
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                break;
            case R.id.tv_forget_password:
                Intent iPwd = new Intent(this, ForgotPasswordActivity.class);
                startActivity(iPwd);
                break;
            case R.id.iv_pass_check:
                if (mEtPassword.getText() != null) {
                    if (mShowflag == 0) {
                        mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        mImgCheck.setImageResource(R.drawable.bottom_eyes_close);
                        mEtPassword.setSelection(mEtPassword.getText().length());
                        mShowflag = 1;
                    } else {
                        mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        mImgCheck.setImageResource(R.drawable.pic_bt_check);
                        mEtPassword.setSelection(mEtPassword.getText().length());
                        mShowflag = 0;
                    }
                }
                break;
            default:
                break;
        }
    }

    public void login() {
        if (mLoginTask != null) {
            return;
        }
        mAccount = mEtAccount.getText().toString();
        mPassword = mEtPassword.getText().toString();

        if (!Utils.isNetworkAvailable(this)) {
            showHint(R.string.lgi_network_unavailable);
        }

        if (validInput(mAccount, mPassword)) {
            new LoginTask().executeLong();
        }
    }

    private class LoginTask extends MsTask {

        public LoginTask() {
            super(getApplicationContext(), MsRequest.LOGIN);
        }

        @Override
        protected String buildParams() {
            PackageInfo pkgInfo = Utils.getPackageInfo(getRefContext());
            String version = pkgInfo == null ? "0" : pkgInfo.versionName;
            return new StringBuilder("account=").append(Utils.urlEncode(mAccount))
                    .append("&password=").append(Utils.urlEncode(mPassword))
                    .append("&version=").append(version)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            mLoginTask = this;
            mCanBack = false;
            Utils.showProgressDialog(LoginActivity.this, R.string.lgi_logging_in);
            updateInputs(false);
        }

        @Override
        protected void onPostExecute(MsResponse result) {
            updateInputs(true);
            mLoginTask = null;
            if (result.code == MsResponse.MS_SUCCESS) {
/**请求成功通知观察者**/
                UserState.getInstance().reset();
                try {
                    JSONObject json = new JSONObject(result.response);
                    if (json.getInt(UserInfo.USER_ID) == 0) {
                        throw new JSONException("");
                    }
                    mSettings.setContactPrivacy(json.optInt(Settings.CONTACT_PRIVACY));
                    Context context = LoginActivity.this;

                    if (json.optInt("checkin", 0) == 1) {
                        CheckinHelper.checkin(context);
                    }

                    UserInfo me = UserInfo.fromJson(json);
                    saveUserInfo(result.response);
                    /**获取token**/
                    String token = json.getString(AccountInfo.TOKEN);
                    /**登录**/
                    LoginStateHelper.accountLogin(context, mAccount, token, me);
                    EMClient.getInstance().login(me.account, token, new EMCallBack() {//回调
                        @Override
                        public void onSuccess() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    EMClient.getInstance().groupManager().loadAllGroups();
                                    EMClient.getInstance().chatManager().loadAllConversations();
                                    Utils.logD(TAG, getString(R.string.login_chat_server_succ));
                                }
                            });
                        }

                        @Override
                        public void onProgress(int progress, String status) {
                        }

                        @Override
                        public void onError(int code, String message) {
                            Utils.logD(TAG, getString(R.string.login_chat_server_fail) + message);
                        }
                    });
                    saveLoginType(false);
                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                } catch (JSONException e) {
                    Utils.logD(TAG, "Error parsing response: " + result.response);
                    showFailedHint(MsResponse.MS_PARSE_FAILED);
                }
            } else if (result.code == MsResponse.MS_VERSION_TOO_OLD) {
                showHint(0);
                showUpdateAppDialog();
            } else {
                showFailedHint(result.code);
            }
            Utils.hidePgressDialog();
            mCanBack = true;
        }
    }

    private void saveUserInfo(String userJson) {
        SharedPreferences sp = DataHelper.getSpForData(this);
        Editor editor = sp.edit();
        editor.putString(AccountInfo.SP_USER_INFO, userJson);
        editor.commit();
    }

    private void showUpdateAppDialog() {
        if (mUpdateAppDialog == null) {
            mUpdateAppDialog = new LightDialog(this);
            mUpdateAppDialog.setTitle(R.string.lgi_failed_version_too_old);
            mUpdateAppDialog.setMessage(R.string.lgi_failed_version_too_old_desc);
            mUpdateAppDialog.setPositiveButton(android.R.string.ok, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.actionView(LoginActivity.this,
                            Uri.parse(getString(R.string.app_url)), null, 0);
                    finish();
                }
            });

        }
        mUpdateAppDialog.show();
    }

    private void showHint(int resId) {
        if (resId > 0) {
            mLoginHint.setText(resId);
        } else {
            mLoginHint.setText("");
        }
    }

    private void showFailedHint(int code) {
        mLoginHint.setText(MsResponse.getFailureDesc(this, R.string.lgi_login_failed, code));
    }

    private void updateInputs(Boolean enabled) {
        mEtAccount.setEnabled(enabled);
        mEtPassword.setEnabled(enabled);
        mButtonLogin.setEnabled(enabled);
    }

    private boolean validInput(String account, String password) {
        if (account.length() < 4) {
            showHint(R.string.lgi_account_too_short);
            return false;
        }
        if (password.length() < 4) {
            showHint(R.string.lgi_password_too_short);
            return false;
        }
        return true;
    }

    private void saveLoginType(boolean loginByExt) {
        Editor editor = mPreferences.edit();
        editor.putBoolean(SP_LOGIN_STYLE, loginByExt);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (!mCanBack) {
            return;
        } else {
            super.onBackPressed();
        }
    }
}
