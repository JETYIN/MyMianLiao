package com.tjut.mianliao.data.bounty;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Credit implements Parcelable {
    public int point;
    public int level;
    public int ratingP;
    public int ratingM;
    public int ratingN;

    public Credit() {}

    public static Credit fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Credit credit = new Credit();
        credit.point = json.optInt("point");
        credit.level = json.optInt("level");
        credit.ratingP = json.optInt("rating_p");
        credit.ratingM = json.optInt("rating_m");
        credit.ratingN = json.optInt("rating_n");
        return credit;
    }

    public static final Parcelable.Creator<Credit> CREATOR =
            new Parcelable.Creator<Credit>() {
        @Override
        public Credit createFromParcel(Parcel in) {
            return new Credit(in);
        }

        @Override
        public Credit[] newArray(int size) {
            return new Credit[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public Credit(Parcel in) {
        point = in.readInt();
        level = in.readInt();
        ratingP = in.readInt();
        ratingM = in.readInt();
        ratingN = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(point);
        out.writeInt(level);
        out.writeInt(ratingP);
        out.writeInt(ratingM);
        out.writeInt(ratingN);
    }
}
