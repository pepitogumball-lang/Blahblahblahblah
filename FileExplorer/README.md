# File Explorer

App Android de exploración de archivos, escrita en **Kotlin** con **Jetpack Compose** y **Material 3**.  
Diseñada para uso personal y privado. Compilable desde GitHub Actions sin configuración manual.

---

## Qué hace

- Muestra carpetas y archivos del almacenamiento del dispositivo en tiempo real
- Ordena carpetas primero, luego archivos (ambos alfabéticamente)
- Navega dentro y fuera de subcarpetas con back stack correcto
- Botón de inicio para volver al almacenamiento raíz con un toque
- Botón de refresco para releer la carpeta actual
- Alternar entre mostrar y ocultar archivos ocultos (que empiezan por `.`)
- Ícono por tipo de archivo: imagen, vídeo, audio, PDF, Office, código, comprimidos, etc.
- Tamaño legible y fecha de modificación en cada ítem
- **Transiciones suaves** entre estados (Crossfade 200 ms)
- **Estado vacío** claro: "No hay archivos en esta carpeta"
- **Estado de error** con botón "Reintentar" funcional
- **Estado de carga** con indicador visual
- Pantalla de permisos con instrucciones paso a paso según la versión de Android

---

## Permisos por versión de Android

| Versión Android | API  | Permiso necesario              | Cómo se concede                          |
|-----------------|------|--------------------------------|------------------------------------------|
| Android 11+     | ≥ 30 | `MANAGE_EXTERNAL_STORAGE`      | Ajustes → Acceso a todos los archivos    |
| Android ≤ 10    | < 30 | `READ_EXTERNAL_STORAGE`        | Diálogo estándar del sistema             |

La app explica el motivo del permiso **antes** de redirigir al usuario a los ajustes.

---

## Estructura del código

```
app/src/main/java/com/private/fileexplorer/
│
├── MainActivity.kt               → Punto de entrada; decide qué pantalla mostrar
│
├── data/
│   ├── FileItem.kt               → Modelo de ítem (archivo o carpeta)
│   └── FileRepository.kt         → Lectura real del disco (en Dispatchers.IO)
│
├── permissions/
│   └── PermissionHelper.kt       → Lógica de permisos adaptada por versión Android
│
├── utils/
│   └── FileUtils.kt              → Funciones de formato puras (tamaño, etiqueta)
│
├── viewmodel/
│   └── FileExplorerViewModel.kt  → Estado y navegación; back stack; cancelación de jobs
│
└── ui/
    ├── FileExplorerScreen.kt     → Lista de archivos con estados: cargando/vacío/error/contenido
    ├── PermissionScreen.kt       → Pantalla de permisos con pasos numerados
    └── theme/
        ├── Color.kt              → Paleta de colores + colores semánticos por tipo de archivo
        ├── Theme.kt              → Tema Material3 (modo claro/oscuro/dinámico) + fileTypeColor()
        └── Type.kt               → Tipografía
```

---

## Requisitos de compilación

| Herramienta                  | Versión |
|------------------------------|---------|
| JDK                          | 17      |
| Android Gradle Plugin (AGP)  | 8.2.2   |
| Kotlin                       | 1.9.22  |
| Compose BOM                  | 2024.02.01 |
| compileSdk / minSdk / targetSdk | 34 / 26 / 34 |

---

## Cómo compilar

### Desde GitHub Actions (recomendado — no necesitas nada instalado)

Cada push a `main` compila el APK debug automáticamente.  
El APK queda en **Actions → Build APK → artefacto `FileExplorer-debug-apk`**.

Para lanzarlo manualmente desde GitHub:
1. Ve a **Actions → Build APK**
2. Pulsa **Run workflow**

### Desde Android Studio localmente

1. Clona el repositorio
2. Abre la carpeta **`FileExplorer/`** en Android Studio *(no la raíz del repo)*
3. Deja que Gradle sincronice — descarga todo automáticamente
4. Conecta un dispositivo o inicia un emulador (API 26+)
5. Pulsa **Run ▶**

O desde la terminal:
```bash
cd FileExplorer
gradle assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

Si prefieres usar `./gradlew`, genera el wrapper una sola vez:
```bash
cd FileExplorer
gradle wrapper --gradle-version 8.6
./gradlew assembleDebug
```

### Instalar en el dispositivo

```bash
adb install FileExplorer/app/build/outputs/apk/debug/app-debug.apk
```

---

## Cómo usar `pusher.py`

Desde la terminal de Replit (o cualquier terminal con Python 3):

```bash
python3 pusher.py
```

El script hace automáticamente:
1. Verifica que el directorio sea un repositorio Git
2. Comprueba que la rama activa sea `main`
3. Ejecuta `git add .`
4. Crea un commit con timestamp (`chore: actualización automática [YYYY-MM-DD HH:MM]`)
5. Hace `git push origin main`

Usa la variable de entorno `GITHUB_PERSONAL_ACCESS_TOKEN` para autenticarse sin interacción manual.  
Si no hay cambios, informa claramente y no crea un commit vacío.

---

## Créditos y licencias

**Todos los recursos gráficos del proyecto (íconos, vectores, fondos) fueron generados  
íntegramente dentro del proyecto como archivos XML de vector drawable.**

No se utilizaron imágenes externas, íconos de terceros, fuentes de pago  
ni ningún recurso con restricciones de copyright.

| Recurso | Origen | Licencia |
|---------|--------|----------|
| `ic_launcher_foreground.xml` | Vector drawable XML generado en el proyecto | Propiedad del proyecto |
| `ic_launcher_background.xml` | Shape drawable XML generado en el proyecto | Propiedad del proyecto |
| Iconos de UI (carpeta, archivo, etc.) | `androidx.compose.material:material-icons-extended` | Apache 2.0 |
| Jetpack Compose + Material3 | Google / AOSP | Apache 2.0 |
| Kotlin | JetBrains | Apache 2.0 |

---

## Notas importantes

- La app es de uso privado y **no está diseñada para Google Play Store**.
- `minifyEnabled` está desactivado en release para facilitar la depuración.
- El archivo `gradle-wrapper.jar` no está en el repositorio (es binario).  
  El CI usa `gradle` directamente a través de `gradle/actions/setup-gradle@v3`.  
  Para usar `./gradlew` localmente, ejecuta `gradle wrapper --gradle-version 8.6` una sola vez.
- Los íconos del launcher usan **adaptive icons** (`mipmap-anydpi-v26`) y se ven correctamente  
  en círculo, squircle y cuadrado redondeado en todos los lanzadores de Android modernos.
