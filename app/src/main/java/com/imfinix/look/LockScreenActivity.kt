package com.imfinix.look

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class LockScreenActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Tela de Bloqueio", "Estilize sua lockscreen dentro do que o Android permite sem root")

        Ui.section(col, "Aplicar")
        Ui.card(col, "🌌", "Wallpaper só na bloqueio", "Aplica um gradiente escuro exclusivo na tela de bloqueio") { applyLockWallpaper() }
        Ui.card(col, "🔒", "Configurações de bloqueio", "Tipo de bloqueio, biometria e notificações na lockscreen") {
            openSettings(Settings.ACTION_SECURITY_SETTINGS)
        }
        Ui.card(col, "🔔", "Notificações na lockscreen", "Escolha o que aparece com a tela bloqueada") {
            openSettings("android.settings.LOCK_SCREEN_SETTINGS")
        }

        Ui.section(col, "Limites sem root")
        Ui.card(col, "ℹ️", "Relógio e layout da lockscreen", "Trocar a posição e o estilo do relógio da lockscreen exige root ou Shizuku. No XOS, parte disso fica no app Temas.") { }
    }

    private fun applyLockWallpaper() {
        try {
            val dm = resources.displayMetrics
            val w = dm.widthPixels; val h = dm.heightPixels
            val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val cv = Canvas(b)
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.shader = LinearGradient(0f, 0f, 0f, h.toFloat(), 0xFF05070C.toInt(), 0xFF0E1526.toInt(), Shader.TileMode.CLAMP)
            cv.drawRect(0f, 0f, w.toFloat(), h.toFloat(), p)
            val g = Paint(Paint.ANTI_ALIAS_FLAG)
            g.shader = RadialGradient(w / 2f, h * 0.25f, w * 0.75f, 0x402F4BFF, android.graphics.Color.TRANSPARENT, Shader.TileMode.CLAMP)
            cv.drawRect(0f, 0f, w.toFloat(), h.toFloat(), g)
            WallpaperManager.getInstance(this).setBitmap(b, null, true, WallpaperManager.FLAG_LOCK)
            Toast.makeText(this, "Wallpaper da tela de bloqueio aplicado", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Não foi possível aplicar neste aparelho", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openSettings(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Toast.makeText(this, "Esta tela não existe neste aparelho", Toast.LENGTH_SHORT).show()
        }
    }
}
