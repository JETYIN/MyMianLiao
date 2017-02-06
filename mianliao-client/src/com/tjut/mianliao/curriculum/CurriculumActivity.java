package com.tjut.mianliao.curriculum;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.ThemeFrameLayout;
import com.tjut.mianliao.curriculum.cell.BaseCellAdapter;
import com.tjut.mianliao.curriculum.cell.CellLayout;
import com.tjut.mianliao.curriculum.search.CourseSearchActivity;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.IMResource;
import com.tjut.mianliao.im.IMResourceManager;
import com.tjut.mianliao.im.IMResourceManager.IMResourceListener;
import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.util.HttpUtil;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;

public class CurriculumActivity extends BaseActivity implements CellLayout.OnCellClickListener,
        View.OnClickListener, Observer, IMResourceListener {
    private int mCellHeight;
    private int mNumRows = 12;

    private CourseManager mCourseManager;
    private CurriculumAdapter mAdapter;

    private InitTask mInitTask;

    private String[] mWeeksDesc;
    private int mCurrentWeek;
    private int mCurrentWeekDay = -1;
    private LightDialog mChooseWeekDialog;
    private LightDialog mDupCourseDialog;

    private ThemeFrameLayout mFlCulem;
    private IMResourceManager mResourceManager;
    private AccountInfo mAccountInfo;

    private static int mColorCellB = 0X33000000;
    private static int mColorCellNum = 0X4D000000;
    
    @Override
    protected int getLayoutResID() {
        return R.layout.activity_curriculum;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFlCulem = (ThemeFrameLayout) findViewById(R.id.fl_curriculum);
        mResourceManager = IMResourceManager.getInstance(this);
        mResourceManager.registerIMResourceListener(this);
        mAccountInfo = AccountInfo.getInstance(this);

        
        // get resource
        mResourceManager.GetMyUsingResource(
                IMResource.TYPE_COURSE_BACKGROUD, mAccountInfo.getUserId());

        mCellHeight = calculateCellHeight();
        mWeeksDesc = new String[CourseUtil.MAX_WEEK + 1];

        mCourseManager = CourseManager.getInstance(this);
        mCurrentWeek = mCourseManager.getCurrentWeek();
        mAdapter = new CurriculumAdapter();
        mAdapter.fillCells(mCourseManager.getCourses());
        mCourseManager.addObserver(this);

        CellLayout courseLayout = (CellLayout) findViewById(R.id.cl_course_content);
        courseLayout.setAdapter(mAdapter);
        courseLayout.setOnCellClickListener(this);

        showWeekDay();
        showPeriod();
        updateWeeksDesc();

        getTitleBar().showTitleText(mWeeksDesc[mCurrentWeek - 1], this);
        getTitleBar().showRightButton(R.drawable.icon_more, this);
        getTitleBar().showTitleTextIcon();
        verifyInit(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private int calculateCellHeight() {
        Resources res = getResources();
        int headHeight = res.getDimensionPixelSize(R.dimen.title_bar_height) +
                res.getDimensionPixelSize(R.dimen.cur_header_height);
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
    private boolean verifyInit(boolean showToast) {
        if (mCourseManager.getSemester() > 0) {
            return true;
        } else if (mInitTask == null) {
            new InitTask().executeLong();
        } else if (showToast) {
            toast(R.string.course_tst_get_init_data);
        }
        return false;
    }

    private void showWeekDay() {
        LinearLayout llWeekDay = (LinearLayout) findViewById(R.id.ll_weekday);
        int weekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) % 7;
        if (mCurrentWeekDay != weekDay) {
            mCurrentWeekDay = weekDay;
            for (int i = 0; i < 7; i++) {
                TextView v = (TextView) llWeekDay.getChildAt(i);
                if ((i + Calendar.MONDAY) % 7 == weekDay) {
                    v.setBackgroundColor(0X66000000);
                } else if (i % 2 == 0) {
                    v.setBackgroundColor(mColorCellB);
                } else {
                    v.setBackgroundResource(0);
                }
                v.setTextColor(Color.WHITE);
            }
        }
    }

    private void showPeriod() {
        LinearLayout llPeriod = (LinearLayout) findViewById(R.id.ll_period);
        int height = mCellHeight + 1;
        for (int i = 1; i <= mNumRows; i++) {
            TextView tvPeriod = (TextView) getLayoutInflater().inflate(
                    R.layout.item_period_number, null);
            tvPeriod.setText(String.valueOf(i));
            if (i % 2 == 1) {
                tvPeriod.setBackgroundColor(mColorCellB);
            } else {
                tvPeriod.setBackgroundColor(mColorCellNum);
            }
            if (i == mNumRows) {
                height -= 1;
            }
            tvPeriod.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, height);
            llPeriod.addView(tvPeriod, lp);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCourseManager.deleteObserver(this);
        mResourceManager.unregisterIMResourceListener(this);
    }

    @Override
    public void onCellClicked(int col, int row) {
        if (!verifyInit(true)) {
            return;
        }
        Intent i = new Intent(this, CourseSearchActivity.class);
        i.putExtra(Course.INTENT_EXTRA_NAME, new Course(mCourseManager.getSemester(), col,
                row, row));
        startActivity(i);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onClick(View v) {

        if (v.getTag() != null && v.getTag() instanceof ArrayList<?>) {
            final ArrayList<Course.Entry> entries = (ArrayList<Course.Entry>) v.getTag();
            int size = entries.size();
            if (size == 1) {
                onCourseClicked(entries.get(0));
            } else if (size > 1) {
                String[] courseDesc = new String[size];
                for (int i = 0; i < size; i++) {
                    courseDesc[i] = entries.get(i).getDesc();
                }
                getDupCourseDialog().setItems(courseDesc, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onCourseClicked(entries.get(which));
                    }
                }).show();
            }
        } else {
            switch (v.getId()) {
                case R.id.tv_title:
                    showWeeksDialog();
                    break;

                case R.id.btn_right:
                    Intent iSetting = new Intent(getApplicationContext(), CourseSettingsActivity.class);
                    startActivity(iSetting);
                    break;

                default:
                    break;
            }
        }
    }

    private void onCourseClicked(Course.Entry entry) {
        Intent i = new Intent(this, CourseActivity.class);
        i.putExtra(Course.INTENT_EXTRA_NAME, entry.getCourse());
        i.putExtra(CourseActivity.EXTRA_SCHEDULE, CourseUtil.getSchedule(entry, mCurrentWeek));
        startActivity(i);
    }

    private void showWeeksDialog() {
        if (mChooseWeekDialog == null) {
            mChooseWeekDialog = new LightDialog(this);
            mChooseWeekDialog.setItems(mWeeksDesc, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    updateWeek(which + 1);
                }
            });
            mChooseWeekDialog.setNegativeButton(android.R.string.cancel, null);
        }
        mChooseWeekDialog.setTitle(mWeeksDesc[mCurrentWeek - 1]);
        mChooseWeekDialog.show();
    }

    private void updateWeek(int week) {
        if (mCurrentWeek != week) {
            mCurrentWeek = week;
            getTitleBar().setTitle(mWeeksDesc[week - 1]);
            mCourseManager.setCurrentWeek(week);
        }
    }

    private void updateWeeksDesc() {
        int initWeek = mCourseManager.getInitWeek();
        for (int i = 1; i <= mWeeksDesc.length; i++) {
            mWeeksDesc[i - 1] = i == initWeek
                    ? getString(R.string.cur_current_week, i)
                    : getString(R.string.cur_title_num_week, i);
        }
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
        if (observable instanceof CourseManager) {
            if (CourseManager.TYPE_REFRESH_COURSE.equals(data)) {
                new CourseTask(mCourseManager.getSemester()).executeLong();
            } else if (CourseManager.TYPE_UPDATE_WEEK.equals(data)) {
                updateWeeksDesc();
                getTitleBar().setTitle(mWeeksDesc[mCurrentWeek - 1]);
            } else {
                mAdapter.fillCells(mCourseManager.getCourses());
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class CurriculumAdapter extends BaseCellAdapter {
        private ArrayList<CellLayout.Cell> mCells = new ArrayList<CellLayout.Cell>();
        private HashMap<CellLayout.Cell, ArrayList<Course.Entry>> mCellMap =
                new HashMap<CellLayout.Cell, ArrayList<Course.Entry>>();

        private void fillCells(SparseArray<Course> courses) {
            mCellMap.clear();
            mCells.clear();
            int size = courses.size();
            ArrayList<CellLayout.Cell> overlapCells = new ArrayList<CellLayout.Cell>();
            for (int i = 0; i < size; i++) {
                Course course = courses.valueAt(i);
                for (Course.Entry entry : course.getEntries()) {
                    overlapCells.clear();
                    for (CellLayout.Cell cell : mCells) {
                        if (entry.overlap(cell)) {
                            overlapCells.add(cell);
                        }
                    }

                    CellLayout.Cell newCell = new CellLayout.Cell();
                    newCell.col = entry.weekday;
                    newCell.rowStart = entry.periodStart;
                    newCell.rowEnd = entry.periodEnd;
                    ArrayList<Course.Entry> newEntries = new ArrayList<Course.Entry>();
                    newEntries.add(entry);
                    for (CellLayout.Cell cell : overlapCells) {
                        mCells.remove(cell);
                        newEntries.addAll(mCellMap.remove(cell));
                        newCell.merge(cell.col, cell.rowStart, cell.rowEnd);
                    }
                    mCells.add(newCell);
                    mCellMap.put(newCell, newEntries);
                }
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
                view = getLayoutInflater().inflate(R.layout.course_item,
                        parent, false);
            } else {
                view = convertView;
            }
            ArrayList<Course.Entry> entries = mCellMap.get(mCells.get(position));
            Course.Entry entry = null;

            // Pick active course of this week, if any
            for (Course.Entry et : entries) {
                entry = et;
                if (CourseUtil.containsWeek(entry.weeks, mCurrentWeek)) {
                    break;
                }
            }

            CourseView cv = (CourseView) view;
            cv.setShowFold(entries.size() > 1);
            cv.setCourseClosed(CourseUtil.isCourseClosed(entry.weeks, mCurrentWeek));
            GradientDrawable bg = (GradientDrawable) view.getBackground();
            int colorBg = CourseUtil.containsWeek(entry.weeks, mCurrentWeek) ?
                    CourseUtil.getColorFor(entry.getCourse().name.hashCode()) : 0x33111111;
            bg.setColor(colorBg);
            cv.setTag(entries);
            cv.setText(entry.getDesc());

            view.setOnClickListener(CurriculumActivity.this);
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

    private class InitTask extends AdvAsyncTask<Void, Void, Integer> {

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
            mInitTask = this;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            MsResponse res = HttpUtil.msRequest(getApplicationContext(), MsRequest.CURRENT_SEMESTER, "");
            if (res.code != MsResponse.MS_SUCCESS) {
                return res.code;
            }
            int semester = 0;
            int week = 0;
            try {
                JSONObject jo = new JSONObject(res.response);
                semester = jo.optInt("semester");
                week = jo.optInt("week");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (semester == 0 || week == 0) {
                return res.code;
            }
            MsResponse resCourse = HttpUtil.msRequest(getApplicationContext(), MsRequest.GET_MY_COURSES,
                    "semester=" + semester);
            if (resCourse.code != MsResponse.MS_SUCCESS) {
                return resCourse.code;
            }

            try {
                JSONArray ja = new JSONArray(resCourse.response);
                int size = ja.length();
                ArrayList<Course> courses = new ArrayList<Course>();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        courses.add(Course.fromJson(ja.getJSONObject(i)));
                    }
                }
                mCourseManager.initCourses(semester, week, courses);
                return resCourse.code;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return resCourse.code;
        }

        @Override
        protected void onPostExecute(Integer code) {
            if (getTitleBar() != null) {
                getTitleBar().hideProgress();
            }
            if (code == MsResponse.MS_SUCCESS) {
                updateWeeksDesc();
                updateWeek(mCourseManager.getCurrentWeek());
                mCourseManager.notifyUpdate();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.course_tst_failed_get_init_data, code));
            }
            mInitTask = null;
        }
    }

    private class CourseTask extends AdvAsyncTask<Void, Void, MsResponse> {
        private int mSemester;

        public CourseTask(int semester) {
            mSemester = semester;
        }

        @Override
        protected void onPreExecute() {
            getTitleBar().showProgress();
        }

        @Override
        protected MsResponse doInBackground(Void... params) {
            MsResponse mr = HttpUtil.msRequest(getApplicationContext(), MsRequest.GET_MY_COURSES,
                    "semester=" + mSemester);
            if (MsResponse.isSuccessful(mr)
                    && mSemester == mCourseManager.getSemester()) {
                try {
                    JSONArray ja = new JSONArray(mr.response);
                    int size = ja.length();
                    mCourseManager.clearCourses();
                    for (int i = 0; i < size; i++) {
                        Course c = Course.fromJson(ja.optJSONObject(i));
                        if (c != null) {
                            mCourseManager.addCourse(c, false);
                        }
                    }
                } catch (JSONException e) {
                    mr.code = MsResponse.MS_PARSE_FAILED;
                }
            }
            return mr;
        }

        @Override
        protected void onPostExecute(MsResponse mr) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(mr)) {
                mCourseManager.notifyUpdate();
            } else {
                toast(MsResponse.getFailureDesc(getApplicationContext(),
                        R.string.course_tst_failed_get_init_data, mr.code));
            }
        }
    }

    @Override
    public void onUsingResUpdated(int userId, int type, IMResource res) {}

    @Override
    public void onGetImResourceSuccess(int type, int requestCode,
            ArrayList<IMResource> mResources) {
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
}