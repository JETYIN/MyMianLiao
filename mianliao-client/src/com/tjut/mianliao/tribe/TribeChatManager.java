package com.tjut.mianliao.tribe;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;

import com.tjut.mianliao.data.tribe.TribeChatRoomInfo;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class TribeChatManager {
    
    public static final int TYPE_ADD_ROOM = 1;
    public static final int TYPE_EXIT_ROOM = 2;
    public static final int TYPE_DEL_ROOM = 3;
    public static final int TYPE_ADD_USERS =4;
    public static final int TYPE_REMOVE_USER = 5;
    public static final int TYPE_TRIBE_ROOM_INFO = 6;
    
    
    private static WeakReference<TribeChatManager> sInstanceRef;
    private Context mContext;
    private List<TribeChatListener> mListeners;
    
    public static synchronized TribeChatManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        TribeChatManager instance = new TribeChatManager(context);
        sInstanceRef = new WeakReference<TribeChatManager>(instance);
        return instance;
    }
    
    private TribeChatManager(Context context) {
        mContext = context.getApplicationContext();
        mListeners = new CopyOnWriteArrayList<TribeChatListener>();
    }
    
    public void registerTribeChatListener(TribeChatListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void unregisterTribeChatListener(TribeChatListener listener) {
        mListeners.remove(listener);
    }
    
    public void accessChatRoom (int uid, int tribeId, String groupId) {
        new AccessChatRoomTask(uid, tribeId, groupId).executeLong();
    }
    
    public void exitChatRoom (int uid, int tribeId, String groupId) {
        new ExitChatRoomTask(uid, tribeId, groupId).executeLong();
    }
    
    public void deleteChatRoom (int uid, int tribeId, String groupId) {
        new DeleteChatRoomTask(uid, tribeId, groupId).executeLong();
    }
    
    public void AddFriendChatRoom (int uid, int tribeId, String groupId, String userIds) {
        new AddFriendToChat(uid, tribeId, groupId, userIds).executeLong();
    }
    
    public void removeFriendChatRoom (int uid, int tribeId, String groupId, int memberId) {
        new RemoveFriendTask(uid, tribeId, groupId, memberId).executeLong();
    } 
    
    public void getTribeChatRoomInfo(int roomId) {
        new GetTribeChatRoomInfoTask(roomId).executeLong();
    }
    
    class AccessChatRoomTask extends MsTask {
        
        int mUid, mTribeId; 
        String mRoomId;
        
        public AccessChatRoomTask(int uid, int tribeId, String groupId) {
            super(mContext, MsRequest.TRIBE_ACCESS_CHAT_ROOM);
            mUid = uid;
            mTribeId = tribeId;
            mRoomId = groupId;
        }
        @Override
        protected String buildParams() {
            return new StringBuilder("tribe_id=").append(mTribeId)
                    .append("&room_id=").append(mRoomId)
                    .append("&uid=").append(mUid)
                    .toString();
        }
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                String jid = response.json.optString("jid");
                for (TribeChatListener listener : mListeners) {
                    listener.onSuccess(TYPE_ADD_ROOM, jid);
                }
            } else {
                for (TribeChatListener listener : mListeners) {
                    listener.onFail(TYPE_ADD_ROOM);
                }
            }
        }
    }
   
    class ExitChatRoomTask extends MsTask {
        
        int mUid, mTribeId; 
        String mRoomId;
        
        public ExitChatRoomTask(int uid, int tribeId, String groupId) {
            super(mContext, MsRequest.TRIBE_EXIT_CHAT_ROOM);
            mUid = uid;
            mTribeId = tribeId;
            mRoomId = groupId;
        }
        @Override
        protected String buildParams() {
            return new StringBuilder("tribe_id=").append(mTribeId)
                    .append("&room_id=").append(mRoomId)
                    .append("&uid=").append(mUid)
                    .toString();
        }
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                for (TribeChatListener listener : mListeners) {
                    listener.onSuccess(TYPE_EXIT_ROOM, "");
                }
            } else {
                for (TribeChatListener listener : mListeners) {
                    listener.onFail(TYPE_EXIT_ROOM);
                }
            }
        }
    }
    
    class DeleteChatRoomTask extends MsTask {
        
        int mUid, mTribeId; 
        String mRoomId;
        
        public DeleteChatRoomTask(int uid, int tribeId, String groupId) {
            super(mContext, MsRequest.TRIBE_DELETE_CHAT_ROOM);
            mUid = uid;
            mTribeId = tribeId;
            mRoomId = groupId;
        }
        @Override
        protected String buildParams() {
            return new StringBuilder("tribe_id=").append(mTribeId)
                    .append("&room_id=").append(mRoomId)
                    .append("&uid=").append(mUid)
                    .toString();
        }
        @Override
        protected void onPostExecute(MsResponse response) {
            MsResponse msre = response;
            if (response.isSuccessful()) {
                for (TribeChatListener listener : mListeners) {
                    listener.onSuccess(TYPE_DEL_ROOM, "");
                }
            } else {
                for (TribeChatListener listener : mListeners) {
                    listener.onFail(TYPE_DEL_ROOM);
                }
            }
        }
    }
    
    class AddFriendToChat extends MsTask {
        
        int mUid, mTribeId; 
        String mRoomId, mUserIds;
        
        public AddFriendToChat(int uid, int tribeId, String groupId, String userIds) {
            super(mContext, MsRequest.TRIBE_ADD_FRIEND_TO_CHAT);
            mUid = uid;
            mTribeId = tribeId;
            mRoomId = groupId;
            mUserIds = userIds;
        }
        @Override
        protected String buildParams() {
            return new StringBuilder("tribe_id=").append(mTribeId)
                    .append("&room_id=").append(mRoomId)
                    .append("&uid=").append(mUid)
                    .append("&member_id=").append(mUserIds)
                    .toString();
        }
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                for (TribeChatListener listener : mListeners) {
                    listener.onSuccess(TYPE_ADD_USERS, "");
                }
            } else {
                for (TribeChatListener listener : mListeners) {
                    listener.onFail(TYPE_ADD_USERS);
                }
            }
        }
    }
    
    class RemoveFriendTask extends MsTask {

        int mUid, mTribeId, mMemberId;
        String mRoomId;

        public RemoveFriendTask(int uid, int tribeId, String groupId, int memberId) {
            super(mContext, MsRequest.TRIBE_REMOVE_FRIEND_TO_CHAT);
            mUid = uid;
            mTribeId = tribeId;
            mRoomId = groupId;
            mMemberId = memberId;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("tribe_id=").append(mTribeId).append("&room_id=").append(mRoomId).append("&uid=")
                    .append(mUid).append("&member_id=").append(mMemberId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                for (TribeChatListener listener : mListeners) {
                    listener.onSuccess(TYPE_REMOVE_USER, "");
                }
            } else {
                for (TribeChatListener listener : mListeners) {
                    listener.onFail(TYPE_REMOVE_USER);
                }
            }
        }
    }
    
    private class GetTribeChatRoomInfoTask extends MsTask{

        private int mRoomId;
        
        public GetTribeChatRoomInfoTask(int roomId) {
            super(mContext, MsRequest.TRIBE_CHATROOM_BY_ID);
            mRoomId = roomId;
        }
        
        @Override
        protected String buildParams() {
            return "room_id=" + mRoomId;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                TribeChatRoomInfo roomInfo = TribeChatRoomInfo.fromJson(response.getJsonObject());
                for (TribeChatListener listener : mListeners) {
                    listener.onSuccess(TYPE_TRIBE_ROOM_INFO, roomInfo);
                }
            }
        }
        
    }
    
    public interface TribeChatListener{
        void onSuccess(int type, Object obj);
        void onFail(int type);
    }
}
