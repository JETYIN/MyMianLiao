package com.tjut.mianliao.explore;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.data.explore.TradeInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class TradeRecordActivity extends BaseActivity implements OnRefreshListener2<ListView> {

    private PullToRefreshListView mPtrListView;
    private ArrayList<TradeInfo> mTradeInfos;
    private TradeInfoAdapter mAdapter;

    @Override
    protected int getLayoutResID() {
        return R.layout.main_tab_news;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.emo_recharge_record);
        getTitleBar().showProgress();
        mTradeInfos = new ArrayList<TradeInfo>();
        mPtrListView = (PullToRefreshListView) findViewById(R.id.ptrlv_news);
        mAdapter = new TradeInfoAdapter();
        mPtrListView.setMode(Mode.BOTH);
        mPtrListView.setOnRefreshListener(this);
        mPtrListView.setAdapter(mAdapter);
        mPtrListView.setRefreshing(Mode.PULL_FROM_START);
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTradeList(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        fetchTradeList(false);
    }

    private void fetchTradeList(boolean refresh) {
        new FetchTradeRecordTask(refresh ? 0 : mAdapter.getCount()).executeLong();
    }

    private class FetchTradeRecordTask extends MsTask {

        private int mOffset;

        public FetchTradeRecordTask(int offset) {
            super(TradeRecordActivity.this, MsRequest.TRADE_TRADE_LIST);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return "offset=" + mOffset;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                ArrayList<TradeInfo> infos = JsonUtil.getArray(
                        response.getJsonArray(), TradeInfo.TRANSFORMER);
                if (infos != null) {
                    if (mOffset > 0) {
                        mTradeInfos.addAll(infos);
                    } else {
                        mTradeInfos = infos;
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class TradeInfoAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mTradeInfos.size();
        }

        @Override
        public TradeInfo getItem(int position) {
            return mTradeInfos.get(position);
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
                view = mInflater.inflate(R.layout.list_item_trade_record, parent, false);
            }
            TradeInfo info = getItem(position);
            TextView tvTime = (TextView) view.findViewById(R.id.tv_trade_time);
            ProImageView pivIcon = (ProImageView) view.findViewById(R.id.piv_trade_icon);
            TextView tvName = (TextView) view.findViewById(R.id.tv_trade_name);
            TextView tvIntro = (TextView) view.findViewById(R.id.tv_trade_intro);
            pivIcon.setImage(info.icon, R.drawable.bg_img_loading);
            tvTime.setText(Utils.getTimeString(4, info.time));
            tvName.setText(info.name);
            tvIntro.setText(info.note);
            return view;
        }
    }

}
