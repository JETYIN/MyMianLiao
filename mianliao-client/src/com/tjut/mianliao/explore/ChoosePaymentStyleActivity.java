package com.tjut.mianliao.explore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;

public class ChoosePaymentStyleActivity extends BaseActivity implements OnClickListener{

    private TextView mTvBalance;
    private Intent mData;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_choose_payment_style;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTvBalance = (TextView) findViewById(R.id.tv_balance);
        mData = new Intent();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_pay_balance:
                mData.putExtra(BuyVipHomePageActivity.EXT_PAY_STYLE, BuyVipHomePageActivity.TYPE_ML_BALANCE);
                setResult(RESULT_OK, mData);
                finish();
                break;
            case R.id.rl_pay_alipay:
                mData.putExtra(BuyVipHomePageActivity.EXT_PAY_STYLE, BuyVipHomePageActivity.TYPE_ALIPAY);
                setResult(RESULT_OK, mData);
                finish();
                break;
            case R.id.rl_pay_unionpay:
                mData.putExtra(BuyVipHomePageActivity.EXT_PAY_STYLE, BuyVipHomePageActivity.TYPE_UNIONPAY);
                setResult(RESULT_OK, mData);
                finish();
                break;
            default:
                break;
        }
    }

}
