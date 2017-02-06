package com.tjut.mianliao.qa;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class Question extends QaRecord {

    public static final String INTENT_EXTRA_NAME = "Question";

    public int answerCount;
    public int answerChosen;
    public long answerChosenOn;

    public Question() {}

    public static Question fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Question question = new Question();
        QaRecord.fillFromJson(question, json);
        question.answerCount = json.optInt("answer_count");
        question.answerChosen = json.optInt("answer_chosen");
        question.answerChosenOn = json.optInt("answer_chosen_on");
        return question;
    }

    public boolean hasAnswered() {
        return answerChosen > 0;
    }

    public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

    private Question(Parcel in) {
        super(in);
        answerCount = in.readInt();
        answerChosen = in.readInt();
        answerChosenOn = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(answerCount);
        dest.writeInt(answerChosen);
        dest.writeLong(answerChosenOn);
    }
}
