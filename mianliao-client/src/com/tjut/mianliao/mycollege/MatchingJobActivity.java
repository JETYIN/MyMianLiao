package com.tjut.mianliao.mycollege;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.data.mycollege.MatchJobInfo;
import com.tjut.mianliao.data.mycollege.SearchTagInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class MatchingJobActivity extends BaseActivity {

    public static final String EXT_TAGS = "ext_tags";
    public static final String EXT_TAGS_STR = "ext_tags_str";
    private static final int SAVE_DATA_MAX_COUNT = 3;

    private TextView mTvTags;
    private TextView mTvMathJobDes;
    private String mTags, mTagStr;
    private ArrayList<Job> mJobs;
    private ArrayList<Job> mShowJobs;
    private ArrayList<MatchJobInfo> mJobInfos;
    private long mTime;
    private ListView mLvJobs;
    private Animation mAnimation;
    private float mHeight;
    private JobShowAdapter mAdapter;
    private int mNameColor;
    private Timer mTimer;
    private TimerTask mTask;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            CharSequence content = Utils.getColoredText(
                    getString(R.string.mc_match_job_count_des, mAdapter.getCount()),
                    String.valueOf(mAdapter.getCount()), mNameColor, false);
            mTvMathJobDes.setText(content);
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected int getLayoutResID() {
        return 0;
    }

    @Override
    protected int getBaseLayoutResID() {
        return R.layout.activity_matching_job;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return true;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);
        mJobInfos = new ArrayList<MatchJobInfo>();
        mShowJobs = new ArrayList<Job>();
        mNameColor = 0XFF33B6BE;
        findViewById(R.id.ll_operate).setVisibility(View.GONE);
        mTags = getIntent().getStringExtra(EXT_TAGS);
        mTagStr = getIntent().getStringExtra(EXT_TAGS_STR);
        mTvTags = (TextView) findViewById(R.id.tv_my_tags);
        mTvMathJobDes = (TextView) findViewById(R.id.tv_match_success);
        mLvJobs = (ListView) findViewById(R.id.lv_matched_job);
        mAdapter = new JobShowAdapter();
        mLvJobs.setAdapter(mAdapter);
        if (mTags != null) {
            mTvTags.setText(getShowTag());
        }
        startMatch();
    }

    private String getShowTag() {
        String [] tags = mTagStr.split(Utils.COMMA_DELIMITER);
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append("#").append(tag).append("  ");
        }
        return sb.toString();
    }

    private void showJobs() {
        mShowJobs.clear();
        ArrayList<Job> jobs = new ArrayList<Job>();
        if (mJobs.size() <= 3) {
            jobs = mJobs;
        } else {
            for (int i = 0; i < 3; i++) {
                jobs.add(mJobs.get(i));
            }
        }
        mJobs = jobs;
        mTimer = new Timer();
        showAnimation();
    }

    private void showAnimation() {
        mTask = new TimerTask() {

            @Override
            public void run() {
                if (mShowJobs.size() < mJobs.size()) {
                    mShowJobs.add(0, mJobs.get(mAdapter.getCount()));
                    mHandler.sendEmptyMessage(Message.obtain().what);
                } else {
                    stop();
                }
            }
        };
        mTimer.schedule(mTask, 500, 1500);
    }

    private void stop() {
        mTimer.cancel();
        mTimer = null;
        mTask = null;
        saveDatas();
    }

    private void startMatch() {
        new MatchJobByTags().executeLong();
    }

    private void saveDatas() {
        fillMatchJobInfo();
        saveAndQuit(getMatchJobIdStr());
    }

    private void saveAndQuit(String tagIds) {
        if (tagIds.length() > 0) {
            SearchTagInfo tagInfo = new SearchTagInfo();
            tagInfo.tagIds = tagIds;
            tagInfo.tags = mTags;
            tagInfo.time = mTime;
            tagInfo.tagStr = mTagStr;
            if (DataHelper.insertTagInfo(this, tagInfo)) {
                quit(tagInfo);
            }
        }
    }

    private String getMatchJobIdStr() {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (MatchJobInfo jobInfo : mJobInfos) {
            long jobId = DataHelper.insertMathchJobInfo(this, jobInfo);
            if (jobId > 0) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    sb.append(",");
                }
                sb.append(jobId);
            }
        }
        return sb.toString();
    }

    private void fillMatchJobInfo() {
        for (Job job : mJobs.size() <= SAVE_DATA_MAX_COUNT ? mJobs : mJobs.subList(0, 2)) {
            MatchJobInfo jobInfo = new MatchJobInfo();
            jobInfo.jobId = job.id;
            jobInfo.corpLogo = job.corpLogo;
            jobInfo.corpName = job.corpName;
            jobInfo.jobTitle = job.title;
            jobInfo.publishTime = job.cTime;
            jobInfo.salary = job.salary;
            jobInfo.localCity = job.locCityName;
            mJobInfos.add(jobInfo);
        }
    }

    private void quit(SearchTagInfo tagInfo) {
        Intent data = new Intent();
        data.putExtra(IntelMatchActivity.EXT_MATCH_JOB_INFO, mJobInfos);
        data.putExtra(IntelMatchActivity.EXT_TAG_INFO, tagInfo);
        setResult(RESULT_OK, data);
        finish();
    }

    private class MatchJobByTags extends MsTask{

        public MatchJobByTags() {
            super(MatchingJobActivity.this, MsRequest.LIST_JOB_BY_TAGS);
        }

        @Override
        protected void onPreExecute() {
            mTime = System.currentTimeMillis();
        }

        @Override
        protected String buildParams() {
            return "job_tags=" + mTags;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                mJobs = JsonUtil.getArray(response.getJsonArray(), Job.TRANSFORMER);
                if (mJobs != null && mJobs.size() > 0) {
                    showJobs();
                } else {
                    mTvTags.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            quit(null);
                        }
                    }, 1000);
                }
            }
        }
    }

    private class JobShowAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mShowJobs.size();
        }

        @Override
        public Job getItem(int position) {
            return mShowJobs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView != null){
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_matched_info, parent, false);
            }
            view.measure(0, 0);
            mHeight = view.getMeasuredHeight();
            Job job = getItem(position);
            mAnimation = new TranslateAnimation(0f, 0f, mHeight * (-1), 0f);
            mAnimation.setDuration(1000);
            TextView tvTag = (TextView) view.findViewById(R.id.tv_tag);
            TextView tvPos = (TextView) view.findViewById(R.id.tv_position_desc);
            TextView tvCorp = (TextView) view.findViewById(R.id.tv_crop_name);
            tvTag.setText(getString(R.string.mc_match_by_tag, job.tag));
            tvPos.setText(job.title);
            tvCorp.setText(job.corpName);
            if (!view.isFocused()) {
                view.startAnimation(mAnimation);
            }
            return view;
        }

    }
}
