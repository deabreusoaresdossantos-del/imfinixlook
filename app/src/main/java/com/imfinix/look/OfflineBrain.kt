package com.imfinix.look

object OfflineBrain {

    private data class Intent(val keys: List<String>, val answer: String)

    private val intents = listOf(
        Intent(listOf("profundidade", "profundo", "opo", "oppo", "recorte", "sujeito"),
            "O modo Profundidade recorta a pessoa da sua foto e coloca o relógio ATRÁS dela, estilo Oppo/iPhone. Vai em Profundidade no menu, escolhe a foto, o estilo do relógio (são 11), a fonte (30 opções) e a cor. Depois define como papel de parede animado."),
        Intent(listOf("liquid", "vidro", "glass", "ícone", "icone"),
            "O Liquid Glass deixa ícones e widgets com cara de vidro translúcido. O launcher do Infinix não aceita pacote de ícones, então o app cria ATALHOS de vidro na tela inicial. Vai em Liquid Glass no menu pra montar os seus."),
        Intent(listOf("shizuku", "domínio", "dominio", "permissão", "permissao", "nível 3", "nivel 3"),
            "O Domínio Total tem 3 níveis. O nível 3 (o poderoso) usa o Shizuku: instala o Shizuku da Play Store, ativa a Depuração Wi-Fi nas Opções do Desenvolvedor, abre o Shizuku e toca em Iniciar. Depois volta no app e toca em Liberar nível 3. Não precisa de PC nem root."),
        Intent(listOf("dpi", "densidade", "tamanho da tela", "compacto"),
            "No Domínio Total (nível 3) tem o controle de DPI: Compacto faz caber mais coisa na tela (cara de tablet), Grande deixa tudo maior. Se estragar algo, toca em Restaurar DPI original que volta na hora."),
        Intent(listOf("90", "hz", "hertz", "taxa", "rolagem", "60"),
            "No Domínio Total (nível 3) tem Taxa de atualização: 90 Hz deixa a rolagem mais lisa, 60 Hz economiza bateria. Se o XOS não obedecer, ajusta em Configurações > Tela."),
        Intent(listOf("status bar", "barra de status", "relógio da barra", "esconder", "ícones da barra"),
            "No Domínio Total (nível 3) dá pra esconder o que quiser da barra de status: relógio, bateria, Wi-Fi, sinal, alarme... É o clássico visual limpo. Também dá pra ativar a porcentagem da bateria."),
        Intent(listOf("animação", "animacao", "velocidade", "rápido", "rapido", "lento", "travando"),
            "No Domínio Total (nível 3) tem a escala de animação. 0,5x deixa o celular voando sem perder as animações; Off dá velocidade máxima. Agora dá pra ajustar janelas, transições e duração separado."),
        Intent(listOf("wallpaper", "papel de parede", "fundo", "parede"),
            "O Estúdio de Wallpapers tem 16 estilos gerados no aparelho (Aurora, Retrô 84, Dunas, Circuito...), cada um na resolução exata da sua tela. Tem ainda a Foto Difusa, que transforma qualquer foto sua num fundo desfocado elegante, e o Wallpaper do dia, que troca sozinho toda manhã."),
        Intent(listOf("foto difusa", "difusa", "minha foto"),
            "No Estúdio de Wallpapers, escolhe o estilo Foto Difusa e toca em Trocar a foto. O app desfoca a imagem e escurece na medida certa pra combinar com relógio e widgets. A foto fica só no seu celular."),
        Intent(listOf("wallpaper do dia", "automático", "automatico", "troca sozinho", "todo dia"),
            "No Estúdio de Wallpapers tem a chave Wallpaper do dia: ligando, todo dia de manhã o estilo que você escolheu ganha uma variação nova sozinho. Funciona mesmo depois de reiniciar o celular."),
        Intent(listOf("fonte", "letra", "tipografia"),
            "São 30 fontes embutidas no app. Elas valem pro relógio do modo Profundidade: abre o Profundidade e mexe no seletor de fonte. A fonte do sistema inteiro muda no Domínio Total (escala), mas o estilo da fonte do sistema o XOS limita."),
        Intent(listOf("cor", "tema", "paleta"),
            "Em Temas você troca a paleta do app inteiro (Índigo, Esmeralda, Âmbar, Rosa, Ciano, Rubi). Os estilos de wallpaper Fluxo, Ondas, Mesh e outros usam a cor do tema que você escolher."),
        Intent(listOf("relógio", "relogio", "clock", "widget"),
            "Em Relógios tem 11 estilos pro Profundidade, mais os widgets de relógio (digital, analógico, fino, de vidro...) que você adiciona na tela inicial segurando um espaço vazio > Widgets > ImfinixLook."),
        Intent(listOf("bateria", "economia", "carga"),
            "No Domínio Total (nível 3) tem a seção Energia: liga a economia na hora ou programa pra ligar sozinha aos 15/30/50%. Combinar com 60 Hz estica bastante a bateria."),
        Intent(listOf("gesto", "borda", "alça", "alca"),
            "Em Gestos tem a alça de borda: uma barrinha flutuante na lateral que abre seus apps favoritos com uma mão só. Precisa da permissão de sobreposição, o app te leva na tela certa."),
        Intent(listOf("notificação", "notificacao", "histórico", "historico"),
            "Em Notificações fica o histórico completo: tudo que chegou, mesmo o que você apagou sem querer. Precisa autorizar o acesso a notificações uma vez."),
        Intent(listOf("imersivo", "tela cheia", "fullscreen"),
            "O modo imersivo (Domínio Total, nível 3) esconde a barra de status ou tudo. Cuidado: tela cheia total esconde a navegação também - use só com navegação por gestos. Desliga no mesmo lugar se se perder."),
        Intent(listOf("atualizar", "atualização", "atualizacao", "versão", "versao", "nova versão"),
            "As versões novas saem no GitHub: github.com/deabreusoaresdossantos-del/imfinixlook na aba Releases. Baixa o APK mais recente e instala por cima - suas configurações ficam salvas."),
        Intent(listOf("crash", "fechando", "travando", "parou", "erro"),
            "Se o app fechar sozinho, me conta qual tela você abriu antes que eu caço o bug. Dica rápida: em Configurações > Apps > ImfinixLook > Limpar cache resolve a maioria dos travamentos sem perder suas chaves."),
        Intent(listOf("claude", "anthropic"),
            "Pra usar o Claude aqui no chat: toca em Configurar, cola sua chave da Anthropic (cria em console.anthropic.com > API Keys) e salva. O uso é cobrado na sua conta da Anthropic. A chave fica salva só no seu celular."),
        Intent(listOf("perplexity", "perplex"),
            "Pra usar o Perplexity: toca em Configurar, cola sua chave (cria em console.perplexity.ai > API) e salva. O Perplexity pesquisa na web antes de responder, então é ótimo pra coisas atuais. O uso é cobrado na sua conta."),
        Intent(listOf("chave", "api", "key", "token"),
            "Cada provedor de IA usa sua própria chave de API: Claude em console.anthropic.com, Perplexity em console.perplexity.ai. Cola a chave em Configurar aqui no chat. Ela fica salva só no seu aparelho e o consumo é cobrado na SUA conta do serviço. O modo Offline não precisa de chave nenhuma."),
        Intent(listOf("offline", "sem internet", "local"),
            "Sou eu! O modo Offline (eu) roda 100% no seu celular: sem internet, sem chave, sem custo. Eu sei tudo sobre o ImfinixLook e dou dicas de XOS/Android. Pra respostas mais elaboradas, use o Claude ou o Perplexity."),
        Intent(listOf("obrigado", "valeu", "brigado", "obg"),
            "Tamo junto! Qualquer coisa é só chamar 😄"),
        Intent(listOf("oi", "olá", "ola", "eai", "e ai", "salve", "bom dia", "boa tarde", "boa noite"),
            "Salve! Sou o assistente do ImfinixLook. Pergunta sobre os módulos do app (Profundidade, Domínio Total, Wallpapers...) ou pede dicas de personalização do seu Infinix.")
    )

    fun reply(question: String): String {
        val q = question.lowercase()
        var best: Intent? = null
        var bestScore = 0
        intents.forEach { intent ->
            val score = intent.keys.count { k -> q.contains(k) }
            if (score > bestScore) {
                bestScore = score
                best = intent
            }
        }
        best?.let { return it.answer }
        return "Ainda não sei responder essa (sou o modo Offline, minha base é fixa 😅). Eu mando bem em: módulos do app (Profundidade, Domínio Total, Wallpapers, Liquid Glass...), Shizuku, DPI, taxa de atualização, bateria e dicas de XOS. Pra qualquer outro assunto, configura o Claude ou o Perplexity aqui em cima."
    }
}
