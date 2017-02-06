package com.tjut.mianliao.data.job;

import java.io.Serializable;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.util.JsonUtil;

public class RecruitInfo implements Parcelable, Serializable {

    public static final int SOURCE_CORP = 0 ;
    public static final int SOURCE_PERSONAL = 1 ;

    public int id;
    public String title;
    public String company;
    public long time;
    public int weekNo;
    public int weekDayNo;
    public int src;
    public int schollId;
    public int classNo;

    public RecruitInfo() {}

    public RecruitInfo(Parcel source) {
        id = source.readInt();
        title = source.readString();
        company = source.readString();
        time = source.readLong();
        weekNo = source.readInt();
        weekDayNo = source.readInt();
        src = source.readInt();
        schollId = source.readInt();
        classNo = source.readInt();
    }

    public boolean isPersonal() {
        return src == SOURCE_PERSONAL;
    }

    public static final JsonUtil.ITransformer<RecruitInfo> TRANSFORMER =
            new JsonUtil.ITransformer<RecruitInfo>() {

        @Override
        public RecruitInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static final Creator<RecruitInfo> CREATOR =
            new Creator<RecruitInfo>() {

        @Override
        public RecruitInfo[] newArray(int size) {
            return new RecruitInfo[size];
        }

        @Override
        public RecruitInfo createFromParcel(Parcel source) {
            return new RecruitInfo(source);
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected static RecruitInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        RecruitInfo info = new RecruitInfo();
        info.id = json.optInt("id");
        info.title = json.optString("title");
        info.company = json.optString("company");
        info.weekNo = json.optInt("weekNo");
        info.weekDayNo = json.optInt("weekDayNo");
        info.src = json.optInt("source");
        info.schollId = json.optInt("schoolId");
        info.classNo = json.optInt("classNo");
        info.time = json.optLong("time") * 1000;
        return info;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(company);
        dest.writeLong(time);
        dest.writeInt(weekNo);
        dest.writeInt(weekDayNo);
        dest.writeInt(src);
        dest.writeInt(schollId);
        dest.writeInt(classNo);
    }

}
