# File Explorer — App Android privada

Explorador de archivos moderno para Android, escrito en **Kotlin** con **Jetpack Compose**.
Pensado para uso personal y privado. Compilable desde GitHub Actions sin configuración extra.

---

## Qué hace la app

- Muestra carpetas y archivos del almacenamiento del dispositivo
- Ordena carpetas primero, luego archivos (ambos en orden alfabético)
- Entra y sale de subcarpetas con back stack correcto
- Botón de inicio para volver al almacenamiento raíz
- Botón de refresco para volver a leer la carpeta actual
- Icono diferente por tipo de archivo (imagen, video, audio, PDF, código, etc.)
- Tamaño y fecha de modificación en cada ítem
- Opción para mostrar u ocultar archivos ocultos (que empiezan por punto)
- Estado vacío claro: "No hay archivos en esta carpeta"
- Estado de error con mensaje legible si hay problemas de lectura
- Manejo de permisos de almacenamiento con explicación antes de pedirlos

---

## Permisos por versión de Android

| Versión Android | API | Permiso necesario | Cómo se concede |
|---|---|---|---|
| Android 11+ | ≥ 30 | `MANAGE_EXTERNAL_STORAGE` | Ajustes del sistema → "Acceso a todos los archivos" |
| Android 10 y anteriores | 26–29 | `READ_EXTERNAL_STORAGE` | Diálogo estándar del sistema |

La app explica el motivo del permiso antes de mandar al usuario a Ajustes. No hay comportamiento oculto.

---

## Requisitos de compilación

| Herramienta | Versión |
|---|---|
| JDK | 17 |
| Android Gradle Plugin | 8.2.2 |
| Kotlin | 1.9.22 |
| Compose BOM | 2024.02.01 |
| compileSdk / minSdk / targetSdk | 34 / 26 / 34 |

---

## Compilar desde GitHub Actions (recomendado)

Cada push a `main` dispara el workflow `.github/workflows/build.yml` automáticamente.

El APK debug queda en **Actions → Build APK → artefacto `FileExplorer-debug-apk`**.

Para lanzarlo manualmente:
1. Ve a **Actions → Build APK**
2. Pulsa **Run workflow**

No necesitas instalar nada localmente para esto.

---

## Compilar localmente desde Android Studio

1. Clona el repositorio
2. Abre la carpeta `FileExplorer/` en Android Studio (no la raíz del repo)
3. Android Studio descarga Gradle automáticamente al sincronizar
4. Conecta un dispositivo o inicia un emulador
5. Pulsa **Run ▶** o usa desde terminal:

```bash
cd FileExplorer
gradle assembleDebug
```

Si prefieres usar `./gradlew`, primero genera el wrapper:
```bash
cd FileExplorer
gradle wrapper --gradle-version 8.6
./gradlew assembleDebug
```

El APK queda en:
```
FileExplorer/app/build/outputs/apk/debug/app-debug.apk
```

---

## Instalar en el dispositivo

```bash
adb install FileExplorer/app/build/outputs/apk/debug/app-debug.apk
```

O copia el APK al dispositivo y ábrelo manualmente (necesitas permitir instalación de fuentes desconocidas).

---

## Automatización Git desde Replit

```bash
python3 pusher.py
```

El script hace `git add .` → commit con timestamp → `git push origin main` automáticamente.
Usa la variable de entorno `GITHUB_PERSONAL_ACCESS_TOKEN` para autenticarse.

---

## Estructura del proyecto

```
FileExplorer/
├── .github/workflows/build.yml           # CI/CD: compila APK en cada push
├── app/
│   ├── build.gradle                      # Dependencias y configuración del módulo
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml           # Permisos y declaración de Activity
│       ├── java/com/private/fileexplorer/
│       │   ├── MainActivity.kt           # Punto de entrada — decide qué pantalla mostrar
│       │   ├── ui/
│       │   │   ├── FileExplorerScreen.kt # Lista de archivos con todos los estados
│       │   │   ├── PermissionScreen.kt   # Explicación + botón de permiso
│       │   │   └── theme/
│       │   │       ├── Color.kt          # Paleta de colores
│       │   │       ├── Theme.kt          # Tema Material3 + helper de colores por tipo
│       │   │       └── Type.kt           # Tipografía
│       │   ├── viewmodel/
│       │   │   └── FileExplorerViewModel.kt  # Estado, navegación, back stack
│       │   └── util/
│       │       ├── FileManager.kt        # Lectura de directorios, ordenado, tamaño
│       │       └── PermissionHelper.kt   # Lógica de permisos por versión Android
│       └── res/
│           ├── drawable/                 # Iconos del launcher
│           ├── mipmap-anydpi-v26/        # Adaptive icons
│           └── values/                   # strings.xml, themes.xml
├── build.gradle                          # Plugins del proyecto
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
├── gradlew                               # Script de wrapper (requiere jar para funcionar)
├── gradlew.bat                           # Script de wrapper para Windows
├── local.properties.example             # Ejemplo de configuración local del SDK
└── settings.gradle
```
