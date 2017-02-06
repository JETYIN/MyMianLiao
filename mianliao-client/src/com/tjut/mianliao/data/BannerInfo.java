package com.tjut.mianliao.data;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class BannerInfo {
    public int id, plate;
    public String image;
    public BannerData data;

    public int getId() {
        return id;
    }

    public int getPlate() {
        return plate;
    }

    public String getImage() {
        return image;
    }

    public BannerData getData() {
        return data;
    }

    public static BannerInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        BannerInfo info = new BannerInfo();
        info.id = json.optInt("id");
        info.plate = json.optInt("plate");
        info.image = json.optString("image");
        JSONObject dataJsonObj = json.optJSONObject("data");

        if (dataJsonObj != null) {
            BannerData data = new BannerData();
            data.setType(dataJsonObj.optInt("type"));
            data.setData(dataJsonObj.optString("data"));
            info.data = data;
        }

        return info;
    }

    public static final JsonUtil.ITransformer<BannerInfo> TRANSFORMER =
            new JsonUtil.ITransformer<BannerInfo>() {

        @Override
        public BannerInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public static class BannerData {
        private int type;
        private String data;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

    }

}
