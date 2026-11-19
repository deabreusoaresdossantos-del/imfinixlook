package com.imfinix.look

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.Calendar

object DailyWallpaper {

    private fun intent(c: Context): PendingIntent {
        val i = Intent(c, DailyWallpaperReceiver::class.java)
        return PendingIntent.getBroadcast(c, 909, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun schedule(c: Context, on: Boolean) {
        val am = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!on) {
            am.cancel(intent(c))
            return
        }
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 7)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        if (cal.timeInMillis < System.currentTimeMillis()) cal.add(Calendar.DAY_OF_YEAR, 1)
        am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, intent(c))
    }
}

class DailyWallpaperReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val prefs = c.getSharedPreferences("imfinix_wall", Context.MODE_PRIVATE)
        if (i.action == Intent.ACTION_BOOT_COMPLETED) {
            if (prefs.getBoolean("daily", false)) DailyWallpaper.schedule(c, true)
            return
        }
        if (!prefs.getBoolean("daily", false)) return
        try {
            val style = prefs.getString("style", "aurora") ?: "aurora"
            if (style != "foto" || WallpaperEngine.hasPhoto(c)) {
                val seed = System.currentTimeMillis() / 86400000L
                val bmp = WallpaperEngine.generate(c, style, seed)
                WallpaperManager.getInstance(c).setBitmap(bmp, null, true, WallpaperManager.FLAG_SYSTEM)
                prefs.edit().putLong("seed", seed).apply()
            }
        } catch (_: Exception) {
        }
        DailyWallpaper.schedule(c, true)
    }
}
