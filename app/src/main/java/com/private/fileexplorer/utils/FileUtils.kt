package com.private.fileexplorer.utils

import com.private.fileexplorer.data.FileItem
import kotlin.math.log10
import kotlin.math.pow

/**
 * Funciones auxiliares de formato puras (sin dependencias de Android o Compose).
 */
object FileUtils {

    /**
     * Convierte [bytes] a una cadena legible: "1.5 MB", "340 KB", etc.
     */
    fun formatSize(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val exp = (log10(bytes.toDouble()) / log10(1024.0))
            .toInt()
            .coerceIn(0, units.lastIndex)
        val value = bytes / 1024.0.pow(exp.toDouble())
        return "%.1f %s".format(value, units[exp])
    }

    /**
     * Etiqueta de tipo de archivo según la extensión.
     * Devuelve la extensión en mayúsculas si no está en la lista, o "Archivo" si no tiene extensión.
     */
    fun fileTypeLabel(item: FileItem): String = when (item.extension) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg" -> "Imagen"
        "mp4", "mkv", "avi", "mov", "webm", "ts"                  -> "Video"
        "mp3", "aac", "ogg", "flac", "wav", "m4a"                 -> "Audio"
        "pdf"                                                       -> "PDF"
        "doc", "docx"                                               -> "Word"
        "xls", "xlsx"                                               -> "Excel"
        "ppt", "pptx"                                               -> "PowerPoint"
        "txt", "md", "log", "csv"                                   -> "Texto"
        "zip", "rar", "7z", "tar", "gz"                             -> "Comprimido"
        "apk"                                                       -> "APK"
        "kt", "java", "py", "js", "ts", "html", "css",
        "json", "xml", "sh", "c", "cpp", "h"                       -> "Código"
        else -> item.extension.uppercase().ifEmpty { "Archivo" }
    }
}
