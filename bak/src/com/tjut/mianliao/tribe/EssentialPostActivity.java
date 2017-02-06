package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

/**
 * Essential post show activity
 * <p>
 * 精华帖
 * 
 * @author YoopWu
 * 
 */
public class EssentialPostActivity extends BaseActivity implements OnRefreshListener<ListView> {

    public static final String EXT_TRIBE_ID = "ext_tribe_id";

    @ViewInject(R.id.ptr_top_5)
    private PullToRefreshListView mPtrEssentialPosts;

    private EssentialPostAdapter mAdapter;

    private int mTribeId;

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
        getTitleBar().setTitle(R.string.tribe_essential_post);
        mTribeId = getIntent().getIntExtra(EXT_TRIBE_ID, 0);
        mAdapter = new EssentialPostAdapter(this);
        mAdapter.setIsTribePosts(true);
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickbel(true);
        mPtrEssentialPosts.setAdapter(mAdapter);
        mPtrEssentialPosts.setMode(Mode.PULL_FROM_START);
        mPtrEssentialPosts.setOnRefreshListener(this);
        mPtrEssentialPosts.setRefreshing(Mode.PULL_FROM_START);
        checkDayNightUI();
    }
    
    @Override
    public void onPause() {
        mAdapter.stopVoicePlay();
        super.onPause();
    }

    private void checkDayNightUI() {
        if (mIsNightMode) {
            mPtrEssentialPosts.setBackgroundResource(R.drawable.bg);
        }
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

    private class EssentialPostAdapter extends ForumPostAdapter {

        public EssentialPostAdapter(Activity context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            FrameLayout mFlHeader = (FrameLayout) view.findViewById(R.id.fl_header_content);
            mInflater.inflate(R.layout.item_essential_post_header, mFlHeader);
            return view;
        }

    }

    @Override
    public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPost();
    }
}
