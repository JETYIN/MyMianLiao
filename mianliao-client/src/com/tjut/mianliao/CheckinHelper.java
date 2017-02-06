package com.tjut.mianliao;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.tjut.mianliao.data.AccountInfo;
import com.tjut.mianliao.data.DataHelper;
import com.tjut.mianliao.util.MsRequest;
import com.tjut.mianliao.util.MsResponse;
import com.tjut.mianliao.util.MsTask;
import com.tjut.mianliao.util.MsTaskListener;
import com.tjut.mianliao.util.Utils;

public class CheckinHelper {

    private CheckinHelper() {}

    private static final String SP_LAST_CHECK_IN_ON = "last_check_in_on";

    private static BroadcastReceiver sConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())
                    && Utils.isWifiConnected(context) && isCheckedIn(context)) {
                checkin(context, null);
            }
        }
    };

    /**
     * Set up auto checkin so that the user can have a stable wifi connection
     */
    public static void setupAutoCheckin(Context ctx) {
        ctx.registerReceiver(sConnectivityReceiver,
                new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }

    public static void disableAutoCheckin(Context ctx) {
        ctx.unregisterReceiver(sConnectivityReceiver);
    }

    public static boolean isCheckedIn(Context context) {
        boolean isCheckedIn = false;
        if (AccountInfo.getInstance(context).isLoggedIn()) {
            long lastCheckinOn = DataHelper.getSpForData(context)
                    .getLong(SP_LAST_CHECK_IN_ON, 0);
            if (lastCheckinOn == 0) {
                isCheckedIn = false;
            } else {
                Calendar cld = Calendar.getInstance();
                int today = cld.get(Calendar.DAY_OF_MONTH);
                cld.setTimeInMillis(lastCheckinOn);
                isCheckedIn = today == cld.get(Calendar.DAY_OF_MONTH);
            }
        }
        return isCheckedIn;
    }

    /**
     * Set local checked in.
     */
    public static void checkin(Context context) {
        DataHelper.getSpForData(context)
                .edit()
                .putLong(SP_LAST_CHECK_IN_ON, System.currentTimeMillis())
                .commit();
    }

    /**
     * Perform checkin action
     */
    public static void checkin(Context context, MsTaskListener listener) {
        new CheckinTask(context, listener == null).setTaskListener(listener).executeLong();
    }

    private static class CheckinTask extends MsTask {
        boolean mAuto;

        public CheckinTask(Context context, boolean auto) {
            super(context, MsRequest.CHECK_IN);
            mAuto = auto;
        }

        @Override
        protected String buildParams() {
            return Utils.getDeviceInfoForWifi(getRefContext()) + "&auto=" + (mAuto ? 1 : 0);
        }

        @Override
        protected void onPostExecute(MsResponse response) {
            super.onPostExecute(response);
            if (!mAuto && (MsResponse.isSuccessful(response) ||
                    response.code == MsResponse.MS_USER_ALREADY_CHECKIN_TODAY)) {
                checkin(getRefContext());
            }
        }
    }
}
