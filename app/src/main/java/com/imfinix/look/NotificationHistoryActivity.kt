package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray

class NotificationHistoryActivity : Activity() {
    override fun onResume() {
        super.onResume()
        val col = Ui.root(this)
        Ui.header(col, "Histórico de Notificações", "Veja notificações que você dispensou sem querer")

        Ui.card(col, "🔓", "1. Liberar acesso", "Toque aqui e ative o ImfinixLook na lista de acesso a notificações") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        Ui.section(col, "Registradas")
        val prefs = getSharedPreferences("notif_log", MODE_PRIVATE)
        val arr = JSONArray(prefs.getString("items", "[]"))
        if (arr.length() == 0) {
            Ui.card(col, "📭", "Nada por aqui ainda", "Depois de liberar o acesso, as novas notificações aparecem nesta lista.") { }
        }
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val when_ = DateUtils.getRelativeTimeSpanString(o.getLong("time")).toString()
            val app = o.getString("app").substringAfterLast('.')
            addItem(col, "${o.getString("title")}", "${o.getString("text")}\n$app - $when_")
        }
    }

    private fun addItem(col: LinearLayout, title: String, sub: String) {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        val bg = android.graphics.drawable.GradientDrawable()
        bg.setColor(Ui.CARD)
        bg.cornerRadius = Ui.dp(this, 14).toFloat()
        box.background = bg
        val pad = Ui.dp(this, 14)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = Ui.dp(this, 10)
        box.layoutParams = lp
        val t = TextView(this)
        t.text = title
        t.textSize = 15f
        t.setTextColor(Ui.TEXT)
        t.typeface = android.graphics.Typeface.DEFAULT_BOLD
        box.addView(t)
        val s = TextView(this)
        s.text = sub
        s.textSize = 13f
        s.setTextColor(Ui.SUB)
        box.addView(s)
        col.addView(box)
    }
}
