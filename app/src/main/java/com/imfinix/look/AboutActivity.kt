package com.imfinix.look

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle

class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Sobre o ImfinixLook", "Feito com carinho para o seu Infinix")

        Ui.hero(col, "ImfinixLook 2.9", "Hub de personalização com 14 módulos, relógios, estúdio de wallpapers, assistente IA e controle real do aparelho.", "💎")

        Ui.section(col, "Projeto")
        Ui.card(col, "🧑‍💻", "Código 100% original", "Escrito do zero em Kotlin para XOS / Android 14. Nenhum código ou recurso visual da Samsung foi usado: a inspiração é só conceitual.", 1) {}
        Ui.card(col, "🐙", "Código aberto no GitHub", "Veja o código, acompanhe atualizações e baixe novas versões", 2) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/deabreusoaresdossantos-del/imfinixlook")))
        }
        Ui.card(col, "📦", "Versão 2.9.0", "Package com.imfinix.look • minSdk 26 • targetSdk 34", 3) {}
        Ui.card(col, "📜", "Licença MIT", "Use, estude e modifique à vontade", 4) {}

        Ui.section(col, "Suas configurações")
        Ui.card(col, "🗄️", "Backup das configurações", "Salva temas, wallpapers e ajustes num arquivo no aparelho (as chaves de IA NÃO entram no backup)", 5) {
            Ui.toast(this, BackupManager.export(application))
        }
        Ui.card(col, "♻️", "Restaurar backup", if (BackupManager.backupExists(this)) "Backup encontrado - toque para restaurar" else "Nenhum backup salvo ainda", 6) {
            Ui.toast(this, BackupManager.restore(application))
        }

        Ui.section(col, "Diagnóstico")
        val app = application as ImfinixApp
        if (CrashLogger.hasLog(application as android.app.Application)) {
            Ui.card(col, "🚨", "Relatório de erro", "O app registrou uma falha. Toque para ver - manda pra mim que eu corrijo.", 7) {
                AlertDialog.Builder(this)
                    .setTitle("Últimas falhas")
                    .setMessage(CrashLogger.read(application as android.app.Application))
                    .setPositiveButton("Limpar") { _, _ ->
                        CrashLogger.clear(application as android.app.Application)
                        recreate()
                    }
                    .setNegativeButton("Fechar", null)
                    .show()
            }
        } else {
            Ui.card(col, "✅", "Nenhuma falha registrada", "Se o app fechar sozinho, o erro fica salvo aqui pra investigação.", 7) {}
        }

        Ui.section(col, "O que exigiria root")
        Ui.note(col, "Reposicionar o relógio da lockscreen, editar os tiles do painel rápido, gestos de borda avançados e temas de sistema completos exigem root ou Shizuku em qualquer aparelho não-Samsung. Todo o resto o ImfinixLook já faz hoje.")
    }
}
