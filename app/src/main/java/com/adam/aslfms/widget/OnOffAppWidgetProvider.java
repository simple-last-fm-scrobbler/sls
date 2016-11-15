/**
 * This file is part of Simple Last.fm Scrobbler.
 * <p>
 * https://github.com/tgwizard/sls
 * <p>
 * Copyright 2011 Simple Last.fm Scrobbler Team
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context,
                                AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "updateAppWidget appWidgetId=" + appWidgetId);

        // Construct the RemoteViews object. It takes the package name (in our
        // case, it's our
        // package, but it needs this because on the other side it's the widget
        // host inflating
        // the layout from our package).
        Log.d(TAG, "Package: " + context.getPackageName());
        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.onoff_appwidget);
        views.setTextViewText(R.id.widget_text, "Adam");

        // Tell the widget manager
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
