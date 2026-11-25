# EMOM Timer — Android V1

Aplicativo Android nativo em Java, sem servidor e sem dependências de runtime externas. Inspirado na tela simples do vídeo: números grandes, fundo verde na primeira metade, vermelho na segunda (se o alerta estiver ligado) e nos rounds completos de descanso. Sons/vibração configuráveis e tela ligada enquanto o treino roda. Pausa automaticamente ao sair do app. Configurações salvas localmente; sem histórico (V1).

## Gerar APK no Windows com Android Studio
1. Extraia o ZIP e abra a pasta EMOM-Android no Android Studio (Open).
2. Use JDK 17 e instale Android SDK Platform 35 quando solicitado. O projeto usa AGP 8.7.3 e Gradle 8.9. Se o IDE solicitar uma distribuição Gradle, selecione 8.9 (o ZIP não contém o wrapper binário). Alternativamente instale Gradle 8.9 localmente e execute `gradle wrapper --gradle-version 8.9` na raiz.
3. Aguarde o Gradle Sync. Em Build > Generate App Bundles or APKs > Generate APKs (em outras versões, Build APK(s)). Ou execute `gradle assembleDebug`.
4. O APK de teste estará em `app/build/outputs/apk/debug/app-debug.apk`. Transfira ao telefone e instale, permitindo instalação pela origem utilizada se necessário.

## Alternativa sem Android Studio: GitHub Actions
Crie um repositório seu, envie todo o conteúdo desta pasta (incluindo `.github`) para a raiz da branch `main` e abra Actions > Build Android APK > Run workflow. Após um build bem-sucedido, baixe o artefato EMOM-Timer-APK, extraia e instale o APK. O workflow gera um APK debug, suficiente para uso pessoal; não é um pacote de publicação na Play Store.

## Testes e limites
O motor do cronômetro é Java puro: `javac -d out app/src/main/java/br/com/emomtimer/TimerEngine.java tests/TimerEngineTest.java` e `java -cp out TimerEngineTest`. O APK não foi compilado neste ambiente (SDK Android ausente e rede indisponível). A interface Android, sons, vibração, instalação e execução em aparelho ainda precisam ser validados. O cronômetro pausa ao sair do app e não executa em segundo plano. Não há persistência de um treino em andamento após encerramento do processo.
