package com.tjut.mianliao.job;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CommonBanner;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.ProgressButton;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Option;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class JobsActivity extends BaseActivity implements Runnable, SearchView.OnSearchTextListener,
        View.OnClickListener, DialogInterface.OnClickListener, PullToRefreshBase.OnRefreshListener2<ListView>,
        AdapterView.OnItemClickListener {

    private static final long DELAY_MILLS = 500;

    private Handler mHandler;
    private String mSearchKey;
    private OptionManager mOptionManager;
    private CommonBanner mVsSwitcher;

    private PullToRefreshListView mPtrJobs;
    private TextView mTvLocation;
    private TextView mTvCategory;
    private TextView mTvType;
    private LocationPicker mLocationPicker;

    private LightDialog mMenuDialog;
    private LightDialog mLocationDialog;
    private LightDialog mCategoryDialog;
    private LightDialog mTypeDialog;

    private Option mLocation;
    private Option mCategory;
    private Option mType;

    private JobAdapter mJobAdapter;

    private SearchJobTask mLastTask;

    private boolean mIsVerified;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_jobs;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsVerified = AccountInfo.getInstance(this).getUserInfo().isVerified();

        getTitleBar().showTitleText(R.string.job_title, null);

        mHandler = new Handler();
        mOptionManager = OptionManager.getInstance(this);

        mPtrJobs = (PullToRefreshListView) findViewById(R.id.ptrlv_jobs);
        mPtrJobs.setOnRefreshListener(this);
        mPtrJobs.setMode(PullToRefreshBase.Mode.BOTH);

        ListView lvJobs = mPtrJobs.getRefreshableView();
        View header = mInflater.inflate(R.layout.list_header_jobs, lvJobs, false);
        mTvLocation = (TextView) header.findViewById(R.id.tv_location);
        mTvCategory = (TextView) header.findViewById(R.id.tv_work_category);
        mTvType = (TextView) header.findViewById(R.id.tv_work_type);
        SearchView searchView = (SearchView) header.findViewById(R.id.sv_search);
        searchView.setHint(R.string.job_search_hint);
        searchView.setOnSearchTextListener(this);
        lvJobs.addHeaderView(header);

        mVsSwitcher =  (CommonBanner) header.findViewById(R.id.vs_switcher);
        mVsSwitcher.setParam(CommonBanner.Plate.RecruitQueryMain, 0);

        mJobAdapter = new JobAdapter();
        lvJobs.setAdapter(mJobAdapter);
        lvJobs.setOnItemClickListener(this);

        searchJob(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getIdentity() && resultCode == RESULT_UPDATED) {
            Job job = data.getParcelableExtra(Job.INTENT_EXTRA_NAME);
            int position = mJobAdapter.getPosition(job);
            if (position != -1) {
                mJobAdapter.getItem(position).offerStatus = job.offerStatus;
                mJobAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void run() {
        searchJob(true);
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        getTitleBar().showProgress();
        mSearchKey = text.toString();
        mHandler.removeCallbacks(this);
        mHandler.postDelayed(this, DELAY_MILLS);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_right:
                if (authCheck()) {
                    showMenuDialog();
                }
                break;
            case R.id.fl_location:
                showLocationDialog();
                break;
            case R.id.fl_work_category:
                showCategoryDialog();
                break;
            case R.id.fl_work_type:
                showTypeDialog();
                break;
            case R.id.pb_apply_job:
                if (v.getTag() != null && v.getTag() instanceof Job) {
                    Job job = (Job) v.getTag();
                    Intent i = new Intent(this, ChooseResumeActivity.class).putExtra(Job.INTENT_EXTRA_NAME, job);
                    startActivityForResult(i, getIdentity());
                    // if (!mIsVerified || job.isOffline() || job.isInvited()) {
                    // showJobDetail(job);
                    // } else if (authCheck() && !job.applying) {
                    // Intent i = new Intent(this, ChooseResumeActivity.class)
                    // .putExtra(Job.INTENT_EXTRA_NAME, job);
                    // startActivityForResult(i, getIdentity());
                    // }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mMenuDialog) {
            onMenuItemClicked(which);
        } else if (dialog == mLocationDialog) {
            updateLocation(mLocationPicker.getLocation());
        } else if (dialog == mCategoryDialog) {
            updateCategory(mOptionManager.getCategory(which));
        } else if (dialog == mTypeDialog) {
            updateType(mOptionManager.getType(which));
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        searchJob(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        searchJob(false);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        showJobDetail((Job) parent.getItemAtPosition(position));
    }

    private void onMenuItemClicked(int which) {
        switch (which) {
            case 0:
                startActivity(new Intent(this, ResumeActivity.class));
                break;
            case 1:
                startActivity(new Intent(this, UploadResumeActivity.class));
                break;
            default:
                break;
        }
    }

    private boolean authCheck() {
        if (!mIsVerified) {
            toast(R.string.job_not_verified);
            return false;
        }
        return true;
    }

    private void showJobDetail(Job job) {
        Intent i = new Intent(this, JobDetailActivity.class).putExtra(Job.INTENT_EXTRA_NAME, job);
        startActivityForResult(i, getIdentity());
    }

    private void showMenuDialog() {
        if (mMenuDialog == null) {
            mMenuDialog = new LightDialog(this).setTitleLd(R.string.please_choose).setItems(R.array.menu_resume, this);
        }
        mMenuDialog.show();
    }

    private void showLocationDialog() {
        if (mLocationDialog == null) {
            mLocationPicker = new LocationPicker(this);
            mLocationPicker.setLocations(mOptionManager.getLocations());
            mLocationDialog = new LightDialog(this).setTitleLd(R.string.please_choose)
                    .setView(mLocationPicker.getView()).setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, this);
        }
        mLocationPicker.setLocation(mLocation);
        mLocationDialog.show();
    }

    private void showCategoryDialog() {
        if (mCategoryDialog == null) {
            mCategoryDialog = new LightDialog(this).setTitleLd(R.string.please_choose).setItems(
                    mOptionManager.getCategories(), this);
        }
        mCategoryDialog.show();
    }

    private void showTypeDialog() {
        if (mTypeDialog == null) {
            mTypeDialog = new LightDialog(this).setTitleLd(R.string.please_choose).setItems(mOptionManager.getTypes(),
                    this);
        }
        mTypeDialog.show();
    }

    private void updateLocation(Option location) {
        if (mLocation != location) {
            mLocation = location;
            if (location == null || location.id == 0) {
                mTvLocation.setText(R.string.job_work_location);
            } else {
                mTvLocation.setText(location.getFullDesc());
            }
            searchJob(true);
        }
    }

    private void updateCategory(Option category) {
        if (mCategory != category) {
            mCategory = category;
            if (category == null || category.id == 0) {
                mTvCategory.setText(R.string.job_work_category);
            } else {
                mTvCategory.setText(category.getFullDesc());
            }
            searchJob(true);
        }
    }

    private void updateType(Option type) {
        if (mType != type) {
            mType = type;
            if (type == null || type.id == 0) {
                mTvType.setText(R.string.job_work_type);
            } else {
                mTvType.setText(type.getFullDesc());
            }
            searchJob(true);
        }
    }

    private void searchJob(boolean refresh) {
        int offset = refresh ? 0 : mJobAdapter.getCount();
        new SearchJobTask(mSearchKey, offset).executeLong();
    }

    private class JobAdapter extends ArrayAdapter<Job> {
        private int mKeyColor;
        private String mKeyword;

        public JobAdapter() {
            super(getApplicationContext(), 0);
            mKeyColor = getResources().getColor(R.color.txt_keyword);
        }

        public void setKeyword(String keyword) {
            mKeyword = keyword;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_job, parent, false);
            }
            Job job = getItem(position);
            Utils.setText(view, R.id.tv_title, Utils.getColoredText(job.title, mKeyword, mKeyColor));
            Utils.setText(view, R.id.tv_corp_desc, job.corpName);
            Utils.setText(view, R.id.tv_salary, job.salary);
            Utils.setText(view, R.id.tv_utime, Utils.getTimeDesc(job.uTime));

            // ((RatingBar)
            // view.findViewById(R.id.rb_rank)).setRating(job.corpRank);

            ((ProImageView) view.findViewById(R.id.iv_corp_logo)).setImage(job.corpLogo, R.drawable.find_botton_icon);

            updateApplyButton(view, job);

            return view;
        }

        private void updateApplyButton(View view, Job job) {
            ProgressButton pbApply = (ProgressButton) view.findViewById(R.id.pb_apply_job);
            pbApply.setEnabled(!job.isApplied());
            pbApply.setInProgress(job.applying);
            pbApply.setTag(job);
//            pbApply.setOnClickListener(JobsActivity.this);
            if (!mIsVerified || job.isOffline()) {
                pbApply.setText(R.string.job_take_a_look);
                pbApply.setEnabled(true);
            } else if (job.isInvited()) {
                pbApply.setText(R.string.job_invited);
            } else if (job.isApplied()) {
                pbApply.setText(R.string.job_applied_resume);
            } else {
                pbApply.setText(R.string.job_apply_resume);
            }
        }
    }

    private class SearchJobTask extends MsTask {
        private String mKey;
        private int mOffset;

        public SearchJobTask(String key, int offset) {
            super(getApplicationContext(), MsRequest.SEARCH_JOB);
            mKey = key;
            mOffset = offset;
        }

        @Override
        protected String buildParams() {
            return new StringBuilder("offset=").append(mOffset).append("&title=").append(Utils.urlEncode(mKey))
                    .append("&location_id=")
                    .append(mLocation == null ? 0 : mLocation.id == 0 ? mLocation.parentId : mLocation.id)
                    .append("&category=").append(mCategory == null ? 0 : mCategory.id).append("&type=")
                    .append(mType == null ? 0 : mType.id).toString();
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            if (mLastTask != null) {
                mLastTask.cancel(true);
            }
            mLastTask = this;
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            mPtrJobs.onRefreshComplete();
            mLastTask = null;
            if (MsResponse.isSuccessful(response)) {
                mJobAdapter.setNotifyOnChange(false);
                if (mOffset == 0) {
                    mJobAdapter.clear();
                }
                mJobAdapter.addAll(JsonUtil.getArray(response.getJsonArray(), Job.TRANSFORMER));
                mJobAdapter.setKeyword(mKey);
                mJobAdapter.notifyDataSetChanged();
            } else {
                response.showFailInfo(getRefContext(), R.string.job_list_job_failed);
            }
        }
    }
}