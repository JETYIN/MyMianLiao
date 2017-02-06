package com.tjut.mianliao.live;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by j_hao on 2016/7/16.
 */
public class LiveRank implements Parcelable {
    public int uid;
    public String name;
    public String nick;
    public String avatar;
    public String school;
    public int amount;

    public static final Creator<LiveRank> CREATOR = new Creator<LiveRank>() {
        @Override
        public LiveRank createFromParcel(Parcel in) {
            return new LiveRank(in);
        }

        @Override
        public LiveRank[] newArray(int size) {
            return new LiveRank[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public static final JsonUtil.ITransformer<LiveRank> TRANSFORMER =
            new JsonUtil.ITransformer<LiveRank>() {
                @Override
                public LiveRank transform(JSONObject json) {
                    return fromJson(json);
                }


            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeString(name);
        dest.writeString(nick);
        dest.writeString(school);
        dest.writeString(avatar);
        dest.writeInt(amount);
    }

    public LiveRank() {
    }

    public LiveRank(Parcel source) {
        uid = source.readInt();
        amount = source.readInt();
        name = source.readString();
        nick = source.readString();
        school = source.readString();
        avatar = source.readString();
    }

    public static LiveRank fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        LiveRank liverank = new LiveRank();
        liverank.uid = json.optInt("uid");
        liverank.avatar = json.optString("avatar");
        liverank.name = json.optString("name");
        liverank.school = json.optString("school");
        liverank.amount = json.optInt("amount");
        liverank.nick = json.optString("nick");

        return liverank;
    }
}
