package com.private.fileexplorer.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest

/**
 * Estado actual del permiso de almacenamiento en el dispositivo.
 */
enum class StoragePermissionState {
    /** Acceso total concedido (MANAGE_EXTERNAL_STORAGE en API 30+). */
    FULL_ACCESS_GRANTED,
    /** Acceso de lectura concedido (READ_EXTERNAL_STORAGE en API 26-32). */
    READ_ACCESS_GRANTED,
    /** Acceso granular concedido (READ_MEDIA_* en API 33+). */
    MEDIA_ACCESS_GRANTED,
    /** Sin permisos. */
    DENIED,
}

object PermissionHelper {

    /**
     * Evalúa el estado actual de permisos de almacenamiento
     * considerando la versión de Android del dispositivo.
     */
    fun getStoragePermissionState(context: Context): StoragePermissionState {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (Environment.isExternalStorageManager()) {
                    StoragePermissionState.FULL_ACCESS_GRANTED
                } else {
                    StoragePermissionState.DENIED
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                val images = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
                val video = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED
                val audio = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_MEDIA_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (images || video || audio) {
                    StoragePermissionState.MEDIA_ACCESS_GRANTED
                } else {
                    StoragePermissionState.DENIED
                }
            }
            else -> {
                val read = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                if (read) StoragePermissionState.READ_ACCESS_GRANTED
                else StoragePermissionState.DENIED
            }
        }
    }

    /** True si hay algún nivel de acceso concedido. */
    fun hasAnyStorageAccess(context: Context): Boolean =
        getStoragePermissionState(context) != StoragePermissionState.DENIED

    /** True si tiene acceso total (MANAGE_EXTERNAL_STORAGE). */
    fun hasFullStorageAccess(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                Environment.isExternalStorageManager()

    /**
     * Devuelve la lista de permisos de tiempo de ejecución a solicitar
     * según la versión de Android.
     * En API 30+ el permiso MANAGE_EXTERNAL_STORAGE se concede desde Ajustes,
     * no mediante requestPermissions, por eso se devuelve lista vacía en esos casos.
     */
    fun getRuntimePermissionsToRequest(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Usamos el intent de ajustes especiales — sin requestPermissions
                emptyArray()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO,
                )
            }
            else -> {
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
            }
        }
    }

    /**
     * Devuelve el Intent que lleva al usuario a la pantalla correcta
     * de ajustes para conceder el permiso según la versión de Android.
     */
    fun buildSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Pantalla específica de "Acceso a todos los archivos"
            try {
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
            } catch (e: Exception) {
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            }
        } else {
            // Ajustes generales de la app
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }
    }

    /**
     * Texto explicativo para el usuario según versión de Android.
     */
    fun permissionExplanation(): String = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ->
            "Esta app necesita el permiso \"Acceso a todos los archivos\" para explorar tu almacenamiento. " +
                    "Pulsa el botón para abrirlo en Ajustes del sistema y actívalo manualmente."
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
            "Esta app necesita acceso a tus archivos multimedia para mostrarte fotos, videos y audio. " +
                    "Concede los permisos cuando el sistema te los solicite."
        else ->
            "Esta app necesita permiso de acceso al almacenamiento externo para explorar archivos y carpetas. " +
                    "Concede el permiso cuando el sistema te lo solicite."
    }
}
