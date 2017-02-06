package com.tjut.mianliao.contact;

import android.content.Context;
import android.content.SharedPreferences;

import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.FocusUserInfo;
import com.tjut.mianliao.data.contact.UserInfo;

import com.tjut.mianliao.data.RadMenInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FollowUserManager {

    private static final String SP_MY_FOLLOW_USER = "sp_my_follow_user";
    
    private static WeakReference<FollowUserManager> sInstanceRef;

    private List<OnUserFollowListener> mListeners;
    
    private Hashtable<String, UserInfo> mFollowTable;
    
    private SharedPreferences mSpFollows;
    
    private String mFollowsStr;
    private static String[] mFollows;
    private static ArrayList<String> mFollowIdList;

    private static ArrayList<RadMenInfo> mFriendList;
    
    private Context mContext;
    
    public static synchronized FollowUserManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        FollowUserManager instance = new FollowUserManager(context);
        sInstanceRef = new WeakReference<FollowUserManager>(instance);
        return instance;
    }

    private FollowUserManager(Context context) {
        mContext = context.getApplicationContext();
        mListeners = new CopyOnWriteArrayList<OnUserFollowListener>();
        mFollowTable = new Hashtable<String, UserInfo>();
        mSpFollows = DataHelper.getSpForData(context);
        mFollowIdList = new ArrayList<>();
        mFriendList = new ArrayList<>();
        new GetMyFollowIdsTask().executeLong();
        new GetContactsTask().executeLong();
        loadDataFromSp();
    }

    public void registerOnUserFollowListener(OnUserFollowListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    
    public void unregisterOnUserFollowListener(OnUserFollowListener listener) {
        mListeners.remove(listener);
    }
    
    public void follow(int uid) {
        new FollowUserTask(uid).executeLong();
    }
    
    public void cancleFollow(int uid) {
        new CancelFollowUserTask(uid).executeLong();
    }
    
    public void getFollows(int offset) {
        new GetMyFollowsListTask(offset).executeLong();
    }
    
    public void getFollows(int offset, MsRequest request) {
        new GetMyFollowsListTask(offset, request).executeLong();
    }
    
    private class FollowUserTask extends MsTask{

        private int mUid;
        
        public FollowUserTask(int uid) {
            super(mContext, MsRequest.FRIEND_FOLLOW_USER);
            mUid = uid;
        }
        
        @Override
        protected String buildParams() {
            return "follow_uid=" + mUid;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                addFollow(mUid);
                new GetContactsTask().executeLong();
                 for (OnUserFollowListener listener : mListeners) {
                     listener.onFollowSuccess();
                 }
            } else {
                for (OnUserFollowListener listener : mListeners) {
                    listener.onFollowFail();
                }
            }
        }
        
    }
    
    private class CancelFollowUserTask extends MsTask{
        
        private int mUid;
        
        public CancelFollowUserTask(int uid) {
            super(mContext, MsRequest.FRIEND_CANCEL_FOLLOW_USER);
            mUid = uid;
        }
        
        @Override
        protected String buildParams() {
            return "follow_uid=" + mUid;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                removeFollow(mUid);
                new GetContactsTask().executeLong();
                for (OnUserFollowListener listener : mListeners) {
                    listener.onCancleFollowSuccess();
                }
            } else {
                for (OnUserFollowListener listener : mListeners) {
                    listener.onCancleFollowFail();
                }
            }
        }
        
    }
    
    private class GetMyFollowsListTask extends MsTask {

        private int mOffset;
        private MsRequest mResquest;

        public GetMyFollowsListTask(int offset) {
            super(mContext, MsRequest.FRIEND_MY_FOLLOW_LIST);
            mOffset = offset;
        }

        public GetMyFollowsListTask(int offset, MsRequest request) {
            super(mContext, request);
            mOffset = offset;
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                for (OnUserFollowListener listener : mListeners) {
                    if (mResquest == MsRequest.FRIEND_MY_FOLLOW_LIST){
                        try {
                            JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                            JSONArray ja;
                            ja = jsonObj.getJSONArray("users");
                            ArrayList<FocusUserInfo> follows = JsonUtil.getArray(ja, FocusUserInfo.TRANSFORMER);
                            saveSpData(follows);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    listener.onGetFollowListSuccess(response, mOffset);
                }
            } else {
                for (OnUserFollowListener listener : mListeners) {
                    listener.onGetFollowListFail();
                }
            }
        }
    }
    
    private class GetMyFollowIdsTask extends MsTask{
        
        public GetMyFollowIdsTask() {
            super(mContext, MsRequest.USER_FOLLOW_IDS);
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                System.out.println("-----------------" + json);
                mFollowIdList.clear();
                mFollowIdList = JsonUtil.getStringArray(json.optJSONArray("followUids"));
            }
        }
        
    }
    
    public interface OnUserFollowListener {
        void onFollowSuccess();
        void onFollowFail();
        void onCancleFollowSuccess();
        void onCancleFollowFail();
        void onGetFollowListSuccess(MsResponse response, int offset);
        void onGetFollowListFail();
    }
    
    private void loadDataFromSp() {
        mFollowsStr = mSpFollows.getString(SP_MY_FOLLOW_USER, "[]");
        mFollows = mFollowsStr.split("-");
        getFollowList();
        
    }
    
    private void getFollowList () {
        mFollowIdList.clear();
        for(int i = 0 ; i < mFollows.length;i++) {
            mFollowIdList.add(mFollows[i]);
        }
    }
    
    private void saveSpData(ArrayList<FocusUserInfo> userInfo) {
        mSpFollows.edit().putString(SP_MY_FOLLOW_USER, buildFollowStr(userInfo)).commit();
    }
   
    private String buildFollowStr (ArrayList<FocusUserInfo> users) {
        String str = "";
        for (int i = 0; i < users.size(); i++) {
            if (i < users.size() - 1) {
                str = str + users.get(i).id + "-";
            } else {
                str = str + users.get(i).id;
            }
        }
        return str;
    }
    
    public boolean isFollow(String uid) {
        return uid != null && isFollowThisId(uid);
    }
    
    private boolean isFollowThisId(String uid){
        for(int i = 0 ; i < mFollowIdList.size();i++) {
            if (uid.equals(mFollowIdList.get(i))){
                return true;
            }
        }
        return false;
    }
    
    private void removeFollow (int id) {
        for (String fid : mFollowIdList) {
            if (fid.equals("" + id)) {
                mFollowIdList.remove(fid);
                break;
            }
        }
    }
    
    private void addFollow (int id) {
        mFollowIdList.add( id + "");
    }

    private class GetContactsTask extends MsTask{

        public GetContactsTask() {
            super(mContext, MsRequest.FRIEND_LIST_FRIENDS);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                mFriendList = JsonUtil.getArray(ja, RadMenInfo.TRANSFORMER);
            }
        }
    }
    public ArrayList<RadMenInfo> getFriendList () {
        return mFriendList;
    }

}
