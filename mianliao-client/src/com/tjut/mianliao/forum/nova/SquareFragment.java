package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.duanqu.qupai.utils.AppGlobalSetting;
//import com.alibaba.sdk.android.AlibabaSDK;
//import com.alibaba.sdk.android.callback.FailureCallback;
import com.duanqu.qupai.sdk.android.QupaiManager;
import com.duanqu.qupai.sdk.android.QupaiService;
//import com.duanqu.qupai.sdk.utils.AppGlobalSetting;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.common.Constant;
import com.tjut.mianliao.common.RecordResult;
import com.tjut.mianliao.common.RequestCode;
import com.tjut.mianliao.component.ArcMenu;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.PopupView.OnItemClickListener;
import com.tjut.mianliao.component.PopupView.PopupItem;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.cache.CacheImageInfo;
import com.tjut.mianliao.data.cache.CachePostInfo;
import com.tjut.mianliao.data.cache.CacheVoteInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.Vote;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.NoContentClickListener;
import com.tjut.mianliao.profile.NewProfileActivity;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.video.JCVideoPlayer;

public class SquareFragment extends Fragment implements OnRefreshListener2<ListView>, OnClickListener,
        OnItemClickListener, NoContentClickListener, MsTaskListener {
    
    private static final int MAX_HOT_POST_COUNT = 3;

    private static final String SP_POST_STREAM = "post_stream";
    protected static final String SP_POST_MINE = "post_mine";

    public PullToRefreshListView mPtrListView;
    private View mHeaderView;
    private PopupView mPopupView;
    private View mRootView;
    private View mViewNoContent;

    private ForumPostAdapter mAdapter;
    private String mSpKey = SP_POST_STREAM;
    private MsTaskManager mTaskManager;

    private int mIdentity;
    private int mSchoolId;
    private String mForumName;

    private ArcMenu mArcMenu;
    private ArrayList<CfPost> mStickLvlPosts;
    private ArrayList<CfPost> mNomalPosts;
    private LayoutInflater mInflater;
    private LinearLayout mLlStickLvlPosts;
    public static long mRefreshTime;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = LayoutInflater.from(getActivity());
        mRootView = inflater.inflate(R.layout.square_post_fragment, container);
        mViewNoContent = inflater.inflate(R.layout.view_no_content, null);
        mViewNoContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });
        JCVideoPlayer.isVideoFinish = true;
        this.initComponents(inflater);
        return mRootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter.hasNewPost()) {
            mPtrListView.getRefreshableView().setSelection(
                    mPtrListView.getRefreshableView().getHeaderViewsCount());
            mAdapter.resetHasPostStatus();
        }
    }

    @Override
    public void onPause() {
        JCVideoPlayer.releaseAllVideos();
        if (JCVideoPlayer.isVideoFinish) {
            JCVideoPlayer.releaseAllVideos();
        }
        JCVideoPlayer.isVideoFinish = true;
        super.onPause();
    }
    
    public void loadPost(int schoolId, String forumName) {
        mSchoolId = schoolId;
        mForumName = forumName;
        mAdapter.setForumName(mForumName);
        loadPosts();
    }
    
    private void reload() {
        mViewNoContent.findViewById(R.id.iv_notice).setVisibility(View.INVISIBLE);
        mViewNoContent.findViewById(R.id.pb_progress).setVisibility(View.VISIBLE);
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }
    
    private void initComponents(LayoutInflater inflater) {
        mPtrListView = (PullToRefreshListView) mRootView.findViewById(R.id.ptrlv_post_stream);
        ListView lvList = mPtrListView.getRefreshableView();

        mHeaderView = inflater.inflate(R.layout.list_header_post_stream, lvList, false);
        mLlStickLvlPosts = (LinearLayout) mHeaderView.findViewById(R.id.ll_top_five);
        lvList.addHeaderView(mHeaderView, null, false);

        mArcMenu = (ArcMenu) mRootView.findViewById(R.id.id_arcmenu);
        
        mTaskManager = MsTaskManager.getInstance(getActivity());
        mTaskManager.registerListener(this);

        mAdapter = new ForumPostAdapter(getActivity());
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickble(true);
        mAdapter.showOtherSchool();
        mAdapter.setOnNoContentListener(this);
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);

        initArcMenuEvent();
    }

    private void initArcMenuEvent() {
        mArcMenu.setOnMenuItemClickListener(new ArcMenu.onMenuItemClickListener() {

            @Override
            public void onClick(View view, int pos) {
                Intent intent = new Intent();
                switch (pos) {
                    case 1:// normal post
                        intent.setClass(getActivity(), NormalPostActivity.class);
                        intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                        intent.putExtra(TxtVotePostActivity.EXT_OTHER_SCHOOL_ID, mSchoolId);
                        startActivityForResult(intent, 0);
                        break;
                    case 2: // txt vote
                        startVideoPost();
                        break;
                    case 3: // pic vote
                    	intent.setClass(getActivity(), TxtVotePostActivity.class);
                    	intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                    	intent.putExtra(TxtVotePostActivity.EXT_OTHER_SCHOOL_ID, mSchoolId);
                    	startActivityForResult(intent, 0);
                        break;
                    default:
                        break;
                }
            }
        });
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


    protected void loadPosts() {
        ArrayList<CfPost> posts = DataHelper.loadPostsInfoBySchoolId(getActivity(), mSchoolId);
        if (posts != null && posts.size() > 0) {
            if (mStickLvlPosts == null) {
                mStickLvlPosts = new ArrayList<>();
            }
            if (mNomalPosts == null) {
                mNomalPosts = new ArrayList<>();
            }
            mStickLvlPosts.clear();
            for (CfPost post : posts) {
                if (post.isStickLvl()) {
                    mStickLvlPosts.add(post);
                } else {
                    mNomalPosts.add(post);
                }
            }
            fillStickLvlPost();
            mAdapter.reset(mNomalPosts);
        }
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

    public void fetchPosts(boolean refresh) {
        int size = 0;
        if (mAdapter.hasData()) {
            size = mAdapter.getCount();
        }
        if (refresh) {
            mRefreshTime = System.currentTimeMillis() / 1000;
            FormOtherSchoolActivity.showRefreshRed(false);
        }
        if (TextUtils.equals(mSpKey, SP_POST_STREAM)) {
            long time = refresh || size == 0 ? 0 : (mAdapter.getItem(size - 1).replyTime - 1000);
            new FetchPostStreamTask(time).executeLong();
        }
    }

    protected void loadPost(String spKey) {
        ArrayList<CfPost> posts = new ArrayList<CfPost>();
        String spValue = DataHelper.getSpForData(getActivity()).getString(spKey, "[]");
        try {
            posts.addAll(JsonUtil.getArray(new JSONArray(spValue), CfPost.TRANSFORMER));
        } catch (JSONException e) {
        }
    }

    private class FetchPostStreamTask extends FetchPostsTask {
        private long mTime;

        public FetchPostStreamTask(long time) {
            super(MsRequest.CF_LIST_THREADS_BY_FORUM, time == 0);
            mTime = time;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("time=").append(mTime / 1000)
                    .append("&forum_type=").append(Forum.TYPE_DEFAULT)
                    .append("&school_id=").append(mSchoolId)
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
            super.onPostExecute(response);
            if (mPtrListView == null) {
                return;
            }
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                final ArrayList<CfPost> posts = JsonUtil.getArray(response.getJsonArray(),
                        CfPost.TRANSFORMER);
                mPtrListView.postDelayed(new Runnable() {
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
    
    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (refreshView == mPtrListView) {
            fetchPosts(true);
            mRefreshTime = System.currentTimeMillis() / 1000;
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (refreshView == mPtrListView) {
            fetchPosts(false);
        }
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
            if(DataHelper.loadCacheUserInfo(getActivity(), post.userInfo.userId) != null) {
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
    
    private void showProfileActivity(UserInfo userInfo) {
        Intent iProfile = new Intent(getActivity(), NewProfileActivity.class);
        iProfile.putExtra(UserInfo.INTENT_EXTRA_INFO, userInfo);
        startActivity(iProfile);
    }

    private void showPopupMenu(View anchor) {
        if (mPopupView == null) {
            mPopupView = new PopupView(getActivity()).setItems(R.array.channel_menu_popup, this);
        }
        mPopupView.showAsDropDown(anchor, true);
    }

    private void showDetailsActivity(CfPost post) {
        Intent iDetails = new Intent(getActivity(), ForumPostDetailActivity.class);
        iDetails.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, post);
        iDetails.putExtra(CfPost.INTENT_EXTRA_NAME, post);
        iDetails.putExtra(Forum.INTENT_EXTRA_ISROAMING, true);
        iDetails.putExtra(ForumPostDetailActivity.EXTRL_IS_SHOW_TRIBE, false);
        iDetails.putExtra(ForumPostDetailActivity.EXTRL_ISSHOW_SCHOOL_NAME, true);
        startActivity(iDetails);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                showPopupMenu(v);
                break;
            case R.id.rl_hot_post:
                CfPost post = (CfPost) v.getTag();
                showPostDetail(post);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position, PopupItem item) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mAdapter.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.RECORDE_SHOW && resultCode == Activity.RESULT_OK) {
            RecordResult result =new RecordResult(data);
            //得到视频地址，和缩略图地址的数组，返回十张缩略图
            String videoPath = result.getPath();
            String [] thum = result.getThumbnail();
            boolean video = Utils.copy(videoPath, Constant.VIDEOPATH);
            boolean thumbnail = Utils.copy(thum[0], Constant.THUMBPATH);
            if (video && thumbnail) {
                // 设置结果返回数据
                Intent intent = new Intent(getActivity(), ForumVideoPostActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_LENGTH, result.getDuration());
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_PATH, Constant.VIDEOPATH);
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_THUMBNAIL, Constant.THUMBPATH);
                intent.putExtra(TxtVotePostActivity.EXT_OTHER_SCHOOL_ID, mSchoolId);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(),"拷贝失败",Toast.LENGTH_LONG).show();
            }
            
            QupaiService qupaiService = QupaiManager
                    .getQupaiService(getActivity());
            qupaiService.deleteDraft(getActivity(), data);
        }
    }

    public int getIdentity() {
        if (mIdentity == -1) {
            mIdentity = Utils.generateIdentify(SquareFragment.class.toString());
        }
        return mIdentity;
    }

    private class UpdateDbPostInfoTask extends AsyncTask<ArrayList<CfPost>, Integer, Boolean>{

        @Override
        protected Boolean doInBackground(ArrayList<CfPost>... params) {
            updatePostDbInfo(params[0]);
            return true;
        }
    }

    @Override
    public void onNoContentClick() {
        reload();
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
            Picasso.with(getActivity()).load(post.videoThumbnail)
                    .into(imageThumbnail);
            mFlVideo.setVisibility(View.VISIBLE);
            if (post.content == null || post.content.equals("")) {
                String key = post.userInfo.getDisplayName(getActivity());
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
                String key = post.userInfo.getDisplayName(getActivity());
                String content = getString(R.string.post_voice_nocontent_desc,
                        key);
                CharSequence coloredText = Utils.getColoredText(content, key,
                        0xff78a8e4);
                mTvTitle.setText(coloredText);
            }
            break;
        case CfPost.THREAD_TYPE_PIC_VOTE:
        case CfPost.THREAD_TYPE_TXT_VOTE:
            imageFlag.setImageResource(R.drawable.nomal_hot_vote);
            imageFlag.setVisibility(View.VISIBLE);
            if (post.content == null || post.content.equals("")) {
                String key = post.userInfo.getDisplayName(getActivity());
                String content = getString(R.string.post_vote_nocontent_desc,
                        key);
                CharSequence coloredText = Utils.getColoredText(content, key,
                        0xff78a8e4);
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
    
    private void fillStickLvlPost() {
        if (mStickLvlPosts == null || mStickLvlPosts.size() <= 0) {
            mLlStickLvlPosts.setVisibility(View.GONE);
            return;
        }
        mLlStickLvlPosts.setVisibility(View.VISIBLE);
        mLlStickLvlPosts.removeAllViews();
        int size = mStickLvlPosts.size();
        if (mStickLvlPosts != null && size > 0) {
            for (int i = 0; i < (size >= MAX_HOT_POST_COUNT ? MAX_HOT_POST_COUNT : size) ; i++) {
                mLlStickLvlPosts.addView(getView(i));
            }
        }
    }
        
    private void showPostDetail(CfPost post) {
        Intent intent = new Intent(getActivity(), ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, post);
        intent.putExtra(ForumPostDetailActivity.EXTRL_IS_SHOW_TRIBE, false);
        getActivity().startActivity(intent);
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
    
    


}
