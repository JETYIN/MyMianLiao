package com.tjut.mianliao.data.job;

import java.util.ArrayList;

import org.json.JSONObject;

import com.tjut.mianliao.data.Property;
import com.tjut.mianliao.util.JsonUtil;

public class Corp {

    public static final String INTRO = "intro";
    public static final String JOBS = "jobs";
    public static final String PROPERTIES = "properties";

    public String intro;
    public ArrayList<Job> jobs;
    public ArrayList<Property> properties;

    public static Corp fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        Corp corp = new Corp();
        corp.intro = json.optString(INTRO);
        corp.jobs = JsonUtil.getArray(json.optJSONArray(JOBS), Job.TRANSFORMER);
        corp.properties = JsonUtil.getArray(json.optJSONArray(PROPERTIES), Property.TRANSFORMER);
        return corp;
    }
}
