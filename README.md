# ImfinixLook

Hub completo de personalização estilo Good Lock, feito para Infinix com XOS (Android 14). Projeto 100% original: nenhum código ou asset da Samsung foi usado, a inspiração é apenas conceitual. Kotlin puro, sem dependências externas, minSdk 26 / targetSdk 34.

## Módulos

| Módulo ImfinixLook | Inspiração (Good Lock) | O que faz de verdade | Limite sem root |
|---|---|---|---|
| Estúdio de Wallpapers | Wonderland | Gera wallpapers (Aurora, Oceano, Pôr do Sol, Neon, Minimal, Galáxia) na resolução exata da tela e aplica na home, lockscreen ou ambas | Wallpapers animados exigem live wallpaper (roadmap) |
| Tela de Bloqueio | LockStar | Wallpaper exclusivo da lockscreen + atalhos para bloqueio, biometria e notificações na lockscreen | Reposicionar relógio/layout da lockscreen exige root/Shizuku |
| Relógios | ClockFace | 5 widgets (Gigante, Minimal, Neon, Digital Data, Analógico) com prévia ao vivo e fixação em 1 toque + Relógio de Mesa (tela cheia) | Faces para lockscreen/Always-On exigem root |
| Temas | Theme Park | Paleta de cor que repinta o app inteiro na hora (6 opções) + combinações de wallpaper/ícones/fonte | Temas do sistema inteiro exigem loja do XOS ou root |
| Ajustes Rápidos | QuickStar | Controle real de brilho, tempo de tela e rotação (com permissão de escrita) + 12 atalhos do sistema | Editar tiles do painel rápido exige root/Shizuku |
| Painel Rápido | RegiStar | 12 configurações escondidas: privacidade, acessos especiais, uso, DND, desenvolvedor, apps padrão e mais | - |
| Perfis de Som | SoundAssistant | Sliders de todos os volumes + modo de toque + perfis salvos (Trabalho, Noite...) aplicados com 1 toque | Volume por app e equalizador exigem APIs da fabricante |
| Gestos | One Hand Operation+ | Alça de Borda: pílula flutuante em qualquer tela que abre seus apps com o polegar + abridor de apps | Gestos de borda avançados exigem acessibilidade/root |
| Notificações | NotiStar | Histórico funcional via NotificationListenerService, com ícone do app, horário e limpeza | - |
| Dicas XOS | - | 15 truques de personalização, gestos, bateria e sistema | - |

## Changelog

### 2.1.0
- Alça de Borda agora roda como serviço em primeiro plano (não é mais morta pelo sistema) com notificação discreta para configurar/desativar
- Tema aplicado na home ao voltar do módulo Temas
- Cache de wallpapers (estilos abrem instantâneo)
- Toque em uma notificação do histórico abre o app de origem
- Perfis de som mostram os níveis salvos no cartão
- Resposta tátil (vibração) ao tocar nos cartões
- Correção: callback de fixação de widget agora aponta para o provider certo

### 2.0.0
- Rebuild completo: novo design system, Estúdio de Wallpapers, 5 relógios, temas de cor, controles reais de tela, perfis de som, Alça de Borda, NotiStar turbinado, 15 dicas XOS

## Build

CI: o GitHub Actions compila o APK a cada push e publica a release automaticamente a cada tag `v*` (`.github/workflows/build.yml`).

Build local:

```bash
gradle assembleDebug
# APK em app/build/outputs/apk/debug/app-debug.apk
```

Requer JDK 17 e Android SDK 34.

## Instalação no celular

1. Baixe o APK da aba [Releases](https://github.com/deabreusoaresdossantos-del/imfinixlook/releases).
2. Permita "instalar apps de fontes desconhecidas" para o navegador/gerenciador de arquivos.
3. Abra o APK e instale.
4. Libere as permissões opcionais dentro do app, conforme o módulo:
   - **Notificações**: acesso a notificações (para o histórico)
   - **Ajustes Rápidos**: modificar configurações do sistema (para brilho/tempo de tela/rotação)
   - **Gestos**: sobrepor a outros apps (para a Alça de Borda)

## Detalhes

- Package: `com.imfinix.look`
- Kotlin, somente framework Android (sem AndroidX, sem libs)
- Permissões: SET_WALLPAPER e SYSTEM_ALERT_WINDOW (somente para a Alça de Borda)
- Dados: tudo local (SharedPreferences); nenhum dado sai do aparelho
- Licença: MIT
