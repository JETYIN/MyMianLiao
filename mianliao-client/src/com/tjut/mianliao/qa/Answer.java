package com.tjut.mianliao.qa;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Answer extends QaRecord {

    public static final String INTENT_EXTRA_NAME = "Answer";

    public Answer() {}

    public static Answer fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Answer answer = new Answer();
        QaRecord.fillFromJson(answer, json);
        return answer;
    }

    public static final Parcelable.Creator<Answer> CREATOR = new Parcelable.Creator<Answer>() {
        @Override
        public Answer createFromParcel(Parcel in) {
            return new Answer(in);
        }

        @Override
        public Answer[] newArray(int size) {
            return new Answer[size];
        }
    };

    private Answer(Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}
