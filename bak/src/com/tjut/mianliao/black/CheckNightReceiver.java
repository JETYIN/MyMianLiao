package com.tjut.mianliao.black;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tjut.mianliao.component.CountDowner;
import com.tjut.mianliao.data.SystemInfo;
import com.tjut.mianliao.settings.Settings;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.Utils;

public class CheckNightReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        new SystemInfoTask(context).executeLong();
    }

    private class SystemInfoTask extends MsTask {

        Context context;

        public SystemInfoTask(Context context) {
            super(context, MsRequest.SYSTEM_INFO);
            this.context = context;
        }

        @Override
        protected void onPostExecute(MsResponse response) {

            if (response.isSuccessful()) {
                SystemInfo systemInfo = SystemInfo.fromJson(response.getJsonObject());
                if (systemInfo != null) {

                    doChangeDayMode(systemInfo.neight, context, systemInfo.start);

                }
            }
        }

    }

    public static void doChangeDayMode(boolean isToNight, Context context, int time) {
        if ((isToNight && Settings.getInstance(context).isNightMode())
                || (!isToNight && !Settings.getInstance(context).isNightMode())) {
            return;
        }

        Settings.setNightInfoToSp(isToNight);
        NightAnimActivity.setAnimFlag(isToNight);

        CountDowner.create(time, isToNight);

        if (!Utils.isRunningBackground(context)) {

            NightAnimActivity.playChangeDayAnim(context);
        }
    }

}
