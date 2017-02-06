package com.tjut.mianliao.data.bounty;

import org.json.JSONObject;

import com.tjut.mianliao.R;
import com.tjut.mianliao.util.JsonUtil;

public class BountyRating {

    public static final int GOOD = 1;
    public static final int NORMAL = 2;
    public static final int BAD = 3;

    public int id;
    public int rating;
    public long time;
    public String comment;
    public Credits userCredit;

    public static BountyRating fromJson(JSONObject json) {
        if (json == null) {
            return null;
        }

        BountyRating br = new BountyRating();
        br.id = json.optInt("id");
        br.rating = json.optInt("rating");
        br.time = json.optLong("time") * 1000;
        br.comment = json.optString("comment");
        br.userCredit = Credits.fromJson(json);
        return br;
    }

    public static int getRatingDesc(int rating) {
        switch (rating) {
            case GOOD:
                return R.string.bty_rating_good;

            case NORMAL:
                return R.string.bty_rating_normal;

            case BAD:
            default:
                return R.string.bty_rating_bad;
        }
    }

    public int getRatingImage() {
        switch (rating) {
            case GOOD:
                return R.drawable.ic_rating_good;

            case NORMAL:
                return R.drawable.ic_rating_normal;

            case BAD:
            default:
                return R.drawable.ic_rating_bad;
        }
    }

    public static final JsonUtil.ITransformer<BountyRating> TRANSFORMER =
            new JsonUtil.ITransformer<BountyRating>() {
        @Override
        public BountyRating transform(JSONObject json) {
            return fromJson(json);
        }
    };
}
