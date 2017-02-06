package com.tjut.mianliao.data.explore;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class RechargeInfo implements Parcelable{

    public int id;
    public int gold;
    public int money;
    public String saleinfo;
    public double orimoney;

    public static final JsonUtil.ITransformer<RechargeInfo> TRANSFORMER =
            new JsonUtil.ITransformer<RechargeInfo>() {

        @Override
        public RechargeInfo transform(JSONObject json) {
            return fromJson(json);
        }

    };
    private static RechargeInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        RechargeInfo rechargeInfo = new RechargeInfo();
        rechargeInfo.id = json.optInt("id");
        rechargeInfo.gold = json.optInt("price");
        rechargeInfo.money = json.optInt("money");
        rechargeInfo.saleinfo = json.optString("info");
        rechargeInfo.orimoney = json.optDouble("ori_money");
        return rechargeInfo;
    }

    public RechargeInfo() {}

    public static final Creator<RechargeInfo> CREATOR =
            new Creator<RechargeInfo>() {

                @Override
                public RechargeInfo[] newArray(int size) {
                    return new RechargeInfo[size];
                }

                @Override
                public RechargeInfo createFromParcel(Parcel source) {
                    return new RechargeInfo(source);
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }


    public RechargeInfo(Parcel source) {
        id = source.readInt();
        gold = source.readInt();
        money = source.readInt();
        saleinfo = source.readString();
        orimoney = source.readDouble();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(gold);
        dest.writeInt(money);
        dest.writeString(saleinfo);
        dest.writeDouble(orimoney);
    }

}
