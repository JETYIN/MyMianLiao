package com.tjut.mianliao;

import java.util.Calendar;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.BaiduNaviManager;
import com.tjut.mianliao.black.CheckNightReceiver;
import com.tjut.mianliao.component.CountDowner;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.SystemInfo;
import com.tjut.mianliao.login.LoginRegistActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.ScreenShotTool;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";

    private static final String SPLASH_IMGID = "splash_imgid";

    private static final String SPLASH_TIME = "splash_time";

    private static final String SP_AD_TOP = "sp_top";

    public static final String EXTRA_SHOW_ONLY = "extra_show_only";

    private static final int SPLASH_SHOWN_MILIS = 100;

    private AccountInfo mAccount;
    // private PromotionManager mPromManager;

    private SharedPreferences mPreferences;

    private boolean isSplashShow = false;

    private int mSplashImgId;
    
    private boolean mAdIsJump = false;

    private String mSplashImg;

    private ProImageView mIvSplashAd;

    private FrameLayout mRlAd;

    private boolean mShowAdv;
    
    private TextView mTvCountDown;
    private int recLen = 4; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.logD(TAG, "start time " + System.currentTimeMillis());
        AnalyticsConfig.setChannel(getString(R.string.app_channel_yingyonghui));
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        showSplash();
        mAccount = AccountInfo.getInstance(this);
        mPreferences = DataHelper.getSpForData(this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                boolean isSameDay = Utils.isSameDay(mPreferences.getLong(SPLASH_TIME,
                        System.currentTimeMillis()));
                boolean isAdTop = mPreferences.getBoolean(SP_AD_TOP, false);
                if (Utils.isNetworkAvailable(SplashActivity.this) && (!isSameDay || !isAdTop)) {
                    new splashAdTask().executeLong();
                }
                isSplashShow = true;
                BaiduNaviManager.getInstance().initEngine(SplashActivity.this, getSdcardDir(),
                        mNaviEngineInitListener, new LBSAuthManagerListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        Utils.logD(TAG, "status = " + status + ", msg = " + msg);
                    }
                });                    
                setupNightChecker();
                if (Utils.isNetworkAvailable(SplashActivity.this) && mAccount.isLoggedIn()) {
                    new SystemInfoTask().executeLong();
                } else {
                    netErrorTodo();
                    processSplash(true);
                }                
            }
        });
    }

    public void setupNightChecker() {
        if (MianLiaoApp.sDayAlarmManager != null) {
            return;
        }
        Intent intent = new Intent(this, CheckNightReceiver.class);
        intent.setAction("com.tjut.mianliao.black");
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());
        int currentMinute = calendar.get(Calendar.MINUTE);
        long diffHourMillis = 60 - currentMinute + 1;

        // 开始时间
        long firstime = SystemClock.elapsedRealtime() + diffHourMillis * 60 * 1000;

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 60 * 60 * 1000, sender);

        MianLiaoApp.sDayAlarmManager = am;
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
        Utils.logD(TAG, "onResume : " + System.currentTimeMillis());
        MobclickAgent.onResume(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.logD(TAG, "end time " + System.currentTimeMillis());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // processSplash();
    }

    private boolean hasIntroductionShow() {
        int introVersion = getSharedPreferences(IntroductionActivity.PREF_NAME, MODE_PRIVATE).getInt(
                IntroductionActivity.PREF_KEY, 0);
        return introVersion >= IntroductionActivity.VERSION;
    }

    private void showSplash() {
        setContentView(R.layout.activity_splash);
        PackageInfo pi = Utils.getPackageInfo(this);
        if (pi != null) {
            TextView tvVersion = (TextView) findViewById(R.id.tv_version);
            tvVersion.setText("面聊v" + pi.versionName + "版");
        }
        Utils.logD(TAG, "show splash");
    }

    private void showAvd() {
        setContentView(R.layout.activity_show_adv);
        mRlAd = (FrameLayout) findViewById(R.id.Rl_ad);
        mIvSplashAd = (ProImageView) findViewById(R.id.iv_ad);
        mTvCountDown = (TextView) findViewById(R.id.tv_count_down);
        mIvSplashAd.setBackground(mSplashImg, R.drawable.ad_uber);
        mIvSplashAd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdIsJump) {
                    Utils.actionView(SplashActivity.this, getAdUrl(), null, 0);
                }
            }

            private String getAdUrl() {
                return Utils.getServerAddress()+"webapp/view/splash?id=" + mSplashImgId;
            }
        });
        ((ImageView) findViewById(R.id.iv_bt_skip)).setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mShowAdv  = true;
                processSplash(false);
            }
        });
        Message message = handler.obtainMessage(1);
        handler.sendMessageDelayed(message, 1000);
    }
    
    final Handler handler = new Handler(){ 
        
        public void handleMessage(Message msg){         // handle message 
            switch (msg.what) { 
                case 1: 
                    recLen--; 
                    mTvCountDown.setText("广告  " + recLen); 
                    
                    if(recLen > 0){ 
                        Message message = handler.obtainMessage(1); 
                        handler.sendMessageDelayed(message, 1000);      // send message 
                    }else{ 
                        mTvCountDown.setVisibility(View.GONE);
                        mShowAdv = true;
                        processSplash(false);
                    } 
            } 
            
            super.handleMessage(msg); 
        } 
    }; 

    private void showPromotion() {
        setContentView(R.layout.activity_splash_promotion);
    }

    private void proceed(Intent intent) {
        if (!getIntent().getBooleanExtra(EXTRA_SHOW_ONLY, false)) {
            startActivity(intent);
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 1s
        View view = this.getWindow().getDecorView();
        ScreenShotTool.appContentHeight = view.getHeight();
        ScreenShotTool.appOtherHeight = ScreenShotTool.getOtherHeight(this);
        finish();
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

    public void processSplash(boolean delay) {
        View view;
        if (mShowAdv) {
            view = mRlAd;
        } else {
            view = findViewById(R.id.iv_logo);
        }
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i;
                if (!hasIntroductionShow()) {
                    i = new Intent(getApplicationContext(), IntroductionActivity.class);
                } else if (mAccount.isLoggedIn()) {
                    i = new Intent(getApplicationContext(), MainActivity.class);
                } else {
                    i = new Intent(getApplicationContext(), LoginRegistActivity.class);
                }
                proceed(i);
            }
        }, delay ? SPLASH_SHOWN_MILIS : 0);
    }

    public void processPromotion() {
        ImageView ivSplash = (ImageView) findViewById(R.id.iv_splash);
        if (mAccount.getUserInfo().isSpecial()) {
            ivSplash.setImageResource(R.drawable.prom_special);
        } else {
            // ivSplash.setImageBitmap(Utils.fileToBitmap(mPromManager.getPromotion().splashImgFile));
        }

        ivSplash.postDelayed(new Runnable() {
            @Override
            public void run() {
                proceed(new Intent(SplashActivity.this, MainActivity.class));
            }
        }, SPLASH_SHOWN_MILIS * 2);
    }

    private void netErrorTodo() {
        Toast.makeText(SplashActivity.this, "网络连接错误，请检查您的网络设置！", Toast.LENGTH_SHORT).show();
    }

    private class SystemInfoTask extends MsTask {

        public SystemInfoTask() {
            super(SplashActivity.this, MsRequest.SYSTEM_INFO);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                SystemInfo systemInfo = SystemInfo.fromJson(response.getJsonObject());
                if (systemInfo != null) {
                    CountDowner.create(systemInfo.start, !systemInfo.neight);
                    if (systemInfo.neight) {
                        Settings.setNightInfoToSp(true);
                    } else {
                        Settings.setNightInfoToSp(false);
                    }
                    if (isSplashShow) {
                        if (mShowAdv) {
                            showAvd();
                        } else {
                            processSplash(false);
                        }
                    }
                }
            } else {
                processSplash(false);
            }
        }

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
                    if (json != null) {
                        mSplashImgId = json.optInt("id");
                        mSplashImg = json.optString("image");
                        mAdIsJump = json.optBoolean("is_jump");
                        saveAdInfo(false);
                        mShowAdv = true;
                    } else {
                        saveAdInfo(true);
                        mShowAdv = false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                netErrorTodo();
                processSplash(false);
            }
        }

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
