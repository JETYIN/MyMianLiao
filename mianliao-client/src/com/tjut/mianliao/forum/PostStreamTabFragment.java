package com.tjut.mianliao.forum;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.duanqu.qupai.sdk.android.QupaiManager;
import com.duanqu.qupai.sdk.android.QupaiService;
import com.duanqu.qupai.utils.AppGlobalSetting;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.BaseTask.TaskType;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.SearchActivity;
import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.common.RecordResult;
import com.tjut.mianliao.common.RequestCode;
import com.tjut.mianliao.component.ArcMenu;
import com.tjut.mianliao.component.ArcMenu.onMenuItemClickListener;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.News;
import com.tjut.mianliao.data.cache.CacheImageInfo;
import com.tjut.mianliao.data.cache.CachePostInfo;
import com.tjut.mianliao.data.cache.CacheVoteInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.NoContentClickListener;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.OnNativeScrollListener;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.forum.nova.ForumVideoPostActivity;
import com.tjut.mianliao.forum.nova.NormalPostActivity;
import com.tjut.mianliao.forum.nova.TxtVotePostActivity;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.news.NewsActivity;
import com.tjut.mianliao.news.NewsManager;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.NewsGuidDialog;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.video.JCResizeSurfaceView;
import com.tjut.mianliao.video.JCVideoPlayer;
import com.umeng.analytics.MobclickAgent;

