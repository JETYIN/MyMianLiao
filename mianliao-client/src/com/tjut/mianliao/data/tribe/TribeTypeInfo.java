package com.tjut.mianliao.data.tribe;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.util.JsonUtil;

public class TribeTypeInfo implements Parcelable {

    public int type;
    public String name;
    public String icon;
    public ArrayList<TribeInfo> tribes;
    public boolean isHotTribe;
    public boolean isNewTribe;

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
        tribeTypeInfo.tribes = JsonUtil.getArray(json.optJSONArray("tribes"), TribeInfo.TRANSFORMER);
        tribeTypeInfo.isHotTribe = json.optBoolean("is_hot");
        tribeTypeInfo.isNewTribe = json.optBoolean("is_latest");
        return tribeTypeInfo;

    }
    
    

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(name);
        dest.writeString(icon);
        int size = getTribesCount();
        if (size > 0) {
            for (TribeInfo tribe : tribes) {
                dest.writeParcelable(tribe, flags);
            }
        }
        dest.writeInt(isHotTribe ? 0 : 1);
        dest.writeInt(isNewTribe ? 0 : 1);
    }

    public TribeTypeInfo(Parcel in) {
        type = in.readInt();
        name = in.readString();
        icon = in.readString();
        int size = in.readInt();
        if (size > 0) {
            tribes = new ArrayList<TribeInfo>(size);
            for (int i = 0; i < size; i++) {
                TribeInfo tribe = in.readParcelable(TribeInfo.class.getClassLoader());
                tribes.add(tribe);
            }
        }
        isHotTribe = in.readInt() == 0;
        isNewTribe = in.readInt() == 0;
    }
    
    public int getTribesCount() {
        return tribes == null ? 0 : tribes.size();
    }

}
