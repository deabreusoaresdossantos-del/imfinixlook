package com.imfinix.look

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class WallpapersActivity : Activity() {

    private var selected = 0
    private var seed = 42L
    private var preview: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("imfinix_wall", MODE_PRIVATE)
        seed = prefs.getLong("seed", 42L)
        val savedStyle = prefs.getString("style", null)
        if (savedStyle != null) {
            val idx = WallpaperEngine.styles.indexOfFirst { it.id == savedStyle }
            if (idx >= 0) selected = idx
        }
        build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 441 && resultCode == RESULT_OK && data?.data != null) {
            val ok = WallpaperEngine.savePhoto(this, data.data!!)
            Ui.toast(this, if (ok) "Foto carregada! Veja a prévia 🖼️" else "Não consegui ler essa foto")
            val idx = WallpaperEngine.styles.indexOfFirst { it.id == "foto" }
            if (ok && idx >= 0) selected = idx
            build()
        }
    }

    private fun pickPhoto() {
        val i = try {
            if (android.os.Build.VERSION.SDK_INT >= 33) Intent(MediaStore.ACTION_PICK_IMAGES)
            else Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        } catch (e: Exception) {
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }
        try {
            startActivityForResult(i, 441)
        } catch (e: Exception) {
            Ui.toast(this, "Nenhum app de galeria encontrado")
        }
    }

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Estúdio de Wallpapers", "Gradientes gerados no aparelho, na resolução exata da sua tela")

        val frame = LinearLayout(this)
        frame.orientation = LinearLayout.VERTICAL
        frame.gravity = Gravity.CENTER_HORIZONTAL
        frame.background = Ui.cardBg(this, 26f)
        frame.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14))
        val flp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        flp.bottomMargin = Ui.dp(this, 16)
        frame.layoutParams = flp

        val img = ImageView(this)
        val h = Ui.dp(this, 260)
        img.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h)
        img.scaleType = ImageView.ScaleType.CENTER_CROP
        img.clipToOutline = true
        img.background = Ui.cardBg(this, 20f)
        frame.addView(img)
        preview = img

        val label = TextView(this)
        label.textSize = 14f
        label.setTextColor(Ui.SUB)
        label.gravity = Gravity.CENTER
        val llp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        llp.topMargin = Ui.dp(this, 10)
        label.layoutParams = llp
        frame.addView(label)
        col.addView(frame)
        Ui.animIn(frame, 0)

        fun refreshPreview() {
            val style = WallpaperEngine.styles[selected]
            img.setImageBitmap(WallpaperEngine.generate(this, style.id, seed))
            label.text = "${style.emoji} ${style.name} - ${style.desc}"
            getSharedPreferences("imfinix_wall", MODE_PRIVATE).edit()
                .putString("style", style.id).putLong("seed", seed).apply()
        }
        refreshPreview()

        Ui.section(col, "Estilos")
        val styles = WallpaperEngine.styles
        var i = 0
        while (i < styles.size) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            val rlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            rlp.bottomMargin = Ui.dp(this, 10)
            row.layoutParams = rlp
            for (j in 0..2) {
                if (i + j < styles.size) {
                    val s = styles[i + j]
                    val chip = TextView(this)
                    chip.text = "${s.emoji}\n${s.name}"
                    chip.textSize = 12f
                    chip.gravity = Gravity.CENTER
                    chip.setTextColor(Ui.TEXT)
                    val bg = Ui.cardBg(this, 16f)
                    if (i + j == selected) {
                        bg.setStroke(Ui.dp(this, 2), Ui.accent(this))
                    }
                    chip.background = bg
                    val clp = LinearLayout.LayoutParams(0, Ui.dp(this, 62), 1f)
                    if (j < 2) clp.rightMargin = Ui.dp(this, 10)
                    chip.layoutParams = clp
                    val which = i + j
                    chip.setOnClickListener {
                        selected = which
                        build()
                    }
                    row.addView(chip)
                }
            }
            col.addView(row)
            i += 3
        }

        Ui.button(col, "🎲 Gerar outra variação", primary = false) {
            seed = java.util.Random().nextLong()
            build()
        }

        if (WallpaperEngine.styles[selected].id == "foto") {
            Ui.button(col, "🖼️ Trocar a foto", primary = false) { pickPhoto() }
            if (!WallpaperEngine.hasPhoto(this)) {
                Ui.note(col, "Toque em \"Trocar a foto\" e escolha uma imagem da galeria. Ela fica só no seu aparelho.")
            }
        }

        Ui.section(col, "Automação")
        val prefs = getSharedPreferences("imfinix_wall", MODE_PRIVATE)
        val dailyOn = prefs.getBoolean("daily", false)
        Ui.toggle(col, "🌅", "Wallpaper do dia", "Todo dia de manhã o estilo escolhido ganha uma variação nova sozinho", dailyOn) { on ->
            prefs.edit().putBoolean("daily", on).apply()
            DailyWallpaper.schedule(this, on)
            Ui.toast(this, if (on) "Wallpaper do dia ativado 🌅" else "Wallpaper do dia desligado")
        }

        Ui.section(col, "Aplicar")
        Ui.button(col, "🏠 Tela inicial") { apply(WallpaperManager.FLAG_SYSTEM) }
        Ui.button(col, "🔒 Tela de bloqueio", primary = false) { apply(WallpaperManager.FLAG_LOCK) }
        Ui.button(col, "✨ As duas telas", primary = false) { apply(WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK) }
        Ui.note(col, "Os wallpapers são criados localmente pelo ImfinixLook: nada é baixado da internet. Cada estilo usa a resolução exata da sua tela.")

        Ui.section(col, "Sua foto")
        Ui.button(col, "🖼️ Wallpaper da sua foto (difuso)", primary = false) { pickPhoto() }
        Ui.note(col, "Escolhe uma foto e ela vira um fundo desfocado elegante - combina com o relógio e os widgets.")
    }

    private fun apply(which: Int) {
        try {
            val style = WallpaperEngine.styles[selected]
            val bmp = WallpaperEngine.generate(this, style.id, seed)
            WallpaperManager.getInstance(this).setBitmap(bmp, null, true, which)
            Ui.toast(this, "Wallpaper ${style.name} aplicado ✅")
        } catch (e: Exception) {
            Ui.toast(this, "Não consegui aplicar: ${e.message}")
        }
    }
}
