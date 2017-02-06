package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.util.AliImgSpec;

public class FaceInfo {

    public static final String MALE = "Male";
    public static final String FEMALE = "Female";

    public String id;
    public String url;
    public float similarity;

    // Attribute
    public String gender;

    public static FaceInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        FaceInfo face = new FaceInfo();
        face.id = json.optString("face_id");
        face.url = AliImgSpec.USER_AVATAR.makeUrl(json.optString("url"));
        face.similarity = (float) json.optDouble("similarity");

        JSONObject attribute = json.optJSONObject("attribute");
        if (attribute != null) {
            face.gender = attribute.optJSONObject("gender").optString("value");
        }

        return face;
    }

    public String oppositeGender() {
        return MALE.equals(gender) ? FEMALE : MALE;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof FaceInfo) {
            FaceInfo other = (FaceInfo) o;
            return id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
