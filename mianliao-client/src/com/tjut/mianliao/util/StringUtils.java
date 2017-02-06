package com.tjut.mianliao.util;

import android.text.TextUtils;

public class StringUtils {

    public static boolean equals(String lStr, String rStr) {
        if (lStr.endsWith(rStr)) {
            return true;
        }
        return false;
    }

    public static String parseName(String jid) {
        if (TextUtils.isEmpty(jid)) {
            return null;
        }
        String name = null;
        int indexAt = jid.indexOf("@");
        if (indexAt == -1) {
            return jid;
        }
        name = jid.substring(0, indexAt);
        return name;
    }
}
