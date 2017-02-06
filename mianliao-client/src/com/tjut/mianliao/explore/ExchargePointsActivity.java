package com.tjut.mianliao.explore;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.data.SystemInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class ExchargePointsActivity extends BaseActivity implements OnClickListener,
DialogInterface.OnClickListener, TextWatcher {

    private EditText mEtGold;
    private TextView mTvPoints;
    private TextView mTvAccountGold, mTvAccountPoints;
    private ImageView mIvDec, mIvAdd;
    private int mPoints;
    private int mGoldCount;
    private int mCount;
    private int mGoldToPoint;
    private boolean mIsGetInfoSuccess;
    private UserInfo mUserInfo;
    private LightDialog mExchargeDialog;
    private String mGoldText;

    private Settings mSettings;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_exchange_point;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        getTitleBar().setTitle(R.string.emo_recharge_exchangepoint);
        getTitleBar().showProgress();
        mUserInfo = getIntent().getParcelableExtra(GoldDepositsActivity.EXT_USER_INFO);
        mIvDec = (ImageView) findViewById(R.id.iv_dec);
        mIvAdd = (ImageView) findViewById(R.id.iv_add);
        mEtGold = (EditText) findViewById(R.id.tv_gold);
        mEtGold.addTextChangedListener(this);
        mEtGold.setOnClickListener(this);
        mTvPoints = (TextView) findViewById(R.id.tv_points);
        mTvAccountGold = (TextView) findViewById(R.id.tv_account_gold);
        mTvAccountPoints = (TextView) findViewById(R.id.tv_account_point);

        getSystemInfo();
        initData();
    }

    @Override
    public void onBackPressed() {
        returnData();
        super.onBackPressed();
    }

    private void initData() {
        mCount = mUserInfo.gold;
        if (mCount <= 0){
            mIvDec.setImageResource(R.drawable.bottom_icon_reduce_grown);
            mIvAdd.setImageResource(R.drawable.bottom_icon_plus_grown);
            mEtGold.setEnabled(false);
        }
        mTvAccountGold.setText(getString(R.string.pay_buy_gold_price_style, mCount));
        mTvAccountPoints.setText(getString(R.string.pay_recharge_wheat_style, mUserInfo.credit));
    }

    private void getSystemInfo() {
        new SystemInfoTask().executeLong();
    }

    private void returnData() {
        Intent intent = new Intent();
        intent.putExtra(GoldDepositsActivity.EXT_USER_INFO, mUserInfo);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_excharge:
                if (mGoldCount > 0) {
                    pay();
                }
                break;
            case R.id.iv_dec:
                if (mIsGetInfoSuccess) {
                    dec();
                } else {
                    toast(R.string.get_info_img);
                }
                break;
            case R.id.iv_add:
                if (mIsGetInfoSuccess) {
                    add();
                } else {
                    toast(R.string.get_info_img);
                }
                break;
            case R.id.tv_gold:
                String value = mEtGold.getText().toString().trim();
                if (value.length() > 0) {
                    mGoldCount = Integer.valueOf(mEtGold.getText().toString().trim()).intValue();
                    if (mGoldCount == 0) {
                        mEtGold.setText("");
                    }
                }
                break;
            default:
                break;
        }
    }

    private void showExchargeDialog() {
        if (mExchargeDialog == null) {
            mExchargeDialog = new LightDialog(this).setTitleLd(R.string.vip_pay_notice)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.yes, this);
        }
        mExchargeDialog.setMessage(
                getString(R.string.pay_recharge_points_msg_style, mGoldCount, mPoints));
        mExchargeDialog.show();
    }

    private void dec() {
        if (mGoldCount > 0) {
            if(--mGoldCount <= 0){
                mIvDec.setImageResource(R.drawable.bottom_icon_reduce_grown);
            }else {
                mIvDec.setImageResource(R.drawable.bottom_icon_reduce_blue);
            }
        } else {
            // 不可再继续点击
            mIvDec.setImageResource(R.drawable.bottom_icon_reduce_grown);
            mGoldCount = 0;
        }
        fillData();
    }

    private void add() {
        if (mGoldCount >= mCount) {
            // 不可再继续点击
            mGoldCount = mCount;
            mIvAdd.setImageResource(R.drawable.bottom_icon_plus_grown);
        } else {
            if (++mGoldCount >= mCount) {
                mIvAdd.setImageResource(R.drawable.bottom_icon_plus_grown);
            } else {
                mIvAdd.setImageResource(R.drawable.bottom_icon_plus_blue);
            }
        }
        fillData();
    }

    private void fillData() {
        mPoints = mGoldCount * mGoldToPoint;
        mEtGold.setText(String.valueOf(mGoldCount));
        mTvPoints.setText(String.valueOf(mPoints));
    }

    private void pay() {
        if (mGoldCount > mCount || mCount <= 0) {
            toast(R.string.vip_pay_gold_not_enough);
            return;
        }
        showExchargeDialog();
    }

    private void setImageClickble() {
        if (mIsGetInfoSuccess) {
            mIvAdd.setClickable(true);
            mIvDec.setClickable(true);
        }
    }

    private void exchargePoint() {
        new ExchargePointTask().executeLong();
    }

    private class SystemInfoTask extends MsTask{

        public SystemInfoTask() {
            super(ExchargePointsActivity.this, MsRequest.SYSTEM_INFO);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                SystemInfo systemInfo = SystemInfo.fromJson(response.getJsonObject());
                if (systemInfo != null) {
                    mGoldToPoint = systemInfo.goldToPoint;
                    mIsGetInfoSuccess = true;
                }
                setImageClickble();
            } else {
                toast(R.string.explor_get_info_fail);
            }
        }
    }

    private class ExchargePointTask extends MsTask{

        public ExchargePointTask() {
            super(ExchargePointsActivity.this, MsRequest.TRADE_GOLD_TO_POINT);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("price=").append(mGoldCount)
                    .append("&credit=").append(mPoints).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().showProgress();
            if (response.isSuccessful()) {
                toast(R.string.explor_exchange_succ);
                mUserInfo.gold -= mGoldCount;
                mUserInfo.credit += mPoints;
                initData();
                returnData();
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            exchargePoint();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String content = mEtGold.getText().toString().trim();
        if (content.equals("")) {
            mGoldCount = 0;
        } else {
            mGoldCount = Integer.valueOf(mEtGold.getText().toString().trim()).intValue();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if( mGoldCount >= mCount){
            mIvAdd.setImageResource(R.drawable.bottom_icon_plus_grown);
            mIvAdd.setEnabled(false);
        } else {
            mIvAdd.setEnabled(true);
            mIvAdd.setImageResource(R.drawable.bottom_icon_plus_blue);
        }
        if (mGoldCount <= 0){
            mIvDec.setImageResource(R.drawable.bottom_icon_reduce_grown);
            mIvDec.setEnabled(false);
        } else {
            mIvDec.setEnabled(true);
            mIvDec.setImageResource(R.drawable.bottom_icon_reduce_blue);
        }
        if (mGoldCount > mCount) {
            mEtGold.setText(mGoldText);
            mEtGold.setSelection(mGoldText.length());
        } else {
            mGoldText = mEtGold.getText().toString().trim();
            mPoints = mGoldCount * mGoldToPoint;
            mTvPoints.setText(String.valueOf(mPoints));
        }
    }
}
