# File Explorer — App Android privada

Explorador de archivos moderno para Android, escrito en Kotlin con Jetpack Compose.
Diseñado para uso personal, compilable desde GitHub Actions o Android Studio.

---

## Características

- Exploración completa del almacenamiento interno y externo
- Soporte para el permiso `MANAGE_EXTERNAL_STORAGE` (API 30+)
- Fallback a `READ_EXTERNAL_STORAGE` para API 26–29
- Navegación con back stack (entrar/salir de carpetas)
- Vista de carpetas y archivos con iconos por tipo
- Botones de refrescar, subir nivel y volver al inicio
- Toggle para mostrar/ocultar archivos ocultos
- Interfaz Material3 con soporte para tema dinámico y modo oscuro

---

## Requisitos de compilación

| Herramienta | Versión mínima |
|-------------|----------------|
| JDK         | 17             |
| Android Gradle Plugin | 8.2.2 |
| Gradle      | 8.6            |
| Kotlin      | 1.9.22         |
| compileSdk  | 34             |
| minSdk      | 26             |

---

## Compilar localmente (Android Studio)

1. Clona el repositorio.
2. Abre la carpeta `FileExplorer/` en Android Studio.
3. Deja que Gradle sincronice las dependencias.
4. Conecta un dispositivo o inicia un emulador.
5. Pulsa **Run** o usa:

```bash
cd FileExplorer
./gradlew assembleDebug
```

El APK queda en:
```
FileExplorer/app/build/outputs/apk/debug/app-debug.apk
```

---

## Compilar desde GitHub Actions

Cada push a `main` dispara el workflow `.github/workflows/build.yml` automáticamente.
El APK debug queda disponible como artefacto descargable en la pestaña **Actions** del repositorio.

Para compilar manualmente desde la interfaz de GitHub:
1. Ve a **Actions → Build APK**.
2. Pulsa **Run workflow**.

---

## Usar la app

1. Instala el APK en tu dispositivo (`adb install app-debug.apk` o copia manual).
2. Al abrir la app, se te pedirá el permiso de almacenamiento.
   - En Android 11+: pulsa "Abrir ajustes del sistema" y activa "Acceso a todos los archivos".
   - En Android 10 y anteriores: acepta el permiso cuando el sistema te lo solicite.
3. Una vez concedido el permiso, la app abre el almacenamiento externo automáticamente.
4. Navega tocando carpetas; usa la flecha atrás para subir un nivel, el icono de casa para ir al inicio.

---

## Estructura del proyecto

```
FileExplorer/
├── app/
│   └── src/main/
│       ├── java/com/private/fileexplorer/
│       │   ├── MainActivity.kt              # Punto de entrada
│       │   ├── ui/
│       │   │   ├── FileExplorerScreen.kt   # Pantalla principal
│       │   │   ├── PermissionScreen.kt     # Pantalla de permisos
│       │   │   └── theme/                  # Colores, tipografía, tema
│       │   ├── viewmodel/
│       │   │   └── FileExplorerViewModel.kt # Estado y lógica de navegación
│       │   └── util/
│       │       ├── FileManager.kt          # Operaciones de archivo
│       │       └── PermissionHelper.kt     # Utilidades de permisos
│       ├── res/values/
│       └── AndroidManifest.xml
├── .github/workflows/build.yml             # CI/CD para APK
├── gradle/wrapper/
└── build.gradle / settings.gradle
```

---

## Automatización Git

Desde la raíz del repositorio (en Replit):

```bash
python3 pusher.py
```

El script detecta cambios, hace commit con timestamp y hace push a `main` usando el token de GitHub.

---

## Notas

- La app es de uso privado y no está pensada para Google Play.
- `minifyEnabled` está desactivado en release para facilitar depuración.
- Los crasheos durante la navegación están mitigados con manejo de errores en `FileManager`.
