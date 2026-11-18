package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Sobre o ImfinixLook", "Feito com carinho para o seu Infinix")

        Ui.hero(col, "ImfinixLook 2.0", "Hub de personalização com 10 módulos, 5 relógios, estúdio de wallpapers e controle real do aparelho.", "💎")

        Ui.section(col, "Projeto")
        Ui.card(col, "🧑‍💻", "Código 100% original", "Escrito do zero em Kotlin para XOS / Android 14. Nenhum código ou recurso visual da Samsung foi usado: a inspiração é só conceitual.", 1) {}
        Ui.card(col, "🐙", "Código aberto no GitHub", "Veja o código, acompanhe atualizações e baixe novas versões", 2) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/deabreusoaresdossantos-del/imfinixlook")))
        }
        Ui.card(col, "📦", "Versão 2.0.0", "Package com.imfinix.look • minSdk 26 • targetSdk 34 • sem dependências externas", 3) {}
        Ui.card(col, "📜", "Licença MIT", "Use, estude e modifique à vontade", 4) {}

        Ui.section(col, "O que exigiria root")
        Ui.note(col, "Reposicionar o relógio da lockscreen, editar os tiles do painel rápido, gestos de borda avançados e temas de sistema completos exigem root ou Shizuku em qualquer aparelho não-Samsung. Todo o resto o ImfinixLook já faz hoje.")
    }
}
