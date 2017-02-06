package com.tjut.mianliao.mycollege;

import java.util.Calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.forum.TimePicker;
import com.tjut.mianliao.forum.TimePicker.Callback;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class AddJobsInfoActivity extends BaseActivity implements OnClickListener, Callback {

    private static final int MAX_COUNT = 5;
    public static final String EXT_CURRENT_WEEK = "ext_current_week";

    private LinearLayout mLlContent;
    private long mEndTime;
    private TimePicker mTimePicker;
    private EditText mEtCurrent;
    private long[] mTimes = new long[MAX_COUNT];
    private int mCurrentPosition;
    private int mCurrentWeek;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_add_jobs_info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentWeek = getIntent().getIntExtra(EXT_CURRENT_WEEK, 0);
        getTitleBar().setTitle(getString(R.string.mc_recruit_calendar_pop_str1));
        getTitleBar().showRightText(R.string.job_new, this);
        mLlContent = (LinearLayout) findViewById(R.id.ll_content);
        mTimePicker = new TimePicker(this);
        mTimePicker.setCallback(this);
        mLlContent.addView(getView());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_add_more_job:
                addMoreJob();
                break;
            case R.id.tv_right:
                if (canAddNewInfo()) {
                    submit();
                }
                break;
            case R.id.et_invite_time:
                mCurrentPosition = (int) v.getTag();
                getCurrentEdit();
                mTimePicker.pick(mEndTime, 0);
                break;
            default:
                break;
        }
    }

    private void submit() {
        getTitleBar().showProgress();
        new AddRecruTask().executeLong();
    }

    private void getCurrentEdit() {
        mEtCurrent = (EditText) mLlContent.getChildAt(mCurrentPosition)
                .findViewById(R.id.et_invite_time);
    }

    private void addMoreJob() {
        if (!canAddNewInfo()) {
            toast("当前信息尚未填写完整");
            return;
        }
        if (mLlContent.getChildCount() < MAX_COUNT) {
            mLlContent.addView(getView(), mLlContent.getChildCount());
        } else {
            toast("您已经添加足够的信息了!");
        }
    }

    private boolean canAddNewInfo() {
        if (mLlContent.getChildCount() < MAX_COUNT) {
            return true;
        }
        View view = mLlContent.getChildAt(mLlContent.getChildCount() - 1);
        EditText etPosition = (EditText) view.findViewById(R.id.et_position);
        EditText etCorp = (EditText) view.findViewById(R.id.et_corp);
        EditText etTime = (EditText) view.findViewById(R.id.et_invite_time);
        String position = etPosition.getText().toString().trim();
        String corp = etCorp.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        if (position.length() > 0 && corp.length() > 0 && time.length() > 0) {
            return true;
        }
        return false;
    }

    private View getView() {
        View view = mInflater.inflate(R.layout.list_item_add_jobs_info, null);
        view.setBackgroundColor(Color.WHITE);
        EditText etTime = (EditText) view.findViewById(R.id.et_invite_time);
        etTime.setOnClickListener(this);
        etTime.setTag(mLlContent.getChildCount());
        return view;
    }

    @Override
    public void onResult(Calendar time, int requestCode) {
        mEndTime = time.getTimeInMillis();
        updateTimeView();
    }

    private void updateTimeView() {
        mEtCurrent.setText(Utils.getTimeString(3, mEndTime));
        mTimes[mCurrentPosition] = mEndTime;
    }

    private String getJsonString() {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < mLlContent.getChildCount(); i++) {
            View view = mLlContent.getChildAt(i);
            EditText etPosition = (EditText) view.findViewById(R.id.et_position);
            EditText etCorp = (EditText) view.findViewById(R.id.et_corp);
            String position = etPosition.getText().toString().trim();
            String corp = etCorp.getText().toString().trim();
            json.append(i == 0 ? "{\"title\":\"" : ",{\"title\":\"")
                .append(Utils.urlEncode(position)).append("\",\"company\":\"")
                .append(Utils.urlEncode(corp)).append("\",\"source\":\"")
                .append(1).append("\",\"weekNo\":\"")
                .append(mCurrentWeek).append("\",\"time\":\"")
                .append((mTimes[i]) / 1000).append("\"}");
        }
        json.append("]");
        return json.toString();
    }

    private class AddRecruTask extends MsTask{

        public AddRecruTask() {
            super(AddJobsInfoActivity.this, MsRequest.JOB_INSERT_RECRUIT);
        }

        @Override
        protected String buildParams() {
            return new StringBuilder().append("json=") + getJsonString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                toast("发布成功");
                finish();
            }
        }
    }
}
