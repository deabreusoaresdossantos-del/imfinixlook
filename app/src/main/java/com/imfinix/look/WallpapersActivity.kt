package com.imfinix.look

import android.app.Activity
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

class WallpapersActivity : Activity() {

    private data class Wp(val name: String, val make: (Int, Int) -> Bitmap)

    private fun gradient(w: Int, h: Int, c1: Int, c2: Int, glow: Int?): Bitmap {
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val cv = Canvas(b)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), c1, c2, Shader.TileMode.CLAMP)
        cv.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
        if (glow != null) {
            val g = Paint(Paint.ANTI_ALIAS_FLAG)
            g.shader = RadialGradient(w / 2f, h * 0.32f, w * 0.7f, glow, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            cv.drawRect(0f, 0f, w.toFloat(), h.toFloat(), g)
        }
        return b
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val list = listOf(
            Wp("Meia-noite") { w, h -> gradient(w, h, 0xFF05070C.toInt(), 0xFF10141F.toInt(), 0x332F3BFF) },
            Wp("Azul profundo") { w, h -> gradient(w, h, 0xFF060913.toInt(), 0xFF0E1A33.toInt(), 0x402F4BFF) },
            Wp("Grafite") { w, h -> gradient(w, h, 0xFF0A0A0C.toInt(), 0xFF1A1C22.toInt(), null) },
            Wp("Aurora escura") { w, h -> gradient(w, h, 0xFF070B10.toInt(), 0xFF0D2226.toInt(), 0x3310C9B0) },
            Wp("Roxo espacial") { w, h -> gradient(w, h, 0xFF0A0712.toInt(), 0xFF1B1030.toInt(), 0x335E3BFF) },
            Wp("Preto puro") { w, h -> gradient(w, h, 0xFF000000.toInt(), 0xFF000000.toInt(), null) },
        )

        val col = Ui.root(this)
        Ui.header(col, "Wallpapers", "Gradientes escuros gerados pelo app. Toque para aplicar.")

        val sw = resources.displayMetrics.widthPixels
        val pw = (sw - Ui.dp(this, 20) * 2 - Ui.dp(this, 12)) / 2
        val ph = (pw * 16 / 9)

        var row: LinearLayout? = null
        list.forEachIndexed { i, wp ->
            if (i % 2 == 0) {
                row = LinearLayout(this)
                val rlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                rlp.bottomMargin = Ui.dp(this, 12)
                row!!.layoutParams = rlp
                col.addView(row)
            }
            val iv = ImageView(this)
            val bmp = wp.make(pw, ph)
            iv.setImageBitmap(bmp)
            iv.scaleType = ImageView.ScaleType.CENTER_CROP
            val lp = LinearLayout.LayoutParams(pw, ph)
            if (i % 2 == 0) lp.marginEnd = Ui.dp(this, 12)
            iv.layoutParams = lp
            iv.setOnClickListener { apply(wp) }
            row!!.addView(iv)
        }

        Ui.section(col, "Como aplicar")
        Ui.card(col, "👆", "Toque em um wallpaper", "Ele vira o fundo da tela inicial e de bloqueio. São gerados no aparelho, sem baixar nada.") { }
    }

    private fun apply(wp: Wp) {
        try {
            val dm = resources.displayMetrics
            val bmp = wp.make(dm.widthPixels, dm.heightPixels)
            WallpaperManager.getInstance(this).setBitmap(bmp)
            Toast.makeText(this, "Wallpaper aplicado: ${wp.name}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível aplicar neste aparelho", Toast.LENGTH_SHORT).show()
        }
    }
}
