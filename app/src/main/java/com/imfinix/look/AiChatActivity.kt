package com.imfinix.look

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class AiChatActivity : Activity() {

    companion object {
        private val history = mutableListOf<Pair<String, String>>()
    }

    private var sending = false

    private fun prefs() = getSharedPreferences("imfinix_ai", MODE_PRIVATE)
    private fun provider(): String = prefs().getString("provider", "offline") ?: "offline"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        build()
    }

    private fun keyFor(p: String): String = prefs().getString("${p}_key", "") ?: ""
    private fun modelFor(p: String): String = prefs().getString("${p}_model", "") ?: ""

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Assistente IA", "Converse com Claude, Perplexity ou o assistente offline do app")

        val prov = provider()
        Ui.section(col, "Provedor")
        val providers = listOf(
            Triple("offline", "🏠 Offline", "Sem internet, sem chave"),
            Triple("claude", "🟣 Claude", "Anthropic"),
            Triple("perplexity", "🌀 Perplexity", "Com busca web")
        )
        Ui.chips(col, providers.map { it.second }, providers.indexOfFirst { it.first == prov }.coerceAtLeast(0)) { i ->
            prefs().edit().putString("provider", providers[i].first).apply()
            build()
        }
        Ui.note(col, providers.first { it.first == prov }.third)

        if (prov != "offline") {
            configSection(col, prov)
        } else {
            Ui.card(col, "🏠", "Modo Offline", "Respostas instantâneas da base local do ImfinixLook: módulos do app, Shizuku, XOS e dicas. Não usa internet e não custa nada.", 0) {}
        }

        Ui.section(col, "Conversa")
        if (history.isEmpty()) {
            Ui.note(col, if (prov == "offline") "Pergunta algo sobre o app ou sobre personalização do seu Infinix."
            else "Manda a primeira mensagem. O uso é cobrado na sua conta do provedor.")
        }
        history.forEachIndexed { idx, (role, text) -> bubble(col, role == "user", text, idx) }
        if (sending) {
            Ui.note(col, "⏳ Pensando...")
        }

        inputRow(col, prov)
    }

    private fun configSection(col: LinearLayout, prov: String) {
        val nome = if (prov == "claude") "Claude" else "Perplexity"
        val hasKey = keyFor(prov).isNotEmpty()
        if (hasKey) {
            Ui.card(col, "🔑", "Chave do $nome configurada", "Modelo: ${modelFor(prov).ifEmpty { if (prov == "claude") "claude-sonnet-5-5" else "sonar" }}. Toque para trocar a chave ou o modelo.", 0) {
                prefs().edit().putString("${prov}_key", "").apply()
                build()
            }
            return
        }
        Ui.card(col, "🔑", "Configurar $nome", if (prov == "claude")
            "Crie sua chave em console.anthropic.com (seção API Keys) e cole aqui."
        else
            "Crie sua chave em console.perplexity.ai (seção API) e cole aqui.", 0) {}
        Ui.note(col, "A chave fica salva só no seu celular. Cada resposta consome créditos da SUA conta no serviço.")

        val keyField = field("Cola a chave de API aqui", password = true)
        col.addView(keyField)
        val modelField = field("Modelo (opcional) - padrão: ${if (prov == "claude") "claude-sonnet-5-5" else "sonar"}", password = false)
        col.addView(modelField)
        Ui.button(col, "💾 Salvar chave") {
            val k = keyField.text.toString().trim()
            if (k.isEmpty()) {
                Ui.toast(this, "Cola a chave primeiro")
            } else {
                prefs().edit()
                    .putString("${prov}_key", k)
                    .putString("${prov}_model", modelField.text.toString().trim())
                    .apply()
                Ui.toast(this, "Chave salva ✅")
                build()
            }
        }
    }

    private fun field(hint: String, password: Boolean): EditText {
        val e = EditText(this)
        e.hint = hint
        e.setHintTextColor(Ui.SUB)
        e.setTextColor(Ui.TEXT)
        e.textSize = 14f
        e.setSingleLine(true)
        if (password) e.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        e.background = Ui.cardBg(this, 14f)
        val pad = Ui.dp(this, 14)
        e.setPadding(pad, pad, pad, pad)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.bottomMargin = Ui.dp(this, 10)
        e.layoutParams = lp
        return e
    }

    private fun bubble(col: LinearLayout, mine: Boolean, text: String, idx: Int) {
        val t = TextView(this)
        t.text = text
        t.textSize = 14f
        t.setTextColor(if (mine) Color.WHITE else Ui.TEXT)
        val bg = Ui.cardBg(this, 18f)
        if (mine) bg.setColor(Ui.blend(Ui.accent(this), Ui.CARD, 0.65f))
        t.background = bg
        val pad = Ui.dp(this, 14)
        t.setPadding(pad, Ui.dp(this, 10), pad, Ui.dp(this, 10))
        val lp = LinearLayout.LayoutParams(
            if (mine) ViewGroup.LayoutParams.WRAP_CONTENT else ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = if (mine) Gravity.END else Gravity.START
        lp.bottomMargin = Ui.dp(this, 8)
        if (mine) lp.leftMargin = Ui.dp(this, 48) else lp.rightMargin = Ui.dp(this, 8)
        t.layoutParams = lp
        t.setTextIsSelectable(true)
        col.addView(t)
        Ui.animIn(t, idx % 6)
    }

    private fun inputRow(col: LinearLayout, prov: String) {
        val blocked = prov != "offline" && keyFor(prov).isEmpty()
        val e = field(if (blocked) "Configura a chave acima pra conversar" else "Escreve sua mensagem...", password = false)
        e.isEnabled = !blocked
        e.imeOptions = EditorInfo.IME_ACTION_SEND
        e.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) trySend(e.text.toString(), prov)
            true
        }
        Ui.button(col, "📤 Enviar") {
            trySend(e.text.toString(), prov)
        }
    }

    private fun trySend(text: String, prov: String) {
        val msg = text.trim()
        if (msg.isEmpty() || sending) return
        if (prov != "offline" && keyFor(prov).isEmpty()) {
            Ui.toast(this, "Configura a chave do provedor primeiro")
            return
        }
        history.add("user" to msg)
        sending = true
        build()
        Thread {
            val reply = when (prov) {
                "claude" -> AiApi.claude(keyFor(prov), modelFor(prov), history.toList())
                "perplexity" -> AiApi.perplexity(keyFor(prov), modelFor(prov), history.toList())
                else -> AiApi.Reply(true, OfflineBrain.reply(msg))
            }
            runOnUiThread {
                sending = false
                history.add("assistant" to reply.text)
                build()
            }
        }.start()
    }
}
