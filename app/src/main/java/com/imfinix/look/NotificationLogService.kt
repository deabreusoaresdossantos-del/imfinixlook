package com.imfinix.look

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONArray
import org.json.JSONObject

class NotificationLogService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            val n = sbn.notification ?: return
            val title = n.extras.getCharSequence("android.title")?.toString() ?: ""
            val text = n.extras.getCharSequence("android.text")?.toString() ?: ""
            if (title.isEmpty() && text.isEmpty()) return

            val prefs = getSharedPreferences("notif_log", MODE_PRIVATE)
            val arr = JSONArray(prefs.getString("items", "[]"))
            val o = JSONObject()
            o.put("app", sbn.packageName)
            o.put("title", title)
            o.put("text", text)
            o.put("time", sbn.postTime)
            val out = JSONArray()
            out.put(o)
            val max = minOf(arr.length(), 99)
            for (i in 0 until max) out.put(arr.get(i))
            prefs.edit().putString("items", out.toString()).apply()
        } catch (_: Exception) {
        }
    }
}
