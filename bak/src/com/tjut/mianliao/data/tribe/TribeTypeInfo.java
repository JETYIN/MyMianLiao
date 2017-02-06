package com.tjut.mianliao.data.tribe;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class TribeTypeInfo implements Parcelable {

    public int type;
    public String name;
    public String icon;

    public TribeTypeInfo() {}

    @Override
    public int describeContents() {
        return 0;
    }

    public static final JsonUtil.ITransformer<TribeTypeInfo> TRANSFORMER =
            new JsonUtil.ITransformer<TribeTypeInfo>() {

        @Override
        public TribeTypeInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };
    
    public static final Parcelable.Creator<TribeTypeInfo> CREATOR =
            new Creator<TribeTypeInfo>() {
        
            @Override
            public TribeTypeInfo[] newArray(int size) {
                return new TribeTypeInfo[size];
            }
    
            @Override
            public TribeTypeInfo createFromParcel(Parcel source) {
                return new TribeTypeInfo(source);
            }
    };

    public static TribeTypeInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        TribeTypeInfo tribeTypeInfo = new TribeTypeInfo();
        tribeTypeInfo.type = json.optInt("type");
        tribeTypeInfo.name = json.optString("name");
        tribeTypeInfo.icon = json.optString("icon");
        return tribeTypeInfo;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(name);
        dest.writeString(icon);
    }

    public TribeTypeInfo(Parcel in) {
        type = in.readInt();
        name = in.readString();
        icon = in.readString();
    }

}
