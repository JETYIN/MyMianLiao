package com.tjut.mianliao.data.explore;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class VCenterListInfo {

    public int id;
    public String name;
    public String label;

    public static final JsonUtil.ITransformer<VCenterListInfo> TRANSFORMER =
            new JsonUtil.ITransformer<VCenterListInfo>() {

        @Override
        public VCenterListInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };
    
    public static VCenterListInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        VCenterListInfo info = new VCenterListInfo();
        info.id = json.optInt("id");
        info.name = json.optString("name");
        info.label = json.optString("label");
        return info;
    }    

}
