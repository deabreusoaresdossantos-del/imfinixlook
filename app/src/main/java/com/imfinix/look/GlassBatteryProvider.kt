package com.imfinix.look

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.RemoteViews

class GlassBatteryProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val st: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = st?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = st?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else -1
        for (id in appWidgetIds) {
            val v = RemoteViews(context.packageName, R.layout.widget_glass_battery)
            v.setTextViewText(R.id.glass_battery_pct, if (pct >= 0) "$pct%" else "--%")
            appWidgetManager.updateAppWidget(id, v)
        }
    }
}
