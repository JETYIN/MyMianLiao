package com.tjut.mianliao.live;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import android.view.View.OnClickListener;
import java.util.ArrayList;

/**
 * Created by Silva on 2016/6/22.
 */
public class MyLiveListActivity extends BaseActivity implements  OnRefreshListener2<ListView>, OnClickListener{

    @ViewInject(R.id.tv_live_num)
    private TextView mTvLiveNum;
    @ViewInject(R.id.tv_latest)
    private TextView mTvLatest;
    @ViewInject(R.id.tv_hot)
    private TextView mTvHot;
    @ViewInject(R.id.ptrlv_my_live)
    private PullToRefreshListView mPtrMyLive;

    private ArrayList<String> mLives;
    private LiveAdapter mLiveAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_my_live_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        mLives = new ArrayList<>();
        mLiveAdapter = new LiveAdapter();
        mTvHot.setOnClickListener(this);
        mTvLatest.setOnClickListener(this);
        mLives.add("fjsdfkas");
        mLives.add("fjsdfkas");
        mLives.add("fjsdfkas");
        mLives.add("fjsdfkas");
        mPtrMyLive.setMode(PullToRefreshBase.Mode.BOTH);
        mPtrMyLive.setOnRefreshListener(this);
        mPtrMyLive.setAdapter(mLiveAdapter);
    }

    private class LiveAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mLives.size();
        }

        @Override
        public String getItem(int i) {
            return mLives.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = null;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_live, viewGroup, false);
            } else {
                v = view;
            }
            TextView mTvName = (TextView) v.findViewById(R.id.tv_name);
            TextView mTvTime = (TextView) v.findViewById(R.id.tv_time);
            TextView mTvTitle = (TextView) v.findViewById(R.id.tv_live_title);
            AvatarView mIvAvatar = (AvatarView) v.findViewById(R.id.iv_avatar);
            TextView mTvVisitorNum = (TextView) v.findViewById(R.id.tv_visitor_num);

            return v;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case 1:
                break;
            default:
                break;
        }
    }
}
