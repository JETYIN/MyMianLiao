package com.tjut.mianliao.notice;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.notice.Notice;
import com.tjut.mianliao.qa.Answer;
import com.tjut.mianliao.qa.Question;
import com.tjut.mianliao.qa.QuestionActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class NoticeQaActivity extends NoticeListActivity implements OnClickListener {

    private LightDialog mMenuDialog;
    private Notice mNotice;

    @Override
    protected Item getItem(Notice notice) {
        Item item = super.getItem(notice);
        switch (notice.category) {
            case Notice.CAT_QA_QUEST_ANSWER:
            case Notice.CAT_QA_ANSWER_AT:
                if (notice.qaQuest != null && notice.qaAnswer != null) {
                    item.userInfo = notice.qaAnswer.userInfo;
                    item.time = notice.qaAnswer.createdOn;
                    int resId = notice.category == Notice.CAT_QA_QUEST_ANSWER
                            ? R.string.ntc_qa_quest_answer : R.string.ntc_qa_answer_at;
                    item.category = Utils.getColoredText(
                            getString(resId, notice.qaQuest.desc),
                            notice.qaQuest.desc, mKeyColor, false);
                    item.desc = notice.qaAnswer.desc;
                }
                break;

            case Notice.CAT_QA_ANSWER_CHOSEN:
                if (notice.qaQuest != null && notice.qaAnswer != null) {
                    item.userInfo = notice.qaQuest.userInfo;
                    item.time = notice.qaQuest.answerChosenOn;
                    item.category = Utils.getColoredText(
                            getString(R.string.ntc_qa_answer_accept, notice.qaQuest.desc),
                            notice.qaQuest.desc, mKeyColor, false);
                    item.desc = notice.qaAnswer.desc;
                }
                break;

            case Notice.CAT_QA_QUEST_AT:
                if (notice.qaQuest != null) {
                    item.userInfo = notice.qaQuest.userInfo;
                    item.time = notice.qaQuest.createdOn;
                    item.category = getString(R.string.ntc_qa_quest_at);
                    item.desc = notice.qaQuest.desc;
                }
                break;

            default:
                break;
        }
        return item;
    }


    @Override
    protected void onItemClick(Notice notice) {
        mNotice = notice;
        switch (notice.category) {
            case Notice.CAT_QA_QUEST_ANSWER:
                if (notice.qaQuest.hasAnswered()) {
                    viewQuestion(notice.qaQuest);
                } else {
                    showMenuDialog();
                }
                break;

            case Notice.CAT_QA_ANSWER_CHOSEN:
            case Notice.CAT_QA_QUEST_AT:
            case Notice.CAT_QA_ANSWER_AT:
                viewQuestion(notice.qaQuest);
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mMenuDialog) {
            switch (which) {
                case 0:
                    doAccept();
                    break;

                case 1:
                    viewQuestion(mNotice.qaQuest);
                    break;

                default:
                    break;
            }
        }
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
            mMenuDialog.setTitle(R.string.please_choose);
            mMenuDialog.setItems(R.array.ntc_qa_menu, this);
        }
        mMenuDialog.show();
    }

    private void viewQuestion(Question question) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Question.INTENT_EXTRA_NAME, question);
        startActivity(intent);
    }

    private void doAccept() {
        new AnswerTask(mNotice.qaQuest, mNotice.qaAnswer).execute();
    }

    private class AnswerTask extends MsTask {
        private Question mQuestion;
        private Answer mAnswer;

        AnswerTask(Question q, Answer a) {
            super(getApplicationContext(), MsRequest.QA_CHOOSE_ANSWER);
            mQuestion = q;
            mAnswer = a;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("question_id=").append(mQuestion.id)
                    .append("&answer_id=").append(mAnswer.id)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(response)) {
                mQuestion.answerChosen = mAnswer.id;
                mAdapter.notifyDataSetChanged();
                toast(R.string.qa_choose_answer_success);
            } else {
                toast(MsResponse.getFailureDesc(getRefContext(),
                        R.string.qa_choose_answer_failed, response.code));
            }
        }
    }
}
