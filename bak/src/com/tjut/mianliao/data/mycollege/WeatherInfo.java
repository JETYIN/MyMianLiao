package com.tjut.mianliao.data.mycollege;

import java.io.Serializable;

import org.json.JSONObject;

import com.tjut.mianliao.util.JsonUtil;

public class WeatherInfo implements Serializable {

    public String weekDay;
    public String currentDate;
    public int weekNo;
    public String weather;
    public String temper;
    public String weatherIcon;

    public static final JsonUtil.ITransformer<WeatherInfo> TRANSFORMER =
            new JsonUtil.ITransformer<WeatherInfo>() {

        @Override
        public WeatherInfo transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public WeatherInfo() {
    }

    public static WeatherInfo fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        WeatherInfo info = new WeatherInfo();
        info.weekDay = json.optString("week_day");
        info.currentDate = json.optString("current_date");
        info.weekNo = json.optInt("week_no");
        info.weather = json.optString("weather");
        info.temper = json.optString("temperature");
        info.weatherIcon = json.optString("weather_pic");
        return info;
    }

    public static WeatherInfo copy(WeatherInfo weatherInfo) {
        WeatherInfo wi = new WeatherInfo();
        wi.weather = weatherInfo.weather;
        wi.weekDay = weatherInfo.weekDay;
        wi.weekNo = weatherInfo.weekNo;
        wi.currentDate = weatherInfo.currentDate;
        wi.temper = weatherInfo.temper;
        wi.weatherIcon = weatherInfo.weatherIcon;
        return wi;
    }

}
