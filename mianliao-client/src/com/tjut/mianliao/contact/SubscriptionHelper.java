package com.tjut.mianliao.contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMContactManager;
import com.hyphenate.exceptions.HyphenateException;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.explore.TriggerEventTask;
import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ConnectionManager;
import com.tjut.mianliao.xmpp.ConnectionManager.ConnectionObserver;

public class SubscriptionHelper {
    
    public static final String SP_SUBSCRIPTION_RECORDS = "subscription_records";

    protected static final String TAG = "SubscriptionHelper";

    private static WeakReference<SubscriptionHelper> sInstanceRef;

    private Context mContext;
    private ConnectionManager mConnectionManager;
    private CopyOnWriteArrayList<String> mSubscriptions;
    private SharedPreferences mPreferences;
    private EMContactManager mEmContactManager;
    private UserEntryManager mEntryManager;

    private ArrayList<SubRequestListener> mRequestListeners;
    private ArrayList<SubResponseListener> mResponseListeners;
    private ArrayList<NewFriendsRequestListener> mFriendsRequestListeners;
    
    private final Handler msgHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 1:
                    Toast.makeText(mContext, mContext.getString(R.string.disconnected), 
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    public static synchronized SubscriptionHelper getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        SubscriptionHelper instance = new SubscriptionHelper(context);
        sInstanceRef = new WeakReference<SubscriptionHelper>(instance);
        return instance;
    }

    private SubscriptionHelper(Context context) {
        mContext = context.getApplicationContext();
        mSubscriptions = new CopyOnWriteArrayList<String>();
        mRequestListeners = new ArrayList<SubRequestListener>();
        mResponseListeners = new ArrayList<SubResponseListener>();
        mFriendsRequestListeners = new ArrayList<NewFriendsRequestListener>();
        mConnectionManager = ConnectionManager.getInstance(context);
        mConnectionManager.registerConnectionObserver(mConnectionObserver);
        mEmContactManager = EMClient.getInstance().contactManager();
        mEmContactManager.setContactListener(mContactListener);
        mPreferences = DataHelper.getSpForData(context);
        loadDataFromSp();
    }

    private void loadDataFromSp() {
        String record = mPreferences.getString(SP_SUBSCRIPTION_RECORDS, "");
        if (TextUtils.isEmpty(record)) {
            return;
        }
        String[] records = record.split(";");
        ArrayList<String> jids = new ArrayList<>();
        for (int i = 0; i < records.length; i++) {
            if (!TextUtils.isEmpty(records[i])) {
                mSubscriptions.add(records[i]);
                jids.add(records[i]);
            }
        }
    }

    public void registerRequestListener(SubRequestListener listener) {
        if (listener != null && !mRequestListeners.contains(listener)) {
            mRequestListeners.add(listener);
        }
    }

    public void registerResponseListener(SubResponseListener listener) {
        if (listener != null && !mResponseListeners.contains(listener)) {
            mResponseListeners.add(listener);
        }
    }

    public void registerNewFriendsRequestListener(NewFriendsRequestListener listener) {
        if (listener != null && !mFriendsRequestListeners.contains(listener)) {
            mFriendsRequestListeners.add(listener);
        }
    }

    public void unregisterRequestListener(SubRequestListener listener) {
        mRequestListeners.remove(listener);
    }

    public void unregisterResponseListener(SubResponseListener listener) {
        mResponseListeners.remove(listener);
    }

    public void unregisterNewFriendsRequestListener(NewFriendsRequestListener listener) {
        mFriendsRequestListeners.remove(listener);
    }

    public void clear() {
        mSubscriptions.clear();
        mRequestListeners.clear();
        mResponseListeners.clear();
        mConnectionManager.unregisterConnectionObserver(mConnectionObserver);
        sInstanceRef.clear();
    }

    public int getCount() {
        return mSubscriptions.size();
    }

    public ArrayList<String> getSubRequests() {
        return new ArrayList<String>(mSubscriptions);
    }

