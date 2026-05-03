package com.private.fileexplorer.util

import java.io.File

/**
 * Modelo que representa un ítem en el explorador de archivos.
 */
data class FileItem(
    val file: File,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long = if (file.isFile) file.length() else 0L,
    val lastModified: Long = file.lastModified(),
    val extension: String = file.extension.lowercase(),
)

/**
 * Resultado de listar el contenido de una carpeta.
 */
sealed class DirectoryResult {
    data class Success(val items: List<FileItem>) : DirectoryResult()
    data class Error(val message: String) : DirectoryResult()
    object Empty : DirectoryResult()
}

/**
 * Gestiona las operaciones sobre el sistema de archivos.
 * No tiene estado propio; es pura lógica de acceso a archivos.
 */
object FileManager {

    /** Rutas raíz disponibles en el dispositivo. */
    fun getRootPaths(): List<File> {
        val roots = mutableListOf<File>()

        // Almacenamiento interno primario
        val primary = android.os.Environment.getExternalStorageDirectory()
        if (primary.exists()) roots.add(primary)

        // Almacenamiento raíz del sistema (solo si tenemos MANAGE_EXTERNAL_STORAGE)
        val root = File("/")
        if (root.exists() && root.canRead()) roots.add(root)

        // /sdcard como alias común
        val sdcard = File("/sdcard")
        if (sdcard.exists() && !roots.any { it.canonicalPath == sdcard.canonicalPath }) {
            roots.add(sdcard)
        }

        return roots.distinctBy { it.canonicalPath }
    }

    /** Devuelve el directorio de almacenamiento externo principal. */
    fun getPrimaryStorageRoot(): File =
        android.os.Environment.getExternalStorageDirectory()

    /**
     * Lista el contenido de [directory] ordenando carpetas primero,
     * luego archivos, ambos grupos alfabéticamente.
     */
    fun listDirectory(directory: File): DirectoryResult {
        if (!directory.exists()) {
            return DirectoryResult.Error("La ruta no existe: ${directory.path}")
        }
        if (!directory.isDirectory) {
            return DirectoryResult.Error("No es una carpeta: ${directory.path}")
        }
        if (!directory.canRead()) {
            return DirectoryResult.Error("Sin permiso de lectura en: ${directory.path}")
        }

        val files = directory.listFiles()
            ?: return DirectoryResult.Error("No se pudo leer el contenido de: ${directory.path}")

        if (files.isEmpty()) return DirectoryResult.Empty

        val sorted = files
            .filter { !it.name.startsWith(".") } // ocultar archivos ocultos
            .map { FileItem(it) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

        return DirectoryResult.Success(sorted)
    }

    /** Lista incluyendo archivos ocultos (nombre empieza con punto). */
    fun listDirectoryAll(directory: File): DirectoryResult {
        if (!directory.exists()) {
            return DirectoryResult.Error("La ruta no existe: ${directory.path}")
        }
        if (!directory.isDirectory) {
            return DirectoryResult.Error("No es una carpeta: ${directory.path}")
        }
        if (!directory.canRead()) {
            return DirectoryResult.Error("Sin permiso de lectura en: ${directory.path}")
        }

        val files = directory.listFiles()
            ?: return DirectoryResult.Error("No se pudo leer el contenido de: ${directory.path}")

        if (files.isEmpty()) return DirectoryResult.Empty

        val sorted = files
            .map { FileItem(it) }
            .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

        return DirectoryResult.Success(sorted)
    }

    /** Formatea el tamaño de un archivo en bytes a una cadena legible. */
    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
        return "%.1f %s".format(value, units[digitGroups])
    }

    /** Devuelve el tipo de archivo según su extensión. */
    fun fileTypeLabel(item: FileItem): String = when (item.extension) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg" -> "Imagen"
        "mp4", "mkv", "avi", "mov", "webm", "ts"                  -> "Video"
        "mp3", "aac", "ogg", "flac", "wav", "m4a"                 -> "Audio"
        "pdf"                                                       -> "PDF"
        "doc", "docx"                                               -> "Word"
        "xls", "xlsx"                                               -> "Excel"
        "ppt", "pptx"                                               -> "PowerPoint"
        "txt", "md", "log", "csv"                                   -> "Texto"
        "zip", "rar", "7z", "tar", "gz"                             -> "Archivo"
        "apk"                                                       -> "APK"
        "kt", "java", "py", "js", "ts", "html", "css", "json",
        "xml", "sh", "c", "cpp", "h"                               -> "Código"
        else                                                        -> item.extension.uppercase().ifEmpty { "Archivo" }
    }
}
