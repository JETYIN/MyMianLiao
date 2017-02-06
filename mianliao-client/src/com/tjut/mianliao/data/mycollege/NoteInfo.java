package com.tjut.mianliao.data.mycollege;

import org.json.JSONObject;

import android.os.Parcel;

import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.JsonUtil;

public class NoteInfo extends CfPost {

    public static final int TYPE_MEMOR = 0;
    public static final int TYPE_PHOTO = 1;
    public static final int TYPE_HOMEWORK = 2;

    public int noteType;
    public String course;
    public int color;
    public int clock;

    public NoteInfo() {
    }

    public static final JsonUtil.ITransformer<NoteInfo> TRANSFORMER =
            new JsonUtil.ITransformer<NoteInfo>() {

        @Override
        public NoteInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Creator<NoteInfo> CREATOR = new Creator<NoteInfo>() {

        @Override
        public NoteInfo[] newArray(int size) {
            return new NoteInfo[size];
        }

        @Override
        public NoteInfo createFromParcel(Parcel source) {
            return new NoteInfo(source);
        }
    };

    public static NoteInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        NoteInfo noteInfo = new NoteInfo();
        CfPost.fillFromJson(noteInfo, json);
        noteInfo.noteType = json.optInt("note_type");
        if (json.optString("course").equals("")) {
            noteInfo.course = "默认";
        } else {
            noteInfo.course = json.optString("course");
        }
        noteInfo.color = json.optInt("color");
        noteInfo.clock = json.optInt("clock");
        return noteInfo;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(noteType);
        dest.writeString(course);
        dest.writeInt(color);
        dest.writeInt(clock);
    }

    public NoteInfo(Parcel in) {
        super(in);
        noteType = in.readInt();
        course = in.readString();
        color = in.readInt();
        clock = in.readInt();
    }
}
