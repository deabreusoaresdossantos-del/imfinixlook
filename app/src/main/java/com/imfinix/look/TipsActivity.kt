package com.imfinix.look

import android.app.Activity
import android.os.Bundle

class TipsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Dicas XOS", "Truques para o Infinix Hot 50i com XOS 14")

        Ui.section(col, "Personalização")
        Ui.card(col, "🖼️", "Tema escuro total", "Configurações > Tela > Tema escuro: combina com os wallpapers do ImfinixLook e economiza bateria.") { }
        Ui.card(col, "🧩", "Widgets na home", "Segure um espaço vazio na tela inicial > Widgets e arraste o Relógio ImfinixLook.") { }
        Ui.card(col, "📁", "Gaveta de apps", "No XOS Launcher, segure a home > Configurações da tela inicial para escolher entre gaveta ou telas.") { }

        Ui.section(col, "Gestos e atalhos")
        Ui.card(col, "✌️", "Navegação por gestos", "Configurações > Sistema > Gestos > Navegação do sistema.") { }
        Ui.card(col, "👆", "Toque duplo para acordar", "Procure por 'Toque duplo' nas configurações de gestos do XOS.") { }

        Ui.section(col, "Bateria e desempenho")
        Ui.card(col, "🔋", "Economia de bateria", "Ative a economia adaptativa e restrinja apps em segundo plano que você não usa.") { }
        Ui.card(col, "🧹", "Limpeza de RAM", "Use o Phone Master do XOS para fechar apps pesados antes de jogar.") { }
    }
}
