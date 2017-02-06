package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class SignRecordInfo implements Parcelable{
    public long startTime;
    public long endTime;
    public int continuousDay;
    public int credit;
    public String rewardName;
    public String rewardIcon;

    public SignRecordInfo () {}
    
    public static final JsonUtil.ITransformer<SignRecordInfo> TRANSFORMER =
        new JsonUtil.ITransformer<SignRecordInfo>() {
            
            @Override
            public SignRecordInfo transform(JSONObject json) {
                return fromJson(json);
            }
        };
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeInt(continuousDay);
        dest.writeInt(credit);
        dest.writeString(rewardName);
        dest.writeString(rewardIcon);
    }
    
    public static SignRecordInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        SignRecordInfo signRecordInfo = new SignRecordInfo();
        signRecordInfo.startTime = (json.optLong("start_time") * 1000);
        signRecordInfo.endTime = (json.optLong("end_time") * 1000);
        signRecordInfo.continuousDay = json.optInt("continuous_day");
        signRecordInfo.credit = json.optInt("credit");
        signRecordInfo.rewardName = json.optString("reward_name");
        signRecordInfo.rewardIcon = json.optString("reward_icon");

        return signRecordInfo;
    } 
    
    public SignRecordInfo (Parcel in) {
        startTime = in.readLong();
        endTime = in.readLong();
        continuousDay = in.readInt();
        credit = in.readInt();
        rewardName = in.readString();
        rewardIcon = in.readString();
    }
    

}
