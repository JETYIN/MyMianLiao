package com.tjut.mianliao.tribe;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.alibaba.sdk.android.AlibabaSDK;
//import com.alibaba.sdk.android.callback.FailureCallback;
import com.duanqu.qupai.sdk.android.QupaiManager;
import com.duanqu.qupai.sdk.android.QupaiService;
//import com.duanqu.qupai.sdk.utils.AppGlobalSetting;
import com.duanqu.qupai.utils.AppGlobalSetting;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.common.RecordResult;
import com.tjut.mianliao.common.RequestCode;
import com.tjut.mianliao.component.ArcMenu;
import com.tjut.mianliao.component.ArcMenu.onMenuItemClickListener;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.TitleBar;
import com.tjut.mianliao.component.PopupView.OnItemClickListener;
import com.tjut.mianliao.component.PopupView.PopupItem;
import com.tjut.mianliao.component.ProAvatarView;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.cache.CacheImageInfo;
import com.tjut.mianliao.data.cache.CachePostInfo;
import com.tjut.mianliao.data.cache.CacheVoteInfo;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.Vote;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.forum.nova.ForumPostDetailActivity;
import com.tjut.mianliao.forum.nova.ForumVideoPostActivity;
import com.tjut.mianliao.forum.nova.HotPostsActivity;
import com.tjut.mianliao.forum.nova.NormalPostActivity;
import com.tjut.mianliao.forum.nova.TxtVotePostActivity;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.video.JCVideoPlayer;

/**
 * Tribe detail(部落详情)
 * 
 * @author YoopWu
 * 
 */
