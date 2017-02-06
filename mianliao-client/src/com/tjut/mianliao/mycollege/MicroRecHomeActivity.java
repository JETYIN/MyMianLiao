package com.tjut.mianliao.mycollege;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MStaticInterface;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.job.JobsActivity;
import com.tjut.mianliao.job.RecruitCalendarActivity;
import com.tjut.mianliao.job.ResumeActivity;
import com.umeng.analytics.MobclickAgent;

public class MicroRecHomeActivity extends BaseActivity implements OnClickListener {

    private CommonBanner mVsSwitcher;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_micro_rec_homepage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.mc_recru_job);
        mVsSwitcher = (CommonBanner) findViewById(R.id.vs_switcher);
        mVsSwitcher.setParam(CommonBanner.Plate.RecruitMain, 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_search_job:
                startActivity(JobsActivity.class);
                MobclickAgent.onEvent(this, MStaticInterface.LOOK);
                break;
            case R.id.ll_intel_match:
                startActivity(IntelMatchActivity.class);
                MobclickAgent.onEvent(this, MStaticInterface.MATCH);
                break;
            case R.id.ll_my_resume:
                startActivity(ResumeActivity.class);
                MobclickAgent.onEvent(this, MStaticInterface.RESUME);
                break;
            case R.id.ll_emp_calendar:
                startActivity(RecruitCalendarActivity.class);
                MobclickAgent.onEvent(this, MStaticInterface.CALENDAR);
                break;
            default:
                break;
        }
    }

    private void startActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }
}
