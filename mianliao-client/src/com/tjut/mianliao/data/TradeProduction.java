package com.tjut.mianliao.data;


import com.tjut.mianliao.util.JsonUtil;

import org.json.JSONObject;

/**
 * Created by YoopWu on 2016/7/20 0020.
 */
public class TradeProduction {

    public int tradeProductionId;
    public String name;
    public String icon;
    public int rmbPrice;
    public String activityDesc;
    public boolean isAvilable; // 活动关闭时，条目不可用

    public TradeProduction() {}


    public static final JsonUtil.ITransformer<TradeProduction> TRANSFORMER =
            new JsonUtil.ITransformer<TradeProduction>() {
                @Override
                public TradeProduction transform(JSONObject json) {
                    return formJson(json);
                }
            };

    public static TradeProduction formJson(JSONObject json) {
        if (json == null) {
            return null;
        }
        TradeProduction trade = new TradeProduction();
        trade.tradeProductionId = json.optInt("trade_production_id");
        trade.name = json.optString("name");
        trade.icon = json.optString("icon");
        trade.rmbPrice = json.optInt("rmb_price");
        trade.activityDesc = json.optString("activity_desc");
        trade.isAvilable = json.optBoolean("is_available");
        return trade;
    }

}