public class PostStreamTabFragment extends TabFragment implements OnClickListener,
        OnRefreshListener2<ListView>, NoContentClickListener, MsTaskListener,
        OnNativeScrollListener, TaskExecutionListener {

    private static final int MAX_HOT_POST_COUNT = 3;
    private static final int TYPE_CAMPUS = 1;
    private static final int TYPE_ROAM = 2;

    private static final int UPDATE_POST = 6;

    public static final String SP_IS_FIRST_ROAM = "sp_roam_view";

    private View mViewArcBg;
    private View mViewPtrContent;
    private PullToRefreshListView mPtrListView;
    private View mHeaderView;
    private SharedPreferences mPreferences;
    private NewsGuidDialog mGuidDialog;
    private LinearLayout mLlTop5Post;
    private TextView mTvNewsTitle;
    private ImageView mIvTicketFlag;
    private FrameLayout mFlContent;
    private JCResizeSurfaceView mSurfaceView;

    private ForumPostAdapter mAdapter;
    private TabController mSubTabController;
    private CommonBanner mVsSwitcher;
    private MsTaskManager mTaskManager;
    private NewsManager mNewsManager;

    private RoamCollegeView mRoamColleages;

    private TextTab mTtSquare, mTtRoam;
    private ArrayList<CfPost> mStickLvlPosts;

    private int mSchoolId;
    private int mChooseType = TYPE_CAMPUS;

    private String mForumName;
    private UserInfo userInfo;
    protected boolean mShowBanner = true;
    private boolean mIsRefreshing;
    private boolean mIsArcMenuShow;
    private int mFreshTime;

    private ArcMenu mArcMenu;
    private LinearLayout mLlHotPosts;

    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mCount;
    private long mRefreshTime;
    private Handler mFreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_POST:
                    MainActivity.showRefreshRed(0, true);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.main_tab_post_stream;
    }

    @Override
    public int getNaviButtonId() {
        return R.id.nb_forum;
    }

    @Override
    public String getName() {
        return "PostStreamTabFragment";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JCVideoPlayer.isVideoFinish = true;
        mStickLvlPosts = new ArrayList<>();
        mTaskManager = MsTaskManager.getInstance(getActivity());
        mTaskManager.registerListener(this);

        mNewsManager = NewsManager.getInstance(getActivity());
        mNewsManager.registerTaskListener(this);

        mAdapter = new ForumPostAdapter(getActivity());
        mAdapter.setActivity(getActivity());
        mAdapter.showOtherSchool();
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickble(true);
        mAdapter.setOnNoContentListener(this);

        mPreferences = DataHelper.getSpForData(getActivity());
        AccountInfo accountInfo = AccountInfo.getInstance(getActivity());
        userInfo = accountInfo.getUserInfo();
        userInfo = userInfo == null ? accountInfo.loadUserInfoFromSp() : userInfo;
        mSchoolId = userInfo.schoolId;
        mForumName = userInfo.school;
        mFreshTime = Constant.getFreshTimeDelay(getActivity());
    }

    private void startCount() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                refreshCount();
            }

            private void refreshCount() {
                new GetRefreshPostCount().executeLong();
            }
        };

        mTimer.schedule(mTimerTask, mFreshTime, mFreshTime);
    }

    public void getStickLvlPostCount(ArrayList<CfPost> posts) {
        mStickLvlPosts.clear();
        for (CfPost post : posts) {
            if (post.isStickLvl()) {
                mStickLvlPosts.add(post);
            }
        }
    }

    protected void loadPosts() {
        ArrayList<CfPost> posts = DataHelper.loadPostsInfoBySchoolId(getActivity(), mSchoolId);
        if (posts != null && posts.size() > 0) {
            getStickLvlPostCount(posts);
            mAdapter.reset(posts);
            fillStickLvlPost();
        }
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

    private void showGuidDialog() {
        boolean isFirst = mPreferences.getBoolean(SP_IS_FIRST_ROAM, false);
        if (isFirst) {
            mGuidDialog = new NewsGuidDialog(getActivity(), R.style.Translucent_NoTitle);
            mGuidDialog.showGuidImage(getGuidImageRes(), SP_IS_FIRST_ROAM);
        }
    }

    private int[] getGuidImageRes() {
        int[] imgRes = {};
        return imgRes;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mViewPtrContent = view.findViewById(R.id.fl_ptr_content);
        mArcMenu = (ArcMenu) view.findViewById(R.id.id_arcmenu);
        mViewArcBg = view.findViewById(R.id.bg_view);
        mRoamColleages = (RoamCollegeView) view.findViewById(R.id.roaming_colleages);

        mFlContent = (FrameLayout) view.findViewById(R.id.fl_post_stream);
        mSurfaceView = (JCResizeSurfaceView) view.findViewById(R.id.surfaceView);
        mPtrListView = (PullToRefreshListView) view.findViewById(R.id.ptrlv_post_stream);
        ListView lvList = mPtrListView.getRefreshableView();

        if (mShowBanner) {
            mHeaderView = inflater.inflate(R.layout.campus_head_view, lvList, false);
            lvList.addHeaderView(mHeaderView, null, false);
            mVsSwitcher = (CommonBanner) mHeaderView.findViewById(R.id.vs_switcher);
            mLlHotPosts = (LinearLayout) mHeaderView.findViewById(R.id.ll_hot_posts);
            mHeaderView.findViewById(R.id.ll_news_info).setOnClickListener(this);
            // mHeaderView.findViewById(R.id.rl_more_hot).setOnClickListener(this);
            mTvNewsTitle = (TextView) mHeaderView.findViewById(R.id.tv_news_title);
            mIvTicketFlag = (ImageView) mHeaderView.findViewById(R.id.iv_ticket_flag);
            mLlTop5Post = (LinearLayout) mHeaderView.findViewById(R.id.ll_top_five);
        }

        // campus
        mAdapter.setForumName(mForumName);
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);

        setTitle();

        mSubTabController = new TabController();
        mTtSquare = new TextTab((TextView) view.findViewById(R.id.tv_type_square));
        mTtRoam = new TextTab((TextView) view.findViewById(R.id.tv_type_roam));
        mSubTabController.add(mTtSquare);
        mSubTabController.add(mTtRoam);
        mSubTabController.setListener(new TabListener() {

            @Override
            public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
                if (!selected) {
                    return;
                }
                switch (index) {
                    case 0:
                        mViewArcBg.setVisibility(View.VISIBLE);
                        mArcMenu.setVisibility(View.VISIBLE);
                        mViewPtrContent.setVisibility(View.VISIBLE);
                        mPtrListView.setVisibility(View.VISIBLE);
                        mRoamColleages.setVisibility(View.GONE);
                        mChooseType = TYPE_CAMPUS;
                        break;
                    case 1:
                        mViewArcBg.setVisibility(View.GONE);
                        mArcMenu.setVisibility(View.GONE);
                        mViewPtrContent.setVisibility(View.GONE);
                        showGuidDialog();
                        mPtrListView.setVisibility(View.GONE);
                        mRoamColleages.setVisibility(View.VISIBLE);
                        mRoamColleages.fetchData();
                        mChooseType = TYPE_ROAM;
                        break;
                    default:
                        break;
                }
            }
        });
        mSubTabController.select(0);

        initListEvent();
        fillStickLvlPost();
        MainActivity.addTabFragment(this);
        loadPosts();
        startCount();
        return view;
    }

    private void fillStickLvlPost() {
        if (mStickLvlPosts == null || mStickLvlPosts.size() <= 0) {
            mLlHotPosts.setVisibility(View.GONE);
            return;
        }
        mLlHotPosts.setVisibility(View.VISIBLE);
        mLlTop5Post.removeAllViews();
        int size = mStickLvlPosts.size();
        if (mStickLvlPosts != null && size > 0) {
            for (int i = 0; i < (size >= MAX_HOT_POST_COUNT ? MAX_HOT_POST_COUNT : size); i++) {
                mLlTop5Post.addView(getView(i));
            }
        }
    }

    /**
     * 填充公告信息
     *
     * @param news
     */
    private void fillNewsInfo(News news) {
        mTvNewsTitle.setText(news.title);
        boolean ticket = news.type == News.TYPE_TICKET;
        mIvTicketFlag.setVisibility(ticket ? View.VISIBLE : View.GONE);
    }

    private View getView(int i) {
        CfPost post = mStickLvlPosts.get(i);
        View view = mInflater.inflate(R.layout.item_campus_hot_post_info, null);
        TextView mTvTitle = (TextView) view.findViewById(R.id.tv_post_title);
        AvatarView avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
        ImageView image = (ImageView) view.findViewById(R.id.iv_image);
        ImageView imageFlag = (ImageView) view.findViewById(R.id.iv_image_flag);
        ImageView imageThumbnail = (ImageView) view.findViewById(R.id.iv_image_thumbnail);
        FrameLayout mFlVideo = (FrameLayout) view.findViewById(R.id.fl_video);
        FrameLayout mFlAvatar = (FrameLayout) view.findViewById(R.id.fl_avatar_bg);
        mTvTitle.setText(post.title);
        Picasso.with(getActivity())
                .load(post.userInfo.getAvatar())
                .placeholder(post.userInfo.defaultAvatar())
                .into(avatar);
        image.setVisibility(View.GONE);
        mFlVideo.setVisibility(View.GONE);
        mFlAvatar.setBackgroundResource(R.drawable.bg_hot_avatar);
        switch (post.threadType) {
            case CfPost.THREAD_TYPE_RICH_MEDIA:
                imageFlag.setImageResource(R.drawable.nomal_hot_link);
                imageFlag.setVisibility(View.VISIBLE);
                break;
            case CfPost.THREAD_TYPE_VIDEO:
                Picasso.with(getActivity()).load(post.videoThumbnail).into(imageThumbnail);
                mFlVideo.setVisibility(View.VISIBLE);
                if (post.content == null || post.content.equals("")) {
                    String key = post.userInfo.getDisplayName(getActivity());
                    String content = getString(R.string.post_video_nocontent_desc, key);
                    CharSequence coloredText = Utils.getColoredText(content, key, 0xff78a8e4);
                    mTvTitle.setText(coloredText);
                }
                break;
            case CfPost.THREAD_TYPE_VOICE:
                imageFlag.setImageResource(R.drawable.nomal_hot_sound);
                imageFlag.setVisibility(View.VISIBLE);
                if (post.content == null || post.content.equals("")) {
                    String key = post.userInfo.getDisplayName(getActivity());
                    String content = getString(R.string.post_voice_nocontent_desc, key);
                    CharSequence coloredText = Utils.getColoredText(content, key, 0xff78a8e4);
                    mTvTitle.setText(coloredText);
                }
                break;
            case CfPost.THREAD_TYPE_PIC_VOTE:
            case CfPost.THREAD_TYPE_TXT_VOTE:
                imageFlag.setImageResource(R.drawable.nomal_hot_vote);
                imageFlag.setVisibility(View.VISIBLE);
                if (post.content == null || post.content.equals("")) {
                    String key = post.userInfo.getDisplayName(getActivity());
                    String content = getString(R.string.post_vote_nocontent_desc, key);
                    CharSequence coloredText = Utils.getColoredText(content, key, 0xff78a8e4);
                    mTvTitle.setText(coloredText);
                }
                break;
            default:
                if (post.images != null && post.images.size() > 0) {
                    Picasso.with(getActivity())
                            .load(getSmallImageUrl(post.images.get(0).image))
                            .into(image);
                    image.setVisibility(View.VISIBLE);
                    if (post.content == null || post.content.equals("")) {
                        String key = post.userInfo.getDisplayName(getActivity());
                        String content = getString(R.string.post_normal_nocontent_desc, key);
                        CharSequence coloredText = Utils.getColoredText(content, key, 0xff78a8e4);
                        mTvTitle.setText(coloredText);
                    }
                } else {
                    image.setVisibility(View.GONE);
                }
                break;
        }
        view.setTag(post);
        view.setOnClickListener(this);
        return view;
    }

    private String getSmallImageUrl(String url) {
        return AliImgSpec.POST_PHOTO.makeUrlSingleImg(url);
    }

    private void fetchBannerData() {
        mVsSwitcher.setParam(CommonBanner.Plate.SquareMySchool, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MainActivity.mToNewsPost && mAdapter.hasNewPost()) {
            mPtrListView.getRefreshableView().setSelection(
                    mPtrListView.getRefreshableView().getHeaderViewsCount());
            MainActivity.mToNewsPost = false;
            mAdapter.resetHasPostStatus();
        }
        if (mChooseType != TYPE_ROAM) {
            mArcMenu.setVisibility(View.VISIBLE);
        } else {
            mArcMenu.setVisibility(View.GONE);
        }
        if (MainActivity.mChannelIndex >= 0) {
            mSubTabController.select(MainActivity.mChannelIndex);
            MainActivity.mChannelIndex = -1;
        }
        if (mTimer == null) {
            startCount();
        }
        if (mSurfaceView != null) {
            mFlContent.removeView(mSurfaceView);
            mSurfaceView = null;
        }
        System.out.println("----------- poststream : onResume-->" + System.currentTimeMillis());
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.stopVoicePlay();
        mTimer.cancel();
        mTimerTask = null;
        mTimer = null;
        mPtrListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (JCVideoPlayer.isVideoFinish) {
                    JCVideoPlayer.releaseAllVideos();
                }
                JCVideoPlayer.isVideoFinish = true;
            }
        }, 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPtrListView.setOnItemClickListener(null);
        mPtrListView.setOnRefreshListener((OnRefreshListener2<ListView>) null);
        mPtrListView.setAdapter(null);
        mPtrListView = null;
        mTaskManager.unregisterListener(this);
        mNewsManager.unregisterTaskListener(this);
    }

    @Override
    public void onDestroy() {
        mAdapter.destroy();
        if (mTimer != null) {
            mTimer.cancel();
            mTimerTask = null;
        }
        super.onDestroy();
    }

    @Override
    public void onTabButtonClicked() {
        super.onTabButtonClicked();
        if (mChooseType == TYPE_CAMPUS) {
            mPtrListView.setRefreshing(Mode.PULL_FROM_START);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mAdapter.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.RECORDE_SHOW && resultCode == Activity.RESULT_OK) {
            RecordResult result = new RecordResult(data);
            // 得到视频地址，和缩略图地址的数组，返回十张缩略图
            String videoPath = result.getPath();
            String[] thum = result.getThumbnail();
            result.getDuration();
            boolean video = Utils.copy(videoPath, Constant.VIDEOPATH);
            boolean thumbnail = Utils.copy(thum[0], Constant.THUMBPATH);
            if (video && thumbnail) {
                // 设置结果返回数据
                Intent intent = new Intent(getActivity(), ForumVideoPostActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, getForum());
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_LENGTH, result.getDuration());
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_PATH, Constant.VIDEOPATH);
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_THUMBNAIL, Constant.THUMBPATH);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "拷贝失败 ", Toast.LENGTH_LONG).show();
            }

            QupaiService qupaiService = QupaiManager.getQupaiService(getActivity());
            qupaiService.deleteDraft(getActivity(), data);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                MainActivity.showDrawerLayout();
                break;
            case R.id.btn_right:
                startActivity(SearchActivity.class);
                break;
            case R.id.av_avatar:
            case R.id.tv_name:
                if (v.getTag() instanceof UserInfo) {
                    showProfileActivity((UserInfo) v.getTag());
                }
                break;
            case R.id.iv_image:
                if (v.getTag() != null && v.getTag() instanceof CfPost) {
                    CfPost post = (CfPost) v.getTag();
                    showDetailsActivity(post);
                }
                break;
            case R.id.iv_more:
                break;
            case R.id.ll_news_info:
                startActivity(NewsActivity.class);
                MobclickAgent.onEvent(getActivity(), MStaticInterface.NOTICE);
                break;
            case R.id.rl_hot_post:
                CfPost post = (CfPost) v.getTag();
                showPostDetail(post);
                break;
            default:
                break;
        }
    }

    private void showPostDetail(CfPost post) {
        Intent intent = new Intent(getActivity(), ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, post);
        intent.putExtra(ForumPostDetailActivity.EXTRL_IS_SHOW_TRIBE, false);
        intent.putExtra(ForumPostDetailActivity.EXTRL_SHOW_OTHER_SCHOOL_TAG, true);
        getActivity().startActivity(intent);
    }

    private void showDetailsActivity(CfPost post) {
        Intent iDetails = new Intent(getActivity(), ForumPostDetailActivity.class);
        iDetails.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, post);
        startActivity(iDetails);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        mIsRefreshing = true;
        if (refreshView == mPtrListView) {
            fetchPosts(true);
            fetchBannerData();
            mRefreshTime = getCampusTime();
        }
    }

    private long getCampusTime() {
        return System.currentTimeMillis() / 1000;
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        mIsRefreshing = true;
        if (refreshView == mPtrListView) {
            fetchPosts(false);
        }
    }

    protected void setTitle() {
        mTitleBar.setTitle(R.string.cf_post_campus);
        mTitleBar.showRightButton(R.drawable.icon_search, this);
        mTitleBar.showLeftButton(R.drawable.icon_personal, this);
    }

    private void fetchPosts(boolean refresh) {
        int size = mAdapter.getCount();
        long time = refresh || size == 0 ? 0 : (mAdapter.getItem(size - 1).replyTime - 1000);
        if (userInfo != null) {
            new FetchPostStreamTask(time).executeLong();
        }
        if (refresh) {
            mRefreshTime = getCampusTime();
            MainActivity.showRefreshRed(0, false);
        }
        mNewsManager.startNewsFetchLatestTask(0, 1, 1);
    }

    private void showProfileActivity(UserInfo userInfo) {
        Intent iProfile = new Intent(getActivity(), NewProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        startActivity(iProfile);
    }

    private class FetchPostStreamTask extends FetchPostsTask {
        private long mTime;

        public FetchPostStreamTask(long time) {
            super(MsRequest.CF_LIST_THREADS_BY_FORUM, time == 0);
            mTime = time;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("forum_type=")
                    .append(Forum.TYPE_DEFAULT)
                    .append("&school_id=")
                    .append(mSchoolId)
                    .append("&time=")
                    .append(mTime / 1000)
                    .toString();
        }
    }

    private class FetchPostsTask extends MsTask {
        private boolean mRefresh;

        public FetchPostsTask(MsRequest request, boolean refresh) {
            super(getActivity(), request);
            mRefresh = refresh;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (mPtrListView == null) {
                return;
            }
            if (mIsRefreshing) {
                mPtrListView.onRefreshComplete();
            }
            if (response.isSuccessful()) {
                final ArrayList<CfPost> posts = JsonUtil.getArray(
                        response.getJsonArray(), CfPost.TRANSFORMER);
                mPtrListView.postDelayed(new Runnable() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                        if (mRefresh) {
                            final ArrayList<CfPost> normalPosts = filterStickPost(posts);
                            ArrayList<CfPost> copyPosts = Utils.copy(normalPosts);
                            copyPosts.addAll(mStickLvlPosts);
                            new UpdateDbPostInfoTask().execute(copyPosts);
                            mAdapter.reset(normalPosts);
                        } else {
                            mAdapter.addAll(posts);
                        }
                    }
                }, 500);
            }
        }
    }

    private ArrayList<CfPost> filterStickPost(ArrayList<CfPost> posts) {
        if (mStickLvlPosts == null) {
            mStickLvlPosts = new ArrayList<>();
        }
        mStickLvlPosts.clear();
        for (CfPost post : posts) {
            if (post.isStickLvl()) {
                mStickLvlPosts.add(post);
            }
        }
        ArrayList<CfPost> copiedPost = Utils.copy(posts);
        for (CfPost post : mStickLvlPosts) {
            copiedPost.remove(post);
        }
        fillStickLvlPost();
        return copiedPost;
    }

    private void updatePostDbInfo(ArrayList<CfPost> posts) {
        DataHelper.deletePostInfoBySchoolId(getActivity(), mSchoolId);
        DataHelper.deleteCacheImage(getActivity(), getPostImagesIds(posts));
        DataHelper.deleteVoteInfo(getActivity(), getPostVoteIds(posts));
        ArrayList<CachePostInfo> infos = new ArrayList<>();
        for (CfPost post : posts) {
            CachePostInfo info = new CachePostInfo(post);
            info.schoolId = mSchoolId;
            long imageId = DataHelper.insertCacheImages(getActivity(), getCacheImage(post.images));
            long voteId = DataHelper.insertVoteInfo(getActivity(), getCacheVote(post.vote));
            if (DataHelper.loadCacheUserInfo(getActivity(), post.userInfo.userId) != null) {
                DataHelper.updateCacheUserInfo(getActivity(), post.userInfo);
            } else {
                DataHelper.insertCacheUserInfo(getActivity(), post.userInfo);
            }
            info.imageId = imageId;
            info.optionId = voteId;
            infos.add(info);
        }
        DataHelper.insertPostInfo(getActivity(), infos);
    }

    private ArrayList<String> getPostImagesIds(ArrayList<CfPost> posts) {
        ArrayList<String> ids = new ArrayList<>();
        for (CfPost post : posts) {
            if (post.imageId > 0) {
                ids.add(String.valueOf(post.imageId));
            }
        }
        return ids;
    }

    private ArrayList<String> getPostVoteIds(ArrayList<CfPost> posts) {
        ArrayList<String> ids = new ArrayList<>();
        for (CfPost post : posts) {
            if (post.optionId > 0) {
                ids.add(String.valueOf(post.optionId));
            }
        }
        return ids;
    }

    private CacheVoteInfo getCacheVote(Vote vote) {
        if (vote == null || vote.options == null || vote.options.size() == 0) {
            return null;
        }
        CacheVoteInfo voteInfo = new CacheVoteInfo();
        voteInfo.count = vote.options.size();
        if (voteInfo.count == 0) {
            return voteInfo;
        }
        if (voteInfo.count >= 1) {
            voteInfo.option1 = vote.options.get(0);
        }
        if (voteInfo.count >= 2) {
            voteInfo.option2 = vote.options.get(1);
        }
        if (voteInfo.count >= 3) {
            voteInfo.option3 = vote.options.get(2);
        }
        if (voteInfo.count >= 4) {
            voteInfo.option4 = vote.options.get(3);
        }
        return voteInfo;
    }

    private CacheImageInfo getCacheImage(ArrayList<Image> images) {
        CacheImageInfo info = new CacheImageInfo();
        info.count = images.size();
        if (images.size() == 0) {
            return info;
        }
        if (info.count >= 1) {
            info.url1 = images.get(0).image;
        }
        if (info.count >= 2) {
            info.url2 = images.get(1).image;
        }
        if (info.count >= 3) {
            info.url3 = images.get(2).image;
        }
        if (info.count >= 4) {
            info.url4 = images.get(3).image;
        }
        if (info.count >= 5) {
            info.url5 = images.get(4).image;
        }
        if (info.count >= 6) {
            info.url6 = images.get(5).image;
        }
        if (info.count >= 7) {
            info.url7 = images.get(6).image;
        }
        if (info.count >= 8) {
            info.url8 = images.get(7).image;
        }
        if (info.count >= 9) {
            info.url9 = images.get(8).image;
        }
        return info;
    }

    private class UpdateDbPostInfoTask extends AsyncTask<ArrayList<CfPost>, Integer, Boolean> {

        @SuppressWarnings("unchecked")
        @Override
        protected Boolean doInBackground(ArrayList<CfPost>... params) {
            updatePostDbInfo(params[0]);
            return true;
        }

    }

    @Override
    public void onNoContentClick() {
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

    private void initListEvent() {
        mArcMenu.setOnMenuItemClickListener(new onMenuItemClickListener() {

            @Override
            public void onClick(View view, int pos) {
                Intent intent = new Intent();
                switch (pos) {
                    case 1:// normal post
                        intent.setClass(getActivity(), NormalPostActivity.class);
                        intent.putExtra(Forum.INTENT_EXTRA_NAME, getForum());
                        startActivityForResult(intent, 0);
                        break;
                    case 2: // video
                        startVideoPost();
                        break;
                    case 3: // pic vote
                        intent.setClass(getActivity(), TxtVotePostActivity.class);
                        intent.putExtra(Forum.INTENT_EXTRA_NAME, getForum());
                        startActivityForResult(intent, 0);
                        break;
                    default:
                        break;
                }
            }
        });
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void startVideoPost() {
        QupaiService qupaiService = QupaiManager.getQupaiService(getActivity());
        if (qupaiService == null) {
            Toast.makeText(getActivity(), "插件没有初始化，无法获取 QupaiService", Toast.LENGTH_LONG).show();
            return;
        }
        AppGlobalSetting sp = new AppGlobalSetting(getActivity());
        MianLiaoApp.sIsGuidShow = sp.getBooleanGlobalItem(
                MianLiaoApp.PREF_VIDEO_EXIST_USER, true);
        qupaiService.showRecordPage(getActivity(), RequestCode.RECORDE_SHOW, MianLiaoApp.sIsGuidShow);
        sp.saveGlobalConfigItem(MianLiaoApp.PREF_VIDEO_EXIST_USER, false);
    }


    private Forum getForum() {
        return Forum.DEFAULT_FORUM;
    }

    private void showAlphaAnimation(View view, float fromAlpha, float toAlpha, int duration) {
        final boolean show = toAlpha > 0.5f;
        Animation alphaAnimation = new AlphaAnimation(fromAlpha, toAlpha);
        alphaAnimation.setDuration(1000);
        alphaAnimation.setFillAfter(true);
        alphaAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (show) {
                    mIsArcMenuShow = true;
                }
            }
        });
        view.startAnimation(alphaAnimation);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case OnScrollListener.SCROLL_STATE_IDLE:
                showAlphaAnimation(mArcMenu, 0.1f, 1.0f, 500);
                break;
            default:
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        if (mIsArcMenuShow) {
            mIsArcMenuShow = false;
            showAlphaAnimation(mArcMenu, 1.0f, 0.1f, 500);
        }
    }

    @Override
    public void onPreExecute(MsTaskType type) {

    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_PUBLISH_POST:
            case FORUM_STICK_POST_V4:
                fetchPosts(true);
                break;

            default:
                break;
        }
    }

    @Override
    public void onPreExecute(int type) {

    }

    @Override
    public void onPostExecute(int type, MsResponse mr) {
        switch (type) {
            case TaskType.NEWS_FETCH_LATEST:
                if (MsResponse.isSuccessful(mr) && mr.value != null) {
                    try {
                        List<News> newsList = (List<News>) mr.value;
                        if (newsList.size() >= 1) {
                            fillNewsInfo(newsList.get(0));
                        }
                    } catch (Exception e) {
                    }
                }
                break;
            default:
                break;
        }
    }

    private class GetRefreshPostCount extends MsTask {

        public GetRefreshPostCount() {
            super(getActivity(), MsRequest.CF_REFRESH_COUNT);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("school_id=").append(mSchoolId)
                    .append("&time=").append(mRefreshTime).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject jsonObj = response.json.optJSONObject(MsResponse.PARAM_RESPONSE);
                mCount = jsonObj.optInt("count");
                if (mCount > 0) {
                    mFreshHandler.sendEmptyMessage(UPDATE_POST);
                }
            }
        }

    }


}
