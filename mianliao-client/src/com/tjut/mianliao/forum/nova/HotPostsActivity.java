package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.tribe.TribeInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.video.JCVideoPlayer;

public class HotPostsActivity extends BaseActivity implements OnRefreshListener2<ListView> {

    private static final int[] sResFlag = new int[] { R.string.campus_hot_champion,
        R.string.campus_hot_runner_up, R.string.campus_hot_second_runner_up };
    private static final int[] sResIcon = new int[] { R.drawable.square_bg_pic_first,
        R.drawable.square_bg_pic_second, R.drawable.square_bg_pic_third };

    @ViewInject(R.id.ptr_top_5)
    private PullToRefreshListView mPtrHotPosts;

    private HotPostAdapter mAdapter;

    private TribeInfo mTribeInfo;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_hot_top5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JCVideoPlayer.isVideoFinish = true;
        ViewUtils.inject(this);
        mTribeInfo = getIntent().getParcelableExtra(TribeInfo.INTENT_EXTRA_INFO);
        if (mTribeInfo != null) {
            getTitleBar().setTitle(getString(R.string.campus_hot_top_five_title, mTribeInfo.tribeName));
        } else {
            finish();
            return ;
        }
        mAdapter = new HotPostAdapter(this);
        mAdapter.setIsTribePosts(true);
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickble(true);
        mPtrHotPosts.setAdapter(mAdapter);
        mPtrHotPosts.setMode(Mode.BOTH);
        mPtrHotPosts.setOnRefreshListener(this);
        loadData();
    }
    
    private void loadData() {
        ArrayList<CfPost> posts = DataHelper.loadHotPostInfo(this, mTribeInfo.tribeFid);
        if (posts != null && posts.size() > 0) {
            mAdapter.reset(posts);
            fetchPosts(true);
        } else {
            mPtrHotPosts.setRefreshing(Mode.PULL_FROM_START);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (JCVideoPlayer.isVideoFinish) {
            JCVideoPlayer.releaseAllVideos();
        }
        JCVideoPlayer.isVideoFinish = true;
        mAdapter.stopVoicePlay();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
    }

    private class HotPostAdapter extends ForumPostAdapter {

        public HotPostAdapter(Activity context) {
            super(context);
        }

        @Override
        public View updateHeaderView(View view, CfPost post, int position) {
            View mRootView = view;
            FrameLayout mFlHeader = (FrameLayout) view.findViewById(R.id.fl_header_content);
            if (position <= 2) {
                if (mFlHeader != null) {
                    if (mFlHeader.getChildCount() > 0) {
                        mFlHeader.removeViewAt(0);
                    }
                    mInflater.inflate(R.layout.item_hot_flag, mFlHeader);
                    TextView mTvFlag = (TextView) view.findViewById(R.id.tv_flag);
                    mTvFlag.setText(sResFlag[position]);
                    mTvFlag.setCompoundDrawablesWithIntrinsicBounds(sResIcon[position], 0, 0, 0);
                } 
            } else {
                if (mFlHeader != null) {
                    if (mFlHeader.getChildCount() > 0) {
                        mFlHeader.removeViewAt(0);
                    }
                }
            }
            return mRootView;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPosts(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPosts(false);
    }

    private void fetchPosts(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new FetchPostsTask(offset).executeLong();
    }
    
    private class FetchPostsTask extends MsTask {

        private int mOffset;
        
        public FetchPostsTask(int offset) {
            super(HotPostsActivity.this, MsRequest.CF_LIST_HOT_THREADS_BY_FORUM);
            mOffset = offset;
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("forum_type=").append(Forum.TYPE_TRIBE)
                    .append("&forum_id=").append(mTribeInfo.tribeFid)
                    .append("&offset=").append(mOffset)
                    .toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrHotPosts.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<CfPost> posts = JsonUtil.getArray(
                        response.getJsonArray(), CfPost.TRANSFORMER);
                if (mOffset == 0) {
                    mAdapter.reset(posts);
                } else {
                    mAdapter.addAll(posts);
                }
            }
        }
        
    }

}
