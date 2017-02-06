package com.tjut.mianliao.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by Silva on 2016/6/28.
 */
//
//        uid: int(主播uid)
//        nick: string
//        gender: int
//        is_follow_host:boolean(true关注了该主播)
//        avatar:string
//        school:string(学校名称)
public class HostUserInfo implements Parcelable {

    public int uid;
    public int gender;
    public String nickName;
    public String avatar;
    public String school;
    public boolean isFollow;
    public int wheat;
    public int gold;
    public int followNum;


    public HostUserInfo () {}

    protected HostUserInfo(Parcel in) {
        uid = in.readInt();
        gender = in.readInt();
        nickName = in.readString();
        avatar = in.readString();
        school = in.readString();
        isFollow = in.readInt() == 1;
        wheat = in.readInt();
        gold = in.readInt();
        followNum = in.readInt();
    }

    public static final JsonUtil.ITransformer<HostUserInfo> TRANSFORMER =
            new JsonUtil.ITransformer<HostUserInfo>() {

                @Override
                public HostUserInfo transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    public static final Creator<HostUserInfo> CREATOR = new Creator<HostUserInfo>() {
        @Override
        public HostUserInfo createFromParcel(Parcel in) {
            return new HostUserInfo(in);
        }

        @Override
        public HostUserInfo[] newArray(int size) {
            return new HostUserInfo[size];
        }
    };

    public static HostUserInfo fromJson (JSONObject json) {
        HostUserInfo userInfo  = new HostUserInfo();
        userInfo.uid = json.optInt("uid");
        userInfo.gender = json.optInt("gender");
        userInfo.nickName = json.optString("nick");
        userInfo.avatar = json.optString("avatar");
        userInfo.school = json.optString("school");
        userInfo.isFollow = json.optBoolean("is_follow_host");
        userInfo.wheat = json.optInt("credit");
        userInfo.gold = json.optInt("coin");
        userInfo.followNum = json.optInt("follow_count");
        return userInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(uid);
        parcel.writeInt(gender);
        parcel.writeString(nickName);
        parcel.writeString(avatar);
        parcel.writeString(school);
        parcel.writeInt(isFollow ? 1 : 0);
        parcel.writeInt(wheat);
        parcel.writeInt(gold);
        parcel.writeInt(followNum);
    }


}
