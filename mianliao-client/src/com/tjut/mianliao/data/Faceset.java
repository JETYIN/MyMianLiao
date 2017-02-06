package com.tjut.mianliao.data;

import org.json.JSONObject;

public class Faceset {

    public String id;
    public String tag;

    public static Faceset fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        Faceset fs = new Faceset();
        fs.id = json.optString("faceset_id");
        fs.tag = json.optString("tag");

        return fs;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Faceset) {
            Faceset other = (Faceset) o;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
