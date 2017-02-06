package com.tjut.mianliao.forum;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.NotificationHelper;
import com.tjut.mianliao.NotificationHelper.NotificationType;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.nova.ForumPostAdapter;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class TopicPostActivity extends BaseActivity implements OnRefreshListener2<ListView>{
    
	public static final String TOPIC_ID = "topic_id";
    public static final String POST_ID = "post_id" ;
    public static final String CHANNEL_ID = "channel_id";
    public static final String TOPIC_NAME = "topic_name";
    
    private TopPostAdapter mAdapter;
    private PullToRefreshListView mPtrListView; 
    private NotificationHelper mNotificationHelper;
    private int mPostId;
    private int mTopicId;
    private String mTopicName;
    private ArrayList<CfPost> mPosts = new ArrayList<CfPost>();

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_topic_post;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotificationHelper = NotificationHelper.getInstance(this);
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_topic_post);
        mAdapter = new TopPostAdapter(this);
        mAdapter.showOtherSchool();
        mAdapter.reset(mPosts);
        mAdapter.setTextClickble(true);
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        Intent intent = getIntent();
        mTopicId = intent.getIntExtra(TOPIC_ID, -1);
        mPostId = intent.getIntExtra(POST_ID, 0);
        mTopicName = intent.getStringExtra(TOPIC_NAME);
        getTitleBar().setTitle("#"+mTopicName+"#");
        new getTopicPostTask(0).executeLong();
        mNotificationHelper.clearNotification(NotificationType.TOPIC_RECOMMEND);
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
            super(TopicPostActivity.this, mTopicId > -1 ? MsRequest.TRIBE_LIST_THREAD_BY_TOPIC :
            	MsRequest.CF_TOPIC_THREAD);
            moffset = offset;
        }

        @Override
        protected String buildParams() {
        	if (mTopicId > -1) {
        		return "topic_id=" + mTopicId ;
        	}
            return new StringBuilder("name=").append(Utils.urlEncode(mTopicName))
                    .append("&thread_id=").append(mPostId).append("&offset=").append(moffset)
                    .toString();
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (mPtrListView != null) {
                mPtrListView.onRefreshComplete();
            }
            if (response.isSuccessful()) {
                try {
                    final ArrayList<CfPost> posts = JsonUtil.getArray(response.getJsonArray(),
                            CfPost.TRANSFORMER);
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
    
    private class TopPostAdapter extends ForumPostAdapter{
        public TopPostAdapter(Activity context) {
            super(context);
        }


        @Override
        public View updateFooterView(View view, CfPost post) {
            View footerView = super.updateFooterView(view, post);
            FrameLayout mFlFooter = (FrameLayout) view.findViewById(R.id.fl_footer_content);
            if (mFlFooter != null) {
                if (mFlFooter.getChildCount() > 0) {
                    mFlFooter.removeViewAt(0);
                }
                mInflater.inflate(R.layout.item_tribe_footer_from, mFlFooter);
                TextView tvTribeName = (TextView) mFlFooter.findViewById(R.id.tv_tribe_name);
                if (post.forumName == null || "".equals(post.forumName)) {
                    mFlFooter.setVisibility(View.INVISIBLE);
                } else {
                    mFlFooter.setVisibility(View.VISIBLE);
                    tvTribeName.setText(post.forumName);
                    tvTribeName.setBackgroundResource(R.drawable.bg_tribe_from);
                }
            }
            return footerView;
        }
    }
}
