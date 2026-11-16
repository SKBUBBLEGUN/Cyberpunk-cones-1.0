# Cyberpunk 2077 Icon Pack — Samsung Theme Park

Este projeto empacota os 186 PNGs fornecidos em um aplicativo Android de pacote de ícones, com `appfilter.xml` e intent filters comuns de icon packs.

## Gerar o APK no GitHub
1. Crie um repositório GitHub e envie todo o conteúdo desta pasta (não envie a pasta externa como um nível adicional).
2. Abra **Actions** e execute **Build Cyberpunk icon pack APK** (ou faça um push na branch `main`).
3. Ao terminar, abra a execução e baixe o artefato **Cyberpunk-Theme-Park-APK**.
4. Dentro do ZIP do artefato estará `app-debug.apk`. Instale no Samsung e permita a instalação quando solicitado.

## Aplicar no Theme Park
1. Abra o Theme Park → **Icon** → **Create new**.
2. Toque em **Iconpack** (ícone de presente).
3. Procure o pacote em **Third party icon packs**.
4. Selecione-o, salve com a seta no topo e toque em **Apply**.

## Observações importantes
- Inclui 186 ícones originais do ZIP recebido.
- O `appfilter.xml` associa automaticamente alguns apps populares. Os demais ícones permanecem disponíveis no catálogo do pacote, mas a correspondência automática depende do identificador de pacote/atividade de cada app instalado.
- A Samsung altera a compatibilidade e os menus entre versões do Theme Park/One UI. Se o pacote não aparecer, verifique se o APK foi instalado e se sua versão do Theme Park permite importar pacotes de terceiros.
- Este ambiente não possui Android SDK/Gradle instalados, então não foi possível compilar e testar um APK binário aqui. O workflow do GitHub gera o APK debug automaticamente.
