package com.tjut.mianliao.curriculum;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import com.tjut.mianliao.AlarmHelper;
import com.tjut.mianliao.BaseActivity;
import com.tjut.mianliao.R;
import com.tjut.mianliao.component.CheckBoxSetting;
import com.tjut.mianliao.component.DialogSetting;
import com.tjut.mianliao.component.IntentSetting;
import com.tjut.mianliao.component.LightDialog;
import com.tjut.mianliao.component.SettingItem;
import com.tjut.mianliao.component.SettingItem.Interceptor;
import com.tjut.mianliao.curriculum.widget.CurriculumWidgetHintActivity;
import com.tjut.mianliao.settings.DailyAlarmPicker;
import com.tjut.mianliao.settings.Settings;

public class CourseSettingsActivity extends BaseActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, DialogSetting.Listener {

    private WheelView mWvSemesters;
    private WheelView mWvWeeks;

    private CourseManager mCourseManager;
    private int[] mSemesters;
    private int[] mWeeks;

    private AlarmHelper mAlarmHelper;

    private DialogSetting mDsDailyCourse;
    private DailyAlarmPicker mDailyAlarmPicker;
    private int mDailyAlarmDay;
    private int mDailyAlarmHour;
    private int mDailyAlarmMinute;

    private Settings mSettings;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_course_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = Settings.getInstance(this);

        getTitleBar().showTitleText(R.string.cs_title, null);
        mAlarmHelper = AlarmHelper.getInstance(this);
        mCourseManager = CourseManager.getInstance(this);

        mSemesters = mCourseManager.getSemesters();
        mWeeks = new int[CourseUtil.MAX_WEEK + 1];
        for (int i = 0; i < mWeeks.length; i++) {
            mWeeks[i] = i + 1;
        }

        DialogSetting ds = (DialogSetting) findViewById(R.id.setting_cur_semester);
        ds.setListener(this);
        ds.setTitle(R.string.cs_cur_semester);
        ds.setSummary(getSemesterDesc(mCourseManager.getSemester()));
        ds.setBackgroundColor(Color.WHITE);

        ds = (DialogSetting) findViewById(R.id.setting_cur_week);
        ds.setListener(this);
        ds.setTitle(R.string.cs_cur_week);
        ds.setSummary(getWeekDesc(mCourseManager.getInitWeek()));
        ds.setBackgroundColor(Color.WHITE);

        CheckBoxSetting cbs = (CheckBoxSetting) findViewById(R.id.setting_daily_course_alarm);
        cbs.setTitle(R.string.setting_daily_course_alarm);
        cbs.setKey(Settings.KEY_DAILY_COURSE_ALARM);
        cbs.setBackgroundColor(Color.WHITE);

        ds = (DialogSetting) findViewById(R.id.setting_daily_course_alarm_time);
        ds.setListener(this);
        ds.setTitle(R.string.setting_daily_course_alarm_time);
        ds.setVisibility(mSettings.allowDailyCourseAlarm() ? View.VISIBLE : View.GONE);
        ds.setKey(Settings.KEY_DAILY_COURSE_ALARM_TIME);
        ds.setBackgroundColor(Color.WHITE);

        mDsDailyCourse = ds;
        mDailyAlarmDay = mSettings.getDailyCourseAlarmDay();
        mDailyAlarmHour = mSettings.getDailyCourseAlarmHour();
        mDailyAlarmMinute = mSettings.getDailyCourseAlarmMinute();
        setDailyAlarmSummary(ds, mDailyAlarmDay, mDailyAlarmHour, mDailyAlarmMinute);

        IntentSetting is = (IntentSetting) findViewById(R.id.setting_query_score);
        is.setTitle(R.string.cs_query_score);
        is.setClass(CourseScoreActivity.class);
        is.setBackgroundColor(Color.WHITE);

        is = (IntentSetting) findViewById(R.id.setting_desktop_widget);
        is.setTitle(R.string.cs_desktop_widget);
        is.setClass(CurriculumWidgetHintActivity.class);
        is.setBackgroundColor(Color.WHITE);

