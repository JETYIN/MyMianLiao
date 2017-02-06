package com.tjut.mianliao.xmpp;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.DelayInformation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.GroupInfo;
import com.tjut.mianliao.data.LatLngWrapper;
import com.tjut.mianliao.data.UserGroupMap;
import com.tjut.mianliao.data.notice.NoticeSummary;
import com.tjut.mianliao.im.GroupChatManager;
import com.tjut.mianliao.im.GroupChatManager.GroupChatListener;
import com.tjut.mianliao.notice.NoticeManager;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.AliOSSHelper;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ConnectionManager.ConnectionObserver;

/**
 * Helper class to finish chat related operations.
 */
public class ChatHelper {

    private static final String DELAY_INFORMATION = "jabber:x:delay";
    private static final String CHAT_STATE_XMLNS = "http://jabber.org/protocol/chatstates";

    private static final String SP_SHARE_LOC_BY_ME = "share_loc_by_me";
    private static final String SP_SHARE_LOC_BY_OTHER = "share_loc_by_other";
    private static final String SP_SHARE_LOC_LATITUDE = "share_loc_latitude";
    private static final String SP_SHARE_LOC_LONGITUDE = "share_loc_longitude";

    private static WeakReference<ChatHelper> sInstanceRef;

    private Context mContext;
    private Handler mHandler;
    private ConnectionManager mConnectionManager;
    private ChatManager mChatManager;
    private AliOSSHelper mAliOSSHelper;
    private FileDownloader mFileDownloader;
    private SharedPreferences mPreferences;
    private GroupChatManager mGroupChatManager;
    private NoticeManager mNoticeManager;

    private ConcurrentHashMap<String, Chat> mActiveChats;
    private List<MessageReceiveListener> mReceiveListeners;
    private List<MessageSendListener> mSendListeners;
    private String mMyAccount;
    private boolean mShareLocByMe, mShareLocByOther;
    private LatLngWrapper mLatLngWrapper;

