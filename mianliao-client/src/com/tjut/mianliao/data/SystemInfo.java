package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class SystemInfo implements Parcelable {

    public boolean neight;
    public int timestamp;
    public int start;
    public int moneyToGold;
    public int goldToPoint;
    public String dateTime;
    public String checkinRule;

    public static final JsonUtil.ITransformer<SystemInfo> TRANSFORMER =
            new JsonUtil.ITransformer<SystemInfo>() {
        @Override
        public SystemInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public SystemInfo() {}


    public static SystemInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        SystemInfo nightInfo = new SystemInfo();
        nightInfo.neight = json.optBoolean("night");
        nightInfo.timestamp = json.optInt("timestamp");
        nightInfo.start = json.optInt("reach_end");
        nightInfo.moneyToGold = json.optInt("money_price_rate");
        nightInfo.goldToPoint = json.optInt("price_credit_rate");
        nightInfo.dateTime = json.optString("datetime");
        nightInfo.checkinRule = json.optString("checkin_rule");
        return nightInfo;
    }

    public static final Parcelable.Creator<SystemInfo> CREATOR =
            new Parcelable.Creator<SystemInfo>() {
        @Override
        public SystemInfo createFromParcel(Parcel source) {
            return new SystemInfo(source);
        }

        @Override
        public SystemInfo[] newArray(int size) {
            return new SystemInfo[size];
        }
    };

    private SystemInfo(Parcel source) {
        neight = source.readInt() == 1 ? true : false;
        timestamp = source.readInt();
        start = source.readInt();
        moneyToGold = source.readInt();
        goldToPoint = source.readInt();
        dateTime = source.readString();
        checkinRule = source.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeInt(neight ? 1 : 0);
        dest.writeInt(timestamp);
        dest.writeInt(start);
        dest.writeInt(moneyToGold);
        dest.writeInt(goldToPoint);
        dest.writeString(dateTime);
        dest.writeString(checkinRule);
    }

}
