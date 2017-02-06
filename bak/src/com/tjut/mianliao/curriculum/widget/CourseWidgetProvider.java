package com.tjut.mianliao.curriculum.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

public class CourseWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        CourseWidgetHelper.getInstance(context).updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        CourseWidgetHelper.destroyInstance();
    }
}
