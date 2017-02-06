package com.tjut.mianliao.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import com.tjut.mianliao.util.Utils;

public class Attachment {
    private static final String TAG = "Attachment";

    public String name = "";
    public String url = "";
    public int size;

    public Attachment() {
    }

    public static Attachment fromJson(JSONObject json) {
        Attachment att = new Attachment();

        if (json != null) {
            att.name = json.optString("name");
            att.url = json.optString("url");
            att.size = json.optInt("size");
        }

        return att;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Attachment) {
            Attachment other = (Attachment) o;
            return url.equals(other.url);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public Attachment(Parcel in) {
        name = in.readString();
        url = in.readString();
        size = in.readInt();
    }

    public void writeToParcel(Parcel dest) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeInt(size);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("url", url);
            json.put("size", size);
        } catch (JSONException e) {
            Utils.logE(TAG, "Error in toJson(): " + e.getMessage());
        }

        return json;
    }
}
