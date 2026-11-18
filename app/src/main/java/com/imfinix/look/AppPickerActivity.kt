package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class AppPickerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Abrir app", "Toque para abrir qualquer app instalado")

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = try {
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .map { it.activityInfo }
                .distinctBy { it.packageName }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }
        } catch (e: Exception) {
            emptyList()
        }

        if (apps.isEmpty()) {
            Ui.card(col, "📭", "Nenhum app encontrado", "Não consegui listar os apps instalados.") {}
            return
        }

        apps.forEachIndexed { idx, info ->
            val label = info.loadLabel(pm).toString()
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = android.view.Gravity.CENTER_VERTICAL
            row.background = Ui.cardBg(this, 14f)
            val pad = Ui.dp(this, 12)
            row.setPadding(pad, pad, pad, pad)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = Ui.dp(this, 8)
            row.layoutParams = lp

            val icon = ImageView(this)
            icon.layoutParams = LinearLayout.LayoutParams(Ui.dp(this, 38), Ui.dp(this, 38))
            try {
                icon.setImageDrawable(info.loadIcon(pm))
            } catch (_: Exception) {
            }
            row.addView(icon)

            val t = TextView(this)
            t.text = label
            t.textSize = 15f
            t.setTextColor(Ui.TEXT)
            val tlp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            tlp.leftMargin = Ui.dp(this, 12)
            t.layoutParams = tlp
            row.addView(t)

            row.setOnClickListener {
                try {
                    val launch = Intent(Intent.ACTION_MAIN)
                    launch.addCategory(Intent.CATEGORY_LAUNCHER)
                    launch.setClassName(info.packageName, info.name)
                    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(launch)
                    finish()
                } catch (e: Exception) {
                    Ui.toast(this, "Não consegui abrir: ${e.message}")
                }
            }
            col.addView(row)
            if (idx < 24) Ui.animIn(row, idx / 2)
        }
    }
}
