package com.tjut.mianliao.xmpp.call;

import org.jivesoftware.smack.packet.IQ;

/**
 * CallSemaphore, used to communicate and start a voice/video call.
 */
public class CS extends IQ {
    public static final String ELEMENT = "cs";
    public static final String NAME_SPACE = "tjut:ml:cs";
    public static final String CALL_ID = "callid";

    private String mCallId;

    public CS() {
    }

    public CS(String from, String to) {
        setPacketID(getPacketID());
        setFrom(from);
        setTo(to);
        setType(IQ.Type.GET);
    }

    @Override
    public String getChildElementXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<" + ELEMENT + " xmlns='" + NAME_SPACE + "' ");
        if (mCallId != null) {
            sb.append(CALL_ID + "='" + mCallId + "' ");
        }
        sb.append("/>");
        return sb.toString();
    }

    public static CS resposeFor(CS request, String callId) {
        CS response = new CS();
        response.setPacketID(request.getPacketID());
        response.setFrom(request.getTo());
        response.setTo(request.getFrom());
        response.setType(IQ.Type.RESULT);
        response.mCallId = callId;
        return response;
    }

    public String getCallId() {
        return mCallId;
    }

    public void setCallId(String callId) {
        mCallId = callId;
    }
}
