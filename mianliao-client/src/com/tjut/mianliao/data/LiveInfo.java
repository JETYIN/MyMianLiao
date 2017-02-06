package com.tjut.mianliao.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by YoopWu on 2016/6/21 0021.
 */
public class LiveInfo implements Parcelable{

    public static final int STATU_REPLAY = 2;
    public static final int STATU_LIVING = 1;
    public static final int STATU_LIVE_END = 0;

    public int user_gender;

    public int connection_price;
    public int member_numbers;

    public int getUser_gender() {
        return user_gender;
    }

    public void setUser_gender(int user_gender) {
        this.user_gender = user_gender;
    }

    public int getConnection_price() {
        return connection_price;
    }

    public void setConnection_price(int connection_price) {
        this.connection_price = connection_price;
    }

    public int getMember_numbers() {
        return member_numbers;
    }

    public void setMember_numbers(int member_numbers) {
        this.member_numbers = member_numbers;
    }

    public int getOwn_type() {
        return own_type;
    }

    public void setOwn_type(int own_type) {
        this.own_type = own_type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getPrevUrl() {
        return prevUrl;
    }

    public void setPrevUrl(String prevUrl) {
        this.prevUrl = prevUrl;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getUv() {
        return uv;
    }

    public void setUv(String uv) {
        this.uv = uv;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isChoosed() {
        return choosed;
    }

    public void setChoosed(boolean choosed) {
        this.choosed = choosed;
    }

    public int own_type;

    public int id;
    public int uid;
    public String activityId;
    public String title;
    public long  ctime;
    public String nick;
    public String avatar;
    public int gender;
    public String prevUrl;
    public String school;
    public String uv;
    public int type;
    public int status;
    public boolean choosed;

    public LiveInfo() {}

    public static final JsonUtil.ITransformer<LiveInfo> TRANSFORMER =
            new JsonUtil.ITransformer<LiveInfo>(){
                @Override
                public LiveInfo transform(JSONObject json) {
                    return fromJson(json);
                }


            };

    public static LiveInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        LiveInfo live = new LiveInfo();
        live.own_type=json.optInt("own_type");
        live.member_numbers=json.optInt("member_numbers");
        live.connection_price=json.optInt("connection_price");
        live.user_gender=json.optInt("user_gender");
        live.id = json.optInt("id");
        live.uid = json.optInt("uid");
        live.activityId = json.optString("activity_id");
        live.title = json.optString("title");
        live.ctime = json.optLong("ctime");
        live.nick = json.optString("user_nick");
        live.avatar = json.optString("user_avatar");
        live.gender = json.optInt("gender");
        live.school = json.optString("user_school");
        live.prevUrl = json.optString("prev_url");
        live.uv = json.optString("uv");
        live.type = json.optInt("type");
        live.status = json.optInt("status");
        return live;
    }

    public static final Creator<LiveInfo> CREATOR =
            new Creator<LiveInfo>() {
                @Override
                public LiveInfo createFromParcel(Parcel source) {
                    return new LiveInfo(source);
                }

                @Override
                public LiveInfo[] newArray(int size) {
                    return new LiveInfo[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(own_type);
        dest.writeInt(member_numbers);
        dest.writeInt(connection_price);
        dest.writeInt(user_gender);
        dest.writeInt(id);
        dest.writeInt(uid);
        dest.writeString(activityId);
        dest.writeString(title);
        dest.writeLong(ctime);
        dest.writeString(nick);
        dest.writeString(avatar);
        dest.writeInt(gender);
        dest.writeString(school);
        dest.writeString(prevUrl);
        dest.writeString(uv);
        dest.writeInt(type);
        dest.writeInt(status);
    }

    public LiveInfo(Parcel source) {
        own_type=source.readInt();
        member_numbers=source.readInt();
        connection_price=source.readInt();
        user_gender=source.readInt();
        id = source.readInt();
        uid = source.readInt();
        activityId = source.readString();
        title = source.readString();
        ctime = source.readLong();
        nick = source.readString();
        avatar = source.readString();
        gender = source.readInt();
        school = source.readString();
        prevUrl = source.readString();
        uv = source.readString();
        type = source.readInt();
        status = source.readInt();
    }
}
