package com.tjut.mianliao.chat;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.tjut.mianliao.data.DataHelper;

public class HighChatHelper {

    public static final String HIGH_ITEM_DAY = "high_item_day";
    public static final String HIGH_ITEM_DAY_STR = "high_item_day_str";

    public static boolean isChecked(Context context, int index) {
        SharedPreferences mPreferences = DataHelper.getSpForData(context);

        Editor editor = mPreferences.edit();

        String index_str = index + "";

        long current_time = new Date().getTime();

        long last_time = mPreferences.getLong(HIGH_ITEM_DAY, 0);
        // if the first time
        if (last_time == 0) {
            editor.putLong(HIGH_ITEM_DAY, current_time);
            editor.putString(HIGH_ITEM_DAY_STR, index_str);
            editor.commit();
            return true;
        } else {

            // if not the first time

            long current_day = current_time / (3600 * 24 * 1000);

            long last_day = last_time / (3600 * 24 * 1000);

            // if the same day

            if (current_day == last_day) {

                String original_str = mPreferences.getString(HIGH_ITEM_DAY_STR, "");

                String[] array = original_str.split(",");

                // check if exists

                boolean is_found = false;

                for (String str : array) {

                    if (!index_str.equals(str)) {
                        int store_index = Integer.parseInt(str);
                        if ((store_index / 2) == (index / 2)) {
                            is_found = true;
                        }
                    }

                }

                // /////////////////////////////

                if (is_found) {
                    return false;
                } else {
                    editor.putString(HIGH_ITEM_DAY_STR, original_str + "," + index_str);
                    editor.commit();
                }

                return true;

            } else {
                editor.putLong(HIGH_ITEM_DAY, current_time);
                editor.putString(HIGH_ITEM_DAY_STR, index_str);
                editor.commit();
                return true;
            }

        }

    }

}
