package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

class GesturesActivity : Activity() {

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Gestos e Atalhos", "Estilo One Hand Operation+: o aparelho inteiro ao alcance do polegar")

        Ui.section(col, "Alça de Borda")
        val canDraw = Settings.canDrawOverlays(this)
        if (!canDraw) {
            Ui.card(col, "🔑", "Liberar a Alça de Borda", "A alça flutua na borda da tela por cima de qualquer app. Para funcionar, permita \"sobrepor a outros apps\".", 0) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        } else {
            Ui.toggle(col, "🎯", "Alça de Borda ativa", "Pílula flutuante na lateral: arraste para posicionar, toque para abrir seus apps", isRunning()) { on ->
                getSharedPreferences("imfinix_state", MODE_PRIVATE).edit().putBoolean("edge_handle", on).apply()
                if (on) {
                    EdgeHandleService.start(this)
                    Ui.toast(this, "Alça ativada! Procure a pílula na lateral direita 🎯")
                } else {
                    EdgeHandleService.stop(this)
                }
            }
            Ui.note(col, "Como usar: a pílula azul fica na borda direita de qualquer tela. Arraste para cima/baixo para posicionar na altura do seu polegar. Toque nela para abrir a lista de apps.")
        }

        Ui.section(col, "Apps")
        Ui.card(col, "📱", "Abrir app rapidamente", "Lista de todos os apps instalados, ordenados por nome", 1) {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        Ui.section(col, "Gestos do sistema")
        Ui.card(col, "🧭", "Navegação por gestos", "Ative a navegação por gestos do XOS em Configurações", 2) {
            try {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            } catch (e: Exception) {
                Ui.toast(this, "Indisponível")
            }
        }
        Ui.card(col, "♿", "Gestos de acessibilidade", "Toque assistido, ampliação e atalhos de acessibilidade", 3) {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        Ui.card(col, "🏠", "Tela inicial", "Grade de ícones, estilo e gestos do launcher", 4) {
            try {
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            } catch (e: Exception) {
                Ui.toast(this, "Indisponível neste aparelho")
            }
        }

        Ui.note(col, "Gestos de borda customizados (deslizar para ações) no estilo One Hand Operation+ completo exigiriam serviço de acessibilidade avançado ou root. A Alça de Borda do ImfinixLook cobre o caso mais útil: abrir qualquer app com o polegar.")
    }

    private fun isRunning(): Boolean {
        return getSharedPreferences("imfinix_state", MODE_PRIVATE).getBoolean("edge_handle", false)
    }
}
