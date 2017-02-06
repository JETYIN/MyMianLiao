package com.tjut.mianliao.task;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jess.ui.TwoWayAbsListView;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayAdapterView.OnItemClickListener;
import com.jess.ui.TwoWayGridView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.MainActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.contact.AddContactActivity;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.contact.UserInfo;
import com.tjut.mianliao.data.task.FullTask;
import com.tjut.mianliao.data.task.SubTask;
import com.tjut.mianliao.data.task.Task;
import com.tjut.mianliao.data.task.TaskLevel;
import com.tjut.mianliao.explore.DressUpMallActivty;
import com.tjut.mianliao.mycollege.TakeNoticesActivity;
import com.tjut.mianliao.profile.IdVerifyActivity;
import com.tjut.mianliao.profile.SignInActivity;
import com.tjut.mianliao.util.DensityUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

import java.util.ArrayList;
import java.util.List;

public class TaskActivity extends BaseActivity {

    public static final int DAILY_MODE = 0;
    public static final int ROOKIE_MODE = 1;
    public static final int COLLEGE_MODE = 2;

    private LinearLayout mLlTasktitle;
    private TwoWayGridView mLvDayTask, mLvRookieTask, mLvCollegeTask;
    private TextView mTvDayTitle, mTvNewTitle, mTvCollegeTitle;
    private Context mContext;
    private FullTask mTask;
    private TaskAdapter mDayAdapter, mRookieAdapter, mCollegeAdapter;
    private ArrayList<String> mTaskTitle = new ArrayList<>();
    private String mSkipFlag;
    //private TextView mShowKernel;
    private Task mFlagTask;

