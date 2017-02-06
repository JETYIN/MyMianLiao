package com.tjut.mianliao.data.explore;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class TradeInfo implements Parcelable{

    public int id;
    public String name;
    public String icon;
    public String note;
    public long time;

    public static final JsonUtil.ITransformer<TradeInfo> TRANSFORMER =
            new JsonUtil.ITransformer<TradeInfo>() {
        @Override
        public TradeInfo transform(JSONObject json) {
            return fromJson(json);
        }

    };

    public TradeInfo() {}


    private static TradeInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        TradeInfo tradeInfo = new TradeInfo();
        tradeInfo.id = json.optInt("id");
        tradeInfo.name = json.optString("name");
        tradeInfo.icon = json.optString("icon");
        tradeInfo.note = json.optString("note");
        tradeInfo.time =  json.optLong("time") * 1000;
        return tradeInfo;
    }

    public static final Parcelable.Creator<TradeInfo> CREATOR =
            new Parcelable.Creator<TradeInfo>() {
        @Override
        public TradeInfo createFromParcel(Parcel source) {
            return new TradeInfo(source);
        }

        @Override
        public TradeInfo[] newArray(int size) {
            return new TradeInfo[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    public TradeInfo(Parcel source) {
        id = source.readInt();
        name = source.readString();
        icon = source.readString();
        note = source.readString();
        time = source.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeString(note);
        dest.writeLong(time);
    }

}
