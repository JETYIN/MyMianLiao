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
import android.widget.GridView;
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
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.RefFriendActivity;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.EmotionPicker;
import com.tjut.mianliao.component.EmotionPicker.EmotionListener;
import com.tjut.mianliao.component.FlexibleImageView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.MLLinkMovementMethod;
import com.tjut.mianliao.component.MlWebView;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.RichEmotionTextView;
import com.tjut.mianliao.component.RichMlEditText;
import com.tjut.mianliao.component.RichMlEditText.OnAtDelClicklistener;
import com.tjut.mianliao.component.forum.ReplyView;
import com.tjut.mianliao.component.nova.DanmakuLayout;
import com.tjut.mianliao.component.nova.MlVideoView;
import com.tjut.mianliao.component.nova.PicVoteView;
import com.tjut.mianliao.component.nova.PictureVoiceView;
import com.tjut.mianliao.component.nova.VoiceView;
import com.tjut.mianliao.component.nova.VoteView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Emotion;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfRecord;
import com.tjut.mianliao.forum.CfRecord.AtUser;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.forum.nova.ReplyViewManager.ReplyViewListener;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
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
import com.umeng.analytics.MobclickAgent;

public class ForumPostDetailActivity extends BaseActivity implements OnRefreshListener2<ListView>,
        OnClickListener, MsTaskListener, DialogInterface.OnClickListener, ReplyViewListener,
        OnFocusChangeListener, EmotionListener, OnTouchListener , OnAtDelClicklistener{

    public static final String SP_IS_FIRST = "sp_forum_post_detail";

    public static final String EXTRL_POST_DATA = "extrl_post_data";
    public static final String EXTRL_CHANNEL_INFO = "extrl_channel_info";
    public static final String EXTRL_POST_DATA_ID = "extrl_post_data_id";
    public static final String EXTRL_SHOW_OTHER_SCHOOL_TAG = "extrl_show_other_school_tag";
    public static final String EXTRL_SHOW_ISSHOW_CHANNELNAME = "extrl_show_isshow_channelname";
    public static final String EXTRL_SHOW_ISSHOW_SCHOOL = "extrl_show_isshow_school";
    public static final String EXTRL_ISSHOW_SCHOOL_TOPIC = "extrl_isshow_school_topic";
    public static final String EXTRL_IS_FORM_TRIBE = "extrl_is_form_tribe";
    public static final String EXTRL_IS_SHOW_TRIBE = "extrl_is_show_tribe";

    private static final String TAG = "ForumPostDetailActivity";
    
    private static final int MAX_REPLY_LEN = Integer.MAX_VALUE;

    private PullToRefreshListView mPtrListView;
    private PostCommentsAdapter mAdapter;

    private CfPost mCfPost;
    private UserInfo mUserInfo;

    private ProImageView mAvatarView;
    private ImageView mImageGender;
    private ProImageView mIvMedal;

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
    private LightDialog mMenuDialog, mReplyMenuDialog;
    private TextView mTvCommentCount;
    private ImageView mIvNoComment, mIvCommentSuc;
    public static View mViewBg;

    private ReplyViewManager mViewManager;
    private boolean mIsReplyPost = true;
    private TextView mTvFlowersCount;
    private LinearLayout mLlFlowerGroup;
    private ChannelInfo mChannelInfo;
    private LinearLayout mLlInput;

    private DanmakuLayout mDanmakuLayout;
    private boolean mIsDanmakuShown = true;
    private HashMap<CfReply, View> mDanmakuViews;
    private int mCommentCount;
    private SharedPreferences mPreferences;
    private NewsGuidDialog mGuidDialog;
    private boolean mIsNightMode;
    private Settings mSettings;
    private boolean mLikeflag = true;
    private boolean mShowOtherSchoolTag, mShowChannelnameTag, mIsShowSchoolTag;
    private ImageView mIvEmotion;
    private boolean isRefreshing;
    private int mMaxFloor = 0;
    private DanmuThread mDanmukuThread = null;
    private boolean mAutoCalcFloor = true; // 是否自动计算楼层
    private ProgressBar mProgressBar;
    private TextView mTvRefreshing;
    private MlWebView mWbView;

    private MsTask mCurrentTask;
    private boolean isShowSchoolTopic;
    private boolean mHasCallback = true;
    private ArrayList<AtUser> mAtUsers = new ArrayList<AtUser>();
    private ArrayList<UserInfo> mRefFriends = new ArrayList<UserInfo>();

    private boolean mHasGotReplies;
    private boolean mIsFromTribe, mIsShowTribe;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_channel_post_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mIsNightMode = mSettings.isNightMode();
        mCfPost = (CfPost) getIntent().getParcelableExtra(EXTRL_POST_DATA);
        mShowOtherSchoolTag = getIntent().getBooleanExtra(EXTRL_SHOW_OTHER_SCHOOL_TAG, false);
        mShowChannelnameTag = getIntent().getBooleanExtra(EXTRL_SHOW_ISSHOW_CHANNELNAME, false);
        mIsShowSchoolTag = getIntent().getBooleanExtra(EXTRL_SHOW_ISSHOW_SCHOOL, false);
        isShowSchoolTopic = getIntent().getBooleanExtra(EXTRL_ISSHOW_SCHOOL_TOPIC, false);
        mIsFromTribe = getIntent().getBooleanExtra(EXTRL_IS_FORM_TRIBE, false);
        mIsShowTribe = getIntent().getBooleanExtra(EXTRL_IS_SHOW_TRIBE, true);
        mPreferences = DataHelper.getSpForData(this);
        mUserInfo = AccountInfo.getInstance(this).getUserInfo();
        mLlInput = (LinearLayout) findViewById(R.id.ll_input);
        if (mCfPost != null) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    refreshMainView();
                }
            });
        } else {
            mLlInput.setVisibility(View.GONE);
            int postId = getIntent().getIntExtra(EXTRL_POST_DATA_ID, 0);
            new ThreadInfoTask(postId).executeLong();
        }

    }

    private int[] getGuidImageRes() {
        int[] imgRes = { mIsNightMode ? R.drawable.guid_barrage_black : R.drawable.guid_barrage };
        return imgRes;
    }

    private void refreshMainView() {
        mDanmakuViews = new HashMap<CfReply, View>();
        mDanmakuLayout = (DanmakuLayout) findViewById(R.id.danmaku_layout);
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_comments);

        mChannelInfo = (ChannelInfo) getIntent().getParcelableExtra(EXTRL_CHANNEL_INFO);
        mCfPost.getLikedUsers(ForumPostDetailActivity.this);

        mMessageEditor = (RichMlEditText) findViewById(R.id.et_message);
        mMessageEditor.addTextChangedListener(mTextWatcher);
        mMessageEditor.setShouldMatcherAt();
        mEmotionPicker = (EmotionPicker) findViewById(R.id.ep_emotions);
        mIvEmotion = (ImageView) findViewById(R.id.iv_extention);

        mEmotionPicker.setEmotionListener(ForumPostDetailActivity.this);
        mMessageEditor.setOnFocusChangeListener(ForumPostDetailActivity.this);

        checkDayNightUI();

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
        mAvatarView = (ProImageView) headerView.findViewById(R.id.av_avatar);
        mImageGender = (ImageView) headerView.findViewById(R.id.iv_gender);
        mIvMedal = (ProImageView) headerView.findViewById(R.id.iv_medal);
        mTvFlowersCount = (TextView) headerView.findViewById(R.id.tv_visitor_count);
        mLlFlowerGroup = (LinearLayout) headerView.findViewById(R.id.ll_flower_group);
        TextView tvLoc = (TextView) headerView.findViewById(R.id.tv_location);
        headerView.findViewById(R.id.iv_vip_bg).setVisibility(
                mCfPost.userInfo.vip && !mIsNightMode ? View.VISIBLE : View.GONE);
