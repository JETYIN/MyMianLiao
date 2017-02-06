package com.tjut.mianliao.live;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by j_hao on 2016/7/7.
 */
public class Topics implements Parcelable {
    public int id;
    public String name;

    public Topics() {
    }


    public static final Creator<Topics> CREATOR = new Creator<Topics>() {
        @Override
        public Topics createFromParcel(Parcel in) {
            return new Topics(in);
        }

        @Override
        public Topics[] newArray(int size) {
            return new Topics[size];
        }
    };
    public static final JsonUtil.ITransformer<Topics> TRANSFORMER =
            new JsonUtil.ITransformer<Topics>() {
                @Override
                public Topics transform(JSONObject json) {
                    return fromJson(json);
                }


            };

    @Override
    public int describeContents() {
        return 0;
    }

    public static Topics fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Topics topics = new Topics();
        topics.id = json.optInt("id");
        topics.name = json.optString("name");
        return topics;
    }

    protected Topics(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }
}














