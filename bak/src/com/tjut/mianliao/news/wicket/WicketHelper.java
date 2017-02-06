package com.tjut.mianliao.news.wicket;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.MsResponse;

public class WicketHelper {

    public static final String TICKET_API = "api/ticket";
    public static final String REQ_CHECKIN = "checkin";

    public static final String TICKET_REGEX = "TK-\\d{5,}-\\d{1,}-\\d{10,}";

    private static final String SHARED_PREF_NAME = "ticket";
    private static final String SP_CHECKING_RECORDS = "checking_records";

    private static final String SEPARATOR = "===";
    private static final int MAX_HISTORY = 30;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private static WeakReference<WicketHelper> sInstanceRef;

    private ArrayList<WicketRecord> mRecords = new ArrayList<WicketRecord>();
    private SharedPreferences mSharedPrefs;

    private WicketHelper(Context context) {
        mSharedPrefs = context.getSharedPreferences(SHARED_PREF_NAME, 0);

        String tickets = mSharedPrefs.getString(SP_CHECKING_RECORDS, "");
        HashSet<String> ticketSet = new HashSet<String>();
        for (String ticket : tickets.split(SEPARATOR)) {
            if (ticketSet.contains(ticket)) {
                continue;
            }
            ticketSet.add(ticket);
            WicketRecord record = WicketRecord.fromJsonString(mSharedPrefs.getString(ticket, ""));
            if (record != null) {
                mRecords.add(record);
            }
        }
    }

    public static synchronized WicketHelper getInstance(Context context) {
        if (sInstanceRef == null || sInstanceRef.get() == null) {
            WicketHelper th = new WicketHelper(context);
            sInstanceRef = new WeakReference<WicketHelper>(th);
        }
        return sInstanceRef.get();
    }

    public ArrayList<WicketRecord> getRecords() {
        return mRecords;
    }

    public void add(WicketRecord newRecord) {
        if (newRecord != null && !TextUtils.isEmpty(newRecord.ticket)) {
            // Put the record at the beginning.
            mRecords.add(0, newRecord);

            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putString(newRecord.ticket, newRecord.toJsonString());

            // Store 30 record at max
            if (mRecords.size() > MAX_HISTORY) {
                WicketRecord r = mRecords.remove(mRecords.size() - 1);
                editor.putString(r.ticket, null);
            }

            // Update index
            StringBuilder sb = new StringBuilder();
            if (mRecords.size() > 0) {
                for (WicketRecord r : mRecords) {
                    if (sb.length() > 0) {
                        sb.append(SEPARATOR);
                    }
                    sb.append(r.ticket);
                }
            }
            editor.putString(SP_CHECKING_RECORDS, sb.toString()).commit();
        }
    }

    public static String getFailDesc(Context context, int responseCode, WicketRecord record) {
        String reason;
        switch (responseCode) {
            case MsResponse.MS_TICKET_ALREADY_CHECKED:
                reason = context.getString(R.string.tic_fail_checked_on);
                if (record != null && record.checkedOn > 0) {
                    reason += formatTime(record.checkedOn * 1000);
                }
                break;
            case MsResponse.MS_TICKET_INVALID:
                reason = context.getString(R.string.tic_fail_invalid);
                break;
            case MsResponse.MS_TICKET_NOT_AUTHED:
                reason = context.getString(R.string.tic_fail_not_authed);
                break;
            case MsResponse.HTTP_NOT_CONNECTED:
                reason = context.getString(R.string.no_network);
                break;
            case MsResponse.HTTP_TIMEOUT:
                reason = context.getString(R.string.connect_timeout);
                break;
            default:
                reason = context.getString(R.string.tic_fail_other) + responseCode;
                break;
        }
        return reason;
    }

    public static synchronized String formatTime(long time) {
        return DATE_FORMAT.format(new Date(time));
    }
}
