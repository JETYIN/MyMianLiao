package com.tjut.mianliao.news.wicket;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class WicketRecord {

    private static final String PAR_TICKET = "ticket";
    private static final String PAR_NEWS_ID = "broadcast_id";
    private static final String PAR_NEWS_TITLE = "broadcast_title";
    private static final String PAR_CHECKED_ON = "checked_on";
    private static final String PAR_URL = "url";

    public String ticket;
    public int newsId;
    public String newsTitle;
    public String url;
    /**
     * This records server timestamp, with Second as basic unit.
     */
    public long checkedOn;

    public static WicketRecord fromJsonString(String js) {
        if (TextUtils.isEmpty(js)) {
            return null;
        }
        try {
            JSONObject json = new JSONObject(js);
            return fromJson(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WicketRecord fromJson(JSONObject json) {
        WicketRecord record = new WicketRecord();
        record.ticket = json.optString(PAR_TICKET);
        record.newsId = json.optInt(PAR_NEWS_ID);
        record.newsTitle = json.optString(PAR_NEWS_TITLE);
        record.checkedOn = json.optLong(PAR_CHECKED_ON);
        record.url = json.optString(PAR_URL);
        return record;
    }

    public String toJsonString() {
        JSONObject json = new JSONObject();
        try {
            json.put(PAR_TICKET, ticket);
            json.put(PAR_NEWS_ID, newsId);
            json.put(PAR_NEWS_TITLE, newsTitle);
            json.put(PAR_CHECKED_ON, checkedOn);
            json.put(PAR_URL, url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }
}
