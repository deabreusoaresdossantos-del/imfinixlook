package com.imfinix.look

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

class LockScreenActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Tela de Bloqueio", "Estilo LockStar: deixe a lockscreen com a sua cara")

        Ui.section(col, "Wallpaper da lockscreen")
        Ui.note(col, "Aplique um wallpaper exclusivo só na tela de bloqueio, sem mexer na tela inicial. Escolha o estilo no Estúdio de Wallpapers.")
        Ui.button(col, "🌌 Abrir Estúdio de Wallpapers") {
            startActivity(Intent(this, WallpapersActivity::class.java))
        }
        Ui.button(col, "🔀 Usar o wallpaper atual nas duas telas", primary = false) {
            try {
                val wm = WallpaperManager.getInstance(this)
                val d = wm.drawable
                if (d != null) {
                    wm.setBitmap((d as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        ?: throw IllegalStateException("sem bitmap"), null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    Ui.toast(this, "Aplicado nas duas telas ✅")
                }
            } catch (e: Exception) {
                Ui.toast(this, "Não consegui aplicar: ${e.message}")
            }
        }

        Ui.section(col, "Ajustes de bloqueio")
        Ui.card(col, "🔔", "Notificações na lockscreen", "Escolha o que aparece quando o aparelho está bloqueado", 1) {
            startActivity(Intent("android.settings.NOTIFICATION_SETTINGS"))
        }
        Ui.card(col, "🔐", "Bloqueio de tela e biometria", "Senha, PIN, padrão, digital e rosto", 2) {
            startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
        }
        Ui.card(col, "⏲️", "Tempo para bloquear", "Controle em quanto tempo a tela apaga e o aparelho bloqueia", 3) {
            startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
        }
        Ui.card(col, "⌚", "Estilo do relógio da lockscreen", "O XOS tem estilos próprios em Configurações > Tela de bloqueio", 4) {
            try {
                startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
            } catch (e: Exception) {
                Ui.toast(this, "Abra Configurações > Tela de bloqueio no XOS")
            }
        }

        Ui.section(col, "Limite sem root")
        Ui.note(col, "Mover o relógio, trocar atalhos e editar o layout da lockscreen (como o LockStar faz na Samsung) exige root ou Shizuku, porque o XOS não expõe essas opções para apps. O ImfinixLook cobre tudo que é permitido oficialmente.")
    }
}
