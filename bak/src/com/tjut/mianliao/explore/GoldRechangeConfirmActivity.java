package com.tjut.mianliao.explore;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.explore.RechargeInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.unionpay.UPPayAssistEx;
import com.unionpay.uppay.PayActivity;

public class GoldRechangeConfirmActivity extends BaseActivity implements OnClickListener,
        DialogInterface.OnClickListener{

    private static final int MODE_ALIPAY = 1;
    private static final int MODE_UNIONPAY = 2;

    public static final String  RESULT_UNION_OK = "success";
    public static final String  RESULT_UNION_FAIL = "fail";
    public static final String  RESULT_UNION_CANCEL = "cancel";

    private static final String TRADECODE = "00";

    private TextView mTvGoldChoosed, mTvChoosedPrice;
    private TextView mTvAlipay, mTvUnionpay;
    private RechargeInfo mRechargeInfo;
    private int mPayMode = MODE_UNIONPAY;
    private LightDialog mPayDialog;

    private Settings mSettings;
    private boolean mIsNightMode;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_gold_recharge_confirm;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mIsNightMode = mSettings.isNightMode();

        getTitleBar().setTitle("确认付款");
        mRechargeInfo = getIntent().getParcelableExtra(GoldDepositsActivity.EXT_RECHARGE_INFO);
        mTvChoosedPrice = (TextView) findViewById(R.id.tv_choosed_price);
        mTvGoldChoosed = (TextView) findViewById(R.id.tv_choosed_gold);
        mTvGoldChoosed.setText(getString(R.string.pay_buy_gold_price_style, mRechargeInfo.gold));
        mTvChoosedPrice.setText(getString(R.string.pay_buy_price_style, mRechargeInfo.money));
        mTvAlipay = (TextView) findViewById(R.id.tv_alipay);
        mTvUnionpay = (TextView) findViewById(R.id.tv_unionpay);

        if (mIsNightMode) {
            findViewById(R.id.ll_confirm).setBackgroundResource(R.drawable.bg);
        } else {
            findViewById(R.id.ll_confirm).setBackgroundColor(0XFFF2F2F2);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_pay_alipay:
//                changePayMode(1);
            	toast("该功能还在开发中，敬请期待");
                break;
            case R.id.rl_pay_unionpay:
                changePayMode(2);
                break;
            case R.id.tv_pay_confirm:
                showPayDialog();
                break;
            default:
                break;
        }
    }

    private void showPayDialog() {
        if (mPayDialog == null) {
            mPayDialog = new LightDialog(this).setTitleLd("付款提示")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, this);
        }
        mPayDialog.setMessage(getString(R.string.pay_recharge_msg_style,
                mRechargeInfo.money, mRechargeInfo.gold));
        mPayDialog.show();
    }

    private void getTradeNo() {
        new TradeNoTask().executeLong();
    }

    private void changePayMode(int mode) {
        mTvAlipay.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                mode == 1 ? R.drawable.pay_botton_bg_click : R.drawable.pay_botton_bg_icon, 0);
        mTvUnionpay.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                mode == 2 ? R.drawable.pay_botton_bg_click : R.drawable.pay_botton_bg_icon, 0);
        mPayMode = mode == 1 ? MODE_ALIPAY : MODE_UNIONPAY;
    }

    private void payByAlipay(String tradeNo) {

    }

    private void payByUnon(String tradeNo) {
        UPPayAssistEx.startPayByJAR(this, PayActivity.class, null, null, tradeNo, TRADECODE);
    }


    private class TradeNoTask extends MsTask{

        public TradeNoTask() {
            super(GoldRechangeConfirmActivity.this, MsRequest.TRADE_RECHARGE_GOLD);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("id=").append(mRechargeInfo.id)
                    .append("&way=").append(mPayMode).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                String tradeNo = response.getJsonObject().optString("transaction_no");
                if (mPayMode == MODE_UNIONPAY) {
                    payByUnon(tradeNo);
                } else {
                    payByAlipay(tradeNo);
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String str = data.getExtras().getString("pay_result");
        if (str.equalsIgnoreCase(RESULT_UNION_OK)) {
            toast(" 支付成功! ");
            Intent intent = new Intent();
            intent.putExtra(GoldDepositsActivity.EXT_BUY_INFO, mRechargeInfo.gold);
            setResult(RESULT_OK, intent);
            finish();
        } else if (str.equalsIgnoreCase(RESULT_UNION_FAIL)) {
            toast(" 支付失败! ");
        } else if (str.equalsIgnoreCase(RESULT_UNION_CANCEL)) {
            toast(" 你已取消了本次订单的支付! ");
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            getTradeNo();
        }
    }

}