        is = (IntentSetting) findViewById(R.id.setting_refresh_course);
        is.setTitle(R.string.cs_refresh_course);
        is.setBackgroundColor(Color.WHITE);
        is.setInterceptor(new Interceptor() {
            @Override
            public boolean onSettingKey(SettingItem si) {
                return false;
            }

            @Override
            public boolean onSettingItemClick(SettingItem si) {
                mCourseManager.refreshCourses();
                finish();
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSettings.registerChangeListener(this);
    }

    @Override
    protected void onStop() {
        mSettings.unregisterChangeListener(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Settings.KEY_DAILY_COURSE_ALARM.equals(key)) {
            if (mSettings.allowDailyCourseAlarm()) {
                mDsDailyCourse.setVisibility(View.VISIBLE);
                mAlarmHelper.setCourseAlarm(mDailyAlarmDay, mDailyAlarmHour, mDailyAlarmMinute);
            } else {
                mDsDailyCourse.setVisibility(View.GONE);
                mAlarmHelper.cancelCourseAlarm();
            }
        }
    }

    @Override
    public void onCreateDialog(DialogSetting ds, LightDialog dialog) {
        switch (ds.getId()) {
            case R.id.setting_cur_semester:
                String[] semestersDesc = new String[mSemesters.length];
                for (int i = 0; i < semestersDesc.length; i++) {
                    semestersDesc[i] = getSemesterDesc(mSemesters[i]);
                }
                mWvSemesters = createWheelView(semestersDesc);
                dialog.setView(mWvSemesters).setPositiveButton(android.R.string.ok, ds);
                break;

            case R.id.setting_cur_week:
                String[] weeksDesc = new String[mWeeks.length];
                for (int i = 0; i < weeksDesc.length; i++) {
                    weeksDesc[i] = getWeekDesc(mWeeks[i]);
                }
                mWvWeeks = createWheelView(weeksDesc);
                dialog.setView(mWvWeeks).setPositiveButton(android.R.string.ok, ds);
                break;

            case R.id.setting_daily_course_alarm_time:
                mDailyAlarmPicker = new DailyAlarmPicker(this);
                dialog.setView(mDailyAlarmPicker.getRootView())
                        .setPositiveButton(android.R.string.ok, ds);
                break;

            default:
                break;
        }
    }

    @Override
    public void onBindDialog(DialogSetting ds, LightDialog dialog) {
        switch (ds.getId()) {
            case R.id.setting_cur_semester:
                int semester = mCourseManager.getSemester();
                for (int i = 0; i < mSemesters.length; i++) {
                    if (mSemesters[i] == semester) {
                        mWvSemesters.setCurrentItem(i);
                        break;
                    }
                }
                break;

            case R.id.setting_cur_week:
                int week = mCourseManager.getInitWeek();
                for (int i = 0; i < mWeeks.length; i++) {
                    if (mWeeks[i] == week) {
                        mWvWeeks.setCurrentItem(i);
                        break;
                    }
                }
                break;

            case R.id.setting_daily_course_alarm_time:
                mDailyAlarmPicker.setTime(mDailyAlarmDay, mDailyAlarmHour, mDailyAlarmMinute);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCloseDialog(DialogSetting ds, int which) {
        switch (ds.getId()) {
            case R.id.setting_cur_semester:
                int semester = mSemesters[mWvSemesters.getCurrentItem()];
                if (semester != mCourseManager.getSemester()) {
                    ds.setSummary(getSemesterDesc(semester));
                    mCourseManager.changeSemester(semester);
                    if (mCourseManager.getCourses().size() == 0) {
                        mCourseManager.refreshCourses();
                    }
                }
                break;

            case R.id.setting_cur_week:
                int week = mWeeks[mWvWeeks.getCurrentItem()];
                if (week != mCourseManager.getInitWeek()) {
                    ds.setSummary(getWeekDesc(week));
                    mCourseManager.setInitWeek(week);
                }
                break;

            case R.id.setting_daily_course_alarm_time:
                int day = mDailyAlarmPicker.getDay();
                int hour = mDailyAlarmPicker.getHour();
                int minute = mDailyAlarmPicker.getMinute();
                if (mDailyAlarmDay != day || mDailyAlarmHour != hour || mDailyAlarmMinute != minute) {
                    setDailyAlarmSummary(ds, day, hour, minute);
                    mSettings.setDailyCourseAlarmTime(day, hour, minute);
                    mAlarmHelper.setCourseAlarm(day, hour, minute);
                    mDailyAlarmDay = day;
                    mDailyAlarmHour = hour;
                    mDailyAlarmMinute = minute;
                }
                break;
            default:
                break;
        }
    }
    
    //设置时间选择器的样式
    private void setDailyAlarmSummary(DialogSetting ds, int day, int hour, int minute) {
        String[] days = getResources().getStringArray(R.array.setting_daily_course_alarm_day);
        String sDay = day >= 0 && day < days.length ? days[day] : "";
        String summary = getString(
                R.string.setting_daily_course_alarm_time_summary, sDay, hour, minute);
        ds.setSummary(summary);
    }
    // 获取学期信息
    private String getSemesterDesc(int semester) {
        return CourseUtil.getSemesterDesc(semester);
    }

    private String getWeekDesc(int week) {
        return getString(R.string.cur_title_num_week, week);
    }

    private WheelView createWheelView(String[] items) {
        ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(this, items);
        adapter.setItemResource(R.layout.wheel_item_period);
        WheelView wheelView = new WheelView(this);
        wheelView.setViewAdapter(adapter);
        return wheelView;
    }
}