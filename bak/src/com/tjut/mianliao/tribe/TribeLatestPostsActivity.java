package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.forum.nova.TribePostAdapter;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

/**
 * Tribe latest posts
 * <p>
 * 部落最新贴
 * 
 * @author YoopWu
 * 
 */
public class TribeLatestPostsActivity extends BaseActivity implements OnRefreshListener<ListView> {

    @ViewInject(R.id.ptr_top_5)
    private PullToRefreshListView mPtrLatestPosts;

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
        getTitleBar().setTitle(R.string.tribe_latest_post_title);
        mAdapter = new TribePostAdapter(this);
        mAdapter.setIsTribePosts(true);
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickbel(true);
        mPtrLatestPosts.setMode(Mode.PULL_FROM_START);
        mPtrLatestPosts.setOnRefreshListener(this);
        mPtrLatestPosts.setAdapter(mAdapter);
        mPtrLatestPosts.setRefreshing(Mode.PULL_FROM_START);
        checkDayNightUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.stopVoicePlay();
    }
    
    private void checkDayNightUI() {
        if (mIsNightMode) {
            mPtrLatestPosts.setBackgroundResource(R.drawable.bg);
        }
    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        new FetchLatestPostTask().executeLong();
    }

    private class FetchLatestPostTask extends MsTask {

        public FetchLatestPostTask() {
            super(TribeLatestPostsActivity.this, MsRequest.TRIBE_LATEST_POST_LIST);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrLatestPosts.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<CfPost> posts = JsonUtil.getArray(response.getJsonArray(), CfPost.TRANSFORMER);
                if (posts != null) {
                    mAdapter.reset(posts);
                }
            }
        }

    }

}
