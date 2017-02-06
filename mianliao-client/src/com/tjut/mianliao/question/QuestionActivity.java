package com.tjut.mianliao.question;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Question;
import com.tjut.mianliao.util.AdvAsyncTask;

public class QuestionActivity extends BaseActivity implements OnClickListener {

    private View mSplashLayout;

    private View mQuestionLayout;

    private ProgressBar mProgressBar;

    private TextView mNumberView;

    private TextView mContentView;

    private TextView mCommentView;

    private ImageView mResultView;

    private Button mBeginButton;

    private Button mExitButton;

    private Button mCorrectButton;

    private Button mWrongButton;

    private Button mNextButton;

    private QuestionManager mQuestionManager;

    private GetQuestionTask mGetQuestionTask;

    private PostAnswerTask mPostAnswerTask;

    private int mQuestionNumber = 1;

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_question;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSplashLayout = findViewById(R.id.layout_question_splash);
        mBeginButton = (Button) mSplashLayout.findViewById(R.id.btn_begin);
        mBeginButton.setOnClickListener(this);
        mExitButton = (Button) mSplashLayout.findViewById(R.id.btn_exit);
        mExitButton.setOnClickListener(this);

        mQuestionLayout = findViewById(R.id.layout_question);
        mProgressBar = (ProgressBar) mQuestionLayout.findViewById(R.id.pb_loading);
        mNumberView = (TextView) mQuestionLayout.findViewById(R.id.tv_number);
        mContentView = (TextView) mQuestionLayout.findViewById(R.id.tv_content);
        mCommentView = (TextView) mQuestionLayout.findViewById(R.id.tv_comment);
        mResultView = (ImageView) mQuestionLayout.findViewById(R.id.iv_result);

        mCorrectButton = (Button) mQuestionLayout.findViewById(R.id.btn_correct);
        mCorrectButton.setOnClickListener(this);
        mWrongButton = (Button) mQuestionLayout.findViewById(R.id.btn_wrong);
        mWrongButton.setOnClickListener(this);
        mNextButton = (Button) mQuestionLayout.findViewById(R.id.btn_next);
        mNextButton.setOnClickListener(this);

        mQuestionManager = new QuestionManager(getApplicationContext());
    }

    @Override
    protected void onStart() {
        mQuestionManager.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        cancelTask(mGetQuestionTask);
        cancelTask(mPostAnswerTask);
        mQuestionManager.stop();
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Question question = mQuestionManager.getCurrentQuestion();
        switch (v.getId()) {
            case R.id.btn_begin:
                mSplashLayout.setVisibility(View.GONE);
                mQuestionLayout.setVisibility(View.VISIBLE);
                showNextQuestion();
                break;

            case R.id.btn_exit:
                finish();
                break;

            case R.id.btn_correct:
                question.setUserAnswer(Question.CORRECT);
                checkAnswer(question);
                break;

            case R.id.btn_wrong:
                question.setUserAnswer(Question.WRONG);
                checkAnswer(question);
                break;

            case R.id.btn_next:
                if (question.isEnd()) {
                    finish();
                } else {
                    showNextQuestion();
                }
                break;

            default:
                break;
        }
    }

    private void showNextQuestion() {
        cancelTask(mGetQuestionTask);
        new GetQuestionTask().executeLong();
    }

    private void checkAnswer(Question question) {
        mResultView.setImageResource(question.isCorrect() ?
                R.drawable.ic_question_correct : R.drawable.ic_question_wrong);
        mResultView.setVisibility(View.VISIBLE);
        mCommentView.setText(question.getComment());
        mCorrectButton.setVisibility(View.GONE);
        mWrongButton.setVisibility(View.GONE);
        mNextButton.setVisibility(View.VISIBLE);
        postAnswer();
    }

    private void postAnswer() {
        cancelTask(mPostAnswerTask);
        new PostAnswerTask().executeLong();
    }

    @SuppressWarnings("rawtypes")
    private void cancelTask(AdvAsyncTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
    }

    private class GetQuestionTask extends AdvAsyncTask<Void, Void, Question> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.GONE);
            mGetQuestionTask = this;
        }

        @Override
        protected Question doInBackground(Void... params) {
            return mQuestionManager.produceQuestion();
        }

        @Override
        protected void onPostExecute(Question question) {
            mProgressBar.setVisibility(View.GONE);
            mResultView.setVisibility(View.GONE);
            mGetQuestionTask = null;
            if (question != null) {
                if (question.isEnd()) {
                    mNumberView.setText("");
                    mContentView.setText(R.string.quest_no_more);
                    mCommentView.setText(R.string.quest_congrat);
                    mCorrectButton.setVisibility(View.GONE);
                    mWrongButton.setVisibility(View.GONE);
                    mNextButton.setText(R.string.quest_exit);
                    mNextButton.setVisibility(View.VISIBLE);
                } else {
                    mNumberView.setText(getResources().getString(R.string.quest_number, mQuestionNumber++));
                    mContentView.setText(question.getContent());
                    mCommentView.setText("");
                    mCorrectButton.setVisibility(View.VISIBLE);
                    mWrongButton.setVisibility(View.VISIBLE);
                    mNextButton.setVisibility(View.GONE);
                }
            }
        }
    }

    private class PostAnswerTask extends AdvAsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            mPostAnswerTask = this;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mQuestionManager.postAnswer();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mPostAnswerTask = null;
        }
    }
}
