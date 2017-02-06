package com.tjut.mianliao.xmpp;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMChatManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
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
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.AliOSSHelper;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.xmpp.ConnectionManager.ConnectionObserver;

/**
 * Helper class to finish chat related operations.
 */
public class ChatHelper {

    private static  final  String TAG = "ChatHelper";

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
    private AliOSSHelper mAliOSSHelper;
    private FileDownloader mFileDownloader;
    private SharedPreferences mPreferences;
    private GroupChatManager mGroupChatManager;

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
        mReceiveListeners = new CopyOnWriteArrayList<MessageReceiveListener>();
        mSendListeners = new CopyOnWriteArrayList<MessageSendListener>();
        mChatManager = EMClient.getInstance().chatManager();
        mChatManager.addMessageListener(mMessageListener);
        mAliOSSHelper = AliOSSHelper.getInstance(context);
        mFileDownloader = FileDownloader.getInstance(context);
        mMyAccount = AccountInfo.getInstance(context).getAccount();
        mConnectionManager = ConnectionManager.getInstance(context);
        mConnectionManager.registerConnectionObserver(mConnectionObserver);
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

    public void clear() {
        mReceiveListeners.clear();
        mSendListeners.clear();
        mConnectionManager.unregisterConnectionObserver(mConnectionObserver);
        mGroupChatManager.unregisterGroupChatListener(mGroupChatListener);
        mChatManager.removeMessageListener(mMessageListener);
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
            case ChatRecord.CHAT_TYPE_CHAT_MSG:
            case ChatRecord.CHAT_TYPE_NORMAL_GIFT:
            case ChatRecord.CHAT_TYPE_SPECIAL_GIFT:
            case ChatRecord.CHAT_TYPE_SHARE_MSG:
            case ChatRecord.CHAT_TYPE_FOLLOW_MSG:
            case ChatRecord.CHAT_TYPE_DANMU_MSG:
            case ChatRecord.CHAT_TYPE_PRAISE_MSG:
                return false;

            case ChatRecord.CHAT_TYPE_SHARE_REQUEST:
                return !isSharingLoc();

            case ChatRecord.CHAT_TYPE_SHARE_OVER:
                return isNotSharingLoc();

            default:
                return true;
        }
    }

    public void sendText(String target, String text, String groupId) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_TEXT, target);
        record.groupId = groupId;
        record.text = text;
        record.isGroupChat = !TextUtils.isEmpty(groupId);
        new SendMessageTask(record, true).executeLong();
    }

    public void sendText(String target, String text, String groupId, int msgType) {
        ChatRecord record = createRecord(msgType, target);
        record.groupId = groupId;
        record.text = text;
        record.isGroupChat = !TextUtils.isEmpty(groupId);
        new SendMessageTask(record, true).executeLong();
    }


    public void sendEmtionText(String target, String text, String groupId) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_BIG_EMOTION, target);
        record.groupId = groupId;
        record.text = text;
        record.isGroupChat = !TextUtils.isEmpty(groupId);
        new SendMessageTask(record, true).executeLong();
    }
    
    public void sendPicture(String target, String filePath, String groupId) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_PICTURE, target);
        record.groupId = groupId;
        record.filePath = filePath;
        record.isGroupChat = !TextUtils.isEmpty(groupId);
        record.text = mContext.getResources().getString(R.string.picture_message);
        new SendMessageTask(record, true).executeLong();
    }

    public void sendVoice(String target, String filePath, int voiceLength, String groupId) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_VOICE, target);
        record.groupId = groupId;
        record.filePath = filePath;
        record.voiceLength = voiceLength;
        record.isGroupChat = !TextUtils.isEmpty(groupId);
        record.text = mContext.getResources().getString(R.string.voice_message);
        new SendMessageTask(record, true).executeLong();
    }

    public void sendLocation(String target, double longitude, double latitude, String address, String groupId) {
        ChatRecord record = createRecord(ChatRecord.CHAT_TYPE_LOCATION, target);
        record.groupId = groupId;
        record.longitude = longitude;
        record.latitude = latitude;
        record.address = address;
        record.isGroupChat = !TextUtils.isEmpty(groupId);
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
        record.text = "";
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
        record.text = "isChatState";
        record.isChatState = true;
        record.isComposing = isComposing;
        record.isComposingVoice = isVoice;
        new SendMessageTask(record, false).executeLong();
    }

    public boolean sendMessage(ChatRecord record) {
        if (mChatManager != null) {
            EMMessage message = createMessage(record);
            if (message == null) {
                return false;
            }
            mChatManager.sendMessage(message);
            return true;
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

    private ChatRecord createRecord(EMMessage message) {
        Utils.logD(TAG, message.getFrom() + "," + message.toString());
        ChatRecord record = new ChatRecord();
        EMMessage msg = message;
        boolean isChatState = message.getBooleanAttribute("isChatState", false);
        if (isChatState) {
            record.isChatState = true;
            record.isComposing = message.getBooleanAttribute("isComposing", false);
            record.isComposingVoice = message.getBooleanAttribute("voice", false);
        }
        record.groupId = message.getStringAttribute("groupId", "");
        String chatId = message.getStringAttribute("chatId", "");
        if (!TextUtils.isEmpty(chatId)) {
            record.isGroupChat = true;
            record.from = message.getFrom() + "@" + Utils.getChatServerDomain();
            record.target = chatId + "@groupchat." + Utils.getChatServerDomain();
        } else {
            record.isGroupChat = false;
            record.from = message.getFrom() + "@" + Utils.getChatServerDomain();
            record.target = message.getFrom() + "@" + Utils.getChatServerDomain();
        }
        EMMessageBody body = message.getBody();
        if (body != null) {
            if (body instanceof EMTextMessageBody) {
                record.text = ((EMTextMessageBody) body).getMessage();
            }
        }
        Utils.logD(TAG, "body:" + message.getBody().toString());
        long msgTime = message.getMsgTime();
        record.timestamp = msgTime == 0 ? System.currentTimeMillis() : msgTime;

        record.type = message.getIntAttribute("type_id", 0);
        record.url = message.getStringAttribute(ChatRecord.URL, "");
        if (TextUtils.isEmpty(record.url)) {
            record.url = message.getStringAttribute("message_fileUrl", "");
        }
        record.voiceLength = message.getIntAttribute(ChatRecord.VOICE_LENGTH, 0);
        if (record.voiceLength == 0) {
            record.voiceLength = message.getIntAttribute("message_voiceLength", 0);
        }
        record.longitude = Double.parseDouble(message.getStringAttribute(ChatRecord.LONGITUDE, "0"));
        if (record.longitude == 0) {
            record.longitude = Double.parseDouble(message.getStringAttribute("message_longitude", "0"));
        }
        record.latitude = Double.parseDouble(message.getStringAttribute(ChatRecord.LATITUDE, "0"));
        if (record.latitude == 0) {
            record.latitude = Double.parseDouble(message.getStringAttribute("message_latitude", "0"));
        }
        record.address = Utils.urlDecode(message.getStringAttribute(ChatRecord.ADDRESS, ""));
        if (TextUtils.isEmpty(record.address)) {
            record.address = Utils.urlDecode(message.getStringAttribute("message_adress", ""));
        }
        // add by 4.1.0
        record.giftId = message.getIntAttribute("gift_id", 0);
        record.giftCount = message.getIntAttribute("gift_count" ,0);
        record.animType = message.getIntAttribute("animation_type", 0);
        Utils.logD(TAG, "animation type :" + record.animType);
        record.activityId = message.getStringAttribute("activity_id", "");
        record.silenceUserId = message.getIntAttribute("uid", 0);
        return record;
    }

    private EMMessage createMessage(ChatRecord record) {
        Utils.logD(TAG, "send : target = " + record.target + ", from" + record.from + ",text" + record.text);
        String target = StringUtils.parseName(record.target);
        String text = record.text;
        EMMessage message = EMMessage.createTxtSendMessage(text, target);
        if (message == null) {
            return null;
        }
        if (record.isGroupChat) {
            message.setChatType(ChatType.GroupChat);
        }
        if (record.isChatState) {
            message.setAttribute("isChatState", true);
            message.setAttribute("isComposing", record.isComposing);
            message.setAttribute("voice", record.isComposingVoice);
        }
        if (record.isGroupChat) {
            message.setAttribute("chatId", target);
            message.setAttribute("groupId", record.groupId);
        }
        message.setAttribute("type_id", record.type);
        if (!TextUtils.isEmpty(record.url)) {
            message.setAttribute(ChatRecord.FILE_KEY, record.fileKey);
            message.setAttribute(ChatRecord.URL, record.url);
        }
        message.setAttribute(ChatRecord.VOICE_LENGTH, record.voiceLength);
        message.setAttribute(ChatRecord.LONGITUDE, String.valueOf(record.longitude));
        message.setAttribute(ChatRecord.LATITUDE, String.valueOf(record.latitude));
        if (!TextUtils.isEmpty(record.address)) {
            message.setAttribute(ChatRecord.ADDRESS, Utils.urlEncode(record.address));
        }
        return message;
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

    private ConnectionObserver mConnectionObserver = new ConnectionObserver() {
        @Override
        public void onConnectionUpdated(int state) {
            switch (state) {
                case ConnectionManager.EMCHAT_CONNECTED:
                    break;
                case ConnectionManager.EMCHAT_DISCONNECTED:
                case ConnectionManager.CONTECTED_CHAT_SERVER_ERROR:
                case ConnectionManager.USER_LOGIN_ANOTHER_DEVICE:
                case ConnectionManager.USER_REMOVED:
                    break;
                default:
                    break;
            }
        }
    };

    private EMMessageListener mMessageListener = new EMMessageListener() {
        
        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            for (EMMessage message : messages) {
                final ChatRecord record = createRecord(message);
                if (record != null)
                    Utils.logD(TAG, "received message : " + record.from + "," + record.target + ",type:" + record.type);
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
                            case ChatRecord.CHAT_TYPE_DANMU_MSG:
                            case ChatRecord.CHAT_TYPE_NORMAL_GIFT:
                            case ChatRecord.CHAT_TYPE_SPECIAL_GIFT:
                            case ChatRecord.CHAT_TYPE_CHAT_MSG:
                            case ChatRecord.CHAT_TYPE_PRAISE_MSG:
                            case ChatRecord.CHAT_TYPE_FOLLOW_MSG:
                            case ChatRecord.CHAT_TYPE_SHARE_MSG:
                            case ChatRecord.CHAT_TYPE_SYS_MSG:
                            case ChatRecord.CHAT_TYPE_COMEIN_MSG:
                            case ChatRecord.CHAT_TYPE_EXIT_MSG:
                                saveToDB = false;
                                break;
                            default:
                                saveToDB = !record.isChatState;
                                break;
                        }
                        new ReceiveMessageTask(record, saveToDB).executeLong();
                    }
                });
            }
        }
        
        @Override
        public void onMessageReadAckReceived(List<EMMessage> arg0) {
            
        }
        
        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> arg0) {
            
        }
        
        @Override
        public void onMessageChanged(EMMessage arg0, Object arg1) {
            
        }
        
        @Override
        public void onCmdMessageReceived(List<EMMessage> arg0) {
            
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
                            GroupInfo groupInfo = new GroupInfo(String.valueOf(ucp.id), ucp.name, ucp.chatId);
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
    private EMChatManager mChatManager;

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
            this.mSaveToDB = saveToDB;
        }

        @Override
        protected void onPreExecute() {
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

}
