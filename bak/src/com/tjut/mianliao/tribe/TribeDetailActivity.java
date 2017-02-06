package com.tjut.mianliao.tribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import u.aly.bu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
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
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.common.Contant;
import com.tjut.mianliao.common.RecordResult;
import com.tjut.mianliao.common.RequestCode;
import com.tjut.mianliao.component.ArcMenu;
import com.tjut.mianliao.component.ArcMenu.onMenuItemClickListener;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.PopupView;
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
import com.tjut.mianliao.forum.nova.ForumVideoPostActivity;
import com.tjut.mianliao.forum.nova.NormalPostActivity;
import com.tjut.mianliao.forum.nova.TxtVotePostActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;

/**
 * Tribe detail(部落详情)
 * 
 * @author YoopWu
 * 
 */
public class TribeDetailActivity extends BaseActivity implements OnRefreshListener2<ListView>, OnClickListener,
        onMenuItemClickListener, OnItemClickListener, MsTaskListener {

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
    @ViewInject(R.id.ll_hot_top5)
    private LinearLayout mLlTop5Post;
    @ViewInject(R.id.bg_view)
    private View mViewArcBg;
    @ViewInject(R.id.id_arcmenu)
    private ArcMenu mArcMenu;
    private AvatarView mAvatarView;
    private TextView mTvFollow;
    private TextView mTvRule;

    private MsTaskManager mTaskManager;
    private NotificationHelper mNotificationHelper;

    private ForumPostAdapter mAdapter;
    private PopupView mPopupView;

    private TribeInfo mTribeInfo;

    private ArrayList<CfPost> mHotTop5Posts;

    private LinearLayout mLlHotPosts;

    private boolean mIsNightMode;
    private boolean mShouldRefresh;
    
    private int mTribeId;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_tribe_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        mTaskManager = MsTaskManager.getInstance(this);
        mNotificationHelper = NotificationHelper.getInstance(this);
        mTaskManager.registerListener(this);
        mHotTop5Posts = new ArrayList<>();
        mTribeInfo = getIntent().getParcelableExtra(TribeInfo.INTENT_EXTRA_INFO);
        mTribeId = getIntent().getIntExtra(EXT_DATA_ID, 0);
        ListView lvList = mPtrTribeDetail.getRefreshableView();
        View headerView = mInflater.inflate(R.layout.tribe_detail_header, null);
        headerView.setOnClickListener(this);
        lvList.addHeaderView(headerView);
        mLlHotPosts = (LinearLayout) headerView.findViewById(R.id.ll_hot_posts);
        mTvTribeName = (TextView) headerView.findViewById(R.id.tv_tribe_name);
        mTvFollowCount = (TextView) headerView.findViewById(R.id.tv_attention_count);
        mTvPostCount = (TextView) headerView.findViewById(R.id.tv_posts_count);
        mTvUpCount = (TextView) headerView.findViewById(R.id.tv_up_count);
        mTvTribeDesc = (TextView) headerView.findViewById(R.id.tv_tribe_desc);
        mAvatarView = (AvatarView) headerView.findViewById(R.id.piv_avatar);
        mLlTop5Post = (LinearLayout) headerView.findViewById(R.id.ll_hot_top5);
        mTvFollow = (TextView) headerView.findViewById(R.id.tv_follow_with);
        mTvRule = (TextView) headerView.findViewById(R.id.tv_rule);
        mArcMenu.setOnMenuItemClickListener(this);
        
        mShouldRefresh = true;
        mAdapter = new ForumPostAdapter(this);
        mAdapter.setIsTribePosts(true);
        mAdapter.setIsShowTribeIndetail(false);
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickbel(true);
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
        checkDayNightUI();
    }

    private void fillTribeInfo() {
        updateTopView();
        getTitleBar().setTitle(mTribeInfo.tribeName);
        if (mTribeInfo.collected) {
            mTvFollow.setText(getString(R.string.tribe_up_add));
            mTvFollow.setBackgroundResource(R.drawable.bg_light_yellow_over);
        }
    }

    private void loadDatas() {
        ArrayList<CfPost> posts = DataHelper.loadPostsInfo(this, false, mTribeInfo.tribeFid);
        if (posts != null && posts.size() > 0) {
            mAdapter.reset(posts);
            fetchPosts(true);
        } else {
            mPtrTribeDetail.setRefreshing(Mode.PULL_FROM_START);
        }
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            findViewById(R.id.fl_content).setBackgroundResource(R.drawable.bg);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        mTvFollowCount.setText(getString(R.string.tribe_detail_follow_count, mTribeInfo.followCount));
        mTvPostCount.setText(getString(R.string.tribe_detail_post_count, mTribeInfo.threadCount));
        mTvUpCount.setText(getString(R.string.tribe_detail_up_count, mTribeInfo.threadCount));
        mTvTribeDesc.setText(mTribeInfo.tribeDesc);
        if (mTribeInfo.icon != null && !mTribeInfo.icon.equals("")) {
            Picasso.with(this).load(mTribeInfo.icon).into(mAvatarView);
        }
        mTvRule.setText(mTribeInfo.rule);
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
            for (int i = 0; i < (size >= 3 ? 3 : size); i++) {
                mLlTop5Post.addView(getView(i));
            }
        }
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
        Picasso.with(this).load(post.userInfo.getAvatar()).into(avatar);
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
            Picasso.with(this)
                .load(post.videoThumbnail)
                .into(imageThumbnail);
            mFlVideo.setVisibility(View.VISIBLE);
            if (post.content == null || post.content.equals("")) {
                String key = post.userInfo.getDisplayName(this);
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
                String key = post.userInfo.getDisplayName(this);
                String content = getString(R.string.post_voice_nocontent_desc, key);
                CharSequence coloredText = Utils.getColoredText(content, key, 0xff78a8e4);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_follow_with:
                if (!mTribeInfo.collected) {
                    followTribe(true);
                } else {
                    assistTribe();
                }
                break;
            case R.id.ll_tribe_famous_person:
                // 部落名人
                Intent intent = new Intent(TribeDetailActivity.this, TribeCelebrityActivity.class);
                intent.putExtra(TribeCelebrityActivity.EXT_TRIBE_ID, mTribeInfo.tribeId);
                startActivity(intent);
                break;
            case R.id.ll_sticky_post:
                // 精华帖
                Intent essentialIntent = new Intent(this, EssentialPostActivity.class);
                essentialIntent.putExtra(EssentialPostActivity.EXT_TRIBE_ID, mTribeInfo.tribeId);
                startActivity(essentialIntent);
                break;
            case R.id.ll_tribe_chat:
               isCollected();
                break;
            case R.id.tv_more_top:
                // 更多热门

                break;
            case R.id.btn_right:
                showPopMenu(v);
                break;
            case R.id.ll_tribe_detail_header:
                Intent tribeInfointent = new Intent(TribeDetailActivity.this, TribeInfoActivity.class);
                tribeInfointent.putExtra(TribeInfo.INTENT_EXTRA_INFO, mTribeInfo);
                startActivity(tribeInfointent);
                break;
            default:
                break;
        }
    }
    

	private void isCollected() {
		if (mTribeInfo.collected) {
			Intent roomIntent = new Intent(this, TribeChatRoomActivity.class);
			roomIntent.putExtra(TribeChatRoomActivity.EXT_TRIBE_DATA,
					mTribeInfo);
			startActivity(roomIntent);
		} else {
			Toast.makeText(this, "你还没有关注本部落哦", Toast.LENGTH_SHORT).show();
		}
	}

    private void startActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPosts(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (mShouldRefresh) {
            fetchPosts(false);
        }  else {
            mPtrTribeDetail.onRefreshComplete();
        }
    }

    private void fetchPosts(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        if (mTribeInfo != null) {
            new GetTribePostsTask(offset).executeLong();
        }
    }

    private void followTribe(boolean follow) {
        new FollowTribeTask(follow).executeLong();
    }

    private void assistTribe() {
        new AssistTribeTask().executeLong();
    }

    /**
     * Assist tribe
     * <p>
     * 助力部落
     * 
     */
    private class AssistTribeTask extends MsTask {

        public AssistTribeTask() {
            super(TribeDetailActivity.this, MsRequest.TRIBE_ASSIST);
        }

        @Override
        protected String buildParams() {
            return "tribe_id=" + mTribeInfo.tribeId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                // assist success
                mTribeInfo.upCount++;
                updateAssistUI();
            }
        }
    }

    private class FollowTribeTask extends MsTask {

        private boolean mFollow;

        public FollowTribeTask(boolean follow) {
            super(TribeDetailActivity.this, follow ? MsRequest.TRIBE_FOLLOW_WITH : MsRequest.TRIBE_CANCEL_FOLLOW);
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

        private int mOffset;
        private boolean mIsRefresh;

        public GetTribePostsTask(int offset) {
            super(TribeDetailActivity.this, MsRequest.LIST_POSTS);
            mIsRefresh = offset == 0;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            sb.append("forum_id=").append(mTribeInfo.tribeFid).append("&offset=").append(mOffset);
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrTribeDetail.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONArray ja;
                try {
                    ja = response.getJsonObject().getJSONArray("threads");
                    ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
                    if (posts.size() < 20) {
                        mShouldRefresh = false;
                        mPtrTribeDetail.setMode(Mode.PULL_DOWN_TO_REFRESH);
                    } else {
                        mShouldRefresh = true;
                        mPtrTribeDetail.setMode(Mode.BOTH);
                    }
                    if (posts != null) {
                        if (mIsRefresh) {
                            new UpdateDbPostInfoTask().execute(posts);
                            posts = filterHotPost(posts);
                            mAdapter.reset(posts);
                        } else {
                            mAdapter.addAll(posts);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private class GetTribeInfoTask extends MsTask{

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
    
    private class UpdateDbPostInfoTask extends AsyncTask<ArrayList<CfPost>, Integer, Boolean> {

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
        DataHelper.deletePostInfo(this, mTribeInfo.tribeFid, mIsNightMode);
        DataHelper.deleteCacheImage(this, getPostImagesIds(posts));
        DataHelper.deleteVoteInfo(this, getPostVoteIds(posts));
        ArrayList<CachePostInfo> infos = new ArrayList<>();
        for (CfPost post : posts) {
            CachePostInfo info = new CachePostInfo(post);
            long imageId = DataHelper.insertCacheImages(this, getCacheImage(post.images));
            long voteId = DataHelper.insertVoteInfo(this, getCacheVote(post.vote));
            if (DataHelper.loadCacheUserInfo(this, post.userInfo.userId, mIsNightMode) != null) {
                DataHelper.updateCacheUserInfo(this, post.userInfo, mIsNightMode);
            } else {
                DataHelper.insertCacheUserInfo(this, post.userInfo, mIsNightMode);
            }
            info.imageId = imageId;
            info.optionId = voteId;
            info.isNightPost = mIsNightMode;
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
        if (info.count >= 9) {
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
        QupaiService qupaiService = AlibabaSDK.getService(QupaiService.class);

        if (qupaiService == null) {
            Toast.makeText(this, "插件没有初始化，无法获取 QupaiService", Toast.LENGTH_LONG).show();
            return;
        }
        qupaiService.showRecordPage(this, RequestCode.RECORDE_SHOW, MianLiaoApp.sIsGuidShow,
                new FailureCallback() {
                    @Override
                    public void onFailure(int i, String s) {
                        Toast.makeText(TribeDetailActivity.this, "onFailure:"+ s + "CODE"+ i,
                                Toast.LENGTH_LONG).show();
                    }
                });
        setGuidShowSp();
    }

    private void setGuidShowSp() {
        AppGlobalSetting sp = new AppGlobalSetting(this);
        sp.saveGlobalConfigItem(MianLiaoApp.PREF_VIDEO_EXIST_USER, false);
        MianLiaoApp.sIsGuidShow = false;
    }


    public void updateAssistUI() {
        mTvFollow.setCompoundDrawablesWithIntrinsicBounds(R.drawable.channel_icon_ok, 0, 0, 0);
        mTvFollow.setText(getString(R.string.tribe_assist_ok));
        mTvTribeDesc.setText(getString(R.string.tribe_up_count_desc, mTribeInfo.upCount));
        if (!mIsNightMode) {
            mTvFollow.setBackgroundResource(R.drawable.bg_light_assist_day_over);
            mTvFollow.setTextColor(0xff848484);
        } else {
            mTvFollow.setBackgroundResource(R.drawable.bg_light_assist_night_over);
            mTvFollow.setTextColor(0xffffffff);
        }
    }

    public void updateFollowUI(boolean follow) {
        toast(getString(follow ? R.string.tribe_follow_succ : R.string.tribe_cancle_follow_succ));
        mTvFollowCount.setText(getString(R.string.tribe_detail_follow_count, mTribeInfo.followCount)); 
        if (mTribeInfo.collected) {
            mTvFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvFollow.setText(getString(R.string.tribe_up_add));
            mTvFollow.setBackgroundResource(R.drawable.bg_light_yellow_over);
        } else {
            mTvFollow.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvFollow.setText(getString(R.string.tribe_collected_add));
            mTvFollow.setBackgroundResource(R.drawable.bg_light_blue_over);
        }
    }

    private Forum getForum() {
        Forum forum = new Forum();
        forum.id = mTribeInfo.tribeFid;
        return forum;
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

    @Override
    public void onItemClick(int position, PopupItem item) {
        switch (position) {
            case 0:
                Intent intent = new Intent(TribeDetailActivity.this, TribeInfoActivity.class);
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
                fetchPosts(true);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.RECORDE_SHOW && resultCode == Activity.RESULT_OK) {
            RecordResult result =new RecordResult(data);
            //得到视频地址，和缩略图地址的数组，返回十张缩略图
            String videoPath = result.getPath();
            String [] thum = result.getThumbnail();
            try{
                Files.move(new File(videoPath), new File(Contant.VIDEOPATH));
                Files.move(new File(thum[0]), new File(Contant.THUMBPATH));
                // 设置结果返回数据
                Intent intent = new Intent(this, ForumVideoPostActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, getForum());
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_LENGTH, result.getDuration());
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_PATH, Contant.VIDEOPATH);
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_THUMBNAIL, Contant.THUMBPATH);
//                intent.putExtra(TxtVotePostActivity.EXT_OTHER_SCHOOL_ID, mSchoolId);
                startActivity(intent);
            }catch (IOException e){
                Toast.makeText(this,"拷贝失败",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            
            QupaiService qupaiService = AlibabaSDK
                    .getService(QupaiService.class);
            qupaiService.deleteDraft(this, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
