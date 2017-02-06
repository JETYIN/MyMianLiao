package com.tjut.mianliao.chat;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Vibrator;
import android.text.TextUtils;

import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RedDot;
import com.tjut.mianliao.RedDot.RedDotType;
import com.tjut.mianliao.XGMessageReceiver;
import com.tjut.mianliao.XGMessageReceiver.OnPublicNumPushListener;
import com.tjut.mianliao.XGMessageReceiver.OnTaskFinishPushListener;
import com.tjut.mianliao.contact.SubscriptionHelper;
import com.tjut.mianliao.contact.SubscriptionHelper.NewFriendsRequestListener;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.push.PushMessage;
import com.tjut.mianliao.forum.nova.MessageRemindManager;
import com.tjut.mianliao.forum.nova.MessageRemindManager.MessageRemindListener;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.StringUtils;
import com.tjut.mianliao.xmpp.ChatHelper;

public class UnreadMessageHelper implements ChatHelper.MessageReceiveListener,
        OnPublicNumPushListener, MessageRemindListener, NewFriendsRequestListener,
        OnTaskFinishPushListener {

    private static final String SHARED_PREFS_NAME = "unread_message";
    private static final String SP_UNREAD_INFO = "unread_info";
    
    public static final String TARGET_REPLY_AT = "target_reply_at";
    public static final String TARGET_HATE_LIKE = "target_hate_like";
    public static final String TARGET_ADD_FRIEND = "target_add_friend";
    public static final String TARGET_SYS_INFO = "target_sys_info";

    private static final long VIBRATE_MILLIS = 250;

    private static WeakReference<UnreadMessageHelper> sInstanceRef;

    private Context mContext;
    private ChatHelper mChatHelper;
    private NotificationHelper mNotificationHelper;
    private SubscriptionHelper mSubscriptionHelper;
    
    private String mMessageTarget;
    private HashMap<String, NumUnread> mUnreadMessages;
    private CopyOnWriteArrayList<String> mSubscriptions;

    public static synchronized UnreadMessageHelper getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }
        UnreadMessageHelper instance = new UnreadMessageHelper(context);
        sInstanceRef = new WeakReference<UnreadMessageHelper>(instance);
        return instance;
    }

    private UnreadMessageHelper(Context context) {
        mContext = context.getApplicationContext();
        mUnreadMessages = new HashMap<String, NumUnread>();
        mSubscriptions = new CopyOnWriteArrayList<>();
        mNotificationHelper = NotificationHelper.getInstance(context);
        loadNewRequestData();
        loadData();
        mChatHelper = ChatHelper.getInstance(context);
        mChatHelper.registerReceiveListener(this);
        XGMessageReceiver.registerOnPublicNumListener(this);
        XGMessageReceiver.registerOnTaskFinishPushListener(this);
        MessageRemindManager.registerMessageRemindListener(this);
        mSubscriptionHelper = SubscriptionHelper.getInstance(context);
        mSubscriptionHelper.registerNewFriendsRequestListener(this);
    }

    public void clear() {
        clearData();
        mUnreadMessages.clear();
        mChatHelper.unregisterReceiveListener(this);
        sInstanceRef.clear();
    }

    public void setMessageTarget(String target) {
        mMessageTarget = target;
        if (!TextUtils.isEmpty(target)) {
            if (mUnreadMessages.remove(target) != null) {
                saveData();
            }
            mNotificationHelper.clearChatNotification();
        }
    }
    
    public void deleteChat(String target) {
        if (target == null) {
            return;
        }
        DataHelper.deleteChatRecords(mContext, target);
        if (mUnreadMessages.remove(target) != null) {
            saveData();
        }
    }

    public int getCount(String target) {
        if (target == null) {
            return 0;
        }
        NumUnread nu = mUnreadMessages.get(target);
        return nu == null ? 0 : nu.count;
    }

    public void updateRedDot() {
        RedDot.getInstance().update(RedDotType.CHAT, getTotalCount());
    }
    
    @Override
    public void onMessageReceived(ChatRecord record) {
        if (record.isChatState || !mChatHelper.processRecord(record) || isLiveMessage(record)) {
            return;
        }

        if (record.isTarget(mMessageTarget)) {
            Settings settings = Settings.getInstance(mContext);
            AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            if (settings.allowNewMessageVibrate()
                    && (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT)) {
                ((Vibrator) mContext.getSystemService(
                        Context.VIBRATOR_SERVICE)).vibrate(VIBRATE_MILLIS);
            }
        } else {
            NumUnread nu = mUnreadMessages.get(record.target);
            if (nu == null) {
                nu = new NumUnread();
                mUnreadMessages.put(record.target, nu);
            }
            nu.count++;
            saveData();
            sendNotification(record);
        }
    }

    @Override
    public void onMessageReceiveFailed(ChatRecord record) {
    }

    /**
     * Load data from shared preference.
     */
    private void loadData() {
        SharedPreferences prefs = mContext.getSharedPreferences(SHARED_PREFS_NAME, 0);
        String unreads = prefs.getString(SP_UNREAD_INFO, "");
        if (unreads.length() > 0) {
            for (String unread : unreads.split(";")) {
                String[] us = unread.split(":");
                if (us.length != 2) {
                    continue;
                }
                String account = us[0];
                NumUnread nu = new NumUnread();
                try {
                    nu.count = Integer.parseInt(us[1]);
                    mUnreadMessages.put(account, nu);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        updateRedDot();
    }
    
    private void loadNewRequestData() {
        String record = DataHelper.getSpForData(mContext)
                .getString(SubscriptionHelper.SP_SUBSCRIPTION_RECORDS, "");
        if (TextUtils.isEmpty(record)) {
            return;
        }
        String[] records = record.split(";");
        for (int i = 0; i < records.length; i++) {
            if (!TextUtils.isEmpty(records[i])) {
                mSubscriptions.add(records[i]);
                checkUnreadMessage(TARGET_ADD_FRIEND);
            }
        }
        
    }

    /**
     * Save data to shared preference.
     */
    private void saveData() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, NumUnread> entry: mUnreadMessages.entrySet()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue().count);
        }

        mContext.getSharedPreferences(SHARED_PREFS_NAME, 0).edit()
                .putString(SP_UNREAD_INFO, sb.toString())
                .commit();

        updateRedDot();
    }
    
    private void clearData() {
        mContext.getSharedPreferences(SHARED_PREFS_NAME, 0).edit().clear().commit();
    }

    public int getTotalCount() {
        int result = 0;
        for (NumUnread nu : mUnreadMessages.values()) {
            result += nu.count;
        }
        return result;
    }

    private boolean isLiveMessage(ChatRecord record) {
        return record.groupId != null && record.groupId.contains("live");
    }

    private void sendNotification(ChatRecord record) {
        UserInfo user = UserInfoManager.getInstance(mContext).getUserInfo(record.target);
        String name = user == null
                ? StringUtils.parseName(record.target) : user.getDisplayName(mContext);
        String content = new StringBuilder(name).append(": ").append(record.text).toString();
        String title = mContext.getString(R.string.notify_chat, getTotalCount());
        mNotificationHelper.sendChatNotification(title, content, record);
    }

    public static class NumUnread {
        int count;
    }

    @Override
    public void OnPublicNumPush(OfficialAccountInfo info) {
        checkUnreadMessage(getPublicNumTarget(info.id));
    }
    
    public static String getPublicNumTarget(long id) {
        if (id <= 0) {
            return null;
        }
        return id + "@public_news";
    }

    @Override
    public void hasNewMessage(String target) {
        checkUnreadMessage(target);
    }

    @Override
    public void onNewRequest(String target) {
        if (target == null || mSubscriptions.contains(target)) {
           return; 
        }
        checkUnreadMessage(TARGET_ADD_FRIEND);
        mSubscriptions.add(target);
    }

    public void removeSubRequestTarget(String target) {
        if (TextUtils.isEmpty(target)) {
            return;
        }
        mSubscriptions.remove(target);
    }
    
    @Override
    public void onTaskFinished(PushMessage mesage) {
        checkUnreadMessage(TARGET_SYS_INFO);
    }

    private void checkUnreadMessage(String target) {
        checkUnreadMessage(target, false);
    }
    
    private void checkUnreadMessage(String target, boolean single){
        NumUnread nu = mUnreadMessages.get(target);
        if (nu == null) {
            nu = new NumUnread();
            mUnreadMessages.put(target, nu);
        }
        if (!single) {
            nu.count++;
        }
        saveData();
    }
}
