package com.tjut.mianliao.forum;

import java.util.Calendar;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.tjut.mianliao.R;
import com.tjut.mianliao.component.LightDialog;

public class TimePicker implements DialogInterface.OnClickListener, OnWheelChangedListener {

    private static final String FORMAT = "%1$02d";

    private Context mContext;
    private LightDialog mDateDialog;
    private LightDialog mTimeDialog;

    private WheelView mWvYear;
    private WheelView mWvMonth;
    private WheelView mWvHour;
    private WheelView mWvMinute;
    private WheelView mWvDay;
    private RevisableWheelAdapter mDayAdapter;

    private Calendar mTime;
    private int mBaseYear;
    private Callback mCallback;
    private int mRequestCode;

    public TimePicker(Context context) {
        mContext = context;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void pick(long timeMills, int requestCode) {
        mRequestCode = requestCode;

        long time = timeMills == 0 ? System.currentTimeMillis() : timeMills;
        mTime = Calendar.getInstance(mContext.getResources().getConfiguration().locale);
        mTime.setTimeInMillis(time);
        mTime.set(Calendar.SECOND, 0);

        initLayout();
        initTime();
        mDateDialog.show();
    }

    private void initLayout() {
        if (mDateDialog == null) {
            mDateDialog = new LightDialog(mContext)
                    .setTitleLd(R.string.date)
                    .setView(initDateView())
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(android.R.string.cancel, null);
        }
        if (mTimeDialog == null) {
            mTimeDialog = new LightDialog(mContext)
                    .setTitleLd(R.string.time)
                    .setView(initTimeView())
                    .setPositiveButton(android.R.string.ok, this)
                    .setNegativeButton(R.string.back, this);
        }
    }

    private void initTime() {
        mWvYear.setCurrentItem(0);
        mWvMonth.setCurrentItem(mTime.get(Calendar.MONTH));
        mWvDay.setCurrentItem(mTime.get(Calendar.DAY_OF_MONTH) - 1);

        mWvHour.setCurrentItem(mTime.get(Calendar.HOUR_OF_DAY));
        mWvMinute.setCurrentItem(mTime.get(Calendar.MINUTE));
    }

    private void saveDate() {
        mTime.set(Calendar.YEAR, mBaseYear + mWvYear.getCurrentItem());
        mTime.set(Calendar.MONTH, mWvMonth.getCurrentItem());
        mTime.set(Calendar.DAY_OF_MONTH, mWvDay.getCurrentItem() + 1);
    }

    private void saveTime() {
        mTime.set(Calendar.HOUR_OF_DAY, mWvHour.getCurrentItem());
        mTime.set(Calendar.MINUTE, mWvMinute.getCurrentItem());
    }

    private View initDateView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.time_picker_date, null);
        mBaseYear = mTime.get(Calendar.YEAR);
        NumericWheelAdapter yearAdapter = new NumericWheelAdapter(mContext, mBaseYear,
                mBaseYear + 6);
        yearAdapter.setItemResource(R.layout.wheel_item_period);
        mWvYear = (WheelView) view.findViewById(R.id.wv_year);
        mWvYear.setViewAdapter(yearAdapter);

        NumericWheelAdapter monthAdapter = new NumericWheelAdapter(mContext, 1, 12, FORMAT);
        monthAdapter.setItemResource(R.layout.wheel_item_period);
        mWvMonth = (WheelView) view.findViewById(R.id.wv_month);
        mWvMonth.setViewAdapter(monthAdapter);
        mWvMonth.addChangingListener(this);

        mDayAdapter = new RevisableWheelAdapter(mContext, 1,
                mTime.getActualMaximum(Calendar.DAY_OF_MONTH), R.layout.wheel_item_period);
        mWvDay = (WheelView) view.findViewById(R.id.wv_day);
        mWvDay.setViewAdapter(mDayAdapter);

        return view;
    }

    private View initTimeView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.time_picker_time, null);

        NumericWheelAdapter hourAdapter = new NumericWheelAdapter(mContext, 0, 23, FORMAT);
        hourAdapter.setItemResource(R.layout.wheel_item_period);
        mWvHour = (WheelView) view.findViewById(R.id.wv_hour);
        mWvHour.setViewAdapter(hourAdapter);

        NumericWheelAdapter minuteAdapter = new NumericWheelAdapter(mContext, 0, 59, FORMAT);
        minuteAdapter.setItemResource(R.layout.wheel_item_period);
        mWvMinute = (WheelView) view.findViewById(R.id.wv_minute);
        mWvMinute.setViewAdapter(minuteAdapter);

        return view;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mDateDialog) {
            saveDate();
            mTimeDialog.show();
        } else if (dialog == mTimeDialog) {
            saveTime();
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (mCallback != null) {
                        mCallback.onResult(mTime, mRequestCode);
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    mDateDialog.show();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue) {
        if (wheel == mWvMonth) {
            mTime.set(Calendar.MONTH, newValue);
            int actualMax = mTime.getActualMaximum(Calendar.DAY_OF_MONTH);
            mDayAdapter.update(1, actualMax);
            if (mWvDay.getCurrentItem() >= actualMax) {
                mWvDay.setCurrentItem(actualMax - 1);
            }
        }
    }

    public static interface Callback {
        public void onResult(Calendar time, int requestCode);
    }

    private static class RevisableWheelAdapter extends AbstractWheelTextAdapter {

        protected int mMinValue;
        protected int mMaxValue;

        protected RevisableWheelAdapter(Context context, int minValue, int maxValue,
                int itemResource) {
            super(context, itemResource);
            mMinValue = minValue;
            mMaxValue = maxValue;
        }

        protected void update(int minValue, int maxValue) {
            mMinValue = minValue;
            mMaxValue = maxValue;
            notifyDataInvalidatedEvent();
        }

        @Override
        protected CharSequence getItemText(int index) {
            if (index >= 0 && index < getItemsCount()) {
                return String.format(FORMAT, mMinValue + index);
            }
            return null;
        }

        @Override
        public int getItemsCount() {
            return mMaxValue - mMinValue + 1;
        }
    }
}
