package com.tjut.mianliao.data;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.duanqu.qupai.utils.FourCC;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.data.tribe.TribeTypeInfo;
import com.tjut.mianliao.util.JsonUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class LiveMemberInfo implements Parcelable{

    public int id;
    public int gender;
    public String avatar;

    public LiveMemberInfo() {

    }

    public static final Creator<LiveMemberInfo> CREATOR = new Creator<LiveMemberInfo>() {
        @Override
        public LiveMemberInfo createFromParcel(Parcel in) {
            return new LiveMemberInfo(in);
        }

        @Override
        public LiveMemberInfo[] newArray(int size) {
            return new LiveMemberInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public static final JsonUtil.ITransformer<LiveMemberInfo> TRANSFORMER =
            new JsonUtil.ITransformer<LiveMemberInfo>() {

                @Override
                public LiveMemberInfo transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    public static LiveMemberInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        LiveMemberInfo user = new LiveMemberInfo();
        user.id = json.optInt("uid");
        user.gender = json.optInt("gender");
        user.avatar = json.optString("icon");
        return user;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(gender);
        dest.writeString(avatar);

    }

    public LiveMemberInfo (Parcel in) {
        id = in.readInt();
        gender = in.readInt();
        avatar = in.readString();
    }


    public static boolean isFemale(int userGender) {
        return userGender == UserInfo.GENDER_FEMALE;
    }

    public static int getDefaultAvatar(int userGender) {
        return isFemale(userGender) ? R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy;
    }

    public int defaultAvatar() {
        return getDefaultAvatar(gender);
    }


}