    public static synchronized ChatHelper getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        ChatHelper instance = new ChatHelper(context);
        sInstanceRef = new WeakReference<ChatHelper>(instance);
        return instance;
    }

    private ChatHelper(Context context) {
        mContext = context.getApplicationContext();
        mHandler = new Handler(Looper.getMainLooper());
        mActiveChats = new ConcurrentHashMap<String, Chat>();
        mReceiveListeners = new CopyOnWriteArrayList<MessageReceiveListener>();
        mSendListeners = new CopyOnWriteArrayList<MessageSendListener>();
        mAliOSSHelper = AliOSSHelper.getInstance(context);
        mFileDownloader = FileDownloader.getInstance(context);
        mMyAccount = AccountInfo.getInstance(context).getAccount();
        mConnectionManager = ConnectionManager.getInstance(context);
        mConnectionManager.registerConnectionObserver(mConnectionObserver);
        mNoticeManager = NoticeManager.getInstance(context);
        mPreferences = DataHelper.getSpForData(context);
        loadShareLocStatus();
        mGroupChatManager = GroupChatManager.getInstance(context);
        mGroupChatManager.registerGroupChatListener(mGroupChatListener);
        mGroupChatManager.getMyGroupList();
    }

    public void registerReceiveListener(MessageReceiveListener listener) {
        if (listener != null && !mReceiveListeners.contains(listener)) {
            mReceiveListeners.add(listener);
        }
    }

    public void registerSendListener(MessageSendListener listener) {
        if (listener != null && !mSendListeners.contains(listener)) {
            mSendListeners.add(listener);
        }
    }

    public void unregisterReceiveListener(MessageReceiveListener listener) {
        mReceiveListeners.remove(listener);
    }

    public void unregisterSendListener(MessageSendListener listener) {
        mSendListeners.remove(listener);
    }

    public void exit() {
        for (Chat chat : mActiveChats.values()) {
            chat.removeMessageListener(mMessageListener);
        }
    }

    public void clear() {
        mActiveChats.clear();
        mReceiveListeners.clear();
        mSendListeners.clear();
        mConnectionManager.unregisterConnectionObserver(mConnectionObserver);
        mGroupChatManager.unregisterGroupChatListener(mGroupChatListener);
        sInstanceRef.clear();
    }

    public LatLngWrapper getLatLngWrapper() {
        return mLatLngWrapper;
    }

    public boolean isSharingLoc() {
        return mShareLocByMe && mShareLocByOther;
    }

    public boolean isNotSharingLoc() {
        return !mShareLocByMe && !mShareLocByOther;
    }

    public boolean processRecord(ChatRecord record) {
        switch (record.type) {
            case ChatRecord.CHAT_TYPE_SHARE_RESPONSE:
                return false;

            case ChatRecord.CHAT_TYPE_SHARE_REQUEST:
                return !isSharingLoc();

            case ChatRecord.CHAT_TYPE_SHARE_OVER:
                return isNotSharingLoc();

            default:
                return true;
        }
    }

    public void sendText(String target, String text, boolean isGoupChat) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_TEXT, target);
        record.text = text;
        record.isGroupChat = isGoupChat;
        new SendMessageTask(record, true).executeLong();
    }

    public void sendEmtionText(String target, String text, boolean isGoupChat) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_BIG_EMOTION, target);
        record.text = text;
        record.isGroupChat = isGoupChat;
        new SendMessageTask(record, true).executeLong();
    }
    
    public void sendPicture(String target, String filePath, boolean isGoupChat) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_PICTURE, target);
        record.filePath = filePath;
        record.isGroupChat = isGoupChat;
        record.text = mContext.getResources().getString(R.string.picture_message);
        new SendMessageTask(record, true).executeLong();
    }

    public void sendVoice(String target, String filePath, float voiceLength, boolean isGoupChat) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_VOICE, target);
        record.filePath = filePath;
        record.voiceLength = voiceLength;
        record.isGroupChat = isGoupChat;
        record.text = mContext.getResources().getString(R.string.voice_message);
        new SendMessageTask(record, true).executeLong();
    }

    public void sendLocation(String target, double longitude, double latitude, String address, boolean isGoupChat) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_LOCATION, target);
        record.longitude = longitude;
        record.latitude = latitude;
        record.address = address;
        record.isGroupChat = isGoupChat;
        record.text = mContext.getResources().getString(R.string.location_message);
        new SendMessageTask(record, true).executeLong();
    }

    public void sendShareLocStart(String target) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_SHARE_REQUEST, target);
        record.text = mContext.getResources().getString(R.string.share_location_message);
        mShareLocByMe = true;
        saveShareLocStatus();
        new SendMessageTask(record, !mShareLocByOther).executeLong();
    }

    public void sendShareLocLatLng(String target, double latitude, double longitude) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_SHARE_RESPONSE, target);
        record.longitude = longitude;
        record.latitude = latitude;
        new SendMessageTask(record, false).executeLong();
    }

    public void sendShareLocStop(String target) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_SHARE_OVER, target);
        record.text = mContext.getResources().getString(R.string.share_location_over);
        mShareLocByMe = false;
        saveShareLocStatus();
        new SendMessageTask(record, !mShareLocByOther).executeLong();
    }

    public void sendChatState(String target, boolean isComposing, boolean isVoice) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_TEXT, target);
        record.isChatState = true;
        record.isComposing = isComposing;
        record.isComposingVoice = isVoice;
        new SendMessageTask(record, false).executeLong();
    }

    public boolean sendMessage(ChatRecord record) {
        Chat chat = mActiveChats.get(record.target);
        if (chat != null) {
            try {
                chat.sendMessage(createMessage(record));
                return true;
            } catch (XMPPException e) {
            } catch (IllegalStateException e) {
            }
        }
        return false;
    }

    private ChatRecord createRecord(int type, String target) {
        ChatRecord record = new ChatRecord();
        record.type = type;
        record.target = target;
        record.from = mMyAccount;
        record.timestamp = System.currentTimeMillis();
        return record;
    }

    private ChatRecord createRecord(Message message) {
        ChatRecord record = new ChatRecord();

        PacketExtension pe = message.getExtension(CHAT_STATE_XMLNS);
        if (pe != null) {
            record.isChatState = true;
            record.isComposing = ChatState.composing.toString().equals(
                    ((ChatStateExtension) pe).getElementName());
            record.isComposingVoice = Boolean.parseBoolean(message.getAtrribute("voice"));
        }

        String groupId = message.getAtrribute("groupId");
        if (groupId != null) {
            record.isGroupChat = true;
            record.from = StringUtils.parseName(message.getFrom()) + "@"
                    + Utils.getChatServerDomain();
            record.target = groupId + "@groupchat." + Utils.getChatServerDomain();
        } else {
            record.isGroupChat = false;
            record.from = StringUtils.parseResource(message.getFrom());
            record.target = StringUtils.parseBareAddress(message.getFrom());
        }
        record.text = Utils.urlDecode(message.getBody());

        pe = message.getExtension(DELAY_INFORMATION);
        record.timestamp = pe == null ? System.currentTimeMillis()
                : ((DelayInformation) pe).getStamp().getTime();

        Object obj = message.getProperty(ChatRecord.TYPE);
        if (obj instanceof Integer) {
            record.type = (Integer) obj;
        }
        obj = message.getProperty(ChatRecord.URL);
        if (obj instanceof String) {
            record.url = (String) obj;
        }
        obj = message.getProperty(ChatRecord.VOICE_LENGTH);
        if (obj instanceof Float) {
            record.voiceLength = (Float) obj;
        }
        obj = message.getProperty(ChatRecord.LONGITUDE);
        if (obj instanceof Double) {
            record.longitude = (Double) obj;
        }
        obj = message.getProperty(ChatRecord.LATITUDE);
        if (obj instanceof Double) {
            record.latitude = (Double) obj;
        }
        obj = message.getProperty(ChatRecord.ADDRESS);
        if (obj instanceof String) {
            record.address = Utils.urlDecode((String) obj);
        }

        record.isNightRecord = Settings.getInstance(mContext).isNightMode();
        return record;
    }

    private Message createMessage(ChatRecord record) {
        Message message = new Message();
        if (record.isChatState) {
            ChatStateExtension extension = new ChatStateExtension(
                    record.isComposing ? ChatState.composing : ChatState.paused);
            message.addExtension(extension);
            message.setAttribute("voice", String.valueOf(record.isComposingVoice));
        }
        if (record.isGroupChat) {
            String groupId = StringUtils.parseName(record.target);
            message.setAttribute("groupId", groupId);
        }
        message.setProperty(ChatRecord.TYPE, record.type);
        if (!TextUtils.isEmpty(record.url)) {
            message.setProperty(ChatRecord.FILE_KEY, record.fileKey);
            message.setProperty(ChatRecord.URL, record.url);
        }
        message.setProperty(ChatRecord.VOICE_LENGTH, record.voiceLength);
        message.setProperty(ChatRecord.LONGITUDE, record.longitude);
        message.setProperty(ChatRecord.LATITUDE, record.latitude);
        if (!TextUtils.isEmpty(record.address)) {
            message.setProperty(ChatRecord.ADDRESS, Utils.urlEncode(record.address));
        }

        message.setBody(Utils.urlEncode(record.text));
        return message;
    }

    private void addChatListener() {
        mChatManager = mConnectionManager.getConnection().getChatManager();
        if (mChatManager != null) {
            mChatManager.addChatListener(mChatManagerListener);
        }
    }

    private void removeChatListener() {
        if (mChatManager != null) {
            mChatManager.removeChatListener(mChatManagerListener);
        }
    }

    private void loadShareLocStatus() {
        mShareLocByMe = mPreferences.getBoolean(SP_SHARE_LOC_BY_ME, false);
        mShareLocByOther = mPreferences.getBoolean(SP_SHARE_LOC_BY_OTHER, false);
        String lat = mPreferences.getString(SP_SHARE_LOC_LATITUDE, null);
        String lng = mPreferences.getString(SP_SHARE_LOC_LONGITUDE, null);
        if (lat != null && lng != null) {
            try {
                mLatLngWrapper = new LatLngWrapper(
                        Double.parseDouble(lat), Double.parseDouble(lng));
            } catch (NumberFormatException e) {
            }
        }
    }

    private void saveShareLocStatus() {
        Editor editor = mPreferences.edit();
        editor.putBoolean(SP_SHARE_LOC_BY_ME, mShareLocByMe)
                .putBoolean(SP_SHARE_LOC_BY_OTHER, mShareLocByOther);
        if (mLatLngWrapper != null) {
            String lat = String.valueOf(mLatLngWrapper.latLng.latitude);
            String lng = String.valueOf(mLatLngWrapper.latLng.longitude);
            editor.putString(SP_SHARE_LOC_LATITUDE, lat)
                    .putString(SP_SHARE_LOC_LONGITUDE, lng);
        }
        editor.commit();
    }

    private ChatManagerListener mChatManagerListener = new ChatManagerListener() {
        @Override
        public void chatCreated(Chat chat, boolean createdLocally) {
            String target = StringUtils.parseBareAddress(chat.getParticipant());
            Chat oldChat = mActiveChats.put(target, chat);
            if (oldChat != null) {
                oldChat.removeMessageListener(mMessageListener);
            }
            chat.addMessageListener(mMessageListener);
        }
    };

    private ConnectionObserver mConnectionObserver = new ConnectionObserver() {
        @Override
        public void onConnectionUpdated(int state) {
            switch (state) {
                case ConnectionManager.XMPP_CONNECTED:
                    removeChatListener();
                    addChatListener();
//                    updateNoticeInfo(mNoticeManager.getSummaries());
                    break;
                case ConnectionManager.XMPP_DISCONNECTED:
                    removeChatListener();
                    break;
                default:
                    break;
            }
        }
    };

    private MessageListener mMessageListener = new MessageListener() {
        @Override
        public void processMessage(Chat chat, Message message) {
            // Only monitor a real chat!
            if (message.getType() != Message.Type.chat) {
                return;
            }
            String nameFrom = StringUtils.parseName(message.getFrom());
            if (nameFrom.equals(mMyAccount.toLowerCase())) {
                return;
            }
            final ChatRecord record = createRecord(message);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    boolean saveToDB;
                    switch (record.type) {
                        case ChatRecord.CHAT_TYPE_SHARE_REQUEST:
                            mShareLocByOther = true;
                            saveShareLocStatus();
                            saveToDB = !mShareLocByMe;
                            break;

                        case ChatRecord.CHAT_TYPE_SHARE_RESPONSE:
                            mLatLngWrapper = new LatLngWrapper(record.latitude, record.longitude);
                            saveShareLocStatus();
                            saveToDB = false;
                            break;

                        case ChatRecord.CHAT_TYPE_SHARE_OVER:
                            mShareLocByOther = false;
                            saveShareLocStatus();
                            saveToDB = !mShareLocByMe;
                            break;

                        default:
                            saveToDB = !record.isChatState;
                            break;
                    }
                    new ReceiveMessageTask(record, saveToDB).executeLong();
                }
            });
        }
    };

    private GroupChatListener mGroupChatListener = new GroupChatListener() {

        @Override
        public void onGroupChatSuccess(int type, Object obj) {
            switch (type) {
                case GroupChatManager.GET_GROUP_LIST:
                    @SuppressWarnings("unchecked")
                    ArrayList<UserGroupMap> ucps = (ArrayList<UserGroupMap>) obj;
                    if (ucps != null) {
                        for (UserGroupMap ucp : ucps) {
                            GroupInfo groupInfo = new GroupInfo(String.valueOf(ucp.id), ucp.name);
                            if (DataHelper.loadGroupInfo(mContext, String.valueOf(ucp.id)) != null) {
                                DataHelper.updateGroupInfo(mContext, groupInfo);
                            } else {
                                DataHelper.insertGroupInfo(mContext, groupInfo);
                            }
                        }
                    }
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onGroupChatFailed(int type) {
        }
    };

    private class ReceiveMessageTask extends AdvAsyncTask<Void, Void, Boolean>
            implements FileDownloader.Callback {
        private ChatRecord mChatRecord;
        private boolean mSaveToDB;

        public ReceiveMessageTask(ChatRecord record, boolean saveToDB) {
            mChatRecord = record;
            mSaveToDB = saveToDB;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return !mSaveToDB || (mChatRecord.hasId()
                    ? DataHelper.updateChatRecord(mContext, mChatRecord)
                    : DataHelper.insertChatRecord(mContext, mChatRecord));
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (mChatRecord.needDownload()) {
                    mFileDownloader.getFile(mChatRecord.url, this, true);
                } else {
                    for (MessageReceiveListener listener : mReceiveListeners) {
                        listener.onMessageReceived(mChatRecord);
                    }
                }
            } else {
                for (MessageReceiveListener listener : mReceiveListeners) {
                    listener.onMessageReceiveFailed(mChatRecord);
                }
            }
        }

        @Override
        public void onResult(boolean success, String url, String fileName) {
            if (TextUtils.equals(mChatRecord.url, url)) {
                if (success) {
                    mChatRecord.filePath = fileName;
                    new ReceiveMessageTask(mChatRecord, mSaveToDB).executeLong();
                } else {
                    for (MessageReceiveListener listener : mReceiveListeners) {
                        listener.onMessageReceiveFailed(mChatRecord);
                    }
                }
            }
        }
    }

    private class SendMessageTask extends AdvAsyncTask<Void, Void, Boolean>
            implements AliOSSHelper.OnUploadListener {
        private ChatRecord mChatRecord;
        private boolean mSaveToDB;

        public SendMessageTask(ChatRecord record, boolean saveToDB) {
            mChatRecord = record;
            mChatRecord.isNightRecord = Settings.getInstance(mContext).isNightMode();
            this.mSaveToDB = saveToDB;
        }

        @Override
        protected void onPreExecute() {
            if (mActiveChats.get(mChatRecord.target) == null && mChatManager != null) {
                mChatManager.createChat(mChatRecord.target, null);
            }
            if (mSaveToDB && !mChatRecord.hasId()) {
                for (MessageSendListener listener : mSendListeners) {
                    listener.onMessagePreSend(mChatRecord);
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = !mSaveToDB || (mChatRecord.hasId()
                    ? DataHelper.updateChatRecord(mContext, mChatRecord)
                    : DataHelper.insertChatRecord(mContext, mChatRecord));
            if (result && !mChatRecord.needUpload()) {
                result = sendMessage(mChatRecord);
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (mChatRecord.needUpload()) {
                    File file = new File(mChatRecord.filePath);
                    switch (mChatRecord.type) {
                        case ChatRecord.CHAT_TYPE_PICTURE:
                            mAliOSSHelper.uploadImage(file, this);
                            break;
                        case ChatRecord.CHAT_TYPE_VOICE:
                            mAliOSSHelper.uploadVoice(file, this);
                            break;
                        default:
                            mAliOSSHelper.uploadFile(file, this);
                            break;
                    }
                } else {
                    for (MessageSendListener listener : mSendListeners) {
                        listener.onMessageSent(mChatRecord);
                    }
                }
            } else {
                for (MessageSendListener listener : mSendListeners) {
                    listener.onMessageSendFailed(mChatRecord);
                }
            }
        }

        @Override
        public void onUploadSuccess(File file, byte[] data, String url, String objectKey) {
            if (TextUtils.equals(mChatRecord.filePath, file.getAbsolutePath())) {
                mChatRecord.url = url;
                mChatRecord.fileKey = objectKey;
                new SendMessageTask(mChatRecord, mSaveToDB).executeLong();
            }
        }

        @Override
        public void onUploadProgress(File file, byte[] data, int byteCount, int totalSize) {
        }

        @Override
        public void onUploadFailure(File file, byte[] data, String errMsg) {
            for (MessageSendListener listener : mSendListeners) {
                listener.onMessageSendFailed(mChatRecord);
            }
        }
    }

    public interface MessageReceiveListener {
        public void onMessageReceived(ChatRecord record);

        public void onMessageReceiveFailed(ChatRecord record);
    }

    public interface MessageSendListener {
        public void onMessagePreSend(ChatRecord record);

        public void onMessageSent(ChatRecord record);

        public void onMessageSendFailed(ChatRecord record);
    }

    protected void updateNoticeInfo(List<NoticeSummary> summaries) {
        for (NoticeSummary summary : summaries) {
            if (summary.count > 0) {
                createRecord(summary);
            }
        }
    }

    private void createRecord(NoticeSummary summary) {
        ChatRecord chatRecord = new ChatRecord();
    }
}
