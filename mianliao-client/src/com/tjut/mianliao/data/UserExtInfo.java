package com.tjut.mianliao.data;

import java.util.HashMap;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tjut.mianliao.util.FileDownloader;

public class UserExtInfo implements Parcelable, FileDownloader.Callback {

    public static final int TYPE_SINA = 1;
    public static final int TYPE_QQ = 2;
    public static final int TYPE_RENREN = 3;

    public int type;
    public String nickName;
    public int gender;
    public String extId;
    public String token;
    public int schoolId;
//    public int year;
//    public int departmentId;
    public String description;
    public String avatarUrl;
    public String avatarFile;
//    public int education;

    public UserExtInfo() {
    }

    public UserExtInfo(UserExtInfo info) {
        type = info.type;
        nickName = info.nickName;
        gender = info.gender;
        extId = info.extId;
        token = info.token;
        schoolId = info.schoolId;
//        year = info.year;
//        departmentId = info.departmentId;
        description = info.description;
        avatarUrl = info.avatarUrl;
        avatarFile = info.avatarFile;
//        education = info.education;
    }

    public static final Parcelable.Creator<UserExtInfo> CREATOR =
            new Parcelable.Creator<UserExtInfo>() {
        @Override
        public UserExtInfo createFromParcel(Parcel in) {
            return new UserExtInfo(in);
        }

        @Override
        public UserExtInfo[] newArray(int size) {
            return new UserExtInfo[size];
        }
    };

    private UserExtInfo(Parcel source) {
        type = source.readInt();
        nickName = source.readString();
        gender = source.readInt();
        extId = source.readString();
        token = source.readString();
        schoolId = source.readInt();
//        year = source.readInt();
//        departmentId = source.readInt();
        description = source.readString();
        avatarUrl = source.readString();
        avatarFile = source.readString();
//        education =source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(nickName);
        dest.writeInt(gender);
        dest.writeString(extId);
        dest.writeString(token);
        dest.writeInt(schoolId);
//        dest.writeInt(year);
//        dest.writeInt(departmentId);
        dest.writeString(description);
        dest.writeString(avatarUrl);
        dest.writeString(avatarFile);
//        dest.writeInt(education);
    }

    public void setAvatar(Context context, String url) {
        avatarUrl = url;
        FileDownloader.getInstance(context).getFile(url, this, false);
    }


    public HashMap<String, String> getParameters() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("type", String.valueOf(type));
        params.put("ext_id", extId);
        params.put("school_id", String.valueOf(schoolId));
//        params.put("department_id", String.valueOf(departmentId));
//        params.put("start_year", String.valueOf(year));
        params.put("nick", nickName);
        params.put("gender", String.valueOf(gender));
        params.put("description", description);
//        params.put("education", String.valueOf(education));
        return params;
    }

    public HashMap<String, String> getFiles() {
        if (TextUtils.isEmpty(avatarFile)) {
            return null;
        }
        HashMap<String, String> files = new HashMap<String, String>();
        files.put("avatar", avatarFile);
        return files;
    }

    public void clear() {
        type = 0;
        nickName = null;
        gender = 0;
        extId = null;
        token = null;
        schoolId = 0;
//        year = 0;
//        departmentId = 0;
        description = null;
        avatarUrl = null;
        avatarFile = null;
//        education = 0;
    }

    @Override
    public void onResult(boolean success, String url, String fileName) {
        if (success && TextUtils.equals(avatarUrl, url)) {
            avatarFile = fileName;
        }
    }
}
