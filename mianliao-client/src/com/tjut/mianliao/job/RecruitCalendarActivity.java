package com.tjut.mianliao.job;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.PopupView;
import com.tjut.mianliao.component.PopupView.OnItemClickListener;
import com.tjut.mianliao.component.PopupView.PopupItem;
import com.tjut.mianliao.component.ThemeFrameLayout;
import com.tjut.mianliao.curriculum.CourseManager;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.curriculum.CourseView;
import com.tjut.mianliao.curriculum.cell.BaseCellAdapter;
import com.tjut.mianliao.curriculum.cell.CellLayout;
import com.tjut.mianliao.curriculum.cell.CellLayout.OnClickListener;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.data.job.Job;
import com.tjut.mianliao.data.job.RecruitInfo;
import com.tjut.mianliao.data.mycollege.WeekInfo;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.im.IMResourceManager.IMResourceListener;
import com.tjut.mianliao.mycollege.AddJobsInfoActivity;
import com.tjut.mianliao.mycollege.RecruScanActivity;
import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class RecruitCalendarActivity extends BaseActivity implements CellLayout.OnCellClickListener,
        View.OnClickListener, Observer, IMResourceListener, OnClickListener {

    private static final int COLOR_PERSONAL = 0x66000000;
    private static final int COLOR_GRAY = 0x4D000000;
    private static final int COLOR_NORMAL = 0x33000000;
    
    public static final int MAXWEEK = 53;

    private int mCellHeight;
    private int mNumRows = 12;

    private CurriculumAdapter mAdapter;

    private String[] mWeeksDesc, mTitleWeeksDesc;
    private int mCurrentWeek;
    private int mMaxWeek = MAXWEEK;
    private int mCurrentWeekDay = -1;
    private LightDialog mChooseWeekDialog;
    private LightDialog mDupCourseDialog;
    private PopupView mTitlePopupView;
    private FrameLayout mRlView;

    private ThemeFrameLayout mFlCulem;
    private IMResourceManager mResourceManager;
    private AccountInfo mAccountInfo;
    private CourseManager mCourseManager;

    private ArrayList<RecruitInfo> mRecruitInfos;
    private RecruitInfo mRecruitInfo;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_curriculum;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecruitInfos = new ArrayList<>();
        mResourceManager = IMResourceManager.getInstance(this);
        mResourceManager.registerIMResourceListener(this);
        mAccountInfo = AccountInfo.getInstance(this);

        mFlCulem = (ThemeFrameLayout) findViewById(R.id.fl_curriculum);
        mRlView = (FrameLayout) findViewById(R.id.fl_content);
        mRlView.setOnClickListener(this);
        // get resource
        mResourceManager.GetMyUsingResource(IMResource.TYPE_COURSE_BACKGROUD, mAccountInfo.getUserId());

        mCellHeight = calculateCellHeight();
        mCourseManager = CourseManager.getInstance(this);
        
        mAdapter = new CurriculumAdapter();
        // mAdapter.fillCells(new ArrayList<Recruit>());

        CellLayout courseLayout = (CellLayout) findViewById(R.id.cl_course_content);
        courseLayout.setAdapter(mAdapter);
        courseLayout.setOnCellClickListener(this);
        courseLayout.setOnInnerClickListener(this);
        
        new GetWeekInfoTask().executeLong();
        showPeriod();

        getTitleBar().showTitleArrow();
        getTitleBar().showRightButton(R.drawable.icon_more, this);
        getTitleBar().showTitleTextIcon();
        
    }

    private void updateWeekInfo() {
        mWeeksDesc = new String[mMaxWeek];
        mTitleWeeksDesc = new String[mMaxWeek];
        updateWeeksDesc();
        showWeekDay();
        getTitleBar().showTitleText(mTitleWeeksDesc[mCurrentWeek - 1], this);
        new LoadRecruitsTask(mCurrentWeek).executeLong();
    }

    private void updateWeeksDesc() {

        for (int i = 1; i <= mWeeksDesc.length; i++) {
            mWeeksDesc[i - 1] = i == mCurrentWeek ? getString(R.string.cur_current_week, i) : getString(
                    R.string.cur_title_num_week, i);
            mTitleWeeksDesc[i - 1] = i == mCurrentWeek ? getString(R.string.cur_current_week, i) : getString(
                    R.string.cur_discurrent_week, i);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private int calculateCellHeight() {
        Resources res = getResources();
        int headHeight = res.getDimensionPixelSize(R.dimen.title_bar_height)
                + res.getDimensionPixelSize(R.dimen.cur_header_height);
        int totalHeight = getResources().getDisplayMetrics().heightPixels - headHeight;
        int origCellHeight = getResources().getDimensionPixelSize(R.dimen.cur_cell_height);
        if ((origCellHeight + 1) * mNumRows < totalHeight) {
            return totalHeight / mNumRows - 1;
        } else {
            return origCellHeight;
        }
    }

    /**
     * Must get init data before accessing more curriculum features.
     */

    private void showWeekDay() {
        LinearLayout llWeekDay = (LinearLayout) findViewById(R.id.ll_weekday);
        int weekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) % 7;
        if (mCurrentWeekDay != weekDay) {
            mCurrentWeekDay = weekDay;
            for (int i = 0; i < 7; i++) {
                TextView v = (TextView) llWeekDay.getChildAt(i);
                if ((i + Calendar.MONDAY) % 7 == weekDay) {
                    v.setBackgroundResource(R.drawable.bg_week_day_today);
                } else if (i % 2 == 0) {
                    v.setBackgroundColor(COLOR_NORMAL);
                } else {
                    v.setBackgroundColor(COLOR_GRAY);
                }
                v.setTextColor(Color.WHITE);
            }
        }
    }

    private void showPeriod() {
        LinearLayout llPeriod = (LinearLayout) findViewById(R.id.ll_period);
        int height = mCellHeight + 1;
        for (int i = 1; i <= mNumRows; i++) {
            TextView tvPeriod = (TextView) getLayoutInflater().inflate(R.layout.item_period_number, null);
            tvPeriod.setText(String.valueOf(i));
            if ((i & 1) == 1) {
                tvPeriod.setBackgroundColor(COLOR_NORMAL);
            } else {
                tvPeriod.setBackgroundColor(COLOR_GRAY);
            }
            if (i == mNumRows) {
                height -= 1;
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            		LinearLayout.LayoutParams.MATCH_PARENT, height);
            llPeriod.addView(tvPeriod, lp);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mResourceManager.unregisterIMResourceListener(this);
    }

    @Override
    public void onCellClicked(int col, int row) {
        addPersonalJob();
    }

    private void addPersonalJob() {
        Intent intent = new Intent(this, AddJobsInfoActivity.class);
        intent.putExtra(AddJobsInfoActivity.EXT_CURRENT_WEEK, mCurrentWeek);
        startActivity(intent);
    }

    private void showRecruScan() {
        startActivity(new Intent(this, RecruScanActivity.class));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onClick(View v) {
        if (v.getTag() != null && v.getTag() instanceof ArrayList<?>) {
            mRecruitInfos = (ArrayList<RecruitInfo>) v.getTag();
            showJobInfo();
        } else if (v.getTag() != null && v.getTag() instanceof RecruitInfo) {
        	mRecruitInfo = (RecruitInfo) v.getTag();
        	if (!mRecruitInfo.isPersonal()) {
        		Job mJob = new Job();
        		mJob.id = mRecruitInfo.id;
        		mJob.title = mRecruitInfo.title;
        		mJob.cTime = mRecruitInfo.time;
        		mJob.corpName = mRecruitInfo.company;
        		Intent intent = new Intent(RecruitCalendarActivity.this, JobDetailActivity.class);
        		intent.putExtra(JobDetailActivity.EXT_JOB_ID, mJob.id);
        		intent.putExtra(Job.INTENT_EXTRA_NAME, mJob);
        		startActivity(intent);
        	}
        } else {
            switch (v.getId()) {
                case R.id.tv_title:
                    showWeeksDialog();
                    break;

                case R.id.btn_right:
                    showTitlePopupMenu(v);
                    break;

                case R.id.fl_content:
                    mRlView.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        }
    }

    private void showJobInfo() {
        mRlView.removeAllViews();
        mRlView.addView(getView(mRecruitInfos));
        mRlView.setVisibility(View.VISIBLE);
    }

    private void showWeeksDialog() {
        if (mChooseWeekDialog == null) {
            mChooseWeekDialog = new LightDialog(this);
            mChooseWeekDialog.setTitle(mTitleWeeksDesc[mCurrentWeek - 1]);
            mChooseWeekDialog.setItems(mWeeksDesc, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateWeek(which +1);
                    mChooseWeekDialog.setTitle(mTitleWeeksDesc[mCurrentWeek - 1]);
                    getTitleBar().setTitle(mTitleWeeksDesc[which]);
                }
            });
            mChooseWeekDialog.setNegativeButton(android.R.string.cancel, null);
        }

        mChooseWeekDialog.show();
    }

    private View getView(ArrayList<RecruitInfo> infos) {
        View view = mInflater.inflate(R.layout.list_item_job_scan, null);
        TextView tvTime = (TextView) view.findViewById(R.id.tv_invite_time);
        ListView lvJobs = (ListView) view.findViewById(R.id.lv_jobs);
        tvTime.setText(Utils.getTimeString(2, infos.get(0).time));
        RecruJobAdapter adapter = new RecruJobAdapter();
        lvJobs.setAdapter(adapter);
        return view;
    }

    private void updateWeek(int week) {
        if (mCurrentWeek != week) {
            mCurrentWeek = week;
            new LoadRecruitsTask(mCurrentWeek).executeLong();
        }
    }

    private class LoadRecruitsTask extends MsTask {

        private int mWeekNo;

        private LoadRecruitsTask(int weekNo) {
            super(RecruitCalendarActivity.this, MsRequest.RECRUITS_BY_WEEK);
            this.mWeekNo = weekNo;

        }

        @Override
        protected String buildParams() {
            return new StringBuilder("week_no=").append(mWeekNo).toString();
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(response)) {
                JSONArray ja = response.json.optJSONArray(MsResponse.PARAM_RESPONSE);
                ArrayList<RecruitInfo> recruits = JsonUtil.getArray(ja, RecruitInfo.TRANSFORMER);
                mAdapter.fillCells(null);
                if (recruits != null && recruits.size() > 0) {
                    mAdapter.fillCells(sortServerRecruitList(recruits));
                }
                mAdapter.notifyDataSetChanged();

            }
        }
    }
    
    private class GetWeekInfoTask extends MsTask {

        public GetWeekInfoTask() {
            super(RecruitCalendarActivity.this, MsRequest.WEEK_INFO);
        }
        
        @Override
        protected void onPostExecute(MsResponse response) {
            if (response.isSuccessful()) {
                WeekInfo info = WeekInfo.fromJson(response.getJsonObject());
                mCurrentWeek = info.currentWeek;
                mMaxWeek = info.endWeek;
                updateWeekInfo();
            }
        }

    }

    private ArrayList<ArrayList<RecruitInfo>> sortServerRecruitList(ArrayList<RecruitInfo> recruits) {
        HashMap<String, ArrayList<RecruitInfo>> recruitsMap = new HashMap<String, ArrayList<RecruitInfo>>();

        for (RecruitInfo recruit : recruits) {
            String key = recruit.weekNo + "," + recruit.classNo;
            if (recruitsMap.get(key) == null) {
                ArrayList<RecruitInfo> subRecruits = new ArrayList<RecruitInfo>();
                subRecruits.add(recruit);
                recruitsMap.put(key, subRecruits);

            } else {
                recruitsMap.get(key).add(recruit);
            }
        }
        ArrayList<ArrayList<RecruitInfo>> processedList = new ArrayList<ArrayList<RecruitInfo>>();

        for (String key : recruitsMap.keySet()) {
            processedList.add(recruitsMap.get(key));
        }
        return processedList;
    }

    private LightDialog getDupCourseDialog() {
        if (mDupCourseDialog == null) {
            mDupCourseDialog = new LightDialog(this);
            mDupCourseDialog.setTitle(R.string.cur_choose_course);
        }
        return mDupCourseDialog;
    }

    @Override
    public void update(Observable observable, Object data) {

    }

    private class RecruJobAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mRecruitInfos.size();
        }

        @Override
        public RecruitInfo getItem(int position) {
            return mRecruitInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.list_item_recru_view, parent, false);
            }
            RecruitInfo job = getItem(position);
            TextView tvName = (TextView) view.findViewById(R.id.tv_position);
            TextView tvCorp = (TextView) view.findViewById(R.id.tv_corp);
            TextView tvTime = (TextView) view.findViewById(R.id.tv_invite_time);
            tvName.setText(job.title);
            tvCorp.setText(job.company);
            tvTime.setText(Utils.getTimeString(1, job.time));
            if (position < mRecruitInfos.size() - 1) {
                if (job.isPersonal()) {
                    view.setBackgroundColor(COLOR_PERSONAL);
                } else if (position %2 == 0) {
                    view.setBackgroundColor(COLOR_GRAY);
                } else {
                    view.setBackgroundColor(COLOR_NORMAL);
                }
            } else {
                if (job.isPersonal()) {
                    view.setBackgroundResource(R.drawable.bg_job_view_personal);
                } else if (position %2 == 0) {
                    view.setBackgroundResource(R.drawable.bg_job_view_gray);
                } else {
                    view.setBackgroundResource(R.drawable.bg_job_view_normal);
                }
            }
            view.setOnClickListener(RecruitCalendarActivity.this);
            view.setTag(job);
            return view;
        }

    }

    private class CurriculumAdapter extends BaseCellAdapter {
        private ArrayList<CellLayout.Cell> mCells = new ArrayList<CellLayout.Cell>();
        private HashMap<CellLayout.Cell, ArrayList<RecruitInfo>> mCellMap = new HashMap<CellLayout.Cell, ArrayList<RecruitInfo>>();

        private void fillCells(ArrayList<ArrayList<RecruitInfo>> recruits) {
            mCellMap.clear();
            mCells.clear();

            if (recruits == null) {
                return;
            }
            for (ArrayList<RecruitInfo> subRecruitList : recruits) {

                RecruitInfo recruit = subRecruitList.get(subRecruitList.size() - 1);

                CellLayout.Cell newCell = new CellLayout.Cell();
                newCell.col = recruit.weekDayNo;
                newCell.rowStart = recruit.classNo;
                newCell.rowEnd = newCell.rowStart + 1;
                mCells.add(newCell);
                mCellMap.put(newCell, subRecruitList);
            }
            // notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mCells.size();
        }

        @Override
        public Object getItem(int position) {
            return mCells.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CourseView view;
            if (convertView == null) {
                view = (CourseView) getLayoutInflater().inflate(R.layout.course_item, parent, false);
            } else {
                view = (CourseView) convertView;
            }

            ArrayList<RecruitInfo> sbuRecruitList = mCellMap.get(mCells.get(position));

            RecruitInfo recruit = sbuRecruitList.get(sbuRecruitList.size() - 1);
            view.setShowFold(false);
            view.setCourseClosed(false);
            GradientDrawable bg = (GradientDrawable) view.getBackground();
            int colorBg = recruit.isPersonal() ? CourseUtil.getColorFor(recruit.title.hashCode()) : 0x33111111;
            bg.setColor(colorBg);
            view.setTag(sbuRecruitList);                    
            view.setText(recruit.title);
            view.setOnClickListener(RecruitCalendarActivity.this);
            return view;
        }

        @Override
        public CellLayout.Cell getCell(int position) {
            return mCells.get(position);
        }

        @Override
        public int getNumCols() {
            return 7;
        }

        @Override
        public int getNumRows() {
            return mNumRows;
        }

        @Override
        public int getCellHeight() {
            return mCellHeight;
        }
    }

    private void showTitlePopupMenu(View anchor) {
        if (mTitlePopupView == null) {
            mTitlePopupView = new PopupView(this).setItems(R.array.my_college_recruit_calendar_popup,
                    new OnItemClickListener() {

                        @Override
                        public void onItemClick(int position, PopupItem item) {
                            switch (position) {
                                case 0:
                                    addPersonalJob();
                                    break;
                                case 1:
                                    showRecruScan();
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
        }
        mTitlePopupView.showAsDropDown(anchor, true);
    }

    @Override
    public void onUsingResUpdated(int userId, int type, IMResource res) {
    }

    @Override
    public void onGetImResourceSuccess(int type, int requestCode, ArrayList<IMResource> mResources) {
        if (type == IMResource.TYPE_COURSE_BACKGROUD) {
            ArrayList<IMResource> resources = mResources;
            if (resources != null && resources.size() > 0) {
                String url = resources.get(0).urls[0][0];
                mFlCulem.setBackground(url, 0);
            }
        }
    }

    @Override
    public void onUnzipSuccess() {}

    @Override
    public void onUseResSuccess(IMResource res) {}

    @Override
    public void onUnuseResSuccess() {}

    @Override
    public void onAddResSuccess() {}

    @Override
    public void onAddResFail(int code) {}

    @Override
    public void onClick() {
        mRlView.setVisibility(View.GONE);
    }

}