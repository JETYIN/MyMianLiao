package com.tjut.mianliao.xmpp;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.ping.PingManager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.Utils;

/**
 * Manage connection to server. And connection related actions.
 */
public class ConnectionManager {
    private static final String TAG = "ConnectionManager";

    public static final int XMPP_DISCONNECTED = 1;
    public static final int XMPP_CONNECTING = 2;
    public static final int XMPP_CONNECTED = 3;

    private static final long INCRE_RECONN_DELAY = 10 * 1000;
    private static final long MAX_RECONN_DELAY = 600 * 1000;

    private static WeakReference<ConnectionManager> sInstanceRef;

    private Context mContext;
    private Handler mHandler;
    private XmppConnector mXmppConnector;
    private int mState;
    private long mReconnectDelay;

    private List<ConnectionObserver> mConnectionObservers;

    /**
     * Wrap network actions(connect, login, disconnect, etc) with AsyncTask to
     * provide good user experience.
     */
    public static synchronized ConnectionManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        initStaticCode(context);
        ConnectionManager instance = new ConnectionManager(context);
        sInstanceRef = new WeakReference<ConnectionManager>(instance);
        return instance;
    }

    private static void initStaticCode(Context context) {
        ClassLoader loader = context.getClassLoader();
        try {
            Class.forName(PingManager.class.getName(), true, loader);
            Class.forName(PacketMonitor.class.getName(), true, loader);
        } catch (ClassNotFoundException e) {
        }

        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
    }

    private ConnectionManager(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
        mConnectionObservers = new CopyOnWriteArrayList<ConnectionObserver>();
        mXmppConnector = new XmppConnector(mContext);
        mState = XMPP_DISCONNECTED;
    }

    public void confirmConnect() {
        resetDelay();
        scheduleReconnect();
    }

    public void exit() {
        mXmppConnector.removeConnectionListener(mConnectionListener);
        mHandler.removeCallbacks(mConnectRunnable);
        disconnect();
    }

    public void clear() {
        mXmppConnector.destroy();
        mConnectionObservers.clear();
        sInstanceRef.clear();
    }

    public XMPPConnection getConnection() {
        return mXmppConnector.getConnection();
    }

    public boolean isXmppConnected() {
        return mState == XMPP_CONNECTED;
    }

    public boolean isXmppConnecting() {
        return mState == XMPP_CONNECTING;
    }

    public boolean isXmppDisconnected() {
        return mState == XMPP_DISCONNECTED;
    }

    /**
     * Once registered, a connection state update is called immediately to this
     * observer.
     *
     * @param observer
     */
    public void registerConnectionObserver(ConnectionObserver observer) {
        if (observer != null && !mConnectionObservers.contains(observer)) {
            mConnectionObservers.add(observer);
            observer.onConnectionUpdated(mState);
        }
    }

    public void unregisterConnectionObserver(ConnectionObserver observer) {
        mConnectionObservers.remove(observer);
    }

    private boolean isNetworkConnected() {
        return Utils.isNetworkAvailable(mContext);
    }

    private void scheduleReconnect() {
        mHandler.removeCallbacks(mConnectRunnable);
        mHandler.postDelayed(mConnectRunnable, mReconnectDelay);
        increaseDelay();
    }

    private void increaseDelay() {
        mReconnectDelay += INCRE_RECONN_DELAY;
        if (mReconnectDelay > MAX_RECONN_DELAY) {
            mReconnectDelay = MAX_RECONN_DELAY;
        }
    }

    private void resetDelay() {
        mReconnectDelay = 0;
    }

    private void setState(int state) {
        if (mState != state) {
            mState = state;
            for (ConnectionObserver ob : mConnectionObservers) {
                ob.onConnectionUpdated(state);
            }
        }
    }

    private void connect() {
        AccountInfo account = AccountInfo.getInstance(mContext);

        if (account.isLoggedIn()) {
            new ConnectTask(account.getAccount(), account.getToken()).executeLong();
        }
    }

    private void disconnect() {
        new DisconnectTask().executeLong();
    }

    private Runnable mConnectRunnable = new Runnable() {
        @Override
        public void run() {
            Utils.logD(TAG, "ConnectRunnable: current state = " + mState);
            if (isXmppConnecting()) {
                return;
            }
            if (isXmppDisconnected()) {
                connect();
            }
        }
    };

    private ConnectionListener mConnectionListener = new ConnectionListener() {
        @Override
        public void connectionClosed() {
            Utils.logD(TAG, "ConnectionListener: connectionClosed");
            reconnect();
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Utils.logD(TAG, "ConnectionListener: connectionClosedOnError: " + e.getMessage());
            reconnect();
        }

        @Override
        public void reconnectingIn(int seconds) {
            Utils.logD(TAG, "ConnectionListener: reconnectingIn: " + seconds);
        }

        @Override
        public void reconnectionSuccessful() {
            Utils.logD(TAG, "ConnectionListener: reconnectionSuccessful");
            confirmConnect();
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Utils.logD(TAG, "ConnectionListener: reconnectionFailed: " + e.getMessage());
            reconnect();
        }

        private void reconnect() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setState(XMPP_DISCONNECTED);
                }
            });
            scheduleReconnect();
        }
    };

    private class ConnectTask extends AdvAsyncTask<Void, Void, Boolean> {
        private String mUser;
        private String mToken;

        public ConnectTask(String user, String token) {
            mUser = user;
            mToken = token;
        }

        @Override
        protected void onPreExecute() {
            setState(XMPP_CONNECTING);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return isNetworkConnected() ? mXmppConnector.connect(mUser, mToken) : false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setState(XMPP_CONNECTED);
                mXmppConnector.addConnectionListener(mConnectionListener);
            } else {
                setState(XMPP_DISCONNECTED);
                scheduleReconnect();
            }
        }
    }

    private class DisconnectTask extends AdvAsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            setState(XMPP_CONNECTING);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return mXmppConnector.disconnect();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            setState(XMPP_DISCONNECTED);
        }
    }

    public interface ConnectionObserver {
        void onConnectionUpdated(int state);
    }
}
