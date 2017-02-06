package com.tjut.mianliao;

import org.jivesoftware.smack.provider.ProviderManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.tjut.mianliao.xmpp.ConnectionManager;
import com.tjut.mianliao.xmpp.call.CS;
import com.tjut.mianliao.xmpp.call.CSManager;
import com.tjut.mianliao.xmpp.call.CSProvider;

/**
 * This service runs in background to keep connection with chat server. So it
 * should be started if a user is logged in, and should be stopped on
 * quit/logout.
 */
public class MianLiaoService extends Service {

    private ConnectionManager mConnectionManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mConnectionManager = ConnectionManager.getInstance(this);

        CheckinHelper.setupAutoCheckin(this);
        registerReceiver(mConnectivityReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        // Init my own xmpp providers.
        CSManager.init();
        CSManager.getInstance().setConnection(mConnectionManager.getConnection());
        ProviderManager pm = ProviderManager.getInstance();
        pm.addIQProvider(CS.ELEMENT, CS.NAME_SPACE, new CSProvider());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        throw new UnsupportedOperationException("Binding not supported!");
    }

    @Override
    public void onDestroy() {
        CheckinHelper.disableAutoCheckin(this);
        unregisterReceiver(mConnectivityReceiver);
        super.onDestroy();
    }

    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Make sure to connect to XMPP server when there's a network connection.
            mConnectionManager.confirmConnect();
        }
    };
}
