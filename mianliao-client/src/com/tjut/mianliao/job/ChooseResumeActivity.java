package com.tjut.mianliao.job;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.ProgressButton;
import com.tjut.mianliao.data.ResumeAlt;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class ChooseResumeActivity extends BaseActivity {

    private Job mJob;
    private ListView mListView;
    private ResumeAdapter mAdapter;

    private ArrayList<CharSequence> mResumes = new ArrayList<CharSequence>();

    private ArrayList<ResumeAlt> mResumeAltList = new ArrayList<ResumeAlt>();

    private ProgressButton mDeliverButton;
    private EditText mPostScript;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_choose_resume;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJob = getIntent().getParcelableExtra(Job.INTENT_EXTRA_NAME);

        if (mJob == null) {
            toast(R.string.job_not_found);
            finish();
            return;
        }

        getTitleBar().setTitle(getString(R.string.rsm_tst_choose_resume));

        mAdapter = new ResumeAdapter(this, R.layout.list_item_checkable_resume,
                R.id.tv_resume_info, mResumes);

        mListView = (ListView) findViewById(R.id.lv_resume);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.notifyDataSetChanged();
            }
        });

        mDeliverButton = (ProgressButton) findViewById(R.id.pb_deliver_resume);
        mPostScript = (EditText) findViewById(R.id.et_postscript);
        new LoadResumeTask().executeLong();
    }

    private class ResumeAdapter extends ArrayAdapter<CharSequence> {

        public ResumeAdapter(Context context, int resource,
                             int textViewResourceId, List<CharSequence> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);

            boolean isChecked = mListView.isItemChecked(position);
            ((CheckBox) v.findViewById(R.id.cb_check)).setChecked(isChecked);

            return v;
        }
    }

    private class LoadResumeTask extends MsTask {

        public LoadResumeTask() {
            super(getApplicationContext(), MsRequest.JOB_MY_RESUME_ALT);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();

            if (MsResponse.isSuccessful(response)) {
                mResumes.add(getString(R.string.rsm_tst_normal_resume));

                mResumeAltList = JsonUtil.getArray(
                        response.getJsonArray(), ResumeAlt.TRANSFORMER);

                for (ResumeAlt resume : mResumeAltList) {
                    mResumes.add(Html.fromHtml(getString(
                            R.string.att_information, resume.att.name,
                            Utils.getAttSizeString(getRefContext(), resume.att.size))));
                }

                mAdapter.notifyDataSetChanged();

                mPostScript.setVisibility(View.VISIBLE);
                mDeliverButton.setVisibility(View.VISIBLE);
                updateActionButton();
                mListView.setItemChecked(0, true);
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.rsm_tst_load_failed, response.code));
            }
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.pb_deliver_resume) {
            if (!mDeliverButton.isInProgress()) {
                new ApplyJobTask().executeLong();
            }
        }
    }

    private class ApplyJobTask extends MsTask {
        private int mResumeType;

        public ApplyJobTask() {
            super(getApplicationContext(), MsRequest.CANDIDATE_REQUEST);
        }

        @Override
        protected void onPreExecute() {
            mDeliverButton.setInProgress(true);
        }

        @Override
        protected String buildParams() {
            int checkedPosition = mListView.getCheckedItemPosition();
            mResumeType = checkedPosition > 0 ? 1 : 0;

            StringBuilder params = new StringBuilder()
                    .append("job_id=" + mJob.id)
                    .append("&rtype=" + mResumeType);

            if (mResumeType == 1) {
                params.append("&resume_alt_id=" + mResumeAltList.get(checkedPosition - 1).id);
            }

            String msg = mPostScript.getText().toString();
            if (!TextUtils.isEmpty(msg)) {
                params.append("&msg=" + Utils.urlEncode(msg));
            }

            return params.toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            if (MsResponse.isSuccessful(response)) {
                mJob.offerStatus = response.json.optInt(MsResponse.PARAM_RESPONSE);
                toast(new StringBuilder(getString(R.string.job_applied_job)).append(mJob.title));
                updateActionButton();
                setResult(RESULT_UPDATED, new Intent().putExtra(Job.INTENT_EXTRA_NAME, mJob));
            } else if (mResumeType == 0 && response.code == MsResponse.MS_JOB_RESUME_NOT_EXIST) {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.job_apply_failed, response.code));
                startActivity(new Intent(ChooseResumeActivity.this, ResumeActivity.class));
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.job_apply_failed, response.code));
            }

            mDeliverButton.setInProgress(false);
        }
    }

    private void updateActionButton() {
        if (mJob.isApplied()) {
            mDeliverButton.setEnabled(false);
            mDeliverButton.setText(R.string.job_applied_resume);
            mDeliverButton.setTextColor(getResources().getColor(R.color.white));
        } else {
            mDeliverButton.setText(R.string.rsm_tst_deliver_resume);
        }
    }
}
