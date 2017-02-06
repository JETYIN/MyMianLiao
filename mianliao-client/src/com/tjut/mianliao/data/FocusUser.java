package com.tjut.mianliao.data;

import java.util.ArrayList;

import org.json.JSONObject;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.JsonUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class FocusUser implements Parcelable {

    public int id;
    public String nick;
    public String avatar;
    public int gender;
    public boolean isFollow;
    public String school;
    public int count;
    public ArrayList<String> ruNames;
    public int ruCount;

    public FocusUser() {}
    
    public static final JsonUtil.ITransformer<FocusUser> TRANSFORMER = new JsonUtil.ITransformer<FocusUser>() {

        @Override
        public FocusUser transform(JSONObject json) {
            return fromJson(json);
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    protected static FocusUser fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        FocusUser focusUser = new FocusUser();
        focusUser.id = json.optInt("id");
        focusUser.nick = json.optString("nick");
        focusUser.avatar = json.optString("avatar");
        focusUser.gender = json.optInt("gender");
        focusUser.isFollow = json.optBoolean("is_follow");
        focusUser.school = json.optString("school");
        focusUser.count = json.optInt("count");
        focusUser.ruNames = JsonUtil.getStringArray(json.optJSONArray("recommen_friend_name"));
        focusUser.ruCount = json.optInt("recommen_friend_count");
        return focusUser;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nick);
        dest.writeString(avatar);
        dest.writeInt(gender);
        dest.writeInt(isFollow ? 1 : 0);
        dest.writeString(school);
        dest.writeInt(count);
    }
    
    public FocusUser(Parcel source) {
        id = source.readInt();
        nick = source.readString();
        avatar = source.readString();
        gender = source.readInt();
        isFollow = source.readInt() == 1;
        school = source.readString();
        count = source.readInt();
    }

    public int getGenderIcon() {
        return isFemale(gender) ? R.drawable.img_girl : R.drawable.img_boy;
    }
    
    public int defaultAvatar() {
        return getDefaultAvatar(gender);
    }

    public static boolean isFemale(int userGender) {
        return userGender == UserInfo.GENDER_FEMALE;
    }

    public static int getDefaultAvatar(int userGender) {
        return isFemale(userGender) ? R.drawable.chat_botton_bg_favicongirl : R.drawable.chat_botton_bg_faviconboy;
    }
    
    public static final Creator<FocusUser> CREATOR =
            new Creator<FocusUser>() {
                
                @Override
                public FocusUser[] newArray(int size) {
                    return new FocusUser[size];
                }
                
                @Override
                public FocusUser createFromParcel(Parcel source) {
                    return new FocusUser(source);
                }
            };

}
