package com.tjut.mianliao.xmpp.call;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

public class CSProvider implements IQProvider {

    @Override
    public IQ parseIQ(XmlPullParser parser) throws Exception {
        CS callSemaphore = new CS();
        String callId = parser.getAttributeValue(null, CS.CALL_ID);
        callSemaphore.setCallId(callId);
         while (true) {
             if (parser.next() == XmlPullParser.END_TAG) {
                break;
            }
        }
        return callSemaphore;
    }

}
