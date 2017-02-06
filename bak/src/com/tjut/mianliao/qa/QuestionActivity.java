package com.tjut.mianliao.qa;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.Utils;

public class QuestionActivity extends BaseActivity implements OnRefreshListener2<ListView>,
        View.OnClickListener, DialogInterface.OnClickListener {

    public static final int REQUEST_CODE_ANSWER = 100;

    private int mCardItemSpacing;
    private int mPaddingBottom;

    private PullToRefreshListView mPtrLvAnswers;
    private LoadAnswersTask mLastLoadTask;
    private AnswerTask mLastAnswerTask;
    private Answer mToBeAnswer;

    private LightDialog mMenuDialog;
    private LightDialog mChooseAnswerDialog;

    private Question mQuestion;
    private ArrayList<Answer> mAnswers = new ArrayList<Answer>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_qa_question;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getTitleBar().showTitleText(R.string.qa_question, null);
        getTitleBar().showRightButton(R.drawable.btn_title_bar_more, this);

        mQuestion = getIntent().getParcelableExtra(Question.INTENT_EXTRA_NAME);
        if (mQuestion == null) {
            toast(R.string.qa_invalid_question);
            finish();
            return;
        }

        mPaddingBottom = getResources().getDimensionPixelOffset(R.dimen.qa_reply_padding_bottom);
        mCardItemSpacing = getResources().getDimensionPixelOffset(R.dimen.card_item_spacing);

        mPtrLvAnswers = (PullToRefreshListView) findViewById(R.id.ptrlv_answers);
        int padding = getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal);
        mPtrLvAnswers.getRefreshableView().setPadding(padding, 0, padding, 0);
        mPtrLvAnswers.setAdapter(mAdapter);
        mPtrLvAnswers.setMode(Mode.BOTH);
        mPtrLvAnswers.setOnRefreshListener(this);

        getTitleBar().showProgress();
        loadAnswers(true);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        loadAnswers(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        loadAnswers(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                mToBeAnswer = null;
                showMenuDialog();
                break;

            case R.id.iv_menu:
                if (v.getTag() != null && v.getTag() instanceof Answer) {
                    mToBeAnswer = (Answer) v.getTag();
                    showMenuDialog();
                }
                break;

            case R.id.iv_image:
                if (v.getTag() != null && v.getTag() instanceof String) {
                    String url = (String) v.getTag();
                    Intent ivi = new Intent(this, ImageActivity.class);
                    ivi.putExtra(ImageActivity.EXTRA_IMAGE_URL, url);
                    startActivity(ivi);
                }
                break;

            case R.id.btn_answer:
                answerQuestion();
                break;

            case R.id.tv_user_name:
            case R.id.av_avatar:
                if (v.getTag() != null && v.getTag() instanceof QaRecord) {
                    QaRecord record = (QaRecord) v.getTag();
                    Intent iu = new Intent(this, ProfileActivity.class);
                    iu.putExtra(UserInfo.INTENT_EXTRA_INFO, record.userInfo);
                    startActivity(iu);
                }
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
                    if (mToBeAnswer == null) {
                        PoliceHelper.reportQaQuestion(this, mQuestion.id);
                    } else {
                        PoliceHelper.reportQaAnswer(this, mToBeAnswer.id);
                    }
                    break;

                case 1:
                    if (mToBeAnswer == null) {
                        answerQuestion();
                    } else {
                        showChooseAnswerDialog();
                    }
                    break;

                default:
                    break;
            }
        } else if (dialog == mChooseAnswerDialog) {
            new AnswerTask(mToBeAnswer).executeLong();
        }
    }

    private void answerQuestion() {
        Intent iq = new Intent(this, PostActivity.class);
        iq.putExtra(PostActivity.EXTRA_POST_TYPE, PostActivity.POST_ANSWER);
        iq.putExtra(Question.INTENT_EXTRA_NAME, mQuestion);
        startActivityForResult(iq, REQUEST_CODE_ANSWER);
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
            mMenuDialog.setTitle(R.string.please_choose);
        }

        List<String> items = new ArrayList<String>();
        items.add(getString(R.string.report));
        if (mToBeAnswer == null) {
            items.add(getString(R.string.qa_answer));
        } else if (!mQuestion.hasAnswered() && mQuestion.userInfo.isMine(this)
                && mLastAnswerTask == null) {
            items.add(getString(R.string.qa_choose_answer));
        }

        mMenuDialog.setItems(items, this);
        mMenuDialog.show();
    }

    private void showChooseAnswerDialog() {
        if (mChooseAnswerDialog == null) {
            mChooseAnswerDialog = new LightDialog(this);
            mChooseAnswerDialog.setTitle(R.string.qa_choose_answer);
            mChooseAnswerDialog.setMessage(R.string.qa_choose_answer_desc);
            mChooseAnswerDialog.setNegativeButton(android.R.string.cancel, null);
            mChooseAnswerDialog.setPositiveButton(android.R.string.ok, this);
        }
        mChooseAnswerDialog.show();
    }

    private void loadAnswers(boolean refresh) {
        if (mLastLoadTask == null) {
            if (Utils.isNetworkAvailable(this)) {
                int offset = refresh ? 0 : mAnswers.size();
                new LoadAnswersTask(offset).executeLong();
            } else {
                toast(R.string.no_network);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ANSWER && resultCode == RESULT_OK) {
            Answer answer = data.getParcelableExtra(Answer.INTENT_EXTRA_NAME);
            if (mQuestion.hasAnswered()) {
                mAnswers.add(1, answer);
            } else {
                mAnswers.add(0, answer);
            }
            mQuestion.answerCount += 1;

            Intent ir = new Intent();
            ir.putExtra(QaActivity.RESULT_QUESTION, mQuestion);
            setResult(RESULT_OK, ir);

            mAdapter.notifyDataSetChanged();
        }
    }

    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public int getCount() {
            return mAnswers.size() + 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? 0 : 1;
        }

        @Override
        public Object getItem(int position) {
            return position == 0 ? mQuestion : mAnswers.get(position - 1);
        }

        @Override
        public long getItemId(int position) {
            return position == 0 ? mQuestion.id : mAnswers.get(position - 1).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            boolean isQuestion = getItemViewType(position) == 0;
            if (convertView != null) {
                view = convertView;
            } else {
                int layoutId = isQuestion ? R.layout.list_item_qa_answer_q
                        : R.layout.list_item_qa_answer_a;
                view = getLayoutInflater().inflate(layoutId, parent, false);
            }
            QaRecord record = (QaRecord) getItem(position);

            NameView tvName = (NameView) view.findViewById(R.id.tv_user_name);
            tvName.setText(record.userInfo.getDisplayName(getApplicationContext()));
            tvName.setMedal(record.userInfo.primaryBadgeImage);
            tvName.setTag(record);
            tvName.setOnClickListener(QuestionActivity.this);

            String time = QuestionActivity.this.getString(R.string.news_published_on,
                    Utils.getTimeDesc(record.createdOn));
            ((TextView) view.findViewById(R.id.tv_extra_info)).setText(time);

            TextView tvDesc = (TextView) view.findViewById(R.id.tv_desc);
            if (TextUtils.isEmpty(record.desc)) {
                tvDesc.setVisibility(View.GONE);
            } else {
                tvDesc.setVisibility(View.VISIBLE);
                tvDesc.setText(Utils.getRefFriendText(record.desc, getApplicationContext()));
            }

            ProImageView ivImage = (ProImageView) view.findViewById(R.id.iv_image);
            if (TextUtils.isEmpty(record.thumbnail)) {
                ivImage.setVisibility(View.GONE);
            } else {
                ivImage.setVisibility(View.VISIBLE);
                ivImage.setImage(record.thumbnail, R.drawable.bg_img_loading);
                ivImage.setTag(record.image);
                ivImage.setOnClickListener(QuestionActivity.this);
            }

            ProImageView avAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
            avAvatar.setImage(record.userInfo.getAvatar(), record.userInfo.defaultAvatar());
            avAvatar.setTag(record);
            avAvatar.setOnClickListener(QuestionActivity.this);

            if (!isQuestion) {
                View ivMenu = view.findViewById(R.id.iv_menu);
                ivMenu.setTag(record);
                ivMenu.setOnClickListener(QuestionActivity.this);

                if (position == 1 && mQuestion.hasAnswered()) {
                    view.findViewById(R.id.iv_chosen).setVisibility(View.VISIBLE);
                }
            }

            updateBackground(view, position);

            return view;
        }

        private void updateBackground(View view, int position) {
            if (position == 0) {
                return;
            }

            int size = mAnswers.size();
            int resId;
            int pb = mPaddingBottom;

            if ((!mQuestion.hasAnswered() && size == 1)
                    || (mQuestion.hasAnswered() && (size <= 2 || position == 1))) {
                resId = R.drawable.bg_card_with_bottom_space;
                pb += mCardItemSpacing;
            } else if ((!mQuestion.hasAnswered() && position == 1)
                    || (mQuestion.hasAnswered() && position == 2)) {
                resId = R.drawable.bg_card_head;
            } else if (position == size) {
                resId = R.drawable.bg_card_bottom_with_space;
                pb += mCardItemSpacing;
            } else {
                resId = R.drawable.bg_card_middle;
            }
            view.setBackgroundResource(resId);
            view.setPadding(0, 0, 0, pb);
        }
    };

    private class LoadAnswersTask extends MsTask {
        private static final int PAGE_SIZE = 15;

        private int mOffset;

        LoadAnswersTask(int offset) {
            super(getApplicationContext(), MsRequest.QA_LIST_ANSWER);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("id=").append(mQuestion.id)
                    .append("&offset=").append(mOffset)
                    .append("&limit=").append(PAGE_SIZE)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            mLastLoadTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrLvAnswers.onRefreshComplete();
            mLastLoadTask = null;
            if (MsResponse.isSuccessful(response)) {
                if (mOffset == 0) {
                    mAnswers.clear();
                }
                JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                for (int i = 0; i < ja.length(); i++) {
                    Answer answer = Answer.fromJson(ja.optJSONObject(i));
                    if (answer != null) {
                        mAnswers.add(answer);
                    }
                }
                mAdapter.notifyDataSetChanged();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.qa_get_data_failed, response.code));
            }
        }
    }

    private class AnswerTask extends MsTask {

        private Answer mAnswer;

        AnswerTask(Answer answer) {
            super(getApplicationContext(), MsRequest.QA_CHOOSE_ANSWER);
            mAnswer = answer;
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
            mLastAnswerTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mLastAnswerTask = null;
            if (MsResponse.isSuccessful(response)) {
                // Move this answer to the first position
                mAnswers.remove(mAnswer);
                mAnswers.add(0, mAnswer);

                mQuestion.answerChosen = mAnswer.id;
                mQuestion.answerChosenOn = System.currentTimeMillis() / 1000L;
                Intent ir = new Intent();
                ir.putExtra(QaActivity.RESULT_QUESTION, mQuestion);
                setResult(RESULT_OK, ir);

                mAdapter.notifyDataSetChanged();
                mPtrLvAnswers.getRefreshableView().setSelection(0);
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.qa_choose_answer_failed, response.code));
            }
        }
    }
}