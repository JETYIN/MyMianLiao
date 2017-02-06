package com.tjut.mianliao.curriculum.widget;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.tjut.mianliao.R;
import com.tjut.mianliao.curriculum.CourseActivity;
import com.tjut.mianliao.curriculum.CourseManager;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.data.Course;

/**
 * Provide better UXP for devices have android api >= 11.
 */
public class CourseWidgetViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetViewsFactory(getApplicationContext(), intent);
    }

    private static class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context mContext;
        private String mPackageName;
        private List<Course.Entry> mEntries;
        private int mCurrentWeek;

        public WidgetViewsFactory(Context context, Intent intent) {
            mContext = context.getApplicationContext();
            mPackageName = mContext.getPackageName();
        }

        @Override
        public void onCreate() { }

        @Override
        public void onDataSetChanged() {
            mEntries = CourseWidgetHelper.getInstance(mContext).getEntries();
            mCurrentWeek = CourseManager.getInstance(mContext).getCurrentWeek();
        }

        @Override
        public void onDestroy() {
            mContext = null;
            mEntries = null;
        }

        @Override
        public int getCount() {
            return mEntries == null ? 0 : mEntries.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            Course.Entry entry = mEntries.get(position);
            RemoteViews views = CourseWidgetHelper.getItemViews(mContext, mPackageName, entry);
            Intent intent = new Intent();
            intent.putExtra(Course.INTENT_EXTRA_NAME, entry.getCourse());
            intent.putExtra(CourseActivity.EXTRA_SCHEDULE,
                    CourseUtil.getSchedule(entry, mCurrentWeek));
            views.setOnClickFillInIntent(R.id.rl_wid_item, intent);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
