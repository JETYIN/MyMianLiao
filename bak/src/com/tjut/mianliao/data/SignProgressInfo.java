package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class SignProgressInfo implements Parcelable {
    public int day;
    public String giftIcon;
    public String giftName;
    public int kernelNum;
    public String rewardDescription;
    
    public static final JsonUtil.ITransformer<SignProgressInfo> TRANSFORMER =
            new JsonUtil.ITransformer<SignProgressInfo>() {
        
        @Override
        public SignProgressInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public SignProgressInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(day);
        dest.writeString(giftIcon);
        dest.writeString(giftName);
        dest.writeInt(kernelNum);
        dest.writeString(rewardDescription);
        
    }

    public static SignProgressInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        SignProgressInfo signProgressInfo = new SignProgressInfo();
        signProgressInfo.day = json.optInt("day");
        signProgressInfo.giftIcon = json.optString("icon");
        signProgressInfo.giftName = json.optString("name");
        signProgressInfo.kernelNum = json.optInt("kernels");
        signProgressInfo.rewardDescription = json.optString("reward_description");

        return signProgressInfo;
    }

    public SignProgressInfo(Parcel in) {
        day = in.readInt();
        giftIcon = in.readString();
        giftName = in.readString();
        kernelNum = in.readInt();
        rewardDescription = in.readString();
    }

}
