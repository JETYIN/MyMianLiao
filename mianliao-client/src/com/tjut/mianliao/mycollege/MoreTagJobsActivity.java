package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;

public class MoreTagJobsActivity extends BaseActivity {


    private String mTags, mTagStr;
    private ArrayList<Job> mJobs;
    private JobAdapter mAdapter;

    private ListView mLvJobs;
    private TextView mTvTag;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_more_tag_jobs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle("查看更多");
        mTags = getIntent().getStringExtra(MatchingJobActivity.EXT_TAGS);
        mTagStr = getIntent().getStringExtra(MatchingJobActivity.EXT_TAGS_STR);
        findViewById(R.id.ll_operate).setVisibility(View.GONE);
        mTvTag = (TextView) findViewById(R.id.tv_my_tags);
        mJobs = new ArrayList<Job>();
        mLvJobs = (ListView) findViewById(R.id.lv_jobs);
        mAdapter = new JobAdapter(this);
        mAdapter.addAll(mJobs);
        mLvJobs.setAdapter(mAdapter);
        mTvTag.setText(mTagStr);
        getJobByTags();
    }

    private void getJobByTags() {
        new MatchJobByTags().executeLong();
    }

    private class MatchJobByTags extends MsTask{

        public MatchJobByTags() {
            super(MoreTagJobsActivity.this, MsRequest.LIST_JOB_BY_TAGS);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "job_tags=" + mTags;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                mJobs = JsonUtil.getArray(response.getJsonArray(), Job.TRANSFORMER);
                mAdapter.clear();
                mAdapter.addAll(mJobs);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
