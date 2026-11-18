package com.imfinix.look

import android.app.Activity
import android.app.PendingIntent
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Calendar

class DepthActivity : Activity() {

    private val REQ_PICK = 4101
    private var preview: ImageView? = null
    private var status: TextView? = null
    private var bgSrc: Bitmap? = null
    private var bgBlurred: Bitmap? = null
    private var blurFor = -1
    private var fg: Bitmap? = null
    private val ui = Handler(Looper.getMainLooper())
    private var editSecond = false
    private var hueBar: android.widget.SeekBar? = null
    private var satBar: android.widget.SeekBar? = null
    private var valBar: android.widget.SeekBar? = null
    private var swatch: android.view.View? = null

    private fun curColorKey(): String {
        val mode = DepthEngine.prefs(this).getInt("cmode", 0)
        return if (editSecond && mode == 2) "ccolor2" else "ccolor"
    }

    private fun setClockColor(v: Int, fromPalette: Boolean) {
        DepthEngine.prefs(this).edit().putInt(curColorKey(), v).apply()
        val g = android.graphics.drawable.GradientDrawable()
        g.setColor(v)
        g.cornerRadius = Ui.dp(this, 14).toFloat()
        g.setStroke(Ui.dp(this, 1), android.graphics.Color.argb(120, 255, 255, 255))
        swatch?.background = g
        if (fromPalette) {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(v, hsv)
            hueBar?.progress = hsv[0].toInt()
            satBar?.progress = (hsv[1] * 100).toInt()
            valBar?.progress = (hsv[2] * 100).toInt()
        }
        DepthEngine.bumpRev(this)
        refresh()
    }

