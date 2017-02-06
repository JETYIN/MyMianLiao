package com.tjut.mianliao.job;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.PropertiesView;
import com.tjut.mianliao.component.tab.TabController;
import com.tjut.mianliao.component.tab.TextTab;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Forum;
import com.tjut.mianliao.data.job.Corp;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.forum.CourseForumActivity;
import com.tjut.mianliao.profile.IdVerifyActivity;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.SnsHelper;
import com.tjut.mianliao.util.Utils;

public class JobDetailActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    public static final String EXTRA_SHOW_CORP_INFO = "show_corp_info";
    public static final String EXT_JOB_ID = "ext_job_id";

    private Job mJob;

    private LightDialog mInvitationDialog;
    private LightDialog mAuthDialog;
    private SnsHelper mSnsHelper;
    private TabController mTabController;

    private TextView mTvApplyNow, mTvShare;
    private ArrayList<Job> mOtherJobs = new ArrayList<Job>();
    private boolean mIsApplying;
    private int mJobId;

    private TextTab mTtJob, mTtComp;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_job_detail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().showTitleText(R.string.job_detail_title, null);
        mSnsHelper = SnsHelper.getInstance();
        mJobId = getIntent().getIntExtra(EXT_JOB_ID, 0);
        mJob = getIntent().getParcelableExtra(Job.INTENT_EXTRA_NAME);
        mTvApplyNow = (TextView) findViewById(R.id.tv_apply_now);
        mTvShare = (TextView) findViewById(R.id.tv_share);

        if (mJob == null && mJobId > 0) {
           getJobInfo();
        } else {
            updateView();
        }
    }

    private void getJobInfo() {
        new GetJobInfoTask().executeLong();
    }

    private void updateActionButton() {
        if (mJob.isOffline()) {
            mTvApplyNow.setText(R.string.job_download_form);
        } else if (mJob.isInvited()) {
            mTvApplyNow.setText(R.string.job_invited);
        } else if (mJob.isApplied()) {
            mTvApplyNow.setText(R.string.job_applied_resume);
        }
    }

    private void showJobInfo(Job job) {
        setText(R.id.tv_job_title, job.title);
        setText(R.id.tv_utime, getString(R.string.news_published_on,
                Utils.getTimeDesc(job.uTime)));
        setText(R.id.tv_salary, job.salary);
        setText(R.id.tv_job_intro, job.intro);

        PropertiesView pv = (PropertiesView) findViewById(R.id.pv_job_props);
        pv.show(job.properties);
    }

    private void showCorpInfo(Corp corp) {
        ListView lvCorpInfo = (ListView) findViewById(R.id.lv_corp_info);
        View header = mInflater.inflate(R.layout.list_header_corp_info, lvCorpInfo, false);

        Utils.setText(header, R.id.tv_corp_name, mJob.corpName);
        Utils.setText(header, R.id.tv_corp_intro, corp.intro);
        ((RatingBar) header.findViewById(R.id.rb_rank)).setRating(mJob.corpRank);

        PropertiesView pv = (PropertiesView) header.findViewById(R.id.pv_corp_props);
        pv.show(corp.properties);

        if (mOtherJobs.size() == 0) {
            header.findViewById(R.id.fl_other_jobs).setVisibility(View.GONE);
        }

        lvCorpInfo.addHeaderView(header);
        lvCorpInfo.setAdapter(mAdapter);
        lvCorpInfo.setOnItemClickListener(this);

        ((ProImageView) header.findViewById(R.id.iv_corp_logo))
                .setImage(mJob.corpLogo, R.drawable.ic_avatar_corp);
    }

    public void updateView() {
        updateActionButton();
        showJobInfo(mJob);
        mTvShare.setText(getString(R.string.share));
        mTvApplyNow.setText(mJob.isInvited() ? getString(R.string.job_delivered) :
            getString(R.string.job_apply_now));
        mTvApplyNow.setBackgroundResource(mJob.isInvited() ?
                R.drawable.btn_gray : R.drawable.selector_btn_blue);
        if (mJob.corpId > 0 && getIntent().getBooleanExtra(EXTRA_SHOW_CORP_INFO, true)) {
            findViewById(R.id.ll_tab).setVisibility(View.VISIBLE);
            mTabController = new TabController();
            mTtJob = new TextTab((TextView) findViewById(R.id.tv_job_info));
            mTtComp = new TextTab((TextView) findViewById(R.id.tv_corp_info));
            mTtJob.setPage(findViewById(R.id.ll_job_info));
            mTtComp.setPage(findViewById(R.id.lv_corp_info));
            mTtComp.setChosen(false);
            mTabController.add(mTtJob);
            mTabController.add(mTtComp);
            mTabController.select(0);
            new GetCorpInfoTask().executeLong();
        }
    }

    private void share() {
        mSnsHelper.openShareBoard(this, mJob);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_apply_now:
                if (!AccountInfo.getInstance(this).getUserInfo().isVerified()) {
                    showAuthDialog();
                } else if (mJob.isOffline()) {
                    Utils.actionView(this, mJob.attachment, null, 0);
                } else if (!mIsApplying) {
                    if (mJob.isInvited()) {
                        showInvitationDialog();
                    } else {
                        Intent i = new Intent(this, ChooseResumeActivity.class)
                                .putExtra(Job.INTENT_EXTRA_NAME, mJob);
                        startActivityForResult(i, getIdentity());
                    }
                }
                break;

            case R.id.ll_job_forum:
                startActivity(new Intent(this, CourseForumActivity.class)
                            .putExtra(Forum.INTENT_EXTRA_NAME, Forum.JOB_FORUM));
                break;
            case R.id.tv_share:
                share();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Job job = (Job) parent.getItemAtPosition(position);
        Intent i = new Intent(this, JobDetailActivity.class);
        i.putExtra(Job.INTENT_EXTRA_NAME, job);
        i.putExtra(EXTRA_SHOW_CORP_INFO, false);
        startActivityForResult(i, getIdentity());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mSnsHelper.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_UPDATED && requestCode == getIdentity()) {
            Job job = data.getParcelableExtra(Job.INTENT_EXTRA_NAME);
            if (job != null) {
                if (job.id == mJob.id) {
                    mJob.offerStatus = job.offerStatus;
                    updateActionButton();
                } else {
                    for (Job j : mOtherJobs) {
                        if (j.id == job.id) {
                            j.offerStatus = job.offerStatus;
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
        }
    }

    private void showInvitationDialog() {
        if (mInvitationDialog == null) {
            mInvitationDialog = new LightDialog(this);
            mInvitationDialog.setTitle(R.string.job_invited);
            mInvitationDialog.setMessage(R.string.job_invited_desc);
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            new AcceptInviteTask(true).executeLong();
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            new AcceptInviteTask(false).executeLong();
                            break;
                        default:
                            break;
                    }
                }
            };
            mInvitationDialog.setPositiveButton(R.string.agree, listener);
            mInvitationDialog.setNegativeButton(R.string.refuse, listener);
        }
        mInvitationDialog.show();
    }

    private void showAuthDialog() {
        if (mAuthDialog == null) {
            mAuthDialog = new LightDialog(this);
            mAuthDialog.setTitle(R.string.iv_title);
            mAuthDialog.setMessage(R.string.job_not_verified);
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(getApplicationContext(), IdVerifyActivity.class));
                }
            };
            mAuthDialog.setPositiveButton(android.R.string.ok, listener);
            mAuthDialog.setNegativeButton(android.R.string.cancel, null);
        }
        mAuthDialog.show();
    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mOtherJobs.size();
        }

        @Override
        public Object getItem(int position) {
            return mOtherJobs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mOtherJobs.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = getLayoutInflater().inflate(R.layout.list_item_other_job, parent, false);
            }
            Job job = mOtherJobs.get(position);

            TextView tvAction = (TextView) view.findViewById(R.id.tv_action);
            tvAction.setEnabled(!job.isApplied());
            tvAction.setText(job.isApplied() ? R.string.job_applied_resume :
                    R.string.job_take_a_look);
            Utils.setText(view, R.id.tv_job_desc, getString(R.string.job_desc,
                    job.title, job.quota));
            return view;
        }
    };

    private class GetCorpInfoTask extends MsTask {

        public GetCorpInfoTask() {
            super(getApplicationContext(), MsRequest.CORP_INFO);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "corp_id=" + mJob.corpId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                Corp corp = Corp.fromJson(response.getJsonObject());
                if (corp != null) {
                    mOtherJobs.addAll(corp.jobs);
                    mOtherJobs.remove(mJob);
                    showCorpInfo(corp);
                }
            } else {
                response.showFailInfo(getRefContext(), R.string.job_get_corp_info_failed);
            }
        }
    }

    private class AcceptInviteTask extends MsTask {
        private int acceptCode;

        public AcceptInviteTask(boolean accepted) {
            super(getApplicationContext(), MsRequest.CANDIDATE_ACCEPT);
            acceptCode = accepted ? 1 : -1;
        }

        @Override
        protected String buildParams() {
            return "offer_id=" + mJob.offerId + "&accept=" + acceptCode;
        }

        @Override
        protected void onPreExecute() {
            mIsApplying = true;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            mIsApplying = false;
            if (MsResponse.isSuccessful(response)) {
                toast(acceptCode == 1 ? R.string.job_invite_accepted : R.string.job_invite_refused);
                mJob.offerStatus = response.json.optInt(MsResponse.PARAM_RESPONSE);
                updateActionButton();
                setResult(RESULT_UPDATED, new Intent().putExtra(Job.INTENT_EXTRA_NAME, mJob));
            } else {
                int resId = acceptCode == 1 ? R.string.job_invite_accept_failed :
                        R.string.job_invite_refuse_failed;
                toast(MsResponse.getFailureDesc(getApplicationContext(), resId, response.code));
            }
        }
    }

    private class GetJobInfoTask extends MsTask{

        public GetJobInfoTask() {
            super(JobDetailActivity.this, MsRequest.JOB_INFO);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected String buildParams() {
            return "job_id=" + mJobId;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                mJob = Job.fromJson(response.getJsonObject());
                updateView();
            }
        }

    }
}