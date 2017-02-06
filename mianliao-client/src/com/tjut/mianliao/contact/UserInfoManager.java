package com.tjut.mianliao.contact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.content.Context;
import android.util.SparseArray;

import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.util.Utils;

/**
 * Manages contacts info. Contacts info is from MotherShip.
 */
public class UserInfoManager {

//    private static WeakReference<UserInfoManager> sInstanceRef;
    private static UserInfoManager mInstance;

    private Context mContext;
    private SparseArray<UserInfo> mUserInfoList;
    private HashMap<String, UserInfo> mUserMap;

    /**
     * Keep a weak reference to the manager because it's used widely across the
     * application. When there's already an instance of the manager, the next
     * caller don't need to create a new instance of it. And it also make sure
     * the different parts of the application gets the same data.
     */
    public static synchronized UserInfoManager getInstance(Context context) {
        if (mInstance != null) {
            return mInstance;
        }
        mInstance = new UserInfoManager(context);
        return mInstance;
    }

    private UserInfoManager(Context context) {
        mContext = context.getApplicationContext();
        loadUserInfo();
    }

    public void loadUserInfo() {
        if (mUserInfoList == null) {
            mUserInfoList = new SparseArray<UserInfo>();
        } else {
            mUserInfoList.clear();
        }
        if (mUserMap == null) {
            mUserMap = new HashMap<String, UserInfo>();
        } else {
            mUserMap.clear();
        }
        ArrayList<UserInfo> userInfos = DataHelper.loadUserInfos(mContext);
        for (UserInfo user : DataHelper.loadUserInfos(mContext)) {
            mUserInfoList.put(user.userId, user);
            mUserMap.put(user.jid, user);
        }
    }

    public void clear() {
        mUserInfoList.clear();
        mUserMap.clear();
        mInstance = null;
    }

    public UserInfo getUserInfo(int userId) {
        System.out.println("----------面聊小秘书 的用户信息 = " + mUserInfoList.get(userId) == null);
        if (mUserInfoList.get(userId) != null) {
            System.out.println("-------------------- get userInfo 不为空");
            return mUserInfoList.get(userId);
        } else {
            new GetUserInfoById(userId).executeLong();
            System.out.println("-------------------- get userInfo 为空");
            return null;
        }
    }

    public UserInfo getUserInfo(String jid) {
        return mUserMap.get(jid);
    }

    public String getUserName(String jid) {
        UserInfo info = getUserInfo(jid);
        return info == null ? StringUtils.parseName(jid) : info.getDisplayName(mContext);
    }

    /**
     * Get user info from MotherShip server.
     */
    public void acquireUserInfo(Collection<UserEntry> users) {
        ArrayList<String> newUsers = new ArrayList<String>();
        ArrayList<UserInfo> updateUsers = new ArrayList<UserInfo>();
        for (UserEntry user : users) {
            UserInfo info = mUserMap.get(user.jid);
            if (info == null) {
                newUsers.add(user.jid);
            } else {
                updateUsers.add(info);
            }
        }

        if (newUsers.size() > 0) {
            new AcquireUserInfoTask(newUsers).executeLong();
        }

        if (updateUsers.size() > 0) {
            new UpdateUserInfoTask(updateUsers).executeLong();
        }
    }

    public void acquireUserInfo(String jid) {
        if (mUserMap.get(jid) == null) {
            ArrayList<String> users = new ArrayList<String>();
            users.add(jid);
            new AcquireUserInfoTask(users).executeLong();
        }
    }
    public void updateUserInfo(String jid) {
        ArrayList<String> users = new ArrayList<String>();
        users.add(jid);
        new AcquireUserInfoTask(users).executeLong();
    }

    public void addUserInfo(UserInfo user) {
        if (user == null) {
            return;
        }
        ArrayList<UserInfo> users = new ArrayList<UserInfo>();
        users.add(user);
        addUserInfo(users);
    }

    public void addUserInfo(ArrayList<UserInfo> users) {
        for (UserInfo user : users) {
            if (mUserInfoList.get(user.userId) == null) {
                mUserInfoList.put(user.userId, user);
                mUserMap.put(user.jid, user);
            }
        }
        DataHelper.insertUserInfo(mContext, users);
        ContactUpdateCenter.notifyContactsUpdated(UpdateType.UserInfo);
    }

    public void saveUserInfo(UserInfo user) {
        if (user == null) {
            return;
        }
        if (mUserInfoList.get(user.userId) != null) {
            mUserInfoList.put(user.userId, user);
            updateUserInfo(user);
        } else {
            addUserInfo(user);
        }
    }

    public void updateUserInfo(UserInfo user) {
        if (user == null) {
            return;
        }
        ArrayList<UserInfo> users = new ArrayList<UserInfo>();
        users.add(user);
        updateUserInfo(users);
    }

    public void updateUserInfo(ArrayList<UserInfo> users) {
        DataHelper.updateUserInfo(mContext, users);
        ContactUpdateCenter.notifyContactsUpdated(UpdateType.UserInfo);
        updateUserMapInfo(users);
    }


    public void updateUserMapInfo(ArrayList<UserInfo> users) {
        for (UserInfo userInfo : users) {
            if (mUserMap.get(userInfo.jid) != null) {
                 mUserMap.remove(userInfo.jid);
            }
            mUserMap.put(userInfo.jid, userInfo);
            if (mUserInfoList.get(userInfo.userId) != null) {
                mUserInfoList.remove(userInfo.userId);
            }
            mUserInfoList.append(userInfo.userId, userInfo);
        }
    }
    
    private class AcquireUserInfoTask extends MsTask {
        private Collection<String> mUsers;

        public AcquireUserInfoTask(Collection<String> users) {
            super(mContext, MsRequest.FRIEND_LIST_BY_NAMES);
            mUsers = users;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("names=");
            for (String user : mUsers) {
                sb.append(StringUtils.parseName(user)).append(Utils.COMMA_DELIMITER);
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<UserInfo> users = JsonUtil.getArray(
                        response.getJsonArray(), UserInfo.TRANSFORMER);
                if (!users.isEmpty()) {
                    addUserInfo(users);
                }
            }
        }
    }

    private class UpdateUserInfoTask extends MsTask {
        private Collection<UserInfo> mUsers;

        public UpdateUserInfoTask(Collection<UserInfo> users) {
            super(mContext, MsRequest.FRIEND_LIST_BY_NAMES);
            mUsers = users;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder("names=");
            for (UserInfo user : mUsers) {
                sb.append(user.account.toLowerCase()).append(Utils.COMMA_DELIMITER);
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<UserInfo> users = JsonUtil.getArray(
                        response.getJsonArray(), UserInfo.TRANSFORMER);
                if (!users.isEmpty()) {
                    updateUserInfo(users);
                }
            }
        }
    }

    private class GetUserInfoById extends MsTask {
        private int mUserId;
        public GetUserInfoById (int userId) {
            super(mContext, MsRequest.USER_FULL_INFO);
            mUserId = userId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("user_id=").append(mUserId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                System.out.println("-------------------- get userInfo by id 成功");
                UserInfo userInfo = UserInfo.fromJson(response.getJsonObject());
                System.out.println("-------------------- get userInfo " + userInfo.name);
                addUserInfo(userInfo);
            }
        }
    }
}
