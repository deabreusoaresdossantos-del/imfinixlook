package com.imfinix.look

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
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
        goForeground()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        show()
    }

    private fun goForeground() {
        val channelId = "edge_handle"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(channelId) == null) {
            val ch = NotificationChannel(channelId, "Alça de Borda", NotificationManager.IMPORTANCE_MIN)
            ch.description = "Mantém a alça de borda flutuante ativa"
            ch.setShowBadge(false)
            nm.createNotificationChannel(ch)
        }
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, GesturesActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val n: Notification = Notification.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Alça de Borda ativa")
            .setContentText("Toque para configurar ou desativar")
            .setContentIntent(open)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, n)
        }
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
        var downX = 0f
        var startY = 0
        var moved = false
        var gestureDone = false
        v.setOnTouchListener { view, ev ->
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = ev.rawY
                    downX = ev.rawX
                    startY = params.y
                    moved = false
                    gestureDone = false
                    view.animate().alpha(1f).setDuration(120).start()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dy = ev.rawY - downY
                    val dx = ev.rawX - downX
                    if (Math.abs(dy) > 12) moved = true
                    // Gestos com acessibilidade (se ativa): arrastar pra esquerda = voltar
                    if (!gestureDone && dx < -Ui.dp(this, 56) && Math.abs(dx) > Math.abs(dy) * 1.4f) {
                        if (ImfinixAccessibilityService.perform(1)) { // GLOBAL_ACTION_BACK
                            gestureDone = true
                            moved = true
                            Ui.toast(this, "⬅️ Voltar")
                        }
                    } else if (!gestureDone) {
                        params.y = (startY + dy).toInt()
                        try {
                            wm.updateViewLayout(view, params)
                        } catch (_: Exception) {
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    view.animate().alpha(0.65f).setDuration(200).start()
                    if (!moved && !gestureDone) {
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
            stopSelf()
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
        private const val NOTIF_ID = 41

        fun start(c: android.content.Context) {
            val i = Intent(c, EdgeHandleService::class.java)
            c.startForegroundService(i)
        }

        fun stop(c: android.content.Context) {
            c.stopService(Intent(c, EdgeHandleService::class.java))
        }
    }
}
