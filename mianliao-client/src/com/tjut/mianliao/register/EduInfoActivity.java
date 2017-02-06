package com.tjut.mianliao.register;

import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.LoginStateHelper;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CardItemConf;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.UserExtInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.login.LoginActivity;
import com.tjut.mianliao.login.UserExtLoginManager;
import com.tjut.mianliao.login.UserExtLoginManager.UserExtRegisterListener;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;

public class EduInfoActivity extends BaseActivity implements UserExtRegisterListener {

    private RegInfo mRegInfo = RegInfo.getInstance();
    private UserExtInfo mUserExtInfo;
    private UserExtLoginManager mUserExtLoginManager;
    private SharedPreferences mPreferences;
    private RegisterTask mLastTask;
    private LightDialog mEdubackDialog;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_eduinfo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.reg_edu_info, null);
        mUserExtLoginManager = UserExtLoginManager.getInstance(this);
        mUserExtLoginManager.registerUserExtRegisterListener(this);
        mUserExtInfo = mUserExtLoginManager.getUserExtInfo();
        mPreferences = DataHelper.getSpForData(this);
        updateInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserExtLoginManager.unregisterUserExtRegisterListener(this);
    }

    private void updateInfo() {
        ((CardItemConf) findViewById(R.id.cic_school)).setContent(mRegInfo.schoolName);
        ((CardItemConf) findViewById(R.id.cic_department)).setContent(mRegInfo.departmentName);
        ((CardItemConf) findViewById(R.id.cic_year)).setContent(getString(R.string.reg_start_year_desc,
                mRegInfo.startYear));
        if (mRegInfo.eduback == null) {
            RegInfo.getInstance().meduback = 1;
            ((CardItemConf) findViewById(R.id.cic_eduback)).setContent(R.string.prof_degree_bachelor);
        } else {
            ((CardItemConf) findViewById(R.id.cic_eduback)).setContent(mRegInfo.eduback);
        }
        if (mUserExtInfo != null) {
            ((Button) findViewById(R.id.btn_confirm)).setText(R.string.reg_finish);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cic_school:
                Intent iCs = new Intent(this, ChooseSchoolActivity.class);
                iCs.putExtra(RegInfo.EDIT, true);
                startActivityForResult(iCs, getIdentity());
                break;
            case R.id.cic_department:
                Intent iCd = new Intent(this, ChooseDepartmentActivity.class);
                iCd.putExtra(RegInfo.EDIT, true);
                startActivityForResult(iCd, getIdentity());
                break;
            case R.id.cic_year:
                Intent iCsy = new Intent(this, ChooseStartYearActivity.class);
                iCsy.putExtra(RegInfo.EDIT, true);
                startActivityForResult(iCsy, getIdentity());
                break;
            case R.id.cic_eduback:
                showDialog();
                // Intent iCse = new Intent(this, EduBackActivity.class);
                // iCse.putExtra(RegInfo.EDIT, true);
                // startActivityForResult(iCse, getIdentity());
                break;
            case R.id.btn_confirm:
                if (mUserExtInfo != null) {
                    encapsulateData();
                    mUserExtLoginManager.userExtRegister();
                    getTitleBar().showProgress();
                    return;
                }
                register();
                break;
            default:
                break;
        }
    }

    private void saveLoginType(boolean loginByExt) {
        Editor editor = mPreferences.edit();
        editor.putBoolean(LoginActivity.SP_LOGIN_STYLE, loginByExt);
        editor.commit();
    }

    /**
     * encapsulate data into mUserExtInfo
     */
    private void encapsulateData() {
        mUserExtInfo.schoolId = mRegInfo.schoolId;
//        mUserExtInfo.departmentId = mRegInfo.departmentId;
//        mUserExtInfo.year = mRegInfo.startYear;
//        mUserExtInfo.education = mRegInfo.meduback;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_UPDATED && requestCode == getIdentity()) {
            updateInfo();
        }
    }

    @Override
    public void onRegisterSuccess(int type) {
        saveLoginType(true);
        startActivity(new Intent(this, MainActivity.class));
        ActivityCompat.finishAffinity(this);
    }

    @Override
    public void onRegisterFailed() {
        getTitleBar().hideProgress();
        toast(R.string.reg_failed);
    }

    private void register() {
        if (mLastTask == null) {
            new RegisterTask(mRegInfo).executeLong();
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
            getTitleBar().showProgress();
            // mBtConfirm.setEnabled(false);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            mLastTask = null;
            getTitleBar().hideProgress();
            // mBtConfirm.setEnabled(true);
            Context context = EduInfoActivity.this;
            if (response.isSuccessful()) {
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                UserInfo me = UserInfo.fromJson(json);
                if (json == null || TextUtils.isEmpty(json.optString("token")) || me == null || me.userId == 0) {
                    response.showFailInfo(context, R.string.reg_failed);
                } else {
                    String token = json.optString("token");
                    LoginStateHelper.accountLogin(context, mRegInfo.userName, token, me);
                    startActivity(new Intent(context, MainActivity.class));
                    ActivityCompat.finishAffinity(EduInfoActivity.this);
                }
            } else {
                response.showFailInfo(context, R.string.reg_failed);
            }
        }
    }

    private void showDialog() {
        if (mEdubackDialog == null) {
            mEdubackDialog = new LightDialog(this).setTitleLd(R.string.reg_choose_edu_back).setItems(
                    R.array.education_background, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TypedArray ta = getResources().obtainTypedArray(R.array.education_background);
                            RegInfo.getInstance().eduback = ta.getString(which);
                            ((CardItemConf) findViewById(R.id.cic_eduback)).setContent(mRegInfo.eduback);
                        }

                    });
        }
        mEdubackDialog.show();
    }
}