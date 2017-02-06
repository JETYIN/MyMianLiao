package com.tjut.mianliao.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.data.Medal;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.UserExtInfo;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.login.UserExtLoginManager;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMShareBoardListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.media.BaseShareContent;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.RenrenShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.RenrenSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class SnsHelper implements UMShareBoardListener, SnsPostListener {
    private static final String TAG = "SnsHelper";

    private static final String WX_APP_ID = "wx3a234775698dfd6a";
    private static final String WX_APP_SECRET = "4e75c4e398bf434812229c34c4941b52";
    private static final String QQ_APP_ID = "101070455";
    private static final String QQ_APP_KEY = "6d7dd73667a9a8a815028c0316bf411d";
    private static final String RR_APP_ID = "267330";
    private static final String RR_APP_KEY = "2ab3a5a1abf74de4a007ff364c51b761";
    private static final String RR_APP_SECRET = "6b61813fdd8347c69243dc4d312fbae0";

    private static final String DEFAULT_IMG_URL = "http://image.tjut.cc/assets/images/logo/logo_v4.png";
    private static final String DEFAULT_TARGET_URL = Utils.getShareServerAddress() + "webapp/share/share.html";

    private static final int SHARE_TYPE_NEWS = 1;
    private static final int SHARE_TYPE_POST = 2;
    private static final int SHARE_TYPE_MEDAL = 3;
    private static final int SHARE_TYPE_JOB = 4;

    private static WeakReference<SnsHelper> sInstanceRef;

    private Activity mPreviousActivity;
    private UMSocialService mShareController;
    private BaseShareContent[] mShareContents;
    private boolean mShareBoardShown;

    private int mIncShareType;
    private int mIncShareId;
    private String mJsonString = "";
    private ArrayList<IncShareTask> mIncShareTasks;

    private UMSocialService mLoginController;
    private UserExtInfo mUserExtInfo;
    private UserExtLoginManager mUserExtLoginManager;
    
    private boolean mIsSinaAuthed;

    public static synchronized SnsHelper getInstance() {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        SnsHelper instance = new SnsHelper();
        sInstanceRef = new WeakReference<SnsHelper>(instance);
        return instance;
    }

    private SnsHelper() {
        com.umeng.socialize.utils.Log.LOG = Utils.isDebug();
        mLoginController = UMServiceFactory.getUMSocialService("com.umeng.login");
        mShareController = UMServiceFactory.getUMSocialService("com.umeng.share");
        mShareController.setShareBoardListener(this);
        mShareController.getConfig().setDefaultShareLocation(false);
        mShareController.getConfig().setPlatforms(
                SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.WEIXIN,
                SHARE_MEDIA.QZONE,
                SHARE_MEDIA.QQ,
                SHARE_MEDIA.SINA,
                SHARE_MEDIA.RENREN);
        mShareController.getConfig().setPlatformOrder(
                SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.WEIXIN,
                SHARE_MEDIA.QZONE,
                SHARE_MEDIA.QQ,
                SHARE_MEDIA.SINA,
                SHARE_MEDIA.RENREN);
        mShareContents = new BaseShareContent[] {
                new CircleShareContent(),
                new WeiXinShareContent(),
                new QZoneShareContent(),
                new QQShareContent(),
                new SinaShareContent(),
                new RenrenShareContent()
        };
        mIncShareTasks = new ArrayList<IncShareTask>();

        mUserExtInfo = new UserExtInfo();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mShareController != null) {
            UMSsoHandler ssoHandler = mShareController.getConfig().getSsoHandler(requestCode);
            if (ssoHandler != null) {
                ssoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        }
        if (mLoginController != null) {
            UMSsoHandler ssoHandler = mLoginController.getConfig().getSsoHandler(requestCode);
            if (ssoHandler != null) {
                ssoHandler.authorizeCallBack(requestCode, resultCode, data);
            }
        }
    }

    public void openShareBoard(Activity activity, News news) {
        mIncShareType = SHARE_TYPE_NEWS;
        mIncShareId = news.id;
        String content = activity.getString(R.string.sns_share_common, news.title);
        prepareShare(activity, content, news.publicUrl, news.cover);
        mShareController.openShare(activity, this);
    }

    public void openShareBoard(Activity activity, CfPost post) {
        mIncShareType = SHARE_TYPE_POST;
        mIncShareId = post.postId;
        String targetUrl = DEFAULT_TARGET_URL + "?thread_id=" + post.postId;
        String content = activity.getString(R.string.sns_share_common, post.content);
        prepareShare(activity, content, targetUrl, post.image);
        mShareController.openShare(activity, this);
    }

    public void openShareBoard(Activity activity, Medal medal) {
        mIncShareType = SHARE_TYPE_MEDAL;
        mIncShareId = medal.id;
        String content = activity.getString(R.string.sns_share_medal, medal.name);
        prepareShare(activity, content, null, medal.imageUrl);
        mShareController.openShare(activity, this);
    }

    public void openShareBoard(Activity activity, Job job) {
        mIncShareType = SHARE_TYPE_JOB;
        mIncShareId = job.id;
        String content = activity.getString(R.string.sns_share_job, job.title);
        prepareShare(activity, content, null, job.corpLogo);
        mShareController.openShare(activity, this);
    }

    public void openShareBoard(Activity activity, LiveInfo liveInfo, SHARE_MEDIA  media, String json) {
        mIncShareType = SHARE_TYPE_JOB;
        mIncShareId = liveInfo.id;
        mJsonString = json;
        String content = activity.getString(R.string.sns_share_job, liveInfo.title);
        prepareShare(activity, content, json, liveInfo.prevUrl);
        mShareController.directShare(activity, media, this);
    }

    public void openShareBoard(Activity activity, LiveInfo liveInfo, String json) {
        mIncShareType = SHARE_TYPE_JOB;
        mIncShareId = liveInfo.id;
        mJsonString = json;
        String content = activity.getString(R.string.sns_share_job, liveInfo.title);
        prepareShare(activity, content, json, liveInfo.prevUrl);
        mShareController.openShare(activity, this);
    }


    public void closeShareBoard() {
        if (isShareBoardShown()) {
            mShareController.dismissShareBoard();
        }
    }

    public boolean isShareBoardShown() {
        return mShareBoardShown;
    }

    public void clear() {
        closeShareBoard();
        mPreviousActivity = null;
        mShareController = null;
        mShareContents = null;
        sInstanceRef.clear();
    }

    private void prepareShare(Activity activity,
            String content, String targetUrl, String imgUrl) {
        if (mPreviousActivity != activity) {
            mPreviousActivity = activity;
            configHandlers(activity);
        }

        if (TextUtils.isEmpty(targetUrl)) {
            targetUrl = DEFAULT_TARGET_URL;
        }
        if (TextUtils.isEmpty(imgUrl)) {
            imgUrl = DEFAULT_IMG_URL;
        }
        UMImage image = new UMImage(activity, imgUrl);

        for (BaseShareContent shareContent: mShareContents) {
            if (shareContent instanceof SinaShareContent
                    || shareContent instanceof TencentWbShareContent) {
                shareContent.setShareContent(content + " " + targetUrl);
            } else {
                shareContent.setShareContent(content);
            }
            shareContent.setShareImage(image);
            shareContent.setTargetUrl(targetUrl);
            shareContent.setTitle(content);
            if (shareContent instanceof RenrenShareContent) {
                shareContent.setAppWebSite(targetUrl);
            }
            mShareController.setShareMedia(shareContent);
        }
    }

    private void prepareLogin(Activity activity) {
        if (mPreviousActivity != activity) {
            mPreviousActivity = activity;
            configHandlers(activity);
        }
        mUserExtLoginManager = UserExtLoginManager.getInstance(activity);
        mUserExtLoginManager.setUserExtInfo(mUserExtInfo);
    }

    private void configHandlers(Activity activity) {
        // Weixin Circle
        UMWXHandler circleHandler = new UMWXHandler(activity, WX_APP_ID, WX_APP_SECRET);
        circleHandler.setToCircle(true);
        circleHandler.addToSocialSDK();
        // WeiXin
        UMWXHandler wxHandler = new UMWXHandler(activity, WX_APP_ID, WX_APP_SECRET);
        wxHandler.addToSocialSDK();
        // QZone
        QZoneSsoHandler qZoneHandler = new QZoneSsoHandler(activity, QQ_APP_ID, QQ_APP_KEY);
        qZoneHandler.addToSocialSDK();
        // QQ
        UMQQSsoHandler qqHandler = new UMQQSsoHandler(activity, QQ_APP_ID, QQ_APP_KEY);
        qqHandler.addToSocialSDK();
        RenrenSsoHandler renrenHandler = new RenrenSsoHandler(
                activity, RR_APP_ID, RR_APP_KEY, RR_APP_SECRET);
        if (mLoginController != null) {
            // Sina weibo
            mLoginController.getConfig().setSsoHandler(new SinaSsoHandler(activity));
            // RenRen
            mLoginController.getConfig().setSsoHandler(renrenHandler);
        }
        if (mShareController != null) {
            // Sina weibo
            mShareController.getConfig().setSsoHandler(new SinaSsoHandler(activity));
            // RenRen
            mShareController.getConfig().setSsoHandler(renrenHandler);
        }
    }

    @Override
    public void onShow() {
        mShareBoardShown = true;
    }

    @Override
    public void onDismiss() {
        mShareBoardShown = false;
    }

    @Override
    public void onStart() {
        mIncShareTasks.add(new IncShareTask(mIncShareType, mIncShareId, mJsonString));
    }

    @Override
    public void onComplete(SHARE_MEDIA platform, int eCode, SocializeEntity entity) {
        if (!mIncShareTasks.isEmpty()) {
            mIncShareTasks.remove(0).setParams(eCode, platform.toString()).executeLong();
        }
    }

    private class IncShareTask extends MsTask {
        private int mType;
        private int mId;
        private int mCode;
        private String mPlatform;
        private String mJson;

        public IncShareTask(int type, int id) {
            super(mPreviousActivity, MsRequest.INC_SHARE);
            mType = type;
            mId = id;
        }

        public IncShareTask(int type, int id, String json) {
            super(mPreviousActivity, MsRequest.INC_SHARE);
            mType = type;
            mId = id;
            mJson = json;
        }

        public IncShareTask setParams(int code, String platform) {
            mCode = code;
            mPlatform = platform;
            return this;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(mType)
                    .append("&oid=").append(mId)
                    .append("&code=").append(mCode)
                    .append("&dest=").append(mPlatform)
                    .append("&").append(mJson)
                    .toString();
        }
    }

    public void loginByExt(Activity activity, SHARE_MEDIA platform) {
        prepareLogin(activity);
        mLoginController.doOauthVerify(activity, platform, mUMAuthListener);
    }

    private void getPlateformInfo(SHARE_MEDIA platform) {
        mLoginController.getPlatformInfo(mPreviousActivity, platform, mUMDataListener);
    }

    private UMAuthListener mUMAuthListener = new UMAuthListener() {
        @Override
        public void onError(SocializeException e, SHARE_MEDIA platform) {
            Utils.logE(TAG, "授权错误");
            mUserExtInfo.clear();
            mUserExtLoginManager.setIsCancleLoginByExt(true);
        }

        @Override
        public void onComplete(Bundle value, SHARE_MEDIA platform) {
            if (value != null && !TextUtils.isEmpty(value.getString("uid"))) {
                if (mIsSinaAuthed && platform == SHARE_MEDIA.SINA) {
                    mIsSinaAuthed = false;
                    return;
                }
                mIsSinaAuthed = true;
                Utils.logD(TAG, "授权成功");
                mUserExtLoginManager.setIsCancleLoginByExt(false);

                for (String key : value.keySet()) {
                    Utils.logD(TAG, key + "=" + value.getString(key));
                }
                switch (platform) {
                    case SINA:
                        mUserExtInfo.type = UserExtInfo.TYPE_SINA;
                        break;
                    case QQ:
                        mUserExtInfo.type = UserExtInfo.TYPE_QQ;
                        break;
                    case RENREN:
                        mUserExtInfo.type = UserExtInfo.TYPE_RENREN;
                        break;

                    default:
                        break;
                }
                mUserExtInfo.extId = value.getString("uid");
                mUserExtInfo.token = value.getString("access_token");
                getPlateformInfo(platform);
            } else {
                Utils.logE(TAG, "授权失败");
                mUserExtLoginManager.setIsCancleLoginByExt(true);
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            Utils.logD(TAG, "取消授权");
            mUserExtInfo.clear();
            mUserExtLoginManager.setIsCancleLoginByExt(true);
        }

        @Override
        public void onStart(SHARE_MEDIA platform) {
            Utils.logD(TAG, "授权开始");
        }
    };

    private UMDataListener mUMDataListener = new UMDataListener() {
        @Override
        public void onStart() {
        }

        @Override
        public void onComplete(int status, Map<String, Object> info) {
            if (status == 200 && info != null) {
                Utils.logD(TAG, "UMData-->" + info);
                mUserExtInfo.nickName = info.get("screen_name").toString();
                String gender = info.get("gender").toString();
                if ("女".equals(gender)) {
                    mUserExtInfo.gender = 0;
                } else if ("男".equals(gender)) {
                    mUserExtInfo.gender = 1;
                } else if (gender != null && !"".equals(gender) && !"null".equals(gender)) {
                    mUserExtInfo.gender = Integer.parseInt(gender);
                } else {
                    mUserExtInfo.gender = 0;
                }
                if (info.containsKey("description")) {
                    mUserExtInfo.description = info.get("description").toString();
                } else {
                    mUserExtInfo.description = mPreviousActivity.getString(
                            R.string.prof_no_short_desc);
                }
                mUserExtInfo.setAvatar(mPreviousActivity,
                        info.get("profile_image_url").toString());
                if (mUserExtLoginManager.isLoginByExt()  &&
                        !mUserExtLoginManager.isTaskRunning()) {
                    mUserExtLoginManager.userExtLogin();
                } else {
                    mUserExtLoginManager.userExtBind();
                }
                mUserExtLoginManager.setIsCancleLoginByExt(false);
            } else {
                Utils.logD(TAG, "UMData error：" + status);
                mUserExtInfo.clear();
                mUserExtLoginManager.setIsCancleLoginByExt(true);
            }
        }
    };

}
