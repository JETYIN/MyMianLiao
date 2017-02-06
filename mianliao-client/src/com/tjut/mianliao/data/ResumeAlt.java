package com.tjut.mianliao.data;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;
import com.tjut.mianliao.util.Utils;

public class ResumeAlt {
    private static final String TAG = "ResumeAlt";

    public static final int STATUS_UPLOADED      = 0;
    public static final int STATUS_UPLOADING     = 1;
    public static final int STATUS_UPLOAD_FAILED = 2;

    public int id;
    public int time;
    public String intro;
    public Attachment att;

    public int status;

    private ResumeAlt() {
    }

    public ResumeAlt(File file) {
        status = STATUS_UPLOADING;

        att = new Attachment();
        att.name = file.getName();
        att.size = (int) file.length();
        att.url = file.getPath();
    }

    public static ResumeAlt fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        ResumeAlt resumeAlt = new ResumeAlt();
        resumeAlt.id = json.optInt("id");
        resumeAlt.time = json.optInt("time");
        resumeAlt.intro = json.optString("intro");
        resumeAlt.att = Attachment.fromJson(json.optJSONObject("attachment"));
        resumeAlt.status = json.optInt("status", STATUS_UPLOADED);
        return resumeAlt;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("time", time);
            json.put("intro", intro);
            json.put("id", id);
            json.put("attachment", att.toJson());
            json.put("status", status);
        } catch (JSONException e) {
            Utils.logE(TAG, "Error in toJson(): " + e.getMessage());
        }

        return json;
    }

    public static final JsonUtil.ITransformer<ResumeAlt> TRANSFORMER =
            new JsonUtil.ITransformer<ResumeAlt>() {
                @Override
                public ResumeAlt transform(JSONObject json) {
                    return fromJson(json);
                }
            };

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof ResumeAlt) {
            ResumeAlt other = (ResumeAlt) o;
            return att.equals(other.att);
        }
        return false;
    }

    public void copy(ResumeAlt resume) {
        id = resume.id;
        time = resume.time;
        intro = resume.intro;
        status = resume.status;

        att.size = resume.att.size;
        att.name = resume.att.name;
        att.url = resume.att.url;
    }

    @Override
    public int hashCode() {
        return att.hashCode();
    }
}
