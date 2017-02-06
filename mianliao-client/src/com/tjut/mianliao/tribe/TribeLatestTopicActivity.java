package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.TopicInfo;
import com.tjut.mianliao.forum.TopicPostActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class TribeLatestTopicActivity extends BaseActivity implements OnRefreshListener2<ListView> {
    
    @ViewInject(R.id.ptlv_tribe_latest_toipc)
    private PullToRefreshListView mLvTribeLatestTopic;
    
    private ArrayList<TopicInfo> mTopics;
    private TopicListAdapter mAdapter;
    private boolean mIsRefresh = true;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_tribe_latest_topic;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        getTitleBar().setTitle(R.string.tribe_topic);
        mTopics = new ArrayList<TopicInfo>();
        mAdapter = new TopicListAdapter();
        mLvTribeLatestTopic.setOnRefreshListener(this);
        mLvTribeLatestTopic.setMode(Mode.BOTH);
        mLvTribeLatestTopic.setAdapter(mAdapter);
        mLvTribeLatestTopic.setRefreshing(Mode.PULL_FROM_START);
    }
    
    private class TopicListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTopics.size();
        }

        @Override
        public TopicInfo getItem(int position) {
            return mTopics.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
           if (convertView == null) {
               convertView = mInflater.
                       inflate(R.layout.list_item_tribe_latest_topic, parent, false);
               holder = new ViewHolder();
               ViewUtils.inject(holder, convertView);
               convertView.setTag(holder);
           } else {
               holder = (ViewHolder) convertView.getTag();
           }
           TopicInfo topic = getItem(position);
//           if (topic.icon != null && !"".equals(topic.icon)) {
//               Picasso.with(TribeLatestTopicActivity.this).
//                   load(topic.icon).into(holder.mTopicPic);
//           }
           holder.mTvTopicName.setText(topic.name);
           holder.mTopInfo = topic;
           convertView.setOnClickListener(mTopicItemListen);
            return convertView;
        }
        
    }
    
    private OnClickListener mTopicItemListen = new OnClickListener() {
        
        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            TopicInfo topic = holder.mTopInfo;
            Intent intent = new Intent(TribeLatestTopicActivity.this, TopicPostActivity.class);
            intent.putExtra(TopicPostActivity.TOPIC_ID, topic.id);
            intent.putExtra(TopicPostActivity.TOPIC_NAME, topic.name);
            startActivity(intent);
        }
    };
    
    private class ViewHolder {
        @ViewInject(R.id.iv_topic_pic)
        ImageView mTopicPic;
        @ViewInject(R.id.tv_topic_name)
        TextView mTvTopicName;
        TopicInfo mTopInfo;
    }
    
    private class GetLatestTopicTask extends MsTask {
        private int mOffset;

        public GetLatestTopicTask(int offset) {
            super(TribeLatestTopicActivity.this, MsRequest.TRIBE_LATEST_TOPICS);
            mOffset = offset;
        }
        
        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }
        
        
        @Override
        protected void onPostExecute(MsResponse response) {
            mLvTribeLatestTopic.onRefreshComplete();
            if (response.isSuccessful()) {
                if (mIsRefresh) {
                    if (mOffset <= 0) {
                        mTopics.clear();
                    } 
                    ArrayList<TopicInfo> topics = JsonUtil.getArray(
                            response.getJsonArray(), TopicInfo.TRANSFORMER);
                    mTopics.addAll(topics);
                    mIsRefresh = (topics.size() < 20) ? false : true;
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
        
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTopics(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTopics(false);
    }
    
    private void fetchTopics(boolean refresh) {
        int size = mAdapter.getCount();
        int offset = refresh ? 0 : size;
        new GetLatestTopicTask(offset).executeLong();
    }

}
