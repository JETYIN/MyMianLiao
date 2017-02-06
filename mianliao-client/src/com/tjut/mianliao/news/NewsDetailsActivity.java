package com.tjut.mianliao.news;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AttachmentView;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.EmotionPicker.EmotionListener;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.MlEditText;
import com.tjut.mianliao.component.MlWebView;
import com.tjut.mianliao.component.MlWebView.WebViewListener;
import com.tjut.mianliao.component.NameView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.news.TicketView;
import com.tjut.mianliao.contact.UserRemarkManager;
import com.tjut.mianliao.data.Attachment;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.NewsComment;
import com.tjut.mianliao.data.NewsSource;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;

public class NewsDetailsActivity extends BaseActivity implements OnClickListener, TaskExecutionListener,
        WebViewListener, DialogInterface.OnClickListener, OnRefreshListener2<ListView>, EmotionListener,
        OnFocusChangeListener, OnTouchListener {

    public static final String EXTRA_VIEW_SOURCE = "extra_view_source";
    public static final String EXTRA_NEWS_ID = "extra_news_id";
    public static final int REQUEST_REPLY = 100;

    private TextView mTvCommentCount;
    private TextView mTvLikeCount;
    private MlWebView mWvContent;
    private PullToRefreshListView mPtrListView;
    private TicketView mTvTicket;

    private MlEditText mMessageEditor;
    private EmotionPicker mEmotionPicker;
    private RelativeLayout mRlCommentBar;

    private TextView mTvTitle;
    private TextView mTvNewSource;
    private TextView mTvNewTime;
    private ProImageView mNewsImage;
    private View mLlComentFlag;

    private NewsComment mTarget;
    private NewsCommentAdapter mAdapter;
    private NewsManager mNewsManager;
    private News mNews;
    private AttachmentView mAttView;

    private SnsHelper mSnsShareHelper;
    private boolean mRefresh, mIsReplyPost;

    private LightDialog mMenuDialog;
    private ImageView mIvFav;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_news_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNews = getIntent().getParcelableExtra(News.INTENT_EXTRA_NAME);

        if (mNews == null) {
            int newsId = getIntent().getIntExtra(EXTRA_NEWS_ID, 0);
            new FindNewsTask(newsId).executeLong();
        } else {
            refreshMainView();
        }
    }

    private void refreshMainView() {
        if (mNews == null) {
            toast(R.string.news_tst_not_found);
            finish();
            return;
        }
        getTitleBar().showTitleText(R.string.news_details_title, null);
        getTitleBar().showRightText(R.string.news_sources_title, this);

        mSnsShareHelper = SnsHelper.getInstance();
        mNewsManager = NewsManager.getInstance(this);
        mNewsManager.registerTaskListener(this);

        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptr_coment);
        ListView listView = mPtrListView.getRefreshableView();
        View header = mInflater.inflate(R.layout.news_details_header, listView, false);
        listView.addHeaderView(header);
        mWvContent = (MlWebView) header.findViewById(R.id.wv_content);
        mWvContent.setWebViewListener(this);
        mTvTicket = (TicketView) header.findViewById(R.id.tv_ticket);

        mTvTitle = (TextView) header.findViewById(R.id.tv_news_title);
        mTvNewSource = (TextView) header.findViewById(R.id.news_source);
        mTvNewTime = (TextView) header.findViewById(R.id.news_time);
        mNewsImage = (ProImageView) header.findViewById(R.id.iv_image);
        mLlComentFlag = header.findViewById(R.id.ll_coment_flag);
        mAttView = (AttachmentView) header.findViewById(R.id.av_att);
        mAttView.setOnClickListener(this);
        mTvTitle.setText(mNews.title);
        mTvNewSource.setText(mNews.sourceName);
        mTvNewTime.setText(Utils.getTimeDesc(mNews.createTime));
        
        if (!TextUtils.isEmpty(mNews.cover)) {
            mNewsImage.setImage(mNews.getPreviewImage(), R.drawable.bg_img_loading);
        } else {
            mNewsImage.setImageResource(R.drawable.pic_news_default);
        }

        mTvCommentCount = (TextView) findViewById(R.id.tv_comment_count);
        mTvLikeCount = (TextView) findViewById(R.id.tv_like_count);
        mIvFav = (ImageView) findViewById(R.id.iv_fav);
        mMessageEditor = (MlEditText) findViewById(R.id.et_message);
        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mRlCommentBar = (RelativeLayout) findViewById(R.id.rl_comment_bar);
        mEmotionPicker.setEmotionListener(this);
        mMessageEditor.setOnFocusChangeListener(this);

        mAdapter = new NewsCommentAdapter();
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setOnTouchListener(this);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
        updateFav();
    }

    @Override
    protected void onDestroy() {
//        if (mSnsShareHelper != null) {
//            mSnsShareHelper.closeShareBoard();
//        }
        if (mTvTicket != null) {
            mTvTicket.destroy();
        }
        if (mNewsManager != null) {
            mNewsManager.unregisterTaskListener(this);
        }
        mPtrListView = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsShareHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mWvContent != null && mWvContent.canGoBack()) {
            mWvContent.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case 0: // Reply
        		replyComment();
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

    private void replyComment() {
        mRlCommentBar.setVisibility(View.VISIBLE);
        mMessageEditor.setHint("回复:" + mTarget.userInfo.getDisplayName(NewsDetailsActivity.this) + ":");
        mMessageEditor.requestFocus();
        Utils.showInput(mMessageEditor);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_reply:
                mTarget = (NewsComment) (v.getTag());
                showMenuDialog();
                break;
            case R.id.iv_share:
                share();
                break;
            case R.id.iv_fav:
                mNewsManager.startNewsFavoriteTask(mNews);
                break;

            case R.id.tv_right:
                viewSource();
                break;

            case R.id.ll_like: 
                mNewsManager.startNewsLikeTask(mNews);
                break;

            case R.id.ll_comment:
        		if (mRlCommentBar.getVisibility() == View.VISIBLE) {
        			mRlCommentBar.setVisibility(View.GONE);
        		} else {
        			mRlCommentBar.setVisibility(View.VISIBLE);
        		}
        		mPtrListView.getRefreshableView().requestFocusFromTouch();
        		mPtrListView.getRefreshableView().setSelection(
        				mPtrListView.getRefreshableView().getHeaderViewsCount());
                break;

            case R.id.iv_extention:
                Utils.toggleInput(mMessageEditor, mEmotionPicker);
                break;

            case R.id.et_message:
                mEmotionPicker.setVisible(false);
                break;

            case R.id.tv_send:
                String message = mMessageEditor.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    toast(R.string.rpl_tst_content_empty);
                } else if (!mIsReplyPost) {
                    postComment(message);
                    mMessageEditor.setText("");
                    mMessageEditor.setHint(R.string.post_reply_hit);
                    Utils.hideInput(mMessageEditor);
                    mIsReplyPost = true;
                } else {
                    toast(R.string.handling_last_task);
                }
                break;

            case R.id.av_avatar:
            case R.id.tv_name:
                showProfileActivity(((NewsComment) (v.getTag())).userInfo);
                break;

            case R.id.av_att:
                Attachment att = mNews.attachment;
                Utils.downloadFile(this, att.url, att.name);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(String url) {
        return false;
    }

    @Override
    public void onPageStarted(String url) {
    }

    @Override
    public void onPageFinished(String url) {
        mTvTicket.setVisibility(View.VISIBLE);
        mTvTicket.show(mNews);
        mAttView.setVisibility(View.VISIBLE);
        mAttView.show(mNews.attachment);
        updateLike();
        updateComment();
        mLlComentFlag.setVisibility(View.VISIBLE);
        fetchComments(true);
    }

    @Override
    public void onPreExecute(int type) {
        switch (type) {
            case TaskType.NEWS_LIKE:
                getTitleBar().showProgress();
                break;
            case TaskType.NEWS_FAVORITE:
                mIvFav.setEnabled(false);
                getTitleBar().showProgress();
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        if (mPtrListView == null) {
            return;
        }
        switch (type) {
            case TaskType.NEWS_COMMENT:
                getTitleBar().hideProgress();
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    NewsComment comment = NewsComment.fromJson(mr.getJsonObject());
                    if (comment != null) {
                        mAdapter.insert(comment, 0);
                        mPtrListView.getRefreshableView().setSelection(
                                mPtrListView.getRefreshableView().getHeaderViewsCount());
                        mNewsManager.updateNews(type, mNews, (News) mr.value);
                        updateComment();
                    }
                }
                mIsReplyPost = false;
                mRlCommentBar.setVisibility(View.GONE);
                mTarget = null;
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
                    mTarget = null;
                }
                break;

            case TaskType.NEWS_LIKE:
                getTitleBar().hideProgress();
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    mNewsManager.updateNews(type, mNews, (News) mr.value);
                    updateLike();
                    mTarget = null;
                }
                break;
            case TaskType.NEWS_DELETE_CMT:
                getTitleBar().hideProgress();
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    News value = (News) mr.value;
                    NewsComment comment = (NewsComment) value.action;
                    mAdapter.remove(comment);
                    mNewsManager.updateNews(type, mNews, value);
                    updateComment();
                    mTarget = null;
                }
                break;
            case TaskType.NEWS_FAVORITE:
                mIvFav.setEnabled(true);
                getTitleBar().hideProgress();
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    mNewsManager.updateNews(type, mNews, (News) mr.value);
                    updateFav();
                    mTarget = null;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        mWvContent.loadUrl(Utils.getServerAddress() + mNews.contentUrl.substring(1));
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchComments(false);
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

    private void share() {
        mSnsShareHelper.openShareBoard(this, mNews);
    }

    private void showProfileActivity(UserInfo userInfo) {
        Intent iProfile = new Intent(this, NewProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        startActivity(iProfile);
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

    private void updateLike() {
        if (mNews.likedCount > 0){
            mTvLikeCount.setText(String.valueOf(mNews.likedCount));
        }
        mTvLikeCount.setCompoundDrawablesWithIntrinsicBounds(mNews.liked ?
                R.drawable.buttom_like_hover : R.drawable.buttom_like, 0, 0, 0);
    }

    private void updateFav() {
        mIvFav.setImageResource(mNews.favorite ? R.drawable.bottom_collect_full : R.drawable.bottom_collect_empty);
        if (mNews.likedCount > 0){
            mTvLikeCount.setText(String.valueOf(mNews.likedCount));
        }
        mTvLikeCount.setCompoundDrawablesWithIntrinsicBounds(mNews.liked ? R.drawable.buttom_like_hover
                : R.drawable.buttom_like, 0, 0, 0);
    }


    private void updateComment() {
        mTvCommentCount.setText(String.valueOf(mNews.commentedCount));
    }

    private void viewSource() {
        Intent iSource = new Intent(this, NewsSourceDetailsActivity.class);
        iSource.putExtra(NewsSource.INTENT_EXTRA_NAME, NewsSource.fromNews(mNews));
        startActivityForResult(iSource, getIdentity());
    }

    private class NewsCommentAdapter extends ArrayAdapter<NewsComment> {
        private UserRemarkManager mRemarkManager;
        private final int mNameColor;

        public NewsCommentAdapter() {
            super(getApplicationContext(), 0);
            mRemarkManager = UserRemarkManager.getInstance(getApplicationContext());
            mNameColor = getResources().getColor(R.color.txt_lightgray);
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
                view = mInflater.inflate(R.layout.list_item_reply, parent, false);
            }
            view.setBackgroundResource(R.drawable.selector_btn_title);
            NewsComment comment = getItem(position);
            view.setOnClickListener(NewsDetailsActivity.this);
            view.setTag(comment);

            ProImageView ivAvatar = (ProImageView) view.findViewById(R.id.av_avatar);
            ivAvatar.setOnClickListener(NewsDetailsActivity.this);
            ivAvatar.setTag(comment);
            ivAvatar.setImage(comment.userInfo.getAvatar(), comment.userInfo.defaultAvatar());

            View flName = view.findViewById(R.id.tv_desc);
            flName.setTag(comment);
            NameView tvName = (NameView) view.findViewById(R.id.tv_name);
            tvName.setTag(comment);
            tvName.setOnClickListener(NewsDetailsActivity.this);
            tvName.setText(comment.userInfo.getDisplayName(getContext()));

            Utils.setText(view, R.id.tv_intro, getString(R.string.news_published_on, Utils.getTimeDesc(comment.time)));

            CharSequence content = comment.content;
            if (comment.targetId > 0) {
                String name = mRemarkManager.getRemark(comment.targetUid, comment.targetName);
                content = Utils.getColoredText(getString(R.string.news_comment_content, name, content), name,
                        mNameColor, false);
            }
            Utils.setText(view, R.id.tv_desc, Utils.getRefFriendText(content, getContext()));

            view.findViewById(R.id.tv_likes_count).setVisibility(View.GONE);

            return view;
        }
    }

    private class FindNewsTask extends MsTask {

        int mBroadCastId;

        public FindNewsTask(int broadcast_id) {
            super(NewsDetailsActivity.this, MsRequest.NEWS_BY_ID);
            this.mBroadCastId = broadcast_id;

        }

        @Override
        protected String buildParams() {
            return new StringBuffer("id=").append(mBroadCastId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);

            if (response.isSuccessful()) {
                mNews = News.fromJson(response.json.optJSONObject("response"));
                refreshMainView();
            }
        }

    }

    @Override
    public void onEmotionClicked(Emotion emotion) {
        mMessageEditor.getText().insert(mMessageEditor.getSelectionStart(), emotion.getSpannable(this));
    }

    @Override
    public void onBackspaceClicked() {
        Utils.dispatchDelEvent(mMessageEditor);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            Utils.showInput(mMessageEditor);
        } else {
            mEmotionPicker.setVisible(false);
            Utils.hideInput(mMessageEditor);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Utils.hideInput(mMessageEditor);
            mEmotionPicker.setVisible(false);
        }
        return false;
    }

    @Override
    public void onPageReceivedError() {
        mWvContent.loadUrl(Utils.getServerAddress() + mNews.contentUrl.substring(1));
    }
}
