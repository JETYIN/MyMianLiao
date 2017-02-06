package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class GroupInfo implements Parcelable{
    public static final String TABLE_NAME = "group_info";

    public static final String ID = "_id";
    public static final String JID = "jid";
    public static final String GROUP_NAME = "groupName";

    public long id;
    public String jid;
    public String groupName;
    public int count;
    public String myNick;
    public int adminUid;
    public String target;
    public long groupId;

    public GroupInfo() {
    }

    public GroupInfo(String groupId, String groupName, long chatId){
        this.jid = groupId;
        this.groupName = groupName;
        target = chatId + "@groupchat.dev.tjut.cc";
    }

    public GroupInfo(Parcel source) {
        jid = source.readString();
        groupName = source.readString();
        adminUid = source.readInt();
        count = source.readInt();
        myNick = source.readString();
    }

    public static final JsonUtil.ITransformer<GroupInfo> TRANSFORMER =
            new JsonUtil.ITransformer<GroupInfo>() {

                @Override
                public GroupInfo transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GroupInfo> CREATOR =
            new Creator<GroupInfo>() {

                @Override
                public GroupInfo[] newArray(int size) {
                    return new GroupInfo[size];
                }

                @Override
                public GroupInfo createFromParcel(Parcel source) {
                    return new GroupInfo(source);
                }
            };

    protected static GroupInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.groupId = json.optInt("id");
        groupInfo.jid = json.optString("chat_id");
        groupInfo.groupName = json.optString("name");
        groupInfo.adminUid = json.optInt("admin_uid");
        groupInfo.count = json.optInt("member_count");
        groupInfo.myNick = json.optString("my_nick");
        return groupInfo;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jid);
        dest.writeString(groupName);
        dest.writeInt(adminUid);
        dest.writeInt(count);
        dest.writeString(myNick);
    }
}
