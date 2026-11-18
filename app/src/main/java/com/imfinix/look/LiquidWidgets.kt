package com.imfinix.look

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.provider.MediaStore
import android.provider.Settings
import android.widget.RemoteViews

class LiquidClockProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) appWidgetManager.updateAppWidget(id, RemoteViews(context.packageName, R.layout.widget_liquid_clock))
    }
}

class LiquidWideProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) appWidgetManager.updateAppWidget(id, RemoteViews(context.packageName, R.layout.widget_liquid_wide))
    }
}

class LiquidBatteryProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val st: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = st?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = st?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else 0
        for (id in appWidgetIds) {
            val v = RemoteViews(context.packageName, R.layout.widget_liquid_battery)
            v.setTextViewText(R.id.liquid_pct, "$pct%")
            v.setProgressBar(R.id.liquid_bar, 100, pct, false)
            appWidgetManager.updateAppWidget(id, v)
        }
    }
}

class LiquidDockProvider : AppWidgetProvider() {
    private fun pi(c: Context, code: Int, i: Intent): PendingIntent {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(c, code, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            val v = RemoteViews(context.packageName, R.layout.widget_liquid_dock)
            v.setOnClickPendingIntent(R.id.dock_camera, pi(context, 1, Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)))
            v.setOnClickPendingIntent(R.id.dock_settings, pi(context, 2, Intent(Settings.ACTION_SETTINGS)))
            v.setOnClickPendingIntent(R.id.dock_phone, pi(context, 3, Intent(Intent.ACTION_DIAL)))
            v.setOnClickPendingIntent(R.id.dock_web, pi(context, 4, Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))))
            appWidgetManager.updateAppWidget(id, v)
        }
    }
}
