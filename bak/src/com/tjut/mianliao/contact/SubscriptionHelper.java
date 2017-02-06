package com.tjut.mianliao.contact;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

import android.content.Context;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.chat.UnreadMessageHelper;
import com.tjut.mianliao.explore.TriggerEventTask;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.xmpp.ConnectionManager;
import com.tjut.mianliao.xmpp.ConnectionManager.ConnectionObserver;

public class SubscriptionHelper {

    private static WeakReference<SubscriptionHelper> sInstanceRef;

    private Context mContext;
    private ConnectionManager mConnectionManager;
    private XMPPConnection mConnection;
    private CopyOnWriteArrayList<String> mSubscriptions;

    private ArrayList<SubRequestListener> mRequestListeners;
    private ArrayList<SubResponseListener> mResponseListeners;
    private ArrayList<NewFriendsRequestListener> mFriendsRequestListeners;

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

    public void subscribe(String target) {
        new SubscribeTask(target).executeLong();
    }

    public void unsubscribe(String target) {
        new UnsubscribeTask(target).executeLong();
    }

    public void accept(String target) {
        new AcceptTask(target).executeLong();
    }

    public void reject(String target) {
        new RejectTask(target).executeLong();
    }

    private boolean createEntry(String target) {
        if (!mConnection.isConnected()) {
            Toast.makeText(mContext, mContext.getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isFriend(target)) {
            try {
                Roster roster = mConnection.getRoster();
                if (roster == null) {
                    return false;
                }
                roster.createEntry(target, null, null);
            } catch (XMPPException e) {
                return false;
            }
        }
        return true;
    }

    private boolean isFriend(String target) {
        Roster roster = mConnection.getRoster();
        if (roster == null) {
            return false;
        }
        RosterEntry entry = roster.getEntry(target);
        return entry != null && (entry.getType() == RosterPacket.ItemType.both
                || entry.getType() == RosterPacket.ItemType.to);
    }

    private void addSubRequest(String target) {
        if (!mSubscriptions.contains(target)) {
            mSubscriptions.add(target);
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
            ContactUpdateCenter.notifyContactsUpdated(ContactUpdateCenter.UpdateType.Subscription);
        }
    }

    private void sendPresence(String target, Presence.Type type) {
        if (!mConnection.isConnected()) {
            return;
        }
        Presence presence = new Presence(type);
        presence.setTo(target);
        mConnection.sendPacket(presence);

        if (type == Presence.Type.unsubscribed) {
            try {
                Roster roster = mConnection.getRoster();
                if (roster == null) {
                    return;
                }
                RosterEntry entry = roster.getEntry(target);
                if (entry != null) {
                    roster.removeEntry(entry);
                }
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }

    }

    private void addPacketListener() {
        mConnection = mConnectionManager.getConnection();
        if (mConnection != null) {
            mConnection.addPacketListener(mPacketListener, new PacketTypeFilter(Presence.class));
        }
    }

    private void removePacketListener() {
        if (mConnection != null) {
            mConnection.removePacketListener(mPacketListener);
        }
    }

    private PacketListener mPacketListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
            if (packet instanceof Presence) {
                Presence presence = (Presence) packet;
                String target = presence.getFrom();
                switch (presence.getType()) {
                    case subscribe:
                        if (isFriend(target)) {
                            // It's already a friend of mine, just accept it.
                            sendPresence(target, Presence.Type.subscribed);
                            if (target != null && !target.toLowerCase().startsWith("mlserv")) {
                                new TriggerEventTask(mContext, "add_friend").executeLong();
                            }
                        } else {
                            addSubRequest(target);
                        }
                        break;

                    case unsubscribe:
                        sendPresence(target, Presence.Type.unsubscribed);
                        break;

                    default:
                        break;
                }
            }
        }
    };

    private ConnectionObserver mConnectionObserver = new ConnectionObserver() {
        @Override
        public void onConnectionUpdated(int state) {
            switch (state) {
                case ConnectionManager.XMPP_CONNECTED:
                    removePacketListener();
                    addPacketListener();
                    break;

                case ConnectionManager.XMPP_DISCONNECTED:
                    removePacketListener();
                    break;

                default:
                    break;
            }
        }
    };

    private class SubscribeTask extends AdvAsyncTask<Void, Void, Boolean> {
        private String mTarget;

        public SubscribeTask(String target) {
            mTarget = target;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return createEntry(mTarget);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            for (SubRequestListener listener : mRequestListeners) {
                listener.onSubscribe(result);
            }
        }
    }

    private class UnsubscribeTask extends AdvAsyncTask<Void, Void, Boolean> {
        private String mTarget;

        public UnsubscribeTask(String target) {
            mTarget = target;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (isFriend(mTarget)) {
                sendPresence(mTarget, Presence.Type.unsubscribe);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                UnreadMessageHelper.getInstance(mContext).deleteChat(mTarget);
                ContactUpdateCenter.notifyContactsUpdated(ContactUpdateCenter.UpdateType.Unsubscribe, mTarget);
            }
            for (SubRequestListener listener : mRequestListeners) {
                listener.onUnsubscribe(result);
            }
        }
    }

    private class AcceptTask extends AdvAsyncTask<Void, Void, Boolean> {
        private String mTarget;

        public AcceptTask(String target) {
            mTarget = target;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            sendPresence(mTarget, Presence.Type.subscribed);
            boolean result = createEntry(mTarget);
            if (result) {
                removeSubRequest(mTarget);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            for (SubResponseListener listener : mResponseListeners) {
                listener.onSubAccept(result);
            }
        }
    }

    private class RejectTask extends AdvAsyncTask<Void, Void, Boolean> {
        private String mTarget;

        public RejectTask(String target) {
            mTarget = target;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            sendPresence(mTarget, Presence.Type.unsubscribed);
            removeSubRequest(mTarget);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            for (SubResponseListener listener : mResponseListeners) {
                listener.onSubReject(result);
            }
        }
    }

    public interface SubRequestListener {
        public void onSubscribe(boolean success);

        public void onUnsubscribe(boolean success);
    }

    public interface SubResponseListener {
        public void onSubAccept(boolean success);

        public void onSubReject(boolean success);
    }

    public interface NewFriendsRequestListener {
        public void onNewRequest(String target);
    }
}
