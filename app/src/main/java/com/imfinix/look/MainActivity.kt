package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.util.Calendar

class MainActivity : Activity() {

    companion object {
        val C_PURPLE = android.graphics.Color.parseColor("#A78BFA")
        val C_BLUE = android.graphics.Color.parseColor("#6C8CFF")
        val C_CYAN = android.graphics.Color.parseColor("#22D3EE")
        val C_AMBER = android.graphics.Color.parseColor("#FBBF24")
        val C_VIOLET = android.graphics.Color.parseColor("#9D6CFF")
        val C_SLATE = android.graphics.Color.parseColor("#94A3B8")
        val C_GREEN = android.graphics.Color.parseColor("#34D399")
        val C_PINK = android.graphics.Color.parseColor("#F472B6")
        val C_YELLOW = android.graphics.Color.parseColor("#FACC15")
        val C_ORANGE = android.graphics.Color.parseColor("#FB923C")
        val C_TEAL = android.graphics.Color.parseColor("#2DD4BF")
        val C_RED = android.graphics.Color.parseColor("#F87171")
        val C_INDIGO = android.graphics.Color.parseColor("#818CF8")
        val C_LIME = android.graphics.Color.parseColor("#A3E635")
    }

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
        Ui.hero(col, "$greeting! Vamos deixar seu Infinix com a sua cara", "ImfinixLook 2.9 - visual novo, fundação reforçada", "✨")

        val prefs = getSharedPreferences("imfinix_state", MODE_PRIVATE)
        if (!prefs.getBoolean("seen_v29", false)) {
            Ui.card(col, "🎉", "Novidades da versão 2.9", "Visual renovado: capa com brilho, cartões com cor própria por módulo e 3 temas novos (Oceano, Aurora, Café). Fundação reforçada: relatório de erro automático e backup/restauração das suas configurações (em Sobre).") {
                prefs.edit().putBoolean("seen_v29", true).apply()
                recreate()
            }
        }

        Ui.section(col, "Módulos")
        val modules = listOf(
            ModuleItem("🤖", "Assistente IA", "Claude, Perplexity e offline", C_PURPLE),
            ModuleItem("🪄", "Profundidade", "Sujeito na frente do relógio", C_BLUE),
            ModuleItem("💧", "Liquid Glass", "Ícones e widgets de vidro", C_CYAN),
            ModuleItem("👑", "Domínio Total", "Animações, DPI, status bar, Hz", C_AMBER),
            ModuleItem("🌌", "Wallpapers", "16 estilos + sua foto", C_VIOLET),
            ModuleItem("🔒", "Tela de Bloqueio", "Wallpaper e ajustes", C_SLATE),
            ModuleItem("🕐", "Relógios", "5 estilos + modo mesa", C_GREEN),
            ModuleItem("🎨", "Temas", "9 paletas de cor", C_PINK),
            ModuleItem("⚡", "Ajustes Rápidos", "Brilho, tela e atalhos", C_YELLOW),
            ModuleItem("🎛️", "Painel Rápido", "Configs escondidas", C_ORANGE),
            ModuleItem("🔊", "Perfis de Som", "Volumes e perfis", C_TEAL),
            ModuleItem("✋", "Gestos", "Alça de borda e apps", C_RED),
            ModuleItem("🔔", "Notificações", "Histórico completo", C_INDIGO),
            ModuleItem("💡", "Dicas XOS", "Truques do sistema", C_LIME)
        )
        Ui.moduleGrid(col, modules, 2) { which ->
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
        Ui.card(col, "💎", "ImfinixLook 2.9", "Projeto original inspirado em hubs de personalização. Feito para XOS / Android 14.", 12) {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
