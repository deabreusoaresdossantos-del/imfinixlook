package com.imfinix.look

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import java.util.Random

object WallpaperEngine {

    data class Style(val id: String, val name: String, val emoji: String, val desc: String)

    val styles = listOf(
        Style("aurora", "Aurora", "🌌", "Gradiente frio com brilhos suaves e estrelas"),
        Style("oceano", "Oceano", "🌊", "Azuis profundos em degradê diagonal"),
        Style("pordosol", "Pôr do Sol", "🌇", "Tons quentes de fim de tarde"),
        Style("neon", "Neon", "⚡", "Preto com faixas vibrantes na diagonal"),
        Style("minimal", "Minimal", "🖤", "Quase preto, limpo e discreto"),
        Style("galaxia", "Galáxia", "✨", "Roxo profundo com campo de estrelas"),
        Style("fluxo", "Fluxo", "🌀", "Linhas curvas de campo de fluxo na cor do tema"),
        Style("topo", "Topográfico", "🗺️", "Curvas de nível como um mapa de relevo"),
        Style("mesh", "Mesh", "🫧", "Manchas de cor suaves e orgânicas"),
        Style("orbes", "Orbes de Vidro", "🔮", "Esferas de vidro líquido flutuando")
    )

    private fun screenSize(c: Context): Pair<Int, Int> {
        val m = c.resources.displayMetrics
        return Pair(maxOf(m.widthPixels, 720), maxOf(m.heightPixels, 1280))
    }

    private val cache = HashMap<String, Bitmap>()

    fun generate(c: Context, styleId: String, seed: Long = 42L): Bitmap {
        val key = "$styleId@${screenSize(c)}#$seed#${Theme.current(c).id}"
        cache[key]?.let { if (!it.isRecycled) return it }
        val bmp = render(c, styleId, seed)
        if (cache.size > 8) cache.clear()
        cache[key] = bmp
        return bmp
    }

    private fun render(c: Context, styleId: String, seed: Long): Bitmap {
        val (w, h) = screenSize(c)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rnd = Random(seed)

        fun base(colors: IntArray, angleVertical: Boolean = true) {
            paint.shader = if (angleVertical)
                LinearGradient(0f, 0f, 0f, h.toFloat(), colors, null, Shader.TileMode.CLAMP)
            else
                LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), colors, null, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
            paint.shader = null
        }

