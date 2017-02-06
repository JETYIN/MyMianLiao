package com.tjut.mianliao.contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;

import com.hyphenate.EMContactListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMContactManager;
import com.hyphenate.exceptions.HyphenateException;
import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ConnectionManager;
import com.tjut.mianliao.xmpp.ConnectionManager.ConnectionObserver;

/**
 * Manages roster of the user. And listens entry updates from XMPP server.
 */
public class UserEntryManager {

    protected static final String TAG = "UserEntryManager";

    private static WeakReference<UserEntryManager> sInstanceRef;

    private Context mContext;
    private Settings mSettings;
    private ConnectionManager mConnectionManager;
    private SubscriptionHelper mSubscriptionHelper;
    private EMContactManager mContactManager;

    private HashSet<String> mBlacklistSet;
    private Hashtable<String, UserEntry> mUserTable;
    private Hashtable<String, UserEntry> mFriendsTable;

    /**
     * Keep a weak reference to the manager because it's used widely across the
     * application. When there's already an instance of the manager, the next
     * caller don't need to create a new instance of it. And it also make sure
     * the different parts of the application gets the same data.
     */
    public static synchronized UserEntryManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        UserEntryManager instance = new UserEntryManager(context);
        sInstanceRef = new WeakReference<UserEntryManager>(instance);
        return instance;
    }

    private UserEntryManager(Context context) {
        mContext = context;
        mSettings = Settings.getInstance(context);
        mBlacklistSet = new HashSet<String>(mSettings.getBlacklistSet());
        mUserTable = new Hashtable<String, UserEntry>();
        mFriendsTable = new Hashtable<String, UserEntry>();
        for (UserEntry user : DataHelper.loadUserEntries(mContext)) {
            Utils.logD(TAG, "load user from db :" + user.jid);
            mUserTable.put(user.jid, user);
            mFriendsTable.put(user.jid, user);
        }
        mContactManager = EMClient.getInstance().contactManager();
        mContactManager.setContactListener(mContactListener);
        mConnectionManager = ConnectionManager.getInstance(context);
        mConnectionManager.registerConnectionObserver(mConnectionObserver);
        try {
            List<String> contactsUser = mContactManager.getAllContactsFromServer();
            Utils.logD(TAG, "load contact from server : " + contactsUser.size() + "," + contactsUser.toString());
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        bindData();
    }

    public void clear() {
        mConnectionManager.unregisterConnectionObserver(mConnectionObserver);
        mBlacklistSet.clear();
        mUserTable.clear();
        mFriendsTable.clear();
        sInstanceRef.clear();
    }

    public UserEntry getUserEntry(String jid) {
        return mUserTable.get(jid);
    }

    public boolean hasFriends() {
        return !mFriendsTable.isEmpty();
    }

    /**
     * Do not add/remove items in the returned list, cause it infects the source
     * data.
     */
    public Collection<UserEntry> getFriends() {
        return mFriendsTable.values();
    }

    public Collection<String> getBlacklist() {
        return new HashSet<String>(mBlacklistSet);
    }

    public boolean changeBlacklist(String jid, boolean addTo) {
        String userName = StringUtils.parseName(jid);
        if (mSubscriptionHelper == null) {
            mSubscriptionHelper = SubscriptionHelper.getInstance(mContext);
        }
        boolean result = addTo ? mSubscriptionHelper.addToBlackList(userName) : 
            mSubscriptionHelper.removeFromBlackList(userName);

        if (result) {
            synchronized (mBlacklistSet) {
                if (addTo) {
                    mBlacklistSet.add(jid);
                } else {
                    mBlacklistSet.remove(jid);
                }
                mSettings.setBlacklistSet(mBlacklistSet);
            }
            notifyContactsUpdated(UpdateType.Blacklist, mUserTable.values());
        }

        return result;
    }

    public boolean isBlacklisted(String jid) {
        return mBlacklistSet.contains(jid);
    }

    public boolean isFriend(String jid) {
        return jid != null && mFriendsTable.containsKey(jid);
    }

    private ArrayList<String> getBlackLists() {
        List<String> blackList;
        ArrayList<String> blackLists = new ArrayList<>();
        try {
            blackList = mContactManager.getBlackListFromServer();
            for(String user : blackList){
                blackLists.add(user);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
        return blackLists;
    }
    
    /**
     * Bind to a roster instance and get updates from it.
     */
    private void bindData() {
        if (mContactManager == null) {
            return;
        }

        // Bind privacies
        synchronized (mBlacklistSet) {
            mBlacklistSet.clear();
            mBlacklistSet.addAll(getBlacklist());
            mSettings.setBlacklistSet(mBlacklistSet);
        }

        @SuppressWarnings("unchecked")
        Hashtable<String, UserEntry> localEntries = (Hashtable<String, UserEntry>) mUserTable.clone();
        ArrayList<UserEntry> updates = new ArrayList<UserEntry>();
        ArrayList<UserEntry> newEntries = new ArrayList<UserEntry>();

        mUserTable.clear();
        mFriendsTable.clear();
        try {
            List<String> contactsUser = mContactManager.getAllContactsFromServer();
            Utils.logD(TAG, "load contact from server : " + contactsUser.size() + "," + contactsUser.toString());
            for (String userName : mContactManager.getAllContactsFromServer()) {
                Utils.logD(TAG, "contact username : " + userName);
                UserEntry user = UserEntry.fromRosterEntity(userName);
                if (localEntries.containsKey(user.jid)) {
                    if (!localEntries.remove(user.jid).equals(user)) {
                        // Something changed, add it to a list.
                        updates.add(user);
                    }
                } else {
                    // New entry
                    newEntries.add(user);
                }
                mUserTable.put(user.jid, user);
//                if (isFriend(user)) {
                mFriendsTable.put(user.jid, user);
//                }
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }

        if (updates.size() > 0) {
            DataHelper.updateUserEntries(mContext, updates);
        }
        if (newEntries.size() > 0) {
            DataHelper.insertUserEntries(mContext, newEntries);
        }
        if (localEntries.size() > 0) {
            DataHelper.deleteUserEntries(mContext, localEntries.values());
        }

        UserRemarkManager.getInstance(mContext).update();
        notifyContactsUpdated(UpdateType.UserEntry, mUserTable.values());
    }

    private boolean isFriend(UserEntry entry) {
        return mFriendsTable.contains(entry);
    }

    private void notifyContactsUpdated(UpdateType type, Collection<UserEntry> entries) {
        if (entries != null) {
            UserInfoManager.getInstance(mContext).acquireUserInfo(entries);
        }
        ContactUpdateCenter.notifyContactsUpdated(type);
    }

    private ConnectionObserver mConnectionObserver = new ConnectionObserver() {
        @Override
        public void onConnectionUpdated(int state) {
            switch (state) {
                case ConnectionManager.EMCHAT_CONNECTED:
                    bindData();
                    break;
                case ConnectionManager.EMCHAT_DISCONNECTED:
                    break;
                default:
                    break;
            }
        }
    };

    private EMContactListener mContactListener = new EMContactListener() {
        
        @Override
        public void onContactRefused(String username) {
            
        }
        
        @Override
        public void onContactInvited(String username, String reason) {
            
        }
        
        @Override
        public void onContactDeleted(String username) {
            ArrayList<UserEntry> deletes = new ArrayList<UserEntry>();
            String jid = username + Utils.getJidSuffix();
            if (mUserTable.containsKey(jid)) {
                deletes.add(mUserTable.remove(jid));
                mFriendsTable.remove(jid);
            }
            if (deletes.size() > 0) {
                DataHelper.deleteUserEntries(mContext, deletes);
                notifyContactsUpdated(UpdateType.UserEntry, null);
            }
        }
        
        @Override
        public void onContactAgreed(String username) {
            Utils.logD(TAG, "onContactAgreed : " + username);
            ArrayList<UserEntry> newEntries = new ArrayList<UserEntry>();
            UserEntry user = UserEntry.fromRosterEntity(username);
            if (!mUserTable.containsKey(user.jid)) {
                newEntries.add(user);
                mUserTable.put(user.jid, user);
                if (!isFriend(user)) {
                    mFriendsTable.put(user.jid, user);
                }
            }
            if (newEntries.size() > 0) {
                DataHelper.insertUserEntries(mContext, newEntries);
                notifyContactsUpdated(UpdateType.UserEntry, newEntries);
            }
        }
        
        @Override
        public void onContactAdded(String username) {
            Utils.logD(TAG, "onContactAdded : " + username);
            ArrayList<UserEntry> newEntries = new ArrayList<UserEntry>();
            UserEntry user = UserEntry.fromRosterEntity(username);
            if (!mUserTable.containsKey(user.jid)) {
                newEntries.add(user);
                mUserTable.put(user.jid, user);
                if (!isFriend(user)) {
                    mFriendsTable.put(user.jid, user);
                }
            }
            if (newEntries.size() > 0) {
                DataHelper.insertUserEntries(mContext, newEntries);
                notifyContactsUpdated(UpdateType.UserEntry, newEntries);
            }

        }
    };
}
