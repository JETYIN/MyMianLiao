package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.FlexibleImageView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.MLLinkMovementMethod;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.RichEmotionTextView;
import com.tjut.mianliao.component.nova.MlVideoView;
import com.tjut.mianliao.component.nova.PicVoteView;
import com.tjut.mianliao.component.nova.VoiceView;
import com.tjut.mianliao.component.nova.VoteView;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class ForumMyPostActivity extends BaseActivity implements TabController.TabListener,
        OnRefreshListener2<ListView>, OnClickListener, MsTaskListener,
        DialogInterface.OnClickListener {

    public static final String EXT_SHOW_SUBMENU = "ext_show_submenu";

    private ArrayList<VoiceView> mVoiceViews;

    private PullToRefreshListView mPtrListView;
    private TabController mTabController;
    private ArrayList<CfPost> mMyPostList;
    private ArrayList<CfReply> mMyReplyList;
    private int mNameColor;
    private boolean mIsPost = true;
    private MyReplyAdapter mReplyAdapter;
    private ForumPostAdapter mPostAdapter;
    private CfPost mCurrentPost, mLastPost;
    private MsTaskManager mTaskManager;
    private SnsHelper mSnsHelper;
    private boolean mIsNightMode;
    private Settings mSettings;
    private boolean mLikeFlag = true;
    private boolean mClickble = true;
    private LightDialog mMenuDialog;
    private CfReply mCurrentReply;
    private long mCurrentMills;
    private boolean mIsSamePost;
    
    private View mViewNoContent;
    private FrameLayout mViewParent;

    @Override
    protected int getLayoutResID() {
        return R.layout.forum_my_post_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mIsNightMode = mSettings.isNightMode();
        mVoiceViews = new ArrayList<>();
        boolean showMenu = getIntent().getBooleanExtra(EXT_SHOW_SUBMENU, false);
        if (!showMenu) {
            getTitleBar().setTitle(getString(R.string.fe_list_attended));
            findViewById(R.id.ll_menu).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ll_menu).setVisibility(View.GONE);
            getTitleBar().setTitle(getString(R.string.cf_post_mine));
        }
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mSnsHelper = SnsHelper.getInstance();
        mMyPostList = new ArrayList<CfPost>();
        mMyReplyList = new ArrayList<CfReply>();
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_post_stream);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);

        mReplyAdapter = new MyReplyAdapter();
        mPostAdapter = new ForumPostAdapter(this);
        mPostAdapter.setActivity(this);
        mPostAdapter.showOtherSchool();
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickbel(true);
        mPostAdapter.setShowNoContent(false);
        mPtrListView.setAdapter(mPostAdapter);
        mNameColor = getResources().getColor(R.color.channel_post_reply_name);

        mTabController = new TabController();
        TextTab textTabMine = new TextTab((TextView) findViewById(R.id.tv_type_mine));
        TextTab textTabRep = new TextTab((TextView) findViewById(R.id.tv_type_my_reply));
        textTabMine.setNightMode(mIsNightMode);
        textTabRep.setNightMode(mIsNightMode);
        textTabRep.setChosen(false);
        mTabController.add(textTabMine);
        mTabController.add(textTabRep);
        mTabController.setListener(this);
        mTabController.select(0);

        mViewNoContent = mInflater.inflate(R.layout.view_no_content, null);
        mViewParent = (FrameLayout) findViewById(R.id.view_parent);
        checkDayNightUI();
        mViewNoContent.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                reload();
            }
        });

    }
    @Override
    protected void onResume() {
    	mClickble = true;
    	super.onResume();
    }

	private void checkDayNightUI() {
		if (mIsNightMode) {
            findViewById(R.id.ll_my_post).setBackgroundResource(R.drawable.bg);
            mPtrListView.setBackgroundColor(Color.TRANSPARENT);
        }
	}
	
    private void hideNoMessage() {
        if (mViewParent != null && mViewNoContent != null) {
            mViewParent.removeView(mViewNoContent);
        }
    }
    
    private void showNoMessage() {
        if (mViewParent != null && mViewNoContent != null) {
            mViewParent.removeView(mViewNoContent);
            resetNoContentView();
            mViewParent.addView(mViewNoContent);
        }
    }
    
    private void resetNoContentView() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.VISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.GONE);
    }
    
    private void reload() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.INVISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.VISIBLE);
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

    @Override
    public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
        if (!selected) {
            return;
        }
        switch (index) {
            case 0:
                mIsPost = true;
                mPtrListView.setAdapter(mPostAdapter);
                getMyPost(true);
                break;
            case 1:
                mIsPost = false;
                mPtrListView.setAdapter(mReplyAdapter);
                getMyReply(true);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTaskManager.unregisterListener(this);
        for (VoiceView voiceView : mVoiceViews) {
            voiceView.onDestroy();
        }
    }

    private void getMyPost(boolean refresh) {
        new ListMyPostTask(refresh).executeLong();
    }

    private void getMyReply(boolean refresh) {
        new ListMyReplyTask(refresh).executeLong();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mIsPost) {
            getMyPost(true);
        } else {
            getMyReply(true);
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mIsPost) {
            getMyPost(false);
        } else {
            getMyReply(false);
        }
    }

    private class ListMyReplyTask extends MsTask {
        private int mOffset;
        private boolean refresh;
        public ListMyReplyTask(boolean refresh) {
            super(ForumMyPostActivity.this, MsRequest.LIST_MY_REPLY);
            this.refresh = refresh;
            mOffset = refresh ? 0 : mMyReplyList.size();

        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<CfReply> replys = JsonUtil.getArray(ja, CfReply.TRANSFORMER);
                    if (refresh) {
                        mMyReplyList.clear();
                        mMyReplyList.addAll(replys);
                        if (replys != null && replys.size() > 0) {
                            hideNoMessage();
                        } else {
                            showNoMessage();
                        }
                    } else {
                        mMyReplyList.addAll(replys);
                    }
                    mReplyAdapter.notifyDataSetChanged();
            } else {
                showNoMessage();
            }
        }
    }

    private class ListMyPostTask extends MsTask {
        private int mOffset;
        private boolean refresh;

        public ListMyPostTask(boolean refresh) {
            super(ForumMyPostActivity.this, MsRequest.CF_LIST_MY_POSTS);
            this.refresh = refresh;
            mOffset = refresh ? 0 : mPostAdapter.getCount();

        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                if (refresh) {
                    mPostAdapter.reset(posts);
                    if (posts != null && posts.size() > 0) {
                        hideNoMessage();
                    } else {
                        showNoMessage();
                    }
                } else {
                    mPostAdapter.addAll(posts);
                }
            } else {
                showNoMessage();
            }
        }
    }

    private class MyPostAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mMyPostList.size();
        }

        @Override
        public CfPost getItem(int position) {
            return mMyPostList.get(position);
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
                view = mInflater.inflate(R.layout.list_item_channel_posts, parent, false);
            }

            final CfPost post = getItem(position);
            updateView(view, post);
            setViewData(view, post);
            view.setTag(post);

            AvatarView image = (AvatarView) view.findViewById(R.id.av_avatar);
            image.setImage(post.userInfo.getAvatar(), post.userInfo.defaultAvatar());
            view.findViewById(R.id.iv_vip).setVisibility(post.userInfo.vip && !mIsNightMode ?
                    View.VISIBLE : View.GONE);
            view.findViewById(R.id.iv_vip_bg).setVisibility(post.userInfo.vip && !mIsNightMode ?
                    View.VISIBLE : View.GONE);

            image.setTag(post);
            image.setOnClickListener(ForumMyPostActivity.this);

            TextView name = (TextView) view.findViewById(R.id.tv_name);
            name.setText(post.userInfo.getDisplayName(ForumMyPostActivity.this));
            name.setTag(post);
            name.setOnClickListener(ForumMyPostActivity.this);

            Utils.setText(view, R.id.tv_intro, post.getTimeAndRelation());
            ImageView ivGender = (ImageView) view.findViewById(R.id.iv_gender);
            ivGender.setImageResource(post.userInfo.gender == 0 ?
                    R.drawable.img_girl : R.drawable.img_boy);
            ProImageView ivMedal = (ProImageView) view.findViewById(R.id.iv_medal);
            if (post.userInfo.getLatestBadge() != null && post.userInfo.getLatestBadge().startsWith("http") && !mIsNightMode) {
                ivMedal.setVisibility(View.VISIBLE);
                ivMedal.setImage(post.userInfo.getLatestBadge(), R.drawable.ic_medal_empty);
            } else {
                ivMedal.setVisibility(View.GONE);
            }
            TextView channelName = (TextView) view.findViewById(R.id.tv_channel_name);
            channelName.setText("#" + post.forumName + "#");
            channelName.setVisibility(showChannelName(post) ? View.VISIBLE : View.INVISIBLE);
            ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
            // update type icon ,it while show in day time,or it should hide
            int resIcon = post.userInfo.getTypeIcon();
            if (!mIsNightMode && resIcon > 0) {
                ivTypeIcon.setImageResource(resIcon);
                ivTypeIcon.setVisibility(View.VISIBLE);
            } else {
                ivTypeIcon.setVisibility(View.GONE);
            }
            return view;
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

    private void setViewData(View view, CfPost post) {
        LinearLayout likeN = (LinearLayout) view.findViewById(R.id.ll_like_normal);
        LinearLayout disLikeN = (LinearLayout) view.findViewById(R.id.ll_dislike_normal);
        LinearLayout commentN = (LinearLayout) view.findViewById(R.id.ll_comment_normal);

        TextView tvLikeN = (TextView) likeN.findViewById(R.id.tv_like_normal);
        TextView tvHateN = (TextView) disLikeN.findViewById(R.id.tv_dislike_normal);
        TextView tvCommentN = (TextView) commentN.findViewById(R.id.tv_comment_normal);

        tvLikeN.setCompoundDrawablesWithIntrinsicBounds(post.myUp ? R.drawable.buttom_like_hover
                : R.drawable.buttom_like, 0, 0, 0);
        tvHateN.setCompoundDrawablesWithIntrinsicBounds(post.myDown ? R.drawable.buttom_hate_hover
                : R.drawable.buttom_hate, 0, 0, 0);
        tvCommentN.setCompoundDrawablesWithIntrinsicBounds(post.replyCount > 0 ?
                R.drawable.buttom_comment_hover : R.drawable.buttom_comment, 0, 0, 0);

        tvLikeN.setText(post.upCount > 0 ? String.valueOf(post.upCount) : "么么哒");
        tvHateN.setText(post.downCount > 0 ? String.valueOf(post.downCount) : "呵呵哒");
        tvCommentN.setText(post.replyCount > 0 ? String.valueOf(post.replyCount) : "评论");

        likeN.setTag(post);
        disLikeN.setTag(post);
        commentN.setTag(post);
        view.setOnClickListener(this);
        likeN.setOnClickListener(this);
        disLikeN.setOnClickListener(this);
        commentN.setOnClickListener(this);
    }


    private void updateView(View view, final CfPost post) {
        RichEmotionTextView tvDesc = (RichEmotionTextView) view.findViewById(R.id.tv_desc);
        if (!mIsNightMode) {
            tvDesc.setMovementMethod(MLLinkMovementMethod.getInstance());
            tvDesc.setText(post);
            tvDesc.setTopicSpanClickble(true);
            tvDesc.setTextClickble(true);
        } else {
            tvDesc.setText(post.content);
        }
        FlexibleImageView fivImages = (FlexibleImageView) view.findViewById(R.id.fiv_images);
        fivImages.setImages(post.images);
        View channelVote = view.findViewById(R.id.channel_vote);
        View textVote = view.findViewById(R.id.text_vote);
        View channelVoicePicture = view.findViewById(R.id.channel_voice_picture);
        View normalAction = view.findViewById(R.id.normal_action);
        MlVideoView videoView = (MlVideoView) view.findViewById(R.id.channel_video_view);
        VoiceView voiceView = (VoiceView) view.findViewById(R.id.voice_view);
        
        tvDesc.setVisibility(View.VISIBLE);
        fivImages.setVisibility(View.VISIBLE);
        channelVote.setVisibility(View.GONE);
        textVote.setVisibility(View.GONE);
        channelVoicePicture.setVisibility(View.GONE);
        normalAction.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);

        switch (post.threadType) {
            case CfPost.THREAD_TYPE_NORMAL:
            case CfPost.THREAD_TYPE_RICH_MEDIA:
                break;
            case CfPost.THREAD_TYPE_TXT_VOTE:
                VoteView voteView = (VoteView) textVote;
                voteView.setVisibility(View.VISIBLE);
                voteView.show(post);
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
            case CfPost.THREAD_TYPE_VOICE:
                voiceView.setVisibility(View.VISIBLE);
                voiceView.show(post);
                addPicVoiceView(voiceView);
                break;
            case CfPost.THREAD_TYPE_VIDEO:
                tvDesc.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.VISIBLE);
                videoView.show(post);
                videoView.setActivity(this);
                normalAction.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    private void addPicVoiceView(VoiceView voiceView) {
        if (voiceView != null && !mVoiceViews.contains(voiceView)) {
            mVoiceViews.add(voiceView);
        }
    }
    
    private class MyReplyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mMyReplyList.size();
        }

        @Override
        public CfReply getItem(int position) {
            return mMyReplyList.get(position);
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
            final CfReply reply = getItem(position);
            ProImageView avart = (ProImageView) view.findViewById(R.id.av_avatar);
            TextView name = (TextView) view.findViewById(R.id.tv_name);
            ImageView gender = (ImageView) view.findViewById(R.id.iv_gender);
            view.findViewById(R.id.tv_location).setVisibility(View.GONE);
            view.findViewById(R.id.iv_more).setVisibility(View.GONE);
            view.findViewById(R.id.rv_comments).setVisibility(View.GONE);
            TextView content = (TextView) view.findViewById(R.id.tv_desc);
            TextView replyContent = (TextView) view.findViewById(R.id.tv_reply_content);
            replyContent.setBackgroundColor(mIsNightMode ? 0x32000000 : 0xFFF6F6F6);
            replyContent.setTextColor(mIsNightMode ? 0XFF959595 : 0XFF2E2E2E);
            
            view.findViewById(R.id.iv_vip).setVisibility(reply.userInfo.vip && !mIsNightMode ?
                    View.VISIBLE : View.GONE);
            view.findViewById(R.id.iv_vip_bg).setVisibility(reply.userInfo.vip && !mIsNightMode ?
                    View.VISIBLE : View.GONE);

            
            view.setOnClickListener(ForumMyPostActivity.this);
            view.setTag(reply);

            replyContent.setVisibility(View.VISIBLE);
            content.setText(reply.content);
            Object obj = reply.getDirectTarget();
            CfReply targetReply = null;
            CfPost targetPost = null;
            if (obj instanceof CfPost) {
                targetPost = (CfPost) obj;
            } else if (obj instanceof CfReply) {
                targetReply = (CfReply) obj;
            }
            final UserInfo userInfo = reply.userInfo;
            avart.setImage(userInfo.getAvatar(), userInfo.defaultAvatar());
            avart.setTag(userInfo);
            avart.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!mIsNightMode) {
                        showProfileActivity(userInfo);
                    }
                }
            });
            name.setText(userInfo.getDisplayName(ForumMyPostActivity.this));
            gender.setImageResource(userInfo.gender == 0 ?
                    R.drawable.img_girl : R.drawable.img_boy);
            Utils.setText(view, R.id.tv_intro, reply.getTimeAndRelation());
            CharSequence cnt;
            String strReContent;
            if (targetPost != null) {
                cnt = "回复了原帖内容：";
                strReContent = targetPost.content;
            } else if (targetReply != null) {
                cnt = "回复了原帖评论：";
                strReContent = targetReply.content;
            } else {
                cnt = "该回复已删除！";
                strReContent = "";
            }
            cnt = Utils.getColoredText(
                    ForumMyPostActivity.this.getString(R.string.channel_remind_msg, cnt,
                            strReContent), cnt, mNameColor, false);
            replyContent.setText(cnt);
            replyContent.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mCurrentPost = reply.targetPost;
                    showResource();
                }
            });
            return view;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
            case R.id.tv_name:
            	if (mClickble && !mIsNightMode) {
            		mClickble = false;
            		showProfileActivity(((CfPost) (v.getTag())).userInfo);
            	}
                break;
            case R.id.ll_like_normal:
                mLastPost = mCurrentPost;
                mCurrentMills = System.currentTimeMillis();
                mCurrentPost = (CfPost) v.getTag();
                if (mLastPost == null) {
                    mIsSamePost = true;
                } else {
                    mIsSamePost = mCurrentPost.postId == mLastPost.postId;
                }
                if (mCurrentPost.myUp) {
                    return;
                }
                if (mCurrentPost.myDown) {
                    mCurrentPost.downCount--;
                }
                mCurrentPost.upCount++;
                mCurrentPost.myDown = false;
                mCurrentPost.myUp = true;
                updateClickedUI(mCurrentPost, v, true);
                MobclickAgent.onEvent(this, MStaticInterface.LIKE);
                break;

            case R.id.ll_dislike_normal:
                mLastPost = mCurrentPost;
                mCurrentMills = System.currentTimeMillis();
                mCurrentPost = (CfPost) v.getTag();
                if (mLastPost == null) {
                    mIsSamePost = true;
                } else {
                    mIsSamePost = mCurrentPost.postId == mLastPost.postId;
                }
                System.out.println("--- >hate -- " + mCurrentPost.postId + " -- " + mIsSamePost + mCurrentPost.upCount+"--"+mCurrentPost.downCount);
                if (mCurrentPost.myDown) {
                    return;
                }
                if (mCurrentPost.myUp) {
                    mCurrentPost.upCount--;
                }
                mCurrentPost.downCount++;
                mCurrentPost.myUp = false;
                mCurrentPost.myDown = true;
                updateClickedUI(mCurrentPost, v, false);
                MobclickAgent.onEvent(this, MStaticInterface.UNLIKE);
                break;
            case R.id.ll_share:
            case R.id.ll_share_night:
                share();
                break;
            case R.id.ll_comment_normal:
            case R.id.ll_channel_post:
                mCurrentPost = (CfPost) v.getTag();
                showForumPostDetail();
                break;
            case R.id.ll_channel_post_comment:
                mCurrentReply = (CfReply) v.getTag();
                mCurrentPost = mCurrentReply.targetPost;
                showMenuDialog();
                break;
            default:
                break;
        }
    }

    public void updateClickedUI(final CfPost post, final View view, final boolean like) {
        update(mCurrentPost);
        if (!mIsSamePost && mLastPost != null) {
            if (like) {
                mTaskManager.startForumLikeTask(mLastPost);
            } else {
                mTaskManager.startForumHateTask(mLastPost);
            }
        }
        view.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (System.currentTimeMillis() - mCurrentMills > 1000) {
                    if (like) {
                        mTaskManager.startForumLikeTask(post);
                    } else {
                        mTaskManager.startForumHateTask(post);
                    }
                }
            }
        }, 1000);
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this).setItems(R.array.myreply_item_menu, this);
        }
        mMenuDialog.show();
    }


    private void showProfileActivity (UserInfo mUserInfo) {
    	Intent intent = new Intent(ForumMyPostActivity.this, ProfileActivity.class);
        intent.putExtra(UserInfo.INTENT_EXTRA_INFO, mUserInfo);
        startActivity(intent);
    }

    private void showForumPostDetail() {
        Intent cpdIntent = new Intent(this, ForumPostDetailActivity.class);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCurrentPost);
        startActivity(cpdIntent);
    }

    private void share() {
        mSnsHelper.openShareBoard(this, mCurrentPost);
    }

    public void add(int index, CfPost post) {
        mPostAdapter.add(index, post);
    }

    public void remove(CfPost post) {
        mPostAdapter.remove(post);
    }

    public void remove(CfReply reply) {
        if (mMyReplyList.remove(reply)) {
            mReplyAdapter.notifyDataSetChanged();
        }
    }

    public void update(CfPost post) {
        mPostAdapter.update(post);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        getTitleBar().showProgress();
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        getTitleBar().hideProgress();
        switch (type) {
            case FORUM_DELETE_POST:
                if (response.value instanceof CfPost) {
                    remove((CfPost) response.value);
                }
                break;
            case FORUM_LIKE_POST:
            case FORUM_HATE_POST:
            case FORUM_COMMENT_POST:
                if (response.value instanceof CfPost) {
                    update((CfPost) response.value);
                }
                break;
            case FORUM_DELETE_REPLY:
                if (response.value instanceof CfReply) {
                    remove((CfReply) response.value);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
         switch (which) {
            case 0:
                showResource();
                break;
            case 1:
                delete();
                break;
            default:
                break;
        }
    }

    private void showResource() {
        if (mCurrentPost == null) {
            toast("该帖子已经被删除");
        } else {
        	showPostDetail();
        }
    }

    private void showPostDetail() {
        Intent intent = new Intent(this, ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCurrentPost);
        startActivity(intent);
    }

    private void delete() {
        mTaskManager.startForumDeleteTask(mCurrentReply);
    }

}
