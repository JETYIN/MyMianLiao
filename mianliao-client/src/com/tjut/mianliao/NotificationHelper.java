package com.tjut.mianliao;

import java.lang.ref.WeakReference;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.tjut.mianliao.chat.ChatActivity;
import com.tjut.mianliao.contact.NewContactActivity;
import com.tjut.mianliao.contact.SystemNotifyInfoActivity;
import com.tjut.mianliao.curriculum.CurriculumActivity;
import com.tjut.mianliao.data.ChatRecord;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.data.notice.NoticeSummary;
import com.tjut.mianliao.forum.TopicPostActivity;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.mycollege.MemoDetailActivity;
import com.tjut.mianliao.news.NewsListActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.tribe.TribeDetailActivity;
import com.tjut.mianliao.util.Utils;

public class NotificationHelper {

    public enum NotificationType {
        DEFAULT,
        COURSE,
        NOTICE,
        CHAT,
        MEMO,
        POST_RECOMMEND,
        WEB_ADV,
        TOPIC_RECOMMEND,
        TRIBE_RECOMMEND
    }

    private static WeakReference<NotificationHelper> sInstanceRef;

    private Context mContext;
    private Settings mSettings;
    private NotificationManager mNotificationManager;

    public static synchronized NotificationHelper getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }

        NotificationHelper instance = new NotificationHelper(context);
        sInstanceRef = new WeakReference<NotificationHelper>(instance);
        return instance;
    }

    private NotificationHelper(Context context) {
        mContext = context.getApplicationContext();
        mSettings = Settings.getInstance(context);
        mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void clear() {
        mNotificationManager.cancelAll();
        sInstanceRef.clear();
    }

    public void sendDefaultNotification(String title, String content) {
        Intent iMain = new Intent(mContext, MainActivity.class);
        sendNotification(NotificationType.DEFAULT, iMain, title, content);
    }

    public void sendCourseNotification(String title, String content) {
        Intent iCourse = new Intent(mContext, CurriculumActivity.class);
        sendNotification(NotificationType.COURSE, iCourse, title, content);
    }

    public void sendMemoNotification(String title, String content, NoteInfo mNotes) {
    	Intent iMemo  = new Intent(mContext, MemoDetailActivity.class);
    	iMemo.putExtra(MemoDetailActivity.EXT_MEMO_NOTE, mNotes);
    	sendNotification(NotificationType.MEMO, iMemo, title, content);
    }
    
    public void sendNoticeNotification(String title, String content, int subzone) {
        Intent iNotice = NoticeSummary.getIntent(mContext, subzone);
        sendNotification(NotificationType.NOTICE, iNotice, title, content);
    }

    public void sendChatNotification(String title, String content, ChatRecord record) {
        Intent iChat = new Intent(mContext, ChatActivity.class)
                .putExtra(ChatActivity.EXTRA_CHAT_TARGET, record.target)
                .putExtra(ChatActivity.EXTRA_CHAT_ISGOUPCHAT, record.isGroupChat);
        sendNotification(NotificationType.CHAT, iChat, title, content, record.timestamp);
    }
    
    public void sendNewFriendNotification(String title, String content, ChatRecord record) {
        Intent iChat = new Intent(mContext, NewContactActivity.class);
        sendNotification(NotificationType.NOTICE, iChat, title, content, record.timestamp);
    }
    
    public void sendPublicNumNotification(String title, String content, ChatRecord record) {
        Intent iNews = new Intent(mContext, NewsListActivity.class);
        iNews.putExtra(NewsListActivity.EXTRA_ID, record.publicId);
        iNews.putExtra(NewsListActivity.EXTRA_NAME, record.from);
        sendNotification(NotificationType.NOTICE, iNews, title, content, record.timestamp);
    }
    
    public void sendSystemNoticeNotification(String title, String content, ChatRecord record) {
        Intent iChat = new Intent(mContext, SystemNotifyInfoActivity.class);
        sendNotification(NotificationType.NOTICE, iChat, title, content, record.timestamp);
    }
    
    public void sendPostRecommendNotification(String title, String content, int postId) {
        Intent pIntent = new Intent(mContext, ForumPostDetailActivity.class);
        pIntent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA_ID, postId);
        sendNotification(NotificationType.POST_RECOMMEND, pIntent, title, content);
    }
    
    public void sendWebAdvNotification(String title, String content, int advId) {
        Intent waIntent = new Intent(Intent.ACTION_VIEW);
        Uri uri = TextUtils.isEmpty(getAdUrl(advId)) ? null : Uri.parse(getAdUrl(advId));
        waIntent.setDataAndType(uri, null);
        sendNotification(NotificationType.WEB_ADV, waIntent, title, content);
    }
    
    public void sendTopicRecommendNotification(String title, String content, int topId) {
        Intent tIntent = new Intent(mContext, TopicPostActivity.class);
        tIntent.putExtra(TopicPostActivity.TOPIC_ID, topId);
        sendNotification(NotificationType.TOPIC_RECOMMEND, tIntent, title, content);
    }
    
    public void sendTirbeRecommendNotification(String title, String content, int tribeId) {
        Intent trIntent = new Intent(mContext, TribeDetailActivity.class);
        trIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, tribeId);
        sendNotification(NotificationType.TOPIC_RECOMMEND, trIntent, title, content);
    }
    
    public void clearChatNotification() {
        clearNotification(NotificationType.CHAT);
    }

    private void sendNotification(NotificationType type,
            Intent intent, String title, String content) {
        sendNotification(type, intent, title, content, System.currentTimeMillis());
    }

    private void sendNotification(NotificationType type,
            Intent intent, String title, String content, long when) {
        PendingIntent pi = PendingIntent.getActivity(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (TextUtils.isEmpty(title)) {
            title = mContext.getString(R.string.app_name);
        }

        Notification notification = new NotificationCompat.Builder(mContext)
                .setContentIntent(pi)
                .setContentTitle(title)
                .setContentText(content)
                .setWhen(when)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .build();
        if (!mSettings.isInNightMode()) {
            if (mSettings.allowNewMessageSound()) {
                notification.defaults |= Notification.DEFAULT_SOUND;
            }
            if (mSettings.allowNewMessageVibrate()) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }
        }
        mNotificationManager.notify(type.name(), type.ordinal(), notification);
    }

    public void clearNotification(NotificationType type) {
        mNotificationManager.cancel(type.name(), type.ordinal());
    }
    
    private String getAdUrl(int advId) {
        return Utils.getServerAddress()+"webapp/view/splash?id=" + advId;
    }
}
