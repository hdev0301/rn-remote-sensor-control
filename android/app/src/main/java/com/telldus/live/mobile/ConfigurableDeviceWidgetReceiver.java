package com.telldus.live.mobile;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.telldus.live.mobile.test.ConfigurableDeviceWidget;
import com.telldus.live.mobile.test.R;

public class ConfigurableDeviceWidgetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            updateWidgetOnIconListener(context);
        }
    }

    private void updateWidgetOnIconListener(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.configurable_device_widget);
        remoteViews.setImageViewResource(R.id.iconOn, R.drawable.on_light);
        remoteViews.setImageViewResource(R.id.iconOff, R.drawable.off_dark);

//        remoteViews.setOnClickPendingIntent(R.id.iconOn, ConfigurableDeviceWidget.onButtonPendingIntent(context));
//        ConfigurableDeviceWidget.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
    }
}
