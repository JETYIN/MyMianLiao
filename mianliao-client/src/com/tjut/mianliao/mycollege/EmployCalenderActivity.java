package com.tjut.mianliao.mycollege;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.curriculum.CourseManager;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.curriculum.cell.BaseCellAdapter;
import com.tjut.mianliao.curriculum.cell.CellLayout;
import com.tjut.mianliao.curriculum.cell.CellLayout.OnCellClickListener;
import com.tjut.mianliao.data.job.Job;

public class EmployCalenderActivity extends BaseActivity implements OnClickListener,
        OnCellClickListener {

    private static final int REQUEST_CODE = 100;
    private int mCellHeight;
    private int mNumRows = 12;

    private CourseManager mCourseManager;
    private CurriculumAdapter mAdapter;

    private String[] mWeeksDesc;
    private int mCurrentWeek;
    private int mCurrentWeekDay = -1;

    private int mColorCellB;
    private int mColorCellNum;

    private ArrayList<Job> mJobs;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_employ_calender;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources res = getResources();
        mColorCellB = res.getColor(R.color.cur_cell_b);
        mColorCellNum = res.getColor(R.color.cur_color_number);
        mCellHeight = calculateCellHeight();
        mWeeksDesc = new String[CourseUtil.MAX_WEEK + 1];

        mJobs = new ArrayList<Job>();

        mCourseManager = CourseManager.getInstance(this);
        mCurrentWeek = mCourseManager.getCurrentWeek();
        mAdapter = new CurriculumAdapter();
        mAdapter.fillCells(mJobs);

        CellLayout courseLayout = (CellLayout) findViewById(R.id.cl_course_content);
        courseLayout.setAdapter(mAdapter);
        courseLayout.setOnCellClickListener(this);

        showWeekDay();
        showPeriod();
        updateWeeksDesc();

        getTitleBar().showTitleArrow();
        getTitleBar().showTitleText(mWeeksDesc[mCurrentWeek - 1], this);
        getTitleBar().showRightButton(R.drawable.icon_more, this);

    }

    private void fillJobData() {
        for (int i = 0; i < 3; i++) {
            Job job = new Job();
            job.corpName = "产品经理";
            job.categoryName = "泰聚泰";
            job.cTime = System.currentTimeMillis();
            mJobs.add(job);
        }
    }

    private void updateWeeksDesc() {
        int initWeek = mCourseManager.getInitWeek();
        for (int i = 1; i <= mWeeksDesc.length; i++) {
            mWeeksDesc[i - 1] = i == initWeek ? getString(R.string.cur_current_week, i) : getString(
                    R.string.cur_title_num_week, i);
        }
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

    private void showWeekDay() {
        LinearLayout llWeekDay = (LinearLayout) findViewById(R.id.ll_weekday);
        int weekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) % 7;
        int monDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        int monOfYear = Calendar.getInstance().get(Calendar.MONTH);
        TextView tvMonth = (TextView) llWeekDay.findViewById(R.id.tv_month);
        tvMonth.setText((monOfYear + 1) + "月");
        if (mCurrentWeekDay != weekDay) {
            mCurrentWeekDay = weekDay;
            for (int i = 1; i < 8; i++) {
                LinearLayout ll = (LinearLayout) llWeekDay.getChildAt(i);
                int color = mColorCellNum;
                TextView day = (TextView) ll.getChildAt(0);
                TextView v = (TextView) ll.getChildAt(1);
                int wd = ((i - 1) + Calendar.MONDAY) % 7;
                if (wd == weekDay) {
                    ll.setBackgroundResource(R.drawable.bg_week_day_today);
                    color = 0xFFFFFFFF;
                } else if ((i - 1) % 2 == 0) {
                    ll.setBackgroundColor(mColorCellB);
                } else {
                    ll.setBackgroundResource(0);
                }
                if (weekDay > 1) {
                    if (wd > 1) {
                        day.setText((monDay - weekDay + wd) + "");
                    } else {
                        day.setText((monDay - weekDay + wd + 7) + "");
                    }
                }
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
                tvPeriod.setBackgroundColor(mColorCellB);
            }
            if (i == mNumRows) {
                height -= 1;
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
            llPeriod.addView(tvPeriod, lp);
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCellClicked(int col, int row) {
        toast("" + col + "--" + row);
        startActivityForResult(new Intent(this, AddJobsInfoActivity.class), REQUEST_CODE);
    }

    private class CurriculumAdapter extends BaseCellAdapter {
        private ArrayList<CellLayout.Cell> mCells = new ArrayList<CellLayout.Cell>();

        private void fillCells(ArrayList<Job> jobs) {
            mCells.clear();
            int size = jobs.size();
            ArrayList<CellLayout.Cell> overlapCells = new ArrayList<CellLayout.Cell>();
            for (int i = 0; i < size; i++) {
                Job job = jobs.get(i);

                CellLayout.Cell newCell = new CellLayout.Cell();
                newCell.col = job.cls;
                newCell.rowStart = 2 + i;
                newCell.rowEnd = 3 + i;
                ArrayList<Job> newEntries = new ArrayList<Job>();
                newEntries.add(job);
                for (CellLayout.Cell cell : overlapCells) {
                    mCells.remove(cell);
                    newEntries.addAll(mJobs);
                    newCell.merge(cell.col, cell.rowStart, cell.rowEnd);
                }
                mCells.add(newCell);
            }
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
            View view;
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.course_item, parent, false);
            } else {
                view = convertView;
            }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        fillDataToCell();
    }

    private void fillDataToCell() {
        fillJobData();
        mAdapter.fillCells(mJobs);
    }

}
