package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class QuickSettingsActivity : Activity() {

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Ajustes Rápidos", "Estilo QuickStar: controle o aparelho sem caçar menu")

        Ui.section(col, "Controles do aparelho")
        if (!Settings.System.canWrite(this)) {
            Ui.card(col, "🔑", "Liberar controles", "Para controlar brilho, rotação e tempo de tela, permita que o ImfinixLook modifique configurações do sistema. Toque aqui e ative a chave.", 0) {
                startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
            }
            Ui.note(col, "Depois de ativar, volte para cá: os controles aparecem automaticamente.")
        } else {
            controls(col)
        }

        Ui.section(col, "Atalhos do sistema")
        val shortcuts = listOf(
            Triple("🖥️", "Tela", Settings.ACTION_DISPLAY_SETTINGS),
            Triple("🔊", "Som", Settings.ACTION_SOUND_SETTINGS),
            Triple("🔔", "Notificações", "android.settings.NOTIFICATION_SETTINGS"),
            Triple("🔋", "Bateria", Settings.ACTION_BATTERY_SAVER_SETTINGS),
            Triple("📶", "Wi-Fi", Settings.ACTION_WIFI_SETTINGS),
            Triple("🅱️", "Bluetooth", Settings.ACTION_BLUETOOTH_SETTINGS),
            Triple("📊", "Uso de dados", Settings.ACTION_DATA_USAGE_SETTINGS),
            Triple("💾", "Armazenamento", Settings.ACTION_INTERNAL_STORAGE_SETTINGS),
            Triple("🔐", "Segurança", Settings.ACTION_SECURITY_SETTINGS),
            Triple("📍", "Localização", Settings.ACTION_LOCATION_SOURCE_SETTINGS),
            Triple("📱", "Apps", Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS),
            Triple("📅", "Data e hora", Settings.ACTION_DATE_SETTINGS)
        )
        shortcuts.forEachIndexed { idx, (emoji, name, action) ->
            Ui.card(col, emoji, name, "", idx) { safeStart(action) }
        }
    }

    private fun controls(col: LinearLayout) {
        val brightness = try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            128
        }
        Ui.slider(col, "Brilho da tela", "☀️", 255, brightness) { v ->
            try {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, v)
            } catch (e: Exception) {
                Ui.toast(this, "Sem permissão: ${e.message}")
            }
        }
        Ui.note(col, "O controle de brilho desliga o brilho automático para obedecer ao valor que você escolher.")

        Ui.section(col, "Tempo para a tela apagar")
        val currentTimeout = try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        } catch (e: Exception) {
            30000
        }
        val options = listOf(15_000 to "15s", 30_000 to "30s", 60_000 to "1min", 120_000 to "2min", 300_000 to "5min", 600_000 to "10min")
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val rlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rlp.bottomMargin = Ui.dp(this, 12)
        row.layoutParams = rlp
        options.forEachIndexed { i, (ms, label) ->
            val chip = TextView(this)
            chip.text = label
            chip.textSize = 13f
            chip.gravity = Gravity.CENTER
            val bg = Ui.cardBg(this, 12f)
            if (currentTimeout == ms) {
                bg.setStroke(Ui.dp(this, 2), Ui.accent(this))
                chip.setTextColor(Ui.accent(this))
            } else {
                chip.setTextColor(Ui.SUB)
            }
            chip.background = bg
            val clp = LinearLayout.LayoutParams(0, Ui.dp(this, 40), 1f)
            if (i < options.size - 1) clp.rightMargin = Ui.dp(this, 6)
            chip.layoutParams = clp
            chip.setOnClickListener {
                try {
                    Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, ms)
                    Ui.toast(this, "Tela apaga em $label ⏲️")
                    build()
                } catch (e: Exception) {
                    Ui.toast(this, "Sem permissão: ${e.message}")
                }
            }
            row.addView(chip)
        }
        col.addView(row)

        val autoRotate = try {
            Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION) == 1
        } catch (e: Exception) {
            true
        }
        Ui.toggle(col, "🔄", "Rotação automática", "Girar a tela quando você vira o aparelho", autoRotate) { on ->
            try {
                Settings.System.putInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, if (on) 1 else 0)
                Ui.toast(this, if (on) "Rotação ativada" else "Rotação travada")
            } catch (e: Exception) {
                Ui.toast(this, "Sem permissão: ${e.message}")
            }
        }
    }

    private fun safeStart(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Ui.toast(this, "Indisponível neste aparelho")
        }
    }
}
