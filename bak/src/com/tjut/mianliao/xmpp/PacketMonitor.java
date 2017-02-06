package com.tjut.mianliao.xmpp;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ping.packet.Ping;

import com.tjut.mianliao.util.Utils;

public class PacketMonitor {
    private static final String TAG = "PacketMonitor";

    static {
        Connection.addConnectionCreationListener(new ConnectionCreationListener() {
            @Override
            public void connectionCreated(Connection connection) {
                if (Utils.isDebug()) {
                    monitorAllPacket(connection);
                }
            }
        });
    }

    private PacketMonitor() {}

    public static void monitorAllPacket(Connection connection) {
        // Monitor incoming packet
        PacketFilter filter = new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
                return true;
            }
        };

        connection.addPacketListener(new PacketListener() {
            @Override
            public void processPacket(Packet packet) {
                logPacketInfo(packet, false);
            }
        }, filter);

        connection.addPacketInterceptor(new PacketInterceptor() {
            @Override
            public void interceptPacket(Packet packet) {
                logPacketInfo(packet, true);
            }
        }, filter);
    }

    private static void logPacketInfo(Packet packet, boolean out) {
        StringBuilder sb = new StringBuilder();

        if (packet instanceof Ping) {
            sb.append("PING ");
        } else if (packet instanceof IQ) {
            sb.append("IQ ");
        } else if (packet instanceof Message) {
            sb.append("MESSAGE ");
        }

        if (out) {
            sb.append("OUT ");
        } else {
            sb.append("IN ");
        }
        sb.append("From: ");
        sb.append(packet.getFrom());
        sb.append(", To: ");
        sb.append(packet.getTo());
        sb.append(", Detail: ");
        sb.append(packet.toXML());

        Utils.logD(TAG, sb.toString());
    }
}
