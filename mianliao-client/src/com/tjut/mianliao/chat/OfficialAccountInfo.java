package com.tjut.mianliao.chat;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class OfficialAccountInfo implements Parcelable {

    public int id;
    public String name;
    public String content;
    public String vatar;

    public OfficialAccountInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(content);
        dest.writeString(vatar);
    }

    public OfficialAccountInfo(Parcel in) {
        id = in.readInt();
        name = in.readString();
        content = in.readString();
        vatar = in.readString();
    }

    public static OfficialAccountInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        OfficialAccountInfo offA = new OfficialAccountInfo();
        offA.id = json.optInt("id");
        offA.name = json.optString("name");
        offA.content = json.optString("content");
        offA.vatar = json.optString("avatar");
        return offA;
    }

}
