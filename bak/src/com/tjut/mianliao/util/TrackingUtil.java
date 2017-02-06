package com.tjut.mianliao.util;

import java.util.Properties;

import android.content.Context;

import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.forum.CfPost;

public class TrackingUtil {

    private static final int SESSION_TIMEOUT_MS = 30 * 60 * 1000;

    private static final String FORUM_TAB_CLICK = "forum_tab_click";
    private static final String NEWS_TAB_CLICK = "news_tab_click";

    private static final String CLICK_ID = "click_id";

    private TrackingUtil() {
    }

    public static void init(Context context) {
        StatConfig.setEnableStatService(Utils.getMtaEnabled());
        StatConfig.setDebugEnable(Utils.isDebug());
        StatConfig.setAutoExceptionCaught(!Utils.isDebug());
        StatConfig.setSessionTimoutMillis(SESSION_TIMEOUT_MS);
    }

    public static void trackBeginPage(Context context) {
        StatService.onResume(context);
    }

    public static void trackEndPage(Context context) {
        StatService.onPause(context);
    }

    public static void trackBeginPage(Context context, String name) {
        StatService.trackBeginPage(context, name);
    }

    public static void trackEndPage(Context context, String name) {
        StatService.trackEndPage(context, name);
    }

    public static void trackForumTabClick(Context context, String clickId) {
        Properties prop = new Properties();
        prop.setProperty(CLICK_ID, clickId);
        StatService.trackCustomKVEvent(context, FORUM_TAB_CLICK, prop);
    }

    public static void trackForumTabClickTopright(Context context) {
        trackForumTabClick(context, "TOPRIGHT");
    }

    public static void trackForumTabClickBanner(Context context, CfPost post) {
        String clickId = new StringBuilder("BANNER: ").append(post.postId).toString();
        trackForumTabClick(context, clickId);
    }

    public static void trackForumTabClickPop(Context context, CfPost post) {
        String clickId = new StringBuilder("POP: ").append(post.postId).toString();
        trackForumTabClick(context, clickId);
    }

    public static void trackForumTabClickForum(Context context, Forum forum) {
        String clickId = new StringBuilder().append(forum.isPublic()
                ? "FORUM_PUB: " : "FORUM_SCH: ").append(forum.name).toString();
        trackForumTabClick(context, clickId);
    }

    public static void trackNewsTabClick(Context context, String clickId) {
        Properties prop = new Properties();
        prop.setProperty(CLICK_ID, clickId);
        StatService.trackCustomKVEvent(context, NEWS_TAB_CLICK, prop);
    }

    public static void trackNewsTabClickTopright(Context context) {
        trackNewsTabClick(context, "TOPRIGHT");
    }

    public static void trackNewsTabClickBanner(Context context, News news) {
        String clickId = new StringBuilder("BANNER: ").append(news.id).toString();
        trackNewsTabClick(context, clickId);
    }

    public static void trackNewsTabClickNews(Context context, News news) {
        String clickId = new StringBuilder("NEWS: ").append(news.id).toString();
        trackNewsTabClick(context, clickId);
    }
}
