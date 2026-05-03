package com.private.fileexplorer.data

import java.io.File

/**
 * Representa un ítem (archivo o carpeta) dentro del explorador.
 * Todos los valores se calculan en el momento de creación, en un hilo de IO.
 */
data class FileItem(
    val file: File,
    val name: String        = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val size: Long          = if (file.isFile) file.length() else 0L,
    val lastModified: Long  = file.lastModified(),
    val extension: String   = file.extension.lowercase(),
)
