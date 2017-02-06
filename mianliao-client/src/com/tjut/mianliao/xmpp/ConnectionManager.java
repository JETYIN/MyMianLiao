package com.tjut.mianliao.xmpp;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.os.Handler;

import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.NetUtils;
import com.tjut.mianliao.util.Utils;

/**
 * Manage connection to server. And connection related actions.
 */
public class ConnectionManager {
    private static final String TAG = "ConnectionManager";

    public static final int EMCHAT_DISCONNECTED = 1;
    public static final int EMCHAT_CONNECTED = 2;
    public static final int USER_REMOVED = 3;
    public static final int USER_LOGIN_ANOTHER_DEVICE = 4;
    public static final int CONTECTED_CHAT_SERVER_ERROR = 5;

    private static WeakReference<ConnectionManager> sInstanceRef;

    private Context mContext;
    private int mState;
    private EMClient mEmClient;

    private List<ConnectionObserver> mConnectionObservers;

    /**
     * Wrap network actions(connect, login, disconnect, etc) with AsyncTask to
     * provide good user experience.
     */
    public static synchronized ConnectionManager getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        ConnectionManager instance = new ConnectionManager(context);
        sInstanceRef = new WeakReference<ConnectionManager>(instance);
        return instance;
    }

    private ConnectionManager(Context context) {
        mContext = context.getApplicationContext();
        mConnectionObservers = new CopyOnWriteArrayList<ConnectionObserver>();
        mState = EMCHAT_CONNECTED;
        mEmClient = EMClient.getInstance();
        mEmClient.addConnectionListener(mConnectionListener);
    }

    public void exit() {
        mEmClient.removeConnectionListener(mConnectionListener);
    }

    public void clear() {
        mConnectionObservers.clear();
        sInstanceRef.clear();
    }
    
    public boolean chatServerConnected() {
        return mState == EMCHAT_CONNECTED;
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

    private EMConnectionListener  mConnectionListener = new EMConnectionListener() {
        private Handler mHandler = new Handler();
        private Handler mHandlerConnected = new Handler();
        @Override
        public void onDisconnected(final int error) {
            Utils.logE(TAG, "EMConnection disconnected : " + error);
            mHandler.post(new Runnable() {                
                @Override
                public void run() {
                    mState = EMCHAT_DISCONNECTED;
                    switch (error) {
                        case EMError.USER_REMOVED:
                            mState = USER_REMOVED;
                            break;
                        case EMError.USER_LOGIN_ANOTHER_DEVICE:
                            mState = USER_LOGIN_ANOTHER_DEVICE;
                            break;
                        default:
                            if (NetUtils.hasNetwork(mContext)) {
                                mState = CONTECTED_CHAT_SERVER_ERROR;
                            }
                            break;
                    }
                    for (ConnectionObserver observer : mConnectionObservers) {
                        observer.onConnectionUpdated(mState);
                    }
                }
            });
        }

        @Override
        public void onConnected() {
            Utils.logD(TAG, "EMConnection connected success");
            mState = EMCHAT_CONNECTED;
            mHandlerConnected.post(new Runnable() {
                @Override
                public void run() {
                    for (ConnectionObserver observer : mConnectionObservers) {
                        observer.onConnectionUpdated(EMCHAT_CONNECTED);
                    }
                }
            });
        }
    };

    public interface ConnectionObserver {
        void onConnectionUpdated(int state);
    }
}
