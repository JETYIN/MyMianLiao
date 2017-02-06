package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import com.tjut.mianliao.chat.UnreadMessageHelper;
import com.tjut.mianliao.data.InboxMessage;
import com.tjut.mianliao.util.Utils;


public class MessageRemindManager {
    
    public enum MessageType{
        TYPE_AT_USER, TYPE_NOTICE
    }
    
    private static ArrayList<MessageRemindListener> mListeners = new ArrayList<>();
    
    public static void hasNewMessage(int type) {
        for (MessageRemindListener listener : mListeners) {
            listener.hasNewMessage(getTargerByType(type));
        }
    }
    
    public static String getTargerByType(int type) {
        switch (type) {
            case InboxMessage.COMMENT_FORUM_REPLY_AT:
            case InboxMessage.COMMENT_FORUM_THREAD_AT:
            case InboxMessage.COMMENT_REPLIED_AT:
            case InboxMessage.REPLY_COMMENT:
            case InboxMessage.REPLY_POST:
                return UnreadMessageHelper.TARGET_REPLY_AT;
            case InboxMessage.HATE_POST:
            case InboxMessage.LIKE_COMMENT:
            case InboxMessage.LIKE_POST:
                return UnreadMessageHelper.TARGET_HATE_LIKE;
            default:
                return "";
        }
    }
    
    public static void registerMessageRemindListener(MessageRemindListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }
    
    public static void unregisterMessageRemindListener(MessageRemindListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
    }
    
    public interface MessageRemindListener{
        void hasNewMessage(String target);
    }
}
