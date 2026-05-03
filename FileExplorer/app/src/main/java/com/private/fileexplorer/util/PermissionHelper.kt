package com.private.fileexplorer.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Utilidades para verificar y solicitar permisos de almacenamiento.
 *
 * Estrategia por versión de Android:
 *   - API 30+ (Android 11+): MANAGE_EXTERNAL_STORAGE — se concede desde Ajustes del sistema.
 *   - API 26–29: READ_EXTERNAL_STORAGE — se solicita con requestPermissions estándar.
 */
object PermissionHelper {

    /**
     * Devuelve true si hay algún nivel de acceso a almacenamiento concedido.
     * Es el único método que necesita llamarse para decidir si mostrar o no el explorador.
     */
    fun hasAnyStorageAccess(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ — necesita MANAGE_EXTERNAL_STORAGE (incluye API 33+)
            Environment.isExternalStorageManager()
        } else {
            // Android 10 y anteriores — permiso de lectura estándar
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Lista de permisos de tiempo de ejecución a solicitar según versión Android.
     * En API 30+ devuelve lista vacía porque MANAGE_EXTERNAL_STORAGE
     * no se puede pedir con requestPermissions — requiere el intent especial de Ajustes.
     */
    fun getRuntimePermissionsToRequest(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            emptyArray()
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }
    }

    /**
     * Intent para llevar al usuario a la pantalla correcta de permisos.
     *  - API 30+: pantalla específica de "Acceso a todos los archivos".
     *  - API < 30: pantalla de detalles de la app en Ajustes.
     */
    fun buildSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                )
            } catch (_: Exception) {
                // Fallback por si el intent específico no existe en el ROM
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    /**
     * Explicación clara y honesta del permiso que se va a solicitar.
     * Se muestra al usuario ANTES de mandarlo a Ajustes.
     */
    fun permissionExplanation(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "Esta app necesita el permiso \"Acceso a todos los archivos\" para poder " +
                "explorar carpetas y archivos en tu almacenamiento. Sin él, no puede leer " +
                "nada. Pulsa el botón, busca la app en la lista y activa el permiso."
        } else {
            "Esta app necesita permiso de acceso al almacenamiento externo para mostrarte " +
                "tus archivos y carpetas. Sin él, no puede leer el contenido de tu dispositivo. " +
                "Pulsa el botón y acepta el permiso cuando el sistema te lo solicite."
        }
    }
}
