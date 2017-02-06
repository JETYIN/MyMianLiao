package com.tjut.mianliao.explore;

import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.explore.RechargeInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.theme.ThemeLineView;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class GoldDepositsActivity extends BaseActivity implements OnClickListener {

    public static final String EXT_RECHARGE_INFO = "ext_recharge_info";
    public static final String EXT_USER_INFO = "ext_user_info";
    public static final String EXT_BUY_INFO = "ext_buy_info";
    private static final int REQUEST_CODE = 100;
    private static final int REQUEST_RECHARGE_CODE = 101;

    private AccountInfo mAccountInfo;
    private TextView mTvBalance;
    private ListView mLvRecharge;
    private RechargeInfoAdapter mAdapter;
    private LinearLayout mLlTop;
    private ArrayList<RechargeInfo> mRechargeInfos;
    private UserInfo mUserInfo;
    private boolean mIsGetUserInfo, mIsGetInfoSuccess;

    private Settings mSettings;
    private boolean mIsNightMode;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_gold_deposits;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mIsNightMode = mSettings.isNightMode();

        getTitleBar().setTitle("面聊金币充值");
        getTitleBar().showProgress();
        mRechargeInfos = new ArrayList<RechargeInfo>();
        mAccountInfo = AccountInfo.getInstance(this);
        mUserInfo = mAccountInfo.getUserInfo();
        mTvBalance = (TextView) findViewById(R.id.tv_count_balance);
        mLvRecharge = (ListView) findViewById(R.id.lv_recharge_info);
        mLlTop = (LinearLayout) findViewById(R.id.ll_top);
        updateAccountInfo();
        mAdapter = new RechargeInfoAdapter();
        mLvRecharge.setAdapter(mAdapter);
        getRechargeInfo();
        getUserInfo();

        if (mIsNightMode) {
            findViewById(R.id.ll_gold_dep).setBackgroundResource(R.drawable.bg);
            mLvRecharge.setBackgroundColor(Color.TRANSPARENT);
        } else {
            findViewById(R.id.ll_gold_dep).setBackgroundColor(0XFFF2F2F2);
            mLvRecharge.setBackgroundColor(Color.WHITE);
        }
    }

    private void updateAccountInfo() {
        mTvBalance.setText(getString(R.string.pay_money_style_god,  mUserInfo.gold));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_recharge_info:
                goldRecharge((RechargeInfo) v.getTag());
                break;
            case R.id.rl_trans_record:
                showRechargeRecord();
                break;
            case R.id.rl_exchange_point:
                exchargePoint();
                break;
            default:
                break;
        }
    }

    private void showRechargeRecord() {
        startActivity(new Intent(this, TradeRecordActivity.class));
    }

    private void getRechargeInfo() {
        new RechargeInfoTask().executeLong();
    }

    private void exchargePoint() {
        Intent intent = new Intent(this, ExchargePointsActivity.class);
        intent.putExtra(EXT_USER_INFO, mUserInfo);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void goldRecharge(RechargeInfo info) {
        if (mIsGetUserInfo) {
            toast("信息获取中，请稍后");
            return;
        }
        Intent intent = new Intent(this, GoldRechangeConfirmActivity.class);
        intent.putExtra(EXT_RECHARGE_INFO, info);
        startActivityForResult(intent, REQUEST_RECHARGE_CODE);
    }

    private void getUserInfo() {
        new FetchUserTask().executeLong();
    }

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(GoldDepositsActivity.this, MsRequest.USER_FULL_INFO);
        }

        @Override
        protected String buildParams() {
            return "user_id=" + mAccountInfo.getUserId();
        }

        @Override
        protected void onPreExecute() {
            mIsGetUserInfo = true;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (mIsGetInfoSuccess) {
                getTitleBar().hideProgress();
            }
            if (response.isSuccessful()) {
                mUserInfo = UserInfo.fromJson(response.getJsonObject());
                mIsGetUserInfo = false;
                updateAccountInfo();
            }
        }
    }


    private class RechargeInfoTask extends MsTask{

        public RechargeInfoTask() {
            super(GoldDepositsActivity.this, MsRequest.TRADE_RECHARGE_INFO);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (!mIsGetUserInfo) {
                getTitleBar().hideProgress();
            }
            if (response.isSuccessful()) {
                ArrayList<RechargeInfo> infos = JsonUtil.getArray(
                        response.getJsonArray(), RechargeInfo.TRANSFORMER);
                if (infos != null) {
                    mRechargeInfos = infos;
                }
                mIsGetInfoSuccess = true;
                mLlTop.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class RechargeInfoAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mRechargeInfos.size();
        }

        @Override
        public RechargeInfo getItem(int position) {
            return mRechargeInfos.get(position);
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
                view = mInflater.inflate(R.layout.list_item_recharge_info, parent, false);
            }
            LinearLayout mTvSale = (LinearLayout) view.findViewById(R.id.ll_tv_sale);
            TextView mTvoriMoney = (TextView) view.findViewById(R.id.tv_ori_money);
            TextView mTvSaleInfo = (TextView) view.findViewById(R.id.tv_sale_info);
            ThemeLineView mLinDivider = (ThemeLineView) view.findViewById(R.id.lin_list_divider);
            RechargeInfo info = getItem(position);
            if (info.orimoney > 0 ) {
                mTvSale.setVisibility(View.VISIBLE);
                mTvoriMoney.setText(String.valueOf((int)info.orimoney) + "RMB");
                mTvSaleInfo.setText(info.saleinfo);
                mTvoriMoney.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                mTvSale.setVisibility(View.GONE);
            }
            if (position >= (getCount()-1)) {
            	mLinDivider.setVisibility(View.GONE);
            } else {
            	mLinDivider.setVisibility(View.VISIBLE);
            }
            TextView tvGold = (TextView) view.findViewById(R.id.tv_gold);
            TextView tvMoney = (TextView) view.findViewById(R.id.tv_money);
            tvGold.setText(getString(R.string.pay_money_style_god, info.gold));
            tvMoney.setText(getString(R.string.pay_buy_price_style, info.money));
            view.setOnClickListener(GoldDepositsActivity.this);
            view.setTag(info);
            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            mUserInfo = data.getParcelableExtra(EXT_USER_INFO);
            updateAccountInfo();
            UserInfoManager.getInstance(this).updateUserInfo(mUserInfo);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_RECHARGE_CODE) {
            int goldCount = data.getIntExtra(EXT_BUY_INFO, 0);
            mUserInfo.gold += goldCount;
            updateAccountInfo();
            UserInfoManager.getInstance(this).updateUserInfo(mUserInfo);
        }
    }
}
