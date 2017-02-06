package com.tjut.mianliao.tribe;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.AvatarView;
import com.tjut.mianliao.data.tribe.TribeInfo;

public class MiniGameListActivity extends BaseActivity implements OnRefreshListener2<ListView>{
    
    @ViewInject(R.id.ptlv_mini_game)
    private PullToRefreshListView mLvMiniGame;
    
    private ArrayList<TribeInfo> mGames;
    private MiniGameAdapter mAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_mini_game_list;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        getTitleBar().setTitle(getString(R.string.mini_game));
        mGames = new ArrayList<TribeInfo>();
        mLvMiniGame.setMode(Mode.BOTH);
        mLvMiniGame.setOnRefreshListener(this);
        mAdapter = new MiniGameAdapter();
        mLvMiniGame.setAdapter(mAdapter);
        mLvMiniGame.setRefreshing(Mode.PULL_FROM_START);
    }
    
    private class MiniGameAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mGames.size();
        }

        @Override
        public TribeInfo getItem(int position) {
            return mGames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_mini_game, parent, false);
                holder = new ViewHolder();
                ViewUtils.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            TribeInfo tribe = getItem(position);
            holder.mTribe = tribe;
            convertView.setOnClickListener(mGameOnclick);
            return convertView;
        }
    }
    
    private OnClickListener mGameOnclick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            TribeInfo tribe = holder.mTribe;
        }
    };
    
    private class ViewHolder {
        @ViewInject(R.id.iv_game_logo)
        AvatarView mIvGameLogo;
        @ViewInject(R.id.tv_game_name)
        TextView mTvGameName;
        @ViewInject(R.id.tv_game_people_num)
        TextView mTvGamePeopelNum;
        @ViewInject(R.id.tv_play_game)
        TextView mTvPlayGame;
        TribeInfo mTribe;
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {}

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {}
    
    private void fetchTribes(boolean refresh) {
        int size = mAdapter.getCount();
        int offset = refresh ? 0 : size;
        TribeInfo tribe = new TribeInfo();
        mGames.add(tribe);
        mGames.add(tribe);
        mGames.add(tribe);
        mAdapter.notifyDataSetChanged();
//        new GetTribesTask(offset, MsRequest.TRIBE_HOT_LIST).executeLong();
    }
    
}
