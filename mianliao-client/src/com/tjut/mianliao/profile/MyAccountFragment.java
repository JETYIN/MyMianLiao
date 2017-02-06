package com.tjut.mianliao.profile;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.R;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import java.util.ArrayList;

/**
 * Created by j_hao on 2016/7/19.
 */
public class MyAccountFragment extends Fragment implements PullToRefreshBase.OnRefreshListener, View.OnClickListener {
    /**
     * 收益记录
     **/
    public final static int INCOME_RECORD = 0;
    /**
     * 消费记录
     **/
    public final static int CONSUME_RECORD = 1;
    /**
     * 提现记录
     **/
    public final static int WITHDRAW_RECORD = 2;
    /**
     * 充值记录
     **/
    public final static int RECHARGE_RECORD = 3;

    public final static int INCOME_TYPE = 2;
    public final static int CONSUME_TYPE = 1;
    public int type;
    private int mNullViewHeight;
    @ViewInject(R.id.ptrlv_wealth_result)
    private PullToRefreshListView ptrListView;

    private LayoutInflater mInflater;
    private ListAdapter mAdapter;
    private ArrayList<AccountBill> mAcountList = new ArrayList<>();

    public MyAccountFragment() {
    }

    /**
     * 当前的类别
     **/
    public MyAccountFragment(int wealthtype) {
        type = wealthtype;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.layout_listview, container, false);
        ViewUtils.inject(this, view);
        ptrListView.setMode(PullToRefreshBase.Mode.BOTH);
        ptrListView.setOnRefreshListener(this);
        mAdapter = new ListAdapter();
        ptrListView.setAdapter(mAdapter);
        refresh();
        return view;
    }

    @Override
    public void onRefresh(PullToRefreshBase refreshView) {
        fetchAccountBill(false);
    }

    @Override
    public void onClick(View v) {

    }

    public void refresh() {
        fetchAccountBill(true);
    }

    public MyAccountFragment setNullViewHeight(int height) {
        mNullViewHeight = height;
        return this;
    }

    private void fetchAccountBill(boolean refresh) {
        int size = mAcountList.size() - 1;
        int offset = refresh ? 0 : size;
        if (type == RECHARGE_RECORD) {
            new getRechargeRecodsTask(offset).executeLong();
        }
        if (type == WITHDRAW_RECORD) {
            new gatWithdrawalRecods(offset).executeLong();
        }
        if (type == INCOME_RECORD) {
            new getConsumptionRrecords(INCOME_TYPE, offset).executeLong();
        }
        if (type == CONSUME_RECORD) {
            new getConsumptionRrecords(CONSUME_TYPE, offset).executeLong();
        }
    }

    /**
     * 充值记录
     **/
    private class getRechargeRecodsTask extends MsTask {
        int mOffset;

        public getRechargeRecodsTask(int offset) {
            super(getActivity(), MsRequest.LIST_RECHARGE_RECORDS);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<AccountBill> infos = JsonUtil.getArray(
                        response.getJsonArray(), AccountBill.TRANSFORMER);
                mAcountList.clear();
                mAcountList.addAll(infos);
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "充值记录", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 提现记录
     **/
    private class gatWithdrawalRecods extends MsTask {
        int mOffset;

        public gatWithdrawalRecods(int offset) {
            super(getActivity(), MsRequest.LIST_WITHDRAWAL_RECORDS);
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<AccountBill> infos = JsonUtil.getArray(
                        response.getJsonArray(), AccountBill.TRANSFORMER);
                mAcountList.clear();
                mAcountList.addAll(infos);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 消费、收益记录,type:int(2代表收益记录1代表消费记录)
     * offset:int
     **/
    private class getConsumptionRrecords extends MsTask {
        int mType;
        int mOffset;

        public getConsumptionRrecords(int type, int offset) {
            super(getActivity(), MsRequest.LIST_CONSUMPTION_RECORDS);
            mType = type;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset)
                    .append("&type=").append(mType)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                ArrayList<AccountBill> infos = JsonUtil.getArray(
                        response.getJsonArray(), AccountBill.TRANSFORMER);
                mAcountList.clear();
                mAcountList.addAll(infos);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mAcountList.size();
        }

        @Override
        public AccountBill getItem(int position) {
            return mAcountList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AccountBill info = getItem(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_wealth_list, parent, false);
            }
            TextView tvGold = (TextView) convertView.findViewById(R.id.tv_gold_num);
            TextView tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            TextView tvStatus = (TextView) convertView.findViewById(R.id.tv_status);

            if (type == INCOME_RECORD) {
                tvGold.setText(info.consumptionDescription);
                tvTime.setText(String.valueOf(info.consumptionTime));
                if (info.consumptioncoinType == 0) {

                    tvStatus.setText(getActivity().getString(R.string.income_num_kernel, info.consumptionCost));
                }
                if (info.consumptioncoinType == 1) {
                    tvStatus.setText(getActivity().getString(R.string.income_num_gold, info.consumptionCost));
                }
                tvStatus.setTextColor(Color.parseColor("#29ce73"));
            }
            if (type == RECHARGE_RECORD) {
                if (info.type == 0) {
                    tvGold.setText(getActivity().getString(R.string.pecharge_gold_num, info.rechargeAmount));
                }
                tvTime.setText(String.valueOf(info.time));
                if (info.status == 0) {
                    tvStatus.setText("充值中");
                    tvStatus.setTextColor(Color.parseColor("#29ce73"));
                }
                if (info.status == 1) {
                    tvStatus.setText("已到账");
                    tvStatus.setTextColor(Color.parseColor("#969696"));
                }

            }
            if (type == WITHDRAW_RECORD) {
                tvGold.setText(getActivity().getString(R.string.withdraw_num_rmb, info.withdrawalAmount));
                tvTime.setText(String.valueOf(info.withdrawelTime));

                if (info.withdrawelStatus == 0) {
                    tvStatus.setText("提现中");
                    tvStatus.setTextColor(Color.parseColor("#29ce73"));
                }
                if (info.withdrawelStatus == 1) {
                    tvStatus.setText("已到账");
                    tvStatus.setTextColor(Color.parseColor("#969696"));
                }
                if (info.withdrawelStatus == 2) {
                    tvStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                    tvStatus.setText("提现失败");
                    tvStatus.setTextColor(Color.parseColor("#969696"));
                }
            }

            if (type == CONSUME_RECORD) {
                tvGold.setText(info.consumptionDescription);
                tvTime.setText(String.valueOf(info.consumptionTime));
                if (info.consumptioncoinType == 0) {

                    tvStatus.setText(getActivity().getString(R.string.resume_num_kernel, info.consumptionCost));
                }
                if (info.consumptioncoinType == 1) {
                    tvStatus.setText(getActivity().getString(R.string.resume_num_gold, info.consumptionCost));
                }
                tvStatus.setTextColor(Color.parseColor("#969696"));
            }
            return convertView;
        }
    }
}
