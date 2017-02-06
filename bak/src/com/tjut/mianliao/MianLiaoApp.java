package com.tjut.mianliao;

import im.fir.sdk.FIR;

import org.lasque.tusdk.core.TuSdk;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.callback.InitResultCallback;
import com.baidu.mapapi.SDKInitializer;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.sdk.android.QupaiService;
import com.duanqu.qupai.sdk.utils.AppGlobalSetting;
import com.tjut.mianliao.common.Contant;
import com.tjut.mianliao.common.MoreMusicActivity;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.util.TrackingUtil;
import com.tjut.mianliao.util.Utils;

public class MianLiaoApp extends MultiDexApplication {

    protected static final String TAG = "MianLiaoApp";
    public static final String PREF_VIDEO_EXIST_USER = "pref_video_exist_user";
    private static final String sTuSdkKey = "7218e1cfffb6d1fe-00-i7szo1";

    private static Context mContext;

    public static boolean sIsGuidShow;

    public static AlarmManager sDayAlarmManager = null;
    
    private Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        MultiDex.install(mContext);
        Utils.init(mContext);
        TrackingUtil.init(mContext);
        CourseUtil.init(mContext);
        SDKInitializer.initialize(mContext);
        // if (Utils.isNetworkAvailable(mContext)) {
        TuSdk.init(mContext, sTuSdkKey);
        // it should be false while in release
        TuSdk.enableDebugLog(true);
        FIR.init(this);
        mHandler.postDelayed(new Runnable() {
            
            @Override
            public void run() {
                initAliSDK();
            }
        }, 0);
    }

    private void initAliSDK() {
        AlibabaSDK.turnOnDebug();
        AlibabaSDK.asyncInitWithFinish(this, new InitResultCallback() {
            @Override
            public void onSuccess() {
                Utils.logD(TAG, "初始化成功");
                QupaiService qupaiService = AlibabaSDK.getService(QupaiService.class);

                if (qupaiService == null) {
                    Toast.makeText(mContext, "插件没有初始化，无法获取 QupaiService", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent moreMusic = new Intent();
                moreMusic.setClass(MianLiaoApp.this, MoreMusicActivity.class);

                VideoSessionCreateInfo info = new VideoSessionCreateInfo.Builder()
                        .setOutputDurationLimit(Contant.DEFAULT_DURATION_LIMIT)
                        .setOutputVideoBitrate(Contant.DEFAULT_BITRATE).setHasImporter(true)
                         .setWaterMarkPath(Contant.WATER_MARK_PATH)
                        .setWaterMarkPosition(1).setHasEditorPage(true).build();

                qupaiService.hasMroeMusic(moreMusic);

                if (qupaiService != null) {
                    qupaiService.addMusic(0, "Athena", "assets://Qupai/music/Athena");
                    qupaiService.addMusic(1, "Box Clever", "assets://Qupai/music/Box Clever");
                    qupaiService.addMusic(2, "Byebye love", "assets://Qupai/music/Byebye love");
                    qupaiService.addMusic(3, "chuangfeng", "assets://Qupai/music/chuangfeng");
                    qupaiService.addMusic(4, "Early days", "assets://Qupai/music/Early days");
                    qupaiService.addMusic(5, "Faraway", "assets://Qupai/music/Faraway");
                }

                qupaiService.initRecord(info);

                final AppGlobalSetting sp = new AppGlobalSetting(mContext);
                sIsGuidShow = sp.getBooleanGlobalItem(PREF_VIDEO_EXIST_USER, true);
            }

            @Override
            public void onFailure(int i, String s) {
                Utils.logE(TAG, s);
            }
        });
    }

    public static Context getAppContext() {
        return mContext;
    }
}