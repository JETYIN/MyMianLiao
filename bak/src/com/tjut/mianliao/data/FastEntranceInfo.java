package com.tjut.mianliao.data;

import android.os.Parcel;
import android.os.Parcelable;

public class FastEntranceInfo implements Parcelable {

    public static final String SP_SET_FAST_MENU = "sp_set_fast_menu";
    
    public static final String TABLE_NAME = "fast_entrance_info";
    
    public static final String ID = "_id";
    public static final String IMG_RES = "imgRes";
    public static final String NAME = "name";
    public static final String CLASS_NAME = "className";

    public int imgRes;
    public int imgResBig;
    public String name;
    public String className;
    public boolean checked;

    public FastEntranceInfo() {}
    
    public FastEntranceInfo(int imgRes, int imgResBig, String name, String className) {
        this.imgRes = imgRes;
        this.imgResBig = imgResBig;
        this.name = name;
        this.className = className;
    }

    
    public FastEntranceInfo(Parcel source) {
        imgRes = source.readInt();
        imgResBig = source.readInt();
        name = source.readString();
        className = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imgRes);
        dest.writeInt(imgResBig);
        dest.writeString(name);
        dest.writeString(className);
    }

    public static final Creator<FastEntranceInfo> CREATOR =
            new Creator<FastEntranceInfo>() {

        @Override
        public FastEntranceInfo[] newArray(int size) {
            return new FastEntranceInfo[size];
        }

        @Override
        public FastEntranceInfo createFromParcel(Parcel source) {
            return new FastEntranceInfo(source);
        }
    };
}
