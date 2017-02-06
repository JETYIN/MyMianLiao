package com.tjut.mianliao.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by YoopWu on 2016/7/7 0007.
 */
public class LiveTopic implements Parcelable {

    public int id;
    public String color;
    public String name;

    public LiveTopic() {
    }

    public static final JsonUtil.ITransformer<LiveTopic> TRANSFORMER =
            new JsonUtil.ITransformer<LiveTopic>() {
                @Override
                public LiveTopic transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    public static final Creator<LiveTopic> CREATOR =
            new Creator<LiveTopic>() {
                @Override
                public LiveTopic createFromParcel(Parcel source) {
                    return new LiveTopic(source);
                }

                @Override
                public LiveTopic[] newArray(int size) {
                    return new LiveTopic[size];
                }
            };

    private static LiveTopic fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        LiveTopic info = new LiveTopic();
        info.id = json.optInt("id");
        info.color = json.optString("color");
        info.name = json.optString("name");
        return info;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public LiveTopic(Parcel source) {
        id = source.readInt();
        color = source.readString();
        name = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(color);
        dest.writeString(name);
    }
}
