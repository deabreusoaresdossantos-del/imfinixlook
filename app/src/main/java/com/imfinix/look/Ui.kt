package com.imfinix.look

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

object Ui {
    val BG = Color.parseColor("#07090F")
    val SURFACE = Color.parseColor("#10141D")
    val CARD = Color.parseColor("#151A26")
    val STROKE = Color.parseColor("#26FFFFFF")
    val TEXT = Color.parseColor("#F4F6FB")
    val SUB = Color.parseColor("#9BA4B5")
    val GOOD = Color.parseColor("#34D399")
    val WARN = Color.parseColor("#FBBF24")

    fun dp(c: Context, v: Float): Int = (v * c.resources.displayMetrics.density + 0.5f).toInt()
    fun dp(c: Context, v: Int): Int = dp(c, v.toFloat())

    fun accent(c: Context): Int = Theme.current(c).a
    fun accent2(c: Context): Int = Theme.current(c).b

    fun blend(fg: Int, bg: Int, ratio: Float): Int {
        val r = Color.red(fg) * ratio + Color.red(bg) * (1 - ratio)
        val g = Color.green(fg) * ratio + Color.green(bg) * (1 - ratio)
        val b = Color.blue(fg) * ratio + Color.blue(bg) * (1 - ratio)
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    private fun roundRect(color: Int, radiusDp: Float, c: Context): GradientDrawable {
        val g = GradientDrawable()
        g.setColor(color)
        g.cornerRadius = dp(c, radiusDp).toFloat()
        return g
    }

    private fun gradient(c: Context, radiusDp: Float): GradientDrawable {
        val g = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(accent(c), accent2(c)))
        g.cornerRadius = dp(c, radiusDp).toFloat()
        return g
    }

