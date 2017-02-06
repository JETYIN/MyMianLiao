package com.tjut.mianliao.live;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by j_hao on 2016/6/24.
 */
public class LiveGift  implements Parcelable{
/** is_show (1显示0不显示)
      icon  (礼物图标)
 * **/

    public static final int ANIM_TYPE_NORMAL = 1;
    public static final int ANIM_TYPE_SPECIAL= 2;
    public static final int ANIM_TYPE_FIRE = 3;

    public int giftId;
    public String name;
    public int  price;
    public int isShow;
    public String icon;
    public int type;

    public LiveGift() {
    }

    public static final JsonUtil.ITransformer<LiveGift> TRANSFORMER =
            new JsonUtil.ITransformer<LiveGift>(){
                @Override
                public LiveGift transform(JSONObject json) {
                    return fromJson(json);
                }


            };


    public static final Parcelable.Creator<LiveGift> CREATOR =
            new Parcelable.Creator<LiveGift>() {
                @Override
                public LiveGift createFromParcel(Parcel source) {
                    return new LiveGift(source);
                }

                @Override
                public LiveGift[] newArray(int size) {
                    return new LiveGift[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(giftId);
        dest.writeInt(price);
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeInt(isShow);
        dest.writeInt(type);
    }

    public LiveGift(Parcel source) {
        giftId = source.readInt();
        price = source.readInt();
        name = source.readString();
        icon = source.readString();
        isShow = source.readInt();
        type = source.readInt();
    }

    public static LiveGift fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        LiveGift livegift = new LiveGift();
        livegift.giftId = json.optInt("gift_id");
        livegift.name = json.optString("name");
        livegift.price = json.optInt("price");
        livegift.isShow = json.optInt("is_show");
        livegift.icon = json.optString("icon");
        livegift.type = json.optInt("type");
        return livegift;
    }
}
