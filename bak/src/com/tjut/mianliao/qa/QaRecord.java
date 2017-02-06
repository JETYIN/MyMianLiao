package com.tjut.mianliao.qa;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.AliImgSpec;

public abstract class QaRecord implements Parcelable {

    public int id;
    public String desc;
    public String thumbnail;
    public String image;
    public long createdOn;
    public UserInfo userInfo;

    public QaRecord() {}

    protected static void fillFromJson(QaRecord record, JSONObject json) {
        record.id = json.optInt("id");
        record.desc = json.optString("desc");
        record.image = json.optString("image");
        record.thumbnail = AliImgSpec.QA_THUMB.makeUrl(record.image);
        record.createdOn = json.optLong("created_on") * 1000;
        record.userInfo = UserInfo.fromJson(json);
    }

    public QaRecord(Parcel in) {
        id = in.readInt();
        desc = in.readString();
        thumbnail = in.readString();
        image = in.readString();
        createdOn = in.readLong();
        userInfo = in.readParcelable(UserInfo.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(desc);
        out.writeString(thumbnail);
        out.writeString(image);
        out.writeLong(createdOn);
        out.writeParcelable(userInfo, flags);
    }
}
