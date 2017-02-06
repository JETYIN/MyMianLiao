package com.tjut.mianliao.curriculum.search;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.BaseTask.TaskExecutionListener;
import com.tjut.mianliao.R;
import com.tjut.mianliao.curriculum.EditCourseActivity;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.util.MsResponse;

public class SpecificCourseSearchActivity extends BaseActivity implements
        OnItemClickListener, OnRefreshListener2<ListView>, TaskExecutionListener {

    private PullToRefreshListView mLvCourseSearch;

    private CourseSearchManager mSearchManager;
    private CourseAdapter mCourseAdapter;

    private Course mCourse;
    private boolean mRefresh;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_search_specific_course;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourse = getIntent().getParcelableExtra(Course.INTENT_EXTRA_NAME);
        if (mCourse == null) {
            finish();
            return;
        }
        getTitleBar().showTitleText(mCourse.name, null);

        mSearchManager = CourseSearchManager.getInstance(this);
        mSearchManager.registerListener(this);

        mLvCourseSearch = (PullToRefreshListView) findViewById(R.id.ptrlv_search_specific_courses);
        mLvCourseSearch.setOnItemClickListener(this);
        mLvCourseSearch.setOnRefreshListener(this);

        mCourseAdapter = new CourseAdapter(this);
        mLvCourseSearch.setAdapter(mCourseAdapter);

        searchCourse(true);
    }

    @Override
    protected void onDestroy() {
        if (mCourseAdapter != null) {
            mCourseAdapter.destroy();
        }
        if (mSearchManager != null) {
            mSearchManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Course course = (Course) parent.getItemAtPosition(position);
        if (course != null) {
            Intent intent = new Intent(this, EditCourseActivity.class);
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
    public void onPreExecute(int type) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onPostExecute(int type, MsResponse mr) {
        if (CourseSearchManager.TASK_COURSE_SEARCH == type) {
            getTitleBar().hideProgress();
            if (MsResponse.isSuccessful(mr) && mr.value != null) {
                List<Course> courseList = (List<Course>) mr.value;
                if (mRefresh) {
                    mCourseAdapter.reset(courseList);
                } else {
                    mCourseAdapter.append(courseList);
                }
                mLvCourseSearch.onRefreshComplete();
                mLvCourseSearch.setMode(courseList.size() < mSettings.getPageCount()
                        ? Mode.PULL_FROM_START : Mode.BOTH);
            } else {
                mLvCourseSearch.onRefreshComplete();
            }
        }
    }

    private void searchCourse(boolean refresh) {
        getTitleBar().showProgress();
        mRefresh = refresh;
        int offset = refresh ? 0 : mCourseAdapter.getCount();
        mSearchManager.startCourseSearchTask(mCourse, offset);
    }
}
