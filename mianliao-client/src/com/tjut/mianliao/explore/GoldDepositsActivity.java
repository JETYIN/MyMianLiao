package com.tjut.mianliao.explore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.pingplusplus.android.Pingpp;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.contact.UserInfoManager;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.TradeProduction;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

import org.lasque.tusdk.core.listener.AnimationListenerAdapter;

import java.util.ArrayList;

public class GoldDepositsActivity extends BaseActivity implements OnClickListener {

    public static final String EXT_RECHARGE_INFO = "ext_recharge_info";
    public static final String EXT_USER_INFO = "ext_user_info";
    public static final String EXT_BUY_INFO = "ext_buy_info";
    private static final int REQUEST_CODE = 100;
    private static final int REQUEST_RECHARGE_CODE = 101;

    private static final String PAY_ALIPAY = "alipay";
    private static final String PAY_WEIXIN = "wx";

    private AccountInfo mAccountInfo;
    private RechargeInfoAdapter mAdapter;
    private ArrayList<TradeProduction> mTradeProductInfos;
    private UserInfo mUserInfo;
    private boolean mIsGetUserInfo, mIsGetInfoSuccess;
    private TradeProduction mCurrentTradProduct;


    /**
     * 顶部金币
     **/
    @ViewInject(R.id.tv_my_gold)
    private TextView mTvBalance;
    @ViewInject(R.id.lv_gold_change_list)
    private ListView mLvRecharge;

    /**
     * 付款方式父布局
     **/
    @ViewInject(R.id.fl_pay_view)
    private FrameLayout mFlRootPayWay;
    @ViewInject(R.id.ll_pay_way_select)
    private LinearLayout mLlPayWay;
    /**
     * 充值金币数量
     **/
    @ViewInject(R.id.tv_recharge)
    private TextView mTvRecharge;
    /**
     * 充值金币数量价格
     **/
    @ViewInject(R.id.tv_pay_money)
    private TextView mTvPayMoney;

    @ViewInject(R.id.tv_alipay)
    private TextView mTvAlipay;
    @ViewInject(R.id.tv_wx_pay)
    private TextView mTvWXPay;
    @ViewInject(R.id.tv_cancel)
    private TextView mTVCancle;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_gold_live_version;
        //return R.layout.activity_gold_deposits;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        getTitleBar().setTitle(R.string.prof_my_gold);
        getTitleBar().showProgress();
        mTradeProductInfos = new ArrayList<>();
        mAccountInfo = AccountInfo.getInstance(this);
        mUserInfo = mAccountInfo.getUserInfo();

