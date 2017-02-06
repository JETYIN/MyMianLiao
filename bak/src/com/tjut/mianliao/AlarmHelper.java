package com.tjut.mianliao;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.format.Time;

import com.tjut.mianliao.curriculum.CourseManager;
import com.tjut.mianliao.data.mycollege.NoteInfo;
import com.tjut.mianliao.mycollege.MemoDetailActivity;

public class AlarmHelper {

    public static final String ACTION_COURSE_ALARM = "com.tjut.mianliao.action.ACTION_COURSE_ALARM";
    public static final String ACTION_MEMO_ALARM = "com.tjut.mianliao.action.ACTION_MEMO_ALARM";
    
    private static final String EXTRA_COURSE_ALARM_DAY = "extra_course_alarm_day";
    private static final String EXTRA_MEMO_ALARM_DAY = "extra_memo_alarm_day";
    private NoteInfo mNoes;

    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;

    private static WeakReference<AlarmHelper> sInstanceRef;

    private Context mContext;
    private AlarmManager mAlarmManager;

    public static synchronized AlarmHelper getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }

        AlarmHelper instance = new AlarmHelper(context);
        sInstanceRef = new WeakReference<AlarmHelper>(instance);
        return instance;
    }

    private AlarmHelper(Context context) {
        mContext = context.getApplicationContext();
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void clear() {
        cancelCourseAlarm();
        sInstanceRef.clear();
    }

    public void setCourseAlarm(int day, int hour, int minute) {
        Time now = new Time();
        now.setToNow();
        Time alarm = new Time(now);
        alarm.hour = hour;
        alarm.minute = minute;
        alarm.second = 0;
        long firstTime = alarm.toMillis(false);
        if (alarm.before(now)) {
            firstTime += ONE_DAY_MILLIS;
        }
        Intent intent = new Intent(ACTION_COURSE_ALARM).putExtra(EXTRA_COURSE_ALARM_DAY, day);
        setAlarm(intent, true, firstTime, ONE_DAY_MILLIS);
    }
    
    public void setMemoAlarm(long timeMillis,NoteInfo mNote) {
        Intent intent = new Intent(ACTION_MEMO_ALARM);
        cancelAlarm(intent);
        intent.putExtra(MemoDetailActivity.EXT_MEMO_NOTE, mNote);
        setAlarm(intent, false, timeMillis, ONE_DAY_MILLIS);
    }
    
    public void setCourseAlarm(long timeMillis) {
    	Intent intent = new Intent(ACTION_COURSE_ALARM);
        setAlarm(intent, true, timeMillis, ONE_DAY_MILLIS);
    }

    public void cancelCourseAlarm() {
        cancelAlarm(new Intent(ACTION_COURSE_ALARM));
    }

    public void onReceive(Intent intent) {
        if (ACTION_COURSE_ALARM.equals(intent.getAction())) {
            handleCourseAlarm(intent);
        } else if (ACTION_MEMO_ALARM.equals(intent.getAction())) {
        	handleMemoAlarm(intent);
        } 
    }

    private void handleMemoAlarm(Intent intent) {
    	String title = "暖暖备忘贴提醒";
        String content = "您还有一项任务没完成哦!";
        NoteInfo info = intent.getParcelableExtra(MemoDetailActivity.EXT_MEMO_NOTE);
        NotificationHelper.getInstance(mContext).sendMemoNotification(title, content, info);
	}

	private void handleCourseAlarm(Intent intent) {
        Resources res = mContext.getResources();
        int alarmDay = intent.getIntExtra(EXTRA_COURSE_ALARM_DAY, 0);
        String[] days = res.getStringArray(R.array.setting_daily_course_alarm_day);
        Time alarm = new Time();
        alarm.setToNow();
        alarm.weekDay += days.length - alarmDay - 1;
        if (alarm.weekDay == 0) {
            alarm.weekDay = Calendar.DAY_OF_WEEK;
        }
        CourseManager cm = CourseManager.getInstance(mContext);
        int count = cm.getDailyCourses(alarm.weekDay).size();
        if (count > 0) {
            String title = mContext.getString(R.string.notify_course);
            String content = String.format(res.getStringArray(
                    R.array.setting_daily_course_remind)[alarmDay], count);
            NotificationHelper.getInstance(mContext).sendCourseNotification(title, content);
        }
    }

    private void setAlarm(Intent intent, boolean isRepeating,
            long triggerAtMillis, long intervalMillis) {
        PendingIntent pi = PendingIntent.getBroadcast(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (isRepeating) {
            mAlarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, intervalMillis, pi);
        } else {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }

    private void cancelAlarm(Intent intent) {
        PendingIntent pi = PendingIntent.getBroadcast(
                mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.cancel(pi);
    }
}
