package com.tjut.mianliao.data;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.Utils;

/**
 * It's used to save generic account info.
 */
public class AccountInfo {
    private static final String TAG = "AccountInfo";

    public static final String TOKEN = "token";

    private static final String SHARED_PREFS_NAME = "account_info";
    private static final String SP_ACCOUNT = "account";
    private static final String SP_USER_ID = "user_id";
    private static final String SP_TOKEN = TOKEN;
    private static final String SP_LOGGED_IN = "logged_in";
    public static final String SP_USER_INFO = "user_info";

    private static WeakReference<AccountInfo> sInstanceRef;

    private Context mContext;
    private SharedPreferences mSharedPrefs;
    private SharedPreferences mPreferences;

    private String mAccount;
    private int mUserId;
    private String mToken;
    private boolean mLoggedIn;

    private Map<String, CharSequence> mChatDrafts;

    public static synchronized AccountInfo getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        AccountInfo instance = new AccountInfo(context);
        sInstanceRef = new WeakReference<AccountInfo>(instance);
        return instance;
    }

    private AccountInfo(Context context) {
        mContext = context.getApplicationContext();
        mSharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, 0);
        mPreferences = DataHelper.getSpForData(context);
        mChatDrafts = new HashMap<String, CharSequence>();
        loadInfo();
    }

    /**
     * Mark a user as logged in, and save account/profile info.
     */
    public void login(String account, String token, int userId) {
        mAccount = account;
        mToken = token;
        mUserId = userId;
        mLoggedIn = true;
        saveInfo();
    }

    public void logout() {
        mToken = null;
        mLoggedIn = false;
        mChatDrafts.clear();
        saveInfo();
    }

    public String getAccount() {
        return mAccount;
    }

    public String getToken() {
        return mToken;
    }

    public int getUserId() {
        return mUserId;
    }

    public UserInfo getUserInfo() {
        return UserInfoManager.getInstance(mContext).getUserInfo(mUserId);
    }

    public boolean isLoggedIn() {
        return mLoggedIn;
    }

    public boolean isUserChanged(UserInfo info) {
        if (mUserId == 0 || TextUtils.isEmpty(mAccount)) {
            return false;
        }
        return mUserId != info.userId
                || !TextUtils.equals(mAccount.toLowerCase(), info.account.toLowerCase());
    }

    public CharSequence loadChatDraft(String target) {
        return mChatDrafts.get(target);
    }

    public void saveChatDraft(String target, CharSequence draft) {
        mChatDrafts.put(target, draft);
    }

    /**获取是否登录boolean**/
    private void loadInfo() {
        mAccount = mSharedPrefs.getString(SP_ACCOUNT, null);
        mUserId = mSharedPrefs.getInt(SP_USER_ID, 0);
        mToken = mSharedPrefs.getString(SP_TOKEN, null);
        mLoggedIn = mSharedPrefs.getBoolean(SP_LOGGED_IN, false);

        UserInfoManager uim = UserInfoManager.getInstance(mContext);
        UserInfo info = uim.getUserInfo(mUserId);
        if (info == null) {
            String userInfo = mPreferences.getString(SP_USER_INFO, "");
            if (!TextUtils.isEmpty(userInfo)) {
                try {
                    uim.addUserInfo(UserInfo.fromJson(new JSONObject(userInfo)));
                } catch (JSONException e) {
                    Utils.logD(TAG, e.getMessage());
                }
            }
        }
    }

    public UserInfo loadUserInfoFromSp() {
        String userInfo = mPreferences.getString(SP_USER_INFO, "");
        if (!TextUtils.isEmpty(userInfo)) {
            try {
                return UserInfo.fromJson(new JSONObject(userInfo));
            } catch (JSONException e) {
                Utils.logD(TAG, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 保存登录获取的用户密码，以及token值，是否登录
     **/
    private void saveInfo() {
        mSharedPrefs.edit().putString(SP_ACCOUNT, mAccount)
                .putInt(SP_USER_ID, mUserId)
                .putString(SP_TOKEN, mToken)
                .putBoolean(SP_LOGGED_IN, mLoggedIn)
                .commit();
    }
}