    public void addContact(String jid) {
        boolean success = false;
        try {
            String username = StringUtils.parseName(jid);
            mEmContactManager.addContact(username, "");
            success = true;
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        for (SubRequestListener listener : mRequestListeners) {
            listener.onSubscribe(success);
        }
    }

    public void deleteContact(String jid) {
        boolean success = false;
        try {
            String username = StringUtils.parseName(jid);
            mEmContactManager.deleteContact(username);
            success = true;
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        for (SubRequestListener listener : mRequestListeners) {
            listener.onUnsubscribe(success);
        }
    }

    public void accept(String username) {
        boolean success = false;
        if (!TextUtils.isEmpty(username)) {
            try {
                mEmContactManager.acceptInvitation(username);
                username = username + Utils.getJidSuffix();
                removeSubRequest(username);
                success = true;
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        for (SubResponseListener listener : mResponseListeners) {
            listener.onSubAccept(success);
        }
    }

    public void reject(String username) {
        boolean success = false;
        if (!TextUtils.isEmpty(username)) {
            try {
                mEmContactManager.declineInvitation(username);
                username = username + Utils.getJidSuffix();
                removeSubRequest(username);
                success = true;
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        for (SubResponseListener listener : mResponseListeners) {
            listener.onSubReject(success);
        }
    }
    
    public boolean addToBlackList(String usreName) {
        try {
            mEmContactManager.addUserToBlackList(usreName, false);
            return true;
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean removeFromBlackList(String usreName) {
        try {
            mEmContactManager.removeUserFromBlackList(usreName);
            return true;
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isFriend(String target) {
        if (mEntryManager == null) {
            mEntryManager = UserEntryManager.getInstance(mContext);
        }
        return mEntryManager.isFriend(target);
    }

    private void addSubRequest(String target) {
        target = Utils.parseJid(target);
        if (!mSubscriptions.contains(target)) {
            mSubscriptions.add(target);
            dealSubscriptionData(false, target);
        }
        UserInfoManager.getInstance(mContext).acquireUserInfo(target);
        ContactUpdateCenter.notifyContactsUpdated(ContactUpdateCenter.UpdateType.Subscription);
        for (NewFriendsRequestListener listener : mFriendsRequestListeners) {
            listener.onNewRequest(target);
        }
    }
    
    public void clearSubFlag() {
        if (mSubscriptions.size() == 0) {
            for (NewFriendsRequestListener listener : mFriendsRequestListeners) {
                listener.onNewRequest(null);
            }
        }
    }
    
    private void removeSubRequest(String target) {
        if (mSubscriptions.remove(target)) {
            dealSubscriptionData(true, target);
            ContactUpdateCenter.notifyContactsUpdated(ContactUpdateCenter.UpdateType.Subscription);
        }
    }

    public void dealSubscriptionData(boolean isDel, String target) {
        Editor edit = mPreferences.edit();
        String record = mPreferences.getString(SP_SUBSCRIPTION_RECORDS, "");
        String[] records = record.split(";");
        ArrayList<String> arrayRecords = new ArrayList<>();
        for (int i = 0; i < records.length; i++) {
            arrayRecords.add(records[i]);
        }
        if (isDel) {
            arrayRecords.remove(target);
        } else {
            if (!arrayRecords.contains(target)) {
                arrayRecords.add(target);
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String jid : arrayRecords) {
            if (isFirst) {
                sb.append(jid);
                isFirst = false;
            } else {
                sb.append(";").append(jid);
            }
        }
        edit.putString(SP_SUBSCRIPTION_RECORDS, sb.toString()).commit();
    }
    
    private EMContactListener mContactListener = new EMContactListener() {
        
        @Override
        public void onContactRefused(String username) {
            Utils.logD(TAG, "add friend request refused : " + username);
        }
        
        @Override
        public void onContactInvited(String username, String reason) {
            String jid = username + Utils.getJidSuffix();
            if (isFriend(jid)) {
                // It's already a friend of mine, just accept it.
                accept(username);
                if (!TextUtils.isEmpty(username) && !username.toLowerCase().startsWith("mlserv")) {
                    new TriggerEventTask(mContext, "add_friend").executeLong();
                }
            } else {
                addSubRequest(username);
            }
        }
        
        @Override
        public void onContactDeleted(String username) {
            Utils.logD(TAG, "contact deleted : " + username);
        }
        
        @Override
        public void onContactAgreed(String username) {
            Utils.logD(TAG, "add friend request agreed : " + username);
        }
        
        @Override
        public void onContactAdded(String username) {
            Utils.logD(TAG, "add contact : " + username);
        }
    };
    

    private ConnectionObserver mConnectionObserver = new ConnectionObserver() {
        @Override
        public void onConnectionUpdated(int state) {
            switch (state) {
                case ConnectionManager.EMCHAT_CONNECTED:
//                    removePacketListener();
//                    addPacketListener();
                    break;

                case ConnectionManager.EMCHAT_DISCONNECTED:
//                    removePacketListener();
                    break;

                default:
                    break;
            }
        }
    };

    public interface SubRequestListener {
        public void onSubscribe(boolean success);

        public void onUnsubscribe(boolean success);
    }

    public interface SubResponseListener {
        public void onSubAccept(boolean success);

        public void onSubReject(boolean success);
    }

    public interface NewFriendsRequestListener {
        /**
         * Call this method while has a new friend request
         * @param target
         */
        public void onNewRequest(String target);
    }
    
}
