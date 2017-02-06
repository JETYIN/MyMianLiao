package com.tjut.mianliao.data;

import java.util.Locale;

import org.json.JSONObject;

import com.tjut.mianliao.sidebar.PinyinUtil;
import com.tjut.mianliao.util.JsonUtil;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class RadMenInfo implements Parcelable{
    
    public int uid;
    public String nickName;
    public String gender;
    public String avatar;
    public String school;
    public boolean isFollow;
    public String alpha;

    public RadMenInfo () {
    }

    public static final JsonUtil.ITransformer<RadMenInfo> TRANSFORMER = new JsonUtil.ITransformer<RadMenInfo>() {

        @Override
        public RadMenInfo transform(JSONObject json) {
            return fromJson(json);
        }

    };
    
    public static final Creator<RadMenInfo> CREATOR = new Creator<RadMenInfo>() {

        @Override
        public RadMenInfo[] newArray(int size) {
            return new RadMenInfo[size];
        }

        @Override
        public RadMenInfo createFromParcel(Parcel source) {
            return new RadMenInfo(source);
        }
    };
    
    public static RadMenInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        RadMenInfo radMenInfo = new RadMenInfo();

        radMenInfo.uid = json.optInt("uid");

        radMenInfo.nickName = json.optString("nick");
        radMenInfo.avatar = json.optString("avatar");
        radMenInfo.school = json.optString("school");
        radMenInfo.isFollow = json.optBoolean("is_follow");
        return radMenInfo;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeString(nickName);
        dest.writeString(gender);
        dest.writeString(avatar);
        dest.writeString(school);
        dest.writeInt(isFollow == true ? 0 : 1);
        dest.writeString(alpha);
        
    }
    
    public RadMenInfo(Parcel in) {
        uid = in.readInt();
        nickName = in.readString();
        gender = in.readString();
        avatar = in.readString();
        school = in.readString();
        isFollow = in.readInt() == 0 ? true : false;
        alpha = in.readString();
    }
    
    public  String getAlpha(Context context) {
        return PinyinUtil.getPinyin(nickName.toUpperCase(Locale.getDefault()));
    }
    

}
