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
        Ui.button(col, "🔤 Estilo: ${DepthEngine.clockStyles[p.getInt("style", 0)]}", primary = false) {
            p.edit().putInt("style", (p.getInt("style", 0) + 1) % DepthEngine.clockStyles.size).apply()
            DepthEngine.bumpRev(this)
            recreate()
        }
        Ui.button(col, "🎨 Cor: ${DepthEngine.clockColors[p.getInt("color", 0)]}", primary = false) {
            p.edit().putInt("color", (p.getInt("color", 0) + 1) % DepthEngine.clockColors.size).apply()
            DepthEngine.bumpRev(this)
            recreate()
        }
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
