package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Intent;
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
import com.tjut.mianliao.component.MLLinkMovementMethod;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.RichEmotionTextView;
import com.tjut.mianliao.component.nova.MlVideoView;
import com.tjut.mianliao.component.nova.PicVoteView;
import com.tjut.mianliao.component.nova.VoiceView;
import com.tjut.mianliao.component.nova.VoteView;
import com.tjut.mianliao.contact.SubscriptionHelper.SubRequestListener;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.NoContentClickListener;
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

public class CollectedPostActivity extends BaseActivity implements OnClickListener,
        OnRefreshListener2<ListView>, MsTaskListener, NoContentClickListener ,
        SubRequestListener {
    
    private MsTaskManager mTaskManager;
    private SnsHelper mSnsHelper;

    private PullToRefreshListView mPtrListView;
    private ArrayList<CfPost> mUserPostList;
//    private UserPostAdapter mPostAdapter;
    private ForumPostAdapter mPostAdapter;
    private CfPost mCurrentPost, mLastPost;
    private boolean mIsNightMode;
    
    private ArrayList<VoiceView> mVoiceViews;
    private TextView mTvDesc;
    
    private long mCurrentMills;
    private boolean mIsSamePost;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_user_post;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        mVoiceViews = new ArrayList<>();
        getTitleBar().setTitle("我的收藏");
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        mUserPostList = new ArrayList<CfPost>();
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_user_posts);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        mPostAdapter = new ForumPostAdapter(this);
        mPostAdapter.setActivity(this);
        mPostAdapter.showOtherSchool();
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickbel(true);
        mPostAdapter.setOnNoContentListener(this);
        mPtrListView.setAdapter(mPostAdapter);
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
        checkDayNightUI();
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            mPtrListView.setBackgroundResource(R.drawable.bg);
        } else {
            mPtrListView.setBackgroundColor(0XFFF2F2F2);
        }
    }

    @Override
    public void onPause() {
        mPostAdapter.stopVoicePlay();
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTaskManager.unregisterListener(this);
        for (VoiceView voiceView : mVoiceViews) {
            voiceView.onDestroy();
        }
    }

    private void fetchPost(boolean refresh) {
        new MyCollectedPostTask(refresh).executeLong();
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.av_avatar:
            case R.id.tv_name:
                showProfileActivity(((CfPost) (v.getTag())).userInfo);
                break;
            case R.id.ll_like_active:
            case R.id.ll_like_normal:
            case R.id.ll_like_night:
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

            case R.id.ll_dislike_active:
            case R.id.ll_dislike_normal:
            case R.id.ll_dislike_night:
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
            case R.id.ll_comment_active:
            case R.id.ll_comment_vote:
            case R.id.ll_channel_post:
                mCurrentPost = (CfPost) v.getTag();
                if (hasDetail(mCurrentPost)) {
                    showForumPostDetail();
                }
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
    
    private void showProfileActivity(UserInfo userInfo) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        startActivity(intent);
    }

    private boolean hasDetail(CfPost post) {
        if (post.threadType == CfPost.THREAD_TYPE_NORMAL
                || post.threadType == CfPost.THREAD_TYPE_TXT_VOTE
                || post.threadType == CfPost.THREAD_TYPE_PIC_VOTE
                || post.threadType == CfPost.THREAD_TYPE_VIDEO
                ||  post.threadType == CfPost.THREAD_TYPE_RICH_MEDIA) {
            return true;
        }
        return false;
    }

    private void share() {
        mSnsHelper.openShareBoard(this, mCurrentPost);
    }

    private void showForumPostDetail() {
        Intent cpdIntent = new Intent(this, ForumPostDetailActivity.class);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCurrentPost);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_CHANNEL_INFO, new ChannelInfo());
        startActivity(cpdIntent);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPost(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPost(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }

    private class MyCollectedPostTask extends MsTask {
        private int mOffset;
        private boolean refresh;

        public MyCollectedPostTask(boolean refresh) {
            super(CollectedPostActivity.this, MsRequest.CF_COLLECT_THREAD_LISTS);
            this.refresh = refresh;
            mOffset = refresh ? 0 : mUserPostList.size();
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja = response.getJsonArray();
                ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                if (refresh) {
                    mPostAdapter.reset(posts);
                } else {
                    mPostAdapter.addAll(posts);
                }
            }
        }
    }

    private class UserPostAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mUserPostList.size();
        }

        @Override
        public CfPost getItem(int position) {
            return mUserPostList.get(position);
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
            image.setOnClickListener(CollectedPostActivity.this);

            TextView name = (TextView) view.findViewById(R.id.tv_name);
            name.setText(post.userInfo.getDisplayName(CollectedPostActivity.this));
            name.setTag(post);
            name.setOnClickListener(CollectedPostActivity.this);

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
        voiceView.setVisibility(View.GONE);        

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
                break;
            case CfPost.THREAD_TYPE_PIC_TXT:
            default:
                break;
        }
    }
    
    private void addPicVoiceView(VoiceView voiceView) {
        if (voiceView != null && !mVoiceViews.contains(voiceView)) {
            mVoiceViews.add(voiceView);
        }
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
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
            case FORUM_COLLECT_POST:
                if (response.value instanceof CfPost) {
                    CfPost post = (CfPost) response.value;
                    if (!post.collected) {
                        remove(post);
                    }
                }
                break;
            default:
                break;
        }
    }


    public void add(int index, CfPost post) {
        mPostAdapter.add(index, post);
    }


    public void remove(CfPost post) {
        mPostAdapter.remove(post);
    }

    public void update(CfPost post) {
       mPostAdapter.update(post);
    }

    @Override
    public void onPreExecute(MsTaskType type) {
        
    }

    @Override
    public void onSubscribe(boolean success) {
        mTvDesc.setEnabled(true);
        if (success) {
            mTvDesc.setOnClickListener(null);
            mTvDesc.setText(getString(R.string.cf_add_friend_request_succ));
        } else {
            toast(R.string.adc_request_sent_failed);
        }
    }

    @Override
    public void onUnsubscribe(boolean success) {}

    @Override
    public void onNoContentClick() {
        fetchPost(true);
    }
}
