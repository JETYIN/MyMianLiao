package com.tjut.mianliao.mycollege;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ProImageView;
import com.tjut.mianliao.component.TagView;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.data.mycollege.MatchJobInfo;
import com.tjut.mianliao.data.mycollege.SearchTagInfo;
import com.tjut.mianliao.data.mycollege.TagInfo;
import com.tjut.mianliao.job.JobDetailActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class IntelMatchActivity extends BaseActivity implements OnClickListener,
        DialogInterface.OnClickListener {

    private enum ViewType {
        TYPE_TAG, TYPE_JOB, TYPE_MORE
    }

    public static final String EXT_TAG_INFO = "ext_tag_info";
    public static final String EXT_MATCH_JOB_INFO = "ext_match_job_info";

    private static final int LIMIT = 5;
    private static final int REQUEST_ADD_TAG_CODE = 100;
    private static final int REQUEST_MATCH_JOBS_CODE = 101;

    private ArrayList<TagInfo> mTags;
    private TagView mTagView;
    private TextView mTvIntro;
    private LinearLayout mLlTag;
    private ListView mLvTagJobs;
    private TextView mTvStart;
    private String mTagsStr, mTag;
    private ArrayList<MatchJobInfo> mJobs;
    private ArrayList<Object> mMatchJobs;
    private MatchJobAdapter mAdapter;
    private LightDialog mDelDialog;
    private SearchTagInfo mCurrentTagInfo;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_intel_match;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getTitleBar().setTitle(R.string.job_intel_match);
        getTitleBar().showRightButton(R.drawable.recruit_botton_bg_up, this);
        mJobs = new ArrayList<MatchJobInfo>();
        mMatchJobs = new ArrayList<Object>();
        mTagView = (TagView) findViewById(R.id.tg_mine);
        mTvIntro = (TextView) findViewById(R.id.tv_tag_intro);
        mLlTag = (LinearLayout) findViewById(R.id.ll_start_add_tag);
        mLvTagJobs = (ListView) findViewById(R.id.lv_job_info);
        mTvStart = (TextView) findViewById(R.id.tv_start_match);
        mTagView.setMarginTopWithItemMargin(false);
        mTagView.setClickables(false);
        mAdapter = new MatchJobAdapter();
        mLvTagJobs.setAdapter(mAdapter);
        showTag(null);
        fillDataFromDb();
        mTvStart.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_right:
                switchBtn();
                break;
            case R.id.ll_start_add_tag:
                addTags();
                break;
            case R.id.tv_start_match:
                startMatchJob();
                break;
            case R.id.iv_delete:
                mCurrentTagInfo = (SearchTagInfo) v.getTag();
                showDelDialog();
                break;
            case R.id.ll_jobs:
                MatchJobInfo job = (MatchJobInfo) v.getTag();
                showMoreTagJobs(job.tagStr);
                break;
            case R.id.ll_more_match_job:
                String tags = (String) v.getTag();
                showMoreTagJobs(tags);
                break;
            default:
                break;
        }
    }

    private void addTags() {
        Intent intent = new Intent(this, AddJobTagActivity.class);
//        intent.putExtra(AddJobTagActivity.EXT_TAG_INFOS, mTags);
        startActivityForResult(intent, REQUEST_ADD_TAG_CODE);
    }

    private void startMatchJob() {
        Intent intent = new Intent(this, MatchingJobActivity.class);
        intent.putExtra(MatchingJobActivity.EXT_TAGS, mTag);
        intent.putExtra(MatchingJobActivity.EXT_TAGS_STR, mTagsStr);
        startActivityForResult(intent, REQUEST_MATCH_JOBS_CODE);
    }

    private String getTagsString() {
        StringBuilder tagStr = new StringBuilder();
        boolean isFirst = true;
        for (TagInfo tag : mTags) {
            if (isFirst) {
                isFirst = false;
            } else {
                tagStr.append(Utils.COMMA_DELIMITER);
            }
            tagStr.append(tag.tagIndex);
        }
        return tagStr.toString();
    }

    private String getShowTagStr(ArrayList<TagInfo> tagInfo) {
        StringBuilder sb = new StringBuilder();
        for (TagInfo tag : tagInfo) {
            sb.append("#").append(tag.name).append("  ");
        }
        return sb.toString();
    }

    private void deleteDatas() {
        String tagIds = mCurrentTagInfo.tagIds;
        if (DataHelper.deleteTagInfo(this, mCurrentTagInfo.id)) {
            if (DataHelper.deleteJobInfo(this, tagIds)) {
                toast("删除成功！");
                fillDataFromDb();
                showButtonAndTag();
            }
        }
    }

    private void showButtonAndTag() {
        if (mMatchJobs.size() < 0) {
            mTagView.setVisibility(View.VISIBLE);
            mTvIntro.setVisibility(View.GONE);
            mTvStart.setEnabled(true);
        }
    }

    private void fillDataFromDb() {
        mMatchJobs.clear();
        ArrayList<SearchTagInfo> tagInfos = DataHelper.loadTagInfos(this, LIMIT);
        if (tagInfos != null && tagInfos.size() > 0) {
            for (SearchTagInfo tag : tagInfos) {
                String [] ids = tag.tagIds.split(Utils.COMMA_DELIMITER);
                mMatchJobs.add(tag);
                for (String id : ids) {
                    MatchJobInfo info = DataHelper.loadMatchJobsInfo(this, Integer.parseInt(id));
                    info.tagStr = tag.tags + ":" + tag.tagStr;
                    mMatchJobs.add(info);
                }
                mMatchJobs.add(tag.tags + ":" + tag.tagStr);
            }
            mLlTag.setVisibility(View.GONE);
            mTvStart.setEnabled(false);
        } else {
            showTagInfo();
        }
        mAdapter.notifyDataSetChanged();
    }

    private void showTagInfo() {
        mLlTag.setVisibility(View.VISIBLE);
        mTvStart.setVisibility(View.VISIBLE);
        showTag(null);
    }

    private void showMoreTagJobs(String tags) {
        String [] tag = tags.split(":");
        Intent intent = new Intent(this, MoreTagJobsActivity.class);
        intent.putExtra(MatchingJobActivity.EXT_TAGS, tag[0]);
        intent.putExtra(MatchingJobActivity.EXT_TAGS_STR, tag[1]);
        startActivity(intent);
    }

    private void showJobDetails(int jobId) {
        Intent intent = new Intent(this, JobDetailActivity.class);
        intent.putExtra(JobDetailActivity.EXT_JOB_ID, jobId);
    }

    private void switchBtn() {
        if(mLlTag.getVisibility() == View.VISIBLE){
            hideTagInfo();
            getTitleBar().showRightButton(R.drawable.recruit_botton_bg_select, this);
        } else {
            showTagInfo();
            getTitleBar().showRightButton(R.drawable.recruit_botton_bg_up, this);
        } 
    }
 
    private void hideTagInfo() {
        mLlTag.setVisibility(View.GONE);
        mTvStart.setVisibility(View.GONE);
    }

    private void showDelDialog() {
        if (mDelDialog == null) {
            mDelDialog = new LightDialog(this)
                    .setTitleLd(getString(R.string.course_time_clear))
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, this);
        }
        mDelDialog.setMessage("您确定要删除这条标签吗？");
        mDelDialog.show();
    }

    private void getMyTag() {
        new GetMyTagTask().executeLong();
    }

    private class GetMyTagTask extends MsTask{

        public GetMyTagTask() {
            super(IntelMatchActivity.this, MsRequest.LIST_MY_JOB_TAGS);
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            getTitleBar().hideProgress();
            if (response.isSuccessful()) {
                ArrayList<TagInfo> tagInfos = JsonUtil.getArray(
                        response.getJsonArray(), TagInfo.TRANSFORMER);
                if (showTag(tagInfos)) {
                    mTvStart.setEnabled(true);
                } else {
                    mTvStart.setEnabled(false);
                }
            }
            fillDataFromDb();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_TAG_CODE) {
            mTagsStr = data.getStringExtra(MatchingJobActivity.EXT_TAGS_STR);
            mTag = data.getStringExtra(MatchingJobActivity.EXT_TAGS);
            mTags = data.getParcelableArrayListExtra(EXT_TAG_INFO);
            if (showTag(mTags)) {
                mTvStart.setVisibility(View.VISIBLE);
                mTvStart.setEnabled(true);
            } else {
                mTvStart.setVisibility(View.VISIBLE);
                mTvStart.setEnabled(false);
            }
        } else if (requestCode == REQUEST_MATCH_JOBS_CODE) {
            mLlTag.setVisibility(View.GONE);
            mTvStart.setEnabled(false);
            mJobs = data.getParcelableArrayListExtra(EXT_MATCH_JOB_INFO);
            SearchTagInfo tagInfo = data.getParcelableExtra(EXT_TAG_INFO);
            if (mJobs != null && mJobs.size() > 0) {
                mMatchJobs.add(0 ,tagInfo.tags + ":" + tagInfo.tagStr);
                for (MatchJobInfo job : mJobs) {
                    job.tagStr = tagInfo.tags + ":" + tagInfo.tagStr;
                    mMatchJobs.add(0 ,job);
                }
                mMatchJobs.add(0 ,tagInfo);
                mAdapter.notifyDataSetChanged();
                mTvStart.setVisibility(View.GONE);
            } else {
                toast(R.string.mc_match_no_job);
                if (mMatchJobs.size() <= 0) {
                    showTagInfo();
                    mTvStart.setVisibility(View.GONE);
                }
            }
        }
    }

    public boolean showTag(ArrayList<TagInfo> tagInfos) {
        if (tagInfos == null || tagInfos.size() == 0) {
            mTagView.setVisibility(View.GONE);
            mTvIntro.setVisibility(View.VISIBLE);
            return false;
        } else {
            mTags = tagInfos;
            mTagView.setVisibility(View.VISIBLE);
            mTvIntro.setVisibility(View.GONE);
            mTagView.updateView(mTags);
            mTag = getTagsString();
            mTagsStr = getShowTagStr(tagInfos);
            return true;
        }
    }

    private class MatchJobAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mMatchJobs.size();
        }

        @Override
        public Object getItem(int position) {
            return mMatchJobs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return ViewType.values().length;
        }

        @Override
        public int getItemViewType(int position) {
            Object obj = getItem(position);
            if (obj instanceof SearchTagInfo) {
                return ViewType.TYPE_TAG.ordinal();
            } else if (obj instanceof MatchJobInfo) {
                return ViewType.TYPE_JOB.ordinal();
            } else {
                return ViewType.TYPE_MORE.ordinal();
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewType type = ViewType.values()[getItemViewType(position)];
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = inflateView(type, parent);
            }

            if (view == null) {
                return null;
            }
            Object obj = getItem(position);
            view.setTag(obj);
            view.setOnClickListener(IntelMatchActivity.this);
            if (obj instanceof SearchTagInfo) {
                view.setOnClickListener(null);
                updateTagView(view, (SearchTagInfo) obj);
            } else if (obj instanceof MatchJobInfo) {
                updateJobView(view, (MatchJobInfo) obj);
            }
            return view;
        }


    }

    public View inflateView(ViewType type, ViewGroup parent) {
        switch (type) {
            case TYPE_TAG:
                return mInflater.inflate(R.layout.item_my_tag_info, parent, false);
            case TYPE_JOB:
                return mInflater.inflate(R.layout.list_item_jobs, parent, false);
            case TYPE_MORE:
                return mInflater.inflate(R.layout.list_item_match_more_jobs, parent, false);
            default:
                return null;
        }
    }

    public void updateTagView(View view, SearchTagInfo tag) {
        TextView tvTag = (TextView) view.findViewById(R.id.tv_my_tags);
        TextView tvTime = (TextView) view.findViewById(R.id.tv_publish_time);
        ImageView ivDel = (ImageView) view.findViewById(R.id.iv_delete);
        tvTag.setText(tag.tagStr);
        tvTime.setText(Utils.getTimeDesc(tag.time));
        ivDel.setTag(tag);
        ivDel.setOnClickListener(this);
    }

    public void updateJobView(View view, MatchJobInfo job) {
        ProImageView icon = (ProImageView) view.findViewById(R.id.iv_icon);
        TextView tvName = (TextView) view.findViewById(R.id.tv_job_name);
        TextView tvSalary = (TextView) view.findViewById(R.id.tv_salary);
        TextView tvComp = (TextView) view.findViewById(R.id.tv_comp);
        TextView tvLoc = (TextView) view.findViewById(R.id.tv_loc);
        TextView tvTime = (TextView) view.findViewById(R.id.tv_time);
        view.setTag(job);
        view.setOnClickListener(this);
        tvName.setText(job.jobTitle);
        tvSalary.setText(job.salary);
        tvComp.setText(job.corpName);
        tvLoc.setText(job.localCity);
        icon.setImage(job.corpLogo, R.drawable.ic_avatar_corp);
        tvTime.setText(Utils.getTimeDesc(job.publishTime));
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            deleteDatas();
        }
    }

}