//        headerView.findViewById(R.id.iv_vip).setVisibility(
//                mCfPost.userInfo.vip && !mIsNightMode ? View.VISIBLE : View.GONE);
        Utils.setText(headerView, R.id.tv_name, mCfPost.userInfo.getDisplayName(ForumPostDetailActivity.this));
        Utils.setText(headerView, R.id.tv_intro,
                mShowOtherSchoolTag && !Utils.isMianLiaoService(mCfPost.userInfo) ? mCfPost.getTimeAndSchool(mCfPost)
                        : Utils.getPostShowTimeString(mCfPost.createdOn));
        if (mIsShowSchoolTag || isShowSchoolTopic) {
            tvLoc.setText(mCfPost.userInfo.school);
            tvLoc.setVisibility(View.VISIBLE);
        }
        if (isShowSchoolTopic) {
            Utils.setText(headerView, R.id.tv_intro, Utils.getPostShowTimeString(mCfPost.createdOn)); 
        }
        mImageGender.setImageResource(mCfPost.userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy);
        if (mCfPost.userInfo.getLatestBadge() != null && mCfPost.userInfo.getLatestBadge().startsWith("http") && !mIsNightMode) {
            mIvMedal.setVisibility(View.VISIBLE);
            mIvMedal.setImage(mCfPost.userInfo.getLatestBadge(), R.drawable.ic_medal_empty);
        } else {
            mIvMedal.setVisibility(View.GONE);
        }
        mAvatarView.setImage(mCfPost.userInfo.getAvatar(), mCfPost.userInfo.defaultAvatar());
        mAvatarView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showProfileActivity(mCfPost.userInfo);
            }
        });

        getTitleBar().setTitle(getString(R.string.course_entry));
        getTitleBar().showRightButton(R.drawable.selector_btn_checkbox, ForumPostDetailActivity.this);

        mAdapter = new PostCommentsAdapter();
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setOnRefreshListener(ForumPostDetailActivity.this);

        toggleDanmaku();
        showImageEle();
        fetchComments(true);
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            mMessageEditor.setBackgroundColor(0X33000000);
            mMessageEditor.setHintTextColor(0XFF807E8B);
            mMessageEditor.setTextColor(0XFF807E8B);
            findViewById(R.id.ll_post_detail).setBackgroundResource(R.drawable.bg);
            findViewById(R.id.rl_input).setBackgroundColor(0xFF1B1425);
            mEmotionPicker.setBackgroundColor(0xFF1B1425);
        }
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
        boolean isFirst = mPreferences.getBoolean(SP_IS_FIRST, false);
        if (isFirst) {
            mGuidDialog = new NewsGuidDialog(this, R.style.Translucent_NoTitle);
            mGuidDialog.showGuidImage(getGuidImageRes(), SP_IS_FIRST);
        }

        // mViewBg.setVisibility(View.GONE);
        super.onResume();
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
        System.gc();
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
            ProImageView ivMedal = (ProImageView) view.findViewById(R.id.iv_medal);
            RichEmotionTextView mTvContent = (RichEmotionTextView) view.findViewById(R.id.tv_desc);
            TextView tvLikedCount = (TextView) view.findViewById(R.id.tv_liked_count);
            ReplyView replyView = (ReplyView) view.findViewById(R.id.rv_comments);
            TextView tvLocation = (TextView) view.findViewById(R.id.tv_location);
            tvLocation.setVisibility(mChannelInfo == null ? View.GONE : View.VISIBLE);
            ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
            ImageView ivBuilderIcon = (ImageView) view.findViewById(R.id.iv_master_build);
            View viewVip = view.findViewById(R.id.iv_vip);
            View viewVipBg = view.findViewById(R.id.iv_vip_bg);
            TextView tvFloor = (TextView) view.findViewById(R.id.tv_floor);

            avatar.setOnClickListener(ForumPostDetailActivity.this);
            tvName.setOnClickListener(ForumPostDetailActivity.this);

            if (!mIsNightMode) {
                mTvContent.setMovementMethod(MLLinkMovementMethod.getInstance());
                mTvContent.setTopicSpanClickble(true);
                mTvContent.setText(reply);
                mTvContent.setAtSpanClickble(true);
            } else {
                mTvContent.setText(reply.content);
                tvFloor.setTextColor(0xff888888);
                tvFloor.setBackgroundColor(0x4ddfe7ff);
            }
            
            // update vip view
            if (!mIsNightMode && reply.userInfo.vip) {
//                viewVip.setVisibility(View.VISIBLE);
                viewVipBg.setVisibility(View.VISIBLE);
            } else {
//                viewVip.setVisibility(View.GONE);
                viewVipBg.setVisibility(View.GONE);
            }
            
            tvLocation.setText(reply.userInfo.school);
            avatar.setImage(reply.userInfo.getAvatar(), reply.userInfo.defaultAvatar());
            tvName.setText(reply.userInfo.getDisplayName(ForumPostDetailActivity.this));
            ivGender.setImageResource(reply.userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy);
            if (reply.userInfo.getLatestBadge() != null && reply.userInfo.getLatestBadge().startsWith("http") && !mIsNightMode) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImage(reply.userInfo.getLatestBadge(), R.drawable.ic_medal_empty);
            } else {
                ivMedal.setVisibility(View.GONE);
            }
            Utils.setText(view, R.id.tv_intro,
                    mShowOtherSchoolTag && !Utils.isMianLiaoService(reply.userInfo) ? reply.getTimeAndSchool(reply)
                            : Utils.getPostShowTimeString(reply.createdOn));
            
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
            if (!mIsNightMode && resIcon > 0) {
                ivTypeIcon.setImageResource(reply.userInfo.getTypeIcon());
                ivTypeIcon.setVisibility(View.VISIBLE);
            } else {
                ivTypeIcon.setVisibility(View.GONE);
            }
            // this is builder icon
            if (getBuilderIcon(reply) > 0) {
                ivBuilderIcon.setImageResource(getBuilderIcon(reply));
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
        if (mChannelInfo != null && mChannelInfo.isModerator(post)) {
            return R.drawable.icon_muster_section;
        }
        return 0;
    }
    
    public int getBuilderIcon(CfReply reply) {
        if (mChannelInfo != null && mChannelInfo.isModerator(reply)) {
            return R.drawable.icon_muster_section;
        }
        if (reply.userInfo.userId == mCfPost.userInfo.userId) {
            return R.drawable.icon_muster_building;
        }
        return 0;
    }
    
    public int getBuilderIcon(CfPost post) {
        if (mChannelInfo != null && mChannelInfo.isModerator(post)) {
            return R.drawable.icon_muster_section;
        }
        if (post.userInfo.userId == mCfPost.userInfo.userId) {
            return R.drawable.icon_muster_building;
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
                mTargetReply = (CfReply) v.getTag();
                mParentReply = mTargetReply;
                if (mParentReply.userInfo.isMine(this)) {
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
                if (mIsNightMode) {
                    toast("你是揭不开我的面具滴");
                    return;
                }
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
                mLikeflag = false;
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
                int tribeId = (int) v.getTag();
                Intent trIntent = new Intent(ForumPostDetailActivity.this, TribeDetailActivity.class);
                trIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, tribeId);
                startActivity(trIntent);
                break;
            default:
                break;
        }
    }

    private void checkIcon() {
        if (mEmotionPicker.isShown()) {
            mIvEmotion.setImageResource(mIsNightMode ? R.drawable.button_ic_key_black : R.drawable.button_ic_key);
        } else {
            mIvEmotion.setImageResource(mIsNightMode ? R.drawable.button_emotion_black : R.drawable.button_emotion);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        if (mDanmakuLayout == null) {
            super.onBackPressed();
        } else {
            if (mDanmakuLayout.isPaused()) {
                danmuResume(false);
            } else {
                if (mDanmukuThread != null) {
                    mDanmukuThread.setAlive(false);
                    mDanmukuThread = null;
                    System.out.println("----> onBack " + System.currentTimeMillis());
                }
                super.onBackPressed();
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
        if (mTargetReply.userInfo.isMine(this)) {
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
                    mTaskManager.startForumDeleteTask(mTargetReply);
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
                    if (mCfPost.userInfo.isMine(this)) {
                        mTaskManager.startForumDeleteTask(mCfPost);
                    } else {
                        report(mCfPost, null);
                        MobclickAgent.onEvent(this, MStaticInterface.REPORT);
                    }
                    break;
                default:
                    break;
            }
        }
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
                }
                showCommmentSucc();
                break;
            case FORUM_COMMENT_REPLY:
                getTitleBar().hideProgress();
                if (response.isSuccessful()) {
                    CfReply reply = CfReply.fromJson(response.getJsonObject());
                    reply.floor = mMaxFloor + 1;
                    updateReply(reply, (CfRecord) response.value);
                }
                showCommmentSucc();
                break;
            case FORUM_DELETE_REPLY:
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

    private void copyToClipboard(boolean isReply) {
        String mCopyContent;
        if (isReply) {
             mCopyContent= exchangeAt(mTargetReply.content);
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
                mIsNightMode ? 0XFFB95167 : 0XFF45D2DC, false));
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
        if (mCfPost.userInfo.isMine(this)) {
            mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_mine_uncollect :
                R.array.channel_post_menu_mine_collect, this);
        } else {
            mMenuDialog.setItems(mCfPost.collected ? R.array.channel_post_menu_uncollect :
                R.array.channel_post_menu_collect, this);
        }
        mMenuDialog.show();
    }

    private void startComment(String content, ArrayList<UserInfo> refUserInfos) {
        if (mIsReplyPost) {
            mTaskManager.startForumCommentTask(mCfPost, content, refUserInfos);
        } else {
            mTaskManager.startChannelPostReplyTask(mParentReply, mTargetReply, content, refUserInfos);
        }
    }

    private void showProfileActivity(UserInfo userInfo) {
        if (mIsNightMode) {
            toast("你是揭不开我的面具滴");
            return;
        }
        Intent iProfile = new Intent(this, ProfileActivity.class);
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
        if (post.content == null || "".equals(post.content)) {
            tvDesc.setVisibility(View.GONE);
        } else {
            tvDesc.setVisibility(View.VISIBLE);
        }
        if (!mIsNightMode) {
            tvDesc.setMovementMethod(MLLinkMovementMethod.getInstance());
            tvDesc.setText(post);
            tvDesc.setChannelInfo(mChannelInfo);
            tvDesc.setTopicSpanClickble(true);
            tvDesc.setAtSpanClickble(true);
        } else {
            tvDesc.setText(post.content);
        }
        FlexibleImageView fivImages = (FlexibleImageView) view.findViewById(R.id.fiv_images);
        fivImages.setImages(post.images);
        mAtUsers = post.atUsers;
        View channelVote = view.findViewById(R.id.channel_vote);
        View textVote = view.findViewById(R.id.text_vote);
        View channelVoicePicture = view.findViewById(R.id.channel_voice_picture);
        View normalAction = view.findViewById(R.id.normal_action);
        ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon); 
        ImageView ivModerator = (ImageView) view.findViewById(R.id.iv_master_section);
        MlVideoView videoView = (MlVideoView) view.findViewById(R.id.mv_video);
        VoiceView normalVoiceView = (VoiceView) view.findViewById(R.id.voice_view);
        View viewTribeFrom = view.findViewById(R.id.ll_tribe_from);
        
        if (post.tribeId > 0 && mIsShowTribe) {
            viewTribeFrom.setVisibility(View.VISIBLE);
            TextView tvFrom = (TextView) viewTribeFrom.findViewById(R.id.tv_tribe_name);
            tvFrom.setText(post.forumName);
            tvFrom.setTag(post.tribeId);
            tvFrom.setOnClickListener(this);
        } else {
            viewTribeFrom.setVisibility(View.GONE);
        }
        
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
        if (!mIsNightMode && resIcon > 0) {
            ivTypeIcon.setImageResource(resIcon);
            ivTypeIcon.setVisibility(View.VISIBLE);
        } else {
            ivTypeIcon.setVisibility(View.GONE);
        }
        
        TextView tvScholl = (TextView) view.findViewById(R.id.tv_location);
        if (mShowChannelnameTag) {
            TextView channelName = (TextView) view.findViewById(R.id.tv_channel_name);
            channelName.setVisibility(View.VISIBLE);
            channelName.setText("#" + post.forumName + "#");
        }

        fivImages.setVisibility(View.VISIBLE);
        channelVote.setVisibility(View.GONE);
        textVote.setVisibility(View.GONE);
        channelVoicePicture.setVisibility(View.GONE);
        normalAction.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        normalVoiceView.setVisibility(View.GONE);

        view.findViewById(R.id.view_line).setBackgroundColor(mIsNightMode ? 0x34FFFFFF : 0xFFEAEAEA);
        tvScholl.setText(post.userInfo.school);
        tvScholl.setVisibility(mChannelInfo == null || mShowChannelnameTag || Utils.isMianLiaoService(post.userInfo) ? View.GONE
                : View.VISIBLE);

        if (mShowChannelnameTag) {
            TextView channelName = (TextView) view.findViewById(R.id.tv_channel_name);
            channelName.setVisibility(View.VISIBLE);
            channelName.setText("#" + post.forumName + "#");
        }
        View viewLine;
        switch (post.threadType) {
            case CfPost.THREAD_TYPE_NORMAL:
                normalAction.setVisibility(View.VISIBLE);
                viewLine = normalAction.findViewById(R.id.view_up_line);
                viewLine.setBackgroundColor(mIsNightMode ? 0x34FFFFFF : 0xFFE6E6E6);
                break;
            case CfPost.THREAD_TYPE_TXT_VOTE:
                VoteView voteView = (VoteView) textVote;
                voteView.setVisibility(View.VISIBLE);
                voteView.show(post);
                normalAction.setVisibility(View.VISIBLE);
                viewLine = normalAction.findViewById(R.id.view_up_line);
                viewLine.setBackgroundColor(mIsNightMode ? 0x34FFFFFF : 0xFFE6E6E6);
                fivImages.setVisibility(View.GONE);
                break;
            case CfPost.THREAD_TYPE_PIC_VOTE:
                fivImages.setVisibility(View.GONE);
                PicVoteView picVoteView = (PicVoteView) channelVote;
                picVoteView.setVisibility(View.VISIBLE);
                picVoteView.show(post);
                break;
            case CfPost.THREAD_TYPE_TXT:
                fivImages.setVisibility(View.GONE);
                break;
            case CfPost.THREAD_TYPE_PIC_TXT:
                break;
            case CfPost.THREAD_TYPE_PIC_VOICE:
                tvDesc.setVisibility(View.GONE);
                fivImages.setVisibility(View.GONE);
                PictureVoiceView voiceView = (PictureVoiceView) channelVoicePicture;
                voiceView.setVisibility(View.VISIBLE);
                voiceView.show(post);
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
                break;
            case CfPost.THREAD_TYPE_VOICE:
                normalVoiceView.setVisibility(View.VISIBLE);
                normalVoiceView.show(post);
                break;
            default:
                break;
        }
    }

    private boolean showChannelName(CfPost post) {
        switch (post.forumType) {
            case 5:
                return false;
            default:
                return true;
        }
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
                refreshMainView();
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
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
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
            if (content.length() > index + ts.length() + 1){
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
