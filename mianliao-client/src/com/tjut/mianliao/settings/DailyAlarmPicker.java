package com.tjut.mianliao.settings;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;

import com.tjut.mianliao.R;

public class DailyAlarmPicker {

    private View mRootView;

    private WheelView mWvDay;
    private WheelView mWvHour;
    private WheelView mWvMinute;

    public DailyAlarmPicker(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.daily_course_alarm_picker, null);
        Resources res = context.getResources();

        mWvDay = (WheelView) view.findViewById(R.id.wv_daily_alarm_day);
        ArrayWheelAdapter<String> dayAdapter = new ArrayWheelAdapter<String>(
                context, res.getStringArray(R.array.setting_daily_course_alarm_day));
        dayAdapter.setItemResource(R.layout.wheel_item_period);
        mWvDay.setViewAdapter(dayAdapter);

        mWvHour = (WheelView) view.findViewById(R.id.wv_daily_alarm_hour);
        NumericWheelAdapter hourAdapter = new NumericWheelAdapter(
                context, 0, 23, res.getString(R.string.setting_daily_course_alarm_hour));
        hourAdapter.setItemResource(R.layout.wheel_item_period);
        mWvHour.setViewAdapter(hourAdapter);
        mWvHour.setCyclic(true);

        mWvMinute = (WheelView) view.findViewById(R.id.wv_daily_alarm_minute);
        NumericWheelAdapter minuteAdapter = new NumericWheelAdapter(
                context, 0, 59, res.getString(R.string.setting_daily_course_alarm_minute));
        minuteAdapter.setItemResource(R.layout.wheel_item_period);
        mWvMinute.setViewAdapter(minuteAdapter);
        mWvMinute.setCyclic(true);

        mRootView = view;
    }

    public View getRootView() {
        return mRootView;
    }

    public int getDay() {
        return mWvDay.getCurrentItem();
    }

    public int getHour() {
        return mWvHour.getCurrentItem();
    }

    public int getMinute() {
        return mWvMinute.getCurrentItem();
    }

    public void setTime(int day, int hour, int minute) {
        mWvDay.setCurrentItem(day);
        mWvHour.setCurrentItem(hour);
        mWvMinute.setCurrentItem(minute);
    }
}
