package com.imfinix.look

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

object Ui {
    const val BG = 0xFF0B0E14.toInt()
    const val CARD = 0xFF161B26.toInt()
    const val ACCENT = 0xFF6C8CFF.toInt()
    const val TEXT = 0xFFF2F4F8.toInt()
    const val SUB = 0xFF9AA3B2.toInt()

    fun dp(c: Context, v: Int): Int = (v * c.resources.displayMetrics.density).toInt()

    fun root(a: Activity): LinearLayout {
        val scroll = ScrollView(a)
        scroll.setBackgroundColor(BG)
        val col = LinearLayout(a)
        col.orientation = LinearLayout.VERTICAL
        val p = dp(a, 20)
        col.setPadding(p, dp(a, 28), p, dp(a, 32))
        scroll.addView(col)
        a.setContentView(scroll)
        return col
    }

    fun header(col: LinearLayout, title: String, subtitle: String) {
        val t = TextView(col.context)
        t.text = title
        t.textSize = 30f
        t.setTextColor(TEXT)
        t.typeface = Typeface.DEFAULT_BOLD
        col.addView(t)
        val s = TextView(col.context)
        s.text = subtitle
        s.textSize = 14f
        s.setTextColor(SUB)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(col.context, 4)
        lp.bottomMargin = dp(col.context, 20)
        s.layoutParams = lp
        col.addView(s)
    }

    fun card(col: LinearLayout, emoji: String, title: String, desc: String, onClick: () -> Unit): View {
        val c = col.context
        val box = LinearLayout(c)
        box.orientation = LinearLayout.VERTICAL
        val bg = GradientDrawable()
        bg.setColor(CARD)
        bg.cornerRadius = dp(c, 18).toFloat()
        box.background = bg
        val pad = dp(c, 18)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(c, 14)
        box.layoutParams = lp

        val ic = TextView(c)
        ic.text = emoji
        ic.textSize = 26f
        box.addView(ic)

        val t = TextView(c)
        t.text = title
        t.textSize = 18f
        t.setTextColor(TEXT)
        t.typeface = Typeface.DEFAULT_BOLD
        val tlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tlp.topMargin = dp(c, 8)
        t.layoutParams = tlp
        box.addView(t)

        val d = TextView(c)
        d.text = desc
        d.textSize = 13f
        d.setTextColor(SUB)
        val dlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dlp.topMargin = dp(c, 4)
        d.layoutParams = dlp
        box.addView(d)

        box.setOnClickListener { onClick() }
        col.addView(box)
        return box
    }

    fun section(col: LinearLayout, name: String) {
        val s = TextView(col.context)
        s.text = name.uppercase()
        s.textSize = 12f
        s.setTextColor(ACCENT)
        s.typeface = Typeface.DEFAULT_BOLD
        s.letterSpacing = 0.12f
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(col.context, 10)
        lp.bottomMargin = dp(col.context, 10)
        s.layoutParams = lp
        col.addView(s)
    }
}
