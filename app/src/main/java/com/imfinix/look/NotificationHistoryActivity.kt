package com.imfinix.look

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONArray

class NotificationHistoryActivity : Activity() {

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun listenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return flat.contains(packageName)
    }

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Histórico de Notificações", "Estilo NotiStar: recupere o que você dispensou sem querer")

        if (!listenerEnabled()) {
            Ui.card(col, "🔓", "Liberar acesso às notificações", "Toque aqui e ative o ImfinixLook na lista. Sem isso, o histórico não consegue registrar nada.", 0) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            Ui.note(col, "O ImfinixLook só guarda título, texto e app de cada notificação, localmente no aparelho. Nada sai do celular.")
            return
        }

        Ui.card(col, "✅", "Registro ativo", "As novas notificações estão sendo salvas aqui automaticamente.", 0) {}

        val prefs = getSharedPreferences("notif_log", MODE_PRIVATE)
        val arr = JSONArray(prefs.getString("items", "[]"))

        Ui.section(col, "Registradas (${arr.length()})")
        if (arr.length() > 0) {
            Ui.button(col, "🧹 Limpar histórico", primary = false) {
                AlertDialog.Builder(this)
                    .setTitle("Limpar histórico")
                    .setMessage("Apagar todas as ${arr.length()} notificações registradas?")
                    .setPositiveButton("Apagar") { _, _ ->
                        prefs.edit().putString("items", "[]").apply()
                        build()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        if (arr.length() == 0) {
            Ui.card(col, "📭", "Nada por aqui ainda", "As próximas notificações que chegarem aparecem nesta lista, mesmo se você dispensar.") {}
        }

        val pm = packageManager
        for (i in 0 until arr.length()) {
            try {
                val o = arr.getJSONObject(i)
                val pkg = o.getString("app")
                val whenStr = DateUtils.getRelativeTimeSpanString(o.getLong("time")).toString()
                val appName = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                } catch (e: Exception) {
                    pkg.substringAfterLast('.')
                }
                addRow(col, pkg, appName, o.getString("title"), o.getString("text"), whenStr, i)
            } catch (_: Exception) {
            }
        }
    }

    private fun addRow(col: LinearLayout, pkg: String, appName: String, title: String, text: String, whenStr: String, index: Int) {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.HORIZONTAL
        box.gravity = Gravity.CENTER_VERTICAL
        box.background = Ui.cardBg(this, 16f)
        val pad = Ui.dp(this, 14)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = Ui.dp(this, 10)
        box.layoutParams = lp

        val icon = ImageView(this)
        icon.layoutParams = LinearLayout.LayoutParams(Ui.dp(this, 40), Ui.dp(this, 40))
        try {
            icon.setImageDrawable(packageManager.getApplicationIcon(pkg))
        } catch (_: Exception) {
        }
        box.addView(icon)

        val texts = LinearLayout(this)
        texts.orientation = LinearLayout.VERTICAL
        val tlp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        tlp.leftMargin = Ui.dp(this, 12)
        texts.layoutParams = tlp

        val head = TextView(this)
        head.text = "$appName • $whenStr"
        head.textSize = 11f
        head.setTextColor(Ui.accent(this))
        texts.addView(head)

        if (title.isNotEmpty()) {
            val t = TextView(this)
            t.text = title
            t.textSize = 15f
            t.setTextColor(Ui.TEXT)
            t.typeface = android.graphics.Typeface.DEFAULT_BOLD
            texts.addView(t)
        }
        if (text.isNotEmpty() && text != title) {
            val s = TextView(this)
            s.text = text
            s.textSize = 13f
            s.setTextColor(Ui.SUB)
            texts.addView(s)
        }
        box.addView(texts)
        col.addView(box)
        if (index < 16) Ui.animIn(box, index / 2)
    }
}
