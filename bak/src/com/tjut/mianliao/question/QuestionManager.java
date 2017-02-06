package com.tjut.mianliao.question;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Question;
import com.tjut.mianliao.util.Utils;

public class QuestionManager {

    private static final String TAG = "QuestionManager";

    private static final int QUEUE_SIZE = 2;

    private Context mContext;

    private BlockingQueue<Question> mQuestions;

    private BlockingQueue<Question> mAnsweredQuests;

    private Question mCurrentQuestion;

    private QuestionProducer mQuestoinPoducer;

    private AnswerPoster mAnswerPoster;

    private List<String> mExcludedQids;

    public QuestionManager(Context context) {
        mContext = context;
        mQuestions = new ArrayBlockingQueue<Question>(QUEUE_SIZE);
        mAnsweredQuests = new ArrayBlockingQueue<Question>(QUEUE_SIZE);
        mExcludedQids = new ArrayList<String>();
    }

    public Question produceQuestion() {
        if (mQuestions.isEmpty() && !Utils.isNetworkAvailable(mContext)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,
                            R.string.lgi_network_unavailable, Toast.LENGTH_LONG).show();
                }
            });
        }
        try {
            mCurrentQuestion = mQuestions.take();
        } catch (InterruptedException e) {
            Utils.logD(TAG, "produceQuestion InterruptedException: " + e.getMessage());
        }
        return mCurrentQuestion;
    }

    public void postAnswer() {
        try {
            mAnsweredQuests.put(mCurrentQuestion);
        } catch (InterruptedException e) {
            Utils.logD(TAG, "postAnswer InterruptedException: " + e.getMessage());
        }
    }

    public void start() {
        mQuestoinPoducer = new QuestionProducer(mContext, mQuestions, mExcludedQids);
        mQuestoinPoducer.start();

        mAnswerPoster = new AnswerPoster(mContext, mAnsweredQuests);
        mAnswerPoster.start();
    }

    public void stop() {
        if (mQuestoinPoducer != null) {
            mQuestoinPoducer.interrupt();
        }

        if (mAnswerPoster != null) {
            mAnswerPoster.interrupt();
        }
    }

    public Question getCurrentQuestion() {
        return mCurrentQuestion;
    }
}
