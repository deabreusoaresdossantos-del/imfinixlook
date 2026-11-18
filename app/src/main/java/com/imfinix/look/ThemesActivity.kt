package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast

class ThemesActivity : Activity() {

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
        Ui.header(col, "Temas e Ícones", "Combine wallpaper, ícones e fonte para um setup único")

        Ui.section(col, "Montar o setup")
        Ui.card(col, "🌑", "Wallpapers ImfinixLook", "Gradientes escuros gerados no app (módulo Wallpapers)") {
            startActivity(Intent(this, WallpapersActivity::class.java))
        }
        Ui.card(col, "🎨", "Loja de temas do XOS", "Abra o app Temas do XOS para fontes e temas de sistema") {
            try {
                val i = packageManager.getLaunchIntentForPackage("com.transsion.theme")
                if (i != null) startActivity(i) else throw Exception()
            } catch (e: Exception) {
                Toast.makeText(this, "App Temas do XOS não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
        Ui.card(col, "🔲", "Ícones monocromáticos", "O XOS 14 aplica ícones temáticos no Android 14 em Configurações > Tela inicial, quando disponível no aparelho") {
            open("android.settings.HOME_SETTINGS")
        }

        Ui.section(col, "Limites sem root")
        Ui.card(col, "ℹ️", "Pacotes de ícones de terceiros", "Aplicar icon packs exige um launcher compatível (Nova, Lawnchair, Niagara). O XOS Launcher nativo aceita só os temas da loja.") { }
    }
}
