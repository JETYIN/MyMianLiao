package com.tjut.mianliao;

import android.content.Context;

import com.tencent.android.tpush.XGPushManager;
import com.tjut.mianliao.chat.UnreadMessageHelper;
import com.tjut.mianliao.contact.ContactUpdateCenter;
import com.tjut.mianliao.contact.SubscriptionHelper;
import com.tjut.mianliao.contact.UserEntryManager;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.curriculum.CourseManager;
import com.tjut.mianliao.curriculum.search.CourseSearchManager;
import com.tjut.mianliao.curriculum.widget.CourseWidgetHelper;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.news.NewsManager;
import com.tjut.mianliao.notice.NoticeManager;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.BitmapLoader;
import com.tjut.mianliao.util.EmotionManager;
import com.tjut.mianliao.util.FaceManager;
import com.tjut.mianliao.util.FileDownloader;
import com.tjut.mianliao.util.LocationHelper;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.xmpp.ChatHelper;
import com.tjut.mianliao.xmpp.ConnectionManager;

public class LoginStateHelper {

    private LoginStateHelper() {}

    public static void xmppInit(Context context) {
        ConnectionManager.getInstance(context);
        UserEntryManager.getInstance(context);
        UnreadMessageHelper.getInstance(context);
        SubscriptionHelper.getInstance(context);
        ChatHelper.getInstance(context);
    }

    public static void xmppExit(Context context) {
//        ChatHelper.getInstance(context).exit();
        ConnectionManager.getInstance(context).exit();
    }

    public static void accountLogin(Context context, String account, String token, UserInfo me) {
        AccountInfo accountInfo = AccountInfo.getInstance(context);

        if (accountInfo.isUserChanged(me)) {
            clear(context);
        }
        accountInfo.login(account, token, me.userId);
        xmppInit(context);
        UserInfoManager.getInstance(context).addUserInfo(me);
        XGPushManager.registerPush(context.getApplicationContext(), account.toLowerCase());
    }

    public static void accountLogout(Context context) {
        AccountInfo.getInstance(context).logout();
        xmppExit(context);
        XGPushManager.registerPush(context.getApplicationContext(), "*");
    }

    public static void clear(Context context) {
        DataHelper.cleanAllData(context);
        ContactUpdateCenter.clearObservers();
        FileDownloader.destroyInstance();
        BitmapLoader.getInstance().clear();
        RedDot.getInstance().clear();
        Settings.getInstance(context).clear();
        CourseManager.getInstance(context).clear();
        CourseSearchManager.getInstance(context).clear();
        CourseWidgetHelper.updateWidget(context);
        NewsManager.getInstance(context).clear();
        FaceManager.getInstance(context).clear();
        LocationHelper.getInstance(context).clear();
        SnsHelper.getInstance().clear();
        UserState.getInstance().clear();
        EmotionManager.getInstance(context).clear();
        NotificationHelper.getInstance(context).clear();
        NoticeManager.getInstance(context).clear();
        AlarmHelper.getInstance(context).clear();
        ConnectionManager.getInstance(context).clear();
        SubscriptionHelper.getInstance(context).clear();
        UnreadMessageHelper.getInstance(context).clear();
        ChatHelper.getInstance(context).clear();
        UserRemarkManager.getInstance(context).clear();
        UserInfoManager.getInstance(context).clear();
        UserEntryManager.getInstance(context).clear();
        IMResourceManager.getInstance(context).clear();
    }
    
}
