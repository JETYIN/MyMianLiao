package com.tjut.mianliao.question;

import java.util.concurrent.BlockingQueue;

import android.content.Context;

import com.tjut.mianliao.data.Question;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.Utils;

public class AnswerPoster extends Thread {

    private static final String TAG = "AnswerPoster";

    private static final String API_QUESTION = "api/question";

    private static final String ACTION_ANSWER = "answer";

    private static final String PARAM_QID = "qid=";

    private static final String PARAM_ANSWER = "answer=";

    private static final long SLEEP_INTERVAL = 1000L;

    private Context mContext;

    private BlockingQueue<Question> mAnsweredQuests;

    public AnswerPoster(Context context, BlockingQueue<Question> queue) {
        mContext = context;
        mAnsweredQuests = queue;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Question q = mAnsweredQuests.take();
                if (q != null && q.isAnswered()) {
                    post(q);
                }
                sleep(SLEEP_INTERVAL);
            }
        } catch (InterruptedException e) {
            Utils.logD(TAG, "InterruptedException: " + e.getMessage());
        }
    }

    private void post(Question question) {
        String params = new StringBuilder(PARAM_QID).append(question.getQid())
                .append("&").append(PARAM_ANSWER)
                .append(question.isCorrect() ? 1 : 0)
                .toString();
        Utils.logD(TAG, "Post answer: " + params);
        HttpUtil.post(mContext, API_QUESTION, ACTION_ANSWER, params);
    }
}
