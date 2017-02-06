package com.tjut.mianliao.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.view.annotation.ViewInject;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;

/**
 * Created by j_hao on 2016/7/19.
 */


public class MyWealthActivity extends BaseActivity implements View.OnClickListener {
    /**
     * 可提现金额
     **/
    @ViewInject(R.id.tv_withdraw_rmb)
    private TextView tvTotalWealth;
    /**
     * 立即提现按钮
     **/
    @ViewInject(R.id.tv_immediate_withdawal)
    private TextView tvImmediate;
    /**
     * 当前麦穗数
     **/
    @ViewInject(R.id.tv_now_kernel_num)
    private TextView tvNowKernel;
    /**
     * 当日收获麦穗数
     **/
    @ViewInject(R.id.tv_today_kernel_num)
    private TextView tvTodayKernel;
    /**
     * 总共麦穗数
     **/
    @ViewInject(R.id.tv_total_kernel_num)
    private TextView tvTotalKernel;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_my_wealth;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(getString(R.string.prof_my_wealth_live));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**立即提现**/
            case R.id.tv_immediate_withdawal:
                break;
            default:
                break;
        }
    }

}
