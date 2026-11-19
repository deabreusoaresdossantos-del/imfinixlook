package com.imfinix.look

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.LinearLayout

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
            Ui.card(col, "🚀", "Liberar nível 3 (Shizuku)", "O Shizuku concede a permissão WRITE_SECURE_SETTINGS, que libera animações, barra de status, DPI, taxa de atualização e muito mais. Toque para configurar.", 2) {
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

            Ui.section(col, "Tempo para apagar a tela")
            screenTimeout(col)
        }

        if (lvl >= 3) {
            Ui.section(col, "Velocidade (escala de animação)")
            animScale(col)
            animScaleIndividual(col)

            Ui.section(col, "Tamanho da tela (DPI)")
            dpiControl(col)

            Ui.section(col, "Taxa de atualização")
            refreshRate(col)

            Ui.section(col, "Barra de status")
            statusBarIcons(col)
            batteryPercent(col)

            Ui.section(col, "Notificações")
            headsUp(col)

            Ui.section(col, "Energia")
            batterySaver(col)

            Ui.section(col, "Modo imersivo")
            immersive(col)
        } else {
            Ui.section(col, "O que o nível 3 libera")
            Ui.note(col, "Velocidade das animações (0,5x deixa o celular voando), DPI da tela, taxa de atualização, esconder relógio/bateria/sinal da barra de status, porcentagem da bateria, heads-up, economia de energia e modo imersivo. Tudo reversível com um toque.")
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
        Ui.chips(col, options.map { it.second }, options.indexOfFirst { Math.abs(it.first - cur) < 0.03f }.coerceAtLeast(0)) { i ->
            try {
                Settings.System.putFloat(contentResolver, Settings.System.FONT_SCALE, options[i].first)
                Ui.toast(this, "Fonte: ${options[i].second} 🔤")
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
        Ui.note(col, "Tamanho da fonte do sistema inteiro.")
    }

    private fun screenTimeout(col: LinearLayout) {
        val cur = try { Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT) } catch (e: Exception) { 30000 }
        val options = listOf(15000 to "15s", 30000 to "30s", 60000 to "1 min", 120000 to "2 min", 300000 to "5 min", 600000 to "10 min")
        Ui.chips(col, options.map { it.second }, options.indexOfFirst { it.first == cur }.coerceAtLeast(0)) { i ->
            try {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, options[i].first)
                Ui.toast(this, "Tela apaga em ${options[i].second} ⏲️")
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
    }

    private fun animScale(col: LinearLayout) {
        val keys = listOf("window_animation_scale", "transition_animation_scale", "animator_duration_scale")
        val cur = try { Settings.Global.getFloat(contentResolver, keys[0]) } catch (e: Exception) { 1f }
        val options = listOf(0.5f to "0,5x ⚡", 1.0f to "1x", 1.5f to "1,5x", 0.0f to "Off 🚀")
        Ui.chips(col, options.map { it.second }, options.indexOfFirst { Math.abs(it.first - cur) < 0.03f }.coerceAtLeast(0)) { i ->
            try {
                keys.forEach { k -> Settings.Global.putFloat(contentResolver, k, options[i].first) }
                Ui.toast(this, "Animações: ${options[i].second}")
                build()
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
        Ui.note(col, "Preset rápido: aplica nas 3 animações ao mesmo tempo. 0,5x deixa tudo mais rápido; Off remove tudo.")
    }

    private fun animScaleIndividual(col: LinearLayout) {
        val items = listOf(
            Triple("🪟 Janelas", "window_animation_scale", "Abrir e fechar janelas e menus"),
            Triple("🔀 Transições", "transition_animation_scale", "Passar de uma tela pra outra"),
            Triple("⏱️ Duração", "animator_duration_scale", "Animações internas dos apps")
        )
        val options = listOf(0f to "Off", 0.5f to "0,5x", 1.0f to "1x", 1.5f to "1,5x", 2f to "2x")
        items.forEach { (name, key, desc) ->
            val cur = try { Settings.Global.getFloat(contentResolver, key) } catch (e: Exception) { 1f }
            Ui.card(col, name, "Ajuste fino", desc + " - agora: " + (options.firstOrNull { Math.abs(it.first - cur) < 0.03f }?.second ?: "${cur}x"), -1) {}
            Ui.chips(col, options.map { it.second }, options.indexOfFirst { Math.abs(it.first - cur) < 0.03f }.coerceAtLeast(0)) { i ->
                try {
                    Settings.Global.putFloat(contentResolver, key, options[i].first)
                    Ui.toast(this, "$name: ${options[i].second}")
                    build()
                } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
            }
        }
    }

    private fun dpiControl(col: LinearLayout) {
        val (code, out) = ShizukuShell.run("wm density")
        val base = Regex("(\\d+)").findAll(out).lastOrNull()?.value?.toIntOrNull() ?: 280
        Ui.card(col, "🔍", "Densidade da tela", "DPI atual: $base. DPI maior = interface maior; DPI menor = cabe mais coisa na tela (estilo tablet).", -1) {}
        val options = listOf(
            (base - 60) to "Compacto",
            base to "Padrão",
            (base + 40) to "Grande",
            (base + 80) to "Gigante"
        )
        Ui.chips(col, options.map { it.second }, 1) { i ->
            val (c2, o2) = ShizukuShell.run("wm density ${options[i].first}")
            if (c2 == 0) {
                Ui.toast(this, "DPI: ${options[i].second} (${options[i].first}) 🔍")
            } else {
                Ui.toast(this, "Falhou: $o2")
            }
        }
        Ui.button(col, "↩️ Restaurar DPI original", primary = false) {
            val (c3, o3) = ShizukuShell.run("wm density reset")
            Ui.toast(this, if (c3 == 0) "DPI restaurado ✅" else "Falhou: $o3")
        }
        Ui.note(col, "Funciona via Shizuku. Se algo ficar estranho, toque em Restaurar. A mudança é instantânea e reversível.")
    }

    private fun refreshRate(col: LinearLayout) {
        val curPeak = try { Settings.System.getFloat(contentResolver, "peak_refresh_rate") } catch (e: Exception) { 0f }
        val options = listOf(
            Triple("Automático", 0f, 0f),
            Triple("60 Hz 🔋", 60f, 60f),
            Triple("90 Hz ⚡", 90f, 90f)
        )
        val selected = when {
            curPeak <= 1f -> 0
            curPeak < 75f -> 1
            else -> 2
        }
        Ui.chips(col, options.map { it.first }, selected) { i ->
            try {
                Settings.System.putFloat(contentResolver, "peak_refresh_rate", options[i].second)
                Settings.System.putFloat(contentResolver, "min_refresh_rate", options[i].third)
                Ui.toast(this, "Taxa: ${options[i].first}")
                build()
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
        Ui.note(col, "60 Hz economiza bateria; 90 Hz deixa a rolagem mais lisa. Se o XOS ignorar, ajuste em Tela nas configurações do aparelho.")
    }

    private fun statusBarIcons(col: LinearLayout) {
        val slots = listOf(
            "clock" to "🕐 Relógio",
            "battery" to "🔋 Bateria",
            "wifi" to "📶 Wi-Fi",
            "mobile" to "📡 Sinal",
            "volte" to "📞 VoLTE",
            "alarm_clock" to "⏰ Despertador",
            "bluetooth" to "🅱️ Bluetooth",
            "nfc" to "💳 NFC",
            "hotspot" to "📡 Hotspot",
            "airplane" to "✈️ Modo avião",
            "data_saver" to "📉 Econ. dados",
            "zen" to "🌙 Não perturbe",
            "headset" to "🎧 Fone",
            "cast" to "📺 Cast",
            "rotate" to "🔄 Rotação"
        )
        val hidden = (try { Settings.Secure.getString(contentResolver, "icon_blacklist") } catch (e: Exception) { "" } ?: "").split(",").toMutableSet()
        Ui.card(col, "🙈", "Esconder ícones", "Toque para esconder ou mostrar cada ícone da barra de status. Esconder o relógio é o clássico visual limpo.", -1) {}
        val labels = slots.map { if (it.first in hidden) "🚫 ${it.second}" else it.second }
        var i = 0
        while (i < slots.size) {
            val row = slots.subList(i, minOf(i + 3, slots.size))
            Ui.chips(col, labels.subList(i, minOf(i + 3, labels.size)), -1) { j ->
                val slot = row[j].first
                try {
                    if (slot in hidden) hidden.remove(slot) else hidden.add(slot)
                    Settings.Secure.putString(contentResolver, "icon_blacklist", hidden.filter { it.isNotEmpty() }.joinToString(","))
                    build()
                } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
            }
            i += 3
        }
        Ui.note(col, "🚫 = escondido. Alguns ícones só somem quando estariam visíveis (ex.: despertador com alarme ativo).")
    }

    private fun batteryPercent(col: LinearLayout) {
        val cur = try { Settings.System.getInt(contentResolver, "status_bar_show_battery_percent") == 1 } catch (e: Exception) { false }
        Ui.toggle(col, "🔢", "Porcentagem da bateria", "Mostra o número ao lado do ícone da bateria", cur) { on ->
            try {
                Settings.System.putInt(contentResolver, "status_bar_show_battery_percent", if (on) 1 else 0)
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
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

    private fun batterySaver(col: LinearLayout) {
        val saver = try { Settings.Global.getInt(contentResolver, "low_power") == 1 } catch (e: Exception) { false }
        Ui.toggle(col, "🪫", "Economia de energia", "Liga o modo de economia do sistema (reduz fundo e vibração)", saver) { on ->
            try {
                Settings.Global.putInt(contentResolver, "low_power", if (on) 1 else 0)
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
        val auto = try { Settings.Global.getInt(contentResolver, "low_power_trigger_level") } catch (e: Exception) { 0 }
        val options = listOf(0 to "Nunca", 15 to "Aos 15%", 30 to "Aos 30%", 50 to "Aos 50%")
        Ui.note(col, "Economia automática quando a bateria chegar em:")
        Ui.chips(col, options.map { it.second }, options.indexOfFirst { it.first == auto }.coerceAtLeast(0)) { i ->
            try {
                Settings.Global.putInt(contentResolver, "low_power_trigger_level", options[i].first)
                Ui.toast(this, "Economia automática: ${options[i].second} 🪫")
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
    }

    private fun immersive(col: LinearLayout) {
        val cur = try { Settings.Global.getString(contentResolver, "policy_control") } catch (e: Exception) { "" } ?: ""
        val options = listOf("immersive.status=*" to "Só status", "immersive.full=*" to "Tela cheia total")
        Ui.card(col, "🖼️", "Modo imersivo", if (cur.isEmpty()) "Desligado - barras normais" else "Ativo: $cur", 3) {}
        val labels = listOf("Desligado") + options.map { it.second }
        val selected = if (cur.isEmpty()) 0 else options.indexOfFirst { cur.contains(it.first.split("=")[0]) } + 1
        Ui.chips(col, labels, selected.coerceIn(0, labels.size - 1)) { i ->
            try {
                if (i == 0) Settings.Global.putString(contentResolver, "policy_control", "")
                else Settings.Global.putString(contentResolver, "policy_control", options[i - 1].first)
                Ui.toast(this, "Imersivo: ${labels[i]}")
                build()
            } catch (e: Exception) { Ui.toast(this, "Falhou: ${e.message}") }
        }
        Ui.note(col, "⚠️ Tela cheia total esconde a barra de navegação - só use com navegação por botões. Desligue aqui se se perder.")
    }
}
