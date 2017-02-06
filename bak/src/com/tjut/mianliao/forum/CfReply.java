package com.tjut.mianliao.forum;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class CfReply extends CfRecord {
    public static final String INTENT_EXTRA_NAME = "cf_reply";

    public int replyId;
    public CfPost targetPost;
    public CfReply targetReply;
    public CfReply parentReply;
    public int targetReplyId;
    public int targetUid;
    public String targetReplyName;
    public boolean sameSchool;
    public String school;
    public int floor;
    public CharSequence replyContent;

    public CfReply() {}

    public static CfReply fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        CfReply reply = new CfReply();
        CfRecord.fillFromJson(reply, json);
        reply.replyId = json.optInt("reply_id");
        reply.targetReply = CfReply.fromJson(json.optJSONObject("target_reply"));
        reply.targetPost = CfPost.fromJson(json.optJSONObject("target_thread"));
        reply.targetReplyId = json.optInt("target_reply_id");
        reply.targetUid = json.optInt("target_uid");
        reply.sameSchool=json.optBoolean("same_school");
        reply.targetReplyName = json.optString("target_reply_name");
        reply.school = json.optString("school");
        reply.floor = json.optInt("floor");
        reply.replyContent = reply.content;
        return reply;
    }

    @Override
    public int getId() {
        return replyId;
    }

    @Override
    public String getIdName() {
        return "reply_id";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof CfReply) {
            CfReply other = (CfReply) o;
            return replyId == other.replyId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return replyId;
    }

    public CfRecord getDirectTarget() {
        return targetReply != null ? targetReply : targetPost;
    }

    public static final JsonUtil.ITransformer<CfReply> TRANSFORMER =
            new JsonUtil.ITransformer<CfReply>() {
        @Override
        public CfReply transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Parcelable.Creator<CfReply> CREATOR =
            new Parcelable.Creator<CfReply>() {
        @Override
        public CfReply createFromParcel(Parcel in) {
            return new CfReply(in);
        }

        @Override
        public CfReply[] newArray(int size) {
            return new CfReply[size];
        }
    };

    public CfReply(Parcel in) {
        super(in);
        replyId = in.readInt();
        targetReplyId = in.readInt();
        targetUid = in.readInt();
        targetReplyName = in.readString();
        school = in.readString();
        floor = in.readInt();
        replyContent = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(replyId);
        dest.writeInt(targetReplyId);
        dest.writeInt(targetUid);
        dest.writeString(targetReplyName);
        dest.writeString(school);
        dest.writeInt(floor);
        dest.writeString(replyContent.toString());
    }
}
