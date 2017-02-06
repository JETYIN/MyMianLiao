package com.tjut.mianliao.data;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tjut.mianliao.data.contact.UserInfo;

public class Resume implements Parcelable {

    public static final String INTENT_EXTRA_NAME = "Resume";

    public static final String ID = "resume_id";
    public static final String PHOTO = "photo";
    public static final String REALNAME = "realname";
    public static final String GENDER = "gender";
    public static final String BIRTH_YEAR = "birthday";
    public static final String EMAIL = "email";
    public static final String INTRO = "intro";
    public static final String GRADUATION_TIME = "graduation";
    public static final String MAJOR="major";
    public static final String EDUBACK = "education";

    public int id;
    public String photo;
    public String realName;
    public int gender;
    public int birthYear;
    public String email;
    public String intro;
    public int graduationYear;
    public String major;
    public int education;


    private Resume() {
    }

    public static Resume fromUserInfo(UserInfo info) {
        Resume resume = new Resume();
        resume.gender = info == null ? 0 : info.gender;
        resume.realName = info == null || info.name == null ? "" : info.nickname;
        resume.email = info == null || info.email == null ? "" : info.email;
        resume.photo = "";
        resume.intro = "";
        return resume;
    }

    public static Resume fromJson(JSONObject json) {
        if (json == null || json.optInt(ID) == 0) {
            return null;
        }
        Resume resume = new Resume();
        resume.id = json.optInt(ID);
        resume.photo = json.optString(PHOTO);
        resume.realName = json.optString(REALNAME);
        resume.gender = json.optInt(GENDER);
        resume.birthYear = json.optInt(BIRTH_YEAR);
        resume.email = json.optString(EMAIL);
        resume.intro = json.optString(INTRO);
        resume.graduationYear=json.optInt(GRADUATION_TIME);
        resume.major=json.optString(MAJOR);
        resume.education = json.optInt(EDUBACK);
        return resume;
    }

    public Resume copy() {
        Resume resume = new Resume();
        resume.id = id;
        resume.photo = photo;
        resume.realName = realName;
        resume.gender = gender;
        resume.birthYear = birthYear;
        resume.graduationYear = graduationYear;
        resume.email = email;
        resume.intro = intro;
        resume.education = education;
        return resume;
    }

    public boolean contentEquals(Resume other) {
        return other != null && TextUtils.equals(other.photo, photo) && TextUtils.equals(other.realName, realName)
                && other.gender == gender && other.birthYear == birthYear && TextUtils.equals(other.email, email)
                && TextUtils.equals(other.intro, intro);
    }

    public static final Parcelable.Creator<Resume> CREATOR = new Parcelable.Creator<Resume>() {
        @Override
        public Resume createFromParcel(Parcel in) {
            return new Resume(in);
        }

        @Override
        public Resume[] newArray(int size) {
            return new Resume[size];
        }
    };

    private Resume(Parcel in) {
        id = in.readInt();
        photo = in.readString();
        realName = in.readString();
        gender = in.readInt();
        birthYear = in.readInt();
        email = in.readString();
        intro = in.readString();
        education = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(photo);
        dest.writeString(realName);
        dest.writeInt(gender);
        dest.writeInt(birthYear);
        dest.writeString(email);
        dest.writeString(intro);
        dest.writeInt(education);
    }
}
