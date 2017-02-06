package com.tjut.mianliao.tribe;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;

import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class FollowUserManager {

    private static WeakReference<FollowUserManager> sInstanceRef;

    private List<OnUserFollowListener> mListeners;
    
    private Context mContext;
    
    public static synchronized FollowUserManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        FollowUserManager instance = new FollowUserManager(context);
        sInstanceRef = new WeakReference<FollowUserManager>(instance);
        return instance;
    }

    public FollowUserManager(Context context) {
        mContext = context.getApplicationContext();
        mListeners = new CopyOnWriteArrayList<OnUserFollowListener>();
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
    
    private class FollowUserTask extends MsTask{

        private int mUid;
        
        public FollowUserTask(int uid) {
            super(mContext, MsRequest.FRIEND_FOLLOW_USER);
            mUid = uid;
        }
        
        @Override
        protected String buildParams() {
            return  new StringBuilder("follow_uid=").append(mUid).toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
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
            return  new StringBuilder("follow_uid=").append(mUid).toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
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
    
    public interface OnUserFollowListener {
        void onFollowSuccess();
        void onFollowFail();
        void onCancleFollowSuccess();
        void onCancleFollowFail();
    }

}