    private fun colorUi(col: LinearLayout) {
        val p = DepthEngine.prefs(this)
        val mode = p.getInt("cmode", 0).coerceIn(0, 2)
        val modes = arrayOf("Cor sólida", "Gradiente do tema", "Gradiente de duas cores")
        Ui.section(col, "Cor do relógio")
        Ui.button(col, "🎨 Modo: ${modes[mode]}", primary = false) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Modo de cor")
                .setItems(modes) { _, i ->
                    p.edit().putInt("cmode", i).apply()
                    recreate()
                }.show()
        }
        if (mode == 1) {
            Ui.note(col, "O relógio usa o degradê das duas cores do tema atual (troque em Temas).")
            return
        }
        if (mode == 2) {
            Ui.button(col, if (editSecond) "✏️ Editando a cor 2 (toque para ir à cor 1)" else "✏️ Editando a cor 1 (toque para ir à cor 2)", primary = false) {
                editSecond = !editSecond
                recreate()
            }
        }
        val sw = android.view.View(this)
        val slp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 34))
        slp.bottomMargin = Ui.dp(this, 10)
        sw.layoutParams = slp
        col.addView(sw)
        swatch = sw
        val cur = p.getInt(curColorKey(), if (curColorKey() == "ccolor2") android.graphics.Color.parseColor("#F472B6") else android.graphics.Color.WHITE)

        val hues = floatArrayOf(55f, 40f, 25f, 5f, 345f, 320f, 285f, 255f, 225f, 200f, 170f, 130f)
        val rows = arrayOf(floatArrayOf(0.45f, 1f), floatArrayOf(0.75f, 1f), floatArrayOf(1f, 0.95f), floatArrayOf(1f, 0.75f), floatArrayOf(1f, 0.55f), floatArrayOf(0.9f, 0.35f), floatArrayOf(0.8f, 0.2f))
        val grays = floatArrayOf(1f, 0.85f, 0.7f, 0.55f, 0.4f, 0.25f, 0.1f)
        for (r in rows.indices) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            for (cIdx in 0..12) {
                val color = if (cIdx < 12) android.graphics.Color.HSVToColor(floatArrayOf(hues[cIdx], rows[r][0], rows[r][1]))
                else android.graphics.Color.HSVToColor(floatArrayOf(0f, 0f, grays[r]))
                val cell = android.view.View(this)
                val cl = LinearLayout.LayoutParams(0, Ui.dp(this, 26), 1f)
                cl.setMargins(Ui.dp(this, 1), Ui.dp(this, 1), Ui.dp(this, 1), Ui.dp(this, 1))
                cell.layoutParams = cl
                val g = android.graphics.drawable.GradientDrawable()
                g.setColor(color)
                g.cornerRadius = Ui.dp(this, 4).toFloat()
                cell.background = g
                cell.setOnClickListener { setClockColor(color, true) }
                row.addView(cell)
            }
            col.addView(row)
        }
        val gap = android.view.View(this)
        gap.layoutParams = LinearLayout.LayoutParams(1, Ui.dp(this, 12))
        col.addView(gap)

        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(cur, hsv)
        fun fromSliders() {
            val h = hueBar?.progress ?: 0
            val s = satBar?.progress ?: 0
            val v = valBar?.progress ?: 100
            setClockColor(android.graphics.Color.HSVToColor(floatArrayOf(h.toFloat(), s / 100f, v / 100f)), false)
        }
        hueBar = Ui.slider(col, "Matiz", "🌈", 360, hsv[0].toInt()) { fromSliders() }
        satBar = Ui.slider(col, "Saturação", "💧", 100, (hsv[1] * 100).toInt()) { fromSliders() }
        valBar = Ui.slider(col, "Brilho", "🔆", 100, (hsv[2] * 100).toInt()) { fromSliders() }
        val g0 = android.graphics.drawable.GradientDrawable()
        g0.setColor(cur)
        g0.cornerRadius = Ui.dp(this, 14).toFloat()
        g0.setStroke(Ui.dp(this, 1), android.graphics.Color.argb(120, 255, 255, 255))
        sw.background = g0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val col = Ui.root(this)
        Ui.header(col, "Profundidade", "Efeito estilo Oppo: o sujeito da foto fica na frente do relógio")

        val frame = LinearLayout(this)
        frame.orientation = LinearLayout.VERTICAL
        frame.gravity = Gravity.CENTER_HORIZONTAL
        frame.background = Ui.cardBg(this, 26f)
        val fp = Ui.dp(this, 14)
        frame.setPadding(fp, fp, fp, fp)
        val flp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        flp.bottomMargin = Ui.dp(this, 14)
        frame.layoutParams = flp
        val img = ImageView(this)
        img.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 420))
        img.scaleType = ImageView.ScaleType.FIT_CENTER
        img.setBackgroundColor(Ui.SURFACE)
        frame.addView(img)
        preview = img
        val st = TextView(this)
        st.textSize = 13f
        st.setTextColor(Ui.SUB)
        st.gravity = Gravity.CENTER
        val slp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        slp.topMargin = Ui.dp(this, 10)
        st.layoutParams = slp
        st.text = if (DepthEngine.hasPhoto(this)) "Foto carregada. Ajuste e aplique." else "Escolha uma foto para começar."
        frame.addView(st)
        status = st
        col.addView(frame)

        Ui.button(col, "🖼️ Escolher foto da galeria") { pickPhoto() }

        val p = DepthEngine.prefs(this)

        Ui.section(col, "Camadas")
        Ui.toggle(col, "🪄", "Sujeito na frente do relógio", "Liga o recorte de profundidade", p.getBoolean("depth", true)) {
            p.edit().putBoolean("depth", it).apply()
            DepthEngine.bumpRev(this)
            refresh()
        }

        Ui.section(col, "Relógio")
        Ui.button(col, "🔤 Estilo: ${DepthEngine.clockStyles[p.getInt("style", 0).coerceIn(0, DepthEngine.clockStyles.size - 1)]}", primary = false) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Estilo do relógio")
                .setItems(DepthEngine.clockStyles.toTypedArray()) { _, i ->
                    p.edit().putInt("style", i).apply()
                    DepthEngine.bumpRev(this)
                    recreate()
                }.show()
        }
        Ui.button(col, "🅰️ Fonte: ${DepthEngine.fontNames[p.getInt("font", 0).coerceIn(0, DepthEngine.fontNames.size - 1)]}", primary = false) {
            android.app.AlertDialog.Builder(this)
                .setTitle("Fonte do relógio")
                .setItems(DepthEngine.fontNames.toTypedArray()) { _, i ->
                    p.edit().putInt("font", i).apply()
                    DepthEngine.bumpRev(this)
                    recreate()
                }.show()
        }
        colorUi(col)
        Ui.slider(col, "Tamanho", "📏", 100, p.getInt("size", 70)) {
            p.edit().putInt("size", maxOf(it, 30)).apply()
            refresh()
        }
        Ui.slider(col, "Posição vertical", "↕️", 80, p.getInt("ypos", 12)) {
            p.edit().putInt("ypos", it).apply()
            refresh()
        }
        Ui.slider(col, "Opacidade", "👻", 100, p.getInt("alpha", 100)) {
            p.edit().putInt("alpha", maxOf(it, 20)).apply()
            refresh()
        }

        Ui.section(col, "Fundo")
        Ui.slider(col, "Desfoque", "🌫️", 12, p.getInt("blur", 0)) {
            p.edit().putInt("blur", it).apply()
            refresh()
        }
        Ui.slider(col, "Escurecer", "🌑", 70, p.getInt("dim", 0)) {
            p.edit().putInt("dim", it).apply()
            refresh()
        }

        Ui.section(col, "Aplicar")
        Ui.button(col, "✨ Ativar wallpaper vivo (relógio ao vivo + profundidade)") { applyLive() }
        Ui.button(col, "🏠 Só imagem fixa na tela inicial", primary = false) { applyStatic(WallpaperManager.FLAG_SYSTEM) }
        Ui.button(col, "🔒 Só imagem fixa no bloqueio", primary = false) { applyStatic(WallpaperManager.FLAG_LOCK) }
        Ui.note(col, "O wallpaper vivo desenha o relógio por trás do sujeito e atualiza sozinho. Se o XOS não aplicar na tela de bloqueio, use a imagem fixa (o relógio fica parado na hora em que foi gerada).")

        Ui.section(col, "Widgets de vidro")
        Ui.button(col, "🕐 Fixar relógio de vidro", primary = false) { pin(GlassClockProvider::class.java) }
        Ui.button(col, "🔋 Fixar bateria de vidro", primary = false) { pin(GlassBatteryProvider::class.java) }
        Ui.note(col, "Vidro translúcido de verdade (com desfoque do fundo) não é possível em widgets sem root. Os widgets usam um painel branco translúcido com borda, que combina com wallpapers escuros ou desfocados.")

        loadSaved()
    }

    private fun pickPhoto() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "image/*"
        startActivityForResult(Intent.createChooser(i, "Escolha uma foto"), REQ_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_PICK || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        status?.text = "Processando a foto..."
        Thread {
            try {
                val bmp = DepthEngine.loadScaled(this, uri)
                ui.post {
                    if (bmp == null) {
                        status?.text = "Não consegui abrir essa foto."
                    } else {
                        status?.text = "Recortando o sujeito (na primeira vez pode baixar o modelo)..."
                        DepthEngine.segment(bmp) { cut, msg ->
                            Thread {
                                try {
                                    DepthEngine.save(this, bmp, cut)
                                } catch (e: Exception) {
                                }
                                ui.post {
                                    bgSrc = bmp
                                    blurFor = -1
                                    fg = cut
                                    status?.text = msg
                                    refresh()
                                }
                            }.start()
                        }
                    }
                }
            } catch (e: Throwable) {
                ui.post { status?.text = "Erro ao ler a foto: ${e.message}" }
            }
        }.start()
    }

    private fun loadSaved() {
        if (!DepthEngine.hasPhoto(this)) return
        Thread {
            val b = android.graphics.BitmapFactory.decodeFile(DepthEngine.bgFile(this).absolutePath)
            val f = if (DepthEngine.fgFile(this).exists()) android.graphics.BitmapFactory.decodeFile(DepthEngine.fgFile(this).absolutePath) else null
            ui.post {
                bgSrc = b
                fg = f
                refresh()
            }
        }.start()
    }

    private fun currentBg(): Bitmap? {
        val src = bgSrc ?: return null
        val amount = DepthEngine.prefs(this).getInt("blur", 0)
        if (amount != blurFor || bgBlurred == null) {
            bgBlurred = DepthEngine.blur(src, amount)
            blurFor = amount
        }
        return bgBlurred
    }

    private fun refresh() {
        val bg = currentBg() ?: return
        val pw = bg.width / 2
        val ph = bg.height / 2
        val out = Bitmap.createBitmap(pw, ph, Bitmap.Config.ARGB_8888)
        DepthEngine.drawScene(this, Canvas(out), pw, ph, bg, fg, Calendar.getInstance())
        preview?.setImageBitmap(out)
    }

    private fun applyStatic(which: Int) {
        val bg = currentBg()
        if (bg == null) {
            Ui.toast(this, "Escolha uma foto primeiro")
            return
        }
        Thread {
            try {
                val out = Bitmap.createBitmap(bg.width, bg.height, Bitmap.Config.ARGB_8888)
                DepthEngine.drawScene(this, Canvas(out), bg.width, bg.height, bg, fg, Calendar.getInstance())
                WallpaperManager.getInstance(this).setBitmap(out, null, true, which)
                ui.post { Ui.toast(this, "Imagem aplicada ✅") }
            } catch (e: Exception) {
                ui.post { Ui.toast(this, "Não consegui aplicar: ${e.message}") }
            }
        }.start()
    }

    private fun applyLive() {
        if (!DepthEngine.hasPhoto(this)) {
            Ui.toast(this, "Escolha uma foto primeiro")
            return
        }
        DepthEngine.bumpRev(this)
        try {
            val i = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this, DepthWallpaperService::class.java))
            startActivity(i)
        } catch (e: Exception) {
            try {
                startActivity(Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))
            } catch (e2: Exception) {
                Ui.toast(this, "Abra Papéis de parede > Wallpapers animados e escolha Imfinix Profundidade")
            }
        }
    }

    private fun pin(cls: Class<*>) {
        try {
            val mgr = AppWidgetManager.getInstance(this)
            val component = ComponentName(this, cls)
            if (mgr.isRequestPinAppWidgetSupported) {
                val cbIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                cbIntent.component = component
                val cb = PendingIntent.getBroadcast(this, 0, cbIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                mgr.requestPinAppWidget(component, null, cb)
            } else {
                Ui.toast(this, "Segure a tela inicial > Widgets > ImfinixLook")
            }
        } catch (e: Exception) {
            Ui.toast(this, "Não consegui fixar: ${e.message}")
        }
    }
}
