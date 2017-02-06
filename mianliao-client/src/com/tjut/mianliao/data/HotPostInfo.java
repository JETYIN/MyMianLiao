package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.JsonUtil.ITransformer;

public class HotPostInfo implements Parcelable {

    public int id;
    public int listOrder;
    public int threadId;
    public int upCount;
    public int userId;
    public int userGender;
    public long createTime;
    public String title;
    public String cover;
    public String forum;
    public String userNick;
    public String userAvatar;
    
    public HotPostInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(listOrder);
        dest.writeInt(threadId);
        dest.writeInt(upCount);
        dest.writeInt(userId);
        dest.writeInt(userGender);
        dest.writeLong(createTime);
        dest.writeString(title);
        dest.writeString(cover);
        dest.writeString(forum);
        dest.writeString(userNick);
        dest.writeString(userAvatar);
    }

    public HotPostInfo(Parcel in) {
        id = in.readInt();
        listOrder = in.readInt();
        threadId = in.readInt();
        upCount = in.readInt();
        userId = in.readInt();
        userGender = in.readInt();
        createTime = in.readLong();
        title = in.readString();
        cover = in.readString();
        forum = in.readString();
        userNick = in.readString();
        userAvatar = in.readString();
    }

    public static final JsonUtil.ITransformer<HotPostInfo> TRANSFORMER = 
            new ITransformer<HotPostInfo>() {

        @Override
        public HotPostInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Creator<HotPostInfo> CREATOR = new Creator<HotPostInfo>() {

        @Override
        public HotPostInfo[] newArray(int size) {
            return new HotPostInfo[size];
        }

        @Override
        public HotPostInfo createFromParcel(Parcel source) {
            return new HotPostInfo(source);
        }
    };

    public static HotPostInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        HotPostInfo mTpInfo = new HotPostInfo();
        mTpInfo.id = json.optInt("id");
        mTpInfo.listOrder = json.optInt("list_order");
        mTpInfo.threadId = json.optInt("thread_id");
        mTpInfo.upCount = json.optInt("up_count");
        mTpInfo.userId = json.optInt("user_id");
        mTpInfo.userGender = json.optInt("user_gender");
        mTpInfo.createTime = json.optLong("created_time") * 1000;
        mTpInfo.title = json.optString("title");
        mTpInfo.cover = json.optString("image");
        mTpInfo.forum = json.optString("forum");
        mTpInfo.userNick = json.optString("user_nick");
        mTpInfo.userAvatar = json.optString("user_avatar");
        return mTpInfo;
    }
}
