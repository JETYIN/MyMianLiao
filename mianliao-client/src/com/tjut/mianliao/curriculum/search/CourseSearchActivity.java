package com.tjut.mianliao.curriculum.search;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BaseTask;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.SearchView;
import com.tjut.mianliao.component.SearchView.OnSearchConditionListener;
import com.tjut.mianliao.component.SearchView.OnSearchTextListener;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.curriculum.EditCourseActivity;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.Course.Entry;
import com.tjut.mianliao.util.MsResponse;

public class CourseSearchActivity extends BaseActivity implements DialogInterface.OnClickListener,
        View.OnClickListener, View.OnTouchListener, OnItemClickListener, OnRefreshListener2<ListView>, Runnable,
        OnSearchConditionListener, OnSearchTextListener, TaskExecutionListener {

    private static final long DELAY_MILLS = 1000;

    private PullToRefreshListView mLvCourseSearch;
    private SearchView mCourseSearchView;
    private TextView mTvInfo;
    private View mCourseAddBtn;

    private CourseSearchManager mSearchManager;
    private CourseAdapter mCourseAdapter;
    private CourseCategoryAdapter mCategoryAdapter;
    private CourseCategoryAdapter mCurrentAdapter;

    private LightDialog mClearTimeDialog;
    private Handler mHandler;
    private Course mCourse;
    private boolean mRefresh;
    private boolean mPrepareSearch;
    private BaseTask mSearchTask;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search_course;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Course course = getIntent().getParcelableExtra(Course.INTENT_EXTRA_NAME);
        if (course == null) {
            finish();
            return;
        }
        mCourse = Course.copy(course);
        mHandler = new Handler();
        getTitleBar().showTitleText(R.string.course_search_title, null);

        mCourseAddBtn = findViewById(R.id.tv_course_add);
        mCourseAddBtn.setOnClickListener(this);

        mCourseSearchView = (SearchView) findViewById(R.id.sv_course);
        mCourseSearchView.setCondition(CourseUtil.getPeriodDesc(course.getEntries().get(0)));
        mCourseSearchView.setHint(R.string.course_search_hint);
        mCourseSearchView.setOnSearchConditionListener(this);
        mCourseSearchView.setOnSearchTextListener(this);

        mTvInfo = (TextView) findViewById(R.id.tv_course_info);
        mTvInfo.setText(getString(R.string.course_search_info, CourseUtil.getSemesterDesc(course.semester)));

        mLvCourseSearch = (PullToRefreshListView) findViewById(R.id.ptrlv_search_courses);
        mLvCourseSearch.setOnItemClickListener(this);
        mLvCourseSearch.setOnRefreshListener(this);
        mLvCourseSearch.getRefreshableView().setOnTouchListener(this);

        mSearchManager = CourseSearchManager.getInstance(this);
        mSearchManager.registerListener(this);

        mCourseAdapter = new CourseAdapter(this);
        mCategoryAdapter = new CourseCategoryAdapter(this);
        searchCourse(true);
    }

    @Override
    protected void onDestroy() {
        if (mSearchTask != null) {
            mSearchTask.cancel(true);
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(this);
        }
        if (mCourseAdapter != null) {
            mCourseAdapter.destroy();
        }
        if (mSearchManager != null) {
            mSearchManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mCourseSearchView.setCondition(null);
        Entry entry = mCourse.getEntries().get(0);
        entry.weekday = 0;
        entry.periodStart = 0;
        searchCourse(true);
    }

    @Override
    public void onClick(View v) {
        if (v == mCourseAddBtn) {
            mCourseSearchView.hideInput();
            Intent intent = new Intent(this, EditCourseActivity.class);
            intent.putExtra(Course.INTENT_EXTRA_NAME, mCourse);
            startActivity(intent);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            mCourseSearchView.hideInput();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        boolean categoryShown = mCurrentAdapter == mCategoryAdapter;
        if (categoryShown && (mPrepareSearch || mSearchTask != null)) {
            // User has already input some text to search when category is
            // shown.
            // In this case we should prevent entering
            // SpecificCourseSearchActivity
            // to avoid messing up the searching results.
            return;
        }

        Course course = (Course) parent.getItemAtPosition(position);
        if (course != null && mSearchTask == null) {
            Intent intent = new Intent(this, categoryShown ? SpecificCourseSearchActivity.class
                    : EditCourseActivity.class);
            intent.putExtra(Course.INTENT_EXTRA_NAME, course);
            startActivity(intent);
        }
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        searchCourse(true);
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        searchCourse(false);
    }

    @Override
    public void run() {
        String searchText = mCourseSearchView.getSearchText();
        if (searchText.equalsIgnoreCase(mCourse.name)) {
            if (mSearchTask == null) {
                getTitleBar().hideProgress();
            }
        } else {
            mCourse.name = searchText;
            mCourseAdapter.setSearchText(searchText);
            searchCourse(true);
        }
        mPrepareSearch = false;
    }

    @Override
    public void onSearchConditionClicked() {
        getClearTimeDialog().show();
    }

    @Override
    public void onSearchTextChanged(CharSequence text) {
        getTitleBar().showProgress();
        if (TextUtils.isEmpty(text)) {
            mCourseAddBtn.setVisibility(View.GONE);
            mTvInfo.setVisibility(View.VISIBLE);
        } else {
            mCourseAddBtn.setVisibility(View.VISIBLE);
            mTvInfo.setVisibility(View.GONE);
        }
        mHandler.removeCallbacks(this);
        mHandler.postDelayed(this, DELAY_MILLS);
        mPrepareSearch = true;
    }

    @Override
    public void onPreExecute(int type) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        if (mSearchTask != null) {
            switch (type) {
                case CourseSearchManager.TASK_COURSE_CATEGORY:
                case CourseSearchManager.TASK_COURSE_SEARCH:
                    getTitleBar().hideProgress();
                    if (MsResponse.isSuccessful(mr) && mr.value != null) {
                        if (CourseSearchManager.TASK_COURSE_CATEGORY == type && mCurrentAdapter != mCategoryAdapter) {
                            mCurrentAdapter = mCategoryAdapter;
                            mLvCourseSearch.setAdapter(mCategoryAdapter);
                        } else if (CourseSearchManager.TASK_COURSE_SEARCH == type && mCurrentAdapter != mCourseAdapter) {
                            mCurrentAdapter = mCourseAdapter;
                            mLvCourseSearch.setAdapter(mCourseAdapter);
                        }

                        List<Course> courseList = (List<Course>) mr.value;
                        if (mRefresh) {
                            mCurrentAdapter.reset(courseList);
                        } else {
                            mCurrentAdapter.append(courseList);
                        }
                        mLvCourseSearch.onRefreshComplete();
                        mLvCourseSearch.setMode(courseList.size() < mSettings.getPageCount() ? Mode.PULL_FROM_START
                                : Mode.BOTH);
                    } else {
                        mLvCourseSearch.onRefreshComplete();
                    }
                    mSearchTask = null;
                    break;

                default:
                    break;
            }
        }
    }

    private LightDialog getClearTimeDialog() {
        if (mClearTimeDialog == null) {
            mClearTimeDialog = new LightDialog(this);
            mClearTimeDialog.setTitle(R.string.course_time_clear);
            mClearTimeDialog.setMessage(R.string.course_time_clear_desc)
                    .setNegativeButton(android.R.string.cancel, null).setPositiveButton(android.R.string.yes, this);
        }
        return mClearTimeDialog;
    }

    private void searchCourse(boolean refresh) {
        getTitleBar().showProgress();
        mRefresh = refresh;
        int offset = refresh || mCurrentAdapter == null ? 0 : mCurrentAdapter.getCount();
        if (mSearchTask != null) {
            mSearchTask.cancel(true);
        }
        mSearchTask = mSearchManager.startCourseSearchTask(Course.copy(mCourse), offset);
    }
}
