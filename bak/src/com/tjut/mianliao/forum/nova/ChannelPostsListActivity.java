package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.forum.CfPost;
import com.tjut.mianliao.theme.ThemeTextView;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class ChannelPostsListActivity extends BaseActivity implements OnClickListener {
    public static final String SEARCH_CHANNEL_WAY = "search_channel_way";
    public static final String SEARCH_CHANNEL_NAME = "search_channel_name";
    public static final String SEARCH_CHANNEL_INFO = "search_channel_info";
 
    private ListView mLvPosts;
    private String mSearchType;
    private ChannelInfoAdapter mAdapter;
    private boolean mIsSearchByContent;
    private ArrayList<ChannelInfo> mChannelInfos;
    private ProImageView mIvChannelIcon;
    private ThemeTextView mTvChannelTitle, mTvChannelContent;
    private ChannelTagInfo mChlTypeInfo;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_channel_post_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchType = getIntent().getStringExtra(SEARCH_CHANNEL_WAY);
        mChlTypeInfo = getIntent().getParcelableExtra(SEARCH_CHANNEL_INFO);
        mLvPosts = (ListView) findViewById(R.id.lv_posts);
        mIvChannelIcon = (ProImageView) findViewById(R.id.iv_channel_icon);
        mTvChannelTitle = (ThemeTextView) findViewById(R.id.tv_channel_title);
        mTvChannelContent = (ThemeTextView) findViewById(R.id.tv_channel_content);
        
        if (mChlTypeInfo != null) {
            mIsSearchByContent = true;
            getTitleBar().setTitle(mChlTypeInfo.name);
            mIvChannelIcon.setImage(mChlTypeInfo.icon,R.drawable.image_tree_hole);
            mTvChannelTitle.setText(mChlTypeInfo.name);
            mTvChannelContent.setText(mChlTypeInfo.nameEn);
            mSearchType = mChlTypeInfo.name;
        } else {
            findViewById(R.id.mrl_top_view).setVisibility(View.GONE);
            setTitle();
        }

        mChannelInfos = new ArrayList<>();
        mAdapter = new ChannelInfoAdapter();
        mLvPosts.setAdapter(mAdapter);
        new SearchChannelTask().executeLong();
        getTitleBar().showProgress();

    }

    private void setTitle() {
        int type = Integer.parseInt(mSearchType);
        if (type == CfPost.THREAD_TYPE_PIC_VOICE) {
            getTitleBar().setTitle(getString(R.string.channel_voice));
        } else if (type == CfPost.THREAD_TYPE_PIC_TXT) {
            getTitleBar().setTitle(getString(R.string.channel_picture));
        } else {
            getTitleBar().setTitle(getString(R.string.channel_text));
        }
    }

    private class SearchChannelTask extends MsTask {

        public SearchChannelTask() {
            super(ChannelPostsListActivity.this, MsRequest.SEARCH);
        }

        @Override
        protected String buildParams() {
            StringBuilder sb = new StringBuilder();
            if (mIsSearchByContent) {
                sb.append("tag=");
            } else {
                sb.append("thread_type=");
            }
            sb.append(Utils.urlEncode(mSearchType));
            return sb.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                ArrayList<ChannelInfo> channelInfos = JsonUtil.getArray(
                        response.getJsonArray(), ChannelInfo.TRANSFORMER);
                mChannelInfos.clear();
                mChannelInfos.addAll(channelInfos);
                mAdapter.notifyDataSetChanged();
            }
        }

    }

    private class ChannelInfoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mChannelInfos.size();
        }

        @Override
        public ChannelInfo getItem(int position) {
            return mChannelInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_channel_night, parent, false);
            }

            ChannelInfo channel = getItem(position);
            view.setOnClickListener(ChannelPostsListActivity.this);
            view.setTag(channel);

            ProImageView piv = (ProImageView) view.findViewById(R.id.piv_channel_icon);
            TextView title = (TextView) view.findViewById(R.id.tv_channel_title);
            TextView intro = (TextView) view.findViewById(R.id.tv_channel_intro);
            piv.setImage(channel.icon, R.drawable.ic_ntc_forum);
            title.setText(channel.name);
            intro.setText(channel.intro);
            if (position == getCount() -1) {
                view.findViewById(R.id.view_split).setVisibility(View.GONE);
            } else {
            	view.findViewById(R.id.view_split).setVisibility(View.VISIBLE);
            }
            return view;
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_channel_neight) {
            showChannelInfoDetail((ChannelInfo) v.getTag());
        }
    }

    private void showChannelInfoDetail(ChannelInfo channel) {
        Intent channelIntent = new Intent(this, ForumChannelDetailActivity.class);
        channelIntent.putExtra(ForumChannelDetailActivity.EXT_DATA, channel);
        startActivity(channelIntent);
    }

}
