package com.tjut.mianliao.data.job;

import org.json.JSONObject;

public class Offer {

    public static final String OFFER_ID = "offer_id";
    public static final String STATUS = "status";
    public static final String U_TIME = "utime";
    public static final String JOB = "job";

    public int status;
    public long uTime;
    public Job job;

    public static Offer fromJson(JSONObject json) {
        if (json == null || json.optInt(OFFER_ID) == 0) {
            return null;
        }
        Offer offer = new Offer();
        offer.status = json.optInt(STATUS);
        offer.uTime = json.optLong(U_TIME) * 1000;
        offer.job = Job.fromJson(json.optJSONObject(JOB));
        offer.job.offerStatus = offer.status;
        return offer;
    }
}