        updateAccountInfo();
        mAdapter = new RechargeInfoAdapter();
        mLvRecharge.setAdapter(mAdapter);
        getTradProductInfo();
        getUserInfo();
    }

    private void updateAccountInfo() {
        mTvBalance.setText(String.valueOf(mUserInfo.gold));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_recharge_info:
                goldRecharge((TradeProduction) v.getTag());
                break;
            case R.id.rl_trans_record:
                showRechargeRecord();
                break;
            case R.id.rl_exchange_point:
                exchargePoint();
                break;
            /**支付宝**/
            case R.id.rl_alipay:
            case R.id.tv_alipay:
                startPay(PAY_ALIPAY);
                break;
            /**微信**/
            case R.id.rl_weixin:
            case R.id.tv_wx_pay:
                startPay(PAY_WEIXIN);
                break;
            case R.id.tv_contact_us_in_qq:
                /**详询官方QQ群**/
                break;
            case R.id.rl_cancel:
            case R.id.tv_cancel:
                hideBottomView();
                break;
            case R.id.ll_view:
                if (mFlRootPayWay.isShown()) {
                    hideBottomView();
                }
                break;
            case R.id.fl_pay_view:
                hideBottomView();
                break;
            default:
                break;
        }
    }

    private void startPay(String payWay) {
        new TradeNoTask(payWay).executeLong();
    }

    private void showRechargeRecord() {
        startActivity(new Intent(this, TradeRecordActivity.class));
    }

    private void getTradProductInfo() {
        new TradeInfoTask().executeLong();
    }

    private void exchargePoint() {
        Intent intent = new Intent(this, ExchargePointsActivity.class);
        intent.putExtra(EXT_USER_INFO, mUserInfo);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void goldRecharge(TradeProduction info) {
        mCurrentTradProduct = info;
        if (mIsGetUserInfo) {
            toast(R.string.explore_Access_to_information);
            return;
        }
        int money = 0;
        try{
            money = Integer.parseInt(info.name);
        } catch (Exception e) {
        }
        CharSequence contentCharge = getString(R.string.pay_recharge_num, money);
        CharSequence key = contentCharge.subSequence(3, contentCharge.length());
        CharSequence coloredText = Utils.getColoredText(contentCharge, key, 0xffffaf47);
        mTvRecharge.setText(coloredText);
        mTvPayMoney.setText(getString(R.string.pay_rmb, money));
        showBottomView();

    }

    private void hideBottomView() {
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_down_animation);
        animation.setAnimationListener(new AnimationListenerAdapter(){
            @Override
            public void onAnimationEnd(Animation animation) {
                mLlPayWay.setVisibility(View.GONE);
                mFlRootPayWay.setVisibility(View.GONE);
            }
        });
        mLlPayWay.startAnimation(animation);
    }

    private void showBottomView() {
        mFlRootPayWay.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.live_up_animation);
        mLlPayWay.startAnimation(animation);
        mLlPayWay.setVisibility(View.VISIBLE);

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


    private class TradeInfoTask extends MsTask {

        public TradeInfoTask() {
            super(GoldDepositsActivity.this, MsRequest.DEAL_TRADE_PRODUCTION_LIST);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (!mIsGetUserInfo) {
                getTitleBar().hideProgress();
            }
            if (response.isSuccessful()) {
                ArrayList<TradeProduction> infos = JsonUtil.getArray(
                        response.getJsonArray(), TradeProduction.TRANSFORMER);
                if (infos != null) {
                    ArrayList<TradeProduction> trades = new ArrayList<>();
                    for (TradeProduction trade : infos) {
                        if (!trade.isAvilable) {
                            trades.add(trade);
                        }
                    }
                    mTradeProductInfos = infos;
                    mTradeProductInfos.removeAll(trades);
                }
                mIsGetInfoSuccess = true;
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class RechargeInfoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTradeProductInfos.size();
        }

        @Override
        public TradeProduction getItem(int position) {
            return mTradeProductInfos.get(position);
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
            TextView mTvSale = (TextView) view.findViewById(R.id.tv_sale_info); //
            TextView tvGold = (TextView) view.findViewById(R.id.tv_gold); // tv_gold
            TextView tvMoney = (TextView) view.findViewById(R.id.tv_money); //
            View mLinDivider = (View) view.findViewById(R.id.lin_list_divider);
            TradeProduction info = getItem(position);
            if (position >= (getCount() - 1)) {
                mLinDivider.setVisibility(View.GONE);
            } else {
                mLinDivider.setVisibility(View.VISIBLE);
            }
            tvMoney.setText(getString(R.string.pay_buy_price_style, info.rmbPrice));
            mTvSale.setText(info.activityDesc);
            tvGold.setText(info.name);
            view.setOnClickListener(GoldDepositsActivity.this);
            view.setTag(info);
            return view;
        }
    }

    private class TradeNoTask extends MsTask{

        private String mPayWay;

        public TradeNoTask(String payWay) {
            super(GoldDepositsActivity.this, MsRequest.DEAL_RECHARGE_ANDROID);
            mPayWay = payWay;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("product_id=").append(mCurrentTradProduct.tradeProductionId)
                    .append("&num=").append(mCurrentTradProduct.name)
                    .append("&sign=").append(mPayWay)
                    .append("&ip=").append(Utils.getDevicesIp(GoldDepositsActivity.this))
                    .append("&recharge_amount=").append(mCurrentTradProduct.rmbPrice)
                    .toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                String tradeNo = response.getJsonObject().optString("transaction_no");
                Pingpp.createPayment(GoldDepositsActivity.this, tradeNo);
            }
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
        } else if (requestCode == Pingpp.REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getExtras().getString("pay_result");

                // 处理返回值
                // "success" - 支付成功
                // "fail"    - 支付失败
                // "cancel"  - 取消支付
                // "invalid" - 支付插件未安装（一般是微信客户端未安装的情况）

                String errorMsg = data.getExtras().getString("error_msg"); // 错误信息
                String extraMsg = data.getExtras().getString("extra_msg"); // 错误信息
                showMsg(result, errorMsg, extraMsg);
            }
        }
    }


    public void showMsg(String title, String msg1, String msg2) {
        String str = title;
        if (null !=msg1 && msg1.length() != 0) {
            str += "\n" + msg1;
        }
        if (null !=msg2 && msg2.length() != 0) {
            str += "\n" + msg2;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(GoldDepositsActivity.this);
        builder.setMessage(getMessage(str));
        builder.setTitle("提示");
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    private String getMessage(String msg) {
        if (msg.equals("success")) {
            getTradProductInfo();
            return "支付成功";
        } else if (msg.equals("fail")) {
            return "支付失败";
        } else if (msg.equals("invalid")) {
            return "您的手机尚未安装微信，无法完成支付!";
        } else {
            return "您已取消支付";
        }
    }
}
