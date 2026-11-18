package com.imfinix.look

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class ThemesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        build()
    }

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Temas e Ícones", "Estilo Theme Park: a cor que você escolher pinta o app inteiro")

        Ui.section(col, "Cor do tema")
        Ui.note(col, "Toque em uma palestra... quer dizer, paleta. Ela muda botões, destaques, ícones e até o relógio Neon do ImfinixLook.")

        val current = Theme.current(this)
        Theme.palettes.forEachIndexed { idx, p ->
            val box = LinearLayout(this)
            box.orientation = LinearLayout.HORIZONTAL
            box.gravity = Gravity.CENTER_VERTICAL
            box.background = Ui.cardBg(this, 18f)
            val pad = Ui.dp(this, 14)
            box.setPadding(pad, pad, pad, pad)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.bottomMargin = Ui.dp(this, 10)
            box.layoutParams = lp

            val swatch = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(p.a, p.b))
            swatch.cornerRadius = Ui.dp(this, 14).toFloat()
            val sw = TextView(this)
            sw.background = swatch
            sw.gravity = Gravity.CENTER
            sw.text = if (p.id == current.id) "✓" else ""
            sw.textSize = 18f
            sw.setTextColor(android.graphics.Color.WHITE)
            sw.layoutParams = LinearLayout.LayoutParams(Ui.dp(this, 52), Ui.dp(this, 52))
            box.addView(sw)

            val t = TextView(this)
            t.text = "${p.emoji} ${p.name}"
            t.textSize = 15f
            t.setTextColor(Ui.TEXT)
            t.typeface = android.graphics.Typeface.DEFAULT_BOLD
            val tlp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            tlp.leftMargin = Ui.dp(this, 14)
            t.layoutParams = tlp
            box.addView(t)

            box.setOnClickListener {
                Theme.set(this, p.id)
                Ui.toast(this, "Tema ${p.name} aplicado 🎨")
                build()
            }
            col.addView(box)
            Ui.animIn(box, idx)
        }

        Ui.section(col, "Combine assim")
        Ui.card(col, "🌌", "Wallpaper no mesmo tom", "Gere um wallpaper que combine com a paleta escolhida no Estúdio", 8) {
            startActivity(Intent(this, WallpapersActivity::class.java))
        }
        Ui.card(col, "🧩", "Ícones monocromáticos", "No XOS: segure a tela inicial > Estilo de ícones, ou use Nova/Lawnchair para icon packs", 9) {
            try {
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            } catch (e: Exception) {
                Ui.toast(this, "Abra as configurações da tela inicial no XOS")
            }
        }
        Ui.card(col, "🔤", "Fonte do sistema", "Troque a fonte em Configurações > Tela > Fonte (o XOS tem loja de fontes)", 10) {
            startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
        }
    }
}
