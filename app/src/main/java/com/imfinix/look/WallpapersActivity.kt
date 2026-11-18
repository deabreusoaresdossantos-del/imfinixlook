package com.imfinix.look

import android.app.Activity
import android.app.WallpaperManager
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class WallpapersActivity : Activity() {

    private var selected = 0
    private var seed = 42L
    private var preview: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        build()
    }

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Estúdio de Wallpapers", "Gradientes gerados no aparelho, na resolução exata da sua tela")

        val frame = LinearLayout(this)
        frame.orientation = LinearLayout.VERTICAL
        frame.gravity = Gravity.CENTER_HORIZONTAL
        frame.background = Ui.cardBg(this, 26f)
        frame.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14))
        val flp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        flp.bottomMargin = Ui.dp(this, 16)
        frame.layoutParams = flp

        val img = ImageView(this)
        val h = Ui.dp(this, 260)
        img.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h)
        img.scaleType = ImageView.ScaleType.CENTER_CROP
        img.clipToOutline = true
        img.background = Ui.cardBg(this, 20f)
        frame.addView(img)
        preview = img

        val label = TextView(this)
        label.textSize = 14f
        label.setTextColor(Ui.SUB)
        label.gravity = Gravity.CENTER
        val llp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llp.topMargin = Ui.dp(this, 10)
        label.layoutParams = llp
        frame.addView(label)
        col.addView(frame)
        Ui.animIn(frame, 0)

        fun refreshPreview() {
            val style = WallpaperEngine.styles[selected]
            img.setImageBitmap(WallpaperEngine.generate(this, style.id, seed))
            label.text = "${style.emoji} ${style.name} - ${style.desc}"
        }
        refreshPreview()

        Ui.section(col, "Estilos")
        val styles = WallpaperEngine.styles
        var i = 0
        while (i < styles.size) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            val rlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            rlp.bottomMargin = Ui.dp(this, 10)
            row.layoutParams = rlp
            for (j in 0..2) {
                if (i + j < styles.size) {
                    val s = styles[i + j]
                    val chip = TextView(this)
                    chip.text = "${s.emoji}\n${s.name}"
                    chip.textSize = 12f
                    chip.gravity = Gravity.CENTER
                    chip.setTextColor(Ui.TEXT)
                    val bg = Ui.cardBg(this, 16f)
                    if (i + j == selected) {
                        bg.setStroke(Ui.dp(this, 2), Ui.accent(this))
                    }
                    chip.background = bg
                    val clp = LinearLayout.LayoutParams(0, Ui.dp(this, 62), 1f)
                    if (j < 2) clp.rightMargin = Ui.dp(this, 10)
                    chip.layoutParams = clp
                    val which = i + j
                    chip.setOnClickListener {
                        selected = which
                        build()
                    }
                    row.addView(chip)
                }
            }
            col.addView(row)
            i += 3
        }

        Ui.button(col, "🎲 Gerar outra variação", primary = false) {
            seed = java.util.Random().nextLong()
            build()
        }

        Ui.section(col, "Aplicar")
        Ui.button(col, "🏠 Tela inicial") { apply(WallpaperManager.FLAG_SYSTEM) }
        Ui.button(col, "🔒 Tela de bloqueio", primary = false) { apply(WallpaperManager.FLAG_LOCK) }
        Ui.button(col, "✨ As duas telas", primary = false) { apply(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK) }
        Ui.note(col, "Os wallpapers são criados localmente pelo ImfinixLook: nada é baixado da internet. Cada estilo usa a resolução exata da sua tela.")
    }

    private fun apply(which: Int) {
        try {
            val style = WallpaperEngine.styles[selected]
            val bmp = WallpaperEngine.generate(this, style.id, seed)
            WallpaperManager.getInstance(this).setBitmap(bmp, null, true, which)
            Ui.toast(this, "Wallpaper ${style.name} aplicado ✅")
        } catch (e: Exception) {
            Ui.toast(this, "Não consegui aplicar: ${e.message}")
        }
    }
}
