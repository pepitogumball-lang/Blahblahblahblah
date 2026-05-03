package com.private.fileexplorer.permissions

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
 * Gestión de permisos de almacenamiento.
 *
 * Estrategia por versión de Android:
 *   API ≥ 30 (Android 11+): MANAGE_EXTERNAL_STORAGE — se concede desde Ajustes del sistema.
 *   API 26–29            : READ_EXTERNAL_STORAGE   — diálogo estándar de runtime permission.
 */
object PermissionHelper {

    /**
     * True si la app tiene algún nivel de acceso al almacenamiento externo.
     * Es el único método que necesitas llamar para decidir qué pantalla mostrar.
     */
    fun hasAnyStorageAccess(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Permisos de runtime a solicitar con [requestPermissions].
     * En API 30+ devuelve array vacío porque MANAGE_EXTERNAL_STORAGE
     * no se puede conceder con requestPermissions; usa [buildSettingsIntent].
     */
    fun getRuntimePermissionsToRequest(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            emptyArray()
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        }

    /**
     * Intent que lleva al usuario a la pantalla correcta de ajustes de permisos.
     *   API 30+: pantalla "Acceso a todos los archivos".
     *   API < 30: pantalla de detalles de la app.
     */
    fun buildSettingsIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                )
            } catch (_: Exception) {
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }

    /**
     * Descripción del permiso necesario, adaptada a la versión de Android del dispositivo.
     * Se muestra ANTES de redirigir al usuario a los ajustes.
     */
    fun permissionRationale(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            "Esta app necesita el permiso «Acceso a todos los archivos» para poder " +
                "leer carpetas y archivos en tu almacenamiento. Sin él, no puede " +
                "mostrarte ningún contenido."
        } else {
            "Esta app necesita permiso de acceso al almacenamiento externo para " +
                "leer tus archivos y carpetas. Sin él, no puede mostrar ningún contenido."
        }

    /**
     * Nombre del permiso tal como aparece en los ajustes del sistema,
     * para que el usuario sepa qué activar.
     */
    fun permissionSettingsLabel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            "Acceso a todos los archivos"
        else
            "Almacenamiento → Permitir"
}
