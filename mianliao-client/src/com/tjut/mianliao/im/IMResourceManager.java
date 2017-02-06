package com.tjut.mianliao.im;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.cocos2dx.lib.Cocos2dxHelper;

import android.content.Context;
import android.text.TextUtils;
import android.util.SparseArray;

import com.tjut.mianliao.R;
import com.tjut.mianliao.cocos2dx.CocosAvatarView;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.explore.EmotionsInfo;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.ScreenShotTool;
import com.tjut.mianliao.util.Utils;

public class IMResourceManager {

    static {
        System.loadLibrary("mianliao_avatar");
    }

    private static WeakReference<IMResourceManager> sInstanceRef;

    private static final String PLIST_VERSION = "cocos/nv6/publishArmatureActions.plist";
    private static final String PLIST_ACTIONS = "cocos/nv6/publishActionText.plist";
    private static final String PLIST_SUIT_INFO = "cocos/clothes/%s/suit_info.plist";
    private static final String PATH_SUIT_INFO = "cocos/clothes/%s/";
    private static final String SUIT_UNZIP_PATH = "%s/cocos/clothes";

    public static final int OPT_ADD = 1;
    public static final int OPT_USE = 2;
    public static final int OPT_UNUSE = 3;

    private static final String SUIT_FEMALE = "suit_female";
    private static final String SUIT_MALE = "suit_male";

    private Context mContext;
    private AccountInfo mMyAccount;
    private UserInfoManager mUserInfoManager;
    private int mAvatarVersion;
    private String mAvatarResPath;
    private boolean mFetchingAction;
    private boolean mChangingSuit;

    private ArrayList<IMResourceListener> mListeners;
    private HashMap<String, String> mAvatarActions, mAvatarSuitPaths;
    private HashMap<String, HashMap<String, String>> mAvatarSuits;
    private SparseArray<ArrayList<IMResource>> mUsingResources;

