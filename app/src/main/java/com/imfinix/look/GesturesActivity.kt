package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

class GesturesActivity : Activity() {

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
        Ui.header(col, "Gestos e Atalhos", "Navegação e atalhos de gestos do XOS")

        Ui.section(col, "Gestos do sistema")
        Ui.card(col, "🧭", "Navegação do sistema", "Escolha entre gestos ou botões") { open("android.settings.GESTURE_NAVIGATION_SETTINGS") }
        Ui.card(col, "↔️", "Sensibilidade de voltar", "Ajuste a borda do gesto de voltar") { open("android.settings.GESTURE_SETTINGS") }
        Ui.card(col, "✋", "Gestos do XOS", "Toque duplo, captura com 3 dedos e mais (quando disponível)") {
            open("com.android.settings.GestureSettings")
        }

        Ui.section(col, "Atalhos úteis")
        Ui.card(col, "➕", "Atalhos na tela inicial", "Segure o ícone do ImfinixLook na home para fixar módulos como atalhos") { }
        Ui.card(col, "ℹ️", "Gestos avançados sem root", "Criar gestos personalizados de borda (estilo One Hand Operation+) exige root, Shizuku ou apps de acessibilidade de terceiros.") { }
    }
}
