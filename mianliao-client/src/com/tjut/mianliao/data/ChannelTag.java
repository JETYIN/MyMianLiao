package com.tjut.mianliao.data;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.Utils;

public class ChannelTag {
    private static final String TAG = "Tag";
    public String name = "";
    public String image = "";

    public ChannelTag() {
    }

    public ChannelTag(String name, String image) {
        this.name = name;
        this.image = image;
    }

    public static final JsonUtil.ITransformer<ChannelTag> TRANSFORMER =
            new JsonUtil.ITransformer<ChannelTag>() {
        @Override
        public ChannelTag transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static ChannelTag fromJson(JSONObject json) {
        ChannelTag tag = new ChannelTag();

        if (json != null) {
            tag.name = json.optString("name");
            tag.image = json.optString("image");
        }

        return tag;
    }

    @Override
    public int hashCode() {
        return image.hashCode();
    }

    public ChannelTag(Parcel in) {
        name = in.readString();
        image = in.readString();
    }

    public void writeToParcel(Parcel dest) {
        dest.writeString(name);
        dest.writeString(image);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("image", image);
        } catch (JSONException e) {
            Utils.logE(TAG, "Error in toJson(): " + e.getMessage());
        }

        return json;
    }
}
