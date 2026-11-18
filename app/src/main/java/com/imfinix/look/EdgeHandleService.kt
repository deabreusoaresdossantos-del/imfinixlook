package com.imfinix.look

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager

class EdgeHandleService : Service() {

    private var handle: View? = null
    private lateinit var wm: WindowManager

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        show()
    }

    private fun show() {
        if (handle != null) return
        val v = View(this)
        val bg = GradientDrawable()
        bg.setColor(Ui.accent(this))
        bg.cornerRadius = Ui.dp(this, 12).toFloat()
        bg.alpha = 160
        v.background = bg

        val params = WindowManager.LayoutParams(
            Ui.dp(this, 10), Ui.dp(this, 120),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.END or Gravity.CENTER_VERTICAL

        var downY = 0f
        var startY = 0
        var moved = false
        v.setOnTouchListener { view, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = ev.rawY
                    startY = params.y
                    moved = false
                    view.animate().alpha(1f).setDuration(120).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = ev.rawY - downY
                    if (Math.abs(dy) > 12) moved = true
                    params.y = (startY + dy).toInt()
                    try {
                        wm.updateViewLayout(view, params)
                    } catch (_: Exception) {
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.animate().alpha(0.65f).setDuration(200).start()
                    if (!moved) {
                        val i = Intent(this, AppPickerActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(i)
                    }
                    true
                }
                else -> false
            }
        }
        v.alpha = 0.65f

        try {
            wm.addView(v, params)
            handle = v
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        handle?.let {
            try {
                wm.removeView(it)
            } catch (_: Exception) {
            }
        }
        handle = null
        super.onDestroy()
    }

    companion object {
        fun start(c: android.content.Context) {
            c.startService(Intent(c, EdgeHandleService::class.java))
        }

        fun stop(c: android.content.Context) {
            c.stopService(Intent(c, EdgeHandleService::class.java))
        }
    }
}
