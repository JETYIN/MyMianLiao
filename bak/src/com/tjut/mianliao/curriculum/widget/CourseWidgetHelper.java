package com.tjut.mianliao.curriculum.widget;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.tjut.mianliao.R;
import com.tjut.mianliao.SplashActivity;
import com.tjut.mianliao.curriculum.CourseActivity;
import com.tjut.mianliao.curriculum.CourseManager;
import com.tjut.mianliao.curriculum.CourseUtil;
import com.tjut.mianliao.curriculum.CurriculumActivity;
import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.Course;

public class CourseWidgetHelper {

    public static final String EXTRA_ACTION = "extra_action";

    public static final int ACTION_NONE = 0;
    public static final int ACTION_NEXT = 1;
    public static final int ACTION_PREV = 2;
    public static final int ACTION_RESET = 3;

    private static CourseWidgetHelper sInstance;

    private String mPackageName;

    private int mCurrentIndex = 0;
    private int mCurrentWeekDay;
    ArrayList<Course.Entry> mEntries;

    private String mWeekDesc;

    public static synchronized CourseWidgetHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new CourseWidgetHelper(context);
        }
        return sInstance;
    }

    public static void updateWidget(Context context) {
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = awm.getAppWidgetIds(
                new ComponentName(context, CourseWidgetProvider.class));
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            CourseWidgetHelper cwh = getInstance(context);
            cwh.prepareEntries(context, ACTION_RESET);
            cwh.updateWidgets(context, awm, appWidgetIds);
        }
    }

    private CourseWidgetHelper(Context context) {
        mWeekDesc = context.getString(R.string.cur_title_num_week);
        mPackageName = context.getPackageName();
        prepareEntries(context, ACTION_NONE);
    }

    public void updateWidgets(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        if (appWidgetIds == null || appWidgetIds.length == 0) {
            return;
        }

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews root = new RemoteViews(mPackageName, R.layout.widget_course);
            fillTitle(context, root);

            // set up adapter
            Intent intent = new Intent(context, CourseWidgetViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            root.setRemoteAdapter(R.id.lv_courses, intent);

            // set up PendingIntent template
            Intent iTemplate = new Intent(context, CourseActivity.class);
            PendingIntent piTemplate = PendingIntent.getActivity(context, 0,
                    iTemplate, PendingIntent.FLAG_UPDATE_CURRENT);
            root.setPendingIntentTemplate(R.id.lv_courses, piTemplate);
            root.setEmptyView(R.id.lv_courses, R.id.tv_widget_hint);

            // set up launch MainActivity
            String hint;
            Intent iLaunch;
            if (AccountInfo.getInstance(context).isLoggedIn()) {
                hint = context.getString(R.string.course_wid_no_lesson);
                iLaunch = new Intent(context, CurriculumActivity.class);
            } else {
                iLaunch = new Intent(context, SplashActivity.class);
                hint = context.getString(R.string.course_wid_not_logged_in);
            }
            root.setTextViewText(R.id.tv_widget_hint, hint);
            PendingIntent piLaunch = PendingIntent.getActivity(context, 0, iLaunch,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            root.setOnClickPendingIntent(R.id.tv_widget_hint, piLaunch);

            appWidgetManager.updateAppWidget(appWidgetId, root);
        }

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lv_courses);
    }

    public void prepareEntries(Context context, int mAction) {
        if (mEntries == null) {
            initEntries(context);
        }

        CourseManager cm = CourseManager.getInstance(context);
        switch (mAction) {
            case ACTION_NEXT:
                mCurrentWeekDay += 1;
                if (mCurrentWeekDay == 8) {
                    mCurrentWeekDay = 7;
                }
                mEntries = cm.getEntriesFor(mCurrentWeekDay);
                Collections.sort(mEntries, mCourseEntryComparator);
                mCurrentIndex = 0;
                break;

            case ACTION_PREV:
                mCurrentWeekDay -= 1;
                if (mCurrentWeekDay == 0) {
                    mCurrentWeekDay = 1;
                }
                mEntries = cm.getEntriesFor(mCurrentWeekDay);
                Collections.sort(mEntries, mCourseEntryComparator);
                break;

            case ACTION_RESET:
                initEntries(context);
                break;

            case ACTION_NONE:
            default:
                break;
        }
    }

    private void initEntries(Context context) {
        mCurrentIndex = 0;
        int weekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 1;
        mCurrentWeekDay = weekDay == 0 ? 7 : weekDay;
        CourseManager cm = CourseManager.getInstance(context);
        mEntries = cm.getEntriesFor(mCurrentWeekDay);
        Collections.sort(mEntries, mCourseEntryComparator);
    }

    public void fillTitle(Context context, RemoteViews root) {
        // setup buttons
        Intent iNext = new Intent(context, CourseWidgetUpdateService.class);
        iNext.putExtra(EXTRA_ACTION, ACTION_NEXT);
        PendingIntent piNext = PendingIntent.getService(context, ACTION_NEXT, iNext,
                PendingIntent.FLAG_UPDATE_CURRENT);
        root.setOnClickPendingIntent(R.id.btn_wid_course_next, piNext);
        boolean nextEnabled = !(mCurrentWeekDay == 7);
        root.setBoolean(R.id.btn_wid_course_next, "setEnabled", nextEnabled);

        Intent iPrev = new Intent(context, CourseWidgetUpdateService.class);
        iPrev.putExtra(EXTRA_ACTION, ACTION_PREV);
        PendingIntent piPrev = PendingIntent.getService(context, ACTION_PREV, iPrev,
                PendingIntent.FLAG_UPDATE_CURRENT);
        root.setOnClickPendingIntent(R.id.btn_wid_course_prev, piPrev);
        root.setBoolean(R.id.btn_wid_course_prev, "setEnabled",
                mCurrentIndex != 0 || mCurrentWeekDay != 1);

        // setup date
        String date = String.format(mWeekDesc,
                CourseManager.getInstance(context).getCurrentWeek())
                + "  " + CourseUtil.getWeekdayDesc(mCurrentWeekDay);
        root.setTextViewText(R.id.tv_date, date);

        Intent iLaunch;
        if (AccountInfo.getInstance(context).isLoggedIn()) {
            iLaunch = new Intent(context, CurriculumActivity.class);
        } else {
            iLaunch = new Intent(context, SplashActivity.class);
        }
        PendingIntent piLaunch = PendingIntent.getActivity(context, 0, iLaunch,
                PendingIntent.FLAG_UPDATE_CURRENT);
        root.setOnClickPendingIntent(R.id.tv_date, piLaunch);
    }

    public List<Course.Entry> getEntries() {
        return new ArrayList<Course.Entry>(mEntries);
    }

    public static RemoteViews getItemViews(Context context, String packageName,
            Course.Entry entry) {
        RemoteViews item = new RemoteViews(packageName, R.layout.widget_course_item);
        item.setTextViewText(R.id.tv_course_name, entry.getCourse().name);
        String classroom = TextUtils.isEmpty(entry.classroom) ?
                context.getString(R.string.course_wid_unknown_location) : entry.classroom;
        item.setTextViewText(R.id.tv_location, classroom);
        String teacher = TextUtils.isEmpty(entry.getCourse().teacher) ?
                context.getString(R.string.course_wid_unknown) : entry.getCourse().teacher;
        item.setTextViewText(R.id.tv_teacher, teacher);
        item.setTextViewText(R.id.tv_period, entry.periodStart + "-" + entry.periodEnd);
        item.setInt(R.id.tv_color_mark, "setBackgroundColor",
                CourseUtil.getColorFor(entry.getCourse().name.hashCode()));

        return item;
    }

    public static void destroyInstance() {
        sInstance = null;
    }

    private Comparator<Course.Entry> mCourseEntryComparator = new Comparator<Course.Entry>() {
        @Override
        public int compare(Course.Entry lhs, Course.Entry rhs) {
            if (lhs.periodStart == rhs.periodStart) {
                return 0;
            } else {
                return lhs.periodStart > rhs.periodStart ? 1 : -1;
            }
        }
    };
}