    public static synchronized IMResourceManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            IMResourceManager manager = sInstanceRef.get();
            return manager;
        }
        IMResourceManager instance = new IMResourceManager(context);
        sInstanceRef = new WeakReference<IMResourceManager>(instance);
        return instance;
    }

    private IMResourceManager(Context context) {
        Cocos2dxHelper.init(context);
        mContext = context.getApplicationContext();
        mMyAccount = AccountInfo.getInstance(context);
        mUserInfoManager = UserInfoManager.getInstance(context);

        mAvatarResPath = Cocos2dxHelper.getCocos2dxWritablePath();
        mListeners = new ArrayList<IMResourceListener>();
        mUsingResources = new SparseArray<ArrayList<IMResource>>();
        mAvatarActions = new HashMap<String, String>();
        mAvatarSuitPaths = new HashMap<String, String>();
        mAvatarSuits = new HashMap<String, HashMap<String, String>>();
        updateAvatarAction(true);
        loadAvatarActions(true);
        loadAvatarSuit(SUIT_FEMALE, true);
        loadAvatarSuit(SUIT_MALE, true);
    }

    public void clear() {
        CocosAvatarView.setOnAvatarLoadedListener(null);
        mListeners.clear();
        mUsingResources.clear();
        mAvatarActions.clear();
        sInstanceRef.clear();
    }

    public void registerIMResourceListener(IMResourceListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void unregisterIMResourceListener(IMResourceListener listener) {
        mListeners.remove(listener);
    }

    public void getImResource(int type, int limit, boolean showChild) {
        new GetImResourceTask(type, limit, showChild).executeLong();
    }

    public void UnzipFile(IMResource resource) {
        new UnzipFile(resource).executeLong();
    }

    public void getImResourceById(int type, int resId) {
        new GetResourceByIdTask(type, resId).executeLong();
    }

    public void GetMyImResources(int type) {
        new GetMyImResourceTask(type).executeLong();
    }

    public void GetMyUsingResource(int type, int userId) {
        new GetMyUsingResourceTask(type, userId).executeLong();
    }

    public void UseImResource(int id) {
        new UseImResourceTask(id).executeLong();// use 
    }

    public void UnseImResource(int id) {
        new UnuseImResourceTask(id).executeLong();// 正在用的
    }

    public void AddImResource(int id) {
        new AddImResourceask(id).executeLong();
    }

    public void showAvatar(int size, boolean mineOnly) {
        if (mineOnly) {

            int height = ScreenShotTool.getAppContentHeight(mContext);
            int otherHeight = ScreenShotTool.getAppOtherHeight(mContext);

            int titleBarHeight = (int) mContext.getResources().getDimension(R.dimen.title_bar_height_with_shadow);

            CocosAvatarView.showMyAvatar(height - size - otherHeight - titleBarHeight, size);
        } else {
            CocosAvatarView.addLeftAvatar();
            CocosAvatarView.locateAvatars(0, size);
        }
    }

    public void changeSuit(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        int userId = userInfo.userId;
        IMResource res = getUsingResource(IMResource.TYPE_CHARACTER_ACCESSORY, userInfo.userId);
        changeSuit(userId, res);
    }

    public void changeSuit(int userId, IMResource res) {
        if (res == null) {
            changeGenderSuit(mUserInfoManager.getUserInfo(userId));
        } else {
            changeSuit(userId == mMyAccount.getUserId() ? 1 : 0, res.sn, res.urls[0][0]);
        }
    }

    public void changeSuit(int side, String suitName, String url) {
        HashMap<String, String> suit = mAvatarSuits.get(suitName);
        if (suit != null) {
            CocosAvatarView.changeSuit(side, suit, mAvatarSuitPaths.get(suitName));
        } else if (!mChangingSuit) {
            new ChangeSuitTask(side, suitName, url).executeLong();
        }
    }

    public void playArmature(ChatRecord record) {
        if (record == null || TextUtils.isEmpty(record.text)) {
            return;
        }
        if (Utils.AVATAR_PLAY_PATTERN.matcher(record.text).matches()) {
            String key = record.text.substring(1, record.text.length() - 1);
            String name = getAvatarActionName(key);
            if (!TextUtils.isEmpty(name)) {
                int side = record.isFrom(mMyAccount.getAccount()) ? 1 : 0;
                CocosAvatarView.playArmatureByName(side, name);
            }
        }
    }

    public void playArmature(int side, String name) {
        if (!TextUtils.isEmpty(name)) {
            CocosAvatarView.playArmatureByName(side, name);
        }
    }

    public void stopArmature(int side) {
        CocosAvatarView.stopAction(side);
    }

    public void updateAvatarAction(boolean isFromAsset) {
        int version = getAvatarVersion(isFromAsset);
        if (version > mAvatarVersion && loadAvatarActions(isFromAsset)) {
            mAvatarVersion = version;
            fetchActionRes();
        }
    }

    public void onAvatarLoaded() {
        CocosAvatarView.addLeftAvatar();

    }

    public String getAvatarActionName(String key) {
        return mAvatarActions.get(key);
    }

    public String getAvatarActionKey(String name) {
        for (Entry<String, String> entry : mAvatarActions.entrySet()) {
            if (TextUtils.equals(name, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public IMResource getUsingResource(int type, int userId) {
        return getUsingResource(type, mUsingResources.get(userId));
    }

    public void setUsingRes(int id, int type, int optType) {
        new SetUsingResTask(id, type, optType).executeLong();
    }

    public void fetchUsingRes(int userId) {
        new FetchUsingResTask(userId).executeLong();
    }

    private void fetchActionRes() {
        if (!mFetchingAction) {
            new FetchActionResTask(mAvatarVersion).executeLong();
        }
    }

    private void changeGenderSuit(UserInfo userInfo) {
        if (userInfo == null) {
            return;
        }
        int side;
        if (userInfo.userId == mMyAccount.getUserId()) {
            side = 1;
            CocosAvatarView.removeRightAvatar();
            CocosAvatarView.addRightAvatar();
        } else {
            side = 0;
            CocosAvatarView.removeLeftAvatar();
            CocosAvatarView.addLeftAvatar();
        }
        String suitName = UserInfo.isFemale(userInfo.gender) ? SUIT_FEMALE : SUIT_MALE;
        CocosAvatarView.changeSuit(side, mAvatarSuits.get(suitName), mAvatarSuitPaths.get(suitName));
    }

    public int getAvatarVersion(boolean isFromAsset) {
        int version = 0;
        InputStream is = null;
        try {
            is = isFromAsset ? mContext.getAssets().open(PLIST_VERSION) : new FileInputStream(new File(mAvatarResPath,
                    PLIST_VERSION));
            HashMap<String, String> dict = Utils.getDictFromPlist(is);
            if (dict != null) {
                version = Integer.parseInt(dict.get("verNo"));
            }
        } catch (NumberFormatException e) {
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return version;
    }

    private boolean loadAvatarActions(boolean isFromAsset) {
        InputStream is = null;
        try {
            is = isFromAsset ? mContext.getAssets().open(PLIST_ACTIONS) : new FileInputStream(new File(mAvatarResPath,
                    PLIST_ACTIONS));
            HashMap<String, String> actions = Utils.getDictFromPlist(is);
            if (actions != null) {
                mAvatarActions.clear();
                mAvatarActions.putAll(actions);
                if (!isFromAsset) {
                    CocosAvatarView.loadArmatureData(true);
                }
                return true;
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    private boolean loadAvatarSuit(String suitName, boolean isFromAsset) {
        if (TextUtils.isEmpty(suitName)) {
            return false;
        }
        String fileName = String.format(PLIST_SUIT_INFO, suitName);
        String filePath = String.format(PATH_SUIT_INFO, suitName);
        InputStream is = null;
        try {
            is = isFromAsset ? mContext.getAssets().open(fileName) : new FileInputStream(new File(mAvatarResPath,
                    fileName));
            if (isFromAsset) {
                mAvatarSuitPaths.put(suitName, filePath + suitName + ".atlas");
            } else {
                mAvatarSuitPaths.put(suitName, mAvatarResPath + filePath + suitName + ".atlas");
            }

            HashMap<String, String> info = Utils.getDictFromPlist(is);
            if (info != null) {
                // CocosAvatarView.loadClothesData(suitName, !isFromAsset);
                mAvatarSuits.put(suitName, info);
                return true;
            }
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }

    private IMResource getUsingResource(int type, ArrayList<IMResource> resList) {
        if (resList != null) {
            for (IMResource res : resList) {
                if (res.type == type) {
                    return res;
                }
            }
        }
        return null;
    }

    private void checkUsingResUpdate(int userId, int type, IMResource oldRes, ArrayList<IMResource> newList) {
        IMResource newRes = getUsingResource(type, newList);
        checkUsingResUpdate(userId, type, oldRes, newRes);
    }

    private void checkUsingResUpdate(int userId, int type, IMResource oldRes, IMResource newRes) {
        if (!IMResource.equals(oldRes, newRes)) {
            for (IMResourceListener listener : mListeners) {
                listener.onUsingResUpdated(userId, type, newRes);
            }
        }
    }

    private String getZipfilePath(String url) {
        StringBuilder sb = new StringBuilder(mAvatarResPath).append(File.separator);
        int index = url.lastIndexOf(File.separator);
        if (index != -1) {
            sb.append(url.substring(index + 1));
        } else {
            sb.append(url);
        }
        return sb.toString();
    }

    private class SetUsingResTask extends MsTask {
        private int mId;
        private int mType;

        public SetUsingResTask(int id, int type, int optType) {
            super(mContext, optType == OPT_ADD ? MsRequest.IMUR_ADD : optType == OPT_USE ? MsRequest.IMUR_USE
                    : MsRequest.IMUR_UNUSE);
            mId = id;
            mType = type;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("id=").append(mId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                int userId = mMyAccount.getUserId();
                IMResource oldRes = getUsingResource(mType, userId);
                IMResource newRes = IMResource.fromJson(response.getJsonObject());

                ArrayList<IMResource> resList = mUsingResources.get(userId);
                if (resList == null) {
                    resList = new ArrayList<IMResource>();
                    mUsingResources.put(userId, resList);
                } else {
                    resList.remove(oldRes);
                }
                if (newRes != null) {
                    resList.add(newRes);
                }

                checkUsingResUpdate(userId, mType, oldRes, newRes);
            } else {
                response.showFailInfo(getRefContext(), R.string.cht_ext_res_set_failed);
            }
        }
    }

    private class FetchUsingResTask extends MsTask {
        private int mUserId;

        public FetchUsingResTask(int userId) {
            super(mContext, MsRequest.IMUR_FIND_USER_USING);
            mUserId = userId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("user_id=").append(mUserId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                IMResource oldResBg = getUsingResource(IMResource.TYPE_BACKGROUND, mUserId);
                IMResource oldResBubble = getUsingResource(IMResource.TYPE_BUBBLE, mUserId);
                IMResource oldResSuit = getUsingResource(IMResource.TYPE_CHARACTER_ACCESSORY, mUserId);

                ArrayList<IMResource> newList = JsonUtil.getArray(response.getJsonArray(), IMResource.TRANSFORMER);
                mUsingResources.put(mUserId, newList);

                checkUsingResUpdate(mUserId, IMResource.TYPE_BACKGROUND, oldResBg, newList);
                checkUsingResUpdate(mUserId, IMResource.TYPE_BUBBLE, oldResBubble, newList);
                checkUsingResUpdate(mUserId, IMResource.TYPE_CHARACTER_ACCESSORY, oldResSuit, newList);
            } else {
                // response.showFailInfo(getRefContext(),
                // R.string.cht_ext_res_get_failed);
            }
        }
    }

    private class FetchActionResTask extends MsTask {
        private int mVersion;

        public FetchActionResTask(int version) {
            super(mContext, MsRequest.IMUR_NEWEST);
            mVersion = version;
        }

        @Override
        protected void onPreExecute() {
            mFetchingAction = true;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("type=").append(IMResource.TYPE_CHARACTER_ACTION).append("&ver=").append(mVersion)
                    .toString();
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            MsResponse response = super.doInBackground(params);
            if (!response.isSuccessful()) {
                return response;
            }

            ArrayList<IMResource> resList = JsonUtil.getArray(response.getJsonArray(), IMResource.TRANSFORMER);
            if (resList.isEmpty()) {
                return null;
            }

            String url = resList.get(0).urls[0][0];
            String zipPath = getZipfilePath(url);
            if (!HttpUtil.downLoad(mContext, url, zipPath) || !Utils.unzipFile(zipPath, mAvatarResPath)) {
                response.code = MsResponse.MS_FAILED;
            }

            return response;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mFetchingAction = false;
            if (response != null && !response.isSuccessful()) {
                // response.showFailInfo(getRefContext(),
                // R.string.cht_ext_res_get_failed);
            }
        }
    }

    private class ChangeSuitTask extends MsTask {
        private int mSide;
        private String mSuitName;
        private String mUrl;

        public ChangeSuitTask(int side, String suitName, String url) {
            super(mContext, (MsRequest) null);
            mSide = side;
            mSuitName = suitName;
            mUrl = url;
        }

        @Override
        protected void onPreExecute() {
            mChangingSuit = true;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            MsResponse response = new MsResponse();
            response.code = MsResponse.MS_SUCCESS;

            String fileName = String.format(PLIST_SUIT_INFO, mSuitName);
            if (new File(mAvatarResPath, fileName).exists()) {
                return response;
            }

            String zipPath = getZipfilePath(mUrl);
            boolean success = true;
            if (!new File(zipPath).exists()) {
                success = HttpUtil.downLoad(mContext, mUrl, zipPath);
            }
            if (success) {
                String destPath = String.format(SUIT_UNZIP_PATH, mAvatarResPath);
                success = Utils.unzipFile(zipPath, destPath);
            }

            if (!success) {
                response.code = MsResponse.MS_FAILED;
            }
            return response;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mChangingSuit = false;
            if (response.isSuccessful()) {
                if (loadAvatarSuit(mSuitName, false)) {
                    CocosAvatarView.changeSuit(mSide, mAvatarSuits.get(mSuitName), mAvatarSuitPaths.get(mSuitName));
                }
            } else {
                // response.showFailInfo(getRefContext(),
                // R.string.cht_ext_res_get_failed);
            }
        }
    }

    private class GetImResourceTask extends MsTask {

        private int mType, mLimit;
        private boolean mShowChild;

        public GetImResourceTask(int type, int limit, boolean showChild) {
            super(mContext, MsRequest.IMUR_LIST);
            mType = type;
            mLimit = limit;
            mShowChild = showChild;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("type=").append(mType).append("&user_id=").append(mMyAccount.getUserId())
                    .append("&children=").append(mShowChild).append("&limit=").append(mLimit).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<IMResource> mResources = JsonUtil.getArray(response.getJsonArray(), IMResource.TRANSFORMER);
                for (IMResourceListener listener : mListeners) {
                    listener.onGetImResourceSuccess(mType, 0, mResources);
                }
            }
        };
    }

    private class UnzipFile extends MsTask {

        private IMResource imResource;

        public UnzipFile(IMResource resource) {
            super(mContext, (MsRequest) null);
            imResource = resource;
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            MsResponse response = new MsResponse();
            response.code = MsResponse.MS_SUCCESS;
            if (imResource.urls.length <= 0 || imResource.urls[0].length <= 0) {
                response.code = MsResponse.MS_FAILED;
                return response;
            }

            String url = imResource.urls[0][0];
            if (!isZipFile(url)) {
                response.code = MsResponse.MS_FAILED;
                return response;
            }
            String eleUnzipPath = Utils.getElementUnzipfilePath(url);
            String zipPath = getZipfilePath(url);
            boolean success = true;
            if (!new File(zipPath).exists()) {
                success = HttpUtil.downLoad(mContext, url, zipPath);
            }
            if (success) {
                success = Utils.unzipFile(zipPath, eleUnzipPath);
            }

            if (!success) {
                response.code = MsResponse.MS_FAILED;
            } else {
                response.code = MsResponse.MS_SUCCESS;
            }

            // save data to database
            EmotionsInfo emotionInfo = new EmotionsInfo(imResource.name, url, zipPath, eleUnzipPath, imResource.use);
            if (DataHelper.loadEmotionInfo(mContext, imResource.name) == null) {
                DataHelper.insertEmotionInfo(mContext, emotionInfo);
            } else {
                DataHelper.updateEmotionInfo(mContext, emotionInfo);
            }
            return response;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                for (IMResourceListener listener : mListeners) {
                    listener.onUnzipSuccess();
                }
            }
        }

    }

    private class GetResourceByIdTask extends MsTask {

        private int mResId;
        private int mType;

        public GetResourceByIdTask(int type, int resId) {
            super(mContext, MsRequest.IMUR_FIND_BY_ID);
            mResId = resId;
            mType = type;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("id=").append(mResId).append("&user_id=").append(mMyAccount.getUserId())
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                IMResource resource = IMResource.fromJson(response.getJsonObject());
                ArrayList<IMResource> resources = new ArrayList<>();
                resources.add(resource);
                for (IMResourceListener listener : mListeners) {
                    listener.onGetImResourceSuccess(mType, 0, resources);
                }
            }
        }
    }

    private class GetMyImResourceTask extends MsTask {

        private int mType;

        public GetMyImResourceTask(int type) {
            super(mContext, MsRequest.IMUR_LIST_MY);
            mType = type;
        }

        @Override
        protected String buildParams() {
            return "type=" + mType;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<IMResource> resources = JsonUtil.getArray(response.getJsonArray(), IMResource.TRANSFORMER);
                for (IMResourceListener listener : mListeners) {
                    listener.onGetImResourceSuccess(mType, 0, resources);
                }
            }
        }
    }

    private class GetMyUsingResourceTask extends MsTask {

        private int mType, mUserId;

        public GetMyUsingResourceTask(int type, int userId) {
            super(mContext, MsRequest.IMUR_FIND_USER_USING);
            mType = type;
            mUserId = userId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("type=").append(mType).append("&user_id=").append(mUserId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<IMResource> resources = JsonUtil.getArray(response.getJsonArray(), IMResource.TRANSFORMER);
                for (IMResourceListener listener : mListeners) {
                    listener.onGetImResourceSuccess(mType, mType, resources);
                }
            }
        }

    }

    private class UseImResourceTask extends MsTask {

        private int mId;

        public UseImResourceTask(int id) {
            super(mContext, MsRequest.IMUR_USE);
            mId = id;
        }

        @Override
        protected String buildParams() {
            return "id=" + mId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                IMResource res = IMResource.fromJson(response.getJsonObject());
                for (IMResourceListener listener : mListeners) {
                    listener.onUseResSuccess(res);// on user -----------------------
                }
            }
        }
    }

    private class UnuseImResourceTask extends MsTask {

        private int mId;

        public UnuseImResourceTask(int id) {
            super(mContext, MsRequest.IMUR_UNUSE);
            mId = id;
        }

        @Override
        protected String buildParams() {
            return "id=" + mId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                for (IMResourceListener listener : mListeners) {
                    listener.onUnuseResSuccess();// nut use --------------------------
                }
            }
        }
    }

    private class AddImResourceask extends MsTask {

        private int mId;

        public AddImResourceask(int id) {
            super(mContext, MsRequest.IMUR_ADD);
            mId = id;
        }

        @Override
        protected String buildParams() {
            return "id=" + mId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                for (IMResourceListener listener : mListeners) {
                    listener.onAddResSuccess();
                }
            } else {
                for (IMResourceListener listener : mListeners) {
                    listener.onAddResFail(response.code);
                }
            }
        }

    }

    private boolean isZipFile(String url) {
        int index = url.lastIndexOf(".");
        if (index != -1) {
            String fileTpe = url.substring(index + 1).toUpperCase();
            if ("ZIP".equals(fileTpe)) {
                return true;
            }
        }
        return false;
    }

    public interface IMResourceListener {
        public void onUsingResUpdated(int userId, int type, IMResource res);

        public void onGetImResourceSuccess(int type, int requestCode, ArrayList<IMResource> mResources);

        public void onUnzipSuccess();

        public void onUseResSuccess(IMResource res);

        public void onUnuseResSuccess();

        public void onAddResSuccess();

        public void onAddResFail(int code);
    }
}
