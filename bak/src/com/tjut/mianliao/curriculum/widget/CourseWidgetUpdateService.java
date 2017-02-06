package com.tjut.mianliao.curriculum.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;

public class CourseWidgetUpdateService extends IntentService {
    private static final String TAG = "CourseWidgetUpdateService";

    public CourseWidgetUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager awm = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = awm.getAppWidgetIds(new ComponentName(this,
                CourseWidgetProvider.class));
        int action = intent.getIntExtra(CourseWidgetHelper.EXTRA_ACTION, 0);

        CourseWidgetHelper cwh = CourseWidgetHelper.getInstance(this);
        cwh.prepareEntries(this, action);
        cwh.updateWidgets(this, awm, appWidgetIds);
    }
}
