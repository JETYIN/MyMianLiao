package com.tjut.mianliao.data.cache;

import com.tjut.mianliao.data.contact.UserInfo;

public class CacheUserInfo extends UserInfo{

    public static final String TABLE_NAME = "cache_user_info";
    
    public static final String ID = "_id";
    public static final String AVATAR_NIGHT = "avatarNight";
    public static final String NICK_NIGHT = "nickNight";
    
    public String avatarNight;
    public String nickNight;
    
    public int id;
}
