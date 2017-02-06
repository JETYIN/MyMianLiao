package com.tjut.mianliao.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.mapapi.model.LatLng;

public class LatLngWrapper implements Parcelable {

    public LatLng latLng;

    public static final Creator<LatLngWrapper> CREATOR = new Creator<LatLngWrapper>() {
        @Override
        public LatLngWrapper createFromParcel(Parcel source) {
            return new LatLngWrapper(source);
        }

        @Override
        public LatLngWrapper[] newArray(int size) {
            return new LatLngWrapper[0];
        }
    };

    public LatLngWrapper(double latitude, double longitude) {
        this(new LatLng(latitude, longitude));
    }

    public LatLngWrapper(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLngWrapper(Parcel in) {
        latLng = new LatLng(in.readDouble(), in.readDouble());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latLng.latitude);
        dest.writeDouble(latLng.longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
