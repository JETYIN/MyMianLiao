package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.AliImgSpec;

public class NewsSource implements Parcelable {
    private static final String TAG = "NewsSource";
    public static final String INTENT_EXTRA_NAME = TAG;

    public static final int TYPE_MIANLIAO = 0;
    public static final int TYPE_SCHOOL = 1;
    public static final int TYPE_RECOMMEND = 2;

    public static final String ID = "id";
    public static final String GUID = "guid";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String PHONE = "phone";
    public static final String EMAIL = "email";
    public static final String ADDRESS = "address";
    public static final String WEB = "web";
    public static final String AVATAR = "avatar";
    public static final String FOLLOWED = "followed";
    public static final String FOLLOWER_COUNT = "follower_count";
    public static final String TYPE = "type";

    public int id;
    public String guid;
    public String name;
    public String description;
    public String phone;
    public String email;
    public String address;
    public String web;
    public String avatar;
    public boolean followed;
    public int followerCount;
    public int type;
    public boolean following;

    public NewsSource() { }

    public static final NewsSource fromNews(News news) {
        NewsSource source = new NewsSource();
        if (news != null) {
            source.id = news.sourceId;
            source.name = news.sourceName;
        }
        return source;
    }

    public static final NewsSource fromJson(JSONObject json) {
        if (json == null || json.optInt(ID) == 0) {
            return null;
        }
        NewsSource source = new NewsSource();
        source.id = json.optInt(ID);
        source.guid = json.optString(GUID);
        source.name = json.optString(NAME);
        source.description = json.optString(DESCRIPTION);
        source.phone = json.optString(PHONE);
        source.email = json.optString(EMAIL);
        source.address = json.optString(ADDRESS);
        source.web = json.optString(WEB);
        source.avatar = AliImgSpec.NEWS_AVATAR.makeUrl(json.optString(AVATAR));
        source.followed = json.optBoolean(FOLLOWED);
        source.followerCount = json.optInt(FOLLOWER_COUNT);
        source.type = json.optInt(TYPE);
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof NewsSource) {
            NewsSource other = (NewsSource) o;
            return id == other.id;
        }
        return false;
    }

    public boolean isSchoolSource() {
        return type == TYPE_SCHOOL;
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    public void copy(NewsSource source) {
        if (source != null) {
            id = source.id;
            guid = source.guid;
            name = source.name;
            description = source.description;
            phone = source.phone;
            email = source.email;
            address = source.address;
            web = source.web;
            avatar = source.avatar;
            followed = source.followed;
            followerCount = source.followerCount;
            type = source.type;
        }
    }

    public static final Parcelable.Creator<NewsSource> CREATOR = new Parcelable.Creator<NewsSource>() {
        @Override
        public NewsSource createFromParcel(Parcel in) {
            return new NewsSource(in);
        }

        @Override
        public NewsSource[] newArray(int size) {
            return new NewsSource[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(guid);
        out.writeString(name);
        out.writeString(description);
        out.writeString(phone);
        out.writeString(email);
        out.writeString(address);
        out.writeString(web);
        out.writeString(avatar);
        out.writeInt(followed ? 1 : 0);
        out.writeInt(followerCount);
        out.writeInt(type);
    }

    private NewsSource(Parcel in) {
        id = in.readInt();
        guid = in.readString();
        name = in.readString();
        description = in.readString();
        phone = in.readString();
        email = in.readString();
        address = in.readString();
        web = in.readString();
        avatar = in.readString();
        followed = in.readInt() != 0;
        followerCount = in.readInt();
        type = in.readInt();
    }
}
