package com.tjut.mianliao.data.notice;

import android.content.Context;
import android.content.Intent;

import com.tjut.mianliao.contact.NewContactActivity;
import com.tjut.mianliao.job.OfferActivity;
import com.tjut.mianliao.notice.NoticeSummaryActivity;
import com.tjut.mianliao.notice.NoticeSysActivity;

public class NoticeSummary {

    public static final String INTENT_EXTRA_NAME = "NoticeSummary";
    public static final String SUBZONE = "subzone";
    public static final String COUNT = "count";

    public static final int SUBZONE_SYS = 1;
    public static final int SUBZONE_NEWS = 2;
    public static final int SUBZONE_FORUM = 3;
    public static final int SUBZONE_JOBS = 4;
    public static final int SUBZONE_QA = 5;
    public static final int SUBZONE_NF = 999;  // New Friends

    public int nameRes;
    public int iconRes;
    public int subzone;
    public int count;

    public static Intent getIntent(Context context, int subzone) {
        Intent intent = new Intent().putExtra(NoticeSummary.SUBZONE, subzone);
        switch (subzone) {
            case NoticeSummary.SUBZONE_SYS:
                intent.setClass(context, NoticeSysActivity.class);
                break;

            case NoticeSummary.SUBZONE_NEWS:
                break;

            case NoticeSummary.SUBZONE_FORUM:
                break;

            case NoticeSummary.SUBZONE_JOBS:
                intent.setClass(context, OfferActivity.class);
                break;

            case NoticeSummary.SUBZONE_QA:
                break;

            case NoticeSummary.SUBZONE_NF:
                intent.setClass(context, NewContactActivity.class);
                break;

            default:
                intent.setClass(context, NoticeSummaryActivity.class);
                break;
        }
        return intent;
    }

    public NoticeSummary(int subzone, int nameRes, int iconRes) {
        this.subzone = subzone;
        this.nameRes = nameRes;
        this.iconRes = iconRes;
    }
}
