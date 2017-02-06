package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.os.Bundle;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.TribePostAdapter;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

/**
 * Hot Essential Activity
 * <p>热帖精选</p>
 * Created by YoopWu on 2016/2/23 0023.
 */
public class HotEssentialActivity extends BaseActivity implements OnRefreshListener{

    @ViewInject(R.id.ptr_top_5)
    private PullToRefreshListView mPtrHotEssentialPosts;

    private TribePostAdapter mAdapter;

    private boolean mIsNightMode;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_hot_top5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mIsNightMode = Settings.getInstance(this).isNightMode();
        getTitleBar().setTitle(R.string.tribe_hot_post);
        mAdapter = new TribePostAdapter(this);
        mAdapter.setIsTribePosts(true);
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickbel(true);
        mPtrHotEssentialPosts.setMode(Mode.PULL_FROM_START);
        mPtrHotEssentialPosts.setOnRefreshListener(this);
        mPtrHotEssentialPosts.setAdapter(mAdapter);
        mPtrHotEssentialPosts.setRefreshing(Mode.PULL_FROM_START);
        checkDayNightUI();
    }

    @Override
    public void onPause() {
        mAdapter.stopVoicePlay();
        super.onPause();
    }
    
    private void checkDayNightUI() {
        if (mIsNightMode) {
            mPtrHotEssentialPosts.setBackgroundResource(R.drawable.bg);
        }
    }

    private void fetchPosts() {
        new FetchPostsTask().executeLong();
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        fetchPosts();
    }

    private class FetchPostsTask extends MsTask {

        public FetchPostsTask() {
            super(HotEssentialActivity.this, MsRequest.TRIBE_LIST_HOT_THREADS);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
        	mPtrHotEssentialPosts.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<CfPost> posts = JsonUtil.getArray(
                        response.getJsonArray(), CfPost.TRANSFORMER);
                if (posts != null) {
                    mAdapter.reset(posts);
                }
            }
        }
    }
}
