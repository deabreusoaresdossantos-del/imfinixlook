package com.imfinix.look

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class SoundProfilesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val col = Ui.root(this)
        Ui.header(col, "Perfis de Som", "Alterne o perfil e ajuste os volumes em um só lugar")

        Ui.section(col, "Perfil")
        Ui.card(col, "🔔", "Som normal", "Toque e notificações com som") { setMode(am, AudioManager.RINGER_MODE_NORMAL, "Perfil: som normal") }
        Ui.card(col, "📳", "Vibrar", "Sem som, só vibração") { setMode(am, AudioManager.RINGER_MODE_VIBRATE, "Perfil: vibrar") }
        Ui.card(col, "🔇", "Silencioso", "Sem som nem vibração") { setMode(am, AudioManager.RINGER_MODE_SILENT, "Perfil: silencioso") }

        Ui.section(col, "Volumes")
        addVolume(col, am, "Mídia", AudioManager.STREAM_MUSIC)
        addVolume(col, am, "Toque", AudioManager.STREAM_RING)
        addVolume(col, am, "Alarme", AudioManager.STREAM_ALARM)

        Ui.section(col, "Mais")
        Ui.card(col, "📵", "Não perturbe", "Regras e exceções do modo DND") {
            try { startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) } catch (e: Exception) {
                Toast.makeText(this, "Esta tela não existe neste aparelho", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setMode(am: AudioManager, mode: Int, msg: String) {
        try {
            am.ringerMode = mode
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "O aparelho bloqueou a troca de perfil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addVolume(col: LinearLayout, am: AudioManager, name: String, stream: Int) {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        val bg = android.graphics.drawable.GradientDrawable()
        bg.setColor(Ui.CARD)
        bg.cornerRadius = Ui.dp(this, 18).toFloat()
        box.background = bg
        val pad = Ui.dp(this, 18)
        box.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = Ui.dp(this, 14)
        box.layoutParams = lp

        val label = TextView(this)
        label.text = name
        label.textSize = 15f
        label.setTextColor(Ui.TEXT)
        box.addView(label)

        val sb = SeekBar(this)
        sb.max = am.getStreamMaxVolume(stream)
        sb.progress = am.getStreamVolume(stream)
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, v: Int, fromUser: Boolean) {
                if (fromUser) try { am.setStreamVolume(stream, v, 0) } catch (_: Exception) {}
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        box.addView(sb)
        col.addView(box)
    }
}