public class TribeDetailActivity extends BaseActivity implements
		OnRefreshListener2<ListView>, OnClickListener, onMenuItemClickListener,
		OnItemClickListener, MsTaskListener {

    private static final int UPDATE_POST = 6;
    
	public static final String EXT_DATA_INFO = "ext_data_info";
	public static final String EXT_DATA_ID = "ext_data_id";

	@ViewInject(R.id.ptr_tribe_posts)
	private PullToRefreshListView mPtrTribeDetail;
	@ViewInject(R.id.tv_tribe_name)
	private TextView mTvTribeName;
	@ViewInject(R.id.tv_attention_count)
	private TextView mTvFollowCount;
	@ViewInject(R.id.tv_posts_count)
	private TextView mTvPostCount;
	private TextView mTvUpCount;
	@ViewInject(R.id.tv_tribe_desc)
	private TextView mTvTribeDesc;
	@ViewInject(R.id.piv_avatar)
	private ProAvatarView mAvatar;
	@ViewInject(R.id.bg_view)
	private View mViewArcBg;
	@ViewInject(R.id.id_arcmenu)
	private ArcMenu mArcMenu;
	private AvatarView mAvatarView;
	private TextView mTvFollow;

	private MsTaskManager mTaskManager;
	private NotificationHelper mNotificationHelper;

	private ForumPostAdapter mAdapter;
	private PopupView mPopupView;

	private TribeInfo mTribeInfo;

	private ArrayList<CfPost> mStickLvlPosts;

	private LinearLayout mLlStickLvlPosts;

	private boolean mShouldRefresh, isShowToast;

	private int mTribeId;
    private View mHeaderView;
    private int mFreshTime;
    
    private Timer mTimer;
    private TimerTask mTimerTask;
    private int mCount;
    private TitleBar mTitleBar;
    private long mRefreshTime;
    private TextView mTvTitle;
    
    private Handler mFreshHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_POST:
                    mTitleBar.showRefreshRed(true);
                    break;
                default:
                    break;
            }
        }
    };

	@Override
	protected int getLayoutResID() {
		return R.layout.activity_tribe_detail;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		JCVideoPlayer.isVideoFinish = true;
		mFreshTime = Constant.getFreshTimeDelay(this);
		mTaskManager = MsTaskManager.getInstance(this);
		mNotificationHelper = NotificationHelper.getInstance(this);
		mTaskManager.registerListener(this);
		mTitleBar = getTitleBar(); 
		mStickLvlPosts = new ArrayList<>();
		mTribeInfo = getIntent()
				.getParcelableExtra(TribeInfo.INTENT_EXTRA_INFO);
		mTribeId = getIntent().getIntExtra(EXT_DATA_ID, 0);
		ListView lvList = mPtrTribeDetail.getRefreshableView();
		mHeaderView = mInflater.inflate(R.layout.tribe_detail_header, null);
		mHeaderView.setVisibility(View.INVISIBLE);
		mHeaderView.setOnClickListener(this);
		lvList.addHeaderView(mHeaderView);
		mLlStickLvlPosts = (LinearLayout) mHeaderView.findViewById(R.id.ll_hot_top5);
		mTvTribeName = (TextView) mHeaderView.findViewById(R.id.tv_tribe_name);
		mTvFollowCount = (TextView) mHeaderView.findViewById(R.id.tv_attention_count);
		mTvPostCount = (TextView) mHeaderView.findViewById(R.id.tv_posts_count);
		mTvUpCount = (TextView) mHeaderView.findViewById(R.id.tv_up_count);
		mTvTribeDesc = (TextView) mHeaderView.findViewById(R.id.tv_tribe_desc);
		mAvatarView = (AvatarView) mHeaderView.findViewById(R.id.piv_avatar);
		mTvFollow = (TextView) mHeaderView.findViewById(R.id.tv_follow_with);
		mTvTitle = (TextView) this.findViewById(R.id.tv_title);

		mTvTitle.setOnClickListener(this);
		mArcMenu.setOnMenuItemClickListener(this);

		mShouldRefresh = true;
		isShowToast = false;
		mAdapter = new ForumPostAdapter(this);
		mAdapter.setIsShowTribeIndetail(false);
		mAdapter.setTextClickble(true);
		mAdapter.setSpanClickble(true);
		mAdapter.setIsShowSchoolName(true);
		mPtrTribeDetail.setMode(Mode.BOTH);
		mPtrTribeDetail.setOnRefreshListener(this);
		mPtrTribeDetail.setAdapter(mAdapter);
		getTitleBar().showRightButton(R.drawable.icon_more, this);
		if (mTribeInfo != null) {
			fillTribeInfo();
			loadDatas();
		} else {
			if (mTribeId > 0) {
				new GetTribeInfoTask().executeLong();
				mNotificationHelper.clearNotification(NotificationType.TRIBE_RECOMMEND);
			}
		}
		fillStickLvlPost();
		
		mTimer = new Timer();
        mTimerTask = new TimerTask() {

            @Override
            public void run() {
                new GetRefreshPostCount().executeLong();
            } 
        };

        mTimer.schedule(mTimerTask, mFreshTime, mFreshTime);
	}

	private void fillTribeInfo() {
		updateTopView();
		getTitleBar().setTitle(mTribeInfo.tribeName);
		if (mTribeInfo.collected) {

			if (mTribeInfo.hasAssisted) {
				initUp();
			}
			if (!mTribeInfo.hasAssisted) {
				initValueOfUp();
			}
		}
	}

	private void loadDatas() {
		ArrayList<CfPost> posts = DataHelper.loadPostsInfo(this, mTribeInfo.tribeFid);
		if (posts != null && posts.size() > 0) {
			mAdapter.reset(posts);
			mHeaderView.setVisibility(View.VISIBLE);
		}
		mPtrTribeDetail.setRefreshing(Mode.PULL_FROM_START);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTimer.cancel();
		mTimerTask = null;
		mTaskManager.unregisterListener(this);
	}

	private void showPopMenu(View anchor) {
		if (mPopupView == null) {
			mPopupView = new PopupView(this);
		}
		if (mTribeInfo.collected) {
			mPopupView.setItems(R.array.tribe_detail_menu_collected, this);
		} else {
			mPopupView.setItems(R.array.tribe_detail_menu_uncollected, this);
		}
		mPopupView.showAsDropDown(anchor, false);
	}

	private void updateTopView() {
		mTvTribeName.setText(mTribeInfo.tribeName);
		mTvFollowCount.setText(getString(R.string.tribe_detail_follow_count,
				mTribeInfo.followCount));
		mTvPostCount.setText(getString(R.string.tribe_detail_post_count,
				mTribeInfo.threadCount));
		mTvUpCount.setText(getString(R.string.tribe_detail_up_count,
				mTribeInfo.upCount));
		mTvTribeDesc.setText(mTribeInfo.tribeDesc);
		if (mTribeInfo.icon != null && !mTribeInfo.icon.equals("")) {
			Picasso.with(this).load(mTribeInfo.icon).into(mAvatarView);
		}

	}

	private void fillStickLvlPost() {
		if (mStickLvlPosts == null || mStickLvlPosts.size() <= 0) {
			mLlStickLvlPosts.setVisibility(View.GONE);
			return;
		}
		mLlStickLvlPosts.setVisibility(View.VISIBLE);
		mLlStickLvlPosts.removeAllViews();
		int size = mStickLvlPosts.size();
		if (mStickLvlPosts != null && size > 0) {
			for (int i = 0; i < (size >= 3 ? 3 : size); i++) {
			    mLlStickLvlPosts.addView(getView(i));
			}
		}
	}

	private View getView(int i) {
		CfPost post = mStickLvlPosts.get(i);
		View view = mInflater.inflate(R.layout.item_campus_hot_post_info, null);
		TextView mTvTitle = (TextView) view.findViewById(R.id.tv_post_title);
		AvatarView avatar = (AvatarView) view.findViewById(R.id.iv_avatar);
		ImageView image = (ImageView) view.findViewById(R.id.iv_image);
		ImageView imageFlag = (ImageView) view.findViewById(R.id.iv_image_flag);
		ImageView imageThumbnail = (ImageView) view
				.findViewById(R.id.iv_image_thumbnail);
		FrameLayout mFlVideo = (FrameLayout) view.findViewById(R.id.fl_video);
		FrameLayout mFlAvatar = (FrameLayout) view
				.findViewById(R.id.fl_avatar_bg);
		mTvTitle.setText(post.title);
		Picasso.with(this)
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
			Picasso.with(this).load(post.videoThumbnail).into(imageThumbnail);
			mFlVideo.setVisibility(View.VISIBLE);
			if (post.content == null || post.content.equals("")) {
				String key = post.userInfo.getDisplayName(this);
				String content = getString(R.string.post_video_nocontent_desc,
						key);
				CharSequence coloredText = Utils.getColoredText(content, key,
						0xff78a8e4);
				mTvTitle.setText(coloredText);
			}
			break;
		case CfPost.THREAD_TYPE_VOICE:
			imageFlag.setImageResource(R.drawable.nomal_hot_sound);
			imageFlag.setVisibility(View.VISIBLE);
			if (post.content == null || post.content.equals("")) {
				String key = post.userInfo.getDisplayName(this);
				String content = getString(R.string.post_voice_nocontent_desc,
						key);
				CharSequence coloredText = Utils.getColoredText(content, key,
						0xff78a8e4);
				mTvTitle.setText(coloredText);
			}
			break;

		default:
			if (post.images != null && post.images.size() > 0) {
				Picasso.with(this)
						.load(getSmallImageUrl(post.images.get(0).image))
						.into(image);
				image.setVisibility(View.VISIBLE);
				if (post.content == null || post.content.equals("")) {
					String key = post.userInfo.getDisplayName(this);
					String content = getString(
							R.string.post_normal_nocontent_desc, key);
					CharSequence coloredText = Utils.getColoredText(content,
							key, 0xff78a8e4);
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_follow_with:
			if (!mTribeInfo.collected) {
				followTribe(true);
			}
			if (mTribeInfo.collected) {
				if (!mTribeInfo.hasAssisted) {
					assistTribe(true);
				}
			}
			break;
		case R.id.ll_tribe_famous_person:

			// 部落名人
			Intent intent = new Intent(TribeDetailActivity.this,
					TribeCelebrityActivity.class);
			intent.putExtra(TribeCelebrityActivity.EXT_TRIBE_ID,
					mTribeInfo.tribeId);
			startActivity(intent);
			break;
		case R.id.ll_sticky_post:
			// 热门帖
			Intent hotIntent = new Intent(this, HotPostsActivity.class);
			hotIntent.putExtra(TribeInfo.INTENT_EXTRA_INFO, mTribeInfo);
			startActivity(hotIntent);
			break;
		case R.id.ll_tribe_chat:
			isCollected();
			break;
		case R.id.btn_right:
			showPopMenu(v);
			break;
		case R.id.ll_tribe_detail_header:
			Intent tribeInfointent = new Intent(TribeDetailActivity.this,
					TribeInfoActivity.class);
			tribeInfointent.putExtra(TribeInfo.INTENT_EXTRA_INFO, mTribeInfo);
			startActivity(tribeInfointent);
			break;
		case R.id.rl_hot_post:
			CfPost post = (CfPost) v.getTag();
			showPostDetail(post);
			break;
		case R.id.tv_title:
		    mPtrTribeDetail.setRefreshing(Mode.PULL_FROM_START);
            break;
		default:
			break;
		}
	}

	private void showPostDetail(CfPost post) {
		Intent intent = new Intent(this, ForumPostDetailActivity.class);
		intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, post);
		intent.putExtra(ForumPostDetailActivity.EXTRL_IS_SHOW_TRIBE, false);
		intent.putExtra(ForumPostDetailActivity.EXTRL_ISSHOW_SCHOOL_NAME, true);
		startActivity(intent);
	}

	private void isCollected() {
		if (mTribeInfo.collected) {
			Intent roomIntent = new Intent(this, TribeChatRoomActivity.class);
			roomIntent.putExtra(TribeChatRoomActivity.EXT_TRIBE_DATA,
					mTribeInfo);
			startActivity(roomIntent);
		} else {
			Toast.makeText(this,
					this.getString(R.string.tribe_not_concern_this_tribe),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		if (mTribeId > 0) {
		    new GetTribeInfoTask().executeLong();
            mRefreshTime = System.currentTimeMillis() / 1000;
		}
		fetchPosts(true);
		isShowToast = false;
		
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
	    isShowToast = true;
		fetchPosts(false);
	}

	private void fetchPosts(boolean refresh) {
		int size = mAdapter.getCount();
        long time = refresh || size == 0 ? 0 : (mAdapter.getItem(size - 1).replyTime - 1000);
        if (refresh) {
            mTitleBar.showRefreshRed(false);
            mRefreshTime = System.currentTimeMillis() / 1000;
        }
        if (mTribeInfo != null) {
            new GetTribePostsTask(time).executeLong();
        }
		    
	}

	private void followTribe(boolean follow) {
		new FollowTribeTask(follow).executeLong();
	}

	private void assistTribe(boolean hasAssisted) {
		new AssistTribeTask(hasAssisted).executeLong();
	}

	/**
	 * Assist tribe
	 * <p>
	 * 助力部落
	 * 
	 */
	private class AssistTribeTask extends MsTask {
		private boolean mhasAssisted;

		public AssistTribeTask(boolean hasAssisted) {
			super(TribeDetailActivity.this, MsRequest.TRIBE_ASSIST);
			mhasAssisted = hasAssisted;
		}

		@Override
		protected String buildParams() {
			return "tribe_id=" + mTribeInfo.tribeId;
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			if (response.isSuccessful()) {
				// assist success
				mTribeInfo.hasAssisted = mhasAssisted;
				if (mhasAssisted) {
					mTribeInfo.upCount++;
				}
				updateAssistUI();
			} else {
				response.showInfo(TribeDetailActivity.this,
						response.getFailureDesc(response.code));
			}
		}
	}

	private class FollowTribeTask extends MsTask {

		private boolean mFollow;

		public FollowTribeTask(boolean follow) {
			super(TribeDetailActivity.this,
					follow ? MsRequest.TRIBE_FOLLOW_WITH
							: MsRequest.TRIBE_CANCEL_FOLLOW);
			mFollow = follow;
		}

		@Override
		protected String buildParams() {
			return "tribe_id=" + mTribeInfo.tribeId;
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			if (response.isSuccessful()) {
				mTribeInfo.collected = mFollow;
				if (mFollow) {
					mTribeInfo.followCount++;
				} else {
					mTribeInfo.followCount--;
				}
				updateFollowUI(mFollow);
			}
		}

	}

	private class GetTribePostsTask extends MsTask {

		private long mTime;
		private boolean mIsRefresh;

		public GetTribePostsTask(long time) {
			super(TribeDetailActivity.this, MsRequest.CF_LIST_THREADS_BY_FORUM);
			mIsRefresh = time == 0;
			mTime = time;
		}

		@Override
		protected String buildParams() {
			StringBuilder sb = new StringBuilder();
			sb.append("forum_id=").append(mTribeInfo.tribeFid)
					.append("&forum_type=").append(Forum.TYPE_TRIBE)
					.append("&time=").append(mTime / 1000);
			return sb.toString();
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			mPtrTribeDetail.onRefreshComplete();
			if (!mShouldRefresh && isShowToast) {
			    toast(getString(R.string.tribe_no_more_message));
			}
			if (response.isSuccessful()) {
				ArrayList<CfPost> posts = JsonUtil.getArray(
						response.getJsonArray(), CfPost.TRANSFORMER);
				if (posts.size() < 20) {
					mShouldRefresh = false;
				} else {
					mShouldRefresh = true;
					mPtrTribeDetail.setMode(Mode.BOTH);
				}
				if (posts != null) {
					if (mIsRefresh) {
						new UpdateDbPostInfoTask().execute(posts);
						posts = filterStickPost(posts);
						mAdapter.reset(posts);
			            mHeaderView.setVisibility(View.VISIBLE);
					} else {
						mAdapter.addAll(posts);
					}
				}
			}
		}
	}

	private class GetTribeInfoTask extends MsTask {

		public GetTribeInfoTask() {
			super(TribeDetailActivity.this, MsRequest.TRIBE_GET_INFO_BY_ID);
		}

		@Override
		protected String buildParams() {
			return "tribe_id=" + mTribeId;
		}

		@Override
		protected void onPostExecute(MsResponse response) {
			if (response.isSuccessful()) {
				mTribeInfo = TribeInfo.fromJson(response.getJsonObject());
				fillTribeInfo();
				loadDatas();
			}
		}

	}

	private class UpdateDbPostInfoTask extends
			AsyncTask<ArrayList<CfPost>, Integer, Boolean> {

		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(ArrayList<CfPost>... params) {
			updatePostDbInfo(params[0]);
			return true;
		}

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

	private void updatePostDbInfo(ArrayList<CfPost> posts) {
		DataHelper.deletePostInfo(this, mTribeInfo.tribeFid);
		DataHelper.deleteCacheImage(this, getPostImagesIds(posts));
		DataHelper.deleteVoteInfo(this, getPostVoteIds(posts));
		ArrayList<CachePostInfo> infos = new ArrayList<>();
		for (CfPost post : posts) {
			CachePostInfo info = new CachePostInfo(post);
			long imageId = DataHelper.insertCacheImages(this,
					getCacheImage(post.images));
			long voteId = DataHelper.insertVoteInfo(this,
					getCacheVote(post.vote));
			if (DataHelper.loadCacheUserInfo(this, post.userInfo.userId) != null) {
				DataHelper.updateCacheUserInfo(this, post.userInfo);
			} else {
				DataHelper.insertCacheUserInfo(this, post.userInfo);
			}
			info.imageId = imageId;
			info.optionId = voteId;
			infos.add(info);
		}
		DataHelper.insertPostInfo(this, infos);
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
		if (info.count == 9) {
			info.url9 = images.get(8).image;
		}
		return info;
	}

	@Override
	public void onClick(View view, int pos) {
		Intent intent = new Intent();
		switch (pos) {
		case 1:// normal post
			intent.setClass(this, NormalPostActivity.class);
			intent.putExtra(Forum.INTENT_EXTRA_NAME, getForum());
			startActivityForResult(intent, 0);
			break;
		case 2: // video post
			startVideoPost();
			break;
		case 3: // txt vote
			intent.setClass(this, TxtVotePostActivity.class);
			intent.putExtra(Forum.INTENT_EXTRA_NAME, getForum());
			startActivityForResult(intent, 0);
			break;
		default:
			break;
		}
	}

	private void startVideoPost() {
		QupaiService qupaiService = QupaiManager.getQupaiService(this);

		if (qupaiService == null) {
			Toast.makeText(this,
					this.getString(R.string.tribe_can_not_gain_qupai),
					Toast.LENGTH_LONG).show();
			return;
		}
		AppGlobalSetting sp = new AppGlobalSetting(this);
		MianLiaoApp.sIsGuidShow = sp.getBooleanGlobalItem(
				MianLiaoApp.PREF_VIDEO_EXIST_USER, true);
		qupaiService.showRecordPage(this, RequestCode.RECORDE_SHOW, MianLiaoApp.sIsGuidShow);
		sp.saveGlobalConfigItem(MianLiaoApp.PREF_VIDEO_EXIST_USER, false);
	}




	public void updateAssistUI() {
		if(mTribeInfo.hasAssisted){
			initUp();
		}else{
			
		}
		
	}

	private void initUp(){
		mTvFollow.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.channel_icon_ok, 0, 0, 0);
		mTvFollow.setText(getString(R.string.tribe_assist_ok));
		mTvUpCount.setText(getString(R.string.tribe_detail_up_count, mTribeInfo.upCount));
		mTvFollow.setBackgroundResource(R.drawable.bg_light_assist_day_over);
		mTvFollow.setTextColor(0xff848484);
	}
	
	private void initValueOfUp(){
		mTvFollow.setText(getString(R.string.tribe_up_add));
		mTvFollow.setBackgroundResource(R.drawable.bg_light_yellow_over);
	}
	
	public void updateFollowUI(boolean follow) {
		toast(getString(follow ? R.string.tribe_follow_succ
				: R.string.tribe_cancle_follow_succ));
		mTvFollowCount.setText(getString(R.string.tribe_detail_follow_count,
				mTribeInfo.followCount));
		if (mTribeInfo.collected) {
			if(mTribeInfo.hasAssisted){
				initUp();
			}
			else{
			mTvFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			initValueOfUp();
			}
		} else {
			mTvFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			mTvFollow.setText(getString(R.string.tribe_collected_add));
			mTvFollow.setTextColor(getResources().getColor(R.color.white));
			mTvFollow.setBackgroundResource(R.drawable.bg_light_blue_over);
		}
	}

	private Forum getForum() {
		Forum forum = new Forum();
		forum.id = mTribeInfo.tribeFid;
		forum.type = Forum.TYPE_TRIBE;
		return forum;
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
		for (CfPost post : mStickLvlPosts) {
			posts.remove(post);
		}
		fillStickLvlPost();
		return posts;
	}

	@Override
	public void onItemClick(int position, PopupItem item) {
		switch (position) {
		case 0:
			Intent intent = new Intent(TribeDetailActivity.this,
					TribeInfoActivity.class);
			intent.putExtra(TribeInfo.INTENT_EXTRA_INFO, mTribeInfo);
			startActivity(intent);
			break;
		case 1:
			followTribe(!mTribeInfo.collected);
			break;
		default:
			break;
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RequestCode.RECORDE_SHOW
				&& resultCode == Activity.RESULT_OK) {
			RecordResult result = new RecordResult(data);
			// 得到视频地址，和缩略图地址的数组，返回十张缩略图
			String videoPath = result.getPath();
			String[] thum = result.getThumbnail();
			boolean video = Utils.copy(videoPath, Constant.VIDEOPATH);
			boolean thumbnail = Utils.copy(thum[0], Constant.THUMBPATH);
			if (video && thumbnail) {
				// 设置结果返回数据
				Intent intent = new Intent(this, ForumVideoPostActivity.class);
				intent.putExtra(Forum.INTENT_EXTRA_NAME, getForum());
				intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_LENGTH,
						result.getDuration());
				intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_PATH,
						Constant.VIDEOPATH);
				intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_THUMBNAIL,
						Constant.THUMBPATH);
				// intent.putExtra(TxtVotePostActivity.EXT_OTHER_SCHOOL_ID,
				// mSchoolId);
				startActivity(intent);
			} else {
				Toast.makeText(this,
						this.getString(R.string.tribe_copy_failed),
						Toast.LENGTH_LONG).show();
			}

			QupaiService qupaiService = QupaiManager
					.getQupaiService(this);
			qupaiService.deleteDraft(this, data);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private class GetRefreshPostCount extends MsTask {

        public GetRefreshPostCount() {
            super(TribeDetailActivity.this, MsRequest.CF_REFRESH_COUNT);
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("forum_id=").append(mTribeInfo.tribeFid)
                    .append("&time=").append(mRefreshTime).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                JSONObject jsonObj = response.getJsonObject();
                mCount = jsonObj.optInt("count");
                if (mCount > 0) {
                    mFreshHandler.sendEmptyMessage(UPDATE_POST);
                }
            }
        }

    }
	
	@Override
	protected void onPause() {
	    if (JCVideoPlayer.isVideoFinish) {
	        JCVideoPlayer.releaseAllVideos();
	    }
	    JCVideoPlayer.isVideoFinish = true;
	    super.onPause();
	}
	
}
