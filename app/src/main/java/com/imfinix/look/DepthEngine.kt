package com.imfinix.look

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
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

    val clockStyles = listOf("Vertical gigante", "Horizontal", "Vertical fino")
    val clockColors = listOf("Branco", "Cor do tema", "Preto")

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

    fun clockColor(c: Context): Int {
        return when (prefs(c).getInt("color", 0)) {
            1 -> Theme.current(c).a
            2 -> Color.parseColor("#14161C")
            else -> Color.WHITE
        }
    }

    fun drawClock(c: Context, cv: Canvas, w: Int, h: Int, now: Calendar) {
        val p = prefs(c)
        val style = p.getInt("style", 0)
        val sizePct = p.getInt("size", 70).coerceIn(30, 100)
        val yPct = p.getInt("ypos", 12).coerceIn(0, 80)
        val alpha = p.getInt("alpha", 100).coerceIn(20, 100)
        val color = clockColor(c)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = color
        paint.alpha = (alpha * 255 / 100)
        paint.textAlign = Paint.Align.CENTER
        val hh = String.format("%02d", now.get(Calendar.HOUR_OF_DAY))
        val mm = String.format("%02d", now.get(Calendar.MINUTE))
        val top = h * yPct / 100f
        val targetW = w * sizePct / 100f
        if (style == 1) {
            paint.typeface = Typeface.create("sans-serif", Typeface.BOLD)
            paint.textSize = 100f
            val tw = paint.measureText("$hh:$mm")
            paint.textSize = 100f * targetW / tw
            val fm = paint.fontMetrics
            cv.drawText("$hh:$mm", w / 2f, top - fm.ascent, paint)
            drawDate(c, cv, w, top - fm.ascent + paint.textSize * 0.28f, now, paint)
        } else {
            paint.typeface = if (style == 2) Typeface.create("sans-serif-thin", Typeface.NORMAL)
            else Typeface.create("sans-serif-black", Typeface.NORMAL)
            paint.textSize = 100f
            val tw = paint.measureText("00")
            paint.textSize = 100f * targetW / tw
            val fm = paint.fontMetrics
            val lineH = (fm.descent - fm.ascent) * 0.80f
            val y1 = top - fm.ascent * 0.85f
            cv.drawText(hh, w / 2f, y1, paint)
            cv.drawText(mm, w / 2f, y1 + lineH, paint)
            drawDate(c, cv, w, y1 + lineH + paint.textSize * 0.22f, now, paint)
        }
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
