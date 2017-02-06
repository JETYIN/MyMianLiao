package com.tjut.mianliao.data.mycollege;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchTagInfo implements Parcelable{

    public static final String TABLE_NAME = "tag_info";

    public static final String ID = "_id";
    public static final String TAG_ID = "tag_ids";
    public static final String TAGS = "tags";
    public static final String TAG_STR = "tag_str";
    public static final String TIME = "time";

    public long id;
    public String tagIds;
    public String tags;
    public String tagStr;
    public long time;

    public SearchTagInfo() {}

    public static final Creator<SearchTagInfo> CREATOR =
            new Creator<SearchTagInfo>() {

                @Override
                public SearchTagInfo[] newArray(int size) {
                    return new SearchTagInfo[size];
                }

                @Override
                public SearchTagInfo createFromParcel(Parcel source) {
                    return new SearchTagInfo(source);
                }
            };

    public SearchTagInfo(Parcel source) {
        id = source.readLong();
        tagIds = source.readString();
        tags = source.readString();
        time = source.readLong();
        tagStr = source.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(tagIds);
        dest.writeString(tags);
        dest.writeLong(time);
        dest.writeString(tagStr);
    }

}
