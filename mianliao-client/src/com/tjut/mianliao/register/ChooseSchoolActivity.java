package com.tjut.mianliao.register;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.LoginStateHelper;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.UserExtInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.login.LoginActivity;
import com.tjut.mianliao.login.UserExtLoginManager;
import com.tjut.mianliao.login.UserExtLoginManager.UserExtRegisterListener;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsMhpTask;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class ChooseSchoolActivity extends BaseActivity implements
        OnSearchTextListener, Runnable, OnItemClickListener, OnRefreshListener<ListView>, UserExtRegisterListener {

    private static final long DELAY_MILLS = 1000;
    private static final int OTHER_SCHOOL_ID = 3;

    private RegInfo mRegInfo = RegInfo.getInstance();
    private PullToRefreshListView mPtrlvSchools;
    private School mOtherSchool;

    private Handler mHandler;
    private ArrayList<School> mSchools = new ArrayList<School>();
    private BaseAdapter mAdapter;

    private String mKeyWord;
    private UserExtLoginManager mUserExtLoginManager;
    private UserExtInfo mUserExtInfo;
    private SharedPreferences mPreferences;
    private RegisterTask mLastTask;
    private boolean edit, mIsExtLogin;
    private TextView mSchoolName;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search_generic;
    }

    /* (non-Javadoc)
     * @see com.tjut.mianliao.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsExtLogin = getIntent().getBooleanExtra(LoginActivity.IS_EXT_LOGIN, false);
        edit = getIntent().getBooleanExtra(RegInfo.EDIT, false);

        mOtherSchool = new School(OTHER_SCHOOL_ID, getString(R.string.reg_other_school));

        getTitleBar().showTitleText(R.string.reg_choose_school, null);
        ((TextView) findViewById(R.id.tv_search_hint)).setText(R.string.reg_choose_school_hint);

        mHandler = new Handler();
        mAdapter = new ArrayAdapter<School>(this, R.layout.list_item_tv_search_result, mSchools);
        SearchView svSchool = (SearchView) findViewById(R.id.sv_search);
        svSchool.setHint(R.string.reg_choose_school_search_hint);
        svSchool.setOnSearchTextListener(this);

        mPtrlvSchools = (PullToRefreshListView) findViewById(R.id.lv_search_result);
        mPtrlvSchools.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mPtrlvSchools.getRefreshableView().addFooterView(new View(this));
        mPtrlvSchools.setOnRefreshListener(this);
        mPtrlvSchools.setAdapter(mAdapter);
        mPtrlvSchools.setOnItemClickListener(this);
        getTitleBar().showProgress();
        run();
        mUserExtLoginManager = UserExtLoginManager.getInstance(this);
        mUserExtLoginManager.registerUserExtRegisterListener(this);
        mUserExtInfo = mUserExtLoginManager.getUserExtInfo();
        mPreferences = DataHelper.getSpForData(this);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserExtLoginManager.unregisterUserExtRegisterListener(this);
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        getTitleBar().showProgress();

        mKeyWord = text.toString();
        mHandler.removeCallbacks(this);
        mHandler.postDelayed(this, DELAY_MILLS);
    }

    @Override
    public void run() {
        new LoadSchoolTask(0, mKeyWord).executeLong();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        School school = (School) parent.getItemAtPosition(position);
        if (school != null) {
            RegInfo info = RegInfo.getInstance();
            info.schoolId = school.id;
            info.schoolName = school.name;

            Intent i = new Intent();
            if (mIsExtLogin) {
                if (mUserExtInfo != null) {
                    mUserExtInfo.schoolId = school.id;
                    mUserExtLoginManager.userExtRegister();
                    getTitleBar().showProgress();  
                    return;
                }
            } else {
                if (edit) {
                    i.putExtra(AccountBasicActivity.MY_SCHOOL_INFO,school.name);
                    setResult(RESULT_OK, i);
                    finish();
                } else {
                    i.putExtra(AccountBasicActivity.MY_SCHOOL_INFO,school.name);
                    i.putExtra(AccountBasicActivity.MY_SCHOOL_ID, school.id);
                    setResult(RESULT_OK, i);
                    finish();
                }  
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_UPDATED && requestCode == getIdentity()) {
            setResult(RESULT_UPDATED);
            finish();
        }
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        if (TextUtils.isEmpty(mKeyWord)) {
            new LoadSchoolTask(mSchools.size(), mKeyWord).executeLong();
        }
    }

    private static class School {
        int id;
        String name;

        public School(int id, String name) {
            this.id = id;
            this.name = name;
        }

        static final JsonUtil.ITransformer<School> TRANSFORMER
                = new JsonUtil.ITransformer<School>() {
            @Override
            public School transform(JSONObject json) {
                return json == null ? null : new School(json.optInt("id"), json.optString("name"));
            }
        };

        @Override
        public String toString() {
            return name;
        }
    }

    private class LoadSchoolTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private static final int LIMIT = 20;
        private int mOffset;
        private String mSearchKey;

        private LoadSchoolTask(int offset, String keyword) {
            mOffset = offset;
            mSearchKey = keyword;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            String prm = "name=" + Utils.urlEncode(mSearchKey) + "&offset=" + mOffset +
                    "&limit=" + LIMIT;
            return HttpUtil.msRequest(getApplicationContext(), MsRequest.SEARCH_SCHOOL, prm);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(response)) {
                JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                ArrayList<School> schools = JsonUtil.getArray(ja, School.TRANSFORMER);
                Utils.logD(getTag(), "size: " + schools.size());
                if (TextUtils.isEmpty(mSearchKey) && schools.size() == LIMIT) {
                    mPtrlvSchools.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
                } else {
                    mPtrlvSchools.setMode(PullToRefreshBase.Mode.DISABLED);
                }

                if (mOffset == 0) {
                    mSchools.clear();
                }
                mSchools.addAll(schools);
                if (mSchools.size() < LIMIT) {
                    mSchools.add(mOtherSchool);
                }
                mAdapter.notifyDataSetChanged();
                mPtrlvSchools.onRefreshComplete();
            }
        }
    }

    @Override
    public void onRegisterSuccess(int type) {
        saveLoginType(true);
        startActivity(new Intent(this, MainActivity.class));
        try {
            ActivityCompat.finishAffinity(this);
        } catch (IllegalStateException e) {
            finish();
        }
    }

    @Override
    public void onRegisterFailed() {
        getTitleBar().hideProgress();
        toast(R.string.reg_failed);
    }

    private void saveLoginType(boolean loginByExt) {
        Editor editor = mPreferences.edit();
        editor.putBoolean(LoginActivity.SP_LOGIN_STYLE, loginByExt);
        editor.commit();
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
            Context context = ChooseSchoolActivity.this;
            if (response.isSuccessful()) {
                JSONObject json = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                UserInfo me = UserInfo.fromJson(json);
                if (json == null || TextUtils.isEmpty(json.optString("token")) || me == null || me.userId == 0) {
                    response.showFailInfo(context, R.string.reg_failed);
                } else {
                    String token = json.optString("token");
                    LoginStateHelper.accountLogin(context, me.account, token, me);
                    startActivity(new Intent(context, MainActivity.class));
                    ActivityCompat.finishAffinity(ChooseSchoolActivity.this);
                }
            } else {
                response.showFailInfo(context, R.string.reg_failed);
            }
        }
    }


}