    private AccountInfo mAccountInfo;
    private UserInfo mUserInfo;
    private boolean mHasGetData;
    private int mDayTaskProgress, mNewTaskProgress, myCollegeProgress;
    private ImageView mIvUnclock;
    private TextView mTvPrompt;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_task;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mAccountInfo = AccountInfo.getInstance(this);
        mUserInfo = mAccountInfo.getUserInfo();
        fillComponents();
        getTitleBar().setTitle(R.string.explore_task_center);
        new MyTaskTask().executeLong();
        // mVsSwitcher = (CommonBanner) findViewById(R.id.vs_switcher);
        // mVsSwitcher.setParam(CommonBanner.Plate.TaskMain, 0);
        //mShowKernel = (TextView) findViewById(R.id.tv_show_my_kernel);
        //mShowKernel.setText(String.valueOf(mUserInfo.credit));
        getUserInfo();
    }

    @Override
    protected void onResume() {
        if (!mHasGetData) {
            new MyTaskTask().executeLong();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        mHasGetData = false;
        super.onPause();
    }

    private void fillComponents() {

        mDayAdapter = new TaskAdapter(DAILY_MODE);
        mRookieAdapter = new TaskAdapter(ROOKIE_MODE);
        mCollegeAdapter = new TaskAdapter(COLLEGE_MODE);
        mLvDayTask = (TwoWayGridView) this.findViewById(R.id.day_gridview);
        mTvDayTitle = (TextView) this.findViewById(R.id.tv_task_title1);
        mTvNewTitle = (TextView) this.findViewById(R.id.tv_task_title2);
        mTvCollegeTitle = (TextView) this.findViewById(R.id.tv_task_title3);
        mLvRookieTask = (TwoWayGridView) this
                .findViewById(R.id.rookie_gridview);
        mLvCollegeTask = (TwoWayGridView) this
                .findViewById(R.id.college_gridview);
        mLlTasktitle = (LinearLayout) this.findViewById(R.id.ll_task_title);
        mLvDayTask.setAdapter(mDayAdapter);
        mLvRookieTask.setAdapter(mRookieAdapter);
        mLvCollegeTask.setAdapter(mCollegeAdapter);

        mLvDayTask.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(TwoWayAdapterView parent, View v,
                                    int position, long id) {

            }
        });

        mLvRookieTask.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(TwoWayAdapterView parent, View v,
                                    int position, long id) {

            }
        });

        mLvCollegeTask.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(TwoWayAdapterView parent, View v,
                                    int position, long id) {

            }
        });

    }

    class TaskAdapter extends BaseAdapter {
        int mode;

        TaskAdapter(int mode) {
            this.mode = mode;
        }

        private SubTask getSubTask() {
            if (mTask == null) {
                return null;
            }
            switch (mode) {
                case DAILY_MODE: {
                    return mTask.getDailyInfo();
                }
                case ROOKIE_MODE: {
                    return mTask.getRookieInfo();
                }
                case COLLEGE_MODE: {
                    return mTask.getCollegeInfo();
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {

            SubTask subTask = getSubTask();
            if (subTask == null) {
                return 0;
            } else {
                return subTask.getInfo().size();
            }

        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            SubTask subTask = getSubTask();
            Task task = subTask.getInfo().get(arg0);

            View view = mInflater.inflate(R.layout.task_item, null);

            RelativeLayout mRlTask = (RelativeLayout) view
                    .findViewById(R.id.rl_root_layout);
            ProImageView ivTaskIcon = (ProImageView) view
                    .findViewById(R.id.iv_icon);
            if (task.isFinish()) {
                ivTaskIcon
                        .setImageResource(R.drawable.pic_bg_sign_in_to_complete);
            } else {
                ivTaskIcon.setImage(task.getIcon(), R.drawable.icon_stream);
            }
            // ImageView ivStatus = (ImageView)`
            // view.findViewById(R.id.iv_status);
            // ivStatus.setVisibility(task.isIs_finish() ? View.VISIBLE :
            // View.GONE);
            // ivTaskIcon.setVisibility(task.isIs_finish() ? View.GONE :
            // View.VISIBLE);

            TextView tvTaskName = (TextView) view
                    .findViewById(R.id.tv_task_name);

            tvTaskName.setText(task.getName());
            view.setTag(task);

            TextView mTvProgress = (TextView) view
                    .findViewById(R.id.tv_progress);
            mTvProgress.setText(task.getProcess() + "/" + task.getMax());

            TextView tvCredit = (TextView) view.findViewById(R.id.tv_credit);
            tvCredit.setText(task.getCredit() + "");

            GradientDrawable bgShape = (GradientDrawable) mRlTask
                    .getBackground();

            String colorStr = Long.toHexString(task.getBgColor());
            bgShape.setColor(Color.parseColor("#" + colorStr));

            view.setLayoutParams(new TwoWayAbsListView.LayoutParams(DensityUtil
                    .dip2pxInt(mContext, 150), DensityUtil.dip2pxInt(mContext,
                    90)));
            view.setOnClickListener(mListen);
            return view;

        }

    }

    private class MyTaskTask extends MsTask {

        public MyTaskTask() {
            super(mContext, MsRequest.MY_TASK_LIST);
            mHasGetData = true;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mTask = FullTask.fromJson(response.getJsonObject());
                List<TaskLevel> levels = mTask.getCollegeInfo().getLevel();
                mTaskTitle.clear();
                if (levels != null) {
                    for (int i = 0; i < levels.size(); i++) {
                        mTaskTitle.add(levels.get(i).getName());

                    }
                }
                showTaskInfo();
                mDayAdapter.notifyDataSetChanged();
                mRookieAdapter.notifyDataSetChanged();
                mCollegeAdapter.notifyDataSetChanged();
                getTaskProgress();
                mTvDayTitle.setText(getString(R.string.task_daily_task,
                        mDayTaskProgress,
                        mTask.getDailyInfo().getInfo().size()));
                mTvNewTitle.setText(getString(R.string.task_newbie_task,
                        mNewTaskProgress, (mTask.getRookieInfo()).getInfo().size()));
                mTvCollegeTitle.setText(getString(R.string.task_roaming_the_unlock_task,
                        myCollegeProgress, 5));
            }
        }
    }

    private void showTaskInfo() {
        mLlTasktitle.removeAllViews();
        for (int i = 0; i < mTaskTitle.size(); i++) {
            mLlTasktitle.addView(getView(i, mTaskTitle.get(i)));
        }
    }

    private View getView(int position, String title) {
        View view = mInflater.inflate(R.layout.list_item_tasktitle, null);
        TextView mTvCollegeClass = (TextView) view
                .findViewById(R.id.tv_college_class);
        mIvUnclock = (ImageView) view.findViewById(R.id.iv_isunclock);
        mTvPrompt = (TextView) view.findViewById(R.id.tv_prompt);

        LinearLayout mYelloPoint = (LinearLayout) view
                .findViewById(R.id.ll_yellow_point);
        mTvCollegeClass.setText(title);
        if (position == mTaskTitle.size() - 1) {
            mIvUnclock.setImageResource(R.drawable.img_lock_yellow);
            mTvPrompt.setVisibility(View.VISIBLE);
            mYelloPoint.setVisibility(View.GONE);
        }
        return view;
    }

    OnClickListener mListen = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            mFlagTask = (Task) v.getTag();
            mSkipFlag = mFlagTask.getName();
            if (mFlagTask.getProcess() < mFlagTask.getMax()) {
                if (mSkipFlag.equals(getString(R.string.task_day_sign))) {
                    intent.setClass(TaskActivity.this, SignInActivity.class);
                    startActivity(intent);
                } else if (mSkipFlag
                        .equals(getString(R.string.task_school_post))) {
                    intent.setClass(TaskActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ACTIVE_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_FRAGMENT_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_POST_TAB_INDEX, 0);
                    startActivity(intent);
                } else if (mSkipFlag
                        .equals(getString(R.string.task_tribe_post))) {
                    intent.setClass(TaskActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ACTIVE_TAB_INDEX, 1);
                    intent.putExtra(MainActivity.EXTRA_FRAGMENT_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_POST_TAB_INDEX, 1);
                    startActivity(intent);
                } else if (mSkipFlag.equals(getString(R.string.task_day_tribe_post))) {
                    intent.setClass(TaskActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ACTIVE_TAB_INDEX, 1);
                    intent.putExtra(MainActivity.EXTRA_FRAGMENT_TAB_INDEX,
                            0);
                    intent.putExtra(MainActivity.EXTRA_POST_TAB_INDEX, 1);
                    startActivity(intent);
                } else if (mSkipFlag.equals(getString(R.string.task_night_tribe_post))) {
                    intent.setClass(TaskActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ACTIVE_TAB_INDEX, 1);
                    intent.putExtra(MainActivity.EXTRA_FRAGMENT_TAB_INDEX,
                            0);
                    intent.putExtra(MainActivity.EXTRA_POST_TAB_INDEX, 1);
                    startActivity(intent);
                } else if (mSkipFlag.equals(getString(R.string.task_roam_school_post))) {
                    intent.setClass(TaskActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ACTIVE_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_FRAGMENT_TAB_INDEX, 0);
                    intent.putExtra(MainActivity.EXTRA_POST_TAB_INDEX, 1);
                    startActivity(intent);
                } else if (mSkipFlag.equals(getString(R.string.task_take_note))) {
                    intent.setClass(TaskActivity.this,
                            TakeNoticesActivity.class);
                    startActivity(intent);
                } else if (mSkipFlag.equals(getString(R.string.task_add_friend))) {
                    intent.setClass(TaskActivity.this,
                            AddContactActivity.class);
                    startActivity(intent);
                } else if (mSkipFlag.equals(getString(R.string.task_identity_verification))) {
                    intent.setClass(TaskActivity.this, IdVerifyActivity.class);
                    startActivity(intent);
                } else if (mSkipFlag
                        .equals(getString(R.string.task_interactive_cartoon))) {
                    intent.setClass(TaskActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_ACTIVE_TAB_INDEX, 2);
                    startActivity(intent);
                } else if (mSkipFlag.equals(getString(R.string.task_shopping))) {
                    intent.setClass(TaskActivity.this, DressUpMallActivty.class);
                    startActivity(intent);
                }
            } else {
                toast(TaskActivity.this.getString(R.string.task_good_job));
            }
        }
    };

    private class FetchUserTask extends MsTask {

        public FetchUserTask() {
            super(TaskActivity.this, MsRequest.USER_FULL_INFO);
        }

        @Override
        protected String buildParams() {
            return "user_id=" + mAccountInfo.getUserId();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mUserInfo = UserInfo.fromJson(response.getJsonObject());
                //updateAccountInfo();
            }
        }
    }

	/*private void tvKernel() {
        mShowKernel.setText(String.valueOf(mUserInfo.credit));
	}*/

    private void getUserInfo() {
        new FetchUserTask().executeLong();
    }

    private void getTaskProgress() {
        mDayTaskProgress = 0;
        mNewTaskProgress = 0;
        for (int i = 0; i < (mTask.getDailyInfo().getInfo().size()); i++) {
            if (mTask.getDailyInfo().getInfo().get(i).isFinish()) {
                mDayTaskProgress++;
            }
        }
        for (int i = 0; i < (mTask.getRookieInfo().getInfo().size()); i++) {
            if (mTask.getRookieInfo().getInfo().get(i).isFinish()) {
                mNewTaskProgress++;
            }
        }

        if (mTask.getCollegeInfo().getInfo().size() == 0) {
            myCollegeProgress = mTask.getCollegeInfo().getLevel().size();
        } else {
            for (int i = 0; i < (mTask.getCollegeInfo().getInfo().size()); i++) {
                if (mTask.getCollegeInfo().getLevel().size() < 5) {
                    myCollegeProgress = mTask.getCollegeInfo().getLevel()
                            .size() - 1;
                } else {
                    int num = 0;
                    for (int j = 0; j < (mTask.getCollegeInfo().getInfo()
                            .size()); j++) {
                        if (mTask.getCollegeInfo().getInfo().get(i).isFinish()) {
                            num++;
                        }
                        if (num >= (mTask.getCollegeInfo().getInfo().size())) {
                            myCollegeProgress = mTask.getCollegeInfo()
                                    .getLevel().size();
                        } else {
                            myCollegeProgress = mTask.getCollegeInfo()
                                    .getLevel().size() - 1;
                        }
                    }
                }
            }
        }
        if (myCollegeProgress >= mTaskTitle.size()) {
            mIvUnclock.setImageResource(R.drawable.img_unlock_yellow);
            mTvPrompt.setVisibility(View.GONE);
        }
    }

}
