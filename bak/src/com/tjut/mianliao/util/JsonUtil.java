package com.tjut.mianliao.util;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Json helper class, currently the main function is to help create a java Array from a Json Array.
 */
public class JsonUtil {

    private JsonUtil() {}

    public interface ITransformer<T> {
        T transform(JSONObject json);
    }

    public static <T> ArrayList<T> getArray(JSONArray ja, ITransformer<T> transformer) {
        ArrayList<T> items = new ArrayList<T>();
        int length = ja == null ? 0 : ja.length();
        for (int i = 0; i < length; i++) {
            T item = transformer.transform(ja.optJSONObject(i));
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    public static <T> ArrayList<ArrayList<T>> getArrays(JSONArray ja, ITransformer<T> transformer) {
        ArrayList<ArrayList<T>> items = new ArrayList<ArrayList<T>>();
        int length = ja == null ? 0 : ja.length();
        for (int i = 0; i < length; i++) {
            ArrayList<T> itemsSub = new ArrayList<T>();
            JSONArray array = ja.optJSONArray(i);
            int subLength = array == null ? 0 : array.length();
            for (int j = 0; j < subLength; j++) {
                T subItem = transformer.transform(array.optJSONObject(j));
                if (subItem != null) {
                    itemsSub.add(subItem);
                }
            }
            if (itemsSub.size() > 0) {
                items.add(itemsSub);
            }
        }
        return items;
    }

    public static ArrayList<String> getStringArray(JSONArray ja) {
        ArrayList<String> items = new ArrayList<String>();
        int length = ja == null ? 0 : ja.length();
        for (int i = 0; i < length; i++) {
            String s = ja.optString(i);
            if (s != null) {
                items.add(s);
            }
        }
        return items;
    }

    public static int[] getIntArray(JSONArray ja) {
        int size = ja == null ? 0 : ja.length();
        int[] nums = size > 0 ? new int[size] : null;
        for (int i = 0; i < size; i++) {
            nums[i] = ja.optInt(i);
        }
        return nums;
    }
}
