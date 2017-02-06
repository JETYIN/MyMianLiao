package com.tjut.mianliao.live;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.main.TabFragment;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import java.util.ArrayList;

/**
 * Created by YoopWu on 2016/6/23 0023.
 */
public class LiveTabFragment extends TabFragment implements PullToRefreshBase.OnRefreshListener2,
        AdapterView.OnItemClickListener, View.OnClickListener {

    private LayoutInflater mInflater;

    private ArrayList<LiveInfo> mLiveInfos;

    private PullToRefreshListView mListLive;

    private LiveRoomAdapter mAdapter;

    private MsTask mCurrentTask;


    @Override
    public int getLayoutId() {
        return R.layout.activity_living_rooms;
    }

    @Override
    public boolean isTitleShow() {
        return false;
    }

    @Override
    public int getNaviButtonId() {
        return R.id.nb_live;
    }

    @Override
    public String getName() {
        return "LiveTabFragment";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.findViewById(R.id.tv_start_living).setOnClickListener(this);
        mListLive = (PullToRefreshListView) view.findViewById(R.id.list_lives);
        mListLive.setMode(PullToRefreshBase.Mode.BOTH);
        mListLive.setOnRefreshListener(this);
        mInflater = LayoutInflater.from(getActivity());
        mLiveInfos = new ArrayList<>();
        mAdapter = new LiveRoomAdapter();
        mListLive.setAdapter(mAdapter);
        mListLive.setOnItemClickListener(this);
        mListLive.setRefreshing(PullToRefreshBase.Mode.PULL_FROM_START);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_start_living:
                Intent intent = new Intent(getActivity(), CreateLiveRoomActivity.class);
                startActivity(intent);
                break;
            default:break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchLiveRooms(true);
    }

    @Override
    public void onTabButtonClicked() {
        super.onTabButtonClicked();
        mListLive.setRefreshing(PullToRefreshBase.Mode.PULL_FROM_START);
    }


    private void fetchLiveRooms(boolean refresh) {
        int offset = refresh ? 0 : mLiveInfos.size();
        if (mCurrentTask == null) {
            new GetLivingRooms(offset).executeLong();
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {
        fetchLiveRooms(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        fetchLiveRooms(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LiveInfo live = mLiveInfos.get(position - 1);
        Intent intent = new Intent(getActivity(), LivingActivity.class);
        intent.putExtra(LivingActivity.DATA_LIVE_INFO, live);
        startActivity(intent);
    }


    private class GetLivingRooms extends MsTask {

        private int mOffset;

        public GetLivingRooms(int offset) {
            super(getActivity(), MsRequest.LIST_LIVE);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPreExecute() {
            if (mCurrentTask == null) {
                mCurrentTask = this;
            }
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mListLive.onRefreshComplete();
            mCurrentTask = null;
            if (response.isSuccessful()) {
                ArrayList<LiveInfo> infos = JsonUtil.getArray(response.getJsonArray(), LiveInfo.TRANSFORMER);
                if (infos != null && infos.size() > 0) {
                    if (mOffset == 0) {
                        mLiveInfos.clear();
                    }
                    mLiveInfos.addAll(infos);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class LiveRoomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLiveInfos.size();
        }

        @Override
        public LiveInfo getItem(int position) {
            return mLiveInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_live_room, parent, false);
            }
            LiveInfo live = getItem(position);
            ImageView ivAvatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
            TextView tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            ImageView ivUser = (ImageView) convertView.findViewById(R.id.av_avatar);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
            TextView tvSchool = (TextView) convertView.findViewById(R.id.tv_school);
            tvName.setText(live.nick);
            tvSchool.setText(live.school);
            if (live != null && !TextUtils.isEmpty(live.prevUrl)) {
                Picasso.with(getActivity())
                        .load(live.prevUrl)
                        .placeholder(R.drawable.pic_face_07)
                        .into(ivAvatar);
            }
            if (live != null && !TextUtils.isEmpty(live.avatar)) {
                Picasso.with(getActivity())
                        .load(live.avatar)
                        .into(ivUser);
            }
            tvTitle.setText(live.title);
            return convertView;
        }
    }

}
