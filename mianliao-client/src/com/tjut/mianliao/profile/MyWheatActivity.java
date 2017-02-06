package com.tjut.mianliao.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LiveDialog;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.explore.RechargeInfo;
import com.tjut.mianliao.explore.DressUpMallActivty;
import com.tjut.mianliao.theme.ThemeLineView;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import java.util.ArrayList;

/**
 * Created by j_hao on 2016/7/15.
 */
public class MyWheatActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXT_RECHARGE_INFO = "ext_recharge_info";
    public static final String EXT_USER_INFO = "ext_user_info";
    public static final String EXT_BUY_INFO = "ext_buy_info";
    private static final int REQUEST_CODE = 100;
    private static final int REQUEST_RECHARGE_CODE = 101;

    private AccountInfo mAccountInfo;
    private RechargeInfoAdapter mAdapter;
    private ArrayList<RechargeInfo> mRechargeInfos;
    private UserInfo mUserInfo;
    private boolean mIsGetUserInfo, mIsGetInfoSuccess;

    @ViewInject(R.id.lv_incharge_list)
    private ListView mLvRecharge;

    @ViewInject(R.id.tv_gain_free)
    private TextView tvFree;
    @ViewInject(R.id.tv_entry_shop)
    private TextView tvShop;

    @ViewInject(R.id.tv_my_coins)
    private TextView tvCoins;
    @ViewInject(R.id.tv_my_kernel)
    private TextView tvKernel;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_my_wheat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        getTitleBar().setTitle("我的麦粒");
        getTitleBar().showProgress();
        mRechargeInfos = new ArrayList<RechargeInfo>();
        mAccountInfo = AccountInfo.getInstance(this);
        mUserInfo = mAccountInfo.getUserInfo();
        setCoinsNum();

        mAdapter = new RechargeInfoAdapter();
        mLvRecharge.setAdapter(mAdapter);
        getRechargeInfo();
        getUserInfo();
    }

    private void setCoinsNum() {
        tvCoins.setText(getString(R.string.points_gold_remain, mUserInfo.gold));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_gain_free:
                startActivity(new Intent(this, DressUpMallActivty.class));
                break;
            case R.id.tv_entry_shop:
                startActivity(new Intent(this, DressUpMallActivty.class));
                break;
            case R.id.rl_recharge_info:
                RechargeInfo info = (RechargeInfo) v.getTag();
                showComfirmDialog(info.gold);
                break;
            default:
                break;
        }
    }

    private void showComfirmDialog(int price) {
        LiveDialog dialog = new LiveDialog(this);
        dialog.setText(getString(R.string.change_gold, price));
        dialog.setPositiveButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
        dialog.setNegativeButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();


    }

    private void getRechargeInfo() {
        new RechargeInfoTask().executeLong();
    }


    private void getUserInfo() {
        new FetchUserTask().executeLong();
    }

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(MyWheatActivity.this, MsRequest.USER_FULL_INFO);
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

    private void updateAccountInfo() {
        tvKernel.setText(String.valueOf(mUserInfo.credit));
    }

    private class RechargeInfoTask extends MsTask {

        public RechargeInfoTask() {
            super(MyWheatActivity.this, MsRequest.TRADE_RECHARGE_INFO);
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
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class RechargeInfoAdapter extends BaseAdapter {

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
                view = mInflater.inflate(R.layout.item_my_wheat, parent, false);
            }
            LinearLayout mTvSale = (LinearLayout) view.findViewById(R.id.ll_tv_sale);
            TextView mTvoriMoney = (TextView) view.findViewById(R.id.tv_ori_money);
            TextView mTvSaleInfo = (TextView) view.findViewById(R.id.tv_sale_info);
            ThemeLineView mLinDivider = (ThemeLineView) view.findViewById(R.id.lin_list_divider);
            RechargeInfo info = getItem(position);
            if (info.orimoney > 0) {
                mTvSale.setVisibility(View.VISIBLE);
                mTvoriMoney.setText(String.valueOf((int) info.orimoney) + "RMB");
                mTvSaleInfo.setText(info.saleinfo);
                mTvoriMoney.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                mTvSale.setVisibility(View.GONE);
            }
            if (position >= (getCount() - 1)) {
                mLinDivider.setVisibility(View.GONE);
            } else {
                mLinDivider.setVisibility(View.VISIBLE);
            }
            TextView tvGold = (TextView) view.findViewById(R.id.tv_gold);
            TextView tvMoney = (TextView) view.findViewById(R.id.tv_money);
            tvGold.setText(getString(R.string.pay_money_style_wheat, info.gold));
            tvMoney.setText(getString(R.string.pay_buy_gold_price_style, info.money));
            view.setOnClickListener(MyWheatActivity.this);
            view.setTag(info);
            return view;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            mUserInfo = data.getParcelableExtra(EXT_USER_INFO);
            UserInfoManager.getInstance(this).updateUserInfo(mUserInfo);
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_RECHARGE_CODE) {
            int goldCount = data.getIntExtra(EXT_BUY_INFO, 0);
            mUserInfo.gold += goldCount;
            UserInfoManager.getInstance(this).updateUserInfo(mUserInfo);
        }
    }
}
