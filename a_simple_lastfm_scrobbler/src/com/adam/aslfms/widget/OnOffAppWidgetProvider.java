package com.adam.aslfms.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.adam.aslfms.R;

public class OnOffAppWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "OnOffAppWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {
        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId);

        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        Log.d(TAG, "Package: " + context.getPackageName());
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.onoff_appwidget);
        views.setTextViewText(R.id.widget_text, "Adam");

        // Tell the widget manager
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