        fun glow(x: Float, y: Float, radius: Float, color: Int, alpha: Int) {
            val core = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
            paint.shader = RadialGradient(x, y, radius, core, Color.TRANSPARENT, Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
            paint.shader = null
        }

        fun stars(count: Int, maxAlpha: Int) {
            paint.shader = null
            for (i in 0 until count) {
                val a = rnd.nextInt(maxAlpha) + 20
                paint.color = Color.argb(a, 255, 255, 255)
                val r = rnd.nextFloat() * 2.2f + 0.6f
                canvas.drawCircle(rnd.nextFloat() * w, rnd.nextFloat() * h, r, paint)
            }
        }

        fun bands(colorA: Int, colorB: Int) {
            paint.shader = null
            paint.strokeWidth = h * 0.05f
            paint.color = Color.argb(90, Color.red(colorA), Color.green(colorA), Color.blue(colorA))
            canvas.drawLine(-w * 0.2f, h * 0.85f, w * 1.2f, h * 0.25f, paint)
            paint.strokeWidth = h * 0.028f
            paint.color = Color.argb(120, Color.red(colorB), Color.green(colorB), Color.blue(colorB))
            canvas.drawLine(-w * 0.2f, h * 0.95f, w * 1.2f, h * 0.35f, paint)
        }

        when (styleId) {
            "aurora" -> {
                base(intArrayOf(Color.parseColor("#0B1026"), Color.parseColor("#132A4A"), Color.parseColor("#0B1026")))
                glow(w * 0.25f, h * 0.22f, w * 0.7f, Color.parseColor("#22D3EE"), 70)
                glow(w * 0.8f, h * 0.55f, w * 0.65f, Color.parseColor("#9D6CFF"), 60)
                glow(w * 0.4f, h * 0.9f, w * 0.6f, Color.parseColor("#34D399"), 45)
                stars(140, 170)
            }
            "oceano" -> {
                base(intArrayOf(Color.parseColor("#04121F"), Color.parseColor("#0A2E4E"), Color.parseColor("#063B5C")), false)
                glow(w * 0.7f, h * 0.15f, w * 0.6f, Color.parseColor("#22D3EE"), 55)
                glow(w * 0.2f, h * 0.85f, w * 0.7f, Color.parseColor("#0E7490"), 70)
                stars(40, 90)
            }
            "pordosol" -> {
                base(intArrayOf(Color.parseColor("#1B0B2B"), Color.parseColor("#7C2D12"), Color.parseColor("#F97316")))
                glow(w * 0.5f, h * 0.95f, w * 0.8f, Color.parseColor("#FBBF24"), 90)
                glow(w * 0.85f, h * 0.2f, w * 0.5f, Color.parseColor("#F472B6"), 50)
                stars(70, 120)
            }
            "neon" -> {
                base(intArrayOf(Color.parseColor("#05060A"), Color.parseColor("#0C0F18")))
                bands(Color.parseColor("#6C8CFF"), Color.parseColor("#F472B6"))
                glow(w * 0.2f, h * 0.75f, w * 0.45f, Color.parseColor("#6C8CFF"), 60)
                glow(w * 0.85f, h * 0.3f, w * 0.45f, Color.parseColor("#F472B6"), 55)
            }
            "minimal" -> {
                base(intArrayOf(Color.parseColor("#0A0C11"), Color.parseColor("#12151C"), Color.parseColor("#0A0C11")))
                glow(w * 0.5f, h * 0.0f, w * 0.6f, Color.parseColor("#2A3242"), 60)
            }
            "fluxo" -> {
                base(intArrayOf(Color.parseColor("#05070D"), Color.parseColor("#0B1020")))
                val pal = Theme.current(c)
                glow(w * 0.8f, h * 0.2f, w * 0.7f, pal.b, 40)
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND
                val ph = rnd.nextFloat() * 6f
                for (i in 0 until 90) {
                    var x = rnd.nextFloat() * w
                    var y = rnd.nextFloat() * h
                    val path = android.graphics.Path()
                    path.moveTo(x, y)
                    for (k in 0 until 40) {
                        val ang = Math.sin((x * 0.0045f + ph).toDouble()) * 1.6 + Math.cos((y * 0.0032f - ph).toDouble()) * 1.4
                        x += (Math.cos(ang) * 22).toFloat()
                        y += (Math.sin(ang) * 22).toFloat()
                        path.lineTo(x, y)
                    }
                    val col = if (i % 2 == 0) pal.a else pal.b
                    paint.color = Color.argb(40 + rnd.nextInt(110), Color.red(col), Color.green(col), Color.blue(col))
                    paint.strokeWidth = 1.5f + rnd.nextFloat() * 4f
                    canvas.drawPath(path, paint)
                }
                paint.style = Paint.Style.FILL
            }
            "topo" -> {
                base(intArrayOf(Color.parseColor("#06080C"), Color.parseColor("#0C1118")))
                val pal = Theme.current(c)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.2f
                for (cn in 0 until 3) {
                    val cx = rnd.nextFloat() * w
                    val cy = rnd.nextFloat() * h
                    val k1 = 2 + rnd.nextInt(3)
                    val k2 = 3 + rnd.nextInt(4)
                    val p1 = rnd.nextFloat() * 6f
                    val p2 = rnd.nextFloat() * 6f
                    var r = 30f
                    var ring = 0
                    while (r < h * 0.9f) {
                        val path = android.graphics.Path()
                        for (s in 0..120) {
                            val t = s / 120f * 6.2831855f
                            val rr = r * (1f + 0.16f * Math.sin((k1 * t + p1).toDouble()).toFloat() + 0.08f * Math.sin((k2 * t + p2 + r * 0.01f).toDouble()).toFloat())
                            val px = cx + (Math.cos(t.toDouble()) * rr).toFloat()
                            val py = cy + (Math.sin(t.toDouble()) * rr).toFloat()
                            if (s == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        path.close()
                        val col = if (cn % 2 == 0) pal.a else pal.b
                        paint.color = Color.argb(if (ring % 5 == 0) 150 else 70, Color.red(col), Color.green(col), Color.blue(col))
                        canvas.drawPath(path, paint)
                        r += 26f
                        ring++
                    }
                }
                paint.style = Paint.Style.FILL
            }
            "mesh" -> {
                val pal = Theme.current(c)
                base(intArrayOf(Color.parseColor("#080A12"), Color.parseColor("#0E1220")))
                for (i in 0 until 7) {
                    val col = if (i % 2 == 0) pal.a else pal.b
                    glow(rnd.nextFloat() * w, rnd.nextFloat() * h, w * (0.55f + rnd.nextFloat() * 0.5f), col, 80 + rnd.nextInt(60))
                }
            }
            "orbes" -> {
                val pal = Theme.current(c)
                base(intArrayOf(Color.parseColor("#070913"), Color.parseColor("#101426")))
                glow(w * 0.2f, h * 0.2f, w * 0.7f, pal.a, 50)
                glow(w * 0.85f, h * 0.8f, w * 0.7f, pal.b, 50)
                for (i in 0 until 8) {
                    val r = w * (0.07f + rnd.nextFloat() * 0.17f)
                    val cx = rnd.nextFloat() * w
                    val cy = rnd.nextFloat() * h
                    val tint = if (i % 2 == 0) pal.a else pal.b
                    paint.shader = RadialGradient(cx - r * 0.3f, cy - r * 0.3f, r * 1.3f,
                        intArrayOf(Color.argb(150, 255, 255, 255), Color.argb(70, Color.red(tint), Color.green(tint), Color.blue(tint)), Color.argb(20, Color.red(tint), Color.green(tint), Color.blue(tint))),
                        floatArrayOf(0f, 0.55f, 1f), Shader.TileMode.CLAMP)
                    canvas.drawCircle(cx, cy, r, paint)
                    paint.shader = null
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2.5f
                    paint.color = Color.argb(140, 255, 255, 255)
                    canvas.drawCircle(cx, cy, r, paint)
                    paint.style = Paint.Style.FILL
                }
            }
            else -> { // galaxia
                base(intArrayOf(Color.parseColor("#0A0618"), Color.parseColor("#1B1040"), Color.parseColor("#07040F")))
                glow(w * 0.3f, h * 0.3f, w * 0.55f, Color.parseColor("#9D6CFF"), 75)
                glow(w * 0.75f, h * 0.7f, w * 0.6f, Color.parseColor("#F472B6"), 55)
                glow(w * 0.6f, h * 0.15f, w * 0.35f, Color.parseColor("#22D3EE"), 45)
                stars(220, 200)
            }
        }
        return bmp
    }
}
