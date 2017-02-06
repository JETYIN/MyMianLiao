package com.tjut.mianliao.data;

import java.util.ArrayList;

import org.json.JSONObject;

import android.text.TextUtils;

import com.tjut.mianliao.util.JsonUtil;

public class Option {

    public int id;
    public int parentId;
    public String desc;
    public String fullDesc;

    public ArrayList<Option> subOptions;

    public static Option fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        Option opt = new Option();
        opt.id = json.isNull("id") ? json.optInt("code") : json.optInt("id");
        opt.desc = json.optString("name");
        opt.parentId = json.optInt("parent_id");
        opt.fullDesc = json.optString("fullname");

        return opt;
    }

    public static final JsonUtil.ITransformer<Option> TRANSFORMER =
            new JsonUtil.ITransformer<Option>() {
        @Override
        public Option transform(JSONObject json) {
            return fromJson(json);
        }
    };

    public Option copy() {
        Option opt = new Option();
        opt.id = id;
        opt.parentId = parentId;
        opt.desc = desc;
        opt.fullDesc = desc;
        return opt;
    }

    public void addSubOption(Option opt) {
        if (subOptions == null) {
            subOptions = new ArrayList<Option>();
        }
        opt.parentId = id;
        subOptions.add(opt);
    }

    public Option getSubOption(int index) {
        return subOptions != null && index >= 0 && index < subOptions.size()
                ? subOptions.get(index) : null;
    }

    public boolean hasParent() {
        return parentId > 0;
    }

    public boolean hasSubOptions() {
        return subOptions != null && !subOptions.isEmpty();
    }

    public String getFullDesc() {
        return TextUtils.isEmpty(fullDesc) ? desc : fullDesc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
