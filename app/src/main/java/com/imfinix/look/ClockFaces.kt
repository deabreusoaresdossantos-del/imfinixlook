package com.imfinix.look

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ClockFaceView(context: Context, private val style: String, private val big: Boolean = false) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            invalidate()
            handler.postDelayed(this, 1000)
        }
    }
    private val pt = Locale("pt", "BR")
    private val timeFmt = SimpleDateFormat("HH:mm", pt)
    private val dateFmt = SimpleDateFormat("EEEE, d 'de' MMMM", pt)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(ticker)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val now = Date()
        val time = timeFmt.format(now)
        val date = dateFmt.format(now).replaceFirstChar { it.uppercase(pt) }
        val accent = Ui.accent(context)
        val accent2 = Ui.accent2(context)
        val scale = if (big) 1f else 0.5f

        when (style) {
            "giant" -> {
                paint.textAlign = Paint.Align.LEFT
                paint.color = Ui.TEXT
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textSize = 110f * scale
                canvas.drawText(time, 8f, h * 0.55f, paint)
                paint.textSize = 34f * scale
                paint.typeface = Typeface.DEFAULT
                paint.color = Ui.SUB
                canvas.drawText(date, 8f, h * 0.72f, paint)
            }
            "minimal" -> {
                paint.textAlign = Paint.Align.CENTER
                paint.color = Ui.TEXT
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 92f * scale
                canvas.drawText(time, w / 2, h * 0.55f, paint)
                paint.textSize = 30f * scale
                paint.color = Ui.SUB
                canvas.drawText(date, w / 2, h * 0.7f, paint)
            }
            "neon" -> {
                paint.textAlign = Paint.Align.LEFT
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textSize = 104f * scale
                paint.color = accent
                paint.setShadowLayer(28f * scale, 0f, 0f, accent2)
                canvas.drawText(time, 8f, h * 0.55f, paint)
                paint.clearShadowLayer()
                paint.textSize = 32f * scale
                paint.color = Ui.TEXT
                canvas.drawText(date, 8f, h * 0.72f, paint)
            }
            "digital" -> {
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textSize = 100f * scale
                paint.color = Ui.TEXT
                canvas.drawText(time, w / 2, h * 0.52f, paint)
                paint.textSize = 34f * scale
                paint.color = accent2
                canvas.drawText(date, w / 2, h * 0.7f, paint)
            }
            else -> { // analog
                val cx = w / 2
                val cy = h / 2
                val r = minOf(w, h) * 0.42f
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 5f * scale
                paint.color = Ui.STROKE
                canvas.drawCircle(cx, cy, r, paint)
                paint.style = Paint.Style.FILL
                for (i in 0 until 12) {
                    val ang = Math.toRadians((i * 30).toDouble())
                    val dotR = if (i % 3 == 0) 6f * scale else 3f * scale
                    paint.color = if (i % 3 == 0) accent else Ui.SUB
                    canvas.drawCircle(cx + (r - 14f * scale) * Math.sin(ang).toFloat(), cy - (r - 14f * scale) * Math.cos(ang).toFloat(), dotR, paint)
                }
                val cal = java.util.Calendar.getInstance()
                val hourAng = Math.toRadians(((cal.get(java.util.Calendar.HOUR) % 12) * 30 + cal.get(java.util.Calendar.MINUTE) / 2).toDouble())
                val minAng = Math.toRadians((cal.get(java.util.Calendar.MINUTE) * 6).toDouble())
                val secAng = Math.toRadians((cal.get(java.util.Calendar.SECOND) * 6).toDouble())
                paint.strokeCap = Paint.Cap.ROUND
                paint.strokeWidth = 10f * scale
                paint.color = Ui.TEXT
                canvas.drawLine(cx, cy, cx + r * 0.5f * Math.sin(hourAng).toFloat(), cy - r * 0.5f * Math.cos(hourAng).toFloat(), paint)
                paint.strokeWidth = 6f * scale
                canvas.drawLine(cx, cy, cx + r * 0.75f * Math.sin(minAng).toFloat(), cy - r * 0.75f * Math.cos(minAng).toFloat(), paint)
                paint.strokeWidth = 3f * scale
                paint.color = accent
                canvas.drawLine(cx, cy, cx + r * 0.85f * Math.sin(secAng).toFloat(), cy - r * 0.85f * Math.cos(secAng).toFloat(), paint)
                paint.style = Paint.Style.FILL
                canvas.drawCircle(cx, cy, 7f * scale, paint)
            }
        }
    }
}
