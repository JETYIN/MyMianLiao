package com.tjut.mianliao.curriculum.edit;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.data.Course;

/**
 * A widget to pick weeks for a course.
 */
public class WeekPicker {

    private static final int NUM_COL = 4;

    private Context mContext;

    private LayoutInflater mInflater;

    private GridView mGvWeeks;

    private TextView mTvWeeks;

    private Course.Entry mEntry;

    private Course.Entry mPreViewEntry = new Course.Entry();

    private boolean mAddWeek = false;

    public WeekPicker(Context context) {
        mContext = context;
        mGvWeeks = new GridView(mContext);
        mGvWeeks.setNumColumns(NUM_COL);
        mGvWeeks.setHorizontalSpacing(1);
        mGvWeeks.setVerticalSpacing(1);
        mGvWeeks.setOnTouchListener(mWeeksTouchListener);
        mGvWeeks.setAdapter(mWeekAdapter);

        mInflater = LayoutInflater.from(mContext);
    }

    public View getRootView() {
        return mGvWeeks;
    }

    public void setTarget(TextView tvWeeks, Course.Entry entry) {
        mTvWeeks = tvWeeks;
        mEntry = entry;
        mPreViewEntry.weeks = mEntry.weeks;
        mPreViewEntry.weekday = mEntry.weekday;
        mPreViewEntry.periodStart = mEntry.periodStart;
        mPreViewEntry.periodEnd = mEntry.periodEnd;
        mWeekAdapter.notifyDataSetChanged();
    }

    public void cancelEdit() {
        clear();
    }

    public boolean hasOverlap(ArrayList<Course.Entry> entries) {
        for (Course.Entry entry : entries) {
            if (entry != mEntry && entry.overlap(mPreViewEntry)) {
                return true;
            }
        }
        return false;
    }

    public void finishEdit() {
        mEntry.weeks = mPreViewEntry.weeks;
        showWeeks();

        clear();
    }

    private void clear() {
        mTvWeeks = null;
        mEntry = null;
    }

    private void showWeeks() {
        mTvWeeks.setText(CourseUtil.getWeekDesc(mEntry.weeks));
    }

    private OnTouchListener mWeeksTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float width = v.getWidth();
            float height = v.getHeight();
            if (event.getX() > width - 1 || event.getY() > height - 1) {
                // ignore the touch out of the view
                return true;
            }

            float cellWidth = width / NUM_COL;
            float cellHeight = height / (CourseUtil.MAX_WEEK / NUM_COL);

            int col = (int) (event.getX() / cellWidth) + 1;
            int row = (int) (event.getY() / cellHeight);

            int week = row * NUM_COL + col;

            if (mAddWeek && !CourseUtil.containsWeek(mPreViewEntry.weeks, week)) {
                mPreViewEntry.weeks = CourseUtil.addWeek(mPreViewEntry.weeks, week);
                mWeekAdapter.notifyDataSetChanged();
            } else if (!mAddWeek && CourseUtil.containsWeek(mPreViewEntry.weeks, week)) {
                mPreViewEntry.weeks = CourseUtil.removeWeek(mPreViewEntry.weeks, week);
                mWeekAdapter.notifyDataSetChanged();
            }
            return true;
        }
    };

    private OnTouchListener mItemTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getTag() != null && v.getTag() instanceof Integer) {
                int week = (Integer) v.getTag();
                if (CourseUtil.containsWeek(mPreViewEntry.weeks, week)) {
                    mPreViewEntry.weeks = CourseUtil.removeWeek(mPreViewEntry.weeks, week);
                    mAddWeek = false;
                } else {
                    mPreViewEntry.weeks = CourseUtil.addWeek(mPreViewEntry.weeks, week);
                    mAddWeek = true;
                }
            }
            mWeekAdapter.notifyDataSetChanged();
            return false;
        }
    };

    private BaseAdapter mWeekAdapter = new BaseAdapter() {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            int week = position + 1;
            if (convertView != null) {
                view = convertView;
            } else {
                view = mInflater.inflate(R.layout.gv_item_week, parent, false);
            }
            view.setTag(week);
            view.setOnTouchListener(mItemTouchListener);
            if (CourseUtil.containsWeek(mPreViewEntry.weeks, week)) {
                view.setBackgroundColor(0XFFFFC21C);
            } else {
                view.setBackgroundColor(0xFFFFFFFF);
            }
            TextView tv = (TextView) view;
            tv.setText(String.valueOf(week));
            return view;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return CourseUtil.MAX_WEEK;
        }
    };
}
