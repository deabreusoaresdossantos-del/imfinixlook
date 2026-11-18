package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings

class QuickPanelActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Painel Rápido", "Configurações escondidas e avançadas do Android, em um só lugar")

        Ui.section(col, "Privacidade e segurança")
        Ui.card(col, "🛡️", "Painel de privacidade", "Quais apps usaram câmera, microfone e localização", 0) { safe(Settings.ACTION_PRIVACY_SETTINGS) }
        Ui.card(col, "🔑", "Acessos especiais", "Apps que podem sobrepor tela, modificar sistema, acessar uso", 1) { safe("android.settings.MANAGE_SPECIAL_APP_ACCESSES_SETTINGS") }
        Ui.card(col, "📈", "Acesso a dados de uso", "Liberar apps que medem tempo de uso e estatísticas", 2) { safe(Settings.ACTION_USAGE_ACCESS_SETTINGS) }
        Ui.card(col, "🔕", "Não perturbe", "Regras de silêncio total, prioridade e exceções", 3) { safe(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS) }

        Ui.section(col, "Sistema avançado")
        Ui.card(col, "🛠️", "Opções do desenvolvedor", "Animações, depuração USB, escala de janela e mais", 4) { safe(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS) }
        Ui.card(col, "⭐", "Apps padrão", "Navegador, telefone, SMS, assistente e launcher padrão", 5) { safe(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS) }
        Ui.card(col, "🔋", "Otimização de bateria", "Escolha quais apps podem rodar sem restrição", 6) { safe(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS) }
        Ui.card(col, "♿", "Acessibilidade", "Serviços, legendas, contraste e interações", 7) { safe(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
        Ui.card(col, "💾", "Armazenamento interno", "Espaço por categoria e limpeza", 8) { safe(Settings.ACTION_INTERNAL_STORAGE_SETTINGS) }
        Ui.card(col, "📱", "Todos os apps", "Gerenciar, limpar cache e desinstalar", 9) { safe(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS) }
        Ui.card(col, "🌐", "VPN e redes privadas", "Conexões VPN configuradas", 10) { safe(Settings.ACTION_VPN_SETTINGS) }
        Ui.card(col, "📖", "Dicionário pessoal", "Palavras e atalhos do teclado", 11) { safe(Settings.ACTION_USER_DICTIONARY_SETTINGS) }

        Ui.note(col, "Algumas telas variam conforme a versão do XOS. Se um atalho não abrir, o aparelho não expõe aquela tela para apps de terceiros.")
    }

    private fun safe(action: String) {
        try {
            startActivity(Intent(action))
        } catch (e: Exception) {
            Ui.toast(this, "Indisponível neste aparelho")
        }
    }
}
