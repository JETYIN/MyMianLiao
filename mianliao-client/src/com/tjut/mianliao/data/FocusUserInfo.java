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

public class FocusUserInfo implements Parcelable{
    
    public int id;
    public int gender;
    public int followsTotal;
    public String nickName;
    public String avatar;
    public String school;
    public boolean isFollow;
    public ArrayList<String> followsList;
    
    public FocusUserInfo() {
        
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final JsonUtil.ITransformer<FocusUserInfo> TRANSFORMER = 
            new JsonUtil.ITransformer<FocusUserInfo>() {

        @Override
        public FocusUserInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };    
    
    public static FocusUserInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        FocusUserInfo focusUserInfo = new FocusUserInfo();
        focusUserInfo.id = json.optInt("id");
        focusUserInfo.gender = json.optInt("gender");
        focusUserInfo.followsTotal = json.optInt("recommen_friend_count");
        focusUserInfo.nickName = json.optString("nick");
        focusUserInfo.avatar = json.optString("avatar");
        focusUserInfo.school = json.optString("school");
        focusUserInfo.isFollow = json.optBoolean("is_follow");
        try {
            focusUserInfo.followsList = JsonUtil.getStringArray(json.getJSONArray("recommen_friend_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return focusUserInfo;
    } 
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(gender);
        dest.writeInt(followsTotal);
        dest.writeString(nickName);
        dest.writeString(avatar);
        dest.writeString(school);
        int size = getFollowsCount();
        if (size > 0) {
            for (String name : followsList) {
                dest.writeString(name);
            }
        }
    }
    
    public FocusUserInfo (Parcel in) {
        id = in.readInt();
        gender = in.readInt();
        followsTotal = in.readInt();
        nickName = in.readString();
        avatar = in.readString();
        school = in.readString();
        int size = in.readInt();
        if (size > 0) {
            followsList = new ArrayList<String>(size);
            for (int i = 0; i < size; i++) {
                String name = in.readString();
                followsList.add(name);
            }
        }
    }
    
    public int getFollowsCount() {
        return followsList == null ? 0 : followsList.size();
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
