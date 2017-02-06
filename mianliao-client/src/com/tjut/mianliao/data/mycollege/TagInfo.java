package com.tjut.mianliao.data.mycollege;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class TagInfo implements Parcelable {

    public int tagIndex;
    public String name;
    public long bgColor;
    public boolean isSelected;

    public TagInfo() {
    }

    public TagInfo(Parcel source) {
        tagIndex = source.readInt();
        name = source.readString();
        bgColor = source.readLong();
    }

    public static final JsonUtil.ITransformer<TagInfo> TRANSFORMER =
            new JsonUtil.ITransformer<TagInfo>() {

        @Override
        public TagInfo transform(JSONObject json) {
            return fromJson(json);
        }

    };

    public static TagInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        TagInfo info = new TagInfo();
        info.name = json.optString("name");
        info.tagIndex = json.optInt("tag_index");
        info.bgColor = json.optLong("bg_color");
        return info;
    }

    public static final Creator<TagInfo> CREATOR =
            new Creator<TagInfo>() {

        @Override
        public TagInfo[] newArray(int size) {
            return new TagInfo[size];
        }

        @Override
        public TagInfo createFromParcel(Parcel source) {
            return new TagInfo(source);
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(tagIndex);
        dest.writeString(name);
        dest.writeLong(bgColor);
    }
}
