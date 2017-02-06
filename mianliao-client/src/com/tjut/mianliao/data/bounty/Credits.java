package com.tjut.mianliao.data.bounty;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.tjut.mianliao.data.contact.UserInfo;

public class Credits implements Parcelable {
    public static final String INTENT_EXTRA_NAME = "Credits";

    public static final String HOST = "host";
    public static final String GUEST = "guest";

    public UserInfo userInfo;
    public Credit host;
    public Credit guest;

    public Credits() {}

    public static Credits fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Credits credits = new Credits();
        credits.userInfo = UserInfo.fromJson(json);

        JSONObject creditJson = json.optJSONObject("user_credit");
        if (creditJson != null) {
            credits.host = Credit.fromJson(creditJson.optJSONObject(HOST));
            credits.guest = Credit.fromJson(creditJson.optJSONObject(GUEST));
        }

        return credits;
    }

    public int getCreditLevel(boolean isHost) {
        Credit credit = isHost ? host : guest;
        return credit == null ? 0 : credit.level;
    }

    public static final Parcelable.Creator<Credits> CREATOR =
            new Parcelable.Creator<Credits>() {
        @Override
        public Credits createFromParcel(Parcel in) {
            return new Credits(in);
        }

        @Override
        public Credits[] newArray(int size) {
            return new Credits[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Credits(Parcel in) {
        userInfo = in.readParcelable(UserInfo.class.getClassLoader());
        host = in.readParcelable(Credit.class.getClassLoader());
        guest = in.readParcelable(Credit.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(userInfo, flags);
        out.writeParcelable(host, flags);
        out.writeParcelable(guest, flags);
    }
}
