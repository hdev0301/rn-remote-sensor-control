package com.telldus.live.mobile.test;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import org.w3c.dom.Text;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link ConfigurableDeviceWidgetConfigureActivity ConfigurableDeviceWidgetConfigureActivity}
 */
public class ConfigurableDeviceWidget extends AppWidgetProvider {

    private static final String ACTION_ON = "ACTION_ON";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.configurable_device_widget);
        Intent intent = new Intent(context, ConfigurableDeviceWidget.class);
        intent.setAction(ACTION_ON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.iconOn, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId: appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("******************************", "************************************");
        if (ACTION_ON.equals(intent.getAction())) {
            Log.d("##################################################", "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.configurable_device_widget);
            remoteViews.setImageViewResource(R.id.iconOn, R.drawable.on_light);
            ComponentName appWidget = new ComponentName(context, ConfigurableDeviceWidget.class);
            AppWidgetManager appWidgetManager  = AppWidgetManager.getInstance(context);
            appWidgetManager.updateAppWidget(appWidget, remoteViews);
        }
    }
}
