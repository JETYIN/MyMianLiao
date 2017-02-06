package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

import android.R.bool;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
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
import com.tjut.mianliao.component.nova.PictureVoiceView;
import com.tjut.mianliao.component.nova.VoiceView;
import com.tjut.mianliao.component.nova.VoteView;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.tribe.TribeDetailActivity;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class ForumPostAdapter extends BaseAdapter implements MsTaskListener, OnClickListener,
        DialogInterface.OnClickListener, OnCompletionListener {

    private static final String TAG = "ForumPostAdapter";
    
    protected HashMap<View, CfPost> mViewMaps;
    protected ArrayList<CfPost> mPosts;
    protected CfPost mCurrentPost, mLastPost;
    protected Activity mContext;
    protected LayoutInflater mInflater;
    protected MsTaskManager mTaskManager;
    protected UserInfoManager mUserInfoManager;
    protected SnsHelper mSnsHelper;
    protected LightDialog mMenuDialog;
    protected ArrayList<PictureVoiceView> mVoiceViews;
    protected ArrayList<VoiceView> mPostVoiceViews;
    protected Settings mSettings;
    protected AbsListView listView = null;
    protected boolean mIsSamePost;
    protected boolean mShowOtherSchoolTag;
    public boolean mIsNightMode;
    protected boolean isPermitLoad = true;
    protected boolean mHasNewPost;
    protected boolean mTextClickble, mSpanClickble;
    protected boolean mIsFromTribe;
    protected boolean mIsShowTribe;
    
    protected String mForumName;
    protected int mHotPostCount;

    protected long mCurrentMills;
    protected int lastVisibleItem = 0;
    protected long lastScrollTime = 0;
    protected static int mVoicePlayingPostPosition = -1;
    
    protected boolean isShowSchool = false;
    protected boolean mHasData;
    protected boolean mShowNoContentView = true;
    protected NoContentClickListener mListener;
    protected OnNativeScrollListener mScrollListener;
    
    protected View mViewNoContent;

    public ForumPostAdapter(Activity context) {
        mContext = context;
        mPosts = new ArrayList<CfPost>();
        mInflater = LayoutInflater.from(context);
        mTaskManager = MsTaskManager.getInstance(context);
        mUserInfoManager = UserInfoManager.getInstance(context);
        mSnsHelper = SnsHelper.getInstance();
        mTaskManager.registerListener(this);
        mVoiceViews = new ArrayList<PictureVoiceView>();
        mPostVoiceViews = new ArrayList<>();
        mSettings = Settings.getInstance(context);
        mViewMaps = new HashMap<View, CfPost>();
    }
    
    public ForumPostAdapter(Activity context, boolean isShowSchool) {
        this(context);
        this.isShowSchool = isShowSchool;
    }
    
    public void setIsTribePosts(boolean isTribe) {
        mIsFromTribe = isTribe;
    }
    
    public void setIsShowTribeIndetail(boolean isShow) {
        mIsShowTribe = isShow;
    }

    public ForumPostAdapter setShowNoContent(boolean show) {
        mShowNoContentView = show;
        return this;
    }

    public void addAll(ArrayList<CfPost> posts) {
        mPosts.addAll(posts);
        notifyDataSetChanged();
    }
    
    public boolean hasData() {
        return mHasData;
    }

    public void add(int index, CfPost post) {
        mPosts.add(index, post);
        notifyDataSetChanged();
    }
    
    public void setTextClickble(boolean clickble) {
        mTextClickble = clickble;
    }
    
    public void setSpanClickbel(boolean clickble) {
        mSpanClickble = clickble;
    }
    
    public void setForumName(String forumName) {
        mForumName = forumName;
    }
    
    public void setActivity(Activity activity) {
        mContext = activity;
    }

    public boolean hasNewPost() {
        return mHasNewPost;
    }

    public void setVoicePlayingPostPosition(int position) {
        mVoicePlayingPostPosition = position;
    }

    
    /**
     * call this method to set mHasNewPost to false
     */
    public void resetHasPostStatus() {
        mHasNewPost = false;
    }
    
    public void configListView() {
        listView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        isPermitLoad = true;
                        lastScrollTime = 0;
                        lastVisibleItem = 0;
                        notifyDataSetChanged();
                        if (mScrollListener != null) {
                        	mScrollListener.onScrollStateChanged(view, scrollState);
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (lastVisibleItem != firstVisibleItem) {
                    long nowTime = System.currentTimeMillis();
                    double speed = Math.abs(firstVisibleItem - lastVisibleItem) / ((nowTime - lastScrollTime) / 1000f);
                    lastScrollTime = nowTime;
                    lastVisibleItem = firstVisibleItem;
                    if (speed > 5) {
                        isPermitLoad = false;
                    } else {
                        isPermitLoad = true;
                    }
                    if (mScrollListener != null) {
                    	mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                    }
                    if (mVoicePlayingPostPosition < firstVisibleItem ||
                            mVoicePlayingPostPosition > firstVisibleItem + visibleItemCount) {
                        stopVoicePlay();
                    }
                }
            }
        });

    }

    public void destroy() {
        mSnsHelper.closeShareBoard();
        mTaskManager.unregisterListener(this);
        stopVoicePlay();
    }
    
    public void stopVoicePlay() {
        for (PictureVoiceView voiceView : mVoiceViews) {
            voiceView.onDestroy();
        }
        for (VoiceView voiceView : mPostVoiceViews) {
        	voiceView.onDestroy();
        }
    }

    public void remove(CfPost post) {
        if (mPosts.remove(post)) {
            notifyDataSetChanged();
        }
        for (PictureVoiceView voiceView : mVoiceViews) {
            voiceView.destroyView(post);
        }
        for (VoiceView voiceView : mPostVoiceViews) {
        	voiceView.destroyView(post);
        }
    }

    public void reset(ArrayList<CfPost> posts) {
        mPosts.clear();
        getHotPostCount(posts);
        addAll(posts);
    }
    

    private void getHotPostCount(ArrayList<CfPost> posts) {
        if (posts == null || posts.size() == 0) {
            return;
        }
        mHotPostCount = 0;
        for (CfPost post : posts) {
            if (post.hot == 1) {
                mHotPostCount++;
            }
        }
    }

    public void showOtherSchool() {
        mShowOtherSchoolTag = true;
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

    public void update(CfPost post) {
        int index = mPosts.indexOf(post);
        if (index != -1) {
            mPosts.remove(index);
            mPosts.add(index, post);
            notifyDataSetChanged();
        }
        notifyDataSetChanged();
    }
    
    @Override
    public int getCount() {
        int count;
        if (mPosts.size() == 0) {
            mHasData = false;
            count = mShowNoContentView ? 1 : 0;
        } else {
            mHasData = true;
            count = mPosts.size();
        }
        return count;
    }

    @Override
    public CfPost getItem(int position) {
        if (mHasData) {
            return mPosts.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return PostType.TYPE_COUNT;
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    @Override
    public int getItemViewType(int position) {
        if (!mHasData) {
            return -1;
        }
        CfPost post = mPosts.get(position);
        return getPostType(post);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        long beginTime = System.currentTimeMillis();
        if (listView == null) {
            listView = (AbsListView) parent;
            configListView();
        }
        if (!mHasData) {
            mViewNoContent = mInflater.inflate(R.layout.view_no_content, parent, false);
            mViewNoContent.findViewById(R.id.fl_no_content).setOnClickListener(this);
            resetNoContentView();
            return mViewNoContent;
        }
        CfPost post = getItem(position);
        int viewType = getItemViewType(position);
        View view;
        if (convertView != null) {
            view = convertView;
            if (mViewMaps.get(view) == post && !isPermitLoad) {
                Utils.logD(TAG, "mama " + isPermitLoad);
                return view;
            }
        } else {
            view = inflateView(parent, viewType);
        }

        if (view == null) {
            return null;
        }

        mViewMaps.put(view, post);
        mIsNightMode = mSettings.isNightMode();
        view.setOnClickListener(this);
        view.setTag(post);
        switch (viewType) {
            case PostType.NORMAL:
            case PostType.RICH_MEDIA:
            case PostType.DAY_CHANNEL_ACTIVE:
                updateNormalPostView(view, post);
                break;
            case PostType.TXT_VOTE:
                updateTxtVotePostView(view, post);
                break;
            case PostType.NIGHT_TXT:
                updateNightTxtView(view, post);
                break;
            case PostType.NIGHT_PIC_TXT:
                updatePicTxtView(view, post);
                break;
            case PostType.VIDEO_THREAD:
                updateVideoView(view, post);
                break;
            case PostType.NIGHT_PIC_VOICE:
            case PostType.VOICE_POST:
            	updateVoicePostView(view, post);
            	break;
            default:

                break;
        }
        updateFooterView(view, post);
        updateHeaderView(view, post, position);
        Utils.logD(TAG, "Time" + (System.currentTimeMillis() - beginTime));
        return view;
    }
    
    public View updateHeaderView(View view, CfPost post, int position) {
        FrameLayout mFlHeader = (FrameLayout) view.findViewById(R.id.fl_header_content);
        if (post.isTribePicked) {
            if (mFlHeader.getChildCount() > 0) {
                mFlHeader.removeViewAt(0);
            }
            mInflater.inflate(R.layout.item_essential_post_header, mFlHeader);
        } else {
            if (mFlHeader.getChildCount() > 0) {
                mFlHeader.removeViewAt(0);
            }
        }
        return view;
    }

    public View updateFooterView(View view, CfPost post){
        return view;
    }

    @Override
    public void onPreExecute(MsTaskType type) {

    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_PUBLISH_POST:
                if (response.value instanceof CfPost) {
                    CfPost post = ((CfPost) response.value);
                    if (post.forumName.equals(mForumName)) {
                        mHasNewPost = true;
                        add(mHotPostCount, post);
                    }
                }
                break;
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
            case FORUM_COMMENT_REPLY:
            case FORUM_DELETE_REPLY:
                if (response.value instanceof CfReply) {
                    update(((CfReply) response.value).targetPost);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_more:
                showMenuDialog((CfPost) v.getTag());
                break;
            case R.id.av_avatar:
            case R.id.tv_name:
                if (mIsNightMode) {
                    Toast.makeText(mContext, "你是揭不开我的面具滴！", Toast.LENGTH_SHORT).show();
                    return;
                }
                showProfileActivity(((CfPost) (v.getTag())).userInfo);
                MobclickAgent.onEvent(mContext, MStaticInterface.FACE);
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
                MobclickAgent.onEvent(mContext, MStaticInterface.LIKE);
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
                MobclickAgent.onEvent(mContext, MStaticInterface.UNLIKE);
                break;
            case R.id.ll_share:
            case R.id.ll_share_night:
                mCurrentPost = (CfPost) v.getTag();
                MobclickAgent.onEvent(mContext, MStaticInterface.SHARE);
                share();
                break;
            case R.id.ll_comment_normal:
            case R.id.ll_comment_active:
            case R.id.ll_comment_vote:
            case R.id.ll_channel_post:
                mCurrentPost = (CfPost) v.getTag();
                showForumPostDetail();
                MobclickAgent.onEvent(mContext, MStaticInterface.DISCUSS);
                break;
            case R.id.fl_no_content:
                // update view
                updateNoContentView();
                if (mListener != null) {
                    mListener.onNoContentClick();
                }
                break;
            default:
                break;
        }
    }
    
    private void resetNoContentView() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.VISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.GONE);
    }

    private void updateNoContentView() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.INVISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mMenuDialog) {
            switch (which) {
                case 0:
                    share();
                    MobclickAgent.onEvent(mContext, MStaticInterface.SHARE);
                    break;
                case 1:
                    mTaskManager.startForumPostCollectTask(mCurrentPost);
                    break;
                case 2:
                    copyToClipboard();
                    MobclickAgent.onEvent(mContext, MStaticInterface.DUPLICATE_CONTENT);
                    break;
                case 3:
                    if (mCurrentPost.userInfo.isMine(mContext)) {
                        mTaskManager.startForumDeleteTask(mCurrentPost);
                    } else {
                        report();
                        MobclickAgent.onEvent(mContext, MStaticInterface.REPORT);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }

    private void addPicVoiceView(PictureVoiceView voiceView) {
        if (voiceView != null && !mVoiceViews.contains(voiceView)) {
            mVoiceViews.add(voiceView);
        }
    }
    
    private void addPostVoiceView(VoiceView voiceView) {
    	if (mPostVoiceViews != null && !mPostVoiceViews.contains(voiceView)) {
    		mPostVoiceViews.add(voiceView);
    	}
    }

    private void showMenuDialog(CfPost post) {
        mCurrentPost = post;
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(mContext);
            mMenuDialog.setTitle(R.string.please_choose);
        }
        if (post.userInfo.isMine(mContext)) {
            mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_uncollect :
                R.array.channel_post_menu_mine_collect, this);
        } else {
            mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_uncollect :
                R.array.channel_post_menu_collect, this);
        }
        mMenuDialog.show();
    }

    private void showForumPostDetail() {
        Intent cpdIntent = new Intent(mContext, ForumPostDetailActivity.class);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCurrentPost);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_SHOW_OTHER_SCHOOL_TAG, mShowOtherSchoolTag);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_ISSHOW_SCHOOL_TOPIC, isShowSchool);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_IS_FORM_TRIBE, mIsFromTribe);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_IS_SHOW_TRIBE, mIsShowTribe);
        mContext.startActivity(cpdIntent);
    }

    private void share() {
        mSnsHelper.openShareBoard(mContext, mCurrentPost);
    }

    private void showProfileActivity(UserInfo userInfo) {
        Intent iProfile = new Intent(mContext, ProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        mContext.startActivity(iProfile);
    }

    private int getPostType(CfPost post) {
        switch (post.threadType) {
            case CfPost.THREAD_TYPE_NORMAL:
                return PostType.NORMAL;
            case CfPost.THREAD_TYPE_TXT_VOTE:
                return PostType.TXT_VOTE;
            case CfPost.THREAD_TYPE_TXT:
                return PostType.NIGHT_TXT;
            case CfPost.THREAD_TYPE_PIC_TXT:
                return PostType.NIGHT_PIC_TXT;
            case CfPost.THREAD_TYPE_PIC_VOICE:
                return PostType.NIGHT_PIC_VOICE;
            case CfPost.THREAD_TYPE_RICH_MEDIA:
                return PostType.RICH_MEDIA;
            case CfPost.THREAD_TYPE_VIDEO:
                return PostType.VIDEO_THREAD;
            case CfPost.THREAD_TYPE_VOICE:
                return PostType.VOICE_POST;
            default:
                return PostType.NORMAL;
        }
    }

    public View inflateView(ViewGroup parent, int type) {
        switch (type) {
            case PostType.NORMAL:
            case PostType.RICH_MEDIA:
            case PostType.DAY_CHANNEL_ACTIVE:
            	if (!mIsNightMode) {
                    return mInflater.inflate(R.layout.list_item_post_normal, parent, false);
                }
                return mInflater.inflate(R.layout.list_item_post_normal_night, parent, false);
            case PostType.TXT_VOTE:
            	if (mIsNightMode) {
            		return mInflater.inflate(R.layout.list_item_post_txt_vote_night, parent, false);
            	} else {
            		return mInflater.inflate(R.layout.list_item_post_txt_vote, parent, false);
            	}
            case PostType.VIDEO_THREAD:
                if (mIsNightMode) {
                    return mInflater.inflate(R.layout.list_item_post_video_night, parent, false);
                }
                return mInflater.inflate(R.layout.list_item_post_video, parent, false);
            case PostType.VOICE_POST:
            case PostType.NIGHT_PIC_VOICE:
            	if (!mIsNightMode) {
                    return mInflater.inflate(R.layout.list_item_voice_post_normal, parent, false);
                }
                return mInflater.inflate(R.layout.list_item_voice_post_normal_night, parent, false);
            case PostType.NIGHT_TXT:
                return mInflater.inflate(R.layout.list_item_post_night_txt, parent, false);
            case PostType.NIGHT_PIC_TXT:
                return mInflater.inflate(R.layout.list_item_post_pic_txt, parent, false);
            default:
                return mInflater.inflate(R.layout.list_item_post_normal, parent, false);
        }
    }

    private void updateVoicePostView(View view, CfPost post) {
        updateNormalPostView(view, post);
        updateVoiceView(view, post);
    }

    private void updateNormalPostView(View view, CfPost post) {
        updateTopView(view, post, true);
        updateTextView(view, post);
        updateImageView(view, post);
        updateNormalBottomView(view, post);
    }

    private void updateTxtVotePostView(View view, CfPost post) {
        updateTopView(view, post, true);
        updateTextView(view, post);
        updateTxtVoteView(view, post);
        updateNormalBottomView(view, post);
    }

    private void updateNightTxtView(View view, CfPost post) {
        updateTopView(view, post, false);
        updateTextView(view, post);
        updateNormalBottomView(view, post);
    }

    private void updatePicTxtView(View view, CfPost post) {
        updateTopView(view, post, false);
        updateTextView(view, post);
        updateImageView(view, post);
        updateNormalBottomView(view, post);
    }

    private void updatePictureVoiceView(View view, CfPost post) {
        updateTopView(view, post, false);
        updatePicVoiceView(view, post);
        updateNormalBottomView(view, post);
    }

    private void updateVideoView(View view, CfPost post) {
        updateTopView(view, post, false);
        updateTextView(view, post);
        updateVideoInfo(view, post);
        updateNormalBottomView(view, post);
    }
    
    private void updateVideoInfo(View view, CfPost post) {
        MlVideoView mlVideoView = (MlVideoView) view.findViewById(R.id.sv_video);
        mlVideoView.show(post);
        mlVideoView.setActivity(mContext);
    }
    
    
    private void updateTextView(View view, CfPost post) {
        RichEmotionTextView tvContent = (RichEmotionTextView) view.findViewById(R.id.tv_desc);
        if (post.content == null || "".equals(post.content)) {
            tvContent.setVisibility(View.GONE);
            return;
        } 
        if (!mIsNightMode) {
            tvContent.setMovementMethod(MLLinkMovementMethod.getInstance());
            tvContent.setText(post);
            tvContent.setTextClickble(mTextClickble);
            tvContent.setTopicSpanClickble(mSpanClickble);
            tvContent.setAtSpanClickble(true);
        } else {
           tvContent.setText(post.content); 
        }
        tvContent.setVisibility(View.VISIBLE);
    }

    private void updateImageView(View view, CfPost post) {
        FlexibleImageView fivImages = (FlexibleImageView) view.findViewById(R.id.fiv_images);
        fivImages.setPermitLoad(isPermitLoad);
        fivImages.setImages(post.images);
    }

    private void updatePicVoteView(View view, CfPost post) {
        PicVoteView vote = (PicVoteView) view.findViewById(R.id.channel_vote);
        vote.show(post);
    }

    private void updateTxtVoteView(View view, CfPost post) {
        VoteView voteView = (VoteView) view.findViewById(R.id.text_vote);
        voteView.show(post);
    }

    private void updateVoiceView(View view, CfPost post) {
        VoiceView voiceView = (VoiceView) view.findViewById(R.id.voice_view);
        voiceView.show(post);
        voiceView.setPostPosition(mPosts.indexOf(post));
        voiceView.setPostAdapter(this);
        addPostVoiceView(voiceView);
    }

    private void updateNormalBottomView(View view, CfPost post) {
        if (!isPermitLoad) {
            return;
        }
        View viewLike = view.findViewById(R.id.ll_like_normal);
        View viewDislike = view.findViewById(R.id.ll_dislike_normal);
        View viewComment = view.findViewById(R.id.ll_comment_normal);

        TextView tvLike = (TextView) viewLike.findViewById(R.id.tv_like_normal);
        TextView tvDislike = (TextView) viewDislike.findViewById(R.id.tv_dislike_normal);
        TextView tvComment = (TextView) viewComment.findViewById(R.id.tv_comment_normal);

        if (post.myUp) {
            tvLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_like_hover, 0, 0, 0);
            tvDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_hate, 0, 0, 0);
        } else if (post.myDown) {
            tvLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_like, 0, 0, 0);
            tvDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_hate_hover, 0, 0, 0);
        } else {
            tvLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_like, 0, 0, 0);
            tvDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_hate, 0, 0, 0);
        }
        tvComment.setCompoundDrawablesWithIntrinsicBounds(R.drawable.buttom_comment, 0, 0, 0);

        tvLike.setText(post.upCount > 0 ? String.valueOf(post.upCount) : "么么哒");
        tvDislike.setText(post.downCount > 0 ? String.valueOf(post.downCount) : "呵呵哒");
        tvComment.setText(post.replyCount > 0 ? String.valueOf(post.replyCount) : "评论");

        viewLike.setTag(post);
        viewDislike.setTag(post);
        viewComment.setTag(post);

        viewLike.setOnClickListener(this);
        viewDislike.setOnClickListener(this);
        viewComment.setOnClickListener(this);
    }

    private void updateTopView(View view, CfPost post, boolean showDistance) {
        AvatarView image = (AvatarView) view.findViewById(R.id.av_avatar);
        TextView name = (TextView) view.findViewById(R.id.tv_name);
        ImageView ivGender = (ImageView) view.findViewById(R.id.iv_gender);
        ProImageView ivMedal = (ProImageView) view.findViewById(R.id.iv_medal);
        if (!isPermitLoad) {
        	Picasso.with(mContext)
		    	.load(post.userInfo.getAvatar())
		    	.placeholder(post.userInfo.defaultAvatar())
		    	.into(image);
            name.setText(post.userInfo.getDisplayName(mContext));
            Picasso.with(mContext)
                .load(post.userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy)
                .into(ivGender);
            return;
        }

        if (post.userInfo != null && post.userInfo.jid != null) {
            mUserInfoManager.acquireUserInfo(getUserJid(post.userInfo));
        }
        ImageView ivMore = (ImageView) view.findViewById(R.id.iv_more);
        TextView tvIntro = (TextView) view.findViewById(R.id.tv_intro);
        TextView tvDistance = (TextView) view.findViewById(R.id.tv_distance);
        TextView tvScholl = (TextView) view.findViewById(R.id.tv_location);
//        ImageView ivHotLogo = (ImageView) view.findViewById(R.id.iv_logo_hot);
        ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
        ImageView ivMasterSection = (ImageView) view.findViewById(R.id.iv_master_section);
        
        tvScholl.setVisibility(mShowOtherSchoolTag || Utils.isMianLiaoService(post.userInfo) ?
                View.GONE : View.VISIBLE);
        View viewVip = view.findViewById(R.id.iv_vip);
        View viewVipBg = view.findViewById(R.id.iv_vip_bg);
//        if (viewVip != null) {
//            viewVip.setVisibility(
//                    post.userInfo.vip && !mIsNightMode ? View.VISIBLE : View.GONE);
//        }
        if (viewVipBg != null) {
            viewVipBg.setVisibility(
                    post.userInfo.vip && !mIsNightMode ? View.VISIBLE : View.INVISIBLE);
        }
//        if (ivHotLogo != null) {
//            ivHotLogo.setVisibility(post.hot == 1 ? View.VISIBLE : View.GONE);
//        }
        int typeIcon = post.userInfo.getTypeIcon();
        if (ivTypeIcon != null && !mIsNightMode) {
            if (typeIcon > 0){
                ivTypeIcon.setImageResource(typeIcon);
                ivTypeIcon.setVisibility(View.VISIBLE);
            } else {
                ivTypeIcon.setVisibility(View.GONE);
            }
        }

        // show master section icon
        if (ivMasterSection != null) {
            int resIcon = getMasterSectionIcon(post);
            if (resIcon > 0) {
                ivMasterSection.setImageResource(resIcon);
                ivMasterSection.setVisibility(View.VISIBLE);
            } else {
                ivMasterSection.setVisibility(View.GONE);
            }
        }
        
        Picasso.with(mContext)
	    	.load(post.userInfo.getAvatar())
	    	.placeholder(post.userInfo.defaultAvatar())
	    	.into(image);
        name.setText(post.userInfo.getDisplayName(mContext));
        tvIntro.setText(mShowOtherSchoolTag && !Utils.isMianLiaoService(post.userInfo) ?
                post.getTimeAndSchool(post) : Utils.getPostShowTimeString(post.createdOn));
        Picasso.with(mContext)
            .load(post.userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy)
            .into(ivGender);
        if (isShowSchool) {
            tvScholl.setVisibility(View.VISIBLE);
            tvIntro.setText(Utils.getPostShowTimeString(post.createdOn));
        }
        tvScholl.setText(post.userInfo.school);
        if (tvDistance != null) {
        	tvDistance.setText(post.getDistanceAndRelation());
        	if (showDistance) {
        		checkDistanceDayNightUI(tvDistance);
        	}
        }

        if (ivMedal != null) {
            if (!mIsNightMode && post.userInfo.getLatestBadge() != null &&
                    post.userInfo.getLatestBadge().startsWith("http")) {
                ivMedal.setVisibility(View.VISIBLE);
                Picasso.with(mContext)
                    .load(post.userInfo.getLatestBadge())
                    .placeholder(R.drawable.ic_medal_empty)
                    .into(ivMedal);
            } else {
                ivMedal.setVisibility(View.GONE);
            }
        }
        ivMore.setOnClickListener(this);
        ivMore.setTag(post);
        image.setTag(post);
        image.setOnClickListener(this);
        name.setTag(post);
        name.setOnClickListener(this);

    }

    private int getMasterSectionIcon(CfPost post) {
        return 0;
    }

    private void checkDistanceDayNightUI(View llDistance) {
        llDistance.setVisibility(mIsNightMode ? View.VISIBLE : View.GONE);
    }

    private void updatePicVoiceView(View view, CfPost post) {
        PictureVoiceView voiceView = (PictureVoiceView) view.findViewById(R.id.channel_voice_picture);
        voiceView.show(post);
        addPicVoiceView(voiceView);
    }

    private String getUserJid(UserInfo userInfo) {
        if (!"".equals(userInfo.account)) {
            int indexOf = userInfo.jid.indexOf("@");
            if (indexOf < 3) {
                return userInfo.account + userInfo.jid;
            } else {
                return userInfo.jid;
            }
        }
        return userInfo.jid;
    }

    private void copyToClipboard() {
        String mCopyContent = exchangeAt(mCurrentPost.content);
        Utils.copyToClipboard(mContext, mCopyContent, mCopyContent);
        Toast.makeText(mContext, mContext.getString(R.string.clip_board_clipped), Toast.LENGTH_SHORT).show();
    }

    private void report() {
        PoliceHelper.reportForumPost(mContext, mCurrentPost.postId);
    }

    public class PostType {

        public static final int TYPE_COUNT = 10;

        public static final int NORMAL = 0;
        public static final int TXT_VOTE = 1;
        public static final int DAY_CHANNEL_ACTIVE = 2;
//        public static final int DAY_PIC_VOTE = 3;
        public static final int NIGHT_TXT = 4;
        public static final int NIGHT_PIC_TXT = 5;
        public static final int NIGHT_PIC_VOICE = 6;
        public static final int RICH_MEDIA = 7;
        public static final int VIDEO_THREAD = 8;
        public static final int VOICE_POST = 9;
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

    public interface NoContentClickListener {
        void onNoContentClick();
    }
    
    public void setOnNoContentListener(NoContentClickListener listener) {
        mListener = listener;
    }
    
    public interface OnNativeScrollListener{
    	void onScrollStateChanged(AbsListView view, int scrollState);
    	void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                int totalItemCount);
    }
    
    public void setOnNativeScrollListener(OnNativeScrollListener listener) {
    	mScrollListener = listener;
    }

	@Override
	public void onCompletion(MediaPlayer mp) {
		
	}
    
}