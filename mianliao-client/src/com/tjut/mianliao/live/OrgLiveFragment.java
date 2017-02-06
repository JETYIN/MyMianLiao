package com.tjut.mianliao.live;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.MianLiaoApp;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ExpandGridView;
import com.tjut.mianliao.data.LiveInfo;
import com.tjut.mianliao.forum.Event;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

import java.util.ArrayList;

/**
 * Created by YoopWu on 2016/7/7 0007.
 */
public class OrgLiveFragment extends Fragment implements PullToRefreshBase.OnRefreshListener,
        View.OnClickListener {

    public static final int TYPE_ORG = 0; // 社团
    public static final int TYPE_CLASS = 1; // 课堂
    public static final int TYPE_PERSON = 2; //个人秀
    private static final String TAG = "OrgLiveFragment";

    @ViewInject(R.id.gv_live)
    private PullToRefreshGridView mGvLive;

    private LayoutInflater mInflater;
    private ArrayList<LiveInfo> mLiveInfos;

    private SearchLiveAdapter mAdapter;

    private int mLiveType;

    private int mNullViewHeight;

    private LivingTabFragrement.onRefreshCompleteListener mListener;
    private GridView mGridView;
    private int lastVisibleItem;
    private long lastScrollTime;
    private int mListViewFirstItem;
    private int mScreenY;

    public OrgLiveFragment() {
    }

    public OrgLiveFragment(int liveType) {
        mLiveType = liveType;
    }

    public OrgLiveFragment setRefreshListener(LivingTabFragrement.onRefreshCompleteListener listener) {
        mListener = listener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLiveInfos = new ArrayList<>();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.logD("Live", "onCreateView was called");
        mInflater = inflater;
        View view = inflater.inflate(R.layout.layout_live_gv,  container, false);
        ViewUtils.inject(this, view);
        mGvLive.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        mGvLive.setOnRefreshListener(this);
        mAdapter = new SearchLiveAdapter();
        mGvLive.setAdapter(mAdapter);
        refresh();
        return view;
    }

    public OrgLiveFragment setNullViewHeight(int height) {
        mNullViewHeight = height;
        return this;
    }

    public void refresh() {
        fetchLives(true);
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        ViewParent parent = mGvLive.getParent();
        if (parent instanceof ViewPager) {
            Utils.logD("LivingTabFragrement", "Parrent measure height = " + ((ViewPager) parent).getHeight());
        }
        fetchLives(false);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.ll_live_info:
                LiveInfo liveInfo = (LiveInfo) v.getTag();
                showLive(liveInfo);
                break;
        }
    }

    private void showLive(LiveInfo liveInfo) {
        if (liveInfo.status == LiveInfo.STATU_LIVING) {
            Intent intent = new Intent(getActivity(), LivingActivity.class);
            intent.putExtra(LivingActivity.DATA_LIVE_INFO, liveInfo);
            startActivity(intent);
        } else if (liveInfo.status == LiveInfo.STATU_REPLAY) {
            Intent intent = new Intent(getActivity(), ReplayLiveActivity.class);
            intent.putExtra(ReplayLiveActivity.EXT_VU, liveInfo.uv);
            intent.putExtra(LivingActivity.DATA_LIVE_INFO, liveInfo);
            startActivity(intent);
        }
    }

    private void fetchLives(boolean refresh) {
        int offset = refresh ? 0 : mLiveInfos.size();
        new GetLiveList(offset).executeLong();
    }

    public void viewFirstItem() {
        if (mGridView != null)
            mGridView.setSelection(0);
    }

    private class GetLiveList extends MsTask{

        private int mOffset;

        public GetLiveList(int offset) {
            super(getActivity(), MsRequest.LIST_LIVE);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("limit=20")
                    .append("&offset=").append(mOffset)
                    .append("&type=").append(mLiveType)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (mListener != null) {
                mListener.onRefreshComplete();
            }
            if (mGvLive == null) {
                return;
            }
            mGvLive.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<LiveInfo> lives = JsonUtil.getArray(response.getJsonArray(),
                        LiveInfo.TRANSFORMER);
                if (lives != null && lives.size() > 0) {
                    if (mOffset == 0) {
                        mLiveInfos.clear();
                    }
                    mLiveInfos.addAll(lives);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void configGridView(){
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (lastVisibleItem != firstVisibleItem) {
                    long nowTime = System.currentTimeMillis();
                    double speed = Math.abs(firstVisibleItem - lastVisibleItem) / ((nowTime - lastScrollTime) / 1000f);
                    lastScrollTime = nowTime;
                    lastVisibleItem = firstVisibleItem;
                    int[] location = new int[2];
                    if (firstVisibleItem != mListViewFirstItem) {
                        if (firstVisibleItem > mListViewFirstItem) {
                            Utils.logD(TAG, "向上滑动");
                        } else {
                            Utils.logD(TAG, "向下滑动");
                        }
                        mListViewFirstItem = firstVisibleItem;
                        if (isTop(mGridView)) {
                            Utils.logD(TAG, "data is to top");
                            mGridView.getParent().getParent().requestDisallowInterceptTouchEvent(true);
                        } else {
                            Utils.logD(TAG, "data is not to top");
                        }
                        mScreenY = location[1];
                    } else {
                        if (mScreenY > location[1]) {
                            Utils.logD(TAG, "向上滑动");
                        } else if (mScreenY < location[1]) {
                            Utils.logD(TAG, "向下滑动");
                            if (isTop(mGridView)) {
                                Utils.logD(TAG, "data is to top");
                                mGridView.getParent().getParent().requestDisallowInterceptTouchEvent(true);
                            } else {
                                Utils.logD(TAG, "data is not to top");
                            }
                        }
                        mScreenY = location[1];
                    }
                }
            }
        });
    }

    private boolean isTop(GridView gridView){
        View firstView=null;
        if(gridView.getCount()==0){
            return true;
        }
        firstView=gridView.getChildAt(0);
        if(firstView!=null){
            if(gridView.getFirstVisiblePosition()==0&&firstView.getTop()==gridView.getListPaddingTop()){
                return true;
            }
        }else{
            return true;
        }

        return false;
    }


    private class SearchLiveAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLiveInfos.size();
        }

        @Override
        public LiveInfo getItem(int position) {
//            return position <= 1 ? null : mLiveInfos.get(position - 2);
            return mLiveInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (mGridView == null) {
                mGridView = (GridView) parent;
                configGridView();
            }
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.grid_item_live, parent, false);
            }
            ImageView mIvCove = (ImageView) convertView.findViewById(R.id.iv_live_cove);
            ImageView mIvHot = (ImageView) convertView.findViewById(R.id.iv_hot_live);
            TextView mTvSchool = (TextView) convertView.findViewById(R.id.tv_school_name);
            TextView mTvUserName = (TextView) convertView.findViewById(R.id.tv_name);
            TextView mTvSpectatorNum = (TextView) convertView.findViewById(R.id.tv_spectator_num);
            TextView mTvContent = (TextView) convertView.findViewById(R.id.tv_live_content);
            TextView tvFlag = (TextView) convertView.findViewById(R.id.tv_live);
            LiveInfo liveInfo = getItem(position);
            if (!TextUtils.isEmpty(liveInfo.prevUrl)) {
                Picasso.with(getActivity()).load(liveInfo.prevUrl).into(mIvCove);
            }
            mTvSchool.setText(liveInfo.school);
            mTvContent.setText(liveInfo.title);
            mTvUserName.setText(liveInfo.nick);
            mTvSpectatorNum.setText(getString(R.string.living_see_people_num, liveInfo.member_numbers));
            convertView.setTag(liveInfo);
            if (liveInfo.status == LiveInfo.STATU_LIVING) {
                tvFlag.setText("直播");
            } else if (liveInfo.status == LiveInfo.STATU_REPLAY) {
                tvFlag.setText("录播");
            } else {
                tvFlag.setText("结束");
            }
            convertView.setOnClickListener(OrgLiveFragment.this);
            return convertView;
        }
    }
}
