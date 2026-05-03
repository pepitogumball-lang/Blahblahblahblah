# FileExplorer — Proyecto Android + Git Automation

## Descripción
Repositorio con dos componentes:

1. **`pusher.py`** — Script Python 3 para automatizar el flujo Git (add → commit → push a `main`).
2. **`FileExplorer/`** — App Android privada en Kotlin/Jetpack Compose: explorador de archivos con permisos de almacenamiento completo.

---

## Estructura del repositorio

```
/
├── pusher.py                          # Automatizador Git (ejecutar: python3 pusher.py)
├── FileExplorer/
│   ├── .github/workflows/build.yml   # CI: compila APK debug en cada push a main
│   ├── app/
│   │   ├── build.gradle
│   │   ├── proguard-rules.pro
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/private/fileexplorer/
│   │       │   ├── MainActivity.kt
│   │       │   ├── ui/
│   │       │   │   ├── FileExplorerScreen.kt
│   │       │   │   ├── PermissionScreen.kt
│   │       │   │   └── theme/ (Color.kt, Theme.kt, Type.kt)
│   │       │   ├── viewmodel/FileExplorerViewModel.kt
│   │       │   └── util/ (FileManager.kt, PermissionHelper.kt)
│   │       └── res/values/ (strings.xml, themes.xml)
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradle.properties
│   ├── gradle/wrapper/gradle-wrapper.properties
│   ├── local.properties.example
│   └── README.md
└── replit.md
```

---

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 1.9.22 |
| UI | Jetpack Compose + Material3 |
| Arquitectura | ViewModel + StateFlow |
| Permisos | Accompanist Permissions + lógica manual por API level |
| Build | Gradle 8.6 + AGP 8.2.2 |
| CI/CD | GitHub Actions |
| minSdk / targetSdk | 26 / 34 |

---

## Uso rápido

### Subir cambios a GitHub
```bash
python3 pusher.py
```
Usa `GITHUB_PERSONAL_ACCESS_TOKEN` del entorno automáticamente.

### Compilar APK localmente
```bash
cd FileExplorer
./gradlew assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk
```

### CI en GitHub Actions
Cada push a `main` compila y publica el APK como artefacto descargable.

---

## Secrets configurados
- `GITHUB_PERSONAL_ACCESS_TOKEN` — usado por `pusher.py` para autenticación HTTPS en push.
