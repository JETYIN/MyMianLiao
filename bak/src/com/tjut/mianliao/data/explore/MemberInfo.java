package com.tjut.mianliao.data.explore;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class MemberInfo implements Parcelable{

    public int id;
    public int  month;
    public double money;
    public int gold; // gold
    public String info;

    public boolean enable;

    public static final JsonUtil.ITransformer<MemberInfo> TRANSFORMER =
            new JsonUtil.ITransformer<MemberInfo>() {

                @Override
                public MemberInfo transform(JSONObject json) {
                    return fromJson(json);
                }

            };

    public MemberInfo() {}

    public MemberInfo(Parcel source) {
        id = source.readInt();
        month = source.readInt();
        money = source.readDouble();
        gold = source.readInt();
        info = source.readString();
    }

    private static MemberInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.id = json.optInt("id");
        memberInfo.month = json.optInt("month");
        memberInfo.money = json.optDouble("money");
        memberInfo.gold = json.optInt("price");
        memberInfo.info = json.optString("info");
        return memberInfo;
    }

    public static final Creator<MemberInfo> CREATOR =
            new Creator<MemberInfo>() {

                @Override
                public MemberInfo[] newArray(int size) {
                    return new MemberInfo[size];
                }

                @Override
                public MemberInfo createFromParcel(Parcel source) {
                    return new MemberInfo(source);
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(month);
        dest.writeDouble(money);
        dest.writeInt(gold);
        dest.writeString(info);
    }
}
