package com.tjut.mianliao.forum.nova;

import java.io.File;
import java.io.IOException;
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
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.common.Contant;
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
import com.tjut.mianliao.profile.ProfileActivity;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.AliImgSpec;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class SquareFragment extends Fragment implements OnRefreshListener2<ListView>, OnClickListener,
        OnItemClickListener, NoContentClickListener {
    
    private static final int MAX_HOT_POST_COUNT = 2;

    private static final String SP_POST_STREAM = "post_stream";
    protected static final String SP_POST_MINE = "post_mine";

    private PullToRefreshListView mPtrListView;
    private View mHeaderView;
    private PopupView mPopupView;
    private View mRootView;
    private View mViewNoContent;

    private ForumPostAdapter mAdapter;
    private String mSpKey = SP_POST_STREAM;

    private int mIdentity;
    private int mSchoolId;
    private boolean mIsNightMode;
    private int mHotPostCount;
    private String mForumName;

    private ArcMenu mArcMenu;
    private ArrayList<CfPost> mHotTop5Posts;
    private ArrayList<CfPost> mNomalPosts;
    private LayoutInflater mInflater;
    private LinearLayout mLlHotPosts;
    private LinearLayout mLlTop5Post;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = LayoutInflater.from(getActivity());
        mRootView = inflater.inflate(R.layout.square_post_fragment, container);
        mIsNightMode = Settings.getInstance(getActivity()).isNightMode();
        mViewNoContent = inflater.inflate(R.layout.view_no_content, null);
        mViewNoContent.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                reload();
            }
        });
        this.initComponents(inflater);
        return mRootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter.hasNewPost()) {
            mPtrListView.getRefreshableView().setSelection(
                    mPtrListView.getRefreshableView().getHeaderViewsCount() + mHotPostCount);
            mAdapter.resetHasPostStatus();
        }
    }

    @Override
    public void onPause() {
        mAdapter.stopVoicePlay();
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
        mLlHotPosts = (LinearLayout) mHeaderView.findViewById(R.id.ll_hot_posts);
        mLlTop5Post = (LinearLayout) mHeaderView.findViewById(R.id.ll_top_five);
        mHeaderView.findViewById(R.id.tv_more_hot).setOnClickListener(this);
        lvList.addHeaderView(mHeaderView, null, false);

        mArcMenu = (ArcMenu) mRootView.findViewById(R.id.id_arcmenu);

        mAdapter = new ForumPostAdapter(getActivity());
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickbel(true);
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
        QupaiService qupaiService = AlibabaSDK.getService(QupaiService.class);

        if (qupaiService == null) {
            Toast.makeText(getActivity(), "插件没有初始化，无法获取 QupaiService", Toast.LENGTH_LONG).show();
            return;
        }
        qupaiService.showRecordPage(this, RequestCode.RECORDE_SHOW, MianLiaoApp.sIsGuidShow,
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

    protected void loadPosts() {
        ArrayList<CfPost> posts = DataHelper.loadPostsInfoBySchoolId(getActivity(), mIsNightMode, mSchoolId);
        if (posts != null && posts.size() > 0) {
            if (mHotTop5Posts == null) {
                mHotTop5Posts = new ArrayList<>();
            }
            if (mNomalPosts == null) {
                mNomalPosts = new ArrayList<>();
            }
            mHotTop5Posts.clear();
            mHotPostCount = 0;
            for (CfPost post : posts) {
                if (post.hot == 1) {
                    mHotPostCount++;
                    mHotTop5Posts.add(post);
                } else {
                    mNomalPosts.add(post);
                }
            }
            fill5TopPost();
            mAdapter.reset(mNomalPosts);
        }
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

    private void fetchPosts(boolean refresh) {
        int size = 0;
        if (mAdapter.hasData()) {
            size = mAdapter.getCount();
        }
        if (TextUtils.equals(mSpKey, SP_POST_STREAM)) {
            long time = refresh || size == 0 ? 0 : (mAdapter.getItem(size - 1).createdOn - 1000);
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
            super(MsRequest.CFC_LIST_TIMELINE, time == 0);
            mTime = time;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("time=").append(mTime / 1000)
                    .append("&other_school_id=").append(mSchoolId)
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
                            new UpdateDbPostInfoTask().execute(posts);
                            getHotPostCount(posts);
                            fill5TopPost();
                            mAdapter.reset(mNomalPosts);
//                            if (posts != null && posts.size() > 0) {
//                                hideNoMessage();
//                            } else {
//                                showNoMessage();
//                            }
                        } else {
                            mAdapter.addAll(posts);
                        }
                    }
                }, 500);
//            } else {
//                showNoMessage();
            }
        }

    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (refreshView == mPtrListView) {
            fetchPosts(true);
        }
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        if (refreshView == mPtrListView) {
            fetchPosts(false);
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
            if(DataHelper.loadCacheUserInfo(getActivity(), post.userInfo.userId, mIsNightMode) != null) {
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
    
    private void showProfileActivity(UserInfo userInfo) {
        Intent iProfile = new Intent(getActivity(), ProfileActivity.class);
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
            case R.id.tv_more_hot:
                Intent hotIntent = new Intent(getActivity(), HotTop5Activity.class);
                hotIntent.putExtra(HotTop5Activity.EXT_HOT_TOP5_POSTS, mHotTop5Posts);
                hotIntent.putExtra(HotTop5Activity.EXT_SCHOOL_NAME, mForumName);
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
            try{
                Files.move(new File(videoPath), new File(Contant.VIDEOPATH));
                Files.move(new File(thum[0]), new File(Contant.THUMBPATH));
                // 设置结果返回数据
                Intent intent = new Intent(getActivity(), ForumVideoPostActivity.class);
                intent.putExtra(Forum.INTENT_EXTRA_NAME, Forum.DEFAULT_FORUM);
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_LENGTH, result.getDuration());
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_PATH, Contant.VIDEOPATH);
                intent.putExtra(ForumVideoPostActivity.EXT_VIDEO_THUMBNAIL, Contant.THUMBPATH);
                intent.putExtra(TxtVotePostActivity.EXT_OTHER_SCHOOL_ID, mSchoolId);
                startActivity(intent);
            }catch (IOException e){
                Toast.makeText(getActivity(),"拷贝失败",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            
            QupaiService qupaiService = AlibabaSDK
                    .getService(QupaiService.class);
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
            getHotPostCount(params[0]);
            return true;
        }

        public void getHotPostCount(ArrayList<CfPost> posts) {
            mHotPostCount = 0;
            for (CfPost post : posts) {
                if (post.hot == 1) {
                    mHotPostCount++;
                }
            }
        }
    }

    @Override
    public void onNoContentClick() {
        reload();
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
    
    public void getHotPostCount(ArrayList<CfPost> posts) {
        if (mHotTop5Posts == null) {
            mHotTop5Posts = new ArrayList<>();
        }
        if (mNomalPosts == null) {
            mNomalPosts = new ArrayList<>();
        }
        mHotTop5Posts.clear();
        mHotPostCount = 0;
        for (CfPost post : posts) {
            if (post.hot == 1) {
                mHotPostCount++;
                mHotTop5Posts.add(post);
            } else {
                mNomalPosts.add(post);
            }
        }
    }
    private void showPostDetail(CfPost post) {
        Intent intent = new Intent(getActivity(), ForumPostDetailActivity.class);
        intent.putExtra(ForumPostDetailActivity.EXTRL_POST_DATA, post);
        getActivity().startActivity(intent);
    }


}
