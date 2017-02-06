package com.tjut.mianliao.forum.nova;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class ChannelTagInfo implements Parcelable{
    
    public String icon;
    public String name;
    public String nameEn;

    public static final JsonUtil.ITransformer<ChannelTagInfo> TRANSFORMER =

            new JsonUtil.ITransformer<ChannelTagInfo>() {
        @Override
        public ChannelTagInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }

    public ChannelTagInfo() {
    }
    
    public static ChannelTagInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        ChannelTagInfo mChannelTagInfo = new ChannelTagInfo();
        mChannelTagInfo.name = json.optString("name");
        mChannelTagInfo.icon = json.optString("image");
        mChannelTagInfo.nameEn = json.optString("english");
        return mChannelTagInfo;
    }
    
    public static final Parcelable.Creator<ChannelTagInfo> CREATOR =
            new Parcelable.Creator<ChannelTagInfo>() {
        @Override
        public ChannelTagInfo createFromParcel(Parcel source) {
            return new ChannelTagInfo(source);
        }

        @Override
        public ChannelTagInfo[] newArray(int size) {
            return new ChannelTagInfo[size];
        }
    };
    
    private ChannelTagInfo(Parcel source) {
        name = source.readString();
        icon = source.readString();
        nameEn= source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(icon);
        dest.writeString(nameEn);
    }

}
