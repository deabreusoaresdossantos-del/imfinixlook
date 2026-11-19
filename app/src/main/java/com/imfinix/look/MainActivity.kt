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
        if (!prefs.getBoolean("seen_v27", false)) {
            Ui.card(col, "🎉", "Novidades da versão 2.7", "Domínio Total turbinado: DPI da tela, taxa de atualização, ajuste fino de cada animação, esconder relógio/bateria/sinal da barra de status, porcentagem da bateria e economia de energia. E no Estúdio: 6 estilos novos de wallpaper, wallpaper da sua foto e wallpaper do dia automático.") {
                prefs.edit().putBoolean("seen_v27", true).apply()
                recreate()
            }
        }

        Ui.section(col, "Módulos")
        val modules = listOf(
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
                0 -> DepthActivity::class.java
                1 -> IconPackActivity::class.java
                2 -> ControleTotalActivity::class.java
                3 -> WallpapersActivity::class.java
                4 -> LockScreenActivity::class.java
                5 -> ClockStylesActivity::class.java
                6 -> ThemesActivity::class.java
                7 -> QuickSettingsActivity::class.java
                8 -> QuickPanelActivity::class.java
                9 -> SoundProfilesActivity::class.java
                10 -> GesturesActivity::class.java
                11 -> NotificationHistoryActivity::class.java
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
