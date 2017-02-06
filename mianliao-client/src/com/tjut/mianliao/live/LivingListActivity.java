package com.tjut.mianliao.live;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.data.LiveTopic;
import com.tjut.mianliao.data.SchoolInfo;
import com.tjut.mianliao.forum.TopicInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;

import java.util.ArrayList;

/**
 * Created by Silva on 2016/7/7.
 */
public class LivingListActivity extends BaseActivity implements View.OnClickListener,
        OnRefreshListener2<GridView>{

    public static final String LIVE_SCHOOL_INFO_ID =  "live_school_info_id";
    public static final String LIVE_SCHOOL_INFO_NAME =  "live_school_info_name";
    public static final String LIVE_TOPIC_INFO =  "live_topic_info";
    private static final int LIVE_TYPE_SCHOOL = 0;
    private static final int LIVE_TYPE_TOPIC = 1;

    @ViewInject(R.id.ptrg_search_living_list)
    private PullToRefreshGridView mGvLive;
    @ViewInject(R.id.ll_nothing)
    private LinearLayout mLlNothing;

    private int mLiveType = -1;
    private LiveTopic mTopicInfo;
    private SchoolInfo mSchoolInfo;

    private ArrayList<LiveInfo> mSearchLives;
    private SearchLiveAdapter mAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_living_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mSchoolInfo = new SchoolInfo();
        mSchoolInfo.schoolId = getIntent().getIntExtra(LIVE_SCHOOL_INFO_ID, 0);
        mSchoolInfo.name = getIntent().getStringExtra(LIVE_SCHOOL_INFO_NAME);
        mTopicInfo = getIntent().getParcelableExtra(LIVE_TOPIC_INFO);
        getTitleBar().showLeftButton(R.drawable.botton_bg_arrow, this);
        mSearchLives = new ArrayList<>();
        mAdapter = new SearchLiveAdapter();
        mGvLive.setMode(PullToRefreshBase.Mode.BOTH);
        mGvLive.setOnRefreshListener(this);
        mGvLive.setAdapter(mAdapter);
        if (mSchoolInfo.schoolId != 0) {
            mLiveType = LIVE_TYPE_SCHOOL;
            getTitleBar().setTitle(mSchoolInfo.name);
            fetchLiveRoom(true);
        } else if (mTopicInfo != null) {
            mLiveType = LIVE_TYPE_TOPIC;
            getTitleBar().setTitle("#" + mTopicInfo.name + "#");
            fetchLiveRoom(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_live_info:
                LiveInfo liveInfo = (LiveInfo) view.getTag();
                showLive(liveInfo);
                break;
            case R.id.btn_left:
                finish();
                break;
            default:
                break;
        }

    }

    private void showLive(LiveInfo liveInfo) {
        if (liveInfo.status == LiveInfo.STATU_LIVE_END) {
            Toast.makeText(MianLiaoApp.getAppContext(), "直播已结束", Toast.LENGTH_SHORT).show();
        } else if (liveInfo.status == LiveInfo.STATU_LIVING) {
            Intent intent = new Intent(LivingListActivity.this, LivingActivity.class);
            intent.putExtra(LivingActivity.DATA_LIVE_INFO, liveInfo);
            startActivity(intent);
        } else if (liveInfo.status == LiveInfo.STATU_REPLAY) {
            Intent intent = new Intent(LivingListActivity.this, ReplayLiveActivity.class);
            intent.putExtra(ReplayLiveActivity.EXT_VU, liveInfo.uv);
            intent.putExtra(LivingActivity.DATA_LIVE_INFO, liveInfo);
            startActivity(intent);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
        fetchLiveRoom(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
        fetchLiveRoom(false);
    }

    private class SearchLiveAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSearchLives.size();
        }

        @Override
        public LiveInfo getItem(int position) {
            return mSearchLives.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.grid_item_live, parent, false);
            ImageView mIvCove = (ImageView) view.findViewById(R.id.iv_live_cove);
            ImageView mIvHot = (ImageView) view.findViewById(R.id.iv_hot_live);
            TextView mTvSchool = (TextView) view.findViewById(R.id.tv_school_name);
            TextView mTvUserName = (TextView) view.findViewById(R.id.tv_name);
            TextView mTvSpectatorNum = (TextView) view.findViewById(R.id.tv_spectator_num);
            TextView mTvContent = (TextView) view.findViewById(R.id.tv_live_content);
            TextView tvFlag = (TextView) convertView.findViewById(R.id.tv_live);
            LiveInfo liveInfo = getItem(position);
            if (liveInfo.prevUrl != null && !"".equals(liveInfo.prevUrl)) {
                Picasso.with(LivingListActivity.this).load(liveInfo.prevUrl).into(mIvCove);
            }
            mTvSchool.setText(liveInfo.school);
            mTvUserName.setText(liveInfo.nick);
            mTvContent.setText(liveInfo.title);
            mTvSpectatorNum.setText(getString(R.string.living_see_people_num, liveInfo.member_numbers));
            if (liveInfo.status == LiveInfo.STATU_LIVING) {
                tvFlag.setText("直播");
            } else if (liveInfo.status == LiveInfo.STATU_REPLAY) {
                tvFlag.setText("录播");
            } else {
                tvFlag.setText("结束");
            }
            view.setTag(liveInfo);
            view.setOnClickListener(LivingListActivity.this);
            return view;
        }

    }


    private class GetLivingRooms extends MsTask {

        private int mOffset;

        public GetLivingRooms(int offset) {
            super(LivingListActivity.this, MsRequest.LIST_LIVE);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            if (mLiveType == 0) {
                return new StringBuilder("offset=").append(mOffset).append("&school_id=").append(mSchoolInfo.schoolId).toString();
            } else if (mLiveType == 1) {
                return new StringBuilder("offset=").append(mOffset).append("&topic_id=").append(mTopicInfo.id).toString();
            } else {
                return null;
            }
        }


        @Override
        protected void onPostExecute(MsResponse response) {
            mGvLive.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<LiveInfo> infos = JsonUtil.getArray(response.getJsonArray(), LiveInfo.TRANSFORMER);
                if (infos != null && infos.size() > 0) {
                    mLlNothing.setVisibility(View.GONE);
                    if (mOffset <= 0) {
                        mSearchLives.clear();
                    }
                    mSearchLives.addAll(infos);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void fetchLiveRoom(boolean refresh) {
        int offset = refresh ? 0 : mAdapter.getCount();
        new GetLivingRooms(offset).executeLong();
    }
}
