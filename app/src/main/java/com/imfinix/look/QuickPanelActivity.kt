package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class QuickPanelActivity : Activity() {

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
        Ui.header(col, "Painel Rápido", "Atalhos para configurações escondidas e avançadas do sistema")

        Ui.section(col, "Avançado")
        Ui.card(col, "🛠️", "Opções do desenvolvedor", "Animações, DPI, depuração (ative em Sobre o telefone > toque 7x no número da versão)") {
            open(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        }
        Ui.card(col, "📊", "Painel de privacidade", "Permissões usadas recentemente pelos apps") {
            open("android.settings.PRIVACY_DASHBOARD_SETTINGS")
        }
        Ui.card(col, "🗂️", "Gerenciar apps", "Apps instalados, permissões e padrões") { open(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS) }
        Ui.card(col, "⭐", "Apps padrão", "Navegador, launcher e assistente padrão") { open(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS) }

        Ui.section(col, "Acessos especiais")
        Ui.card(col, "🔓", "Acesso a notificações", "Necessário para o Histórico de Notificações do ImfinixLook") {
            open(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        }
        Ui.card(col, "🪟", "Apps sobre outros apps", "Permissão de sobreposição") {
            open(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        }
        Ui.card(col, "📵", "Não perturbe", "Regras e exceções de DND") { open("android.settings.ZEN_MODE_SETTINGS") }
        Ui.card(col, "♿", "Acessibilidade", "Serviços e recursos de acessibilidade") { open(Settings.ACTION_ACCESSIBILITY_SETTINGS) }

        Ui.section(col, "Sistema")
        Ui.card(col, "🗄️", "Armazenamento", "Espaço usado e limpeza") { open(Settings.ACTION_INTERNAL_STORAGE_SETTINGS) }
        Ui.card(col, "🌐", "Idioma e região", "Idiomas do sistema") { open(Settings.ACTION_LOCALE_SETTINGS) }
        Ui.card(col, "📅", "Data e hora", "Fuso e formato de 24h") { open(Settings.ACTION_DATE_SETTINGS) }
    }
}
