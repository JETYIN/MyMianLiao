package com.tjut.mianliao.theme;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.tjut.mianliao.R;

public class ThemeDrawableSource {

    static HashMap<String, Integer> sResNameMap;

    static HashMap<Integer, String> sResIdMap;

    public static String getResName(int resId) {
        if (sResIdMap == null) {
            sResIdMap = new HashMap<Integer, String>();
            Field[] fields = R.drawable.class.getFields();

            for (Field field : fields) {
                try {
                    sResIdMap.put(field.getInt(null), field.getName());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        return sResIdMap.get(Integer.valueOf(resId));
    }

    public static int getResId(String resName) {
        sResNameMap = new HashMap<String, Integer>();
        if (sResNameMap.size() == 0) {
            Field[] fields = R.drawable.class.getFields();

            for (Field field : fields) {
                try {
                    sResNameMap.put(field.getName(), field.getInt(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        }

        if (sResNameMap.get(resName) == null) {
            return -1;
        } else {
            return sResNameMap.get(resName);
        }

    }

}
