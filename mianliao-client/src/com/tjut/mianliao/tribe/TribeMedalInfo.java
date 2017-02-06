package com.tjut.mianliao.tribe;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class TribeMedalInfo implements Parcelable{
    
    public int medalId;
    public String medalName;
    public String medalPic;

    public TribeMedalInfo () {}
    
    public static TribeMedalInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        TribeMedalInfo medal = new TribeMedalInfo();
        medal.medalId = json.optInt("id");
        medal.medalName = json.optString("name");
        medal.medalPic = json.optString("img");
        return medal;
    }
    
    public static final JsonUtil.ITransformer<TribeMedalInfo> TRANSFORMER =
            new JsonUtil.ITransformer<TribeMedalInfo>() {
        @Override
        public TribeMedalInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Parcelable.Creator<TribeMedalInfo> CREATOR = new Parcelable.Creator<TribeMedalInfo>() {
        @Override
        public TribeMedalInfo createFromParcel(Parcel source) {
            return new TribeMedalInfo(source);
        }

        @Override
        public TribeMedalInfo[] newArray(int size) {
            return new TribeMedalInfo[size];
        }
    };
    
    public TribeMedalInfo(Parcel in) {
        medalId = in.readInt();
        medalName = in.readString();
        medalPic = in.readString();
        
    }
    
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(medalId);
        dest.writeString(medalName);
        dest.writeString(medalPic);
    }

}
