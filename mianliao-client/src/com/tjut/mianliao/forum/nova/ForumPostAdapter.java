package com.tjut.mianliao.forum.nova;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.FlexibleImageView;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.MLLinkMovementMethod;
import com.tjut.mianliao.component.RichEmotionTextView;
import com.tjut.mianliao.component.nova.MlVideoView;
import com.tjut.mianliao.component.nova.VoiceView;
import com.tjut.mianliao.component.nova.VoteView;
import com.tjut.mianliao.contact.FollowUserManager;
import com.tjut.mianliao.contact.FollowUserManager.OnUserFollowListener;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.FocusTribe;
import com.tjut.mianliao.data.FocusUser;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.CfReply;
import com.tjut.mianliao.profile.FriendDynamicsActivity;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.tribe.TribeDetailActivity;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.PoliceHelper;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.video.JCVideoPlayerStandard;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class ForumPostAdapter extends BaseAdapter implements MsTaskListener, OnClickListener,
        DialogInterface.OnClickListener, OnCompletionListener, OnUserFollowListener {

    private static final String TAG = "ForumPostAdapter";
    
    private static final int TYPE_RECOMMEND = -2;
    
    protected ArrayList<Integer> mRecPersIndexs;
    protected ArrayList<Integer> mRecTribeIndexs;
    protected HashMap<View, CfPost> mViewMaps;
    protected ArrayList<FocusUser> mRecommendPersons;
    protected ArrayList<FocusTribe> mRecommendTribes;
    protected ArrayList<CfPost> mPosts;
    protected CfPost mCurrentPost, mLastPost;
    protected Activity mContext;
    protected LayoutInflater mInflater;
    protected MsTaskManager mTaskManager;
    protected UserInfoManager mUserInfoManager;
    protected FollowUserManager mFollowUserManager;
    protected SnsHelper mSnsHelper;
    protected LightDialog mMenuDialog;
    protected UserInfo mUserInfo;
    protected ArrayList<VoiceView> mPostVoiceViews;
    protected Settings mSettings;
    protected AbsListView listView = null;
    protected boolean mIsSamePost;
    protected boolean mShowOtherSchoolTag;
    protected boolean isPermitLoad = true;
    protected boolean mHasNewPost;
    protected boolean mTextClickble, mSpanClickble;
    protected boolean mIsFromTribe;
    protected boolean mIsShowTribe;
    protected boolean mIsShowSchoolName;
    
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

    private int mListViewFirstItem = 0;
    private int mScreenY = 0;
    protected int mVideoPlayPosition = -1;
    protected FocusTribe mFocusTribe;
    protected FocusUser mFocusUser;
    protected ArrayList<JCVideoPlayerStandard> mVideoPlayers;
    protected LightDialog mNoticeDialog;

    public ForumPostAdapter(Activity context) {
        mContext = context;
        mPosts = new ArrayList<CfPost>();
        mRecommendPersons = new ArrayList<>();
        mRecommendTribes = new ArrayList<>();
        mRecPersIndexs = new ArrayList<>();
        mRecTribeIndexs = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
        mTaskManager = MsTaskManager.getInstance(context);
        mUserInfoManager = UserInfoManager.getInstance(context);
        mFollowUserManager = FollowUserManager.getInstance(context);
        mFollowUserManager.registerOnUserFollowListener(this);
        mSnsHelper = SnsHelper.getInstance();
        mTaskManager.registerListener(this);
        mPostVoiceViews = new ArrayList<>();
        mSettings = Settings.getInstance(context);
        mViewMaps = new HashMap<View, CfPost>();
        mUserInfo = AccountInfo.getInstance(context).getUserInfo();
        mVideoPlayers = new ArrayList<>();
    }
    
    public ForumPostAdapter(Activity context, boolean isShowSchool) {
        this(context);
        this.isShowSchool = isShowSchool;
    }
    
    public void setIsTribePosts(boolean isTribe) {
        mIsFromTribe = isTribe;
    }
    
    public void setIsShowSchoolName(boolean isShow) {
        mIsShowSchoolName = isShow;
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
    
    public void addRecommedUsers(ArrayList<Map<Integer, FocusUser>> focusUsers) {
        if (focusUsers == null || focusUsers.size() == 0) {
            return;
        }
        mRecPersIndexs = new ArrayList<>();
        mRecommendPersons.clear();
        for (int i=0;i<focusUsers.size();i++) {
            Map<Integer, FocusUser> user = focusUsers.get(i);
            Set<Integer> keySet = user.keySet();
            Iterator<Integer> iterator = keySet.iterator();
            Integer index = iterator.next();
            FocusUser focusUser = user.get(index);
            mRecPersIndexs.add(index);
            mRecommendPersons.add(focusUser);
        }
    }
    
    public void addRecommedTribes(ArrayList<Map<Integer, FocusTribe>> focusTribe) {
        if (focusTribe == null || focusTribe.size() == 0) {
            return;
        }
        mRecTribeIndexs = new ArrayList<>();
        mRecommendTribes.clear();
        for (int i=0;i<focusTribe.size();i++) {
            Map<Integer, FocusTribe> user = focusTribe.get(i);
            Set<Integer> keySet = user.keySet();
            Iterator<Integer> iterator = keySet.iterator();
            Integer index = iterator.next();
            FocusTribe tribeInfo = user.get(index);
            mRecTribeIndexs.add(index);
            mRecommendTribes.add(tribeInfo);
        }
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
    
    public void setSpanClickble(boolean clickble) {
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
    
    public void setVideoPlayingPosition(int pos) {
        this.mVideoPlayPosition = pos;
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
                if (mScrollListener != null) {
                    mScrollListener.onScrollStateChanged(view, scrollState);
                }
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        isPermitLoad = true;
                        lastScrollTime = 0;
                        lastVisibleItem = 0;
                        notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mScrollListener != null) {
                    mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                }
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
//                    if (mVoicePlayingPostPosition < firstVisibleItem ||
//                            mVoicePlayingPostPosition > firstVisibleItem + visibleItemCount) {
//                        stopVoicePlay();
//                    }
                    int[] location = new int[2];
                    if (firstVisibleItem != mListViewFirstItem) {
                        if (firstVisibleItem > mListViewFirstItem) {
                            Utils.logD(TAG, "向上滑动" + " ---- " + firstVisibleItem + " ---- " + mVideoPlayPosition);
//                            if (mVideoPlayPosition < firstVisibleItem - visibleItemCount) {
//                                releaseVideo();
//                            }
                            if (mVoicePlayingPostPosition < firstVisibleItem - visibleItemCount) {
                                stopVoicePlay();
                            }
                        } else {
                            Utils.logD(TAG, "向下滑动" + " ---- " + firstVisibleItem + " ---- " + mVideoPlayPosition);
//                            if (mVideoPlayPosition > firstVisibleItem + visibleItemCount) {
//                                releaseVideo();
//                            }
                            if (mVoicePlayingPostPosition > firstVisibleItem + visibleItemCount) {
                                stopVoicePlay();
                            }
                        }
                        mListViewFirstItem = firstVisibleItem;
                        mScreenY = location[1];
                    } else {
                        if (mScreenY > location[1]) {
                            Utils.logD(TAG, "向上滑动" + " ---- " + firstVisibleItem + " ---- " + mVideoPlayPosition);
//                            if (mVideoPlayPosition < firstVisibleItem - visibleItemCount) {
//                                releaseVideo();
//                            }
                            if (mVoicePlayingPostPosition < firstVisibleItem - visibleItemCount) {
                                stopVoicePlay();
                            }
                        } else if (mScreenY < location[1]) {
                            Utils.logD(TAG, "向下滑动" + " ---- " + firstVisibleItem + " ---- " + mVideoPlayPosition);
//                            if (mVideoPlayPosition > firstVisibleItem + visibleItemCount) {
//                                releaseVideo();
//                            }
                            if (mVoicePlayingPostPosition > firstVisibleItem + visibleItemCount) {
                                stopVoicePlay();
                            }
                        }
                        mScreenY = location[1];
                    }
                }
            }
        });

    }

    public void destroy() {
//        mSnsHelper.closeShareBoard();
        mTaskManager.unregisterListener(this);
        mFollowUserManager.unregisterOnUserFollowListener(this);
        stopVoicePlay();
    }
    
    public void stopVoicePlay() {
        for (VoiceView voiceView : mPostVoiceViews) {
        	voiceView.onDestroy();
        }
    }

    public void remove(CfPost post) {
        if (mPosts.remove(post)) {
            notifyDataSetChanged();
        }
        for (VoiceView voiceView : mPostVoiceViews) {
        	voiceView.destroyView(post);
        }
    }

    public void reset(ArrayList<CfPost> posts) {
        mPosts.clear();
//        getHotPostCount(posts);
        addAll(posts);
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
    
    protected boolean hasRecommendData() {
        return mRecommendPersons.size() > 0 || mRecommendTribes.size() > 0;
    }
    
    protected int getShowingRecommendCount(int position) {
        int count = 0;
        if (mRecPersIndexs.size() >= mRecTribeIndexs.size()) {
            for (Integer index : mRecPersIndexs) {
                if (index <= position) {
                    count++;
                }
            }
        } else {
            for (Integer index : mRecTribeIndexs) {
                if (index <= position) {
                    count++;
                }
            }
        }
        return count;
    }
    
    public CfPost getLastPostInfo() {
        return mPosts.get(mPosts.size() - 1);
    }
    
    /**
     * Call this method to got the real posts count
     * @return the real posts count
     */
    public int getPostCount() {
        return mPosts.size();
    }
    
    
    @Override
    public int getCount() {
        int count = 0;
        if (mPosts.size() == 0) {
            mHasData = false;
            count = mShowNoContentView ? 1 : 0;
        } else if (hasRecommendData()) {
            mHasData = true;
            count += mPosts.size();
            count += Math.max(mRecommendPersons.size(), mRecommendTribes.size());
        } else {
            mHasData = true;
            count = mPosts.size();
        }
        return count;
    }

    @Override
    public CfPost getItem(int position) {
        if (mHasData) {
            if (hasRecommendData()) {
                int index = mRecPersIndexs.indexOf(position);
                index = index == -1 ? mRecommendTribes.indexOf(position) : index;
                if (index > getCount()) {
                    index = - 1;
                }
                int i = position + getShowingRecommendCount(position);
                boolean hasPost = i <= mPosts.size() - 1;
                return index == -1 ? (hasPost ? mPosts.get(i) : null) : null; 
            } else {
                return mPosts.get(position);
            }
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
        return PostType.TYPE_COUNT + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (!mHasData) {
            return -1;
        } else if (hasRecommendData()) {
            int index = mRecPersIndexs.indexOf(position);
            index = index == -1 ? mRecommendTribes.indexOf(position) : index;
            if (index > getCount()) {
                index = - 1;
            }
            int postPos = position - getShowingRecommendCount(position);
            boolean hasPost = postPos <= mPosts.size() - 1;
            return index == -1 ? (hasPost ? getPostType(mPosts.get(postPos)) : TYPE_RECOMMEND) : TYPE_RECOMMEND; 
        } else {
            CfPost post = mPosts.get(position);
            return getPostType(post);
        }
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
                Utils.logD(TAG, "is permit load ? " + isPermitLoad);
                return view;
            }
        } else {
            view = inflateView(parent, viewType);
        }

        if (view == null) {
            return null;
        }
        mViewMaps.put(view, post);
        if (viewType == TYPE_RECOMMEND) {
            boolean hasView = updateRecommendView(view, position);
            view.setVisibility(hasView ? View.VISIBLE : View.GONE);
            return view;
        }
        
        view.setOnClickListener(this);
        view.setTag(post);
        if (post != null && post.userInfo != null) {
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
        }
        updateFooterView(view, post);
        updateHeaderView(view, post, position);
        Utils.logD(TAG, "Time" + (System.currentTimeMillis() - beginTime));
        return view;
    }
    
    private boolean updateRecommendView(View view, int position) {
        LinearLayout llRecUser = (LinearLayout) view.findViewById(R.id.ll_recommed_user);
        LinearLayout llRecTribe = (LinearLayout) view.findViewById(R.id.ll_recommed_tribe);
        TextView tvFollowUserDesc = (TextView) view.findViewById(R.id.tv_follow_user_desc);
        TextView tvFollowTribeDesc = (TextView) view.findViewById(R.id.tv_follow_tribe_desc);
        ImageView ivUserAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
        ImageView ivTribeIcon = (ImageView) view.findViewById(R.id.iv_tribe_icon);
        TextView tvUserName = (TextView) view.findViewById(R.id.tv_name);
        TextView tvTribeName = (TextView) view.findViewById(R.id.tv_tribe_name);
        ImageView ivGender = (ImageView) view.findViewById(R.id.iv_gender);
        TextView tvSchool = (TextView) view.findViewById(R.id.tv_school);
        TextView tvTribeBase = (TextView) view.findViewById(R.id.tv_tribe_base);
        TextView tvFollowUser = (TextView) view.findViewById(R.id.tv_follow_user);
        TextView tvFollowTribe = (TextView) view.findViewById(R.id.tv_follow_tribe);
        TextView tvRefresh = (TextView) view.findViewById(R.id.tv_refresh_recommend);
        tvRefresh.setOnClickListener(this);
        int indexUser = mRecPersIndexs.indexOf(position);
        int indexTribe = mRecTribeIndexs.indexOf(position);
        FocusUser focusUser = indexUser == -1 ? null : mRecommendPersons.get(0);
        FocusTribe tribeInfo = indexTribe == -1 ? null : mRecommendTribes.get(0);
        if (focusUser == null && tribeInfo == null) {
            view.setVisibility(View.GONE);
            return false;
        }
        if (focusUser == null) {
            llRecUser.setVisibility(View.GONE);
        } else {
            llRecUser.setVisibility(View.VISIBLE);
            Picasso.with(mContext)
                .load(focusUser.avatar)
                .placeholder(focusUser.defaultAvatar())
                .into(ivUserAvatar);
            tvUserName.setText(focusUser.nick);
            ivGender.setImageResource(focusUser.getGenderIcon());
            tvSchool.setText(focusUser.school);
            tvFollowUser.setOnClickListener(this);
            tvFollowUser.setTag(focusUser);
            String name = focusUser.ruNames.size() > 0 ? focusUser.ruNames.get(0) : "";
            tvFollowUserDesc.setText(mContext.getString(R.string.tribe_follow_user_desc,
                    name, focusUser.ruCount));
            if (focusUser.isFollow) {
                tvFollowTribe.setText(R.string.tribe_is_followed);
                tvFollowTribe.setTextColor(0XFF45F7E0);
                tvFollowTribe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.channel_see, 0, 0, 0);
                tvFollowTribe.setCompoundDrawablePadding(mContext.getResources().getDimensionPixelSize(R.dimen.tribe_rad_men_follow_padding));
                tvFollowTribe.setBackgroundResource(R.drawable.bg_tv_green_circle);
            } else {
                tvFollowTribe.setText(R.string.tribe_collected_add);
                tvFollowTribe.setBackgroundResource(R.drawable.bg_tv_rad_circle);
                tvFollowTribe.setTextColor(0XFFFF9393);
                tvFollowTribe.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            ivUserAvatar.setOnClickListener(this);
            ivUserAvatar.setTag(focusUser);
        }
        if (tribeInfo == null) {
            llRecTribe.setVisibility(View.GONE);
        } else {
            llRecTribe.setVisibility(View.VISIBLE);
            Picasso.with(mContext)
                .load(tribeInfo.icon)
                .into(ivTribeIcon);
            tvTribeName.setText(tribeInfo.tribeName);
            tvTribeBase.setText(mContext.getString(R.string.tribe_follow_post_count_desc,
                    tribeInfo.postCount, tribeInfo.followCount));
            tvFollowTribe.setOnClickListener(this);
            tvFollowTribe.setTag(tribeInfo);
            String name = tribeInfo.rfNames.size() > 0 ? tribeInfo.rfNames.get(0) : "";
            tvFollowTribeDesc.setText(mContext.getString(R.string.tribe_follow_tribe_desc,
                    name, tribeInfo.rfCount));
            if (tribeInfo.isFollowed) {
                tvFollowTribe.setText(R.string.tribe_is_followed);
                tvFollowTribe.setTextColor(0XFF45F7E0);
                tvFollowTribe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.channel_see, 0, 0, 0);
                tvFollowTribe.setCompoundDrawablePadding(mContext.getResources().getDimensionPixelSize(R.dimen.tribe_rad_men_follow_padding));
                tvFollowTribe.setBackgroundResource(R.drawable.bg_tv_green_circle);
            } else {
                tvFollowTribe.setText(R.string.tribe_collected_add);
                tvFollowTribe.setBackgroundResource(R.drawable.bg_tv_rad_circle);
                tvFollowTribe.setTextColor(0XFFFF9393);
                tvFollowTribe.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
            ivTribeIcon.setOnClickListener(this);
            ivTribeIcon.setTag(tribeInfo);
        }
        return true;
    }

    public View updateHeaderView(View view, CfPost post, int position) {
        FrameLayout mFlHeader = (FrameLayout) view.findViewById(R.id.fl_header_content);
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
            case FORUM_DELETE_POST_V4:
                if (response.value instanceof CfPost) {
                    remove((CfPost) response.value);
                }
                break;
            case FORUM_LIKE_POST:
            case FORUM_HATE_POST:
            case FORUM_COMMENT_POST:
            case FORUM_STICK_POST:
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
            case R.id.tv_tribe_name:
                CfPost post = (CfPost) v.getTag();
                Intent trIntent = new Intent(mContext, TribeDetailActivity.class);
                trIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, post.tribeId);
                mContext.startActivity(trIntent);
                break;
            case R.id.tv_follow_tribe:
                mFocusTribe = (FocusTribe) v.getTag();
                followTribe(mFocusTribe.id);
                break;
            case R.id.tv_follow_user:
                mFocusUser = (FocusUser) v.getTag();
                mFollowUserManager.follow(mFocusUser.id);
                break;
            case R.id.iv_avatar:
                FocusUser focusUser = (FocusUser) v.getTag();
                Intent proIntent = new Intent(mContext, NewProfileActivity.class);
                UserInfo user = new UserInfo();
                user.userId = focusUser.id;
                proIntent.putExtra(UserInfo.INTENT_EXTRA_INFO, user);
                mContext.startActivity(proIntent);
                break;
            case R.id.iv_tribe_icon:
                FocusTribe tribe = (FocusTribe) v.getTag();
                Intent tribeIntent = new Intent(mContext, TribeDetailActivity.class);
                tribeIntent.putExtra(TribeDetailActivity.EXT_DATA_ID, tribe.id);
                mContext.startActivity(tribeIntent);
                break;
            case R.id.tv_refresh_recommend:
                new GetFocusContent().executeLong();
                break;
            default:
                break;
        }
    }
    
    private void followTribe(int id) {
        new FollowTribeTask(id).executeLong();
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
                    if (mCurrentPost.isUserAreSuperModerator() || Utils.isMianLiaoService(mCurrentPost.userInfo)) {
                        if (mCurrentPost.userInfo.isMine(mContext)) {
                            showDelNoticeDialog();
                        } else {
                            report();
                            MobclickAgent.onEvent(mContext, MStaticInterface.REPORT);
                        }
                    } else if (mCurrentPost.isUserAreModerator()) {
                        if (mCurrentPost.userInfo.isMine(mContext) 
                                || mCurrentPost.isUserAreSuperModerator(mUserInfo.userId) 
                                || Utils.isMianLiaoService(mUserInfo)) {
                            showDelNoticeDialog();
                        } else {
                            report();
                            MobclickAgent.onEvent(mContext, MStaticInterface.REPORT);
                        }
                    } else {
                        if (mCurrentPost.isModerator(mUserInfo.userId) 
                                || mCurrentPost.userInfo.isMine(mContext) 
                                || Utils.isMianLiaoService(mUserInfo) 
                                || mCurrentPost.isUserAreSuperModerator(mUserInfo.userId)) {
                            showDelNoticeDialog();
                        } else {
                            report();
                            MobclickAgent.onEvent(mContext, MStaticInterface.REPORT);
                        }
                    }
                    break;
                case 4:
                    // 置顶
                    mTaskManager.startForumStickTaskV4(mCurrentPost);
                    break;
                default:
                    break;
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
    }
    
    private void addPostVoiceView(VoiceView voiceView) {
    	if (mPostVoiceViews != null && !mPostVoiceViews.contains(voiceView)) {
    		mPostVoiceViews.add(voiceView);
    	}
    }
    
    private void addVidePlayer(JCVideoPlayerStandard video) {
        if (mVideoPlayers != null && !mVideoPlayers.contains(video)) {
            mVideoPlayers.add(video);
        }
    }
    
    private void releaseVideo() {
        for (JCVideoPlayerStandard video : mVideoPlayers) {
            video.releaseAllVideos();
        }
    }

    private void showMenuDialog(CfPost post) {
        mCurrentPost = post;
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(mContext);
            mMenuDialog.setTitle(R.string.please_choose);
        }
        if (Utils.isMianLiaoService(post.userInfo) || post.isUserAreSuperModerator()) {
            if (post.userInfo.isMine(mContext)){
                mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_mine_uncollect :
                    R.array.channel_post_menu_mine_collect, this);
            } else {
                mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_uncollect :
                    R.array.channel_post_menu_collect, this);
            }
        } else if (Utils.isMianLiaoService(mUserInfo) || post.isUserAreSuperModerator(mUserInfo.userId)) {
            if (post.isStickLvl()) {
                mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_uncollect_moderator_stick
                        : R.array.channel_post_menu_mine_collect_moderator_stick, this);
            } else {
                mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_uncollect_moderator
                        : R.array.channel_post_menu_mine_collect_moderator, this);
            }
        } else {
            if (post.isModerator(mUserInfo.userId) && !post.isUserAreSuperModerator()) {
                if (post.isStickLvl()) {
                    mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_uncollect_moderator_stick
                            : R.array.channel_post_menu_mine_collect_moderator_stick, this);
                } else {
                    mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_uncollect_moderator
                            : R.array.channel_post_menu_mine_collect_moderator, this);
                }
            } else {
                if (post.userInfo.isMine(mContext)){
                    mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_mine_uncollect :
                        R.array.channel_post_menu_mine_collect, this);
                } else {
                    mMenuDialog.setItems(post.collected ? R.array.channel_post_menu_uncollect :
                        R.array.channel_post_menu_collect, this);
                }
            }
        }
        mMenuDialog.show();
    }

    private void showForumPostDetail() {
        Intent cpdIntent = new Intent(mContext, ForumPostDetailActivity.class);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, mCurrentPost);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_SHOW_OTHER_SCHOOL_TAG, mShowOtherSchoolTag);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_ISSHOW_SCHOOL_TOPIC, isShowSchool);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_ISSHOW_SCHOOL_NAME, mIsShowSchoolName);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_IS_FORM_TRIBE, mIsFromTribe);
        cpdIntent.putExtra(ForumPostDetailActivity.EXTRL_IS_SHOW_TRIBE, mIsShowTribe);
        mContext.startActivity(cpdIntent);
    }

    private void share() {
        mSnsHelper.openShareBoard(mContext, mCurrentPost);
    }

    private void showProfileActivity(UserInfo userInfo) {
        Intent iProfile = new Intent(mContext, NewProfileActivity.class);
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
                return mInflater.inflate(R.layout.list_item_post_normal, parent, false);
            case PostType.TXT_VOTE:
        		return mInflater.inflate(R.layout.list_item_post_txt_vote, parent, false);
            case PostType.VIDEO_THREAD:
                return mInflater.inflate(R.layout.list_item_post_video, parent, false);
            case PostType.VOICE_POST:
            case PostType.NIGHT_PIC_VOICE:
                return mInflater.inflate(R.layout.list_item_voice_post_normal, parent, false);
            case PostType.NIGHT_TXT:
                return mInflater.inflate(R.layout.list_item_post_night_txt, parent, false);
            case PostType.NIGHT_PIC_TXT:
                return mInflater.inflate(R.layout.list_item_post_pic_txt, parent, false);
            case TYPE_RECOMMEND:
                return mInflater.inflate(R.layout.list_item_recommend, parent, false);
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
//        JCVideoPlayerStandard jcVideoPlayerStandard = (JCVideoPlayerStandard) view.findViewById(R.id.videoplayer);
//        jcVideoPlayerStandard.setUp(post.videoUrl, "");
//        jcVideoPlayerStandard.setAdapter(this);
//        jcVideoPlayerStandard.setDataSourcePosition(mPosts.indexOf(post));
//        if (TextUtils.isEmpty(post.videoThumbnail)) {
//            Picasso.with(mContext)
//                .load(R.drawable.bg_default_big_day)
//                .into(jcVideoPlayerStandard.ivThumb);
//        } else {
//            Picasso.with(mContext)
//                .load(getSmallImage(post.videoThumbnail))
//                .placeholder(R.drawable.bg_default_big_day)
//                .into(jcVideoPlayerStandard.ivThumb);
//        }
//        addVidePlayer(jcVideoPlayerStandard);
    }
    
    private String getSmallImage(String url) {
        if (url.contains("tjt-post.oss-cn-hangzhou.aliyuncs.com")) {
            return url;
        }
        return AliImgSpec.POST_PHOTO.makeUrlSingleImg(url);
    }
    
    private void updateTextView(View view, CfPost post) {
        RichEmotionTextView tvContent = (RichEmotionTextView) view.findViewById(R.id.tv_desc);
        if (post.content == null || "".equals(post.content)) {
            tvContent.setVisibility(View.GONE);
            return;
        } 
        tvContent.setMovementMethod(MLLinkMovementMethod.getInstance());
        tvContent.setText(post);
        tvContent.setTextClickble(mTextClickble);
        tvContent.setTopicSpanClickble(mSpanClickble);
        tvContent.setAtSpanClickble(true);
        tvContent.setIsShowOtherSchoolTag(mShowOtherSchoolTag);
        tvContent.setIsShowSchool(isShowSchool);
        tvContent.setIsShowSchoolName(mIsShowSchoolName);
        tvContent.setIsShowTribeIndetail(mIsShowTribe);
        tvContent.setIsTribePosts(mIsFromTribe);
        tvContent.setVisibility(View.VISIBLE);
    }

    private void updateImageView(View view, CfPost post) {
        FlexibleImageView fivImages = (FlexibleImageView) view.findViewById(R.id.fiv_images);
        fivImages.setPermitLoad(isPermitLoad);
        fivImages.setImages(post.images);
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
         ImageView ivMedal = (ImageView) view.findViewById(R.id.iv_medal);
         if (!isPermitLoad ) {
             if (post.userInfo.getAvatar() != null && "".equals(post.userInfo.getAvatar()) && image != null) {
                 Picasso.with(mContext)
                 .load(post.userInfo.getAvatar())
                 .placeholder(post.userInfo.defaultAvatar())
                 .into(image);
             }
             name.setText(post.userInfo.getDisplayName(mContext));
             Picasso.with(mContext)
             .load(post.userInfo.gender == 0 ?
                     R.drawable.img_girl : R.drawable.img_boy)
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
         ImageView ivTypeIcon = (ImageView) view.findViewById(R.id.iv_type_icon);
         ImageView ivModerator = (ImageView) view.findViewById(R.id.iv_moderator);
         ImageView mIvHotPost = (ImageView) view.findViewById(R.id.iv_hot_post);
         mIvHotPost.setVisibility(post.isHotPost ? View.VISIBLE : View.GONE);
         tvScholl.setVisibility(mShowOtherSchoolTag || Utils.isMianLiaoService(post.userInfo) ?
                 View.GONE : View.VISIBLE);
         View viewVipBg = view.findViewById(R.id.iv_vip_bg);
         if (viewVipBg != null && post != null && post.userInfo != null) {
             viewVipBg.setVisibility(
                     post.userInfo.vip ? View.VISIBLE : View.INVISIBLE);
         }
         int typeIcon = post.userInfo.getTypeIcon();
         if (ivTypeIcon != null) {
             if (typeIcon > 0){
                 ivTypeIcon.setImageResource(typeIcon);
                 ivTypeIcon.setVisibility(View.VISIBLE);
             } else {
                 ivTypeIcon.setVisibility(View.GONE);
             }
         }
         int moderatorIdcon = getModeratorIdcon(post);
         if (moderatorIdcon > 0) {
             Picasso.with(mContext).load(moderatorIdcon).into(ivModerator);
             ivModerator.setVisibility(View.VISIBLE);
         } else {
             ivModerator.setVisibility(View.GONE);
         }
         
         Picasso.with(mContext)
             .load(post.userInfo.getAvatar())
             .placeholder(post.userInfo.defaultAvatar())
             .into(image);
         name.setText(post.userInfo.getDisplayName(mContext));
         name.setTextColor(getNameColor(post));
         Picasso.with(mContext)
             .load(post.userInfo.gender == 0 ? R.drawable.img_girl : R.drawable.img_boy)
             .into(ivGender);
         tvScholl.setVisibility(View.VISIBLE);
         tvIntro.setText(Utils.getPostShowTimeString(post.replyTime));
         tvScholl.setText(post.userInfo.school);
         if (tvDistance != null) {
             tvDistance.setText(post.getDistanceAndRelation());
             if (showDistance) {
                 checkDistanceDayNightUI(tvDistance);
             }
         }
         
         if (!post.isUserAreSuperModerator()) {
             if (ivMedal != null) {
                 if (post.userInfo.getLatestBadge() != null &&
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
         } else {
             ivMedal.setVisibility(View.GONE);
         }
         ivMore.setOnClickListener(this);
         ivMore.setTag(post);
         image.setTag(post);
         image.setOnClickListener(this);
         name.setTag(post);
         name.setOnClickListener(this);
    }

    private int getModeratorIdcon(CfPost post) {
        if (post.isUserAreSuperModerator()) {
            return R.drawable.super_master;
        } else if(post.isUserAreModerator()) {
            return R.drawable.icon_muster_section;
        }
        return 0;
    }

    private int getNameColor(CfPost post) {
        if (post.isUserAreSuperModerator()) {
            return Constant.COLOR_SUPER_MODERATOR;
        } else if(post.isUserAreModerator()) {
            return Constant.COLOR_MODERATOR;
        }
        return Constant.COLOR_NORMAL;
    }

    private void checkDistanceDayNightUI(View llDistance) {
        llDistance.setVisibility(View.GONE);
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
    
    private class FollowTribeTask extends MsTask {

        private int mId;

        public FollowTribeTask(int id) {
            super(mContext, MsRequest.TRIBE_FOLLOW_WITH);
            mId = id;
        }

        @Override
        protected String buildParams() {
            return "tribe_id=" + mId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mFocusTribe.isFollowed = true;
                if (mFocusUser != null) {
                    mFocusTribe.isFollowed = true;
                }
                notifyDataSetChanged();
            } else {
                Toast.makeText(mContext, "关注部落失败", Toast.LENGTH_SHORT).show();
            }
        }
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

    @Override
    public void onFollowSuccess() {
        if (mFocusUser != null) {
            mFocusUser.isFollow = true;
        }
        notifyDataSetChanged();
    }

    @Override
    public void onFollowFail() {
//        Toast.makeText(mContext, "关注用户失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancleFollowSuccess() {}

    @Override
    public void onCancleFollowFail() {}

    @Override
    public void onGetFollowListSuccess(MsResponse response, int offset) { }

    @Override
    public void onGetFollowListFail() {}


    private class GetFocusContent extends MsTask {
        public GetFocusContent () {
            super(mContext, MsRequest.LIST_RECOMMEND_USER_TRIBE);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();
                ArrayList<FocusUser> focusUsers = JsonUtil.getArray(
                        json.optJSONArray("follow_user"), FocusUser.TRANSFORMER);
                ArrayList<FocusTribe> tribeInfos = JsonUtil.getArray(
                        json.optJSONArray("follow_tribe"), FocusTribe.TRANSFORMER);
                addRecommedTribes(getRecommendTribeInfo(tribeInfos));
                addRecommedUsers(getRecommendUserInfo(focusUsers));
                notifyDataSetChanged();
            }
        }
    }

    private ArrayList<Map<Integer, FocusUser>> getRecommendUserInfo(ArrayList<FocusUser> focusUsers) {
        if (focusUsers == null) {
            return null;
        }
        ArrayList<Map<Integer, FocusUser>> recUsers = new ArrayList<>();
        for (int i = 0; i < focusUsers.size(); i++) {
            Map<Integer, FocusUser> userMap = new HashMap<>();
            userMap.put(2 * i + 1, focusUsers.get(i));
            recUsers.add(userMap);
        }
        return recUsers;
    }

    private ArrayList<Map<Integer, FocusTribe>> getRecommendTribeInfo(ArrayList<FocusTribe> tribeInfos) {
        if (tribeInfos == null) {
            return null;
        }
        ArrayList<Map<Integer, FocusTribe>> recTribes = new ArrayList<>();
        for (int i = 0; i < tribeInfos.size(); i++) {
            Map<Integer, FocusTribe> tribeMap = new HashMap<>();
            tribeMap.put(2 * i + 1, tribeInfos.get(i));
            recTribes.add(tribeMap);
        }
        return recTribes;
    }

    private void showDelNoticeDialog () {
        if (mNoticeDialog == null) {
            mNoticeDialog = new LightDialog(mContext);
            mNoticeDialog.setTitle(R.string.confirm);
            mNoticeDialog.setMessage(R.string.cf_delete_post_confirm);
            mNoticeDialog.setNegativeButton(R.string.you_sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mTaskManager.startForumDeleteTaskV4(mCurrentPost);
                }
            });
            mNoticeDialog.setPositiveButton(R.string.search_cancel, null);
        }
        mNoticeDialog.show();
    }
}