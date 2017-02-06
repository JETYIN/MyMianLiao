package com.tjut.mianliao.qa;

import java.util.ArrayList;

import org.json.JSONArray;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.image.ImageActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.Utils;

public class QaActivity extends BaseActivity implements
        View.OnClickListener, DialogInterface.OnClickListener {

    private static final int REQUEST_CODE_ASK = 100;
    private static final int REQUEST_CODE_UPDATE_QUESTION = 200;

    public static final String RESULT_QUESTION = "result_question";

    private MsRequest mRequest = MsRequest.QA_LIST_QUESTION;

    private TitleBar mTitleBar;
    private PopupView mPopupMenu;

    private PullToRefreshListView mPtrLvQuestions;
    private LoadQuestionTask mLastLoadTask;

    private ArrayList<Question> mQuestions = new ArrayList<Question>();

    private LightDialog mRecordDialog;
    private Question mOpRecord;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_qa;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTitleBar = getTitleBar();
        mTitleBar.showTitleArrow();
        mTitleBar.showTitleText(R.string.qa_menu_all, this);
        mTitleBar.showRightText(R.string.qa_ask, this);

        mPtrLvQuestions = (PullToRefreshListView) findViewById(R.id.ptrlv_questions);
        int padding = getResources().getDimensionPixelSize(R.dimen.card_margin_horizontal);
        mPtrLvQuestions.getRefreshableView().setPadding(padding, 0, padding, 0);
        mPtrLvQuestions.setAdapter(mAdapter);
        mPtrLvQuestions.setMode(Mode.BOTH);

        mPtrLvQuestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                viewQuestion((Question) parent.getItemAtPosition(position));
            }
        });

        mPtrLvQuestions.setOnRefreshListener(new OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadQuestions(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                loadQuestions(false);
            }
        });

        mTitleBar.showProgress();
        loadQuestions(true);
    }

    private void loadQuestions(boolean refresh) {
        if (mLastLoadTask == null) {
            if (Utils.isNetworkAvailable(this)) {
                int offset = refresh ? 0 : mQuestions.size();
                new LoadQuestionTask(offset).executeLong();
            } else {
                toast(R.string.no_network);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                Intent iq = new Intent(this, PostActivity.class);
                iq.putExtra(PostActivity.EXTRA_POST_TYPE, PostActivity.POST_ASK);
                startActivityForResult(iq, REQUEST_CODE_ASK);
                break;

            case R.id.tv_title:
                showMenu(v);
                break;

            case R.id.iv_image:
                if (v.getTag() != null && v.getTag() instanceof String) {
                    String url = (String) v.getTag();
                    Intent ivi = new Intent(this, ImageActivity.class);
                    ivi.putExtra(ImageActivity.EXTRA_IMAGE_URL, url);
                    startActivity(ivi);
                }
                break;

            case R.id.tv_user_name:
            case R.id.av_avatar:
                if (v.getTag() != null && v.getTag() instanceof Question) {
                    Question question = (Question) v.getTag();
                    Intent iu = new Intent(this, ProfileActivity.class);
                    iu.putExtra(UserInfo.INTENT_EXTRA_INFO, question.userInfo);
                    startActivity(iu);
                }
                break;

            case R.id.iv_menu:
                if (v.getTag() != null && v.getTag() instanceof Question) {
                    mOpRecord = (Question) v.getTag();
                    showRecordDialog();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mRecordDialog) {
            switch (which) {
                case 0:
                    viewQuestion(mOpRecord);
                    break;

                case 1:
                    PoliceHelper.reportQaQuestion(this, mOpRecord.id);
                    break;

                default:
                    break;
            }
        }
    }

    private void viewQuestion(Question question) {
        Intent iq = new Intent(this, QuestionActivity.class);
        iq.putExtra(Question.INTENT_EXTRA_NAME, question);
        startActivityForResult(iq, REQUEST_CODE_UPDATE_QUESTION);
    }

    private void showRecordDialog() {
        if (mRecordDialog == null) {
            mRecordDialog = new LightDialog(this);
            mRecordDialog.setTitle(R.string.please_choose);
            mRecordDialog.setItems(R.array.qa_record_menu, this);
        }
        mRecordDialog.show();
    }

    private void showMenu(View anchor) {
        if (mPopupMenu == null) {
            mPopupMenu = new PopupView(this);
            mPopupMenu.setWidth(this.getResources()
                    .getDimensionPixelSize(R.dimen.news_category_popup_width));
            mPopupMenu.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

            ArrayAdapter<QaMenuItem> adapter = new ArrayAdapter<QaMenuItem>(this,
                    R.layout.list_item_news_category, R.id.tv_news_category) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView tvCategory = (TextView) view.findViewById(R.id.tv_news_category);
                    tvCategory.setCompoundDrawablesWithIntrinsicBounds(
                            getItem(position).iconResId, 0, 0, 0);
                    return view;
                }
            };

            TypedArray ta = getResources().obtainTypedArray(R.array.qa_menu);
            for (int i = 0; i < ta.length(); i += 2) {
                QaMenuItem nc = new QaMenuItem();
                nc.value = ta.getString(i);
                nc.desResId = ta.getResourceId(i, 0);
                nc.iconResId = ta.getResourceId(i + 1, 0);
                adapter.add(nc);
            }
            ta.recycle();

            mPopupMenu.setAdapter(adapter);
            mPopupMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (mLastLoadTask != null) {
                        return;
                    }
                    QaMenuItem qmi = (QaMenuItem) parent.getItemAtPosition(position);
                    mTitleBar.showTitleText(qmi.value, QaActivity.this);
                    switch (qmi.desResId) {
                        case R.string.qa_menu_asked:
                            switchContent(MsRequest.QA_LIST_MY_QUESTION);
                            break;
                        case R.string.qa_menu_answered:
                            switchContent(MsRequest.QA_LIST_MY_ANSWERED);
                            break;
                        default:
                            switchContent(MsRequest.QA_LIST_QUESTION);
                            break;
                    }
                    mPopupMenu.dismiss();
                }
            });
        }

        mPopupMenu.showAsDropDown(anchor, true);
    }

    private void switchContent(MsRequest request) {
        if (mRequest != request) {
            mRequest = request;
            mPtrLvQuestions.setRefreshing(Mode.PULL_FROM_START);
        }
    }

    private BaseAdapter mAdapter = new BaseAdapter() {

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public int getCount() {
            return mQuestions.size();
        }

        @Override
        public Question getItem(int position) {
            return mQuestions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mQuestions.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = getLayoutInflater().inflate(R.layout.list_item_qa_question, parent, false);
            }

            Question question = mQuestions.get(position);

            NameView tvName = (NameView) view.findViewById(R.id.tv_user_name);
            tvName.setText(question.userInfo.getDisplayName(getApplicationContext()));
            tvName.setMedal(question.userInfo.primaryBadgeImage);
            tvName.setTag(question);
            tvName.setOnClickListener(QaActivity.this);

            String time = QaActivity.this.getString(R.string.news_published_on,
                    Utils.getTimeDesc(question.createdOn));
            ((TextView) view.findViewById(R.id.tv_extra_info)).setText(time);

            view.findViewById(R.id.tv_solved).setVisibility(
                    question.hasAnswered() ? View.VISIBLE : View.GONE);

            View ivMenu = view.findViewById(R.id.iv_menu);
            ivMenu.setTag(question);
            ivMenu.setOnClickListener(QaActivity.this);

            TextView tvDesc = (TextView) view.findViewById(R.id.tv_desc);
            if (TextUtils.isEmpty(question.desc)) {
                tvDesc.setVisibility(View.GONE);
            } else {
                tvDesc.setVisibility(View.VISIBLE);
                tvDesc.setMaxLines(3);
                tvDesc.setText(Utils.getRefFriendText(question.desc, getApplicationContext()));
            }

            ProImageView ivImage = (ProImageView) view.findViewById(R.id.iv_image);
            if (TextUtils.isEmpty(question.thumbnail)) {
                ivImage.setVisibility(View.GONE);
            } else {
                ivImage.setVisibility(View.VISIBLE);
                ivImage.setImage(question.thumbnail, R.drawable.bg_img_loading);
                ivImage.setTag(question.image);
                ivImage.setOnClickListener(QaActivity.this);
            }

            ProImageView avAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
            avAvatar.setImage(question.userInfo.getAvatar(), question.userInfo.defaultAvatar());
            avAvatar.setTag(question);
            avAvatar.setOnClickListener(QaActivity.this);

            ((TextView) view.findViewById(R.id.tv_answer_count))
                    .setText(String.valueOf(question.answerCount));

            return view;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ASK:
                    Question question = data.getParcelableExtra(Question.INTENT_EXTRA_NAME);
                    mQuestions.add(0, question);
                    mAdapter.notifyDataSetChanged();
                    break;

                case REQUEST_CODE_UPDATE_QUESTION:
                    Question rq = data.getParcelableExtra(RESULT_QUESTION);
                    if (rq != null) {
                        for (Question q : mQuestions) {
                            if (q.id == rq.id) {
                                q.answerCount = rq.answerCount;
                                q.answerChosen = rq.answerChosen;
                                q.answerChosenOn = rq.answerChosenOn;
                                mAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private class LoadQuestionTask extends MsTask {
        private static final int PAGE_SIZE = 15;

        private int mOffset;

        LoadQuestionTask(int offset) {
            super(getApplicationContext(), mRequest);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset)
                    .append("&limit=").append(PAGE_SIZE)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            mLastLoadTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mTitleBar.hideProgress();
            mPtrLvQuestions.onRefreshComplete();
            mLastLoadTask = null;
            if (MsResponse.isSuccessful(response)) {
                if (mOffset == 0) {
                    mQuestions.clear();
                }
                JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                for (int i = 0; i < ja.length(); i++) {
                    Question question = Question.fromJson(ja.optJSONObject(i));
                    if (question != null) {
                        mQuestions.add(question);
                    }
                }
                mAdapter.notifyDataSetChanged();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.qa_get_data_failed, response.code));
            }
        }
    }

    private static class QaMenuItem {
        String value;
        int desResId;
        int iconResId;

        @Override
        public String toString() {
            return value;
        }
    }
}