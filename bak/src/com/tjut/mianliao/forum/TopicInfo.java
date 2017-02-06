package com.tjut.mianliao.forum;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.JsonUtil.ITransformer;

import android.os.Parcel;
import android.os.Parcelable;

public class TopicInfo implements Parcelable {

    public int id;
    public String name;
    public int listOrder;
    public int dateType;
    public String icon;

    public TopicInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(listOrder);
        dest.writeInt(dateType);
        dest.writeString(icon);
    }

    public TopicInfo(Parcel in) {
        id = in.readInt();
        name = in.readString();
        listOrder = in.readInt();
        dateType = in.readInt();
        icon = in.readString();
    }

    public static final JsonUtil.ITransformer<TopicInfo> TRANSFORMER = 
            new ITransformer<TopicInfo>() {

        @Override
        public TopicInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Creator<TopicInfo> CREATOR = new Creator<TopicInfo>() {

        @Override
        public TopicInfo[] newArray(int size) {
            return new TopicInfo[size];
        }

        @Override
        public TopicInfo createFromParcel(Parcel source) {
            return new TopicInfo(source);
        }
    };

    public static TopicInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        TopicInfo mTpInfo = new TopicInfo();
        mTpInfo.id = json.optInt("id");
        mTpInfo.name = json.optString("name");
        mTpInfo.listOrder = json.optInt("list_order");
        mTpInfo.icon = json.optString("icon");
        return mTpInfo;
    }
}
