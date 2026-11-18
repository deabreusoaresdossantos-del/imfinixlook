package com.imfinix.look

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

object DepthEngine {

    const val PREFS = "imfinix_depth"

    val clockStyles = listOf(
        "Vertical gigante", "Horizontal", "Vertical fino", "Sobreposto transparente", "Arco curvo",
        "Mínimo no canto", "Editorial (data por extenso)", "Neon brilhante", "Com segundos", "Serifado elegante", "Retrô pixel"
    )

    fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun bgFile(c: Context) = File(c.filesDir, "depth_bg.png")
    fun fgFile(c: Context) = File(c.filesDir, "depth_fg.png")

    fun hasPhoto(c: Context) = bgFile(c).exists()

    fun bumpRev(c: Context) {
        val p = prefs(c)
        p.edit().putInt("rev", p.getInt("rev", 0) + 1).apply()
    }

    fun loadScaled(c: Context, uri: Uri): Bitmap? {
        val dm = c.resources.displayMetrics
        val tw = maxOf(dm.widthPixels, 720)
        val th = maxOf(dm.heightPixels, 1280)
        val bounds = BitmapFactory.Options()
        bounds.inJustDecodeBounds = true
        c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= tw && bounds.outHeight / (sample * 2) >= th) sample *= 2
        val opts = BitmapFactory.Options()
        opts.inSampleSize = sample
        val raw = c.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return null
        var orientation = 1
        try {
            c.contentResolver.openInputStream(uri)?.use {
                orientation = android.media.ExifInterface(it).getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1)
            }
        } catch (e: Exception) {
        }
        val m = Matrix()
        when (orientation) {
            3 -> m.postRotate(180f)
            6 -> m.postRotate(90f)
            8 -> m.postRotate(270f)
        }
        val rotated = if (orientation == 3 || orientation == 6 || orientation == 8)
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true) else raw
        // corta no centro para a proporção da tela e escala para o tamanho da tela
        val targetRatio = tw.toFloat() / th.toFloat()
        val ratio = rotated.width.toFloat() / rotated.height.toFloat()
        var cw = rotated.width
        var ch = rotated.height
        if (ratio > targetRatio) cw = (rotated.height * targetRatio).toInt() else ch = (rotated.width / targetRatio).toInt()
        val x = (rotated.width - cw) / 2
        val y = (rotated.height - ch) / 2
        val cropped = Bitmap.createBitmap(rotated, x, y, maxOf(cw, 1), maxOf(ch, 1))
        return Bitmap.createScaledBitmap(cropped, tw, th, true)
    }

    private fun hasContent(fg: Bitmap): Boolean {
        val small = Bitmap.createScaledBitmap(fg, 48, 48, true)
        var n = 0
        for (yy in 0 until 48) for (xx in 0 until 48) {
            if (Color.alpha(small.getPixel(xx, yy)) > 128) n++
        }
        val ratio = n / (48f * 48f)
        return ratio in 0.02f..0.97f
    }

    fun segment(src: Bitmap, onDone: (Bitmap?, String) -> Unit) {
        try {
            val opts = SubjectSegmenterOptions.Builder().enableForegroundBitmap().build()
            SubjectSegmentation.getClient(opts).process(InputImage.fromBitmap(src, 0))
                .addOnSuccessListener { r ->
                    val fg = r.foregroundBitmap
                    if (fg != null && hasContent(fg)) {
                        val full = if (fg.width != src.width || fg.height != src.height)
                            Bitmap.createScaledBitmap(fg, src.width, src.height, true) else fg
                        onDone(full, "Sujeito recortado (segmentação de objetos)")
                    } else selfie(src, onDone)
                }
                .addOnFailureListener { selfie(src, onDone) }
        } catch (e: Throwable) {
            selfie(src, onDone)
        }
    }

    private fun selfie(src: Bitmap, onDone: (Bitmap?, String) -> Unit) {
        try {
            val o = SelfieSegmenterOptions.Builder().setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build()
            Segmentation.getClient(o).process(InputImage.fromBitmap(src, 0))
                .addOnSuccessListener { m ->
                    val w = m.width
                    val h = m.height
                    val buf = m.buffer
                    val px = IntArray(w * h)
                    for (i in 0 until w * h) {
                        val conf = buf.float
                        val a = (((conf - 0.45f) / 0.3f).coerceIn(0f, 1f) * 255f).toInt()
                        px[i] = Color.argb(a, 255, 255, 255)
                    }
                    val mask = Bitmap.createBitmap(px, w, h, Bitmap.Config.ARGB_8888)
                    val scaled = Bitmap.createScaledBitmap(mask, src.width, src.height, true)
                    val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
                    val cv = Canvas(out)
                    cv.drawBitmap(src, 0f, 0f, null)
                    val p = Paint()
                    p.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
                    cv.drawBitmap(scaled, 0f, 0f, p)
                    if (hasContent(out)) onDone(out, "Pessoa recortada (segmentação de pessoas)")
                    else onDone(null, "Não encontrei um sujeito claro na foto")
                }
                .addOnFailureListener { onDone(null, "Falha na segmentação: ${it.message}") }
        } catch (e: Throwable) {
            onDone(null, "Segmentação indisponível: ${e.message}")
        }
    }

    fun save(c: Context, bg: Bitmap, fg: Bitmap?) {
        FileOutputStream(bgFile(c)).use { bg.compress(Bitmap.CompressFormat.PNG, 100, it) }
        if (fg != null) {
            FileOutputStream(fgFile(c)).use { fg.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } else {
            fgFile(c).delete()
        }
        bumpRev(c)
    }

    fun blur(b: Bitmap, amount: Int): Bitmap {
        if (amount <= 0) return b
        val f = 1 + amount
        val sw = maxOf(b.width / f, 8)
        val sh = maxOf(b.height / f, 8)
        val small = Bitmap.createScaledBitmap(b, sw, sh, true)
        val mid = Bitmap.createScaledBitmap(small, maxOf(sw / 2, 4), maxOf(sh / 2, 4), true)
        val up = Bitmap.createScaledBitmap(mid, sw, sh, true)
        return Bitmap.createScaledBitmap(up, b.width, b.height, true)
    }

    val fontNames = listOf("Padrão do estilo", "Bebas Neue", "Anton", "Oswald", "Playfair Display", "Cormorant", "DM Serif", "Pacifico", "Dancing Script", "Caveat", "Orbitron", "Audiowide", "Rajdhani", "Pixel 8-bit", "VT323 Terminal", "Space Mono", "Monoton Neon", "Bungee", "Bungee Outline", "Righteous", "Lobster", "Abril Fatface", "Black Ops", "Fredoka", "Shrikhand", "Rubik Mono", "Major Mono", "Megrim", "Faster One", "Bangers", "Cinzel")
    private val fontCache = HashMap<Int, Typeface?>()

    @Synchronized
    fun customFont(c: Context): Typeface? {
        val i = prefs(c).getInt("font", 0)
        if (i <= 0 || i > fontNames.size - 1) return null
        return fontCache.getOrPut(i) {
            try { Typeface.createFromAsset(c.assets, "fonts/f%02d.ttf".format(i - 1)) } catch (e: Throwable) { null }
        }
    }

    private fun tface(c: Context, family: String, style: Int): Typeface = customFont(c) ?: Typeface.create(family, style)

    fun clockColor(c: Context): Int = prefs(c).getInt("ccolor", Color.WHITE)

    fun clockColor2(c: Context): Int = prefs(c).getInt("ccolor2", Color.parseColor("#F472B6"))

    private fun clockPaint(c: Context, alphaPct: Int, y0: Float, y1: Float, tf: Typeface): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = clockColor(c)
        val mode = prefs(c).getInt("cmode", 0)
        if (mode == 1) {
            val t = Theme.current(c)
            p.shader = LinearGradient(0f, y0, 0f, maxOf(y1, y0 + 1f), t.a, t.b, Shader.TileMode.CLAMP)
        } else if (mode == 2) {
            p.shader = LinearGradient(0f, y0, 0f, maxOf(y1, y0 + 1f), clockColor(c), clockColor2(c), Shader.TileMode.CLAMP)
        }
        p.alpha = alphaPct * 255 / 100
        p.textAlign = Paint.Align.CENTER
        p.typeface = tf
        return p
    }

    private fun fit(p: Paint, text: String, targetW: Float) {
        p.textSize = 100f
        p.textSize = 100f * targetW / p.measureText(text)
    }

    private fun stacked(c: Context, cv: Canvas, w: Int, top: Float, targetW: Float, tf: Typeface, hh: String, mm: String, alpha: Int, overlap: Float, now: Calendar) {
        val p = clockPaint(c, alpha, top, top + targetW * 1.6f, tf)
        fit(p, "00", targetW)
        val fm = p.fontMetrics
        val lineH = (fm.descent - fm.ascent) * 0.80f
        val y1 = top - fm.ascent * 0.85f
        cv.drawText(hh, w / 2f, y1, p)
        if (overlap < 1f) p.alpha = p.alpha * 6 / 10
        cv.drawText(mm, w / 2f, y1 + lineH * overlap, p)
        p.alpha = alpha * 255 / 100
        drawDate(c, cv, w, y1 + lineH * overlap + p.textSize * 0.22f, now, p)
    }

    private fun row(c: Context, cv: Canvas, w: Int, top: Float, targetW: Float, tf: Typeface, text: String, alpha: Int, now: Calendar, glow: Boolean, retro: Boolean): Float {
        val p = clockPaint(c, alpha, top, top + targetW * 0.4f, tf)
        fit(p, text, targetW)
        val fm = p.fontMetrics
        val y = top - fm.ascent
        if (retro) {
            val sh = Paint(p)
            sh.shader = null
            sh.color = Color.argb(160, 0, 0, 0)
            cv.drawText(text, w / 2f + p.textSize * 0.05f, y + p.textSize * 0.05f, sh)
        }
        if (glow) p.setShadowLayer(p.textSize * 0.35f, 0f, 0f, clockColor(c))
        cv.drawText(text, w / 2f, y, p)
        p.clearShadowLayer()
        drawDate(c, cv, w, y + p.textSize * 0.28f, now, p)
        return y
    }

    fun drawClock(c: Context, cv: Canvas, w: Int, h: Int, now: Calendar) {
        val p0 = prefs(c)
        val style = p0.getInt("style", 0)
        val sizePct = p0.getInt("size", 70).coerceIn(30, 100)
        val yPct = p0.getInt("ypos", 12).coerceIn(0, 80)
        val alpha = p0.getInt("alpha", 100).coerceIn(20, 100)
        val top = h * yPct / 100f
        val targetW = w * sizePct / 100f
        val hh = String.format("%02d", now.get(Calendar.HOUR_OF_DAY))
        val mm = String.format("%02d", now.get(Calendar.MINUTE))
        val ss = String.format("%02d", now.get(Calendar.SECOND))
        when (style) {
            1 -> row(c, cv, w, top, targetW, tface(c, "sans-serif", Typeface.BOLD), "$hh:$mm", alpha, now, false, false)
            2 -> stacked(c, cv, w, top, targetW, tface(c, "sans-serif-thin", Typeface.NORMAL), hh, mm, alpha, 1f, now)
            3 -> stacked(c, cv, w, top, targetW, tface(c, "sans-serif-black", Typeface.NORMAL), hh, mm, alpha, 0.55f, now)
            4 -> {
                val r = targetW * 0.55f
                val cx = w / 2f
                val cy = top + r
                val path = Path()
                path.addArc(RectF(cx - r, cy - r, cx + r, cy + r), 200f, 140f)
                val len = PathMeasure(path, false).length
                val p = clockPaint(c, alpha, top, top + r, tface(c, "sans-serif", Typeface.BOLD))
                fit(p, "$hh:$mm", len * 0.78f)
                cv.drawTextOnPath("$hh:$mm", path, len / 2f - p.measureText("$hh:$mm") / 2f, 0f, p)
                drawDate(c, cv, w, cy - r * 0.2f, now, p)
            }
            5 -> {
                val p = clockPaint(c, alpha, top, top + w * 0.2f, tface(c, "sans-serif-light", Typeface.NORMAL))
                p.textAlign = Paint.Align.LEFT
                p.textSize = w * 0.15f * (sizePct / 70f)
                val x = w * 0.08f
                val y = top - p.fontMetrics.ascent
                cv.drawText("$hh:$mm", x, y, p)
                val d = Paint(p)
                d.shader = null
                d.typeface = tface(c, "sans-serif-medium", Typeface.NORMAL)
                d.textSize = p.textSize * 0.32f
                cv.drawText(dateFull(now), x, y + d.textSize * 1.6f, d)
            }
            6 -> {
                val dias = arrayOf("domingo", "segunda-feira", "terça-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sábado")
                val p = clockPaint(c, alpha, top, top + targetW * 0.6f, tface(c, "serif", Typeface.NORMAL))
                val wd = dias[now.get(Calendar.DAY_OF_WEEK) - 1]
                fit(p, wd, targetW)
                val y1 = top - p.fontMetrics.ascent
                cv.drawText(wd, w / 2f, y1, p)
                val d = Paint(p)
                d.shader = null
                d.typeface = tface(c, "sans-serif-medium", Typeface.NORMAL)
                d.textSize = p.textSize * 0.38f
                cv.drawText(dateFull(now), w / 2f, y1 + d.textSize * 1.5f, d)
                val t = clockPaint(c, alpha, top, top + targetW, tface(c, "sans-serif-black", Typeface.NORMAL))
                fit(t, "$hh:$mm", targetW * 0.62f)
                cv.drawText("$hh:$mm", w / 2f, y1 + d.textSize * 1.5f + t.textSize * 1.25f, t)
            }
            7 -> row(c, cv, w, top, targetW, tface(c, "sans-serif-medium", Typeface.BOLD), "$hh:$mm", alpha, now, true, false)
            8 -> row(c, cv, w, top, targetW, tface(c, "sans-serif-condensed", Typeface.BOLD), "$hh:$mm:$ss", alpha, now, false, false)
            9 -> stacked(c, cv, w, top, targetW, tface(c, "serif", Typeface.BOLD_ITALIC), hh, mm, alpha, 1f, now)
            10 -> row(c, cv, w, top, targetW, tface(c, "monospace", Typeface.BOLD), "$hh:$mm", alpha, now, false, true)
            else -> stacked(c, cv, w, top, targetW, tface(c, "sans-serif-black", Typeface.NORMAL), hh, mm, alpha, 1f, now)
        }
    }

    private fun dateFull(now: Calendar): String {
        val meses = arrayOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro")
        return "${now.get(Calendar.DAY_OF_MONTH)} de ${meses[now.get(Calendar.MONTH)]}"
    }

    private fun drawDate(c: Context, cv: Canvas, w: Int, y: Float, now: Calendar, base: Paint) {
        val dias = arrayOf("dom", "seg", "ter", "qua", "qui", "sex", "sáb")
        val meses = arrayOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
        val txt = "${dias[now.get(Calendar.DAY_OF_WEEK) - 1]}, ${now.get(Calendar.DAY_OF_MONTH)} ${meses[now.get(Calendar.MONTH)]}"
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = base.color
        p.alpha = base.alpha
        p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        p.textSize = w * 0.045f
        cv.drawText(txt, w / 2f, y + p.textSize, p)
    }

    /** Monta a cena em camadas: fundo, escurecimento, relógio, sujeito por cima. */
    fun drawScene(c: Context, cv: Canvas, w: Int, h: Int, bg: Bitmap?, fg: Bitmap?, now: Calendar) {
        val p = prefs(c)
        val dst = Rect(0, 0, w, h)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        if (bg == null) {
            cv.drawColor(Color.BLACK)
        } else {
            cv.drawBitmap(bg, null, dst, paint)
        }
        val dim = p.getInt("dim", 0)
        if (dim > 0) cv.drawColor(Color.argb(dim * 255 / 100, 0, 0, 0))
        drawClock(c, cv, w, h, now)
        if (fg != null && p.getBoolean("depth", true)) cv.drawBitmap(fg, null, dst, paint)
    }
}
