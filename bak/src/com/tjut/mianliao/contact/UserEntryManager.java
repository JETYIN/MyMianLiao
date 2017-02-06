package com.tjut.mianliao.contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Context;

import com.tjut.mianliao.contact.ContactUpdateCenter.UpdateType;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserEntry;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.xmpp.ConnectionManager;
import com.tjut.mianliao.xmpp.ConnectionManager.ConnectionObserver;
import com.tjut.mianliao.xmpp.PrivacyHelper;

/**
 * Manages roster of the user. And listens entry updates from XMPP server.
 */
public class UserEntryManager {

    private static WeakReference<UserEntryManager> sInstanceRef;

    private Context mContext;
    private Settings mSettings;
    private ConnectionManager mConnectionManager;

    private HashSet<String> mBlacklistSet;
    private Hashtable<String, UserEntry> mUserTable;
    private Hashtable<String, UserEntry> mFriendsTable;

    private Roster mRoster;

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
            mUserTable.put(user.jid, user);
            if (isFriend(user)) {
                mFriendsTable.put(user.jid, user);
            }
        }
        mConnectionManager = ConnectionManager.getInstance(context);
        mConnectionManager.registerConnectionObserver(mConnectionObserver);
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
        boolean result = addTo ? PrivacyHelper.addToBlacklist(jid) : PrivacyHelper.removeFromBlackist(jid);

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

    public boolean getPresence(String jid) {
        if (mRoster != null && jid != null) {
            Presence pres = mRoster.getPresence(jid);
            return pres != null && pres.isAvailable();
        }
        return false;
    }

    /**
     * Bind to a roster instance and get updates from it.
     */
    private void bindRoster() {
        mRoster = mConnectionManager.getConnection().getRoster();
        if (mRoster == null) {
            return;
        }

        // Bind privacies
        synchronized (mBlacklistSet) {
            mBlacklistSet.clear();
            mBlacklistSet.addAll(PrivacyHelper.getBlockedJids());
            mSettings.setBlacklistSet(mBlacklistSet);
        }

        @SuppressWarnings("unchecked")
        Hashtable<String, UserEntry> localEntries = (Hashtable<String, UserEntry>) mUserTable.clone();
        ArrayList<UserEntry> updates = new ArrayList<UserEntry>();
        ArrayList<UserEntry> newEntries = new ArrayList<UserEntry>();

        mUserTable.clear();
        mFriendsTable.clear();
        for (RosterEntry re : mRoster.getEntries()) {
            UserEntry user = UserEntry.fromRosterEntity(re);
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
            if (isFriend(user)) {
                mFriendsTable.put(user.jid, user);
            }
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
        mRoster.addRosterListener(mRosterListener);
    }

    /**
     * Unbind from current roster instance.
     */
    private void unbindRoster() {
        if (mRoster != null) {
            mRoster.removeRosterListener(mRosterListener);
        }
    }

    private boolean isFriend(UserEntry entry) {
        return entry.subType == ItemType.both || entry.subType == ItemType.to;
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
                case ConnectionManager.XMPP_CONNECTED:
                    unbindRoster();
                    bindRoster();
                    break;
                case ConnectionManager.XMPP_DISCONNECTED:
                    unbindRoster();
                    break;
                default:
                    break;
            }
        }
    };

    private RosterListener mRosterListener = new RosterListener() {

        @Override
        public void presenceChanged(Presence presence) {
            String jid = StringUtils.parseBareAddress(presence.getFrom());
            if (mUserTable.containsKey(jid)) {
                notifyContactsUpdated(UpdateType.Presence, null);
            }
        }

        @Override
        public void entriesAdded(Collection<String> addresses) {
            ArrayList<UserEntry> newEntries = new ArrayList<UserEntry>();
            for (String addr : addresses) {
                UserEntry user = UserEntry.fromRosterEntity(mRoster.getEntry(addr));
                if (!mUserTable.containsKey(user.jid)) {
                    newEntries.add(user);
                    mUserTable.put(user.jid, user);
                    if (isFriend(user)) {
                        mFriendsTable.put(user.jid, user);
                    }
                }
            }
            if (newEntries.size() > 0) {
                DataHelper.insertUserEntries(mContext, newEntries);
                notifyContactsUpdated(UpdateType.UserEntry, newEntries);
            }
        }

        @Override
        public void entriesDeleted(Collection<String> addresses) {
            ArrayList<UserEntry> deletes = new ArrayList<UserEntry>();
            for (String addr : addresses) {
                String jid = StringUtils.parseBareAddress(addr);
                if (mUserTable.containsKey(jid)) {

                    try {
                        RosterEntry entry = mRoster.getEntry(jid);
                        mRoster.removeEntry(entry);
                    } catch (XMPPException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    deletes.add(mUserTable.remove(jid));
                    mFriendsTable.remove(jid);
                }
            }
            if (deletes.size() > 0) {
                DataHelper.deleteUserEntries(mContext, deletes);
                notifyContactsUpdated(UpdateType.UserEntry, null);
            }
        }

        @Override
        public void entriesUpdated(Collection<String> addresses) {
            ArrayList<UserEntry> updates = new ArrayList<UserEntry>();
            for (String addr : addresses) {
                UserEntry ue = UserEntry.fromRosterEntity(mRoster.getEntry(addr));
                if (mUserTable.containsKey(ue.jid) && !mUserTable.get(ue.jid).equals(ue)) {
                    updates.add(ue);
                    mUserTable.put(ue.jid, ue);
                    if (isFriend(ue)) {
                        mFriendsTable.put(ue.jid, ue);
                    } else {
                        mFriendsTable.remove(ue.jid);
                    }
                }
            }
            if (updates.size() > 0) {
                DataHelper.updateUserEntries(mContext, updates);
                notifyContactsUpdated(UpdateType.UserEntry, updates);
            }
        }
    };
}
