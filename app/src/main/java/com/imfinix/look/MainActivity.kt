package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.util.Calendar

class MainActivity : Activity() {

    private var paletteAtBuild: String = ""

    override fun onResume() {
        super.onResume()
        val now = Theme.current(this).id
        if (paletteAtBuild.isNotEmpty() && paletteAtBuild != now) recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        paletteAtBuild = Theme.current(this).id
        val col = Ui.root(this)

        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Bom dia"
            in 12..17 -> "Boa tarde"
            else -> "Boa noite"
        }
        Ui.hero(col, "$greeting! Vamos deixar seu Infinix com a sua cara", "ImfinixLook 2.0 - hub completo de personalização para o XOS", "✨")

        val prefs = getSharedPreferences("imfinix_state", MODE_PRIVATE)
        if (!prefs.getBoolean("seen_v28", false)) {
            Ui.card(col, "🎉", "Novidade da versão 2.8: Assistente IA", "Agora o app tem chat com Claude e Perplexity (você cola sua chave de API, que fica só no seu celular) e um assistente Offline que responde sem internet e sem chave.") {
                prefs.edit().putBoolean("seen_v28", true).apply()
                recreate()
            }
        }

        Ui.section(col, "Módulos")
        val modules = listOf(
            Triple("🤖", "Assistente IA", "Claude, Perplexity e offline"),
            Triple("🪄", "Profundidade", "Sujeito na frente do relógio"),
            Triple("💧", "Liquid Glass", "Ícones e widgets de vidro"),
            Triple("👑", "Domínio Total", "Animações, DPI, status bar, Hz"),
            Triple("🌌", "Wallpapers", "16 estilos + sua foto"),
            Triple("🔒", "Tela de Bloqueio", "Wallpaper e ajustes"),
            Triple("🕐", "Relógios", "5 estilos + modo mesa"),
            Triple("🎨", "Temas", "Cores do app inteiro"),
            Triple("⚡", "Ajustes Rápidos", "Brilho, tela e atalhos"),
            Triple("🎛️", "Painel Rápido", "Configs escondidas"),
            Triple("🔊", "Perfis de Som", "Volumes e perfis"),
            Triple("✋", "Gestos", "Alça de borda e apps"),
            Triple("🔔", "Notificações", "Histórico completo"),
            Triple("💡", "Dicas XOS", "Truques do sistema")
        )
        Ui.grid(col, modules, 2) { which ->
            val target = when (which) {
                0 -> AiChatActivity::class.java
                1 -> DepthActivity::class.java
                2 -> IconPackActivity::class.java
                3 -> ControleTotalActivity::class.java
                4 -> WallpapersActivity::class.java
                5 -> LockScreenActivity::class.java
                6 -> ClockStylesActivity::class.java
                7 -> ThemesActivity::class.java
                8 -> QuickSettingsActivity::class.java
                9 -> QuickPanelActivity::class.java
                10 -> SoundProfilesActivity::class.java
                11 -> GesturesActivity::class.java
                12 -> NotificationHistoryActivity::class.java
                else -> TipsActivity::class.java
            }
            startActivity(Intent(this, target))
        }

        Ui.section(col, "Sobre")
        Ui.card(col, "💎", "ImfinixLook 2.0", "Projeto original inspirado em hubs de personalização. Feito para XOS / Android 14.", 12) {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
