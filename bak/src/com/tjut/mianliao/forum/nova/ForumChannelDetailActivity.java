package com.tjut.mianliao.forum.nova;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.common.Contant;
import com.tjut.mianliao.common.RecordResult;
import com.tjut.mianliao.common.RequestCode;
import com.tjut.mianliao.component.ArcMenu;
import com.tjut.mianliao.component.ArcMenu.onMenuItemClickListener;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.PopupView.PopupItem;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.Image;
import com.tjut.mianliao.data.cache.CacheImageInfo;
import com.tjut.mianliao.data.cache.CachePostInfo;
import com.tjut.mianliao.data.cache.CacheVoteInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.RemindDialog;
import com.tjut.mianliao.forum.Vote;
import com.tjut.mianliao.forum.nova.ForumPostAdapter.NoContentClickListener;
import com.tjut.mianliao.util.GetImageHelper;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;

public class ForumChannelDetailActivity extends BaseActivity implements
        OnRefreshListener2<ListView>, OnClickListener, OnItemClickListener,
        NoContentClickListener, onMenuItemClickListener, MsTaskListener {

    public static final String EXT_DATA = "extral_data";

    private TextView mTitle, mDesc, mDescTitle, mIntro;
    private PullToRefreshListView mRefreshListView;
    private ForumPostAdapter mPostAdapter;
    private ChannelInfo mChannelInfo;
    private RemindDialog mChannelIntroDialog;
    private ProImageView mIcon;
    private PopupView mPopupView;
    private ArrayList<PopupItem> mPopupItems = new ArrayList<>();
    private PopupAdapter mAadapter;
    private boolean mIsCollected;
    private boolean mIsNightMode;
    private boolean mIsRefreshing;
    private int mHotPostCount;
    private MsTask mCurrentTask;
    private MsTaskManager mTaskManager;

    private View mHeaderView;
    private ArcMenu mArcMenu;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_channel_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArcMenu = (ArcMenu) findViewById(R.id.id_arcmenu);
        mArcMenu.setOnMenuItemClickListener(this);
        mRefreshListView = (PullToRefreshListView) findViewById(R.id.ptr_channel_item);
        mChannelInfo = getIntent().getParcelableExtra(EXT_DATA);
        mIsNightMode = mSettings.isNightMode();
        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);
        checkDayNightUI();
        mPostAdapter = new ForumPostAdapter(this);
        mPostAdapter.setTextClickble(true);
        mPostAdapter.setSpanClickbel(true);
        mPostAdapter.setActivity(this);
        mPostAdapter.setOnNoContentListener(this);
        mRefreshListView.setAdapter(mPostAdapter);
        if (mChannelInfo == null) {
            return;
        }
        mIsCollected = mChannelInfo.collected;
        mRefreshListView.setOnRefreshListener(this);
        mRefreshListView.setMode(Mode.BOTH);
        getTitleBar().setTitle(mChannelInfo.name);
        mAadapter = new PopupAdapter(this);
        loadPostInfo();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (!mPostAdapter.hasNewPost()) {
            return;
        }
        if (mIsNightMode) {
            mRefreshListView.getRefreshableView().setSelection(mHotPostCount);
        } else {
            mRefreshListView.getRefreshableView().setSelection(
                    mRefreshListView.getRefreshableView().getHeaderViewsCount() + mHotPostCount);
        }
        mPostAdapter.resetHasPostStatus();
    }

    private void loadPostInfo() {
        if (mChannelInfo == null) {
            return;
        }
        ArrayList<CfPost> postsInfo = DataHelper.loadPostsInfo(this, mIsNightMode, mChannelInfo.forumId);
        if (postsInfo != null && postsInfo.size() > 0) {
            if (mHeaderView != null) {
                mHeaderView.setVisibility(View.VISIBLE);
            }
            getHotPostCount(postsInfo);
            mPostAdapter.reset(postsInfo);
            fetchPosts(true);
        } else {
            mRefreshListView.setRefreshing(Mode.PULL_FROM_START);
        }
    }
    
    private void reload() {
        fetchPosts(true);
        mIsRefreshing = true;
    }

    public void getHotPostCount(ArrayList<CfPost> posts) {
        mHotPostCount = 0;
        for (CfPost post : posts) {
            if (post.hot == 1) {
                mHotPostCount++;
            }
        }
    }
    
    private void checkDayNightUI() {
        if (!mIsNightMode) {
            ListView listView = mRefreshListView.getRefreshableView();
            mHeaderView = mInflater.inflate(R.layout.channel_post_header, listView, false);
            listView.addHeaderView(mHeaderView);
            mHeaderView.setVisibility(View.INVISIBLE);
            mIcon = (ProImageView) mHeaderView.findViewById(R.id.piv_icon);
            mTitle = (TextView) mHeaderView.findViewById(R.id.tv_channel_title);
            mDesc = (TextView) mHeaderView.findViewById(R.id.tv_desc);
            mDescTitle = (TextView) mHeaderView.findViewById(R.id.tv_desc_main);
            mIntro = (TextView) mHeaderView.findViewById(R.id.tv_intro);
            mIcon.setImage(mChannelInfo.icon, R.drawable.ic_launcher);
            mTitle.setText(mChannelInfo.title);
            mDesc.setText(mChannelInfo.intro);
            mDescTitle.setText(mChannelInfo.ruleTitle);
            mIntro.setText(mChannelInfo.ruleContent);
        } else {
            findViewById(R.id.rl_channel).setBackgroundResource(R.drawable.bg);
            getTitleBar().showRightButton(R.drawable.icon_more_black, this);
            getTitleBar().setRightButtonPadding(10, 0, 10, 0);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mPostAdapter.stopVoicePlay();
    }
    
    @Override
    protected void onDestroy() {
        mPostAdapter.destroy();
        super.onDestroy();
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPosts(true);
        mIsRefreshing = true;
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPosts(false);
        mIsRefreshing = true;
    }
  
    private void fetchPosts(boolean refresh) {
        int offset = refresh ? 0 : mPostAdapter.getCount();
        new FetchPostsTask(offset).executeLong();
    }

    private class FetchPostsTask extends MsTask {

        private int mOffset;

        public FetchPostsTask(int offset) {
            super(ForumChannelDetailActivity.this, MsRequest.LIST_POSTS);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("forum_id=").append(mChannelInfo.forumId)
                    .append("&offset=").append(mOffset)
                    .toString();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCurrentTask = this;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (mRefreshListView != null) {
                if (mIsRefreshing) {
                    mRefreshListView.onRefreshComplete();
                }
                if (mHeaderView != null && !mHeaderView.isShown()) {
                    mHeaderView.postDelayed(new Runnable() {
                        
                        @Override
                        public void run() {
                            mHeaderView.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                }
                if (response.isSuccessful()) {
                    JSONObject json = response.getJsonObject();
                    final ArrayList<CfPost> posts = JsonUtil.getArray(json.optJSONArray("threads"),
                            CfPost.TRANSFORMER);
                    mRefreshListView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mOffset == 0) {
                                new UpdateDbPostInfoTask().execute(posts);
                                if (mIsRefreshing) {
                                    mRefreshListView.postDelayed(new Runnable() {

                                        @Override
                                        public void run() {
                                            mPostAdapter.reset(posts);
                                        }
                                    }, 100);
                                } else {
                                    mPostAdapter.reset(posts);
                                }
                            } else {
                                mPostAdapter.addAll(posts);
                            }
                        }
                    }, 500);
                }
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                showPopupMenu(v);
                break;
            default:
                break;
        }
    }

    private void updatePostDbInfo(ArrayList<CfPost> posts) {
        DataHelper.deletePostInfo(this, mChannelInfo.forumId, mIsNightMode);
        DataHelper.deleteCacheImage(this, getPostImagesIds(posts));
        DataHelper.deleteVoteInfo(this, getPostVoteIds(posts));
        ArrayList<CachePostInfo> infos = new ArrayList<>();
        for (CfPost post : posts) {
            CachePostInfo info = new CachePostInfo(post);
            long imageId = DataHelper.insertCacheImages(this, getCacheImage(post.images));
            long voteId = DataHelper.insertVoteInfo(this, getCacheVote(post.vote));
            if(DataHelper.loadCacheUserInfo(this, post.userInfo.userId, mIsNightMode) != null) {
                DataHelper.updateCacheUserInfo(this, post.userInfo, mIsNightMode);
            } else {
                DataHelper.insertCacheUserInfo(this, post.userInfo, mIsNightMode);
            }
            info.imageId = imageId;
            info.optionId = voteId;
            updateLikedUserInfo(post.likedUsers);
            info.likedUserIds = getLikedUserIds(post.likedUsers);
            info.isNightPost = mIsNightMode;
            infos.add(info);
        }
        DataHelper.insertPostInfo(this, infos);
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

    private ArrayList<String> getPostImagesIds(ArrayList<CfPost> posts) {
        ArrayList<String> ids = new ArrayList<>();
        for (CfPost post : posts) {
            if (post.imageId > 0) {
                ids.add(String.valueOf(post.imageId));
            }
        }
        return ids;
    }

    private String getLikedUserIds(ArrayList<UserInfo> likedUsers) {
        if (likedUsers == null || likedUsers.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (UserInfo info : likedUsers) {
            if (isFirst) {
                sb.append(info.userId);
                isFirst = false;
            } else {
                sb.append(",").append(info.userId);
            }
        }
        return sb.toString();
    }

    private void updateLikedUserInfo(ArrayList<UserInfo> likedUsers) {
        for (UserInfo user : likedUsers) {
            if(DataHelper.loadCacheUserInfo(this, user.userId, mIsNightMode) != null) {
                DataHelper.updateCacheUserInfo(this, user, mIsNightMode);
            } else {
                DataHelper.insertCacheUserInfo(this, user, mIsNightMode);
            }
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

    private void collectChannel() {
        new ChannelCollectTask().executeLong();
    }

    private void showChannelIntroDialog() {
        if (mChannelIntroDialog == null) {
            mChannelIntroDialog = new RemindDialog(this);
            mChannelIntroDialog.setTitle(R.string.channel_intro);
            mChannelIntroDialog.setDialogContent(mChannelInfo.intro);
            mChannelIntroDialog.hideDialogIcon();
        }
        mChannelIntroDialog.show();
    }

    private void showPopupMenu(View anchor) {
        if (mPopupView == null) {
            mPopupView = new PopupView(this);
            mPopupView.setAdapter(mAadapter);
//            mPopupView.setTopIconPosition(1);
            mPopupView.setOnItemClickListener(this);
        }
        mPopupView.showAsDropDown(anchor, false);
        mPopupItems = getItems(mIsCollected ?
                R.array.channel_collected_menu : R.array.channel_uncollect_menu);
        mAadapter.notifyDataSetChanged();
    }

    private ArrayList<PopupItem> getItems(int resId) {
        TypedArray ta = getResources().obtainTypedArray(resId);
        ArrayList<PopupItem> items = new ArrayList<PopupItem>();
        for (int i = 0; i < ta.length(); i += 2) {
            PopupItem item = new PopupItem();
            item.value = ta.getString(i);
            item.iconRes = ta.getResourceId(i + 1, 0);
            items.add(item);
        }
        ta.recycle();
        return items;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPostAdapter.onActivityResult(requestCode, resultCode, data);
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
                startActivity(intent);
            }catch (IOException e){
                Toast.makeText(this,"拷贝失败",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            QupaiService qupaiService = AlibabaSDK
                    .getService(QupaiService.class);
            qupaiService.deleteDraft(this, data);
        } else if (resultCode == RESULT_OK) {
            fetchPosts(true);
        }
    }

    private class PopupAdapter extends ArrayAdapter<PopupItem>{

        public PopupAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public int getCount() {
            return mPopupItems.size();
        }

        @Override
        public PopupItem getItem(int position) {
            return mPopupItems.get(position);
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
                view = mInflater.inflate(R.layout.list_item_news_category, parent, false);
            }
            PopupItem item = getItem(position);
            TextView tvCategory = (TextView) view.findViewById(R.id.tv_news_category);
            tvCategory.setCompoundDrawablesWithIntrinsicBounds(item.iconRes, 0, 0, 0);
            tvCategory.setText(item.value);
            return view;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                collectChannel();
                mPopupView.dismiss();
                break;
            case 1:
                showChannelIntroDialog();
                mPopupView.dismiss();
                break;
            default:
                break;
        }
    }

    private class ChannelCollectTask extends MsTask{

        public ChannelCollectTask() {
            super(ForumChannelDetailActivity.this, MsRequest.CHANNEL_COLLECT);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("forum_id=").append(mChannelInfo.forumId).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mIsCollected = response.getJsonObject().optBoolean("add");
                if (mIsCollected) {
                    toast(getString(R.string.channel_collected_success));
                } else {
                    toast(getString(R.string.news_tst_unfavorite_success));
                }
            }
        }
    }

    private class UpdateDbPostInfoTask extends AsyncTask<ArrayList<CfPost>, Integer, Boolean>{

        @Override
        protected Boolean doInBackground(ArrayList<CfPost>... params) {
            updatePostDbInfo(params[0]);
            getHotPostCount(params[0]);
            return true;
        }
    }

    @Override
    public void onNoContentClick() {
        reload();
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

    private Forum getForum() {
        Forum forum = Forum.DEFAULT_FORUM;
        forum.id = mChannelInfo.forumId;
        return forum;
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
                        Toast.makeText(ForumChannelDetailActivity.this, "onFailure:"+ s + "CODE"+ i,
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

    @Override
    public void onPreExecute(MsTaskType type) {}

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        if (response.isSuccessful()) {
            if (response.value instanceof CfPost) {
                CfPost post = (CfPost) response.value;
                if (post.forumId == mChannelInfo.forumId) {
                    addPost(post);
                }
            }
        }
    }

    private void addPost(CfPost post) {
        mPostAdapter.add(0, post);
        ListView listview = mRefreshListView.getRefreshableView();
        listview.setSelection(listview.getHeaderViewsCount());
    }


}
