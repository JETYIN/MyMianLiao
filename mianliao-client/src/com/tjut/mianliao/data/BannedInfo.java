package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class BannedInfo {

    public String tribeName;
    public int tribeId;
    public boolean checked;

    public BannedInfo() {
    }
    
    public BannedInfo(String tribeName, int tribeId, boolean checked) {
        super();
        this.tribeName = tribeName;
        this.tribeId = tribeId;
        this.checked = checked;
    }
    
    public static final JsonUtil.ITransformer<BannedInfo> TRANSFORMER = new JsonUtil.ITransformer<BannedInfo>() {
        @Override
        public BannedInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    protected static BannedInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        BannedInfo bannedInfo = new BannedInfo();
        bannedInfo.tribeId = json.optInt("fid");
        bannedInfo.tribeName = json.optString("fname");
        return bannedInfo;
    }


}
