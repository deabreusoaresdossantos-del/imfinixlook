package com.imfinix.look

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class ControleTotalActivity : Activity() {

    override fun onResume() {
        super.onResume()
        build()
    }

    private fun level(): Int = when {
        ShizukuShell.hasSecureSettings(this) -> 3
        Settings.System.canWrite(this) -> 2
        else -> 1
    }

    private fun build() {
        val col = Ui.root(this)
        Ui.header(col, "Domínio Total", "Controle de verdade sobre o sistema - como o Good Lock faz na Samsung")

        val lvl = level()
        val lvlDesc = when (lvl) {
            3 -> "Nível 3 - acesso total às configurações do sistema (via Shizuku)"
            2 -> "Nível 2 - configurações básicas liberadas"
            else -> "Nível 1 - somente leitura"
        }
        Ui.card(col, if (lvl == 3) "👑" else "🔓", "Nível de acesso: $lvl/3", lvlDesc, 0) {}

        if (lvl < 2) {
            Ui.card(col, "🔑", "Liberar nível 2", "Permite controlar brilho, fonte, rotação e tempo de tela. Toque e ative a chave na tela que abrir.", 1) {
                startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
            }
        }
        if (lvl < 3) {
            Ui.card(col, "🚀", "Liberar nível 3 (Shizuku)", "O Shizuku concede a permissão WRITE_SECURE_SETTINGS, que libera animações, barra de status, modo imersivo e muito mais. Toque para configurar.", 2) {
                shizukuFlow()
            }
            Ui.note(col, "Sem PC? O Shizuku inicia sozinho pelo celular com a Depuração Wi-Fi (Opções do desenvolvedor). O app te guia passo a passo.")
        }

        if (lvl >= 2) {
            Ui.section(col, "Fonte e vibração")
            fontScale(col)
            val haptic = try { Settings.System.getInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED) == 1 } catch (e: Exception) { false }
            Ui.toggle(col, "📳", "Vibração ao tocar", "Resposta tátil do sistema inteiro", haptic) { on ->
                try {
                    Settings.System.putInt(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED, if (on) 1 else 0)
                } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
            }
        }

        if (lvl >= 3) {
            Ui.section(col, "Velocidade (escala de animação)")
            animScale(col)

            Ui.section(col, "Barra de status")
            statusBarIcons(col)

            Ui.section(col, "Notificações")
            headsUp(col)

            Ui.section(col, "Modo imersivo")
            immersive(col)
        } else {
            Ui.section(col, "O que o nível 3 libera")
            Ui.note(col, "Velocidade das animações (0,5x deixa o celular voando), esconder ícones da barra de status, notificações heads-up, modo imersivo de tela cheia. Tudo reversível com um toque.")
        }
    }

    private fun shizukuFlow() {
        if (!ShizukuShell.installed(this)) {
            AlertDialog.Builder(this)
                .setTitle("Instalar o Shizuku")
                .setMessage("O Shizuku é a ponte segura que permite liberar as permissões profundas sem root.\n\n1. Instale o Shizuku (vou abrir a Play Store)\n2. Ative a Depuração Wi-Fi nas Opções do Desenvolvedor\n3. Abra o Shizuku e toque em \"Iniciar via depuração Wi-Fi\"\n4. Volte aqui e toque neste cartão de novo")
                .setPositiveButton("Abrir Play Store") { _, _ ->
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${ShizukuShell.PKG}")))
                    } catch (e: Exception) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${ShizukuShell.PKG}")))
                    }
                }
                .setNegativeButton("Depois", null)
                .show()
            return
        }
        if (!ShizukuShell.binderAlive()) {
            AlertDialog.Builder(this)
                .setTitle("Shizuku parado")
                .setMessage("O Shizuku está instalado mas não está rodando. Abra o Shizuku e toque em Iniciar (via depuração Wi-Fi). Depois volte aqui.")
                .setPositiveButton("Abrir Shizuku") { _, _ ->
                    try { startActivity(packageManager.getLaunchIntentForPackage(ShizukuShell.PKG)) } catch (_: Exception) {}
                }
                .setNegativeButton("Depois", null)
                .show()
            return
        }
        if (!ShizukuShell.permissionGranted()) {
            ShizukuShell.requestPermission()
            Ui.toast(this, "Confirma o acesso na janela do Shizuku e volta aqui")
            return
        }
        val ok = ShizukuShell.grantSecureSettings(this)
        if (ok) {
            Ui.toast(this, "Nível 3 liberado 👑")
            build()
        } else {
            Ui.toast(this, "Não consegui liberar. Reinicia o Shizuku e tenta de novo.")
        }
    }

    private fun fontScale(col: LinearLayout) {
        val cur = try { Settings.System.getFloat(contentResolver, Settings.System.FONT_SCALE) } catch (e: Exception) { 1f }
        val options = listOf(0.85f to "Pequena", 1.0f to "Padrão", 1.15f to "Grande", 1.3f to "Gigante")
        chipRow(col, options.map { it.second }, options.indexOfFirst { Math.abs(it.first - cur) < 0.03f }.coerceAtLeast(0)) { i ->
            try {
                Settings.System.putFloat(contentResolver, Settings.System.FONT_SCALE, options[i].first)
                Ui.toast(this, "Fonte: ${options[i].second} 🔤")
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
        Ui.note(col, "Tamanho da fonte do sistema inteiro.")
    }

    private fun animScale(col: LinearLayout) {
        val keys = listOf("window_animation_scale", "transition_animation_scale", "animator_duration_scale")
        val cur = try { Settings.Global.getFloat(contentResolver, keys[0]) } catch (e: Exception) { 1f }
        val options = listOf(0.5f to "0,5x ⚡", 1.0f to "1x", 1.5f to "1,5x", 0.0f to "Off 🚀")
        chipRow(col, options.map { it.second }, options.indexOfFirst { Math.abs(it.first - cur) < 0.03f }.coerceAtLeast(0)) { i ->
            try {
                keys.forEach { k -> Settings.Global.putFloat(contentResolver, k, options[i].first) }
                Ui.toast(this, "Animações: ${options[i].second}")
                build()
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
        Ui.note(col, "0,5x deixa tudo mais rápido sem perder as animações. Off remove tudo (máxima velocidade).")
    }

    private fun statusBarIcons(col: LinearLayout) {
        val slots = listOf(
            "alarm_clock" to "⏰ Despertador",
            "bluetooth" to "🅱️ Bluetooth",
            "nfc" to "📡 NFC",
            "hotspot" to "📶 Hotspot",
            "airplane" to "✈️ Modo avião",
            "data_saver" to "📉 Economia de dados"
        )
        val hidden = (try { Settings.Secure.getString(contentResolver, "icon_blacklist") } catch (e: Exception) { "" } ?: "").split(",").toMutableSet()
        slots.forEach { (slot, name) ->
            Ui.toggle(col, "🙈", name, if (slot in hidden) "Escondido da barra de status" else "Visível na barra de status", slot in hidden) { on ->
                try {
                    if (on) hidden.add(slot) else hidden.remove(slot)
                    Settings.Secure.putString(contentResolver, "icon_blacklist", hidden.filter { it.isNotEmpty() }.joinToString(","))
                } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
            }
        }
    }

    private fun headsUp(col: LinearLayout) {
        val cur = try { Settings.Global.getInt(contentResolver, "heads_up_notifications_enabled") == 1 } catch (e: Exception) { true }
        Ui.toggle(col, "💬", "Notificações pop-up (heads-up)", "Banners flutuantes no topo da tela quando chega notificação", cur) { on ->
            try {
                Settings.Global.putInt(contentResolver, "heads_up_notifications_enabled", if (on) 1 else 0)
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
    }

    private fun immersive(col: LinearLayout) {
        val cur = try { Settings.Global.getString(contentResolver, "policy_control") } catch (e: Exception) { "" } ?: ""
        val options = listOf("immersive.status=*" to "Só status", "immersive.full=*" to "Tela cheia total")
        Ui.card(col, "🖼️", "Modo imersivo", if (cur.isEmpty()) "Desligado - barras normais" else "Ativo: $cur", 3) {}
        val labels = listOf("Desligado") + options.map { it.second }
        val selected = if (cur.isEmpty()) 0 else options.indexOfFirst { cur.contains(it.first.split("=")[0]) } + 1
        chipRow(col, labels, selected.coerceIn(0, labels.size - 1)) { i ->
            try {
                if (i == 0) Settings.Global.putString(contentResolver, "policy_control", "")
                else Settings.Global.putString(contentResolver, "policy_control", options[i - 1].first)
                Ui.toast(this, "Imersivo: ${labels[i]}")
                build()
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
        Ui.note(col, "⚠️ Tela cheia total esconde a barra de navegação - só use com navegação por botões. Desligue aqui se se perder.")
    }

    private fun chipRow(col: LinearLayout, labels: List<String>, selected: Int, onPick: (Int) -> Unit) {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        val rlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        rlp.bottomMargin = Ui.dp(this, 10)
        row.layoutParams = rlp
        labels.forEachIndexed { i, label ->
            val chip = TextView(this)
            chip.text = label
            chip.textSize = 13f
            chip.gravity = Gravity.CENTER
            val bg = Ui.cardBg(this, 12f)
            if (i == selected) {
                bg.setStroke(Ui.dp(this, 2), Ui.accent(this))
                chip.setTextColor(Ui.accent(this))
            } else {
                chip.setTextColor(Ui.SUB)
            }
            chip.background = bg
            val clp = LinearLayout.LayoutParams(0, Ui.dp(this, 42), 1f)
            if (i < labels.size - 1) clp.rightMargin = Ui.dp(this, 6)
            chip.layoutParams = clp
            chip.setOnClickListener { onPick(i) }
            row.addView(chip)
        }
        col.addView(row)
    }
}
