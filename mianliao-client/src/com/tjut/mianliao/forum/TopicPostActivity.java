package com.tjut.mianliao.forum;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.nova.TribePostAdapter;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class TopicPostActivity extends BaseActivity implements OnRefreshListener2<ListView> {

    public static final String TOPIC_ID = "topic_id";
    public static final String POST_ID = "post_id";
    public static final String CHANNEL_ID = "channel_id";
    public static final String TOPIC_NAME = "topic_name";

    private TribePostAdapter mAdapter;
    private PullToRefreshListView mPtrListView;
    private NotificationHelper mNotificationHelper;
    private int mPostId;
    private int mTopicId;
    private String mTopicName;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_topic_post;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotificationHelper = NotificationHelper.getInstance(this);
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_topic_post);
        mAdapter = new TribePostAdapter(this);
        mAdapter.setIsShowTribeIndetail(true);
        mAdapter.setActivity(this);
        mAdapter.showOtherSchool();
        mAdapter.setTextClickble(true);
        mAdapter.setTextClickble(true);
        mAdapter.setSpanClickble(true);
        mAdapter.setShowNoContent(false);
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        Intent intent = getIntent();
        mTopicId = intent.getIntExtra(TOPIC_ID, -1);
        mPostId = intent.getIntExtra(POST_ID, 0);
        mTopicName = intent.getStringExtra(TOPIC_NAME);
        if (TextUtils.isEmpty(mTopicName)) {
            mTopicName = "";
        }
        setTitle();
        if (TextUtils.isEmpty(mTopicName)) {
            new GetTopicNameTask().executeQuick();
        }
        new getTopicPostTask(0).executeLong();
        mNotificationHelper.clearNotification(NotificationType.TOPIC_RECOMMEND);
    }

    private void setTitle() {
        if (!TextUtils.isEmpty(mTopicName))
            getTitleBar().setTitle("#" + mTopicName + "#");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPtrListView.setOnRefreshListener((OnRefreshListener2<ListView>) null);
        mPtrListView = null;
        mAdapter.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAdapter.stopVoicePlay();
    }

    private class getTopicPostTask extends MsTask {
        private int moffset;

        public getTopicPostTask(int offset) {
            super(TopicPostActivity.this, mTopicId > -1 ? MsRequest.TRIBE_LIST_THREAD_BY_TOPIC
                    : MsRequest.CF_TOPIC_THREAD);
            moffset = offset;
        }

        @Override
        protected String buildParams() {
            if (mTopicId > -1) {
                return new StringBuilder("topic_id=").append(mTopicId)
                        .append("&offset=").append(moffset)
                        .toString();
            }
            return new StringBuilder("name=").append(Utils.urlEncode(mTopicName))
                    .append("&thread_id=").append(mPostId)
                    .append("&offset=").append(moffset)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (mPtrListView != null) {
                mPtrListView.onRefreshComplete();
            }
            if (response.isSuccessful()) {
                try {
                    final ArrayList<CfPost> posts = JsonUtil.getArray(
                            response.getJsonArray(), CfPost.TRANSFORMER);
                    if (posts != null) {
                        if (moffset <= 0) {
                            mAdapter.reset(posts);
                        } else {
                            mAdapter.addAll(posts);
                        }
                    }
                } catch (Exception e) {
                }

            }
        }
    }
    
    private class GetTopicNameTask extends MsTask {

        public GetTopicNameTask() {
            super(TopicPostActivity.this, MsRequest.CF_TOPIC_BY_ID);
        }
        
        @Override
        protected String buildParams() {
            return "topic_id=" + mTopicId;
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mTopicName = response.getJsonObject().optString("name");
                setTitle();
            }
        }
        
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPost(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchPost(false);
    }

    private void fetchPost(boolean refresh) {
        int size = mAdapter.getCount();
        int offset = refresh ? 0 : size;
        new getTopicPostTask(offset).executeLong();
    }

}
