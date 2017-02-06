package com.tjut.mianliao.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by Silva on 2016/7/7.
 */
public class GiftInfo implements Parcelable{

//    num: int (礼物数量)
//    value: int (总价值)
//    gift_id: int (礼物ID)
//    gift_name: string (礼物名称)
//    sender_uid: int (送礼人ID)
//    sender_nick: string (送礼人昵称)
//    time: int (送礼时间戳)

    public int giftNum;
    public int giftTotalValue;
    public int giftId;
    public int senderId;// 送礼人的ID
    public long giftTime;//送礼时间
    public String giftName;
    public String senderName;// 送礼人的昵称


    public GiftInfo () {}

    protected GiftInfo(Parcel in) {
        giftNum = in.readInt();
        giftTotalValue = in.readInt();
        giftId = in.readInt();
        senderId = in.readInt();
        giftTime = in.readLong();
        giftName = in.readString();
        senderName = in.readString();
    }

    public static final Creator<GiftInfo> CREATOR = new Creator<GiftInfo>() {
        @Override
        public GiftInfo createFromParcel(Parcel in) {
            return new GiftInfo(in);
        }

        @Override
        public GiftInfo[] newArray(int size) {
            return new GiftInfo[size];
        }
    };

    public static final JsonUtil.ITransformer<GiftInfo> TRANSFORMER = new JsonUtil.ITransformer<GiftInfo>(){
        @Override
        public GiftInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static GiftInfo fromJson(JSONObject json) {
        if (json == null) {
            return  null;
        }

        GiftInfo gift = new GiftInfo();
        gift.giftNum = json.optInt("num");
        gift.giftTotalValue = json.optInt("value");
        gift.giftId = json.optInt("gift_id");
        gift.senderId = json.optInt("sender_uid");
        gift.giftTime = json.optLong("time") * 1000;
        gift.giftName = json.optString("gift_name");
        gift.senderName = json.optString("sender_nick");
        return gift;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(giftNum);
        parcel.writeInt(giftTotalValue);
        parcel.writeInt(giftId);
        parcel.writeInt(senderId);
        parcel.writeLong(giftTime);
        parcel.writeString(giftName);
        parcel.writeString(senderName);
    }
}
