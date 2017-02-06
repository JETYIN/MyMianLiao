package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.SharedPreferences;
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
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.video.JCVideoPlayer;

/**
 * Hot Essential Activity
 * <p>热帖精选</p>
 * Created by YoopWu on 2016/2/23 0023.
 */
public class HotEssentialActivity extends BaseActivity implements OnRefreshListener2<ListView> {

    private static final String SP_HOT_ESSENTIAL_POSTS = "sp_hot_essential_posts";
    
    private static final int[] sResFlag = new int[] { R.string.campus_hot_champion,
        R.string.campus_hot_runner_up, R.string.campus_hot_second_runner_up };
    private static final int[] sResIcon = new int[] { R.drawable.square_bg_pic_first,
        R.drawable.square_bg_pic_second, R.drawable.square_bg_pic_third };
    
    @ViewInject(R.id.ptr_top_5)
    private PullToRefreshListView mPtrHotEssentialPosts;

    private HotPostAdapter mAdapter;
    
    private SharedPreferences mPreferences;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_hot_top5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JCVideoPlayer.isVideoFinish = true;
        ViewUtils.inject(this);
        getTitleBar().setTitle(R.string.tribe_hot_post);
        mPreferences = DataHelper.getSpForData(this);
        mAdapter = new HotPostAdapter(this);
        mAdapter.setIsTribePosts(true);
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickble(true);
        mPtrHotEssentialPosts.setMode(Mode.BOTH);
        mPtrHotEssentialPosts.setOnRefreshListener(this);
        mPtrHotEssentialPosts.setAdapter(mAdapter);
        loadDataFromSp();
    }
    
    private void loadDataFromSp() {
        String json = mPreferences.getString(SP_HOT_ESSENTIAL_POSTS, "[]");
        try {
            JSONArray ja = new JSONArray(json);
            ArrayList<CfPost> posts = JsonUtil.getArray(ja, CfPost.TRANSFORMER);
            if (posts != null && posts.size() > 0) {
                mAdapter.reset(posts);
                mPtrHotEssentialPosts.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fetchPosts(true);
                    }
                }, 1000);
            } else {
                mPtrHotEssentialPosts.setRefreshing(Mode.PULL_FROM_START);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.destroy();
    }

    private void fetchPosts(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new FetchPostsTask(offset).executeLong();
    }

    private class FetchPostsTask extends MsTask {

        private int mOffset;
        
        public FetchPostsTask(int offset) {
            super(HotEssentialActivity.this, MsRequest.THREAD_HOT_POST_LIST );
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return "offset=" + mOffset;

        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
        	mPtrHotEssentialPosts.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<CfPost> posts = JsonUtil.getArray(
                        response.getJsonArray(), CfPost.TRANSFORMER);
                if (mOffset == 0) {
                    mAdapter.reset(posts);
                    saveSpData(response.getJsonArray().toString());
                } else {
                    mAdapter.addAll(posts);
                }
            }
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

    private void saveSpData(String json) {
        mPreferences.edit().putString(SP_HOT_ESSENTIAL_POSTS, json).commit();
    }
    
    private class HotPostAdapter extends ForumPostAdapter {

        public HotPostAdapter(Activity context) {
            super(context);
        }

        @Override
        public View updateHeaderView(View view, CfPost post, int position) {
            View mRootView = view;
            FrameLayout mFlFooter = (FrameLayout) view.findViewById(R.id.fl_footer_content);
            if (mFlFooter != null) {
                if (mFlFooter.getChildCount() > 0) {
                    mFlFooter.removeViewAt(0);
                }
                mInflater.inflate(R.layout.item_tribe_footer_from, mFlFooter);
                TextView tvTribeName = (TextView) mFlFooter.findViewById(R.id.tv_tribe_name);
                if (post.forumName == null || "".equals(post.forumName) || post.tribeId <= 0) {
                    mFlFooter.setVisibility(View.GONE);
                } else {
                    mFlFooter.setVisibility(View.VISIBLE);
                    tvTribeName.setText(post.forumName);
                    tvTribeName.setTag(post);
                    tvTribeName.setOnClickListener(this);
                    tvTribeName.setBackgroundResource(R.drawable.bg_tribe_from);
                }
            }
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

}
