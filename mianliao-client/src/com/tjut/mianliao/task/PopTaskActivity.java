package com.tjut.mianliao.task;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.Utils;

public class PopTaskActivity extends Activity  {

    public static final String EXT_TASK_CONTENT = "ext_task_content";
    public static final String EXT_TASK_NAME = "ext_task_name";
    public static final String EXT_PROCESS = "ext_process";
    public static final String EXT_CREDIT = "ext_credit";
    public static final String EXT_POP = "ext_pop";
    public static final String EXT_SCHOOL_NUM = "ext_school_num";

    private LinearLayout mLlMsg, mLlRoamUnlock;
    private String mTaskName;
    private String mProcess, mCredit, mContent;
    private TextView mTvName, mTvCredit, mLevelContent;
    private boolean mIsPop;
    private TextView mTvUnlockNum;
    private int unlockSchoolNum;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_task);
        mContent = getIntent().getStringExtra(EXT_TASK_CONTENT);
        mTaskName = getIntent().getStringExtra(EXT_TASK_NAME);
        unlockSchoolNum = getIntent().getIntExtra(EXT_SCHOOL_NUM, 0);
        mProcess = getIntent().getIntExtra(EXT_PROCESS, 0) + "";
        mCredit = getIntent().getIntExtra(EXT_CREDIT, 0) + "";
        mIsPop = getIntent().getBooleanExtra(EXT_POP, false);
        mLlMsg = (LinearLayout) findViewById(R.id.ll_msg);
        mLlRoamUnlock = (LinearLayout) findViewById(R.id.ll_msg_roam);
        mTvName = (TextView) findViewById(R.id.tv_task_name);
        mTvCredit = (TextView) findViewById(R.id.tv_kernel_num);
        mLevelContent = (TextView) findViewById(R.id.tv_level_name);
        mTvUnlockNum = (TextView) findViewById(R.id.tv_unlock_school);
        mTvName.setText(mTaskName);
        mTvCredit.setText("+" + mCredit);
        mLevelContent.setText(mContent);
        String unlockNum = getString(R.string.unlock_school) + 
                unlockSchoolNum + getString(R.string.unlock_school_num);
        mTvUnlockNum.setText(Utils.getColoredText(unlockNum, String.valueOf(unlockSchoolNum),
                0XFFFFA800, false));
        mLlMsg.setVisibility(mIsPop ? View.GONE : View.VISIBLE);
        mLlRoamUnlock.setVisibility(mIsPop ? View.VISIBLE : View.GONE);
        mLlMsg.postDelayed(new Runnable() {

            @Override
            public void run() {
                finish();
            }
        }, 2000);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }
}
