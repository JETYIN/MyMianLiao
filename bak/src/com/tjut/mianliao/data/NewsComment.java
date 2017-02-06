package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.data.contact.UserInfo;

public class NewsComment implements Parcelable {

    public static final String INTENT_EXTRA_NAME = "NewsComment";

    public static final String ID = "id";
    public static final String UID = "uid";
    public static final String TIME = "time";
    public static final String NAME = "name";
    public static final String AVATAR = "avatar";
    public static final String MEDAL = "badge";
    public static final String GENDER = "gender";
    public static final String CONTENT = "text";
    public static final String TARGET_ID = "target_id";
    public static final String TARGET_UID = "target_uid";
    public static final String TARGET_NAME = "target_name";

    public int id;
    public long time;
    public String content;
    public int targetId;
    public int targetUid;
    public String targetName;
    public UserInfo userInfo;

    private NewsComment() {
    }

    public static final NewsComment fromJson(JSONObject json) {
        if (json == null || json.optInt(ID) == 0) {
            return null;
        }
        NewsComment comment = new NewsComment();
        comment.id = json.optInt(ID);
        comment.time = json.optLong(TIME) * 1000;
        comment.content = json.optString(CONTENT);
        comment.targetId = json.optInt(TARGET_ID);
        comment.targetUid = json.optInt(TARGET_UID);
        comment.targetName = json.optString(TARGET_NAME);
        comment.userInfo = UserInfo.fromJson(json);
        return comment;
    }

    public static final Parcelable.Creator<NewsComment> CREATOR = new
            Parcelable.Creator<NewsComment>() {
        @Override
        public NewsComment createFromParcel(Parcel in) {
            return new NewsComment(in);
        }

        @Override
        public NewsComment[] newArray(int size) {
            return new NewsComment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeLong(time);
        out.writeString(content);
        out.writeInt(targetId);
        out.writeInt(targetUid);
        out.writeString(targetName);
        out.writeParcelable(userInfo, flags);
    }

    private NewsComment(Parcel in) {
        id = in.readInt();
        time = in.readLong();
        content = in.readString();
        targetId = in.readInt();
        targetUid = in.readInt();
        targetName = in.readString();
        userInfo = in.readParcelable(UserInfo.class.getClassLoader());
    }
}
