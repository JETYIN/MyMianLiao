package com.tjut.mianliao.settings;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.text.TextUtils;
import android.text.format.Time;

import com.tjut.mianliao.util.AdvAsyncTask;
import com.tjut.mianliao.xmpp.PrivacyHelper;

public class Settings implements OnSharedPreferenceChangeListener {
    static final String NAME_SHARED_PREFS = "settings";

    public static final String CONTACT_PRIVACY = "contact_privacy";

    static final String KEY_NEW_MESSAGE_SOUND = "new_message_sound";
    static final String KEY_NEW_MESSAGE_VIBRATE = "new_message_vibrate";
    static final String KEY_NIGHT_MODE = "night_mode";
    static final String KEY_ENTER_SEND_MESSAGE = "enter_send_message";
    public static final String KEY_CHECK_DAY_NIGHT = "check_day_night";
    static final String KEY_DEFAULT_TAB = "default_tab";
    public static final String KEY_DAILY_COURSE_ALARM = "daily_course_alarm";
    public static final String KEY_DAILY_COURSE_ALARM_TIME = "daily_course_alarm_time";
    static final String KEY_ONLY_DOWNLOAD_PICTURES_WITH_WIFI = "only_download_pictures_with_wifi";

    static final String KEY_TEMPORARY_CHAT = "temporary_chat";
    static final String KEY_CONTACT_PRIVACY = CONTACT_PRIVACY;

    static final int NIGHT_MODE_HOUR_BEGIN = 23;
    static final int NIGHT_MODE_HOUR_END = 8;
    static final int DEFAULT_TAB_INDEX = 0;

    private static final String KEY_BLACKLIST_SET = "blacklist_set";
    private static final String KEY_NICK_HINT = "nick_hint";

    private static final String KEY_GPS_HINT = "gps_hint";

    private static final String KEY_PAGE_COUNT = "page_count";

    private static WeakReference<Settings> sInstanceRef;

    private static SharedPreferences mSharedPrefs;

    public static synchronized Settings getInstance(Context context) {
        if (sInstanceRef != null && sInstanceRef.get() != null) {
            return sInstanceRef.get();
        }

        Settings instance = new Settings(context);
        sInstanceRef = new WeakReference<Settings>(instance);
        return instance;
    }

