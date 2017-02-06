package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class Property implements Parcelable {

    public String key;
    public String value;

    public Property() {}

    public static Property fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        Property property = new Property();
        property.key = json.optString("k");
        property.value = json.optString("v");

        return property;
    }

    public static final JsonUtil.ITransformer<Property> TRANSFORMER =
            new JsonUtil.ITransformer<Property>() {
        @Override
        public Property transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Parcelable.Creator<Property> CREATOR =
            new Parcelable.Creator<Property>() {
        @Override
        public Property createFromParcel(Parcel source) {
            return new Property(source);
        }

        @Override
        public Property[] newArray(int size) {
            return new Property[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Property(Parcel source) {
        key = source.readString();
        value = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
    }
}
