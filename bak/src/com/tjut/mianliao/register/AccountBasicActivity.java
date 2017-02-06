package com.tjut.mianliao.register;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.LoginStateHelper;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.UserExtInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.login.LoginActivity;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.GetImageHelper.ImageResultListener;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class AccountBasicActivity extends BaseActivity implements OnClickListener,
        ImageResultListener {

    private static final int PASSWORD_MIN_LEN = 5;
    public static final int CHOOSE_SCHOOL_VIEW = 214;
    public static final String MY_SCHOOL_INFO = "my_school_info";
    public static final String MY_SCHOOL_ID = "my_school_id";
    
    private RegInfo mRegInfo = RegInfo.getInstance();
    private LightDialog mGenderDialog;
    private EditText mEtPassword;
    private ImageView mCheckPass;
    private int mShowflag = 0;
    private EditText mEtNickname;
    private EditText mEtEmail, mShowSchool;
    private LinearLayout mLlNickname, mRlCerti;
    private RelativeLayout mRlUserinfo, mRlsex, mRlSchool;
    private RegisterTask mLastTask;
    private TextView mShowSex;
    private View mLin1;
    private String mSchool;
    private int mSchoolId;
    private UserExtInfo mUserExtInfo;
    private SharedPreferences mPreferences;
    private ImageView mSexLogo;
    private GetImageHelper mGetImageHelper;
    private String mImagePath;
    private AvatarView mAvatar;

    private boolean mUserNameUpdated = false;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_account_basic;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = DataHelper.getSpForData(this);
        getTitleBar().showTitleText(R.string.regist, null);
        mEtPassword = (EditText) findViewById(R.id.et_pass_word);
        mCheckPass = (ImageView) findViewById(R.id.iv_password_check);
        mEtNickname = (EditText) findViewById(R.id.et_nick_name);
        mEtEmail = (EditText) findViewById(R.id.et_emails);
        mLlNickname = (LinearLayout) findViewById(R.id.ll_nickname);
        mRlUserinfo =(RelativeLayout) findViewById(R.id.rl_muserinfo);
        mRlsex =(RelativeLayout) findViewById(R.id.rl_msex);
        mShowSex = (TextView) findViewById(R.id.tv_show_sex);
        mLin1 = findViewById(R.id.view_lin3);
        mRlCerti = (LinearLayout) findViewById(R.id.rl_register_cer);
        mRlSchool = (RelativeLayout) findViewById(R.id.rl_school);
        mShowSchool = (EditText) findViewById(R.id.tv_show_school);
        mSexLogo = (ImageView) findViewById(R.id.iv_sex);
        mAvatar = (AvatarView) findViewById(R.id.iv_get_avatar);
        mGetImageHelper = new GetImageHelper(this, this);

        mShowSchool.setText("");
        mEtPassword.setText("");
        mEtNickname.setText("");
        mEtEmail.setText("");
        mShowSex.setText(getString(R.string.prof_female));
        mRegInfo.gender = 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_password_check:
                    if(mShowflag == 0){
                        mEtPassword.setTransformationMethod(
                                HideReturnsTransformationMethod.getInstance());
                        mCheckPass.setImageResource(R.drawable.bottom_eyes_close);
                        mEtPassword.setSelection(mEtPassword.getText().length());
                        mShowflag = 1;
                    }else {
                        mEtPassword.setTransformationMethod(
                                PasswordTransformationMethod.getInstance());
                        mCheckPass.setImageResource(R.drawable.pic_bt_check);
                        mEtPassword.setSelection(mEtPassword.getText().length());
                        mShowflag = 0;
                    }
                break;
            case R.id.bt_confirm:
                collectInfo();
                validInfo();

                break;
            case R.id.rl_msex:
//                showGenderDialog();
                if (mRegInfo.gender == 0 ){
                    mShowSex.setText(getString(R.string.prof_male));
                    mSexLogo.setImageResource(R.drawable.register_pic_big_man);
                    mRegInfo.gender = 1;
                } else {
                    mShowSex.setText(getString(R.string.prof_female));
                    mSexLogo.setImageResource(R.drawable.register_pic_big_woman);
                    mRegInfo.gender = 0;
                }
                break;
            case R.id.rl_school:
            case R.id.tv_show_school:
                Intent intent = new Intent(AccountBasicActivity.this, ChooseSchoolActivity.class);
                intent.putExtra(LoginActivity.IS_EXT_LOGIN, false);
                startActivityForResult(intent, CHOOSE_SCHOOL_VIEW);
                break;
            case R.id.iv_get_avatar:
                mGetImageHelper.getImage(true, 1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        collectInfo();
        super.onBackPressed();
    }

    private void collectInfo() {
        mRegInfo.schoolId = mSchoolId;
        mRegInfo.password = mEtPassword.getText().toString();
        mRegInfo.nickName = mEtNickname.getText().toString();
        mRegInfo.email = mEtEmail.getText().toString();
        mRegInfo.avatar = mImagePath;
    }

    private void validInfo() {
		if (isNicknameValid() && isMyschoolValid() && isPasswordValid() && isEmailValid()) {
			register();
        }
    }

    private boolean isUserNameValid() {
        if (Utils.USER_NAME_PATTERN.matcher(mRegInfo.userName).matches()) {
            return true;
        } else {
            Toast.makeText(this, R.string.reg_format_user_name, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean isPasswordValid() {
        if (mEtPassword.length() > PASSWORD_MIN_LEN) {
            return true;
        } else {
            toast(R.string.reg_format_password);
            return false;
        }
    }

    private boolean isEmailValid() {
        if (Utils.EMAIL_PATTERN.matcher(mEtEmail.getText().toString().trim()).matches()) {
            return true;
        } else {
            toast(R.string.reg_email_invalid);
            return false;
        }
    }

    private boolean isNicknameValid() {
        if (Utils.isHasAt(mRegInfo.nickName)){
            toast(R.string.reg_format_nickname_puppet);
            return false;
        } else if (Utils.isHasTopic(mRegInfo.nickName)) {
            toast(R.string.reg_format_nickname_puppet);
            return false;
        } else if (Utils.NICK_NAME_PATTERN.matcher(mRegInfo.nickName).matches()) {
            return true;
        } else {
            toast(R.string.reg_format_nickname);
            return false;
        }
    } 
    
    
    
    private boolean isMyschoolValid() {
    	if (mShowSchool.getText().length() <= 0) {
    		toast(R.string.lgi_show_error_school);
    		return false;
    	} else {
    		return true;
    	}
    }

    private void register() {
        if (mLastTask == null) {
            new RegisterTask(mRegInfo).executeLong();
        }
    }

    @Override
    protected void onDestroy() {
        if (mLastTask != null) {
            mLastTask.cancel(false);
        }
        super.onDestroy();
    }
    private void showGenderDialog() {
        if (mGenderDialog == null) {
            mGenderDialog = new LightDialog(this);
            mGenderDialog.setTitle(R.string.prof_gender);
            mGenderDialog.setItems(R.array.reg_choose_gender_menu, new DialogInterface
                    .OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mRegInfo.gender = which;
                    mShowSex.setText(mRegInfo.gender == 0 ? R.string.prof_female : R.string.prof_male);
                }
            });
        }
        mGenderDialog.show();  
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_SCHOOL_VIEW:
                if (resultCode == RESULT_OK) {
                    mSchool = data.getStringExtra(MY_SCHOOL_INFO);
                    mSchoolId = data.getIntExtra(MY_SCHOOL_ID, 0);
                    mShowSchool.setText(mSchool);
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    mGetImageHelper.handleResult(requestCode, data);
                }
                break;
        }
    }

    private class RegisterTask extends MsMhpTask {
        public RegisterTask(RegInfo regInfo) {
            super(getApplicationContext(), MsRequest.REGISTER, regInfo.getParameters(), regInfo.getFiles());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLastTask = this;
            Utils.showProgressDialog(AccountBasicActivity.this, R.string.reg_waiting);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            mLastTask = null;
            Utils.hidePgressDialog();
            Context context = AccountBasicActivity.this;
            if (response.isSuccessful()) {
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                UserInfo me = UserInfo.fromJson(json);
                if (json == null || TextUtils.isEmpty(json.optString("token")) || me == null || me.userId == 0) {
                    response.showFailInfo(context, R.string.reg_failed);
                } else {
                    String token = json.optString("token");
                    LoginStateHelper.accountLogin(context, me.account, token, me);
                    startActivity(new Intent(context, MainActivity.class));
                    saveLoginType(false);
                    ActivityCompat.finishAffinity(AccountBasicActivity.this);
                }
            } else {
                response.showInfo(context, response.getFailureDesc(response.code));
            }
        }
    }
    
    private void saveLoginType(boolean loginByExt) {
        Editor editor = mPreferences.edit();
        editor.putBoolean(LoginActivity.SP_LOGIN_STYLE, loginByExt);
        editor.commit();
    }

    @Override
    public void onImageResult(boolean success, String imageFile, Bitmap bm) {
        mImagePath = imageFile;
        if (!(bm == null)){
            mAvatar.setImageBitmap(bm);
        }
    }

    @Override
    public void onImageResult(boolean success, ArrayList<String> images) {
        if (success) {
            String imageFile = images.get(0);
            Bitmap bm = BitmapFactory.decodeFile(imageFile);
            mImagePath = imageFile;
            if (!(bm == null)){
                mAvatar.setImageBitmap(bm);
            }
        }
    }
    
}