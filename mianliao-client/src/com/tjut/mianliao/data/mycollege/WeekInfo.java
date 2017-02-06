package com.tjut.mianliao.data.mycollege;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class WeekInfo implements Parcelable{

    public int startWeek;
    public int currentWeek;
    public int endWeek;
    public long startTimeStamps;
    
    public WeekInfo() {
    }
    
    public static final JsonUtil.ITransformer<WeekInfo> TRANSFORMER =
            new JsonUtil.ITransformer<WeekInfo>() {
                
                @Override
                public WeekInfo transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    public WeekInfo(Parcel source) {
        startWeek = source.readInt();
        endWeek = source.readInt();
        currentWeek = source.readInt();
        startTimeStamps = source.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static WeekInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        WeekInfo weekInfo = new WeekInfo();
        weekInfo.startWeek = json.optInt("start");
        weekInfo.endWeek = json.optInt("end");
        weekInfo.currentWeek = json.optInt("current");
        weekInfo.startTimeStamps = (long) (json.optLong("start_timestamp") * 1000);
        return weekInfo;
    }
    
    public static final Creator<WeekInfo> CREATOR =
            new Creator<WeekInfo>() {
                
                @Override
                public WeekInfo[] newArray(int size) {
                    return new WeekInfo[0];
                }
                
                @Override
                public WeekInfo createFromParcel(Parcel source) {
                    return new WeekInfo(source);
                }
            };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(startWeek);
        dest.writeInt(endWeek);
        dest.writeInt(currentWeek);
        dest.writeLong(startTimeStamps);
    }

}
