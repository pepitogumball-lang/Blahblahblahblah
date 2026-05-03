# FileExplorer — App Android + Automatización Git

## Descripción del proyecto

Repositorio con dos componentes principales:

1. **`pusher.py`** — Script Python 3 para automatizar el flujo Git.
   Uso: `python3 pusher.py`
   Hace: verificación de repo → rama main → git add . → commit con timestamp → push.
   Usa `GITHUB_PERSONAL_ACCESS_TOKEN` del entorno para autenticarse.

2. **`FileExplorer/`** — App Android en Kotlin/Jetpack Compose.
   Explorador de archivos con manejo correcto de permisos de almacenamiento por versión Android.

---

## Estructura de archivos clave

```
/
├── pusher.py
└── FileExplorer/
    ├── .github/workflows/build.yml           # CI: compila APK debug en cada push a main
    ├── app/
    │   ├── build.gradle                      # AGP 8.2.2, Compose BOM 2024.02.01
    │   └── src/main/
    │       ├── AndroidManifest.xml
    │       ├── java/com/private/fileexplorer/
    │       │   ├── MainActivity.kt
    │       │   ├── ui/FileExplorerScreen.kt
    │       │   ├── ui/PermissionScreen.kt
    │       │   ├── ui/theme/ (Color, Theme, Type)
    │       │   ├── viewmodel/FileExplorerViewModel.kt
    │       │   └── util/ (FileManager, PermissionHelper)
    │       └── res/ (drawables, mipmap-anydpi-v26, values)
    ├── build.gradle / settings.gradle / gradle.properties
    ├── gradle/wrapper/gradle-wrapper.properties
    ├── gradlew / gradlew.bat
    └── README.md
```

---

## Stack técnico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin 1.9.22 |
| UI | Jetpack Compose + Material3 |
| Arquitectura | ViewModel + StateFlow |
| Permisos | Lógica manual por API level (sin librerías extra) |
| Build | Gradle 8.6 + AGP 8.2.2 |
| CI/CD | GitHub Actions — usa `gradle` directamente (sin wrapper jar) |
| minSdk / targetSdk | 26 / 34 |

---

## Dependencias de la app

- `androidx.compose:compose-bom:2024.02.01`
- `androidx.compose.material3:material3`
- `androidx.compose.material:material-icons-extended`
- `androidx.core:core-ktx:1.12.0`
- `androidx.lifecycle:lifecycle-runtime-ktx:2.7.0`
- `androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0`
- `androidx.activity:activity-compose:1.8.2`

No hay dependencias de terceros innecesarias.

---

## Permisos por versión Android

- API 30+ (Android 11+): `MANAGE_EXTERNAL_STORAGE` → vía Ajustes del sistema
- API 26–29: `READ_EXTERNAL_STORAGE` → diálogo estándar

---

## Secrets configurados

- `GITHUB_PERSONAL_ACCESS_TOKEN` — usado por pusher.py para autenticación HTTPS en push

---

## Notas importantes

- El `gradle-wrapper.jar` no está en el repo (es binario). El CI usa `gradle` directamente.
- Para compilar localmente con `./gradlew`, generar el jar con: `gradle wrapper --gradle-version 8.6`
- El icono del launcher usa adaptive icons (mipmap-anydpi-v26) compatibles con minSdk 26.
- `minifyEnabled` está desactivado en release para facilitar la depuración en uso privado.
