package com.tjut.mianliao.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AtInfo implements Parcelable {
    public String AtContent;
    public int AtIndex;
    public int index = -1;

    public AtInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(AtContent);
        dest.writeInt(AtIndex);
        dest.writeInt(index);

    }

    public AtInfo(Parcel in) {
        AtContent = in.readString();
        AtIndex = in.readInt();
        index = in.readInt();
    }
}