package com.tjut.mianliao.question;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Question;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.Utils;

public class QuestionProducer extends Thread {

    private static final String TAG = "QuestionProducer";

    private static final String API_QUESTION = "api/question";

    private static final String ACTION_NEXT = "next";

    private static final String PARAM_QIDS = "qids=";

    private static final long SLEEP_INTERVAL = 1000L;

    private Context mContext;

    private BlockingQueue<Question> mQuestions;

    private List<String> mExcludedQids;

    public QuestionProducer(Context context, BlockingQueue<Question> queue, List<String> excludedQids) {
        mContext = context;
        mQuestions = queue;
        mExcludedQids = excludedQids;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Question question = produce();
                if (question != null) {
                    mQuestions.put(question);
                    if (question.isEnd()) {
                        break;
                    }
                    mExcludedQids.add(question.getQid());
                }
                sleep(SLEEP_INTERVAL);
            }
        } catch (InterruptedException e) {
            Utils.logD(TAG, "InterruptedException: " + e.getMessage());
        }
    }

    private Question produce() {
        Question question = null;
        String excludedQids = TextUtils.join(",", mExcludedQids);
        Utils.logD(TAG, "Excluded qids:" + excludedQids);

        AccountInfo account = AccountInfo.getInstance(mContext);
        StringBuffer sb = new StringBuffer();
        sb.append(Utils.getServerAddress() + API_QUESTION);
        sb.append("?request=" + ACTION_NEXT);
        sb.append("&uid=" + account.getUserId());
        sb.append("&token=" + Utils.urlEncode(account.getToken()));
        sb.append("&" + PARAM_QIDS + excludedQids);
        String req = sb.toString();
        try {
            String response = HttpUtil.get(mContext, req);
            JSONObject json = new JSONObject(response);
            if (json != null) {
                int code = json.optInt(MsResponse.PARAM_CODE);
                if (MsResponse.MS_SUCCESS == code) {
                    question = Question.fromJSONString(json.optString(MsResponse.PARAM_RESPONSE));
                } else if (MsResponse.MS_QUESTION_NO_MORE == code) {
                    question = Question.createEndingQuestion();
                }
            }
        } catch (IOException e) {
            Utils.logD(TAG, "Get Request: " + req);
            Utils.logD(TAG, "Get Error: " + e.getMessage());
        } catch (JSONException e) {
            Utils.logD(TAG, "Get Request: " + req);
            Utils.logD(TAG, "Error: " + e.getMessage());
        } catch (Exception e) {
            Utils.logD(TAG, "Get Request: " + req);
            Utils.logD(TAG, "Error: " + e.getMessage());
        }

        Utils.logD(TAG, "Produce question: " + question);
        return question;
    }
}
