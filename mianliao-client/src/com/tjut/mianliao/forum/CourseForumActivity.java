package com.tjut.mianliao.forum;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ForumHeader;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.forum.nova.TxtVotePostActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.MsTaskManager;
import com.tjut.mianliao.util.Utils;
import com.tjut.mianliao.video.JCVideoPlayer;

public class CourseForumActivity extends BaseActivity implements
        OnClickListener, OnRefreshListener2<ListView>, MsTaskListener {

    private PullToRefreshListView mPtrPosts;
    private View mTvEmpty;

    private MsTaskManager mTaskManager;
    private ForumHeader mForumHeader;
    private ForumPostAdapter mAdapter;
    private Forum mForum;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_course_forum;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JCVideoPlayer.isVideoFinish = true;
        mForum = getForum();
        if (!mForum.isValid()) {
            toast(R.string.cf_forum_not_exist);
            finish();
            return;
        }

        mTaskManager = MsTaskManager.getInstance(this);
        mTaskManager.registerListener(this);

        mTvEmpty = findViewById(R.id.tv_empty);
        mForumHeader = new ForumHeader(this).setTitleBar(getTitleBar()).setActivity(this);
        mAdapter = new ForumPostAdapter(this);

        mPtrPosts = (PullToRefreshListView) findViewById(R.id.lv_posts);
        mPtrPosts.getRefreshableView().addHeaderView(mForumHeader, null, false);
        mPtrPosts.setMode(Mode.BOTH);
        mPtrPosts.setAdapter(mAdapter);
        mPtrPosts.setOnRefreshListener(this);

        if (mForum.hasValidId()) {
            updateForum(mForum);
            mPtrPosts.setRefreshing(Mode.PULL_FROM_START);
        } else if (mForum.hasGuid()) {
            updateTitle();
            new FindForumTask().executeLong();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            mAdapter.destroy();
        }
        if (mTaskManager != null) {
            mTaskManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mForumHeader.handleActivityResult(requestCode, resultCode, data)) {
            mAdapter.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                showPostActivity();
                break;

            default:
                break;
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


    @Override
    public void onPreExecute(MsTaskType type) {
        switch (type) {
            case FORUM_STICK_POST:
            case FORUM_DELETE_POST:
                getTitleBar().showProgress();
                break;

            default:
                break;
        }
    }

    @Override
    public void onPostExecute(MsTaskType type, MsResponse response) {
        switch (type) {
            case FORUM_STICK_POST:
                getTitleBar().hideProgress();
                break;

            case FORUM_PUBLISH_POST:
                if (response.value instanceof CfPost) {
                    if (mForum.id == ((CfPost) response.value).forumId) {
                        updateCount(1, 0);
                    }
                }
                break;

            case FORUM_DELETE_POST:
                getTitleBar().hideProgress();
                if (response.value instanceof CfPost) {
                    if (mForum.id == ((CfPost) response.value).forumId) {
                        updateCount(-1, ((CfPost) response.value).createdOn);
                    }
                }
                break;

            default:
                break;
        }
    }

    private Forum getForum() {
        Forum forum = getIntent().getParcelableExtra(Forum.INTENT_EXTRA_NAME);
        if (forum == null || !forum.hasValidId()) {
            forum = new Forum();
            forum.name = getString(R.string.loading);
            forum.guid = getIntent().getStringExtra(Forum.INTENT_EXTRA_GUID);
        }
        return forum;
    }

    private void updateForum(Forum forum) {
        mForum = forum;
        updateTitle();
        mForumHeader.setForum(forum);
    }

    private void updateTitle() {
        getTitleBar().showTitleText(mForum.name, null);
        if (!mForum.isUserForum() || mForum.canPost()) {
            getTitleBar().showRightText(R.string.cf_post, this);
        }
    }

    private void updateCount(int delta, long time) {
        mForum.threadCount += delta;
        if (time == 0 || Utils.isToday(time)) {
            mForum.postCountToday += delta;
        }
        mForumHeader.setForum(mForum);
        setResult(RESULT_UPDATED, new Intent().putExtra(Forum.INTENT_EXTRA_NAME, mForum));
    }

    private void showPostActivity() {
        Intent iPost = new Intent(this, TxtVotePostActivity.class);
        iPost.putExtra(Forum.INTENT_EXTRA_NAME, mForum);
        startActivityForResult(iPost, getIdentity());
    }

    private void fetchPosts(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new FetchPostsTask(offset).executeLong();
    }

    private class FetchPostsTask extends MsTask {
        private int mOffset;

        public FetchPostsTask(int offset) {
            super(getApplicationContext(), MsRequest.LIST_POSTS);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            if (mForum.type == Forum.TYPE_DEFAULT) {
                return new StringBuilder("forum_type").append("=")
                        .append(mForum.type).append("&offset=").append(mOffset)
                        .toString();
            } else {
                return new StringBuilder(mForum.getIdName()).append("=")
                        .append(mForum.getId()).append("&offset=").append(mOffset)
                        .toString();
            }
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            mPtrPosts.onRefreshComplete();
            if (response.isSuccessful()) {
                JSONObject json = response.getJsonObject();

                Forum forum = Forum.fromJson(json);
                if (forum != null) {
                    updateForum(forum);
                }

                ArrayList<CfPost> posts = JsonUtil.getArray(
                        json.optJSONArray("threads"), CfPost.TRANSFORMER);
                if (mOffset == 0) {
                    mAdapter.reset(posts);
                    mTvEmpty.setVisibility(mAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    mAdapter.addAll(posts);
                }
            } else {
                response.showFailInfo(getRefContext(), R.string.cf_failed_to_get_post);
            }
        }
    }

    private class FindForumTask extends MsTask {

        public FindForumTask() {
            super(getApplicationContext(), MsRequest.FIND_FORUM_BY_ID);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("guid=").append(mForum.guid).toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            Forum forum = response.isSuccessful()
                    ? Forum.fromJson(response.getJsonObject()) : null;
            if (forum == null) {
                toast(R.string.cf_forum_not_exist);
            } else {
                updateForum(forum);
                mPtrPosts.setRefreshing(Mode.PULL_FROM_START);
            }
        }
    }
    
    @Override
    protected void onPause() {
        if (JCVideoPlayer.isVideoFinish) {
            JCVideoPlayer.releaseAllVideos();
        }
        JCVideoPlayer.isVideoFinish = true;
        super.onPause();
    }
}
