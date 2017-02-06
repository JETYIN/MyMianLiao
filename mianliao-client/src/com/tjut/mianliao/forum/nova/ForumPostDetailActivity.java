package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.json.JSONArray;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RefFriendActivity;
import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.EmotionPicker.EmotionListener;
import com.tjut.mianliao.component.FlexibleImageView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.MLLinkMovementMethod;
import com.tjut.mianliao.component.MlWebView;
import com.tjut.mianliao.component.RichEmotionTextView;
import com.tjut.mianliao.component.RichMlEditText;
import com.tjut.mianliao.component.RichMlEditText.OnAtDelClicklistener;
import com.tjut.mianliao.component.forum.ReplyView;
import com.tjut.mianliao.component.nova.DanmakuLayout;
import com.tjut.mianliao.component.nova.MlVideoView;
import com.tjut.mianliao.component.nova.VoiceView;
import com.tjut.mianliao.component.nova.VoteView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfRecord;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.forum.nova.ReplyViewManager.ReplyViewListener;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.tribe.TribeDetailActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.NewsGuidDialog;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.video.JCVideoPlayer;
import com.umeng.analytics.MobclickAgent;

public class ForumPostDetailActivity extends BaseActivity implements OnRefreshListener2<ListView>,
        OnClickListener, MsTaskListener, DialogInterface.OnClickListener, ReplyViewListener,
        OnFocusChangeListener, EmotionListener, OnTouchListener, OnAtDelClicklistener {

    public static final String SP_IS_FIRST = "sp_forum_post_detail";

    public static final String EXTRL_POST_DATA = "extrl_post_data";
    public static final String EXTRL_CHANNEL_INFO = "extrl_channel_info";
    public static final String EXTRL_POST_DATA_ID = "extrl_post_data_id";
    public static final String EXTRL_SHOW_OTHER_SCHOOL_TAG = "extrl_show_other_school_tag";
    public static final String EXTRL_SHOW_ISSHOW_CHANNELNAME = "extrl_show_isshow_channelname";
    public static final String EXTRL_SHOW_ISSHOW_SCHOOL = "extrl_show_isshow_school";
    public static final String EXTRL_ISSHOW_SCHOOL_TOPIC = "extrl_isshow_school_topic";
    public static final String EXTRL_ISSHOW_SCHOOL_NAME = "extrl_isshow_school_name";
    public static final String EXTRL_IS_FORM_TRIBE = "extrl_is_form_tribe";
    public static final String EXTRL_IS_SHOW_TRIBE = "extrl_is_show_tribe";
    public static final String EXTRL_TOPIC_NAME = "extrl_topic_name";

    private static final String TAG = "ForumPostDetailActivity";

    private static final int MAX_REPLY_LEN = Integer.MAX_VALUE;

    private PullToRefreshListView mPtrListView;
    private PostCommentsAdapter mAdapter;

    private CfPost mCfPost;
    private UserInfo mUserInfo;

    private ImageView mAvatarView;
    private ImageView mImageGender;
    private ImageView mIvMedal;

    private ArrayList<ArrayList<CfReply>> mReplys = new ArrayList<>();
    private ArrayList<CfReply> mDanmuReplys = new ArrayList<CfReply>();
    private CfReply mTargetReply, mParentReply;

    private MsTaskManager mTaskManager;
    private SnsHelper mSnsHelper;

    private TextView mTvLikeNormal;
    private TextView mTvHateNormal;
    private TextView mTvCommentN;
    private RichMlEditText mMessageEditor;
    private EmotionPicker mEmotionPicker;
    private LightDialog mMenuDialog, mReplyMenuDialog, mPostNoticeDialog, mRepNoticeDialog;
    private TextView mTvCommentCount;
    private ImageView mIvNoComment, mIvCommentSuc;
    public static View mViewBg;

    private ReplyViewManager mViewManager;
    private boolean mIsReplyPost = true;
    private LinearLayout mLlInput;

    private DanmakuLayout mDanmakuLayout;
    private boolean mIsDanmakuShown = true;
    private HashMap<CfReply, View> mDanmakuViews;
    private int mCommentCount;
    private SharedPreferences mPreferences;
    private NewsGuidDialog mGuidDialog;
    private ImageView mIvEmotion;
    private boolean isRefreshing;
    private int mMaxFloor = 0;
    private DanmuThread mDanmukuThread = null;
    private boolean mAutoCalcFloor = true; // 是否自动计算楼层
    private ProgressBar mProgressBar;
    private TextView mTvRefreshing;
    private MlWebView mWbView;
    private VoiceView mVoiceView;

    private MsTask mCurrentTask;
    private boolean mHasCallback = true;
    private ArrayList<UserInfo> mRefFriends = new ArrayList<UserInfo>();

    private boolean mHasGotReplies;
    private boolean mIsShowTribe;
    private String mTopicName;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            refreshMainView();
        }

        ;
    };

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_channel_post_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCfPost = (CfPost) getIntent().getParcelableExtra(EXTRL_POST_DATA);
        mIsShowTribe = getIntent().getBooleanExtra(EXTRL_IS_SHOW_TRIBE, true);
        mPreferences = DataHelper.getSpForData(this);
        mUserInfo = AccountInfo.getInstance(this).getUserInfo();
        mLlInput = (LinearLayout) findViewById(R.id.ll_input);
        if (mCfPost != null) {
            mHandler.sendMessage(Message.obtain());
        } else {
            mLlInput.setVisibility(View.GONE);
            int postId = getIntent().getIntExtra(EXTRL_POST_DATA_ID, 0);
            mTopicName = getIntent().getStringExtra(EXTRL_TOPIC_NAME);
            NotificationHelper.getInstance(this).clearNotification(NotificationType.POST_RECOMMEND);
            new ThreadInfoTask(postId).executeLong();
        }
    }

    private int[] getGuidImageRes() {
        int[] imgRes = {};
        return imgRes;
    }

    private void refreshMainView() {
        mDanmakuViews = new HashMap<CfReply, View>();
        mDanmakuLayout = (DanmakuLayout) findViewById(R.id.danmaku_layout);
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_comments);

        mMessageEditor = (RichMlEditText) findViewById(R.id.et_message);
        mMessageEditor.addTextChangedListener(mTextWatcher);
        mMessageEditor.setShouldMatcherAt();
        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mIvEmotion = (ImageView) findViewById(R.id.iv_extention);

        mEmotionPicker.setEmotionListener(ForumPostDetailActivity.this);
        mMessageEditor.setOnFocusChangeListener(ForumPostDetailActivity.this);

        mTaskManager = MsTaskManager.getInstance(ForumPostDetailActivity.this);
        mSnsHelper = SnsHelper.getInstance();
        mTaskManager.registerListener(ForumPostDetailActivity.this);
        mViewManager = ReplyViewManager.getInstance(ForumPostDetailActivity.this);
        mViewManager.registerReplyViewListener(ForumPostDetailActivity.this);

        ListView listView = mPtrListView.getRefreshableView();
        listView.setEnabled(false);
        View headerView = mInflater.inflate(R.layout.channel_post_detail_header, listView, false);
        listView.addHeaderView(headerView);
        mPtrListView.getRefreshableView().setOnTouchListener(ForumPostDetailActivity.this);
        headerView.setOnClickListener(ForumPostDetailActivity.this);
        mProgressBar = (ProgressBar) headerView.findViewById(R.id.pb_loading);
        mTvRefreshing = (TextView) headerView.findViewById(R.id.tv_refreshing);

        mWbView = (MlWebView) headerView.findViewById(R.id.wv_content);
        mCommentCount = mCfPost.replyCount;
        mIvNoComment = (ImageView) headerView.findViewById(R.id.iv_eleph);
        mIvCommentSuc = (ImageView) findViewById(R.id.iv_comment_suc);
        mTvCommentCount = (TextView) headerView.findViewById(R.id.tv_comment_count);
        mTvCommentCount.setText(getString(R.string.channel_comments_count, mCommentCount));
        mViewBg = findViewById(R.id.view_show_bg);

        updateView(headerView, mCfPost);
        setViewData(headerView, mCfPost);
        mAvatarView = (ImageView) headerView.findViewById(R.id.av_avatar);
        mImageGender = (ImageView) headerView.findViewById(R.id.iv_gender);
        mIvMedal = (ImageView) headerView.findViewById(R.id.iv_medal);
        TextView tvLoc = (TextView) headerView.findViewById(R.id.tv_location);
        headerView.findViewById(R.id.iv_vip_bg).setVisibility(
                mCfPost.userInfo.vip ? View.VISIBLE : View.GONE);
        String displayName = mCfPost.userInfo.getDisplayName(ForumPostDetailActivity.this);
        TextView tvName = (TextView) headerView.findViewById(R.id.tv_name);
        tvName.setText(displayName);
        tvName.setTextColor(getNameColor(mCfPost));
        Utils.setText(headerView, R.id.tv_intro, Utils.getPostShowTimeString(mCfPost.createdOn));
        tvLoc.setText(mCfPost.userInfo.school);
        tvLoc.setVisibility(View.VISIBLE);
        mImageGender.setImageResource(mCfPost.userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy);
        if (mCfPost.userInfo.getLatestBadge() != null
                && mCfPost.userInfo.getLatestBadge().startsWith("http")
                && !mCfPost.isUserAreSuperModerator()) {
            mIvMedal.setVisibility(View.VISIBLE);
            Picasso.with(this)
                    .load(mCfPost.userInfo.getLatestBadge())
                    .placeholder(R.drawable.ic_medal_empty)
                    .into(mIvMedal);
        } else {
            mIvMedal.setVisibility(View.GONE);
        }
        Picasso.with(this)
                .load(mCfPost.userInfo.getAvatar())
                .placeholder(mCfPost.userInfo.defaultAvatar())
                .into(mAvatarView);
        mAvatarView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showProfileActivity(mCfPost.userInfo);
            }
        });

        if (mTopicName == null || "".equals(mTopicName)) {
            getTitleBar().setTitle(getString(R.string.course_entry));
        } else {
            getTitleBar().setTitle("#" + mTopicName + "#");
        }
        getTitleBar().showRightButton(R.drawable.selector_btn_checkbox, ForumPostDetailActivity.this);

        mAdapter = new PostCommentsAdapter();
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setOnRefreshListener(ForumPostDetailActivity.this);

        toggleDanmaku();
        showImageEle();
        fetchComments(true);
    }

    private int getNameColor(CfPost post) {
        if (post.isUserAreSuperModerator()) {
            return Constant.COLOR_SUPER_MODERATOR;
        } else if (post.isUserAreModerator()) {
            return Constant.COLOR_MODERATOR;
        }
        return Constant.COLOR_NORMAL;
    }

    private void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mTvRefreshing.setVisibility(View.VISIBLE);
        mTvCommentCount.setVisibility(View.GONE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
        mTvRefreshing.setVisibility(View.GONE);
        mTvCommentCount.setVisibility(View.VISIBLE);
    }

    private void showImageEle() {
        if (mCfPost.replyCount > 0 || mHasGotReplies) {
            if (mIsDanmakuShown) {
                mIvNoComment.setVisibility(View.VISIBLE);
                mIvNoComment.setImageResource(R.drawable.img_nocontent);
            } else {
                mIvNoComment.setVisibility(View.GONE);
            }
        } else {
            mIvNoComment.setVisibility(View.VISIBLE);
            mIvNoComment.setImageResource(R.drawable.img_nocontent_elephent);
        }
    }

    private void showCommmentSucc() {
        showCommentImage();
        mIvCommentSuc.setVisibility(View.VISIBLE);
        mIvCommentSuc.postDelayed(new Runnable() {

            @Override
            public void run() {
                mIvCommentSuc.setVisibility(View.GONE);
            }
        }, 2000);
    }

    private void showCommentImage() {
        if (!mIsDanmakuShown) {
            mIvNoComment.setVisibility(mCfPost.replyCount > 0 ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isFirst = mPreferences.getBoolean(SP_IS_FIRST, false);
        if (isFirst) {
            mGuidDialog = new NewsGuidDialog(this, R.style.Translucent_NoTitle);
            mGuidDialog.showGuidImage(getGuidImageRes(), SP_IS_FIRST);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuLayout != null && mIsDanmakuShown && mAdapter.getCount() > 0) {
            mDanmakuLayout.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVoiceView != null) {
            mVoiceView.onDestroy();
        }
        mPtrListView = null;
        if (mDanmakuLayout != null) {
            mDanmakuLayout.clear();
        }
        if (mTaskManager != null) {
            mTaskManager.unregisterListener(this);
        }
        if (mViewManager != null) {
            mViewManager.unregisterReplyViewListener(this);
        }
        JCVideoPlayer.releaseAllVideos();
    }

    public class DanmuThread extends Thread {

        private boolean isAlive = true;

        public void setAlive(boolean flag) {
            isAlive = flag;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (!isAlive) {
                        mDanmuReplys.clear();
                        Thread.currentThread().interrupt();
                        return;
                    }
                    final ArrayList<View> views = new ArrayList<View>();
                    while (mDanmuReplys.size() > 0) {
                        CfReply reply = mDanmuReplys.remove(0);
                        if (reply == null) {
                            return;
                        }
                        View view = getDanmakuView(reply);
                        views.add(view);
                    }
                    for (View view : views) {
                        mDanmakuLayout.addDanmaku(view);
                    }
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mIsDanmakuShown) {
                                mDanmakuLayout.start();
                            }
                        }
                    });
                    Thread.sleep(5000);// 线程暂停10秒，单位毫秒
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PostCommentsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mIsDanmakuShown ? 0 : mReplys.size();
        }

        @Override
        public ArrayList<CfReply> getItem(int position) {
            return mReplys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_channel_post_comments, parent, false);
            }

            if (view == null) {
                return null;
            }

            ArrayList<CfReply> replies = getItem(position);
            CfReply reply = replies.get(0);
            if (reply.floor > mMaxFloor) {
                mMaxFloor = reply.floor;
            }
            @SuppressWarnings("unchecked")
            ArrayList<CfReply> subReplies = (ArrayList<CfReply>) replies.clone();
            subReplies.remove(0);

            view.setOnClickListener(ForumPostDetailActivity.this);
            view.setTag(reply);

            AvatarView avatar = (AvatarView) view.findViewById(R.id.av_avatar);
            TextView tvName = (TextView) view.findViewById(R.id.tv_name);
            ImageView ivGender = (ImageView) view.findViewById(R.id.iv_gender);
            ImageView ivMedal = (ImageView) view.findViewById(R.id.iv_medal);
            RichEmotionTextView mTvContent = (RichEmotionTextView) view.findViewById(R.id.tv_desc);
            TextView tvLikedCount = (TextView) view.findViewById(R.id.tv_liked_count);
            ReplyView replyView = (ReplyView) view.findViewById(R.id.rv_comments);
            TextView tvLocation = (TextView) view.findViewById(R.id.tv_location);
            tvLocation.setVisibility(View.VISIBLE);
            ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
            ImageView ivBuilderIcon = (ImageView) view.findViewById(R.id.iv_master_build);
            View viewVip = view.findViewById(R.id.iv_vip);
            View viewVipBg = view.findViewById(R.id.iv_vip_bg);
            TextView tvFloor = (TextView) view.findViewById(R.id.tv_floor);
            tvName.setTextColor(getNameColor(reply));

            avatar.setOnClickListener(ForumPostDetailActivity.this);
            tvName.setOnClickListener(ForumPostDetailActivity.this);
            mTvContent.setOnClickListener(ForumPostDetailActivity.this);
            mTvContent.setText(reply.content);
            tvFloor.setTextColor(0xff888888);
            tvFloor.setBackgroundColor(0x4ddfe7ff);
            mTvContent.setTag(reply);
            // update vip view
            viewVipBg.setVisibility(View.GONE);

            tvLocation.setText(reply.userInfo.school);
            avatar.setImage(reply.userInfo.getAvatar(), reply.userInfo.defaultAvatar());
            tvName.setText(reply.userInfo.getDisplayName(ForumPostDetailActivity.this));
            ivGender.setImageResource(reply.userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy);
            if (reply.userInfo.getLatestBadge() != null
                    && reply.userInfo.getLatestBadge().startsWith("http")
                    && !mCfPost.isUserAreSuperModerator(reply.userInfo.userId)) {
                ivMedal.setVisibility(View.VISIBLE);
                Picasso.with(ForumPostDetailActivity.this)
                        .load(reply.userInfo.getLatestBadge())
                        .placeholder(R.drawable.ic_medal_empty)
                        .into(ivMedal);
            } else {
                ivMedal.setVisibility(View.GONE);
            }
            Utils.setText(view, R.id.tv_intro, Utils.getPostShowTimeString(reply.createdOn));

            tvLikedCount.setVisibility(View.VISIBLE);
            tvFloor.setVisibility(View.VISIBLE);
            tvFloor.setText(getString(R.string.channel_comment_floor, String.valueOf(reply.floor)));
            view.findViewById(R.id.iv_more).setVisibility(View.GONE);

            tvLikedCount.setCompoundDrawablesWithIntrinsicBounds(reply.myUp ? R.drawable.buttom_like_hover
                    : R.drawable.buttom_like, 0, 0, 0);
            tvLikedCount.setText(String.valueOf(reply.upCount));
            tvLikedCount.setTag(reply);

            // this is identify icon, and it is show while in day time, or it should hide;
            int resIcon = reply.userInfo.getTypeIcon();
            if (resIcon > 0) {
                ivTypeIcon.setImageResource(reply.userInfo.getTypeIcon());
                ivTypeIcon.setVisibility(View.VISIBLE);
            } else {
                ivTypeIcon.setVisibility(View.GONE);
            }
            // this is builder icon
            if (getModeratorIcon(reply) > 0) {
                ivBuilderIcon.setImageResource(getModeratorIcon(reply));
                ivBuilderIcon.setVisibility(View.VISIBLE);
            } else {
                ivBuilderIcon.setVisibility(View.GONE);
            }

            if (subReplies.size() > 0) {
                replyView.setMaxCount(2);
                replyView.setReplies(reply, subReplies);
            } else {
                replyView.removeAllViews();
            }
            avatar.setTag(reply);
            tvName.setTag(reply);
            return view;
        }

    }

    private class GetPostCommentsTask extends MsTask {

        private int mOffset;

        public GetPostCommentsTask(int offset) {
            super(ForumPostDetailActivity.this, MsRequest.LIST_COMMENT_WITH_REPLY_BY_THREAD_V2);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("thread_id=").append(mCfPost.postId)
                    .append("&offset=").append(mOffset)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            if (mCurrentTask == null) {
                showProgress();
            }
            mCurrentTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (mPtrListView != null) {
                if (mProgressBar.getVisibility() != View.GONE) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                        }
                    });
                }
                if (isRefreshing) {
                    mPtrListView.onRefreshComplete();
                    isRefreshing = false;
                }
                if (response.isSuccessful()) {
                    mCfPost.replyCount = response.getJsonObject().optInt("comment_count");
                    JSONArray commentsArray = response.getJsonObject().optJSONArray("comment_list");
                    ArrayList<ArrayList<CfReply>> arrayReplies = JsonUtil.getArrays(commentsArray,
                            CfReply.TRANSFORMER);
                    if (mOffset == 0) {
                        if (arrayReplies.size() > 0) {
                            mMaxFloor = arrayReplies.get(0).get(0).floor;
                        }
                        mReplys.clear();
                        mDanmakuLayout.clear();
                    }
                    if (!arrayReplies.isEmpty()) {
                        mHasGotReplies = true;
                        mReplys.addAll(arrayReplies);
                        for (ArrayList<CfReply> replies : arrayReplies) {
                            for (CfReply reply : replies) {
                                reply.parentReply = replies.get(0);
                                reply.targetPost = mCfPost;
                                mDanmuReplys.add(reply);
                                if (mDanmukuThread == null) {
                                    mDanmukuThread = new DanmuThread();
                                    mDanmukuThread.start();
                                }
                            }
                        }
                        showImageEle();
                        updateCommentCountInfo();
                        if (mIsDanmakuShown) {
                            mDanmakuLayout.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    fetchComments(false);
                                }
                            }, 5000);
                        } else {
                            if (mAdapter != null) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                        mCommentCount = mCfPost.replyCount;
                        mTvCommentCount.setText(getString(R.string.channel_comments_count, mCommentCount));
                    } else {
                        mHasGotReplies = false;
                    }
                }
            }

        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        isRefreshing = true;
        fetchComments(true);
    }

    public int getModeratorIcon(CfPost post) {
        if (post.isUserAreSuperModerator()) {
            return R.drawable.super_master;
        } else if (post.isUserAreModerator()) {
            return R.drawable.icon_muster_section;
        }
        return 0;
    }

    private int getNameColor(CfReply reply) {
        if (mCfPost.isUserAreSuperModerator(reply.userInfo.userId)) {
            return Constant.COLOR_SUPER_MODERATOR;
        } else if (mCfPost.isUserAreModerator(reply.userInfo.userId)) {
            return Constant.COLOR_MODERATOR;
        }
        return Constant.COLOR_NORMAL;
    }

    public int getModeratorIcon(CfRecord reply) {
        if (mCfPost.isUserAreSuperModerator(reply.userInfo.userId)) {
            return R.drawable.super_master;
        } else if (mCfPost.isUserAreModerator(reply.userInfo.userId)) {
            return R.drawable.icon_muster_section;
        }
        return 0;
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        isRefreshing = true;
        fetchComments(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_more:
                showMenuDialog();
                break;
            case R.id.ll_header:
                mIsReplyPost = true;
                mMessageEditor.setHint(R.string.post_reply_hit);
                //Utils.showInput(mMessageEditor);
                break;

            case R.id.ll_channel_post_comment:
            case R.id.tv_desc:
                mTargetReply = (CfReply) v.getTag();
                mParentReply = mTargetReply;
                if (mParentReply.userInfo.isMine(this) ||
                        (mCfPost.isModerator(mUserInfo.userId)) &&
                                !mCfPost.isModerator(mTargetReply.userInfo.userId) ||
                        (mCfPost.isUserAreSuperModerator(mUserInfo.userId)) &&
                                !mCfPost.isUserAreSuperModerator(mTargetReply.userInfo.userId)) {
                    showReplyMenuDialog();
                } else {
                    mMessageEditor.setHint("回复 " + mTargetReply.userInfo.getDisplayName(this) + ":");
                    mMessageEditor.requestFocus();
                    Utils.showInput(mMessageEditor);
                    mIsReplyPost = false;
                }
                break;

            case R.id.iv_extention:
                checkIcon();
                Utils.toggleInput(mMessageEditor, mEmotionPicker);
                break;

            case R.id.tv_send:
                if (mMessageEditor.getText().toString().length() > MAX_REPLY_LEN) {
                    toast("输入内容过长请重新输入");
                } else {
                    String message = mMessageEditor.getText().toString().trim();
                    if (TextUtils.isEmpty(message)) {
                        toast(R.string.rpl_tst_content_empty);
                    } else {
                        startComment(message, mRefFriends);
                        mMessageEditor.setText("");
                        mMessageEditor.setHint(R.string.post_reply_hit);
                        Utils.hideInput(mMessageEditor);
                        mIsReplyPost = true;
                    }
                }
                break;

            case R.id.et_message:
                mEmotionPicker.setVisible(false);
                break;

            case R.id.av_avatar:
            case R.id.tv_name:
                MobclickAgent.onEvent(this, MStaticInterface.FACE);
                mTargetReply = (CfReply) v.getTag();
                showProfileActivity(mTargetReply.userInfo);
                break;
            case R.id.iv_avatar:
                UserInfo info = (UserInfo) v.getTag();
                showProfileActivity(info);
                break;
            case R.id.ll_like_active:
            case R.id.ll_like_normal:
            case R.id.ll_like_night:
                if (mCfPost.myUp) {
                    return;
                }
                if (mCfPost.myDown) {
                    mCfPost.downCount--;
                    mCfPost.myDown = false;
                }
                mCfPost.upCount++;
                mCfPost.myUp = true;
                mCfPost.addLikedUser(mUserInfo);
                updatePost(mCfPost);
                mTaskManager.startForumLikeTask(mCfPost);
                MobclickAgent.onEvent(this, MStaticInterface.LIKE);
                break;

            case R.id.ll_dislike_active:
            case R.id.ll_dislike_normal:
            case R.id.ll_dislike_night:
                if (mCfPost.myDown) {
                    return;
                }
                if (mCfPost.myUp) {
                    mCfPost.upCount--;
                    mCfPost.myUp = false;
                }
                mCfPost.downCount++;
                mCfPost.myDown = true;
                mCfPost.removeLikedUser(mUserInfo);
                updatePost(mCfPost);
                mTaskManager.startForumHateTask(mCfPost);
                MobclickAgent.onEvent(this, MStaticInterface.UNLIKE);
                break;

            case R.id.ll_share_active:
            case R.id.ll_share_normal:
            case R.id.ll_share_vote:
                share();
                MobclickAgent.onEvent(this, MStaticInterface.SHARE);
                break;

            case R.id.btn_right:
                toggleDanmaku();
                showImageEle();
                MobclickAgent.onEvent(this, MStaticInterface.BARRAGE_SWITCH);
                break;

            case R.id.danmaku_layout:
                mDanmakuLayout.resume();
                mMessageEditor.setHint(R.string.post_reply_hit);
                mIsReplyPost = true;
                Utils.hideInput(mMessageEditor);
                break;

            case R.id.danmaku_view:
                mDanmakuLayout.clickDanmaku(v);
                mTargetReply = (CfReply) v.getTag();
                mParentReply = mTargetReply.parentReply;
                mMessageEditor.setHint("回复 " + mTargetReply.userInfo.getDisplayName(this) + ":");
                mIsReplyPost = false;
                mMessageEditor.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showInput(mMessageEditor);
                    }
                }, 400);
                break;

            case R.id.tv_liked_count:
                CfReply reply = (CfReply) v.getTag();
                if (!reply.liking) {
                    mTaskManager.startForumLikeTask(reply);
                }
                break;
            case R.id.view_show_bg:
                danmuResume(false);
                mMessageEditor.setHint(R.string.post_reply_hit);
                break;
            case R.id.tv_tribe_name:
                CfPost post = (CfPost) v.getTag();
                if (post.tribeId > 0) {
                    Intent trIntent = new Intent(ForumPostDetailActivity.this, TribeDetailActivity.class);
                    trIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, post.tribeId);
                    startActivity(trIntent);
                } else if (post.forumName.equals(post.userInfo.school)) {
                    Intent intent = new Intent();
                    intent.setClass(ForumPostDetailActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ACTIVE_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_FRAGMENT_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_POST_TAB_INDEX, 0);
                    startActivity(intent);
                } else if (post.tribeId <= 0 && post.schoolId <= 0) {
                    Toast.makeText(ForumPostDetailActivity.this, R.string.tribe_not_exist, Toast.LENGTH_SHORT).show();
                } else {
                    if (DataHelper.getSchoolisUnlock(ForumPostDetailActivity.this, post.forumName)) {
                        Intent intent = new Intent();
                        intent.setClass(ForumPostDetailActivity.this, FormOtherSchoolActivity.class);
                        intent.putExtra(Forum.INTENT_EXTRA_SCHOOLID, post.schoolId);
                        intent.putExtra(Forum.INTENT_EXTRA_SCHOOLNAME, post.forumName);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ForumPostDetailActivity.this, R.string.cf_school_is_lock, Toast.LENGTH_SHORT).show();
                    }
                }
            default:
                break;
        }
    }

    private void checkIcon() {
        if (mEmotionPicker.isShown()) {
            mIvEmotion.setImageResource(R.drawable.button_ic_key);
        } else {
            mIvEmotion.setImageResource(R.drawable.button_emotion);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        if (mDanmakuLayout == null) {
            //super.onBackPressed();
            finish();
        } else {
            if (mDanmakuLayout.isPaused()) {
                danmuResume(false);
            } else {
                if (mDanmukuThread != null) {
                    mDanmukuThread.setAlive(false);
                    mDanmukuThread = null;
                    System.out.println("----> onBack " + System.currentTimeMillis());
                }
                //super.onBackPressed();
                finish();
            }
        }

    }

    private void danmuResume(boolean isEnd) {
        mDanmakuLayout.resume();
        Utils.hideInput(mMessageEditor);
        mViewBg.setVisibility(View.GONE);
        if (isEnd) {
            mDanmakuLayout.postDelayed(new Runnable() {

                @Override
                public void run() {
                    mDanmakuLayout.end();
                }
            }, 200);
        }
    }

    @Override
    public void onClickReplyView(CfReply parentReply, CfReply targetReply) {
        mTargetReply = targetReply;
        mParentReply = parentReply;
        if (mTargetReply.userInfo.isMine(this) ||
                (mCfPost.isModerator(mUserInfo.userId)) &&
                        !mCfPost.isModerator(mTargetReply.userInfo.userId) ||
                (mCfPost.isUserAreSuperModerator(mUserInfo.userId)) &&
                        !mCfPost.isUserAreSuperModerator(mTargetReply.userInfo.userId)) {
            showReplyMenuDialog();
        } else {
            mMessageEditor.setHint("回复 " + mTargetReply.userInfo.getDisplayName(this) + ":");
            mMessageEditor.requestFocus();
            Utils.showInput(mMessageEditor);
            mIsReplyPost = false;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mReplyMenuDialog) {
            switch (which) {
                case 0:
                    mMessageEditor.setHint("回复 " + mTargetReply.userInfo.getDisplayName(this) + ":");
                    mMessageEditor.requestFocus();
                    Utils.showInput(mMessageEditor);
                    mIsReplyPost = false;
                    break;
                case 1:
                    showPepNoticeDialog();
                    break;
                case 2:
                    report(null, mTargetReply);
                    MobclickAgent.onEvent(this, MStaticInterface.REPORT);
                    break;
                default:
                    mParentReply = null;
                    break;
            }
        } else if (dialog == mMenuDialog) {
            switch (which) {
                case 0:
                    share();
                    MobclickAgent.onEvent(this, MStaticInterface.SHARE);
                    break;
                case 1:
                    mTaskManager.startForumPostCollectTask(mCfPost);
                    break;
                case 2:
                    copyToClipboard(false);
                    MobclickAgent.onEvent(this, MStaticInterface.DUPLICATE_CONTENT);
                    break;
                case 3:
                    if (mCfPost.isUserAreSuperModerator() || Utils.isMianLiaoService(mCfPost.userInfo)) {
                        if (mCfPost.userInfo.isMine(this)) {
                            showPostNoticeDialog();
                        } else {
                            report();
                            MobclickAgent.onEvent(this, MStaticInterface.REPORT);
                        }
                    } else if (mCfPost.isUserAreModerator()) {
                        if (mCfPost.userInfo.isMine(this)
                                || mCfPost.isUserAreSuperModerator(mUserInfo.userId)
                                || Utils.isMianLiaoService(mUserInfo)) {
                            showPostNoticeDialog();
                        } else {
                            report();
                            MobclickAgent.onEvent(this, MStaticInterface.REPORT);
                        }
                    } else {
                        if (mCfPost.isModerator(mUserInfo.userId)
                                || mCfPost.userInfo.isMine(this)
                                || Utils.isMianLiaoService(mUserInfo)
                                || mCfPost.isUserAreSuperModerator(mUserInfo.userId)) {
                            showPostNoticeDialog();
                        } else {
                            report();
                            MobclickAgent.onEvent(this, MStaticInterface.REPORT);
                        }
                    }
                    break;
                case 4:
                    // 置顶
                    mTaskManager.startForumStickTaskV4(mCfPost);
                    break;
                default:
                    break;
            }
        }
    }

    private void report() {
        PoliceHelper.reportForumPost(this, mCfPost.postId);
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
    public void onPreExecute(MsTaskType type) {
        switch (type) {
            case FORUM_LIKE_POST:
            case FORUM_HATE_POST:
            case FORUM_LIKE_REPLY:
            case FORUM_COMMENT_POST:
            case FORUM_COMMENT_REPLY:
            case FORUM_DELETE_REPLY:
                getTitleBar().showProgress();
                break;

            default:
                break;
        }
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_LIKE_POST:
            case FORUM_HATE_POST:
                getTitleBar().hideProgress();
                if (response.isSuccessful() && (response.value instanceof CfPost)) {
                    updatePost((CfPost) response.value);
                }
                break;
            case FORUM_LIKE_REPLY:
                getTitleBar().hideProgress();
                if (response.value instanceof CfReply) {
                    CfReply reply = CfReply.fromJson(response.getJsonObject());
                    updateReplyInfo(reply, (CfRecord) response.value);
                }
                break;
            case FORUM_COMMENT_POST:
                getTitleBar().hideProgress();
                if (response.isSuccessful()) {
                    CfReply reply = CfReply.fromJson(response.getJsonObject());
                    reply.targetPost = (CfPost) response.value;
                    reply.floor = ++mMaxFloor;
                    updateReply(reply, (CfRecord) response.value);
                    showCommmentSucc();
                } else {
                    switch (response.code) {
                        case MsResponse.FAIL_HAS_BEEN_BANNED:
                            toast(R.string.no_speak_toast);
                            break;
                        default:
                            break;
                    }
                }
                break;
            case FORUM_COMMENT_REPLY:
                getTitleBar().hideProgress();
                if (response.isSuccessful()) {
                    CfReply reply = CfReply.fromJson(response.getJsonObject());
                    reply.floor = mMaxFloor + 1;
                    updateReply(reply, (CfRecord) response.value);
                    showCommmentSucc();
                } else {
                    switch (response.code) {
                        case MsResponse.FAIL_HAS_BEEN_BANNED:
                            toast(R.string.no_speak_toast);
                            break;
                        default:
                            break;
                    }
                }
                break;
            case FORUM_DELETE_REPLY:
            case FORUM_DELETE_POST_V4:
            case FORUM_DELETE_POST:
                getTitleBar().hideProgress();
                if (response.value instanceof CfReply) {
                    CfReply reply = (CfReply) response.value;
                    ArrayList<CfReply> reps = null;
                    mMaxFloor--;
                    int index = -1;
                    for (ArrayList<CfReply> replies : mReplys) {
                        if (replies.contains(reply)) {
                            int cIndex = replies.indexOf(reply);
                            if (cIndex == 0 && replies.size() >= 1) {
                                reps = replies;
                                index = mReplys.indexOf(replies);
                                mDanmakuLayout.removeDanmaku(mDanmakuViews.remove(replies));
                            } else {
                                replies.remove(reply);
                                index = mReplys.indexOf(replies);
                                mDanmakuLayout.removeDanmaku(mDanmakuViews.remove(reply));
                            }
                        }
                    }
                    if (reps != null) {
                        mReplys.remove(reps);
                        if (index > 0 && mAutoCalcFloor) {
                            for (int i = 0; i < index; i++) {
                                mReplys.get(i).get(0).floor--;
                            }
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    updateCommentCountInfo();
                    showCommentImage();
                }
                if (response.value instanceof CfPost) {
                    setResult(RESULT_OK);
                    this.finish();
                }
                break;
            case FORUM_COLLECT_POST:
                if (response.value instanceof CfPost) {
                    CfPost post = (CfPost) response.value;
                }
                break;
            default:
                break;
        }
        mRefFriends.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (requestCode == BasePostActivity.REQUEST_REF_SQUARE && resultCode == RESULT_OK) {
                String refs = data.getStringExtra(RefFriendActivity.EXTRA_RESULT);
                ArrayList<UserInfo> datas = data.getParcelableArrayListExtra(RefFriendActivity.EXTRA_USERINFOS);
                if (mRefFriends == null || mRefFriends.size() == 0) {
                    mRefFriends = datas;
                } else {
                    mRefFriends.addAll(datas);
                }
                int ss = mMessageEditor.getSelectionStart();
                Editable editable = mMessageEditor.getText();
                Editable s = editable.replace(ss - 1, editable.length(), refs);
                mMessageEditor.setText(s);
                mHasCallback = true;
            } else if (requestCode == BasePostActivity.REQUEST_REF_SQUARE && resultCode == RESULT_CANCELED) {
                int ss = mMessageEditor.getSelectionStart();
                Editable editable = mMessageEditor.getText();
                Editable s = editable.replace(ss - 1, editable.length(), "");
                mMessageEditor.setText(s);
                mHasCallback = true;
            } else {
                mSnsHelper.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void copyToClipboard(boolean isReply) {
        String mCopyContent;
        if (isReply) {
            mCopyContent = exchangeAt(mTargetReply.content);
            Utils.copyToClipboard(this, mCopyContent, mCopyContent);
        } else {
            mCopyContent = exchangeAt(mCfPost.content);
            Utils.copyToClipboard(this, mCopyContent, mCopyContent);
        }
        Toast.makeText(this, getString(R.string.clip_board_clipped), Toast.LENGTH_SHORT).show();
    }

    private void fetchComments(boolean refresh) {
        new GetPostCommentsTask(refresh ? 0 : mReplys.size()).executeLong();
    }

    private View getDanmakuView(CfReply reply) {
        View view = mInflater.inflate(R.layout.list_item_danmaku, mDanmakuLayout, false);
        mDanmakuViews.put(reply, view);
        view.setTag(reply);
        view.setOnClickListener(this);

        ((AvatarView) view.findViewById(R.id.dv_avatar)).setImage(reply.userInfo.getAvatar() == null ? null
                : reply.userInfo.getAvatar(), reply.userInfo.defaultAvatar());

        String name = reply.userInfo.getDisplayName(this);
        if (name.length() > 6) {
            name = name.substring(0, 6) + "...";
        }
        Utils.setText(view, R.id.dv_content, Utils.getColoredText(String.format("%s %s", name, reply.content), name,
                0XFF45D2DC, false));
        return view;
    }

    private void report(CfPost post, CfReply reply) {
        if (post != null) {
            PoliceHelper.reportForumPost(this, post.postId);
        }
        if (reply != null) {
            PoliceHelper.reportForumPost(this, reply.postId);
        }
    }

    private void setViewData(View view, CfPost post) {
        LinearLayout shareN = (LinearLayout) view.findViewById(R.id.ll_share_normal);
        LinearLayout likeN = (LinearLayout) view.findViewById(R.id.ll_like_normal);
        LinearLayout disLikeN = (LinearLayout) view.findViewById(R.id.ll_dislike_normal);
        LinearLayout commentN = (LinearLayout) view.findViewById(R.id.ll_comment_normal);

        mTvLikeNormal = (TextView) likeN.findViewById(R.id.tv_like_normal);
        mTvHateNormal = (TextView) disLikeN.findViewById(R.id.tv_dislike_normal);
        mTvCommentN = (TextView) commentN.findViewById(R.id.tv_comment_normal);

        mTvLikeNormal.setCompoundDrawablesWithIntrinsicBounds(post.myUp ? R.drawable.buttom_like_hover
                : R.drawable.buttom_like, 0, 0, 0);
        mTvHateNormal.setCompoundDrawablesWithIntrinsicBounds(post.myDown ? R.drawable.buttom_hate_hover
                : R.drawable.buttom_hate, 0, 0, 0);
        mTvCommentN.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_comment, 0, 0, 0);
        mTvLikeNormal.setText(post.upCount > 0 ? String.valueOf(post.upCount) : "么么哒");
        mTvHateNormal.setText(post.downCount > 0 ? String.valueOf(post.downCount) : "呵呵哒");
        updateCommentInfo(post);
        shareN.setTag(post);
        likeN.setTag(post);
        disLikeN.setTag(post);
        view.setOnClickListener(this);
        shareN.setOnClickListener(this);
        likeN.setOnClickListener(this);
        disLikeN.setOnClickListener(this);
    }

    private void updateCommentInfo(CfPost post) {
        mTvCommentN.setText(post.replyCount > 0 ? String.valueOf(post.replyCount) : "评论");
    }

    private void share() {
        mSnsHelper.openShareBoard(this, mCfPost);
    }

    private void showReplyMenuDialog() {
        if (mReplyMenuDialog == null) {
            mReplyMenuDialog = new LightDialog(this);
            mReplyMenuDialog.setTitle(R.string.please_choose);
            mReplyMenuDialog.setItems(R.array.channel_post_comment_menu_mine, this);
        }
        mReplyMenuDialog.show();
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this);
            mMenuDialog.setTitle(R.string.please_choose);
        }
        if (Utils.isMianLiaoService(mCfPost.userInfo) || mCfPost.isUserAreSuperModerator()) {
            if (mCfPost.userInfo.isMine(this)) {
                mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_mine_uncollect :
                        R.array.channel_post_menu_mine_collect, this);
            } else {
                mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_uncollect :
                        R.array.channel_post_menu_collect, this);
            }
        } else if (Utils.isMianLiaoService(mUserInfo) || mCfPost.isUserAreSuperModerator(mUserInfo.userId)) {
            if (mCfPost.isStickLvl()) {
                mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_uncollect_moderator_stick
                        : R.array.channel_post_menu_mine_collect_moderator_stick, this);
            } else {
                mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_uncollect_moderator
                        : R.array.channel_post_menu_mine_collect_moderator, this);
            }
        } else {
            if (mCfPost.isModerator(mUserInfo.userId) && !mCfPost.isUserAreSuperModerator()) {
                if (mCfPost.isStickLvl()) {
                    mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_uncollect_moderator_stick
                            : R.array.channel_post_menu_mine_collect_moderator_stick, this);
                } else {
                    mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_uncollect_moderator
                            : R.array.channel_post_menu_mine_collect_moderator, this);
                }
            } else {
                if (mCfPost.userInfo.isMine(this)) {
                    mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_mine_uncollect :
                            R.array.channel_post_menu_mine_collect, this);
                } else {
                    mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_uncollect :
                            R.array.channel_post_menu_collect, this);
                }
            }
        }
        mMenuDialog.show();
    }

    private void showPostNoticeDialog() {
        if (mPostNoticeDialog == null) {
            mPostNoticeDialog = new LightDialog(this);
            mPostNoticeDialog.setTitle(R.string.confirm);
            mPostNoticeDialog.setMessage(R.string.cf_delete_post_confirm);
            mPostNoticeDialog.setNegativeButton(R.string.you_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mTaskManager.startForumDeleteTaskV4(mCfPost);
                }
            });
            mPostNoticeDialog.setPositiveButton(R.string.search_cancel, null);
        }
        mPostNoticeDialog.show();
    }

    private void showPepNoticeDialog() {
        if (mRepNoticeDialog == null) {
            mRepNoticeDialog = new LightDialog(this);
            mRepNoticeDialog.setTitle(R.string.confirm);
            mRepNoticeDialog.setMessage(R.string.cf_delete_reply_confirm);
            mRepNoticeDialog.setNegativeButton(R.string.you_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mTaskManager.startForumDeleteTaskV4(mTargetReply);
                }
            });
            mRepNoticeDialog.setPositiveButton(R.string.search_cancel, null);
        }
        mRepNoticeDialog.show();
    }

    private void startComment(String content, ArrayList<UserInfo> refUserInfos) {
        if (mIsReplyPost) {
            mTaskManager.startForumCommentTask(mCfPost, content, refUserInfos);
        } else {
            mTaskManager.startChannelPostReplyTask(mParentReply, mTargetReply, content, refUserInfos);
        }
    }

    private void showProfileActivity(UserInfo userInfo) {
        Intent iProfile = new Intent(this, NewProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        startActivity(iProfile);
    }

    private void toggleDanmaku() {
        mIsDanmakuShown = !mIsDanmakuShown;
        Utils.hideInput(mMessageEditor);
        if (mIsDanmakuShown) {
            mPtrListView.setMode(Mode.DISABLED);
            // mPtrListView.getRefreshableView().setEnabled(false);
            // mPtrListView.getRefreshableView().setClickable(false);
            mPtrListView.getRefreshableView().setEnabled(true);
            mPtrListView.getRefreshableView().setClickable(true);
            mDanmakuLayout.setOnClickListener(ForumPostDetailActivity.this);
            mDanmakuLayout.start();
            getTitleBar().setRightButtonImage(R.drawable.bottom_ic_danmu_off);
        } else {

            mPtrListView.setMode(Mode.BOTH);
            mPtrListView.getRefreshableView().setEnabled(true);
            mPtrListView.getRefreshableView().setClickable(true);
            mDanmakuLayout.setOnClickListener(null);
            if (mDanmakuLayout.isPaused()) {
                danmuResume(true);
            } else {
                mDanmakuLayout.end();
            }
            getTitleBar().setRightButtonImage(R.drawable.bottom_ic_danmu_on);
        }
        showImageEle();
        mAdapter.notifyDataSetChanged();
    }

    private void updatePost(CfPost post) {
        mCfPost = post;
        if (post.myUp) {
            mTvLikeNormal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_like_hover, 0, 0, 0);
            mTvHateNormal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_hate, 0, 0, 0);
        } else if (post.myDown) {
            mTvLikeNormal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_like, 0, 0, 0);
            mTvHateNormal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_hate_hover, 0, 0, 0);
        } else {
            mTvLikeNormal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_like, 0, 0, 0);
            mTvHateNormal.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_hate, 0, 0, 0);
        }

        mTvLikeNormal.setText(post.upCount > 0 ? String.valueOf(post.upCount) : "么么哒");
        mTvHateNormal.setText(post.downCount > 0 ? String.valueOf(post.downCount) : "呵呵哒");

    }

    private void updateView(View view, final CfPost post) {
        RichEmotionTextView tvDesc = (RichEmotionTextView) view.findViewById(R.id.tv_desc);
        if (TextUtils.isEmpty(post.content)) {
            tvDesc.setVisibility(View.GONE);
        } else {
            tvDesc.setVisibility(View.VISIBLE);
            tvDesc.setMovementMethod(MLLinkMovementMethod.getInstance());
            tvDesc.setText(post);
            tvDesc.setTopicSpanClickble(true);
            tvDesc.setAtSpanClickble(true);
        }
        FlexibleImageView fivImages = (FlexibleImageView) view.findViewById(R.id.fiv_images);
        fivImages.setImages(post.images);
        View textVote = view.findViewById(R.id.text_vote);
        View normalAction = view.findViewById(R.id.normal_action);
        ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
        MlVideoView videoView = (MlVideoView) view.findViewById(R.id.mv_video);
        ImageView mIvHotPost = (ImageView) view.findViewById(R.id.iv_hot_post);
//        JCVideoPlayerStandard jcVideoPlayerStandard = (JCVideoPlayerStandard) view.findViewById(R.id.videoplayer);
        ImageView ivModerator = (ImageView) view.findViewById(R.id.iv_moderator);
        VoiceView normalVoiceView = (VoiceView) view.findViewById(R.id.voice_view);
        View viewTribeFrom = view.findViewById(R.id.ll_tribe_from);

        if (mIsShowTribe && post.forumName != null && !"".equals(post.forumName)) {
            viewTribeFrom.setVisibility(View.VISIBLE);
            TextView tvSchoolName = (TextView) viewTribeFrom.findViewById(R.id.tv_from_where);
            TextView tvFrom = (TextView) viewTribeFrom.findViewById(R.id.tv_tribe_name);
            if (post.tribeId > 0) {
                tvSchoolName.setText(R.string.tribe_footer_from_tribe);
            } else {
                tvSchoolName.setText(R.string.tribe_footer_from_school);
            }
            tvFrom.setText(post.forumName);
            tvFrom.setTag(post);
            tvFrom.setOnClickListener(this);
        } else {
            viewTribeFrom.setVisibility(View.GONE);
        }

        mIvHotPost.setVisibility(post.isHotPost ? View.VISIBLE : View.GONE);

        // update moderator
        int moderIcon = getModeratorIcon(post);
        if (moderIcon > 0) {
            ivModerator.setImageResource(moderIcon);
            ivModerator.setVisibility(View.VISIBLE);
        } else {
            ivModerator.setVisibility(View.GONE);
        }

        // update type icon;it while show in day time ,or it should hide;
        int resIcon = post.userInfo.getTypeIcon();
        if (resIcon > 0) {
            ivTypeIcon.setImageResource(resIcon);
            ivTypeIcon.setVisibility(View.VISIBLE);
        } else {
            ivTypeIcon.setVisibility(View.GONE);
        }

        TextView tvScholl = (TextView) view.findViewById(R.id.tv_location);

        fivImages.setVisibility(View.VISIBLE);
        textVote.setVisibility(View.GONE);
        normalAction.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        normalVoiceView.setVisibility(View.GONE);

        view.findViewById(R.id.view_line).setBackgroundColor(0xFFEAEAEA);
        tvScholl.setText(post.userInfo.school);
        tvScholl.setVisibility(View.VISIBLE);

        View viewLine;
        switch (post.threadType) {
            case CfPost.THREAD_TYPE_NORMAL:
                normalAction.setVisibility(View.VISIBLE);
                viewLine = normalAction.findViewById(R.id.view_up_line);
                viewLine.setBackgroundColor(0xFFE6E6E6);
                break;
            case CfPost.THREAD_TYPE_TXT_VOTE:
                VoteView voteView = (VoteView) textVote;
                voteView.setVisibility(View.VISIBLE);
                voteView.show(post);
                normalAction.setVisibility(View.VISIBLE);
                viewLine = normalAction.findViewById(R.id.view_up_line);
                viewLine.setBackgroundColor(0xFFE6E6E6);
                fivImages.setVisibility(View.GONE);
                break;
            case CfPost.THREAD_TYPE_PIC_VOTE:
                break;
            case CfPost.THREAD_TYPE_TXT:
                fivImages.setVisibility(View.GONE);
                break;
            case CfPost.THREAD_TYPE_PIC_TXT:
                break;
            case CfPost.THREAD_TYPE_PIC_VOICE:
            case CfPost.THREAD_TYPE_VOICE:
                tvDesc.setVisibility(View.GONE);
                fivImages.setVisibility(View.GONE);
                mVoiceView = normalVoiceView;
                normalVoiceView.setVisibility(View.VISIBLE);
                normalVoiceView.show(post);
                break;
            case CfPost.THREAD_TYPE_RICH_MEDIA:
                tvDesc.setVisibility(View.GONE);
                fivImages.setVisibility(View.GONE);
                mWbView.setVisibility(View.VISIBLE);
                mWbView.loadUrl(Utils.getRichMediaRequestUrl(post.postId));
                break;
            case CfPost.THREAD_TYPE_VIDEO:
                fivImages.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.show(post);
                videoView.setActivity(this);
//                jcVideoPlayerStandard.setVisibility(View.VISIBLE);
//                HttpProxyCacheServer proxy = MianLiaoApp.getProxy(this);
//                String proxyUrl = proxy.getProxyUrl(post.videoUrl);
//                jcVideoPlayerStandard.setUp(post.videoUrl, "");
//                Picasso.with(ForumPostDetailActivity.this).load(post.videoThumbnail).
//                    placeholder(R.drawable.bg_default_big_day).into(jcVideoPlayerStandard.ivThumb);
            default:
                break;
        }
        updateHeaderView(view, post);
    }

    public View updateHeaderView(View view, CfPost post) {
        FrameLayout mFlHeader = (FrameLayout) view.findViewById(R.id.fl_header_content);
        return view;
    }

    private void updateReplyInfo(CfReply reply, CfRecord parent) {
        if (reply != null) {
            if (parent instanceof CfReply) {
                for (int i = 0; i < mReplys.size(); i++) {
                    if (mReplys.get(i).get(0) == parent) {
                        mReplys.get(i).get(0).upCount = parent.upCount;
                        break;
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    private void updateReply(CfReply reply, CfRecord parent) {
        if (reply != null) {
            ArrayList<CfReply> replies = null;
            if (parent instanceof CfReply) {
                for (int i = 0; i < mReplys.size(); i++) {
                    if (mReplys.get(i).get(0) == parent) {
                        reply.parentReply = (CfReply) parent;
                        replies = mReplys.get(i);
                        break;
                    }
                }
            }
            if (replies == null) {
                reply.parentReply = reply;
                replies = new ArrayList<CfReply>();
                mReplys.add(0, replies);
            }
            replies.add(reply);
            mDanmakuLayout.replyDanmaku(getDanmakuView(reply));
            mAdapter.notifyDataSetChanged();
            if (mIsDanmakuShown) {
                mDanmakuLayout.start();
            }
            showImageEle();
            updateCommentCountInfo();
        }
    }

    private void updateCommentCountInfo() {
        mCommentCount = mCfPost.replyCount;
        mTvCommentCount.setText(getString(R.string.channel_comments_count, mCommentCount));
        updateCommentInfo(mCfPost);
    }

    @Override
    public void onEmotionClicked(Emotion emotion) {
        mMessageEditor.getText().insert(mMessageEditor.getSelectionStart(), emotion.getSpannable(this));
    }

    @Override
    public void onBackspaceClicked() {
        Utils.dispatchDelEvent(mMessageEditor);
    }

    private class ThreadInfoTask extends MsTask {

        int mThreadId;

        public ThreadInfoTask(int threadId) {
            super(ForumPostDetailActivity.this, MsRequest.THREAD_INFO);
            mThreadId = threadId;

        }

        @Override
        protected String buildParams() {
            return new StringBuffer("thread_id=").append(mThreadId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mCfPost = CfPost.fromJson(response.json.optJSONObject("response"));
                mHandler.sendMessage(Message.obtain());
                mLlInput.setVisibility(View.VISIBLE);
            }
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            Utils.hideInput(mMessageEditor);
            mEmotionPicker.setVisible(false);
        }
        return false;
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        CharSequence lastChar;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() >= 1) {
                lastChar = s.toString().substring(s.length() - 1);
                if ("@".equals(lastChar) && mHasCallback) {
                    startActivityForResult(new Intent(ForumPostDetailActivity.this, RefFriendActivity.class), BasePostActivity.REQUEST_REF_SQUARE);
                    mHasCallback = false;
                }
            }
        }
    };

    @Override
    public void onDelClick(int index) {
        if (index < mRefFriends.size()) {
            mRefFriends.remove(index);
        }
    }

    private String exchangeAt(String content) {
        Matcher mRefMatcher = Utils.REF_FRIEND_PATTERN.matcher(content);
        int refLastIndex = 0;
        SpannableStringBuilder ssb = new SpannableStringBuilder(content);
        while (mRefMatcher.find()) {
            String ts = mRefMatcher.group();
            int index = content.indexOf(ts, refLastIndex);
            refLastIndex = index;
            if (content.length() > index + ts.length() + 1) {
                ssb.replace(index, index + ts.length() + 1, "");
            } else {
                ssb.replace(index, index + ts.length(), "");
            }
            mRefMatcher = Utils.REF_FRIEND_PATTERN.matcher(ssb.toString());
            content = ssb.toString();
        }
        return ssb.toString();

    }

}
