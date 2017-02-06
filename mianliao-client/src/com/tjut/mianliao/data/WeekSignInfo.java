package com.tjut.mianliao.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class WeekSignInfo implements Parcelable{
    
    public int weekContinousDay;
    public int continuesDay;
    public int currentWeekDay;
    public boolean isSignTodday;
    public String checkInStr;
    public static ArrayList<SignProgressInfo> ProgressInfos;
    
    public static final JsonUtil.ITransformer<WeekSignInfo> TRANSFORMER =
            new JsonUtil.ITransformer<WeekSignInfo>() {
        
        @Override
        public WeekSignInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };
    
    public WeekSignInfo (){}
    

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(weekContinousDay);
        out.writeInt(continuesDay);
        out.writeInt(continuesDay);
        out.writeInt(isSignTodday ? 1 : 0);
        out.writeString(checkInStr);
        int SignProgressSize  = getAtUserCount();
        out.writeInt(SignProgressSize);
        if (SignProgressSize > 0) {
            for (SignProgressInfo progressInfo : ProgressInfos) {
                out.writeInt(progressInfo.day);
                out.writeString(progressInfo.giftIcon);
                out.writeString(progressInfo.giftName);
                out.writeInt(progressInfo.kernelNum);
                out.writeString(progressInfo.rewardDescription);
            }
        }
    }
    
    public int getAtUserCount() {
        return ProgressInfos == null ? 0 : ProgressInfos.size();
    }
    
    public static WeekSignInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        WeekSignInfo weekSignInfo = new WeekSignInfo();
        weekSignInfo.weekContinousDay = json.optInt("continuous_checkin_week_days");
        weekSignInfo.continuesDay = json.optInt("continuous_checkin_all_days");
        weekSignInfo.currentWeekDay = json.optInt("current_weekday");
        weekSignInfo.isSignTodday = json.optBoolean("is_today_checkin");
        weekSignInfo.checkInStr = json.optString("checkin_str");
        try {
            weekSignInfo.ProgressInfos = JsonUtil.getArray(new JSONArray(json.optString("weekday_reward_list")), SignProgressInfo.TRANSFORMER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weekSignInfo;
    }
    
    public WeekSignInfo (Parcel in) {
        weekContinousDay = in.readInt();
        continuesDay = in.readInt();
        currentWeekDay = in.readInt();
        isSignTodday = in.readInt() != 0;
        checkInStr = in.readString();
        int SignProgressSize = in.readInt();
        if (SignProgressSize > 0) {
            ProgressInfos = new ArrayList<SignProgressInfo>(SignProgressSize);
            for (int i = 0; i < SignProgressSize; i++) {
                SignProgressInfo progressInfo = new SignProgressInfo();
                progressInfo.day = in.readInt();
                progressInfo.giftIcon = in.readString();
                progressInfo.giftName = in.readString();
                progressInfo.kernelNum = in.readInt();
                progressInfo.rewardDescription = in.readString();
                ProgressInfos.add(progressInfo);
            }
        }
    }
}
