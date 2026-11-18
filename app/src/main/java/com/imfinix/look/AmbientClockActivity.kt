package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AmbientClockActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var battery: TextView
    private val pt = Locale("pt", "BR")
    private val dateFmt = SimpleDateFormat("EEEE, d 'de' MMMM", pt)

    private val batteryTick = object : Runnable {
        override fun run() {
            val i: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            if (level >= 0) battery.text = "🔋 ${(level * 100 / scale)}%"
            handler.postDelayed(this, 30000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER
        root.setBackgroundColor(Color.BLACK)

        val clock = ClockFaceView(this, "neon", big = true)
        clock.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(this, 220)
        )
        root.addView(clock)

        val date = TextView(this)
        date.text = dateFmt.format(Date()).replaceFirstChar { it.uppercase(pt) }
        date.textSize = 16f
        date.setTextColor(Ui.SUB)
        date.gravity = Gravity.CENTER
        root.addView(date)

        battery = TextView(this)
        battery.textSize = 14f
        battery.setTextColor(Ui.accent(this))
        battery.gravity = Gravity.CENTER
        val blp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        blp.topMargin = Ui.dp(this, 18)
        battery.layoutParams = blp
        root.addView(battery)

        val hint = TextView(this)
        hint.text = "Toque em voltar para sair"
        hint.textSize = 11f
        hint.setTextColor(Color.parseColor("#3A4152"))
        hint.gravity = Gravity.CENTER
        val hlp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        hlp.topMargin = Ui.dp(this, 40)
        hint.layoutParams = hlp
        root.addView(hint)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        handler.post(batteryTick)
    }

    override fun onPause() {
        handler.removeCallbacks(batteryTick)
        super.onPause()
    }
}
