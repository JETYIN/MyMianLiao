package com.tjut.mianliao.component;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Message;

import com.tjut.mianliao.util.Utils;

public class CountDowner {

    private static final String TAG = "CountDowner";

    ArrayList<CommonBanner> notifiedBanners = new ArrayList<CommonBanner>();

    ArrayList<CommonBanner> preNotifiedBanners = new ArrayList<CommonBanner>();
    
    ArrayList<Handler> mHandlers = new ArrayList<>();
    
    int countdownSecs;
    boolean isToNight;
    Timer timer;
    CountTask task;
    private static CountDowner sCountDowner;

    class CountTask extends TimerTask {

        @Override
        public void run() {

            if (countdownSecs == 0) {

                return;
            }

            notifiedBanners.addAll(preNotifiedBanners);

            preNotifiedBanners.clear();

            for (CommonBanner banner : notifiedBanners) {
                String str;
                String timeStr = getTimeDistance(countdownSecs);
                
                if (isToNight) {
                    str = "距黑洞还有" + timeStr;
                } else {
                    str = "距白洞还有" + timeStr;
                }
                Message msg = new Message();
                msg.obj = str;
                banner.handler.sendMessage(msg);
            }
            
            for (Handler handler : mHandlers) {
                Message msg = new Message();
                msg.obj = getTimeDistance(countdownSecs);
                handler.sendMessage(msg);
            }

            countdownSecs--;

        }

    }

    private String getTimeDistance(int secs) {
        StringBuffer sb = new StringBuffer();
        if (secs >= 3600) {
            int hour = secs / 3600;
            int minute = (secs - hour * 3600) / 60;
            int second = secs % 60;

            sb.append((hour < 10 ? "0" : "") + hour + ":");
            sb.append((minute < 10 ? "0" : "") + minute + ":");
            sb.append((second < 10 ? "0" : "") + second);
        } else if (secs >= 60) {
            int minute = secs / 60;
            int sec = secs - minute * 60;
            sb.append("00" + ":");
            sb.append((minute < 10 ? "0" : "") + minute + ":");
            sb.append((sec < 10 ? "0" : "") + sec);
        } else {
            sb.append("00:");
            sb.append("00:");
            sb.append((secs < 10 ? "0" : "") + secs);
        }

        return sb.toString();

    }

    public void registerBanner(CommonBanner tv) {
        preNotifiedBanners.add(tv);
    }

    public static void create(int countdown, boolean isToNight) {
        Utils.logD(TAG, "create countdown --> " + countdown + ",is_to_night--> " + isToNight);
        if (getInstance() != null) {
            CountDowner cd = getInstance();
            cd.retrigger();
            cd.countdownSecs = countdown;
            cd.isToNight = isToNight;

            return;
        }
        CountDowner cd = new CountDowner(countdown, isToNight);
        sCountDowner = cd;
    }

    public static CountDowner getInstance() {
        return sCountDowner;
    }

    public void retrigger() {
        if (timer != null) {
            timer.cancel();
            task.cancel();
            task = new CountTask();
            timer = new Timer();
            timer.schedule(task, 0, 1000);

        }
    }

    public CountDowner(int countdown, boolean is_to_night) {
        this.countdownSecs = countdown;
        this.isToNight = is_to_night;
        timer = new Timer();
        task = new CountTask();
        timer.schedule(task, 0, 1000);
    }

    public void setHandler(Handler handler) {
        if (!mHandlers.contains(handler)) {
            mHandlers.add(handler);
        }
    }
    
}
