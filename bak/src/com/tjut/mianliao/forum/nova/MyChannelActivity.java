package com.tjut.mianliao.forum.nova;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.tab.Tab;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TabController.TabListener;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.data.ChannelInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class MyChannelActivity extends BaseActivity implements OnClickListener {

    private PullToRefreshListView mPtrChannel;
    private TabController mTabController;
    private boolean mIsCreatedList = true;
    private ArrayList<ChannelInfo> mChannels = new ArrayList<>();
    private ChannelInfoAdapter mAdapter;
    private int mColor;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_my_channel;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.channel_mine));
        mColor = 0xffb95167;
        mPtrChannel = (PullToRefreshListView) findViewById(R.id.ptrlv_channel_list);
        mTabController = new TabController();
        TextTab create = new TextTab((TextView) findViewById(R.id.tv_channel_created));
        TextTab collected = new TextTab((TextView) findViewById(R.id.tv_channel_collected));
        create.setNightMode(true);
        collected.setNightMode(true);
        collected.setChosen(false);
        mTabController.add(create);
        mTabController.add(collected);
        mTabController.setListener(new TabListener() {

            @Override
            public void onTabSelectionChanged(int index, boolean selected, Tab tab) {
                if (!selected) {
                    return;
                }
                if (index == 0) {
                    mIsCreatedList = true;
                    getChannels(true);
                } else {
                    mIsCreatedList = false;
                    getChannels(false);
                }
            }
        });
        mTabController.select(0);
//        mPtrChannel.setMode(mode)
        mAdapter = new ChannelInfoAdapter();
        mPtrChannel.setAdapter(mAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

    private void getChannels(boolean isCreatedList) {
        new GetChannelListTask(isCreatedList).executeLong();
    }

    private class GetChannelListTask extends MsTask {

        public GetChannelListTask(boolean isCreatedList) {
            super(MyChannelActivity.this, isCreatedList ?
                    MsRequest.MY_CHANNEL : MsRequest.COLLECTION_LIST);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                ArrayList<ChannelInfo> infos = JsonUtil.getArray(
                        response.getJsonArray(), ChannelInfo.TRANSFORMER);
                if (infos != null) {
                    mChannels.clear();
                    mChannels.addAll(infos);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

    }

    private class ChannelInfoAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mChannels.size();
        }

        @Override
        public ChannelInfo getItem(int position) {
            return mChannels.get(position);
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
                view = mInflater.inflate(R.layout.list_item_my_channel, parent, false);
            }

            ChannelInfo channel = getItem(position);

            view.setOnClickListener(MyChannelActivity.this);
            view.setTag(channel);

            ProImageView piv = (ProImageView) view.findViewById(R.id.piv_channel_icon);
            TextView title = (TextView) view.findViewById(R.id.tv_channel_title);
            TextView intro = (TextView) view.findViewById(R.id.tv_channel_intro);
            TextView tvUserCount = (TextView) view.findViewById(R.id.tv_user_count);
            TextView tvMyCount = (TextView) view.findViewById(R.id.tv_my_count);
            CharSequence context;
            if (mIsCreatedList) {
                tvUserCount.setVisibility(View.VISIBLE);
                tvMyCount.setVisibility(View.GONE);
                String count = String.valueOf(channel.getUserCount());
                context = Utils.getColoredText(getString(R.string.channel_my_created_des, count),
                        count, mColor, false);
                tvUserCount.setText(context);
            } else {
                tvMyCount.setVisibility(View.VISIBLE);
                tvUserCount.setVisibility(View.GONE);
                String count = String.valueOf(channel.getMyThreadCount());
                context = Utils.getColoredText(getString(R.string.channel_my_collected_des, count),
                        count, mColor, false);
                tvMyCount.setText(context);
            }
            piv.setImage(channel.icon, R.drawable.ic_ntc_forum);
            title.setText(channel.name);
            intro.setText(channel.getTimeDes());
            if (position == getCount() - 1) {
                view.findViewById(R.id.view_splite).setVisibility(View.GONE);
            }

            return view;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_my_channel_info:
                ChannelInfo channel = (ChannelInfo) v.getTag();
                Intent intent = new Intent(this, ForumChannelDetailActivity.class);
                intent.putExtra(ForumChannelDetailActivity.EXT_DATA, channel);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

}
