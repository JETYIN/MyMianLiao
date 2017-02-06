package com.tjut.mianliao.forum;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.alibaba.sdk.android.AlibabaSDK;
import com.alibaba.sdk.android.callback.FailureCallback;
import com.duanqu.qupai.sdk.android.QupaiService;
import com.duanqu.qupai.sdk.utils.AppGlobalSetting;
import com.google.common.io.Files;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.SearchActivity;
import com.tjut.mianliao.common.Contant;
import com.tjut.mianliao.common.RecordResult;
import com.tjut.mianliao.common.RequestCode;
import com.tjut.mianliao.component.ArcMenu;
import com.tjut.mianliao.component.ArcMenu.onMenuItemClickListener;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.PopupView.OnItemClickListener;
import com.tjut.mianliao.component.PopupView.PopupItem;
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
import com.tjut.mianliao.forum.nova.ChannelSearchActivity;
import com.tjut.mianliao.forum.nova.CreateChannelActivity;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.NoContentClickListener;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.OnNativeScrollListener;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.forum.nova.ForumVideoPostActivity;
import com.tjut.mianliao.forum.nova.HotTop5Activity;
import com.tjut.mianliao.forum.nova.MyChannelActivity;
import com.tjut.mianliao.forum.nova.NormalPostActivity;
import com.tjut.mianliao.forum.nova.TxtVotePostActivity;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.news.NewsActivity;
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.NewsGuidDialog;
import com.tjut.mianliao.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class PostStreamTabFragment extends TabFragment implements OnClickListener,
        OnRefreshListener2<ListView>, OnItemClickListener, NoContentClickListener,
        OnNativeScrollListener, MsTaskListener {

    private static final int MAX_HOT_POST_COUNT = 2;
	private static final int TYPE_CAMPUS = 1;
	private static final int TYPE_ROAM = 2;
	
    public static final String SP_IS_FIRST_ROAM = "sp_roam_view";
    protected static final String SP_POST_MINE = "post_mine";

    private View mTabView;
	private View mViewArcBg;
    private View mViewPtrContent;
    private PullToRefreshListView mPtrListView; 
    private View mHeaderView;
    private SharedPreferences mPreferences;
    private NewsGuidDialog mGuidDialog;
    private FrameLayout mFlMain;
    private LinearLayout mLlTop5Post;
    private TextView mTvNewsTitle;
    private ImageView mIvTicketFlag;

    private ForumPostAdapter mAdapter;
    private TabController mSubTabController;
    private CommonBanner mVsSwitcher;
    private MsTaskManager mTaskManager;
    
    private RoamCollegeView mRoamColleages;
    // private PullToRefreshListView mPtrLvChannel;
    
    private TextTab mTtSquare, mTtRoam;
    private ArrayList<CfPost> mHotTop5Posts;

    private int mSchoolId;
    private int mHotPostCount;
    private int mChooseType = TYPE_CAMPUS;

    private Settings mSettings;
    private String mForumName;

    protected boolean mShowBanner = true;
    private boolean mIsNightMode;
    private boolean mIsRefreshing;
    private boolean mIsArcMenuShow;

    private ArcMenu mArcMenu;
	private LinearLayout mLlHotPosts;

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
        mHotTop5Posts = new ArrayList<>();
        mTaskManager = MsTaskManager.getInstance(getActivity());
        mTaskManager.registerListener(this);
        
        mAdapter = new ForumPostAdapter(getActivity());
        mAdapter.setActivity(getActivity());
        mAdapter.showOtherSchool();
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickbel(true);
        mAdapter.setOnNoContentListener(this);

        mPreferences = DataHelper.getSpForData(getActivity());
        AccountInfo accountInfo = AccountInfo.getInstance(getActivity());
        UserInfo userInfo = accountInfo.getUserInfo();
        userInfo = userInfo == null ? accountInfo.loadUserInfoFromSp() : userInfo;
        mSchoolId = userInfo.schoolId;
        mForumName = userInfo.school;
    }

    public void getHotPostCount(ArrayList<CfPost> posts) {
        mHotTop5Posts.clear();
        mHotPostCount = 0;
        for (CfPost post : posts) {
            if (post.hot == 1) {
                mHotPostCount++;
                mHotTop5Posts.add(post);
            }
        }
    }

    protected void loadPosts() {
        ArrayList<CfPost> posts = DataHelper.loadPostsInfoBySchoolId(getActivity(), mIsNightMode, mSchoolId);
        if (posts != null && posts.size() > 0) {
            getHotPostCount(posts);
            mAdapter.reset(posts);
            fill5TopPost();
            fetchPosts(true);
        } else {
            mPtrListView.setRefreshing(Mode.PULL_FROM_START);
        }
    }

    private void showGuidDialog() {
        boolean isFirst = mPreferences.getBoolean(SP_IS_FIRST_ROAM, false);
        if (isFirst) {
            mGuidDialog = new NewsGuidDialog(getActivity(), R.style.Translucent_NoTitle);
            mGuidDialog.showGuidImage(getGuidImageRes(), SP_IS_FIRST_ROAM);
        }
    }

    private int[] getGuidImageRes() {
        int[] imgRes = { R.drawable.guid_forum_roma_collect, R.drawable.guid_forum_search_school,
                R.drawable.guid_forum_unlock };
        if (mIsNightMode) {
            imgRes = new int[] { R.drawable.guid_forum_roma_collect_black, R.drawable.guid_forum_search_school_black,
                    R.drawable.guid_forum_unlock_black };
        }
        return imgRes;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mFlMain = (FrameLayout) view.findViewById(R.id.fl_post_stream);
        mViewPtrContent = view.findViewById(R.id.fl_ptr_content);
        mArcMenu = (ArcMenu) view.findViewById(R.id.id_arcmenu);
        mViewArcBg = view.findViewById(R.id.bg_view);
        mRoamColleages = (RoamCollegeView) view.findViewById(R.id.roaming_colleages);
        mTabView = view.findViewById(R.id.post_type_tab_layout);
        
        mSettings = Settings.getInstance(getActivity());
        mIsNightMode = mSettings.isNightMode();
        mPtrListView = (PullToRefreshListView) view.findViewById(R.id.ptrlv_post_stream);
        ListView lvList = mPtrListView.getRefreshableView();
        
        if (mShowBanner) {
            mHeaderView = inflater.inflate(R.layout.campus_head_view, lvList, false);
            lvList.addHeaderView(mHeaderView, null, false);
            mVsSwitcher =  (CommonBanner) mHeaderView.findViewById(R.id.vs_switcher);
            mLlHotPosts = (LinearLayout) mHeaderView.findViewById(R.id.ll_hot_posts);
            mHeaderView.findViewById(R.id.ll_news_info).setOnClickListener(this);
            mHeaderView.findViewById(R.id.tv_more_hot).setOnClickListener(this);
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
        mTtRoam.setNightMode(mIsNightMode);
        mTtSquare.setNightMode(mIsNightMode);
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

        checkDayNightUI();
        loadPosts();
        initListEvent();
        // test --- >
        fill5TopPost();
        // test < ---
        MainActivity.addTabFragment(this);
        return view;
    }

    private void fill5TopPost() {
    	if (mHotTop5Posts == null || mHotTop5Posts.size() <= 0) {
    		mLlHotPosts.setVisibility(View.GONE);
    		return;
    	}
    	mLlHotPosts.setVisibility(View.VISIBLE);
        mLlTop5Post.removeAllViews();
        int size = mHotTop5Posts.size();
        if (mHotTop5Posts != null && size > 0) {
            for (int i = 0; i < (size >= MAX_HOT_POST_COUNT ? MAX_HOT_POST_COUNT : size) ; i++) {
                mLlTop5Post.addView(getView(i));
            }
        }
	}
    
    /**
     * 填充公告信息
     * @param news
     */
    private void fillNewsInfo(News news) {
    	mTvNewsTitle.setText(news.title);
    	boolean ticket = news.type == News.TYPE_TICKET;
    	mIvTicketFlag.setVisibility(ticket ? View.VISIBLE : View.GONE);
    }

	private View getView(int i) {
        CfPost post =  mHotTop5Posts.get(i);
		View view = mInflater.inflate(R.layout.item_campus_hot_post_info, null);
		TextView mTvFlag = (TextView) view.findViewById(R.id.tv_flag);
        TextView mTvTitle = (TextView) view.findViewById(R.id.tv_post_title);
        AvatarView avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
        ImageView image = (ImageView) view.findViewById(R.id.iv_image);
        ImageView imageFlag = (ImageView) view.findViewById(R.id.iv_image_flag);
        ImageView imageThumbnail = (ImageView) view.findViewById(R.id.iv_image_thumbnail);
        FrameLayout mFlVideo = (FrameLayout) view.findViewById(R.id.fl_video);
        FrameLayout mFlAvatar = (FrameLayout) view.findViewById(R.id.fl_avatar_bg);
        mTvFlag.setText("Top" + (i + 1));
        mTvFlag.setBackgroundResource(mIsNightMode ? R.drawable.pic_bg_top_black :
            R.drawable.pic_bg_top);
        mTvFlag.setTextColor(getColorByPosition(i));
        mTvTitle.setText(post.content);
        Picasso.with(getActivity()).load(post.userInfo.getAvatar()).into(avatar);
        image.setVisibility(View.GONE);
        mFlVideo.setVisibility(View.GONE);
        mFlAvatar.setBackgroundResource(mIsNightMode ?
                R.drawable.bg_hot_avatar_black : R.drawable.bg_hot_avatar);
        switch (post.threadType) {
		case CfPost.THREAD_TYPE_RICH_MEDIA:
			image.setImageResource(mIsNightMode ?
					R.drawable.nomal_hot_link_black : R.drawable.nomal_hot_link);
			image.setVisibility(View.VISIBLE);
			break;
		case CfPost.THREAD_TYPE_VIDEO:
			Picasso.with(getActivity())
				.load(post.videoThumbnail)
				.into(imageThumbnail);
			mFlVideo.setVisibility(View.VISIBLE);
			if (post.content == null || post.content.equals("")) {
				String key = post.userInfo.getDisplayName(getActivity());
				String content = getString(R.string.post_video_nocontent_desc, key);
				CharSequence coloredText = Utils.getColoredText(content, key, 0xff78a8e4);
				mTvTitle.setText(coloredText);
			}
			break;
		case CfPost.THREAD_TYPE_VOICE:
		    imageFlag.setImageResource(mIsNightMode ?
					R.drawable.nomal_hot_sound_black : R.drawable.nomal_hot_sound);
		    imageFlag.setVisibility(View.VISIBLE);
			if (post.content == null || post.content.equals("")) {
				String key = post.userInfo.getDisplayName(getActivity());
				String content = getString(R.string.post_voice_nocontent_desc, key);
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

    private int getColorByPosition(int i) {
        switch (i) {
            case 0:
                return 0xffff5151;
            case 1:
                return 0xfffe6e6e;
            default:
                return 0xfffd8d8d;
        }
    }

    private String getSmallImageUrl(String url) {
        return AliImgSpec.POST_PHOTO.makeUrlSingleImg(url);
    }

	private void fetchBannerData() {
        mVsSwitcher.setParam(CommonBanner.Plate.SquareMySchool, 0);
    }

    @Override
    public void onResume() {
        if (MainActivity.mToNewsPost && mAdapter.hasNewPost()) {
            mPtrListView.getRefreshableView().setSelection(
                    mPtrListView.getRefreshableView().getHeaderViewsCount() + mHotPostCount);
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
        super.onResume();
    }

    @Override
    public void onPause() {
        mAdapter.stopVoicePlay();
        super.onPause();
    }
    
    private void checkDayNightUI() {
        changeNightModeView();
    }

    private void changeNightModeView() {
        if (mIsNightMode) {
            mTabView.setBackgroundColor(getResources().getColor(R.color.bg_night_color));
            mFlMain.setBackgroundResource(R.drawable.bg);
            mTtSquare.setNightMode(true);
            mTtRoam.setNightMode(true);
        }
        mTtSquare.setChosen(isSquareChoosed());
        mTtRoam.setChosen(!isSquareChoosed());
    }

    private boolean isSquareChoosed() {
        return mSubTabController.getCurrent() == 0;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPtrListView.setOnItemClickListener(null);
        mPtrListView.setOnRefreshListener((OnRefreshListener2<ListView>) null);
        mPtrListView.setAdapter(null);
        mPtrListView = null;
        mTaskManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {
        mAdapter.destroy();
        super.onDestroy();
    }

    @Override
    public void onTabButtonClicked() {
        super.onTabButtonClicked();
    	mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mAdapter.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.RECORDE_SHOW && resultCode == Activity.RESULT_OK) {
            RecordResult result =new RecordResult(data);
            //得到视频地址，和缩略图地址的数组，返回十张缩略图
            String videoPath = result.getPath();
            String [] thum = result.getThumbnail();
            try{
                File file = new File(Contant.VIDEOPATH);
                Files.move(new File(videoPath), file);
                Files.move(new File(thum[0]), new File(Contant.THUMBPATH));
                // 设置结果返回数据
                Intent intent = new Intent(getActivity(), ForumVideoPostActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, getForum());
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_LENGTH, result.getDuration());
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_PATH, Contant.VIDEOPATH);
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_THUMBNAIL, Contant.THUMBPATH);
                startActivity(intent);
            } catch (IOException e){
                Toast.makeText(getActivity(),"拷贝失败 ",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            
            QupaiService qupaiService = AlibabaSDK
                    .getService(QupaiService.class);
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
		case R.id.tv_more_hot:
			Intent hotIntent = new Intent(getActivity(), HotTop5Activity.class);
			hotIntent.putExtra(HotTop5Activity.EXT_HOT_TOP5_POSTS, mHotTop5Posts);
			startActivity(hotIntent);
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
        }
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
        if (size == 1 && !mAdapter.hasData()) {
            size = 0;
        }
        long time = refresh || size == 0 ? 0 : (mAdapter.getItem(size - 1).createdOn - 1000);
        new FetchPostStreamTask(time).executeLong();
        new GetLatestNewsTask().executeLong();
    }

    private void showProfileActivity(UserInfo userInfo) {
        Intent iProfile = new Intent(getActivity(), ProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        startActivity(iProfile);
    }

    private class FetchPostStreamTask extends FetchPostsTask {
        private long mTime;

        public FetchPostStreamTask(long time) {
            super(MsRequest.CFC_LIST_TIMELINE, time == 0);
            mTime = time;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("time=").append(mTime / 1000)
                    .append("&all_types=1").toString();
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
            /*showAlphaAnimation(mArcMenu, 0.1f, 1.0f, 500);*/
            if (response.isSuccessful()) {
                final ArrayList<CfPost> posts = JsonUtil.getArray(response.getJsonArray(),
                        CfPost.TRANSFORMER);
                mPtrListView.postDelayed(new Runnable() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                        if (mRefresh) {
                            final ArrayList<CfPost> normalPosts = filterHotPost(posts);
                            new UpdateDbPostInfoTask().execute(posts);
                            mAdapter.reset(normalPosts);
                        } else {
                            mAdapter.addAll(posts);
                        }
                    }
                }, 500);
            }
        }
    }

    private ArrayList<CfPost> filterHotPost(ArrayList<CfPost> posts) {
        if (mHotTop5Posts == null) {
            mHotTop5Posts = new ArrayList<>();
        }
        mHotTop5Posts.clear();
        for (CfPost post : posts) {
            if (post.hot == 1) {
                mHotTop5Posts.add(post);
            }
        }
        for (CfPost post : mHotTop5Posts) {
            posts.remove(post);
        }
        fill5TopPost();
        return posts;
    }

    private class GetLatestNewsTask extends MsTask{

        public GetLatestNewsTask() {
            super(getActivity(), MsRequest.NEWS_MY_BROADCAST_TODAY);
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<News> news = JsonUtil.getArray(
                        response.getJsonArray(), News.TRANSFORMER);
                if (news != null && news.size() > 0) {
                    fillNewsInfo(news.get(0));
                }
            }
        }
        
    }

    private void updatePostDbInfo(ArrayList<CfPost> posts) {
        DataHelper.deletePostInfoBySchoolId(getActivity(), mSchoolId, mIsNightMode);
        DataHelper.deleteCacheImage(getActivity(), getPostImagesIds(posts));
        DataHelper.deleteVoteInfo(getActivity(), getPostVoteIds(posts));
        ArrayList<CachePostInfo> infos = new ArrayList<>();
        for (CfPost post : posts) {
            CachePostInfo info = new CachePostInfo(post);
            info.schoolId = mSchoolId;
            long imageId = DataHelper.insertCacheImages(getActivity(), getCacheImage(post.images));
            long voteId = DataHelper.insertVoteInfo(getActivity(), getCacheVote(post.vote));
            if (DataHelper.loadCacheUserInfo(getActivity(), post.userInfo.userId, mIsNightMode) != null) {
                DataHelper.updateCacheUserInfo(getActivity(), post.userInfo, mIsNightMode);
            } else {
                DataHelper.insertCacheUserInfo(getActivity(), post.userInfo, mIsNightMode);
            }
            info.imageId = imageId;
            info.optionId = voteId;
            info.isNightPost = mIsNightMode;
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

    @Override
    public void onItemClick(int position, PopupItem item) {
        switch (position) {
            case 0:
                startActivity(new Intent(getActivity(), CreateChannelActivity.class));
                MobclickAgent.onEvent(getActivity(), MStaticInterface.CREAT_CHANNEL);
                break;
            case 1:
                startActivity(new Intent(getActivity(), ChannelSearchActivity.class));
                MobclickAgent.onEvent(getActivity(), MStaticInterface.SEARCH_CHANNEL);
                break;
            case 2:
                startActivity(new Intent(getActivity(), MyChannelActivity.class));
                break;
            default:
                break;
        }
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
    
    private void startVideoPost() {
        QupaiService qupaiService = AlibabaSDK.getService(QupaiService.class);

        if (qupaiService == null) {
            Toast.makeText(getActivity(), "插件没有初始化，无法获取 QupaiService", Toast.LENGTH_LONG).show();
            return;
        }
        qupaiService.showRecordPage(getActivity(), RequestCode.RECORDE_SHOW, MianLiaoApp.sIsGuidShow,
                new FailureCallback() {
                    @Override
                    public void onFailure(int i, String s) {
                        Toast.makeText(getActivity(), "onFailure:"+ s + "CODE"+ i,
                                Toast.LENGTH_LONG).show();
                    }
                });
        setGuidShowSp();
    }

    private void setGuidShowSp() {
        AppGlobalSetting sp = new AppGlobalSetting(getActivity());
        sp.saveGlobalConfigItem(MianLiaoApp.PREF_VIDEO_EXIST_USER, false);
        MianLiaoApp.sIsGuidShow = false;
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
			public void onAnimationStart(Animation animation) {}
			
			@Override
			public void onAnimationRepeat(Animation animation) {}
			
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
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
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
			break;

		default:
			break;
		}
	}

}
