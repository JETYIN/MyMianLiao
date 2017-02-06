package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.os.Bundle;
import android.widget.ListView;

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
import com.tjut.mianliao.forum.nova.ForumPostAdapter.NoContentClickListener;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.video.JCVideoPlayer;

/**
 * Essential post show activity
 * <p>
 * 精华帖
 * 
 * @author YoopWu
 * 
 */
public class EssentialPostActivity extends BaseActivity implements OnRefreshListener<ListView>,
        NoContentClickListener {

    public static final String EXT_TRIBE_ID = "ext_tribe_id";

    @ViewInject(R.id.ptr_top_5)
    private PullToRefreshListView mPtrEssentialPosts;

    private ForumPostAdapter mAdapter;

    private int mTribeId;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_hot_top5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JCVideoPlayer.isVideoFinish = true;
        ViewUtils.inject(this);
        getTitleBar().setTitle(R.string.tribe_essential_post);
        mTribeId = getIntent().getIntExtra(EXT_TRIBE_ID, 0);
        mAdapter = new ForumPostAdapter(this);
        mAdapter.setIsTribePosts(true);
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickble(true);
        mAdapter.setOnNoContentListener(this);
        mPtrEssentialPosts.setAdapter(mAdapter);
        mPtrEssentialPosts.setMode(Mode.PULL_FROM_START);
        mPtrEssentialPosts.setOnRefreshListener(this);
        mPtrEssentialPosts.setRefreshing(Mode.PULL_FROM_START);
    }
    
    @Override
    public void onPause() {
        if (JCVideoPlayer.isVideoFinish) {
            JCVideoPlayer.releaseAllVideos();
        }
        JCVideoPlayer.isVideoFinish = true;
        mAdapter.stopVoicePlay();
        super.onPause();
    }

    private void fetchPost() {
        new FetchEssentialPostTask().executeLong();
    }

    private class FetchEssentialPostTask extends MsTask {

        public FetchEssentialPostTask() {
            super(EssentialPostActivity.this, MsRequest.TRIBE_LIST_PICK_THREADS);
        }

        @Override
        protected String buildParams() {
            return "tribe_id=" + mTribeId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mPtrEssentialPosts.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<CfPost> posts = JsonUtil.getArray(response.getJsonArray(), CfPost.TRANSFORMER);
                if (posts != null) {
                    mAdapter.reset(posts);
                }
            }
        }

    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPost();
    }

    @Override
    public void onNoContentClick() {
        fetchPost();
    }
    
}
