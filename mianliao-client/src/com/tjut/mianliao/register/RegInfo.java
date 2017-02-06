package com.tjut.mianliao.register;

import java.util.HashMap;

import android.text.TextUtils;

public class RegInfo {

    public static final String EDIT = "edit";

    public int schoolId;
    public String schoolName;
    public int departmentId;
    public String departmentName;
    public int startYear;

    public String userName;
    public String password;
    public String nickName;
    public int gender;
    public String email;
    public String avatar;
    public boolean avatarConfirmed;
    public String eduback;
    public int meduback;

    private static RegInfo sInstance;

    public static RegInfo getInstance() {
        synchronized (RegInfo.class) {
            if (sInstance == null) {
                sInstance = new RegInfo();
            }
        }
        return sInstance;
    }

    public static void destroyInstance() {
        synchronized (RegInfo.class) {
            sInstance = null;
        }
    }

    public HashMap<String, String> getParameters() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("password", password);
        params.put("school_id", String.valueOf(schoolId));
        params.put("nick", nickName);
        params.put("gender", String.valueOf(gender));
        params.put("email", email);
        return params;
    }

    public HashMap<String, String> getFiles() {
        if (TextUtils.isEmpty(avatar)) {
            return null;
        }
        HashMap<String, String> files = new HashMap<String, String>();
        files.put("avatar", avatar);
        return files;
    }
}
