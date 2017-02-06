package com.tjut.mianliao.xmpp.call;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import com.tjut.mianliao.util.Utils;

public class CSManager {
    private static final String TAG = "CSManager";

    private static final int DEFAULT_CS_TIMEOUT = 10000; // milliseconds

    private Set<CSListener> mCSListeners = new CopyOnWriteArraySet<CSListener>();

    // Use weak reference, because the lifecycle of connection is much shorter
    // than CSManager.
    private WeakReference<Connection> mConnectionRef = new WeakReference<Connection>(null);

    private static CSManager sInstance = new CSManager();

    private CSManager() {}

    public static CSManager getInstance() {
        return sInstance;
    }

    /**
     * Subscribe connection status
     */
    public static void init() {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            public void connectionCreated(Connection connection) {
                sInstance.setConnection(connection);
            }
        });
    }

    public synchronized void setConnection(Connection con) {
        Connection connection = mConnectionRef.get();
        if (con == null) {
            return;
        } else if (connection != con) {
            Utils.logD(TAG, "Connection set!");
            mConnectionRef = new WeakReference<Connection>(con);
            con.addPacketListener(new PacketListener() {
                @Override
                public void processPacket(Packet packet) {
                    CS cs = (CS) packet;
                    if (cs.getType() == IQ.Type.GET) {
                        for (CSListener listener : mCSListeners) {
                            listener.onCSRequest((CS) packet);
                        }
                    }
                }
            }, new PacketTypeFilter(CS.class));
        }
    }

    /**
     * @param request
     * @param callId
     *            null or empty string means user unavailable.
     * @return true if response is sent.
     */
    public boolean respondCSRequest(CS request, String callId) {
        Connection connection = mConnectionRef.get();
        if (connection == null || !connection.isAuthenticated()) {
            return false;
        }
        CS response = CS.resposeFor(request, callId);
        connection.sendPacket(response);
        return true;
    }

    public CS requestCall(String from, String to) {
        Connection connection = mConnectionRef.get();
        if (connection == null || !connection.isAuthenticated()) {
            return null;
        }
        CS request = new CS(from, to);
        PacketCollector collector = connection.createPacketCollector(
                new PacketIDFilter(request.getPacketID()));
        connection.sendPacket(request);
        CS response = (CS) collector.nextResult(DEFAULT_CS_TIMEOUT);
        collector.cancel();
        return response;
    }

    public void addCSListener(CSListener listener) {
        if (listener != null) {
            mCSListeners.add(listener);
        }
    }

    public void removeCSListener(CSListener listener) {
        mCSListeners.remove(listener);
    }

    public interface CSListener {
        /**
         * Usually it's called from a non main thread.
         * 
         * @param cs
         */
        void onCSRequest(CS cs);
    }
}
