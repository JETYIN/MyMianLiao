package com.tjut.mianliao;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.duanqu.qupai.engine.session.MovieExportOptions;
import com.duanqu.qupai.engine.session.ProjectOptions;
import com.duanqu.qupai.engine.session.ThumbnailExportOptions;
import com.duanqu.qupai.engine.session.UISettings;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.sdk.android.QupaiManager;
import com.duanqu.qupai.sdk.android.QupaiService;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;
import com.lecloud.config.LeCloudPlayerConfig;
import com.letv.proxy.LeCloudProxy;
import com.lidroid.xutils.util.LogUtils;
import com.pingplusplus.android.PingppLog;
import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.forum.QupaiAuth;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.TrackingUtil;
import com.tjut.mianliao.util.Utils;

import org.apache.commons.lang.StringUtils;
import org.lasque.tusdk.core.TuSdk;

import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class MianLiaoApp extends Application {

    protected static final String TAG = "MianLiaoApp";
    public static final String PREF_VIDEO_EXIST_USER = "pref_video_exist_user";
    public static final String sTuSdkKey = "57bad1d51c434566-02-i7szo1";
    public static final String KEY_DEX2_SHA1 = "dex2-SHA1-Digest";

    private static Context mContext;

    public static boolean sIsGuidShow;

    PackageInfo mPackageInfo;

    private String mCurentProcessName;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        if (!quickStart()) {
            init();
        }
    }

    public static void initAliSDK() {
        QupaiAuth.getInstance().initAuth(mContext, Constant.APP_KEY, Constant.APP_SECRET, Constant.space);
        QupaiService qupaiService = QupaiManager.getQupaiService(mContext);
        if (qupaiService == null) {
            return;
        }
        UISettings _UISettings = new UISettings() {

            @Override
            public boolean hasEditor() {
                return true;
            }

            @Override
            public boolean hasImporter() {
                return super.hasImporter();
            }

            @Override
            public boolean hasSkinBeautifer() {
                return true;
            }

            @Override
            public boolean hasGuide() {
                return true;
            }

        };

        MovieExportOptions movie_options = new MovieExportOptions.Builder()
                .setVideoBitrate(Constant.DEFAULT_BITRATE)
                .configureMuxer("movflags", "+faststart")
                .build();
        /**设置视频最大时长为600s**/
        ProjectOptions projectOptions = new ProjectOptions.Builder()
                .setVideoSize(480, 480)
                .setVideoFrameRate(30)
                .setDurationRange(Constant.DEFAULT_MIN_DURATION_LIMIT, Constant.DEFAULT_DURATION_LIMIT)
                .get();

        ThumbnailExportOptions thumbnailExportOptions = new ThumbnailExportOptions.Builder()
                .setCount(1).get();

        VideoSessionCreateInfo info = new VideoSessionCreateInfo.Builder()
                .setWaterMarkPath(Constant.WATER_MARK_PATH)
                .setWaterMarkPosition(1)
                .setCameraFacing(Camera.CameraInfo.CAMERA_FACING_BACK)
                .setBeautyProgress(60)
                .setBeautySkinOn(true)
                .setMovieExportOptions(movie_options)
                .setThumbnailExportOptions(thumbnailExportOptions)
                .build();

        qupaiService.initRecord(info, projectOptions, _UISettings);


        if (qupaiService != null) {
            qupaiService.addMusic(1, "Box Clever", "assets://Qupai/music/Box Clever");
            qupaiService.addMusic(2, "Byebye love", "assets://Qupai/music/Byebye love");
            qupaiService.addMusic(3, "Early days", "assets://Qupai/music/Early days");
            qupaiService.addMusic(4, "Faraway", "assets://Qupai/music/Faraway");
            qupaiService.addMusic(5, "High high", "assets://Qupai/music/High high");
            qupaiService.addMusic(6, "Missing You", "assets://Qupai/music/Missing You");
            qupaiService.addMusic(7, "Queen", "assets://Qupai/music/Queen");
            qupaiService.addMusic(8, "String", "assets://Qupai/music/String");
            qupaiService.addMusic(9, "Teenage dream", "assets://Qupai/music/Teenage dream");
            qupaiService.addMusic(10, "Theory", "assets://Qupai/music/Theory");
        }
    }

    public void init() {
        Utils.init(mContext);
        PingppLog.DEBUG = true;//支付SDK日志调试开关
        SDKInitializer.initialize(mContext);
        TrackingUtil.init(mContext);
        CourseUtil.init(mContext);
        new InitTask().executeLong();
        new InitEmTask().execute();
        initAliSDK();
        if (getApplicationInfo().packageName.equals(getCurProcessName(mContext))) {
            LeCloudProxy.init(getApplicationContext());
            LeCloudPlayerConfig.getInstance().setDeveloperMode(true).setIsApp();
        }
    }

    class InitEmTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... params) {
            String processAppName = getCurProcessName(mContext);
            if (processAppName == null || !processAppName.equalsIgnoreCase(mContext.getPackageName())) {
                Log.e(TAG, "enter the service process!");
                return null;
            }
            EMOptions options = new EMOptions();
            // 默认添加好友时，是不需要验证的，改成需要验证
            options.setAcceptInvitationAlways(false);
            // 初始化
            EMClient.getInstance().init(mContext, options);
            // 在做打包混淆时，关闭debug模式，避免消耗不必要的资源
            EMClient.getInstance().setDebugMode(false);
            return null;
        }
    }
    
    class InitTask extends MsTask{

        public InitTask() {
            super(mContext, (MsRequest) null);
        }
        
        @Override
        protected MsResponse doInBackground(Void... params) {
            MsResponse response = new MsResponse();
            TuSdk.init(mContext, sTuSdkKey);
            // it should be false while in release version
            TuSdk.enableDebugLog(false);
            return response;
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mPackageInfo = Utils.getPackageInfo(base);
        Log.d("loadDex", "App attachBaseContext ");
        if (!quickStart() && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {// >=5.0的系统默认对dex进行oat优化
            if (needWait(base)) {
                waitForDexopt(base);
            }
            MultiDex.install(this);
        } else {
            return;
        }
    }

    public static Context getAppContext() {
        return mContext;
    }

    public boolean quickStart() {
        if (TextUtils.isEmpty(mCurentProcessName)) {
            mCurentProcessName = getCurProcessName(this);
        }
        if (StringUtils.contains(mCurentProcessName, ":mini")) {
            Log.d("loadDex", ":mini start!");
            return true;
        }
        return false;
    }

    // neead wait for dexopt ?
    private boolean needWait(Context context) {
        String dexFlag = get2thDexSHA1(context);
        Log.d("loadDex", "dex2-sha1 " + dexFlag);
        SharedPreferences sp = context.getSharedPreferences(
                PackageUtil.getPackageInfo(context).versionName,
                MODE_MULTI_PROCESS);
        String saveValue = sp.getString(KEY_DEX2_SHA1, "");
        return !StringUtils.equals(dexFlag, saveValue);
    }

    /**
     * Get classes.dex file signature
     *
     * @param context
     * @return
     */
    private String get2thDexSHA1(Context context) {
        ApplicationInfo ai = context.getApplicationInfo();
        String source = ai.sourceDir;
        try {
            JarFile jar = new JarFile(source);
            Manifest mf = jar.getManifest();
            Map<String, Attributes> map = mf.getEntries();
            Attributes a = map.get("classes2.dex");
            return a.getValue("SHA1-Digest");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // optDex finish
    public void installFinish(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                PackageUtil.getPackageInfo(context).versionName,
                MODE_MULTI_PROCESS);
        sp.edit().putString(KEY_DEX2_SHA1, get2thDexSHA1(context)).commit();
    }

    public static String getCurProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public void waitForDexopt(Context base) {
        Log.d("loadDex", "do waitForDexopt 1 " + LoadResActivity.class.getName());
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName("com.tjut.mianliao",
                LoadResActivity.class.getName());
        intent.setComponent(componentName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        base.startActivity(intent);
        Log.d("loadDex", " start activity " + LoadResActivity.class.getName() + "--" + base.getApplicationContext());
        while (needWait(base)) {
            Log.d("loadDex", "do waitForDexopt 4");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class PackageUtil {
    public static PackageInfo getPackageInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(e.getLocalizedMessage());
        }
        return new PackageInfo();
    }

}