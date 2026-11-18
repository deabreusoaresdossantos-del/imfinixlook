package com.imfinix.look

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class IconPackActivity : Activity() {

    companion object {
        fun liquidIcon(c: Context, d: Drawable): Bitmap {
            val s = 216
            val bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
            val cv = Canvas(bmp)
            val p = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            // cor média do ícone original tinge o vidro
            var tint = Color.parseColor("#9DB8FF")
            try {
                val probe = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
                val pc = Canvas(probe)
                d.setBounds(0, 0, 32, 32)
                d.draw(pc)
                val one = Bitmap.createScaledBitmap(probe, 1, 1, true).getPixel(0, 0)
                if (Color.alpha(one) > 30) tint = Color.rgb(Color.red(one), Color.green(one), Color.blue(one))
            } catch (e: Exception) {
            }
            val rect = RectF(10f, 10f, s - 10f, s - 10f)
            val rad = 58f
            p.shader = LinearGradient(0f, 0f, s.toFloat(), s.toFloat(),
                Color.argb(170, Color.red(tint), Color.green(tint), Color.blue(tint)),
                Color.argb(70, 255, 255, 255), Shader.TileMode.CLAMP)
            cv.drawRoundRect(rect, rad, rad, p)
            p.shader = null
            // brilho especular no topo
            val hl = Path()
            hl.addRoundRect(RectF(22f, 16f, s - 22f, s * 0.46f), 50f, 50f, Path.Direction.CW)
            p.shader = LinearGradient(0f, 16f, 0f, s * 0.46f, Color.argb(150, 255, 255, 255), Color.argb(0, 255, 255, 255), Shader.TileMode.CLAMP)
            cv.drawPath(hl, p)
            p.shader = null
            // brilho de borda inferior
            p.shader = LinearGradient(0f, s * 0.74f, 0f, s - 12f, Color.argb(0, 255, 255, 255), Color.argb(90, 255, 255, 255), Shader.TileMode.CLAMP)
            cv.drawRoundRect(RectF(26f, s * 0.74f, s - 26f, s - 14f), 36f, 36f, p)
            p.shader = null
            // borda refratada
            p.style = Paint.Style.STROKE
            p.strokeWidth = 3.5f
            p.color = Color.argb(190, 255, 255, 255)
            cv.drawRoundRect(rect, rad, rad, p)
            p.style = Paint.Style.FILL
            // ícone original no centro
            val isz = 118
            val off = (s - isz) / 2
            d.setBounds(off, off, off + isz, off + isz)
            d.draw(cv)
            return bmp
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Ícones Liquid Glass", "Atalhos de vidro líquido para a sua tela inicial")
        Ui.note(col, "O launcher do XOS não aceita icon packs de terceiros. Por isso o ImfinixLook cria atalhos fixados com ícone de vidro. Toque num app, confirme o atalho e depois esconda o ícone original (segure nele > Remover da tela inicial). Launchers como Nova, Lawnchair e Smart Launcher permitem usar esses atalhos como o seu dock.")

        Ui.section(col, "Widgets Liquid Glass")
        Ui.button(col, "🕐 Relógio Liquid (2x2)", primary = false) { pinWidget(LiquidClockProvider::class.java) }
        Ui.button(col, "📅 Relógio largo Liquid (4x2)", primary = false) { pinWidget(LiquidWideProvider::class.java) }
        Ui.button(col, "🔋 Bateria Liquid", primary = false) { pinWidget(LiquidBatteryProvider::class.java) }
        Ui.button(col, "🧩 Atalhos Liquid (câmera, ajustes, telefone, web)", primary = false) { pinWidget(LiquidDockProvider::class.java) }
        Ui.note(col, "Widgets não conseguem desfocar o que está atrás deles sem root: o efeito é vidro simulado com brilho, borda e translucidez. Ficam melhores sobre wallpaper colorido, como o Mesh ou Orbes de Vidro.")

        Ui.section(col, "Ícones de vidro")
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps: List<ActivityInfo> = try {
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
            row.gravity = Gravity.CENTER_VERTICAL
            row.background = Ui.cardBg(this, 16f)
            val pad = Ui.dp(this, 10)
            row.setPadding(pad, pad, pad, pad)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = Ui.dp(this, 8)
            row.layoutParams = lp

            val icon = ImageView(this)
            icon.layoutParams = LinearLayout.LayoutParams(Ui.dp(this, 52), Ui.dp(this, 52))
            try {
                icon.setImageBitmap(liquidIcon(this, info.loadIcon(pm)))
            } catch (e: Exception) {
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

            val pin = TextView(this)
            pin.text = "📌 Fixar"
            pin.textSize = 13f
            pin.setTextColor(Ui.accent(this))
            row.addView(pin)

            row.setOnClickListener { pinShortcut(info, label) }
            col.addView(row)
            if (idx < 20) Ui.animIn(row, idx / 2)
        }
    }

    private fun pinWidget(cls: Class<*>) {
        try {
            val mgr = android.appwidget.AppWidgetManager.getInstance(this)
            val component = android.content.ComponentName(this, cls)
            if (mgr.isRequestPinAppWidgetSupported) {
                val cbIntent = Intent(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                cbIntent.component = component
                val cb = android.app.PendingIntent.getBroadcast(this, 0, cbIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
                mgr.requestPinAppWidget(component, null, cb)
            } else {
                Ui.toast(this, "Segure a tela inicial > Widgets > ImfinixLook")
            }
        } catch (e: Exception) {
            Ui.toast(this, "Não consegui fixar: ${e.message}")
        }
    }

    private fun pinShortcut(info: ActivityInfo, label: String) {
        try {
            val sm = getSystemService(ShortcutManager::class.java)
            if (sm == null || !sm.isRequestPinShortcutSupported) {
                Ui.toast(this, "Este launcher não aceita atalhos fixados")
                return
            }
            val launch = Intent(Intent.ACTION_MAIN)
            launch.addCategory(Intent.CATEGORY_LAUNCHER)
            launch.setClassName(info.packageName, info.name)
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val bmp = liquidIcon(this, info.loadIcon(packageManager))
            val sc = ShortcutInfo.Builder(this, "liquid_${info.packageName}")
                .setShortLabel(label)
                .setIcon(Icon.createWithBitmap(bmp))
                .setIntent(launch)
                .build()
            sm.requestPinShortcut(sc, null)
        } catch (e: Exception) {
            Ui.toast(this, "Não consegui criar o atalho: ${e.message}")
        }
    }
}
