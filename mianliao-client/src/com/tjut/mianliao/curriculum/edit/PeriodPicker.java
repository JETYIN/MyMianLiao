package com.tjut.mianliao.curriculum.edit;

import java.util.ArrayList;

import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.tjut.mianliao.R;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.data.Course;

/**
 * A widget to pick weekday/period of a course.
 */
public class PeriodPicker implements OnWheelScrollListener {
    private View mRootView;

    private WheelView mWvWeekDay;
    private WheelView mWvPeriodStart;
    private WheelView mWvPeriodEnd;
    private WeekAdapter mWeekDayAdapter;
    private NumericWheelAdapter mPeriodStartAdapter;
    private NumericWheelAdapter mPeriodEndAdapter;

    private TextView mTvPeriod;
    private Course.Entry mEntry;
    private Course.Entry mPreViewEntry = new Course.Entry();

    public PeriodPicker(Context context) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.course_period_picker, null);

        mWvWeekDay = (WheelView) mRootView.findViewById(R.id.wv_week_day);
        mWvPeriodStart = (WheelView) mRootView.findViewById(R.id.wv_period_start);
        mWvPeriodEnd = (WheelView) mRootView.findViewById(R.id.wv_period_end);

        String format = context.getString(R.string.course_period_desc);
        mWeekDayAdapter = new WeekAdapter(context);
        mWeekDayAdapter.setItemResource(R.layout.wheel_item_period);
        mPeriodStartAdapter = new NumericWheelAdapter(context, 1, 12, format);
        mPeriodStartAdapter.setItemResource(R.layout.wheel_item_period);
        mPeriodEndAdapter = new NumericWheelAdapter(context, 1, 12, format);
        mPeriodEndAdapter.setItemResource(R.layout.wheel_item_period);

        mWvWeekDay.setViewAdapter(mWeekDayAdapter);
        mWvPeriodStart.setViewAdapter(mPeriodStartAdapter);
        mWvPeriodEnd.setViewAdapter(mPeriodEndAdapter);
        mWvPeriodEnd.addScrollingListener(this);
        mWvPeriodStart.addScrollingListener(this);
    }

    public View getRootView() {
        return mRootView;
    }

    private static class WeekAdapter extends AbstractWheelTextAdapter {

        protected WeekAdapter(Context context) {
            super(context);
        }

        @Override
        protected CharSequence getItemText(int index) {
            return CourseUtil.getWeekdayDesc(index + 1);
        }

        @Override
        public int getItemsCount() {
            return 7;
        }
    }

    /**
     * @param tvPeriod The TextView used to show result.
     * @param entry
     */
    public void setTarget(TextView tvPeriod, Course.Entry entry) {
        mTvPeriod = tvPeriod;
        mEntry = entry;
        mPreViewEntry.weeks = mEntry.weeks;
        mPreViewEntry.weekday = mEntry.weekday;
        mPreViewEntry.periodStart = mEntry.periodStart;
        mPreViewEntry.periodEnd = mEntry.periodEnd;
        mWvWeekDay.setCurrentItem(mPreViewEntry.weekday == 0 ? 0 : mPreViewEntry.weekday - 1);
        mWvPeriodStart.setCurrentItem(mPreViewEntry.periodStart == 0 ? 0 : mPreViewEntry.periodStart - 1);
        mWvPeriodEnd.setCurrentItem(mPreViewEntry.periodEnd == 0 ? 0 : mPreViewEntry.periodEnd - 1);
    }

    public void cancelEdit() {
        clear();
    }

    public boolean hasOverlap(ArrayList<Course.Entry> entries) {
        updatePreviewEntry();
        for (Course.Entry entry : entries) {
            if (entry != mEntry && entry.overlap(mPreViewEntry)) {
                return true;
            }
        }
        return false;
    }

    private void updatePreviewEntry() {
        mPreViewEntry.weekday = mWvWeekDay.getCurrentItem() + 1;
        mPreViewEntry.periodStart = mWvPeriodStart.getCurrentItem() + 1;
        mPreViewEntry.periodEnd = mWvPeriodEnd.getCurrentItem() + 1;
        if (mPreViewEntry.periodStart > mPreViewEntry.periodEnd) {
            mPreViewEntry.periodEnd = mPreViewEntry.periodStart;
        }
    }

    /**
     * Finish edit and show result
     */
    public void finishEdit() {
        mEntry.weekday = mPreViewEntry.weekday;
        mEntry.periodStart = mPreViewEntry.periodStart;
        mEntry.periodEnd = mPreViewEntry.periodEnd;
        showPeriods();

        clear();
    }

    private void clear() {
        mTvPeriod = null;
        mEntry = null;
    }

    private void showPeriods() {
        if (mEntry.periodStart > 0) {
            mTvPeriod.setText(CourseUtil.getPeriodDesc(mEntry));
        } else {
            mTvPeriod.setText("");
        }
    }

    @Override
    public void onScrollingStarted(WheelView wheel) {}

    @Override
    public void onScrollingFinished(WheelView wheel) {
        if (mWvPeriodEnd.getCurrentItem() < mWvPeriodStart.getCurrentItem()) {
            mWvPeriodEnd.setCurrentItem(mWvPeriodStart.getCurrentItem(), true);
        }
    }
}
