package com.tjut.mianliao.live;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

import java.util.ArrayList;


/**
 * Created by j_hao on 2016/7/14.
 */
public class LiveGainRankActivity extends BaseActivity implements View.OnClickListener, PullToRefreshBase.OnRefreshListener2 {

    private final static int RANK_TOP_THREE = 0;
    private final static int RANK_NORMAL = 1;
    private final static int VIEW_COUNT = 2;
    @ViewInject(R.id.ptr_list_gain)
    private PullToRefreshListView ptrListView;
    @ViewInject(R.id.tv_total)
    private TextView tvTotaL;
    private ArrayList<LiveRank> listRank;
    private LiveGainRankAdapter mAdapter;
    private int wealth;

    /**
     * item_gain_normal,item_gain_top_three
     **/

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_gain;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        wealth = getIntent().getIntExtra("wealth", 100);
        tvTotaL.setText(Utils.getColoredText(getString(R.string.prof_total_gain, wealth), String.valueOf(wealth), Color.parseColor("#ffad0e")));
        listRank = new ArrayList<>();
        mAdapter = new LiveGainRankAdapter();
        ptrListView.setAdapter(mAdapter);
        ptrListView.setMode(PullToRefreshBase.Mode.BOTH);
        ptrListView.setOnRefreshListener(this);
        getTitleBar().setTitle("直播收获榜");
        new loadRankingTask().executeLong();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new loadRankingTask().executeLong();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            default:
                break;
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase refreshView) {

    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase refreshView) {
        new loadRankingTask().executeLong();
    }

    /**
     * 排行榜适配器
     **/
    private class LiveGainRankAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return listRank.size();
        }

        @Override
        public LiveRank getItem(int position) {
            return listRank.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                case 1:
                case 2:
                    return RANK_TOP_THREE;
            }
            return RANK_NORMAL;
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_COUNT;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LiveRank liveRank = getItem(position);
            int viewType = getItemViewType(position);
            if (convertView == null) {
                convertView = inflateView(viewType, parent);
            }
            ImageView ivAvatar = (ImageView) convertView.findViewById(R.id.av_rank_avatar);
            TextView tvSchool = (TextView) convertView.findViewById(R.id.tv_hosetschool);
            TextView tvWealth = (TextView) convertView.findViewById(R.id.tv_hosetwealth);
            TextView tvName = (TextView) convertView.findViewById(R.id.tv_hosetname);

            tvSchool.setText(liveRank.school);
            tvWealth.setText(String.valueOf(liveRank.amount));
            tvName.setText(liveRank.nick);
            if (!TextUtils.isEmpty(liveRank.avatar)) {
                Picasso.with(LiveGainRankActivity.this)
                        .load(liveRank.avatar)
                        .placeholder(R.drawable.pic_face_02)
                        .into(ivAvatar);
            }
            if (viewType == RANK_TOP_THREE) {
                ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_rank);
                tvWealth.setTextColor(Color.parseColor("#ff29a6"));
                if (position == 0) {
                    imageView.setImageResource(R.drawable.icon_rank_one);
                }
                if (position == 1) {
                    imageView.setImageResource(R.drawable.icon_rank_two);
                }
                if (position == 2) {
                    imageView.setImageResource(R.drawable.icon_rank_three);
                }
            }
            if (viewType == RANK_NORMAL) {
                TextView tvRank = (TextView) convertView.findViewById(R.id.tv_rank);
                tvRank.setText(String.valueOf(position + 1));
            }
            convertView.setTag(liveRank);
            convertView.setOnClickListener(LiveGainRankActivity.this);
            return convertView;
        }
    }


    public View inflateView(int type, ViewGroup parent) {
        switch (type) {
            case RANK_TOP_THREE:
                return mInflater.inflate(R.layout.item_gain_top_threee, parent, false);
            default:
                return mInflater.inflate(R.layout.item_gain_normal_rank, parent, false);
        }
    }

    private class loadRankingTask extends MsTask {

        public loadRankingTask() {

            super(LiveGainRankActivity.this, MsRequest.CONTRIBUTE_RANKING);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            ptrListView.onRefreshComplete();
            if (response.isSuccessful()) {
                listRank = JsonUtil.getArray(response.getJsonArray(), LiveRank.TRANSFORMER);
                if (listRank.size() > 0) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