    private Settings(Context context) {
        mSharedPrefs = context.getSharedPreferences(NAME_SHARED_PREFS, 0);
        initDefaultValues(false);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_TEMPORARY_CHAT.equals(key)) {
            new AdvAsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    return PrivacyHelper.setAllowTemporaryChat(getBoolean(KEY_TEMPORARY_CHAT));
                }
            }.executeLong();
        }
    }

    public void clear() {
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
        initDefaultValues(true);
        mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    public void registerChangeListener(OnSharedPreferenceChangeListener listener) {
        mSharedPrefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterChangeListener(OnSharedPreferenceChangeListener listener) {
        mSharedPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public boolean allowNewMessageSound() {
        return getBoolean(KEY_NEW_MESSAGE_SOUND);
    }

    public boolean allowNewMessageVibrate() {
        return getBoolean(KEY_NEW_MESSAGE_VIBRATE);
    }

    public boolean isInNightMode() {
        if (getBoolean(KEY_NIGHT_MODE)) {
            Time now = new Time();
            now.setToNow();
            return now.hour < NIGHT_MODE_HOUR_END
                    || now.hour >= NIGHT_MODE_HOUR_BEGIN;
        }
        return false;
    }

    public boolean allowEnterToSend() {
        return getBoolean(KEY_ENTER_SEND_MESSAGE);
    }

    public boolean isNightMode() {
        return getBoolean(KEY_CHECK_DAY_NIGHT);
    }

    public int getDefaultTabIndex() {
        return getInt(KEY_DEFAULT_TAB);
    }

    public void setDefaultTabIndex(int value) {
        setInt(KEY_DEFAULT_TAB, value);
    }

    public boolean allowDailyCourseAlarm() {
        return getBoolean(KEY_DAILY_COURSE_ALARM);
    }

    public int getDailyCourseAlarmDay() {
        return (getInt(KEY_DAILY_COURSE_ALARM_TIME) >> 16) & 0xFF;
    }

    public int getDailyCourseAlarmHour() {
        return (getInt(KEY_DAILY_COURSE_ALARM_TIME) >> 8) & 0xFF;
    }

    public int getDailyCourseAlarmMinute() {
        return getInt(KEY_DAILY_COURSE_ALARM_TIME) & 0xFF;
    }

    public void setDailyCourseAlarmTime(int day, int hour, int minute) {
        int time = (day << 16) + (hour << 8) + minute;
        setInt(KEY_DAILY_COURSE_ALARM_TIME, time);
    }

    public boolean allowTemporaryChat() {
        if (PrivacyHelper.isReady()) {
            return PrivacyHelper.allowTemporaryChat();
        }
        return getBoolean(KEY_TEMPORARY_CHAT);
    }

    public static void setNightInfoToSp(boolean isNightMode) {
        Editor edit = mSharedPrefs.edit();
        edit.putBoolean(Settings.KEY_CHECK_DAY_NIGHT, isNightMode);
        edit.commit();
    }

    public int getContactPrivacy() {
        return getBoolean(KEY_CONTACT_PRIVACY) ? 1 : 0;
    }

    public void setContactPrivacy(int cp) {
        setBoolean(KEY_CONTACT_PRIVACY, cp == 1);
    }

    public Set<String> getBlacklistSet() {
        return getStringSet(KEY_BLACKLIST_SET);
    }

    public void setBlacklistSet(Set<String> value) {
        setStringSet(KEY_BLACKLIST_SET, value);
    }

    public boolean allowNickHint() {
        return getBoolean(KEY_NICK_HINT);
    }

    public void setNickHint(boolean value) {
        setBoolean(KEY_NICK_HINT, value);
    }

    public boolean allowGpsHint() {
        return getBoolean(KEY_GPS_HINT);
    }

    public void setGpsHint(boolean value) {
        setBoolean(KEY_GPS_HINT, value);
    }

    public int getPageCount() {
        return getInt(KEY_PAGE_COUNT);
    }

    public void setPageCount(int value) {
        setInt(KEY_PAGE_COUNT, value);
    }

    public boolean downloadPicturesWithWifi() {
        return mSharedPrefs.getBoolean(KEY_ONLY_DOWNLOAD_PICTURES_WITH_WIFI, false);
    }

    public boolean getBoolean(String key) {
        return mSharedPrefs.getBoolean(key, false);
    }

    public void setBoolean(String key, boolean value) {
        if (key != null) {
            mSharedPrefs.edit().putBoolean(key, value).commit();
        }
    }

    public int getInt(String key) {
        return mSharedPrefs.getInt(key, 0);
    }

    public void setInt(String key, int value) {
        if (key != null) {
            mSharedPrefs.edit().putInt(key, value).commit();
        }
    }

    public String getString(String key) {
        return mSharedPrefs.getString(key, null);
    }

    public void setString(String key, String value) {
        if (key != null) {
            mSharedPrefs.edit().putString(key, value).commit();
        }
    }

    public Set<String> getStringSet(String key) {
        HashSet<String> values = new HashSet<String>();
        String value = mSharedPrefs.getString(key, null);
        if (!TextUtils.isEmpty(value)) {
            for (String s : value.split(",")) {
                values.add(s);
            }
        }
        return values;
    }

    public void setStringSet(String key, Set<String> value) {
        if (key != null) {
            String result = null;
            if (value != null) {
                result = TextUtils.join(",", value.toArray());
            }
            mSharedPrefs.edit().putString(key, result).commit();
        }
    }

    private void initDefaultValues(boolean enforced) {
        if (enforced || !mSharedPrefs.contains(KEY_NEW_MESSAGE_SOUND)) {
            setBoolean(KEY_NEW_MESSAGE_SOUND, true);
        }
        if (enforced || !mSharedPrefs.contains(KEY_NEW_MESSAGE_VIBRATE)) {
            setBoolean(KEY_NEW_MESSAGE_VIBRATE, true);
        }
        if (enforced || !mSharedPrefs.contains(KEY_NIGHT_MODE)) {
            setBoolean(KEY_NIGHT_MODE, false);
        }
        if (enforced || !mSharedPrefs.contains(KEY_ENTER_SEND_MESSAGE)) {
            setBoolean(KEY_ENTER_SEND_MESSAGE, false);
        }
        if (enforced || !mSharedPrefs.contains(KEY_DEFAULT_TAB)) {
            setDefaultTabIndex(DEFAULT_TAB_INDEX);
        }
        if (enforced || !mSharedPrefs.contains(KEY_DAILY_COURSE_ALARM)) {
            setBoolean(KEY_DAILY_COURSE_ALARM, true);
        }
        if (enforced || !mSharedPrefs.contains(KEY_DAILY_COURSE_ALARM_TIME)) {
            setDailyCourseAlarmTime(0, 20, 0);
        }
        if (enforced || !mSharedPrefs.contains(KEY_TEMPORARY_CHAT)) {
            setBoolean(KEY_TEMPORARY_CHAT, true);
        }
        if (enforced || !mSharedPrefs.contains(KEY_CONTACT_PRIVACY)) {
            setBoolean(KEY_CONTACT_PRIVACY, false);
        }
        if (enforced || !mSharedPrefs.contains(KEY_BLACKLIST_SET)) {
            setBlacklistSet(null);
        }
        if (enforced || !mSharedPrefs.contains(KEY_NICK_HINT)) {
            setNickHint(true);
        }
        if (enforced || !mSharedPrefs.contains(KEY_GPS_HINT)) {
            setGpsHint(true);
        }
        if (enforced || !mSharedPrefs.contains(KEY_PAGE_COUNT)) {
            setPageCount(20);
        }
        if (enforced || !mSharedPrefs.contains(KEY_ONLY_DOWNLOAD_PICTURES_WITH_WIFI)) {
            setBoolean(KEY_ONLY_DOWNLOAD_PICTURES_WITH_WIFI, false);
        }
    }
}
