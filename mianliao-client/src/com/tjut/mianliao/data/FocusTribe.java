package com.tjut.mianliao.data;

import java.util.ArrayList;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class FocusTribe implements Parcelable {

    public int id;
    public int fid;
    public int tribeType;
    public String tribeName;
    public int postCount;
    public int followCount;
    public String icon;
    public boolean isFollowed;
    public ArrayList<String> rfNames;
    public int rfCount;

    public FocusTribe() {
    }

    public static final JsonUtil.ITransformer<FocusTribe> TRANSFORMER =
            new JsonUtil.ITransformer<FocusTribe>() {

        @Override
        public FocusTribe transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Creator<FocusTribe> CREATOR = new Creator<FocusTribe>() {

        @Override
        public FocusTribe[] newArray(int size) {
            return new FocusTribe[size];
        }

        @Override
        public FocusTribe createFromParcel(Parcel source) {
            return new FocusTribe(source);
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected static FocusTribe fromJson(JSONObject json) {
        if (json == null)
            return null;
        FocusTribe focusTribe = new FocusTribe();
        focusTribe.id = json.optInt("id");
        focusTribe.fid = json.optInt("fid");
        focusTribe.tribeType = json.optInt("tribe_type");
        focusTribe.tribeName = json.optString("tribe_name");
        focusTribe.postCount = json.optInt("thread_count");
        focusTribe.followCount = json.optInt("follow_count");
        focusTribe.icon = json.optString("icon");
        focusTribe.isFollowed = json.optBoolean("is_follow");
        focusTribe.rfNames = JsonUtil.getStringArray(json.optJSONArray("recommen_friend_name"));
        focusTribe.rfCount = json.optInt("recommen_friend_count");
        return focusTribe;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(fid);
        dest.writeInt(tribeType);
        dest.writeString(tribeName);
        dest.writeInt(postCount);
        dest.writeInt(followCount);
        dest.writeString(icon);
        dest.writeInt(isFollowed ? 1 : 0);
        int count = rfNames == null ? 0 : rfNames.size();
        dest.writeInt(count);
        if (count > 0) {
            for (String name : rfNames) {
                dest.writeString(name);
            }
        }
        dest.writeInt(rfCount);
    }

    public FocusTribe(Parcel source) {
        id = source.readInt();
        fid = source.readInt();
        tribeType = source.readInt();
        tribeName = source.readString();
        postCount = source.readInt();
        followCount = source.readInt();
        icon = source.readString();
        isFollowed = source.readInt() == 1;
        int count = source.readInt();
        if (count > 0) {
            rfNames = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                rfNames.add(source.readString());
            }
        }
    }
}
