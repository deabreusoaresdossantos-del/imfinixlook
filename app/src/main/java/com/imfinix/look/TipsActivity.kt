package com.imfinix.look

import android.app.Activity
import android.os.Bundle

class TipsActivity : Activity() {

    private data class Tip(val emoji: String, val title: String, val detail: String)

    private val sections = listOf(
        "Personalização" to listOf(
            Tip("🌌", "Wallpapers dinâmicos", "Use o Estúdio de Wallpapers do ImfinixLook para gerar gradientes na resolução exata da sua tela. Aplique um estilo na home e outro na lockscreen."),
            Tip("🎨", "Tema com a sua cor", "Escolha a paleta no módulo Temas: o app inteiro muda de cor na hora. Combine com um wallpaper do mesmo tom para um visual de loja."),
            Tip("🧩", "Ícones personalizados", "Segure a tela inicial e procure Estilo de Ícones no launcher XOS. Para icon packs da Play Store, instale o Nova Launcher ou Lawnchair."),
            Tip("🔤", "Fonte nova", "Em Configurações > Tela > Fonte o XOS permite trocar o estilo e o tamanho da fonte do sistema inteiro.")
        ),
        "Gestos" to listOf(
            Tip("🎯", "Alça de Borda", "Ative a Alça de Borda no módulo Gestos: uma pílula flutuante abre qualquer app com o polegar, de qualquer tela."),
            Tip("🧭", "Navegação por gestos", "Em Configurações > Sistema > Gestos, troque os 3 botões pela navegação por gestos: deslize de baixo para cima para a home."),
            Tip("📸", "Print rápido", "Aperte Power + Volume para baixo juntos. Em alguns XOS, deslizar 3 dedos para baixo também captura a tela."),
            Tip("🔦", "Lanterna no bloqueio", "Segure o ícone de lanterna na tela de bloqueio para acender sem desbloquear o aparelho.")
        ),
        "Bateria" to listOf(
            Tip("🔋", "Economia inteligente", "Ative a Economia de Bateria em Configurações > Bateria. O XOS também tem o modo Ultra Economia para dias apertados."),
            Tip("🌙", "Rotina noturna", "Use o Painel Rápido > Otimização de bateria para impedir que apps em segundo plano drenem carga durante a noite."),
            Tip("📉", "Descubra os devoradores", "Em Configurações > Bateria > Uso da bateria, veja qual app mais consome e restrinja o que não for essencial.")
        ),
        "Sistema" to listOf(
            Tip("⏲️", "Tela que não apaga", "Nos Ajustes Rápidos do ImfinixLook, aumente o tempo para a tela apagar quando estiver lendo ou cozinhando."),
            Tip("🔔", "Notificação recuperada", "Dispensou algo importante? O módulo Notificações do ImfinixLook guarda o histórico depois que você libera o acesso."),
            Tip("🧹", "Espaço livre", "Em Painel Rápido > Armazenamento interno, limpe cache e arquivos grandes sem instalar app de limpeza.")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Dicas XOS", "Truques para dominar o seu Infinix")

        var idx = 0
        sections.forEach { (name, tips) ->
            Ui.section(col, name)
            tips.forEach { tip ->
                Ui.card(col, tip.emoji, tip.title, tip.detail, idx++) {}
            }
        }

        Ui.note(col, "Alguns caminhos de menu variam conforme a versão do XOS. Se algo estiver em outro lugar, use a busca das Configurações.")
    }
}
