package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "ImfinixLook", "Hub de personalização estilo Good Lock para Infinix com XOS")

        Ui.section(col, "Aparência")
        Ui.card(col, "🔒", "Tela de Bloqueio", "Wallpaper exclusivo da lockscreen e ajustes de bloqueio") {
            startActivity(Intent(this, LockScreenActivity::class.java))
        }
        Ui.card(col, "🌑", "Wallpapers", "Gradientes escuros minimalistas gerados no app, prontos para aplicar") {
            startActivity(Intent(this, WallpapersActivity::class.java))
        }
        Ui.card(col, "🕐", "Estilos de Relógio", "Três faces de relógio para a tela inicial: Gigante, Minimal e Neon") {
            startActivity(Intent(this, ClockStylesActivity::class.java))
        }
        Ui.card(col, "🎨", "Temas e Ícones", "Combine wallpaper, ícones monocromáticos e fonte") {
            startActivity(Intent(this, ThemesActivity::class.java))
        }

        Ui.section(col, "Sistema")
        Ui.card(col, "⚡", "Ajustes Rápidos", "Atalhos diretos para tela, som, notificações, bateria e mais") {
            startActivity(Intent(this, QuickSettingsActivity::class.java))
        }
        Ui.card(col, "🎛️", "Painel Rápido", "Configurações escondidas e avançadas do Android em um só lugar") {
            startActivity(Intent(this, QuickPanelActivity::class.java))
        }
        Ui.card(col, "🔊", "Perfis de Som", "Alterne normal/vibrar/silencioso e ajuste todos os volumes") {
            startActivity(Intent(this, SoundProfilesActivity::class.java))
        }
        Ui.card(col, "✋", "Gestos e Atalhos", "Navegação por gestos e atalhos do XOS") {
            startActivity(Intent(this, GesturesActivity::class.java))
        }
        Ui.card(col, "🔔", "Histórico de Notificações", "Recupere notificações dispensadas sem querer") {
            startActivity(Intent(this, NotificationHistoryActivity::class.java))
        }

        Ui.section(col, "Aprender")
        Ui.card(col, "💡", "Dicas XOS", "Truques do XOS 14 no Infinix Hot 50i") {
            startActivity(Intent(this, TipsActivity::class.java))
        }

        Ui.section(col, "Sobre")
        Ui.card(col, "✨", "ImfinixLook 1.0", "Projeto original inspirado em hubs de personalização. Feito para XOS / Android 14.") { }
    }
}
