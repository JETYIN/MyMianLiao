package com.tjut.mianliao.task;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;

public class PopTaskActivity extends BaseActivity implements AnimationListener {

    public static final String EXT_TASK_CONTENT = "ext_task_content";
    public static final String EXT_TASK_NAME = "ext_task_name";
    public static final String EXT_PROCESS = "ext_process";
    public static final String EXT_CREDIT = "ext_credit";
    public static final String EXT_POP = "ext_pop";

    private ImageView mIvTag, mIvDelp;
    private LinearLayout mLlMsg, mLlRoamUnlock;
    private String mTaskName;
    private String mProcess, mCredit, mContent;
    private TextView mTvName, mTvProcess, mTvCredit, mLevelContent;
    private boolean mIsPop;
    
    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_pop_task;
    }
    
    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContent = getIntent().getStringExtra(EXT_TASK_CONTENT);
        mTaskName = getIntent().getStringExtra(EXT_TASK_NAME);
        mProcess = getIntent().getIntExtra(EXT_PROCESS, 0) + "";
        mCredit = getIntent().getIntExtra(EXT_CREDIT, 0) + "";
        mIsPop = getIntent().getBooleanExtra(EXT_POP, false);
        mIvTag = (ImageView) findViewById(R.id.iv_tag_succ);
        mIvDelp = (ImageView) findViewById(R.id.iv_delep);
        mLlMsg = (LinearLayout) findViewById(R.id.ll_msg);
        mLlRoamUnlock = (LinearLayout) findViewById(R.id.ll_msg_roam);
        mTvName = (TextView) findViewById(R.id.tv_task_name);
        mTvProcess = (TextView) findViewById(R.id.tv_process);
        mTvCredit = (TextView) findViewById(R.id.tv_credit);
        mLevelContent = (TextView) findViewById(R.id.tv_level_name);
        Animation mAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_slide_right_in);
        mAnimation.setAnimationListener(this);
        mIvTag.startAnimation(mAnimation);
        mIvTag.setImageResource(mIsPop ? R.drawable.pic_bg_school_unlock :
            R.drawable.team_pic_bg_type);
        mTvName.setText(mTaskName+"X");
        mTvProcess.setText(mProcess);
        mTvCredit.setText(mCredit);
        mLevelContent.setText(mContent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return true;
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        mIvDelp.setVisibility(View.VISIBLE);
        mLlMsg.setVisibility(mIsPop ? View.GONE : View.VISIBLE);
        mLlRoamUnlock.setVisibility(mIsPop ? View.VISIBLE : View.GONE);
        mIvTag.postDelayed(new Runnable() {

            @Override
            public void run() {
                finish();
            }
        }, 1500);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

}
