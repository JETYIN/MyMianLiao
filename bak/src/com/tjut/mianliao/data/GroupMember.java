package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.JsonUtil;

public class GroupMember {

    public String nickName;
    public UserInfo userInfo;

    public static final JsonUtil.ITransformer<GroupMember> TRANSFORMER=
            new JsonUtil.ITransformer<GroupMember>() {
        @Override
        public GroupMember transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public GroupMember() {
    }

    public GroupMember(String nickName, UserInfo userInfo) {
        this.nickName = nickName;
        this.userInfo = userInfo;
    }

    public static final GroupMember fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        GroupMember member = new GroupMember();
        member.userInfo = UserInfo.fromJson(json);
        member.nickName = member.userInfo.nickname;
        member.userInfo.nickname = null;
        return member;
    }
}