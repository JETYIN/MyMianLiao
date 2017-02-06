package com.tjut.mianliao.news;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.NewsComment;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.ReplyActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.Utils;

public class NewsCommentActivity extends BaseActivity implements
        DialogInterface.OnClickListener, View.OnClickListener,
        OnRefreshListener2<ListView>, TaskExecutionListener {

    private static final int REQUEST_REPLY = 101;

    private PullToRefreshListView mPtrListView;
    private TextView mTvHeader;
    private NewsManager mNewsManager;
    private LightDialog mMenuDialog;
    private NewsCommentAdapter mAdapter;

    private News mNews;
    private NewsComment mTarget;
    private int mCmtCountColor;
    private boolean mRefresh;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_news_comment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNews = getIntent().getParcelableExtra(News.INTENT_EXTRA_NAME);
        if (mNews == null) {
            toast(R.string.news_tst_not_found);
            finish();
            return;
        }

        getTitleBar().showTitleText(R.string.news_comment_title, null);

        mNewsManager = NewsManager.getInstance(this);
        mNewsManager.registerTaskListener(this);
        mCmtCountColor = getResources().getColor(R.color.news_marked);

        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news_comments);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setMode(Mode.BOTH);

        ListView listView = mPtrListView.getRefreshableView();
        mTvHeader = (TextView) mInflater.inflate(
                R.layout.news_comment_list_header, listView, false);
        listView.addHeaderView(mTvHeader);
        updateHeader();

        mAdapter = new NewsCommentAdapter();
        mPtrListView.setAdapter(mAdapter);

        getTitleBar().showProgress();
        fetchComments(true);
    }

    @Override
    protected void onDestroy() {
        if (mNewsManager != null) {
            mNewsManager.unregisterTaskListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0: // Reply
                showReplyActivity();
                break;

            case 1: // Report
                PoliceHelper.reportNewsComment(this, mTarget.id);
                break;

            case 2: // Delete
                getTitleBar().showProgress();
                mNewsManager.startNewsDeleteCommentTask(mNews, mTarget);
                break;

            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_comment:
                mTarget = null;
                showReplyActivity();
                break;

            case R.id.top_right_item:
                if (v.getTag() != null && v.getTag() instanceof NewsComment) {
                    mTarget = (NewsComment) v.getTag();
                    showMenuDialog();
                }
                break;

            case R.id.av_avatar:
            case R.id.fl_user_name:
                if (v.getTag() != null && v.getTag() instanceof NewsComment) {
                    NewsComment comment = (NewsComment) v.getTag();
                    Intent ip = new Intent(this, ProfileActivity.class);
                    ip.putExtra(UserInfo.INTENT_EXTRA_INFO, comment.userInfo);
                    startActivity(ip);
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchComments(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchComments(false);
    }

    @Override
    public void onPreExecute(int type) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_COMMENT:
                getTitleBar().hideProgress();
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    NewsComment comment = NewsComment.fromJson(mr.getJsonObject());
                    if (comment != null) {
                        mAdapter.insert(comment, 0);
                        mPtrListView.getRefreshableView().setSelection(0);
                        mNewsManager.updateNews(type, mNews, (News) mr.value);
                        updateHeader();
                    }
                }
                break;

            case TaskType.NEWS_DELETE_CMT:
                getTitleBar().hideProgress();
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    News value = (News) mr.value;
                    NewsComment comment = (NewsComment) value.action;
                    mAdapter.remove(comment);
                    mNewsManager.updateNews(type, mNews, value);
                    updateHeader();
                }
                break;

            case TaskType.NEWS_FETCH_CMT:
                getTitleBar().hideProgress();
                mPtrListView.onRefreshComplete();
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    List<NewsComment> commentList = (List<NewsComment>) mr.value;
                    if (mRefresh) {
                        mAdapter.reset(commentList);
                    } else {
                        mAdapter.append(commentList);
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_REPLY && resultCode == RESULT_OK) {
            postComment(data == null ? null : data.getStringExtra(ReplyActivity.EXTRA_RESULT));
        }
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
            mMenuDialog.setTitle(R.string.please_choose);
        }
        if (mTarget.userInfo.isMine(this)) {
            mMenuDialog.setItems(R.array.news_comment_menu_mine, this);
        } else {
            mMenuDialog.setItems(R.array.news_comment_menu, this);
        }
        mMenuDialog.show();
    }

    private void showReplyActivity() {
        Intent intent = new Intent(this, ReplyActivity.class);
        if (mTarget == null) {
            intent.putExtra(ReplyActivity.EXTRA_TITLE_RES, R.string.rpl_title_comment);
        }
        startActivityForResult(intent, REQUEST_REPLY);
    }

    private void updateHeader() {
        mTvHeader.setText(Utils.getColoredText(
                getString(R.string.news_commented_count, mNews.commentedCount),
                String.valueOf(mNews.commentedCount), mCmtCountColor));
    }

    private void fetchComments(boolean refresh) {
        mRefresh = refresh;
        int offset = refresh ? 0 : mAdapter.getCount();
        mNewsManager.startNewsFetchCommentTask(mNews, offset);
    }

    private void postComment(String comment) {
        if (!TextUtils.isEmpty(comment)) {
            mNews.comment = comment;
            getTitleBar().showProgress();
            mNewsManager.startNewsCommentTask(mNews, mTarget);
        }
    }

    private class NewsCommentAdapter extends ArrayAdapter<NewsComment> {
        private UserRemarkManager mRemarkManager;
        private final int mNameColor;

        public NewsCommentAdapter() {
            super(getApplicationContext(), 0);
            mRemarkManager = UserRemarkManager.getInstance(getApplicationContext());
            mNameColor = getResources().getColor(R.color.news_cmt_reply);
        }

        public void append(List<NewsComment> commentList) {
            super.addAll(commentList);
        }

        public void reset(List<NewsComment> commentList) {
            setNotifyOnChange(false);
            clear();
            append(commentList);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_cf_reply, parent, false);
            }
            NewsComment comment = getItem(position);

            ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
            ivAvatar.setOnClickListener(NewsCommentActivity.this);
            ivAvatar.setTag(comment);
            ivAvatar.setImage(comment.userInfo.getAvatar(), comment.userInfo.defaultAvatar());

            View flName = view.findViewById(R.id.fl_user_name);
            flName.setOnClickListener(NewsCommentActivity.this);
            flName.setTag(comment);
            NameView tvName = (NameView) view.findViewById(R.id.tv_user_name);
            tvName.setText(comment.userInfo.getDisplayName(getContext()));
            tvName.setMedal(comment.userInfo.primaryBadgeImage);

            Utils.setText(view, R.id.tv_extra_info, getString(
                    R.string.news_published_on, Utils.getTimeDesc(comment.time)));

            View vTopRight = view.findViewById(R.id.top_right_item);
            vTopRight.setTag(comment);
            vTopRight.setOnClickListener(NewsCommentActivity.this);

            CharSequence content = comment.content;
            if (comment.targetId > 0) {
                String name = mRemarkManager.getRemark(comment.targetUid, comment.targetName);
                content = Utils.getColoredText(getString(
                        R.string.news_comment_content, name, content),
                        name, mNameColor, false);
            }
            Utils.setText(view, R.id.tv_desc, Utils.getRefFriendText(content, getContext()));

            view.findViewById(R.id.tv_like).setVisibility(View.GONE);

            return view;
        }
    }
}
