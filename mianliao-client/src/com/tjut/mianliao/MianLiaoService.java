package com.tjut.mianliao;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.tjut.mianliao.xmpp.ConnectionManager;

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
        }
    };
}
