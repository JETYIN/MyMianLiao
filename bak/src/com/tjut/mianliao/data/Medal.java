package com.tjut.mianliao.data;

import java.util.Comparator;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class Medal implements Parcelable {

    public int id;
    public String name;
    public String description;
    public String imageUrl;
    public int conferDate;
    public int primary;

    public Medal() {}

    public static Medal fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Medal medal = new Medal();
        medal.id = json.optInt("id");
        medal.name = json.optString("name");
        medal.description = json.optString("description");
        medal.imageUrl = json.optString("image");
        medal.conferDate = json.optInt("confer_date");
        medal.primary = json.optInt("primary");
        return medal;
    }

    public boolean isPrimary() {
        return primary > 0;
    }

    public static final Comparator<Medal> PRIMARY_COMPARATOR = new Comparator<Medal>() {
        @Override
        public int compare(Medal lhs, Medal rhs) {
            return rhs.primary - lhs.primary;
        }
    };

    public static final JsonUtil.ITransformer<Medal> TRANSFORMER =
            new JsonUtil.ITransformer<Medal>() {
        @Override
        public Medal transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Parcelable.Creator<Medal> CREATOR = new Parcelable.Creator<Medal>() {
        @Override
        public Medal createFromParcel(Parcel source) {
            return new Medal(source);
        }

        @Override
        public Medal[] newArray(int size) {
            return new Medal[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Medal(Parcel source) {
        id = source.readInt();
        name = source.readString();
        description = source.readString();
        imageUrl = source.readString();
        conferDate = source.readInt();
        primary = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeInt(conferDate);
        dest.writeInt(primary);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Medal) {
            Medal other = (Medal) o;
            return id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
