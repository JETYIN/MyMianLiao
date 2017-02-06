package com.tjut.mianliao.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.tjut.mianliao.util.JsonUtil;

public class CheckIn {

    int month_start, week_start, today_index, card_package_id, continuous_days;

    String checkin;
    
    public static ArrayList<SignProgressInfo> mSignInfos = new ArrayList<SignProgressInfo>();

    public int getCard_package_id() {
        return card_package_id;
    }

    public int getContinuous_days() {
        return continuous_days;
    }

    public void setContinuous_days(int continuous_days) {
        this.continuous_days = continuous_days;
    }

    public void setCard_package_id(int card_package_id) {
        this.card_package_id = card_package_id;
    }

    public int getToday_index() {
        return today_index;
    }

    public void setToday_index(int today_index) {
        this.today_index = today_index;
    }

    public int getMonth_start() {
        return month_start;
    }

    public void setMonth_start(int month_start) {
        this.month_start = month_start;
    }

    public int getWeek_start() {
        return week_start;
    }

    public void setWeek_start(int week_start) {
        this.week_start = week_start;
    }

    public String getCheckin() {
        return checkin;
    }

    public void setCheckin(String checkin) {
        this.checkin = checkin;
    }

    public static CheckIn fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        CheckIn check_in = new CheckIn();
        check_in.setCheckin(json.optString("checkin"));
        check_in.setMonth_start(json.optInt("month_start"));
        check_in.setWeek_start(json.optInt("current_week_index"));
        check_in.setToday_index(json.optInt("today_index"));
        check_in.setCard_package_id(json.optInt("card_package_id"));
        check_in.setContinuous_days(json.optInt("continuous_days"));
        JSONArray ja = json.optJSONArray("reward_list");
        try {
            if (ja != null) {
                ArrayList<SignProgressInfo> infos = new ArrayList<>();
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jso = ja.getJSONObject(i);
                    SignProgressInfo spi = SignProgressInfo.fromJson(jso);
                    if (spi != null) {
                        infos.add(spi);
                    }
                }
                fillData(infos);
            }
        } catch (Exception e) {
        }
        return check_in;
    }

    private static void fillData(ArrayList<SignProgressInfo> infos) {
        if (infos != null && infos.size() > 0) {
            mSignInfos = infos;
        }
    }

    public static final JsonUtil.ITransformer<CheckIn> TRANSFORMER =
            new JsonUtil.ITransformer<CheckIn>() {

        @Override
        public CheckIn transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public String currentCheckInStrs() {
        return this.getCheckin().substring(this.getWeek_start(), this.getWeek_start() + 7);
    }

    public int getSequenceSignCount() {
        int count = 0;
        for (int i = 0; i <= 27; i++) {
            if (this.getCheckin().charAt(i) == '1') {
                count++;
            } else if (getCheckin().charAt(i) == '0') {
                count = 0;
            }
        }
        return count;
    }

    @SuppressLint("SimpleDateFormat")
    public String[] dateWeekStr() {
        Date date = new Date();// 取时间
        long time = this.getMonth_start() * 1000L;
        date.setTime(time);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        SimpleDateFormat sf = new SimpleDateFormat("MM月dd日");
        calendar.add(Calendar.DATE, this.week_start - 1);

        String[] weekStrArray = new String[7];

        for (int i = 0; i < 7; i++) {
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime();
            weekStrArray[i] = sf.format(date);
        }
        return weekStrArray;
    }

}
