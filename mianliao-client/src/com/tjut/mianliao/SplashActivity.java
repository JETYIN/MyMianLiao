package com.tjut.mianliao;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager;

import com.hyphenate.chat.EMClient;

import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.login.LoginRegistActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.ScreenShotTool;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;


public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";

    private static final String SPLASH_IMGID = "splash_imgid";

    private static final String SPLASH_TIME = "splash_time";

    private static final String SP_AD_TOP = "sp_top";

    public static final String EXTRA_SHOW_ONLY = "extra_show_only";

    private static final int SPLASH_SHOWN_MILIS = 100;

    private static final int AD_MSG = 1;

    private static final int DONE_FIRST_ADV = 1;

    private AccountInfo mAccount;

    private SharedPreferences mPreferences;

    private int mSplashImgId;

    private boolean mAdIsJump = false;

    private String mSplashImg;

    private ProImageView mIvSplashAd;

    private FrameLayout mRlAd;

    private boolean mShowAdv;

    private TextView mTvCountDown;
    private int recLen = 4;

    private boolean mIsSplashShow;

    private boolean mIsAdvShowing;

    private boolean mIsSkiped;

    private int mDoneFirst = 0;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        showSplash();
        mAccount = AccountInfo.getInstance(this);
        mPreferences = DataHelper.getSpForData(this);
        boolean isSameDay = Utils.isSameDay(mPreferences.getLong(SPLASH_TIME,
                System.currentTimeMillis()));
        boolean isAdTop = mPreferences.getBoolean(SP_AD_TOP, false);
        /**获取sharespreference数据进行登录**/
        if (Utils.isNetworkAvailable(this) && (!isSameDay || !isAdTop) && mAccount.isLoggedIn()) {
            new splashAdTask().executeQuick();
            mIsSplashShow = true;
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                BaiduNaviManager.getInstance().initEngine(SplashActivity.this, getSdcardDir(),
                        mNaviEngineInitListener, new LBSAuthManagerListener() {
                            @Override
                            public void onAuthResult(int status, String msg) {
                                Utils.logD(TAG, "status = " + status + ", msg = " + msg);
                            }
                        });
//                MianLiaoApp.initAliSDK();
                if (Utils.isNetworkAvailable(SplashActivity.this) && mAccount.isLoggedIn()) {
                    getFreshRateInfo();
                } else {
                    processSplash(true);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void getFreshRateInfo() {
        new GetRefreshRate().executeLong();
    }

    private boolean needShowIntroduction() {
        int introVersion = getSharedPreferences(IntroductionActivity.PREF_NAME, MODE_PRIVATE)
                .getInt(IntroductionActivity.PREF_KEY, 0);
        return introVersion < mPackgeInfo.versionCode;
    }

    private void showSplash() {
        setContentView(R.layout.activity_splash);
        mPackgeInfo = Utils.getPackageInfo(this);
        if (mPackgeInfo != null) {
            TextView tvVersion = (TextView) findViewById(R.id.tv_version);
            tvVersion.setText(getString(R.string.ml_version_desc, mPackgeInfo.versionName));
        }
    }

    private void showAvd() {
        if (mIsAdvShowing) {
            return;
        }
        mIsSkiped = false;
        mIsAdvShowing = true;
        setContentView(R.layout.activity_show_adv);
        mRlAd = (FrameLayout) findViewById(R.id.Rl_ad);
        mIvSplashAd = (ProImageView) findViewById(R.id.iv_ad);
        mTvCountDown = (TextView) findViewById(R.id.tv_count_down);
        mIvSplashAd.setBackground(mSplashImg, 0);
        mIvSplashAd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdIsJump) {
                    Utils.actionView(SplashActivity.this, getAdUrl(), null, 0);
                }
            }

            private String getAdUrl() {
                return Utils.getServerAddress() + "webapp/view/splash?id=" + mSplashImgId;
            }
        });
        ((ImageView) findViewById(R.id.iv_bt_skip)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsSkiped = true;
                mShowAdv = true;
                mHandler.removeMessages(AD_MSG);
                processSplash(false);
            }
        });
        Message message = handler.obtainMessage(AD_MSG);
        handler.sendMessageDelayed(message, 1000);
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {// handle message
            switch (msg.what) {
                case AD_MSG:
                    recLen--;
                    mTvCountDown.setText(getString(R.string.ad, recLen));
                    if (recLen > 0) {
                        Message message = handler.obtainMessage(1);
                        handler.sendMessageDelayed(message, 1000); // send message
                    } else {
                        mTvCountDown.setVisibility(View.GONE);
                        mShowAdv = true;
                        mIsSkiped = true;
                        mIsAdvShowing = false;
                        processSplash(false);
                    }
            }
            super.handleMessage(msg);
        }
    };

    private void proceed(Intent intent) {
        if (!getIntent().getBooleanExtra(EXTRA_SHOW_ONLY, false)) {
            startActivity(intent);
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 1s
        View view = this.getWindow().getDecorView();
        ScreenShotTool.appContentHeight = view.getHeight();
        ScreenShotTool.appOtherHeight = ScreenShotTool.getOtherHeight(this);
        saveScreenInfo(ScreenShotTool.appContentHeight, ScreenShotTool.appOtherHeight);
        finish();
    }

    private void saveScreenInfo(int appContentHeight, int appOtherHeight) {
        SharedPreferences spForData = DataHelper.getSpForData(this);
        Editor editor = spForData.edit();
        editor.putInt(ScreenShotTool.SP_APP_CONTENT_HEIGHT, appContentHeight);
        editor.putInt(ScreenShotTool.SP_APP_OTHER_HEIGHT, appOtherHeight);
        editor.commit();
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
        @Override
        public void engineInitSuccess() {
            Utils.logD(TAG, "engineInitSuccess");
        }

        @Override
        public void engineInitStart() {
            Utils.logD(TAG, "engineInitStart");
        }

        @Override
        public void engineInitFail() {
            Utils.logD(TAG, "engineInitFail");
        }
    };

    private PackageInfo mPackgeInfo;
    private boolean mProcessSplashing;

    View view;

    public void processSplash(boolean delay) {
        if ((!mIsSkiped && mIsAdvShowing) || mProcessSplashing) {
            return;
        }
        mProcessSplashing = true;
        if (mShowAdv) {
            view = mRlAd;
        } else {
            view = findViewById(R.id.tv_version);
        }
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                if (needShowIntroduction()) {
                    i = new Intent(getApplicationContext(), IntroductionActivity.class);
                } /**sharespreference中的保存有登录信息**/
                else if (mAccount.isLoggedIn()) {
                    EMClient.getInstance().groupManager().loadAllGroups();
                    EMClient.getInstance().chatManager().loadAllConversations();
                    i = new Intent(getApplicationContext(), MainActivity.class);
                } else {
                    i = new Intent(getApplicationContext(), LoginRegistActivity.class);
                }
                proceed(i);
            }
        }, delay ? SPLASH_SHOWN_MILIS : 0);
    }

    private class splashAdTask extends MsTask {

        public splashAdTask() {
            super(SplashActivity.this, MsRequest.SPLASH);
        }

        @Override
        protected String buildParams() {
            return "id=" + getAdId();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            Utils.logD(TAG, "get adv " + (response.isSuccessful() ? "Success" : "Fail"));
            if (response.isSuccessful()) {
                try {
                    JSONObject json = response.getJsonObject();
                    mDoneFirst = DONE_FIRST_ADV;
                    if (json != null) {
                        mSplashImgId = json.optInt("id");
                        mSplashImg = json.optString("image");
                        mAdIsJump = json.optBoolean("is_jump");
                        saveAdInfo(false);
                        mShowAdv = true;
                        showAvd();
                    } else {
                        saveAdInfo(true);
                        mShowAdv = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private class InitQupaiTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            MianLiaoApp.initAliSDK();
            return null;
        }
    }

    private class GetRefreshRate extends MsTask {

        public GetRefreshRate() {
            super(SplashActivity.this, MsRequest.REFRESH_RATE);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                int rate = response.getJsonObject().optInt("refreshRate") * 1000;
                saveRateInfo(rate);
                if (mIsSplashShow) {
                    if (mDoneFirst == DONE_FIRST_ADV) {
                        if (mShowAdv) {
                            showAvd();
                        } else {
                            processSplash(false);
                        }
                    }
                } else {
                    processSplash(false);
                }
            } else {
                processSplash(false);
            }
        }
    }

    private void saveRateInfo(int rate) {
        mPreferences.edit().putInt(Constant.SP_FRESH_TIME, rate).commit();
    }

    private void saveAdInfo(boolean isTop) {
        Editor editor = mPreferences.edit();
        if (!isTop) {
            editor.putInt(SPLASH_IMGID, mSplashImgId);
            editor.putLong(SPLASH_TIME, System.currentTimeMillis());
        } else {
            editor.putBoolean(SP_AD_TOP, isTop);
            editor.putInt(SPLASH_IMGID, 0);
        }
        editor.commit();
    }

    private int getAdId() {
        return mPreferences.getInt(SPLASH_IMGID, 0);
    }

}
