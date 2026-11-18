package com.imfinix.look

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.EditText
import org.json.JSONArray
import org.json.JSONObject

class SoundProfilesActivity : Activity() {

    private lateinit var audio: AudioManager
    private val streams = listOf(
        Triple("Toque e notificações", "🔔", AudioManager.STREAM_RING),
        Triple("Mídia", "🎵", AudioManager.STREAM_MUSIC),
        Triple("Alarme", "⏰", AudioManager.STREAM_ALARM),
        Triple("Chamadas", "📞", AudioManager.STREAM_VOICE_CALL)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        build()
    }

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Perfis de Som", "Estilo SoundAssistant: todos os volumes em uma tela")

        Ui.section(col, "Modo de toque")
        val mode = audio.ringerMode
        val modes = listOf(
            Triple(AudioManager.RINGER_MODE_NORMAL, "🔊", "Normal"),
            Triple(AudioManager.RINGER_MODE_VIBRATE, "📳", "Vibrar"),
            Triple(AudioManager.RINGER_MODE_SILENT, "🔇", "Silencioso")
        )
        val row = android.widget.LinearLayout(this)
        row.orientation = android.widget.LinearLayout.HORIZONTAL
        val rlp = android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        rlp.bottomMargin = Ui.dp(this, 12)
        row.layoutParams = rlp
        modes.forEachIndexed { i, (m, emoji, name) ->
            val chip = android.widget.TextView(this)
            chip.text = "$emoji\n$name"
            chip.textSize = 13f
            chip.gravity = android.view.Gravity.CENTER
            val bg = Ui.cardBg(this, 14f)
            if (mode == m) {
                bg.setStroke(Ui.dp(this, 2), Ui.accent(this))
                chip.setTextColor(Ui.accent(this))
            } else {
                chip.setTextColor(Ui.SUB)
            }
            chip.background = bg
            val clp = android.widget.LinearLayout.LayoutParams(0, Ui.dp(this, 56), 1f)
            if (i < modes.size - 1) clp.rightMargin = Ui.dp(this, 8)
            chip.layoutParams = clp
            chip.setOnClickListener {
                try {
                    audio.ringerMode = m
                    build()
                } catch (e: Exception) {
                    Ui.toast(this, "Não consegui mudar: ${e.message}")
                }
            }
            row.addView(chip)
        }
        col.addView(row)
        Ui.card(col, "🔕", "Não perturbe", "Configure exceções e horários de silêncio total", 1) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
        }

        Ui.section(col, "Volumes")
        streams.forEach { (label, emoji, stream) ->
            val max = audio.getStreamMaxVolume(stream)
            val cur = audio.getStreamVolume(stream)
            Ui.slider(col, label, emoji, max, cur) { v ->
                try {
                    audio.setStreamVolume(stream, v, 0)
                } catch (e: Exception) {
                    Ui.toast(this, "Não consegui ajustar: ${e.message}")
                }
            }
        }

        Ui.section(col, "Perfis salvos")
        val prefs = getSharedPreferences("sound_profiles", Context.MODE_PRIVATE)
        val profiles = JSONObject(prefs.getString("profiles", "{}"))
        val names = profiles.keys().asSequence().toList()
        if (names.isEmpty()) {
            Ui.note(col, "Nenhum perfil ainda. Ajuste os volumes acima e salve como perfil, por exemplo \"Trabalho\" ou \"Noite\".")
        }
        names.forEachIndexed { idx, name ->
            Ui.card(col, "🎚️", name, "Toque para aplicar • segure para apagar", idx) {
                applyProfile(profiles.getJSONObject(name))
                Ui.toast(this, "Perfil \"$name\" aplicado 🎧")
            }.setOnLongClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Apagar perfil")
                    .setMessage("Apagar o perfil \"$name\"?")
                    .setPositiveButton("Apagar") { _, _ ->
                        profiles.remove(name)
                        prefs.edit().putString("profiles", profiles.toString()).apply()
                        build()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
                true
            }
        }
        Ui.button(col, "💾 Salvar volumes atuais como perfil") {
            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            input.hint = "Nome do perfil"
            input.setTextColor(Ui.TEXT)
            input.setHintTextColor(Ui.SUB)
            AlertDialog.Builder(this)
                .setTitle("Novo perfil")
                .setView(input)
                .setPositiveButton("Salvar") { _, _ ->
                    val name = input.text.toString().trim()
                    if (name.isNotEmpty()) {
                        val o = JSONObject()
                        streams.forEach { (_, _, stream) ->
                            o.put(stream.toString(), audio.getStreamVolume(stream))
                        }
                        profiles.put(name, o)
                        prefs.edit().putString("profiles", profiles.toString()).apply()
                        build()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        Ui.note(col, "Volume individual por app e equalizador fino exigem APIs que só a fabricante libera; por isso ficam de fora do escopo sem root.")
    }

    private fun applyProfile(o: JSONObject) {
        streams.forEach { (_, _, stream) ->
            val key = stream.toString()
            if (o.has(key)) {
                try {
                    audio.setStreamVolume(stream, o.getInt(key), AudioManager.FLAG_SHOW_UI)
                } catch (_: Exception) {
                }
            }
        }
    }
}
