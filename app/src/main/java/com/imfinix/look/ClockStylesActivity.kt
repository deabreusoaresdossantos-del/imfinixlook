package com.imfinix.look

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class ClockStylesActivity : Activity() {

    private data class Face(
        val styleId: String,
        val name: String,
        val emoji: String,
        val desc: String,
        val provider: Class<*>
    )

    private val faces = listOf(
        Face("giant", "Gigante", "🕐", "Hora enorme com data completa", ClockWidgetProvider::class.java),
        Face("minimal", "Minimal", "◻️", "Fino e discreto, com segundos", ClockWidgetThinProvider::class.java),
        Face("neon", "Neon", "💜", "Hora brilhando na cor do tema", ClockWidgetAccentProvider::class.java),
        Face("digital", "Digital Data", "📅", "Hora + dia da semana em destaque", ClockWidgetDigitalProvider::class.java),
        Face("analog", "Analógico", "⏰", "Ponteiros clássicos em card escuro", ClockWidgetAnalogProvider::class.java)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Estilos de Relógio", "Estilo ClockFace: cinco faces para a sua tela inicial")

        Ui.card(col, "🛋️", "Relógio de Mesa", "Tela cheia estilo sempre ativo: hora gigante, data e bateria. Ideal com o celular carregando.", 0) {
            startActivity(Intent(this, AmbientClockActivity::class.java))
        }

        Ui.section(col, "Faces ao vivo")
        faces.forEachIndexed { idx, face ->
            addFace(col, face, idx + 1)
        }

        Ui.section(col, "Como fixar")
        Ui.note(col, "Toque em \"Fixar na tela inicial\" e confirme. Se o launcher não apoiar a fixação direta, segure a tela inicial > Widgets > ImfinixLook e arraste o relógio. Faces para a lockscreen/Always-On exigiriam root.")
    }

    private fun addFace(col: LinearLayout, face: Face, animIndex: Int) {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.background = Ui.cardBg(this, 22f)
        val pad = Ui.dp(this, 16)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = Ui.dp(this, 14)
        box.layoutParams = lp

        val top = LinearLayout(this)
        top.orientation = LinearLayout.HORIZONTAL
        top.gravity = Gravity.CENTER_VERTICAL
        val t = TextView(this)
        t.text = "${face.emoji} ${face.name}"
        t.textSize = 16f
        t.setTextColor(Ui.TEXT)
        t.typeface = android.graphics.Typeface.DEFAULT_BOLD
        val tlp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        top.addView(t)
        t.layoutParams = tlp
        val d = TextView(this)
        d.text = face.desc
        d.textSize = 12f
        d.setTextColor(Ui.SUB)
        top.addView(d)
        box.addView(top)

        val preview = ClockFaceView(this, face.styleId)
        val plp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 120))
        plp.topMargin = Ui.dp(this, 10)
        preview.layoutParams = plp
        preview.setBackgroundColor(Ui.SURFACE)
        box.addView(preview)

        val btn = TextView(this)
        btn.text = "📌 Fixar na tela inicial"
        btn.textSize = 14f
        btn.gravity = Gravity.CENTER
        btn.typeface = android.graphics.Typeface.DEFAULT_BOLD
        btn.setTextColor(Ui.accent(this))
        val bg = Ui.cardBg(this, 14f)
        bg.setStroke(Ui.dp(this, 1), Ui.accent(this))
        btn.background = bg
        val blp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 44))
        blp.topMargin = Ui.dp(this, 12)
        btn.layoutParams = blp
        btn.setOnClickListener { pin(face) }
        box.addView(btn)

        col.addView(box)
        Ui.animIn(box, animIndex)
    }

    private fun pin(face: Face) {
        try {
            val mgr = AppWidgetManager.getInstance(this)
            val component = ComponentName(this, face.provider)
            if (mgr.isRequestPinAppWidgetSupported) {
                val cb = PendingIntent.getBroadcast(
                    this, 0, Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                mgr.requestPinAppWidget(component, null, cb)
            } else {
                Ui.toast(this, "Launcher não apoia fixação direta. Segure a tela inicial > Widgets > ImfinixLook.")
            }
        } catch (e: Exception) {
            Ui.toast(this, "Não consegui fixar: ${e.message}")
        }
    }
}
