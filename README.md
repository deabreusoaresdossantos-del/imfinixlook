# ImfinixLook

Hub de personalização estilo Good Lock, feito para Infinix com XOS (Android 14). Projeto original: nenhum código ou asset da Samsung foi usado. Testado para compilar no Android 14 (API 34), minSdk 26.

## Módulos e equivalência com o Good Lock

| Módulo ImfinixLook | Inspiração (Good Lock) | O que faz | Limite sem root |
|---|---|---|---|
| Tela de Bloqueio | LockStar | Wallpaper exclusivo da lockscreen, atalhos para configurações de bloqueio e notificações na lockscreen | Reposicionar relógio/layout da lockscreen exige root/Shizuku |
| Wallpapers | Wonderland (parte) | Gradientes escuros minimalistas gerados no aparelho, aplicados com um toque | Wallpapers animados/parallax exigem live wallpaper (roadmap) |
| Estilos de Relógio | ClockFace | Três widgets de relógio para a home: Gigante, Minimal e Neon (fixação com um toque) | Faces para a lockscreen/Always-On exigem root |
| Temas e Ícones | Theme Park | Orienta a combinação de wallpaper + ícones monocromáticos + temas da loja XOS | Icon packs de terceiros exigem launcher compatível (Nova/Lawnchair) |
| Ajustes Rápidos | QuickStar (parte) | Atalhos diretos para telas de configuração (tela, som, notificações, bateria, segurança) | Editar os tiles do painel rápido exige root/Shizuku |
| Painel Rápido | RegiStar / Settings Search | Configurações escondidas: opções do desenvolvedor, painel de privacidade, apps padrão, acessos especiais, DND | - |
| Perfis de Som | SoundAssistant | Perfis normal/vibrar/silencioso e todos os volumes em uma tela | Volume por app e equalizador exigem APIs de terceiros/root |
| Gestos e Atalhos | One Hand Operation+ | Atalhos para navegação por gestos e gestos do XOS | Gestos de borda personalizados exigem root/Shizuku/acessibilidade |
| Histórico de Notificações | NotiStar | Registra e lista as notificações recebidas (após liberar o acesso a notificações) | - |
| Dicas XOS | - | Truques de personalização, gestos e bateria para o XOS 14 | - |

## Build

O APK é compilado automaticamente pelo GitHub Actions a cada push (`.github/workflows/build.yml`). Baixe o artefato `imfinixlook-debug-apk` na aba Actions.

Build local:

```bash
gradle assembleDebug
# APK em app/build/outputs/apk/debug/app-debug.apk
```

Requer JDK 17 e Android SDK 34.

## Instalação no celular

1. Baixe o APK no Infinix.
2. Permita "instalar apps de fontes desconhecidas" para o navegador/gerenciador de arquivos.
3. Abra o APK e instale.
4. Para o Histórico de Notificações: abra o módulo, toque em "Liberar acesso" e ative o ImfinixLook.

## Detalhes

- Package: `com.imfinix.look`
- minSdk 26, targetSdk 34
- Kotlin, sem dependências externas (somente framework Android)
- Licença: MIT
