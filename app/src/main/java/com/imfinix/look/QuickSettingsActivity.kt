package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class QuickSettingsActivity : Activity() {

    private fun open(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Toast.makeText(this, "Esta tela não existe neste aparelho", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Ajustes Rápidos", "Toque para abrir direto a tela de configuração")

        Ui.section(col, "Tela e som")
        Ui.card(col, "🌙", "Tela", "Brilho, tema escuro, tempo limite") { open(Settings.ACTION_DISPLAY_SETTINGS) }
        Ui.card(col, "🔊", "Som e vibração", "Volumes, toque e notificações sonoras") { open(Settings.ACTION_SOUND_SETTINGS) }
        Ui.card(col, "🌃", "Modo noturno / luz noturna", "Filtro de luz azul") { open(Settings.ACTION_NIGHT_DISPLAY_SETTINGS) }

        Ui.section(col, "Sistema")
        Ui.card(col, "🔔", "Notificações", "Apps que podem notificar") { open(Settings.ACTION_APP_NOTIFICATION_SETTINGS) }
        Ui.card(col, "🔋", "Bateria", "Uso e economia de bateria") { open(Settings.ACTION_BATTERY_SAVER_SETTINGS) }
        Ui.card(col, "🔒", "Tela de bloqueio", "Bloqueio e segurança") { open(Settings.ACTION_SECURITY_SETTINGS) }
        Ui.card(col, "🛜", "Wi-Fi", "Redes e conexões") { open(Settings.ACTION_WIFI_SETTINGS) }
        Ui.card(col, "📶", "Bluetooth", "Pareamento de dispositivos") { open(Settings.ACTION_BLUETOOTH_SETTINGS) }
        Ui.card(col, "ℹ️", "Sobre o telefone", "Versão do XOS e Android") { open(Settings.ACTION_DEVICE_INFO_SETTINGS) }
    }
}
