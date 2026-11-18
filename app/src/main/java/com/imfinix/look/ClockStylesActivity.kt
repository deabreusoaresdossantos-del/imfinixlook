package com.imfinix.look

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockStylesActivity : Activity() {

    private val timeFmt by lazy { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    private val timeSecFmt by lazy { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    private val dateFmt by lazy { SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR")) }
    private val dateShortFmt by lazy { SimpleDateFormat("EEE, d MMM", Locale("pt", "BR")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Estilos de Relógio", "Três faces para a sua tela inicial. Toque em um estilo para fixar.")

        val now = Date()
        styleCard(col, "Gigante", timeFmt.format(now), dateFmt.format(now), 64f, Ui.TEXT, Typeface.DEFAULT_BOLD, ClockWidgetProvider::class.java)
        styleCard(col, "Minimal", timeSecFmt.format(now), dateShortFmt.format(now), 40f, Ui.TEXT, Typeface.DEFAULT, ClockWidgetThinProvider::class.java)
        styleCard(col, "Neon", timeFmt.format(now), dateFmt.format(now), 52f, Ui.ACCENT, Typeface.DEFAULT_BOLD, ClockWidgetAccentProvider::class.java)

        Ui.section(col, "Dica")
        Ui.card(col, "🧩", "Também dá pela home", "Segure um espaço vazio na tela inicial > Widgets > ImfinixLook e arraste o estilo que quiser.") { }
    }

    private fun styleCard(col: LinearLayout, name: String, time: String, date: String, size: Float, color: Int, face: Typeface, provider: Class<*>) {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        val bg = GradientDrawable()
        bg.setColor(Ui.CARD)
        bg.cornerRadius = Ui.dp(this, 18).toFloat()
        box.background = bg
        val pad = Ui.dp(this, 22)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = Ui.dp(this, 14)
        box.layoutParams = lp

        val t = TextView(this)
        t.text = time
        t.textSize = size
        t.typeface = face
        t.setTextColor(color)
        box.addView(t)
        val d = TextView(this)
        d.text = date
        d.textSize = 14f
        d.setTextColor(Ui.SUB)
        box.addView(d)
        val n = TextView(this)
        n.text = name.uppercase() + "  -  TOQUE PARA FIXAR"
        n.textSize = 11f
        n.setTextColor(Ui.ACCENT)
        n.typeface = Typeface.DEFAULT_BOLD
        n.letterSpacing = 0.12f
        val nlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        nlp.topMargin = Ui.dp(this, 12)
        n.layoutParams = nlp
        box.addView(n)

        box.setOnClickListener { pinWidget(provider) }
        col.addView(box)
    }

    private fun pinWidget(provider: Class<*>) {
        try {
            val mgr = AppWidgetManager.getInstance(this)
            if (mgr.isRequestPinAppWidgetSupported) {
                mgr.requestPinAppWidget(ComponentName(this, provider), null, null)
            } else {
                Toast.makeText(this, "Segure a tela inicial > Widgets > ImfinixLook", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Segure a tela inicial > Widgets > ImfinixLook", Toast.LENGTH_LONG).show()
        }
    }
}
