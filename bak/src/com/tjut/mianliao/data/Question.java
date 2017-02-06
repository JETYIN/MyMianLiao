package com.tjut.mianliao.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.tjut.mianliao.util.Utils;

public class Question {

    private static final String TAG = "Question";

    public static final int NA = -1;

    public static final int CORRECT = 1;

    public static final int WRONG = 0;

    private String mQid;

    private String mContent;

    private String mComment;

    private int mOriginAnswer;

    private int mUserAnswer = NA;

    private boolean mEnd;

    private Question(String qid, String content, String comment, int answer, boolean end) {
        mQid = qid;
        mContent = content;
        mComment = comment;
        mOriginAnswer = answer;
        mEnd = end;
    }

    public static Question createEndingQuestion() {
        return new Question("", "", "", NA, true);
    }

    public static Question fromJSONString(String jsonString) {
        Question q = null;
        if (jsonString != null && jsonString.length() > 0) {
            try {
                JSONObject json = new JSONObject(jsonString);
                q = new Question(json.getString("id"),
                        json.getString("question"),
                        json.getString("comment"),
                        json.getBoolean("answer") ? CORRECT : WRONG,
                        false);
            } catch (JSONException e) {
                Utils.logE(TAG, "JSONException: " + e.getMessage());
            }
        }
        return q;
    }

    public String getQid() {
        return mQid;
    }

    public String getContent() {
        return mContent;
    }

    public String getComment() {
        return mComment;
    }

    public int getUserAnswer() {
        return mUserAnswer;
    }

    public void setUserAnswer(int answer) {
        mUserAnswer = answer;
    }

    public boolean isCorrect() {
        return mOriginAnswer == mUserAnswer;
    }

    public boolean isAnswered() {
        return mUserAnswer != NA;
    }

    public boolean isEnd() {
        return mEnd;
    }

    @Override
    public String toString() {
        return new StringBuilder("qid = ").append(mQid)
                .append(", content = ").append(mContent)
                .append(", comment = ").append(mComment)
                .append(", origin_answer = ").append(mOriginAnswer)
                .append(", user_answer = ").append(mUserAnswer)
                .toString();
    }
}
