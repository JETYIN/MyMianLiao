package com.tjut.mianliao.explore;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BrowserActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.explore.MemberInfo;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class BuyVipHomePageActivity extends BaseActivity implements OnClickListener,
        DialogInterface.OnClickListener, OnItemClickListener{

    private static final String TAG = "BuyVipHomePageActivity";

    public static final String URL_VIP_AGREEMENT = "assets/pages/vip_agreement.html";

    public static final String EXT_PAY_STYLE = "ext_pay_style";
    public static final String EXT_PAY_YEAR = "ext_pay_year";
    public static final String EXT_USER_INFO = "ext_user_info";
    public static final String  RESULT_UNION_OK = "success";
    public static final String  RESULT_UNION_FAIL = "fail";
    public static final String  RESULT_UNION_CANCEL = "cancel";

    public static final int TYPE_ALIPAY = 1;
    public static final int TYPE_UNIONPAY = 2;
    public static final int TYPE_ML_BALANCE = 3;

    private static final String TRADECODE = "00";

    private AccountInfo mAccountInfo;
    private MemberInfo mCurrentVipInfo;
    private UserInfo mUserInfo;
    private ArrayList<MemberInfo> mVipInfos;
    private LinearLayout mLlBottom;
    private LinearLayout mLvVipInfo;

    private TextView mTvBalance, mTvAlipay, mTvUnionpay, mTvMoneyCount;
    private LightDialog mDisDialog, mPayDialog;
    private String mMoneyStr;
    private int mPayStyle = TYPE_ML_BALANCE;
    private boolean mIsOpenYear, mIsPaying;
    private boolean mIsGetUserInfo, mGetInfoSuccess;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_buy_vip_homepage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVipInfos = new ArrayList<MemberInfo>();
        mAccountInfo = AccountInfo.getInstance(this);
        mTvMoneyCount = (TextView) findViewById(R.id.tv_show_money_count);
        mIsOpenYear = getIntent().getBooleanExtra(EXT_PAY_YEAR, false);
        mUserInfo = getIntent().getParcelableExtra(EXT_USER_INFO);
        mTvBalance = (TextView) findViewById(R.id.tv_balance);
        mTvAlipay = (TextView) findViewById(R.id.tv_alipay);
        mTvUnionpay = (TextView) findViewById(R.id.tv_unionpay);
        mLvVipInfo = (LinearLayout) findViewById(R.id.lv_vip_info);
        mLlBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        updateTitle();
        getVipInfo();
        updateAccountInfo();
    }

    private void updateTitle() {
        if (mUserInfo != null) {
            getTitleBar().setTitle(mUserInfo.vip ? getString(R.string.explore_renewal_vip) : 
                getString(R.string.explore_pay_for_vip));
        } else {
            getTitleBar().setTitle(getString(R.string.explore_pay_for_vip));
            getUserInfo();
        }
    }

    private void updateAccountInfo() {
        if (mUserInfo != null) {
            mTvBalance.setText(getString(R.string.pay_money_balance_style, mUserInfo.gold));
            getTitleBar().setTitle(mUserInfo.vip ? getString(R.string.explore_renewal_vip) : 
                getString(R.string.explore_pay_for_vip));
        }
    }

    private void getUserInfo() {
        new FetchUserTask().executeLong();
    }

    private void fillData(ArrayList<MemberInfo> infos) {
        for (MemberInfo info : infos) {
            mLvVipInfo.addView(getView(info));
        }
    }
    
    private void resetData() {
        mLvVipInfo.removeAllViews();
        fillData(mVipInfos);
    }
    
    private View getView(MemberInfo info) {
        View view = mInflater.inflate(R.layout.list_item_vip_info, null);
        TextView tvMonth = (TextView) view.findViewById(R.id.tv_month);
        TextView tvIntro = (TextView) view.findViewById(R.id.tv_intro);
        tvMonth.setText(getString(R.string.pay_vip_month, info.month));
        tvIntro.setText(info.info);
        tvIntro.setCompoundDrawablesWithIntrinsicBounds(0, 0, info.enable ?
                R.drawable.pay_botton_bg_click : R.drawable.pay_botton_bg_icon, 0);
        view.setTag(info);
        view.setOnClickListener(this);
        return view;
        
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_pay_confirm:
                mIsPaying = true;
                showPayDialog();
                break;
            case R.id.rl_pay_balance:
                changePayStyle(1);
                mPayStyle = TYPE_ML_BALANCE;
                updateView();
                break;
            case R.id.rl_pay_alipay:
                changePayStyle(2);
                mPayStyle = TYPE_ALIPAY;
                updateView();
                break;
            case R.id.rl_pay_unionpay:
                changePayStyle(3);
                mPayStyle = TYPE_UNIONPAY;
                updateView();
                break;
            case R.id.tv_ml_agreement:
                // 面聊协议
                showVipAgreement();
                break;
            case R.id.rl_vip_info:
                mCurrentVipInfo = (MemberInfo) v.getTag();
                changeStatus();
                updateView();
                resetData();
                break;
            default:
                break;
        }
    }

    private void showVipAgreement() {
        Intent intent = new Intent(this, BrowserActivity.class);
        intent.putExtra(BrowserActivity.URL, Utils.getServerAddress() + URL_VIP_AGREEMENT);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mIsPaying) {
            showDiscardDialog();
        }
    }

    private void getVipInfo() {
        new GetVipInfoTask().executeLong();
    }

    private void showDiscardDialog() {
        if(mDisDialog == null) {
            mDisDialog = new LightDialog(this).setTitleLd(getString(R.string.explore_Give_up_the_payment))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, this);
        }
        mDisDialog.show();
    }

    private void showPayDialog() {
        if (mPayDialog == null) {
            mPayDialog = new LightDialog(this).setTitleLd(getString(R.string.explore_pay_point))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, this);
        }
        mPayDialog.setMessage(getString(R.string.pay_vip_pay_message_content,
                mCurrentVipInfo.month, mMoneyStr));
        mPayDialog.show();
    }

    private void pay() {
        switch (mPayStyle) {
            case TYPE_ML_BALANCE:
                if (mUserInfo.gold < mCurrentVipInfo.gold) {
                    toast(R.string.explore_lack_of_balance);
                    return;
                }
                getTradeNo(true);
                break;
            case TYPE_ALIPAY:
//                toast("支付宝支付");
//                getTradeNo(false);
            	toast(R.string.explore_alipay_point);
                break;
            case TYPE_UNIONPAY:
                //getTradeNo(false);
                break;
            default:
                break;
        }
    }

    private void getTradeNo(boolean payByGold) {
        new TradeNoTask(payByGold).executeLong();
    }

    private void payByAlipay(String tradeNo) {

    }

    private void payByUnon(String tradeNo) {
//        UPPayAssistEx.startPayByJAR(this, PayActivity.class, null, null, tradeNo, TRADECODE);
    }

    private void changeStatus() {
        for (MemberInfo vip : mVipInfos) {
            vip.enable = false;
            if (vip == mCurrentVipInfo) {
                vip.enable = true;
            }
        }
    }

    private void changePayStyle(int id) {
        mTvBalance.setCompoundDrawablesWithIntrinsicBounds(0, 0, id == 1 ?
                R.drawable.pay_botton_bg_click : R.drawable.pay_botton_bg_icon, 0);
        mTvAlipay.setCompoundDrawablesWithIntrinsicBounds(0, 0, id == 2 ?
                R.drawable.pay_botton_bg_click : R.drawable.pay_botton_bg_icon, 0);
        mTvUnionpay.setCompoundDrawablesWithIntrinsicBounds(0, 0, id == 3 ?
                R.drawable.pay_botton_bg_click : R.drawable.pay_botton_bg_icon, 0);
    }

    private void updateView() {
        if (mPayStyle == TYPE_ML_BALANCE) {
            if (mCurrentVipInfo != null) {
                mMoneyStr = getString(R.string.pay_money_style_god, mCurrentVipInfo.gold);
            } else {
                mMoneyStr = "";
            }
        } else {
            if (mCurrentVipInfo != null) {
                mMoneyStr = getString(R.string.pay_money_style, mCurrentVipInfo.money);
            } else {
                mMoneyStr = "";
            }
        }
        mTvMoneyCount.setText(mMoneyStr);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mPayDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                pay();
            }
        } else if (dialog == mDisDialog) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                finish();
            }
        }
    }

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(BuyVipHomePageActivity.this, MsRequest.USER_FULL_INFO);
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
            if (mGetInfoSuccess) {
                getTitleBar().hideProgress();
            }
            if (response.isSuccessful()) {
                mUserInfo = UserInfo.fromJson(response.getJsonObject());
                mIsGetUserInfo = false;
                updateView();
            }
        }
    }

    private class GetVipInfoTask extends MsTask{

        public GetVipInfoTask() {
            super(BuyVipHomePageActivity.this, MsRequest.TRADE_VIP_INFO);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (!mIsGetUserInfo) {
                getTitleBar().hideProgress();
            }
            if (response.isSuccessful()) {
                ArrayList<MemberInfo> infos = JsonUtil.getArray(
                        response.getJsonArray(), MemberInfo.TRANSFORMER);
                if (infos != null && infos.size() > 0) {
                    mVipInfos = infos;
                    if (!mIsOpenYear) {
                        mCurrentVipInfo = mVipInfos.get(1);
                        mCurrentVipInfo.enable = true;
                        updateView();
                        changePayStyle(1);
                    } else {
                        mCurrentVipInfo = mVipInfos.get(3);
                        mCurrentVipInfo.enable = true;
                        updateView();
                        changePayStyle(1);
                    }
                }
                mGetInfoSuccess = true;
                mLlBottom.setVisibility(View.VISIBLE);
                fillData(mVipInfos);
            }
        }
    }

    private class TradeNoTask extends MsTask{

        private boolean mPayByGold;

        public TradeNoTask(boolean payByGold) {
            super(BuyVipHomePageActivity.this, payByGold ?
                    MsRequest.TRADE_BUY_VIP_BY_GOLD : MsRequest.TRADE_BUY_VIP_BY_MONEY);
            mPayByGold = payByGold;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("id=").append(mCurrentVipInfo.id)
                    .append("&way=").append(mPayStyle).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                if (mPayByGold) {
                    mUserInfo.gold -= mCurrentVipInfo.gold;
                    updateAccountInfo();
                    toast(R.string.explore_pay_for_success);
                } else {
                    String tradeNo = response.getJsonObject().optString("transaction_no");
                    Utils.logD(TAG , response.response);
                    if (mPayStyle == TYPE_UNIONPAY) {
                        payByUnon(tradeNo);
                    } else {
                        payByAlipay(tradeNo);
                    }
                }
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mCurrentVipInfo = mVipInfos.get(position);
        changeStatus();
        updateView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase(RESULT_UNION_OK)) {
            toast(R.string.explore_pay_for_success);
        } else if (str.equalsIgnoreCase(RESULT_UNION_FAIL)) {
            toast(R.string.explore_pay_for_failed);
        } else if (str.equalsIgnoreCase(RESULT_UNION_CANCEL)) {
            toast(R.string.explore_pay_cancle);
        }
    }

}