    fun cardBg(c: Context, radiusDp: Float = 20f): GradientDrawable {
        if (Theme.liquid(c)) {
            val lg = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(Color.argb(78, 255, 255, 255), Color.argb(20, 255, 255, 255)))
            lg.cornerRadius = dp(c, radiusDp).toFloat()
            lg.setStroke(dp(c, 1), Color.argb(130, 255, 255, 255))
            return lg
        }
        val g = roundRect(CARD, radiusDp, c)
        g.setStroke(dp(c, 1), STROKE)
        return g
    }

    private fun pressable(v: View, onClick: () -> Unit) {
        v.setOnTouchListener { view, ev ->
            when (ev.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(120).start()
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
                }
                android.view.MotionEvent.ACTION_UP ->
                    view.animate().scaleX(1f).scaleY(1f).setDuration(180).setInterpolator(OvershootInterpolator(2f)).start()
                android.view.MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
            }
            false
        }
        v.setOnClickListener { onClick() }
    }

    fun animIn(v: View, index: Int) {
        v.alpha = 0f
        v.translationY = dp(v.context, 14).toFloat()
        v.animate().alpha(1f).translationY(0f).setStartDelay((index * 45).toLong()).setDuration(280).start()
    }

    fun root(a: Activity): LinearLayout {
        a.window.statusBarColor = BG
        a.window.navigationBarColor = BG
        val scroll = ScrollView(a)
        if (Theme.liquid(a)) {
            scroll.background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(blend(accent(a), BG, 0.32f), BG, blend(accent2(a), BG, 0.26f))
            )
        } else {
            scroll.setBackgroundColor(BG)
        }
        scroll.isVerticalScrollBarEnabled = false
        val col = LinearLayout(a)
        col.orientation = LinearLayout.VERTICAL
        val p = dp(a, 20)
        col.setPadding(p, dp(a, 20), p, dp(a, 36))
        scroll.addView(col)
        a.setContentView(scroll)
        return col
    }

    fun hero(col: LinearLayout, title: String, subtitle: String, emoji: String = "✨") {
        val c = col.context
        val box = LinearLayout(c)
        box.orientation = LinearLayout.VERTICAL
        box.background = gradient(c, 26f)
        val pad = dp(c, 22)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(c, 22)
        box.layoutParams = lp
        box.elevation = dp(c, 6).toFloat()

        val e = TextView(c)
        e.text = emoji
        e.textSize = 30f
        box.addView(e)

        val t = TextView(c)
        t.text = title
        t.textSize = 26f
        t.setTextColor(Color.WHITE)
        t.typeface = Typeface.DEFAULT_BOLD
        val tlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tlp.topMargin = dp(c, 10)
        t.layoutParams = tlp
        box.addView(t)

        val s = TextView(c)
        s.text = subtitle
        s.textSize = 14f
        s.setTextColor(Color.parseColor("#E6E9F5"))
        val slp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        slp.topMargin = dp(c, 4)
        s.layoutParams = slp
        box.addView(s)

        col.addView(box)
        animIn(box, 0)
    }

    fun header(col: LinearLayout, title: String, subtitle: String) {
        val c = col.context
        val t = TextView(c)
        t.text = title
        t.textSize = 28f
        t.setTextColor(TEXT)
        t.typeface = Typeface.DEFAULT_BOLD
        col.addView(t)
        val s = TextView(c)
        s.text = subtitle
        s.textSize = 14f
        s.setTextColor(SUB)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(c, 4)
        lp.bottomMargin = dp(c, 18)
        s.layoutParams = lp
        col.addView(s)
        animIn(t, 0)
        animIn(s, 1)
    }

    fun section(col: LinearLayout, name: String) {
        val c = col.context
        val s = TextView(c)
        s.text = name.uppercase()
        s.textSize = 12f
        s.setTextColor(accent(c))
        s.typeface = Typeface.DEFAULT_BOLD
        s.letterSpacing = 0.14f
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(c, 14)
        lp.bottomMargin = dp(c, 10)
        s.layoutParams = lp
        col.addView(s)
    }

    private fun iconChip(c: Context, emoji: String): TextView {
        val chip = TextView(c)
        chip.text = emoji
        chip.textSize = 20f
        chip.gravity = Gravity.CENTER
        val bg = GradientDrawable()
        bg.setColor(blend(accent(c), CARD, 0.22f))
        bg.cornerRadius = dp(c, 14).toFloat()
        chip.background = bg
        val lp = LinearLayout.LayoutParams(dp(c, 44), dp(c, 44))
        chip.layoutParams = lp
        return chip
    }

    fun card(col: LinearLayout, emoji: String, title: String, desc: String, animIndex: Int = -1, onClick: (() -> Unit)? = null): View {
        val c = col.context
        val box = LinearLayout(c)
        box.orientation = LinearLayout.HORIZONTAL
        box.gravity = Gravity.CENTER_VERTICAL
        box.background = cardBg(c)
        val pad = dp(c, 16)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(c, 12)
        box.layoutParams = lp

        box.addView(iconChip(c, emoji))

        val texts = LinearLayout(c)
        texts.orientation = LinearLayout.VERTICAL
        val tlp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        tlp.leftMargin = dp(c, 14)
        texts.layoutParams = tlp

        val t = TextView(c)
        t.text = title
        t.textSize = 16f
        t.setTextColor(TEXT)
        t.typeface = Typeface.DEFAULT_BOLD
        texts.addView(t)

        if (desc.isNotEmpty()) {
            val d = TextView(c)
            d.text = desc
            d.textSize = 13f
            d.setTextColor(SUB)
            val dlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dlp.topMargin = dp(c, 3)
            d.layoutParams = dlp
            texts.addView(d)
        }
        box.addView(texts)

        if (onClick != null) {
            val arrow = TextView(c)
            arrow.text = "›"
            arrow.textSize = 22f
            arrow.setTextColor(SUB)
            box.addView(arrow)
            pressable(box, onClick)
        }
        col.addView(box)
        if (animIndex >= 0) animIn(box, animIndex)
        return box
    }

    fun grid(col: LinearLayout, items: List<Triple<String, String, String>>, startIndex: Int, onClick: (Int) -> Unit) {
        val c = col.context
        var idx = startIndex
        var i = 0
        while (i < items.size) {
            val row = LinearLayout(c)
            row.orientation = LinearLayout.HORIZONTAL
            val rlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            rlp.bottomMargin = dp(c, 12)
            row.layoutParams = rlp
            for (j in 0..1) {
                if (i + j < items.size) {
                    val (emoji, title, desc) = items[i + j]
                    val cell = LinearLayout(c)
                    cell.orientation = LinearLayout.VERTICAL
                    cell.background = cardBg(c, 22f)
                    val pad = dp(c, 16)
                    cell.setPadding(pad, pad, pad, pad)
                    val clp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                    if (j == 0) clp.rightMargin = dp(c, 12)
                    cell.layoutParams = clp

                    cell.addView(iconChip(c, emoji))
                    val t = TextView(c)
                    t.text = title
                    t.textSize = 15f
                    t.setTextColor(TEXT)
                    t.typeface = Typeface.DEFAULT_BOLD
                    val tlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    tlp.topMargin = dp(c, 12)
                    t.layoutParams = tlp
                    cell.addView(t)
                    if (desc.isNotEmpty()) {
                        val d = TextView(c)
                        d.text = desc
                        d.textSize = 12f
                        d.setTextColor(SUB)
                        val dlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        dlp.topMargin = dp(c, 3)
                        d.layoutParams = dlp
                        cell.addView(d)
                    }
                    val which = i + j
                    pressable(cell) { onClick(which) }
                    row.addView(cell)
                    animIn(cell, idx++)
                } else {
                    val spacer = View(c)
                    spacer.layoutParams = LinearLayout.LayoutParams(0, 1, 1f)
                    row.addView(spacer)
                }
            }
            col.addView(row)
            i += 2
        }
    }

    fun button(col: LinearLayout, text: String, primary: Boolean = true, onClick: () -> Unit) {
        val c = col.context
        val b = TextView(c)
        b.text = text
        b.textSize = 15f
        b.typeface = Typeface.DEFAULT_BOLD
        b.gravity = Gravity.CENTER
        if (primary) {
            b.background = gradient(c, 16f)
            b.setTextColor(Color.WHITE)
        } else {
            b.background = cardBg(c, 16f)
            b.setTextColor(accent(c))
        }
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(c, 50))
        lp.bottomMargin = dp(c, 12)
        b.layoutParams = lp
        pressable(b, onClick)
        col.addView(b)
    }

    fun slider(col: LinearLayout, label: String, emoji: String, max: Int, progress: Int, onChange: (Int) -> Unit): SeekBar {
        val c = col.context
        val box = LinearLayout(c)
        box.orientation = LinearLayout.VERTICAL
        box.background = cardBg(c, 18f)
        val pad = dp(c, 16)
        box.setPadding(pad, pad, pad, dp(c, 8))
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(c, 12)
        box.layoutParams = lp

        val top = LinearLayout(c)
        top.orientation = LinearLayout.HORIZONTAL
        top.gravity = Gravity.CENTER_VERTICAL
        val ic = TextView(c)
        ic.text = emoji
        ic.textSize = 16f
        top.addView(ic)
        val t = TextView(c)
        t.text = label
        t.textSize = 14f
        t.setTextColor(TEXT)
        t.typeface = Typeface.DEFAULT_BOLD
        val tlp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        tlp.leftMargin = dp(c, 10)
        t.layoutParams = tlp
        top.addView(t)
        val value = TextView(c)
        value.text = "$progress"
        value.textSize = 13f
        value.setTextColor(SUB)
        top.addView(value)
        box.addView(top)

        val sb = SeekBar(c)
        sb.max = max
        sb.progress = progress
        sb.progressTintList = ColorStateList.valueOf(accent(c))
        sb.thumbTintList = ColorStateList.valueOf(accent(c))
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, p: Int, fromUser: Boolean) {
                value.text = "$p"
                if (fromUser) onChange(p)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        box.addView(sb)
        col.addView(box)
        return sb
    }

    fun toggle(col: LinearLayout, emoji: String, title: String, desc: String, checked: Boolean, onToggle: (Boolean) -> Unit): Switch {
        val c = col.context
        val box = LinearLayout(c)
        box.orientation = LinearLayout.HORIZONTAL
        box.gravity = Gravity.CENTER_VERTICAL
        box.background = cardBg(c, 18f)
        val pad = dp(c, 16)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = dp(c, 12)
        box.layoutParams = lp

        box.addView(iconChip(c, emoji))
        val texts = LinearLayout(c)
        texts.orientation = LinearLayout.VERTICAL
        val tlp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        tlp.leftMargin = dp(c, 14)
        texts.layoutParams = tlp
        val t = TextView(c)
        t.text = title
        t.textSize = 15f
        t.setTextColor(TEXT)
        t.typeface = Typeface.DEFAULT_BOLD
        texts.addView(t)
        if (desc.isNotEmpty()) {
            val d = TextView(c)
            d.text = desc
            d.textSize = 12f
            d.setTextColor(SUB)
            texts.addView(d)
        }
        box.addView(texts)

        val sw = Switch(c)
        sw.isChecked = checked
        sw.thumbTintList = ColorStateList.valueOf(if (checked) accent(c) else SUB)
        sw.trackTintList = ColorStateList.valueOf(blend(accent(c), CARD, 0.35f))
        sw.setOnCheckedChangeListener { _, isChecked ->
            sw.thumbTintList = ColorStateList.valueOf(if (isChecked) accent(c) else SUB)
            onToggle(isChecked)
        }
        box.addView(sw)
        col.addView(box)
        return sw
    }

    fun note(col: LinearLayout, text: String) {
        val c = col.context
        val t = TextView(c)
        t.text = text
        t.textSize = 12f
        t.setTextColor(SUB)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.topMargin = dp(c, 4)
        lp.bottomMargin = dp(c, 14)
        t.layoutParams = lp
        col.addView(t)
    }

    fun chips(col: LinearLayout, labels: List<String>, selected: Int, onPick: (Int) -> Unit) {
        val c = col.context
        val row = LinearLayout(c)
        row.orientation = LinearLayout.HORIZONTAL
        val rlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rlp.bottomMargin = dp(c, 10)
        row.layoutParams = rlp
        labels.forEachIndexed { i, label ->
            val chip = TextView(c)
            chip.text = label
            chip.textSize = 13f
            chip.gravity = Gravity.CENTER
            val bg = cardBg(c, 12f)
            if (i == selected) {
                bg.setStroke(dp(c, 2), accent(c))
                chip.setTextColor(accent(c))
            } else {
                chip.setTextColor(SUB)
            }
            chip.background = bg
            val clp = LinearLayout.LayoutParams(0, dp(c, 42), 1f)
            if (i < labels.size - 1) clp.rightMargin = dp(c, 6)
            chip.layoutParams = clp
            chip.setOnClickListener { onPick(i) }
            row.addView(chip)
        }
        col.addView(row)
    }

    fun toast(c: Context, msg: String) = Toast.makeText(c, msg, Toast.LENGTH_SHORT).show()
}
