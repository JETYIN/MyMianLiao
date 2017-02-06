package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.AliImgSpec;

public class Photo extends Image {
    public static final String IS_AVATAR = "is_avatar";

    public String thumbnail;
    public boolean isAvatar;

    public Photo() {}

    public static Photo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Photo photo = new Photo();
        photo.id = json.optInt(ID);
        photo.image = json.optString(IMAGE);
        photo.thumbnail = AliImgSpec.USER_AVATAR.makeUrl(photo.image);
        photo.isAvatar = json.optBoolean(IS_AVATAR);

        return photo;
    }

    public static final Parcelable.Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel source) {
            return new Photo(source);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public Photo(Parcel source) {
        super(source);
        thumbnail = source.readString();
        isAvatar = source.readInt() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(thumbnail);
        dest.writeInt(isAvatar ? 1 : 0);
    }
}
