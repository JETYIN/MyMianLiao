package com.tjut.mianliao.data.cache;

import com.tjut.mianliao.data.ChannelInfo;

public class CacheChannelInfo extends ChannelInfo{

    public static final String TABLE_NAME = "cache_channel_info";

    public static final int TYPE_CHANNEL_OFFCIAL_DAY = 1;
    public static final int TYPE_CHANNEL_OFFCIAL_NIGHT = 2;
    public static final int TYPE_CHANNEL_USER_NIGHT = 3;
    public static final int TYPE_CHANNEL_LATEST = 4;
    
    public static final String ID = "_id";
    public static final String FORUM_ID = "forumId";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String INTRO = "intro";
    public static final String ICON = "icon";
    public static final String BG_IMG = "bgImg";
    public static final String RULE_ICON = "ruleIcon";
    public static final String RULE_TITLE = "ruleTitle";
    public static final String RULE_CONTENT = "ruleContent";
    public static final String THREAD_TYPE = "threadType";
    public static final String HAVA_RULE = "havaRule";
    public static final String STYLE = "style";
    public static final String DAY_TYPE = "dayType";
    
    public int _id;
    /**
     * 该字段是用来判断对应的频道是白天还是黑夜/官方还是用户推荐
     */
    public int dayType;
    
    public CacheChannelInfo(ChannelInfo info) {
        forumId = info.forumId;
        name = info.name;
        type = info.type;
        title = info.title;
        intro = info.intro;
        icon = info.icon;
        bgImg = info.bgImg;
        ruleIcon = info.ruleIcon;
        ruleTitle = info.ruleTitle;
        ruleContent = info.ruleContent;
        threadType = info.threadType;
        havaRule = info.havaRule;
        style = info.style;
    }
}
