package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class UserGroupMap {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String ADMIN_UID = "admin_uid";
    public static final String MEMBERCOUNT = "member_count";
    public static final String MY_NICK = "my_nick";

    public int id;
    public String name;
    public int adminUid;
    public int memberCount;
    public String myNickName;

    public static final JsonUtil.ITransformer<UserGroupMap> TRANSFORMER=
            new JsonUtil.ITransformer<UserGroupMap>() {
        @Override
        public UserGroupMap transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public UserGroupMap() {
    }

    public UserGroupMap(UserGroupMap ucp) {
        id = ucp.id;
        name = ucp.name;
        adminUid = ucp.adminUid;
        memberCount = ucp.memberCount;
        myNickName = ucp.myNickName;
    }

    public static final UserGroupMap fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        UserGroupMap ucp = new UserGroupMap();
        ucp.id = json.optInt(ID);
        ucp.name = json.optString(NAME);
        ucp.adminUid = json.optInt(ADMIN_UID);
        ucp.memberCount = json.optInt(MEMBERCOUNT);
        ucp.myNickName = json.optString(MY_NICK);
        return ucp;
    }
}

