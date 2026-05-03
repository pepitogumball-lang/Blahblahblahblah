package com.private.fileexplorer.data

import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Resultado de intentar listar un directorio. */
sealed class DirectoryResult {
    data class Success(val items: List<FileItem>) : DirectoryResult()
    data class Error(val message: String)         : DirectoryResult()
    object Empty                                  : DirectoryResult()
}

/**
 * Fuente de datos para el sistema de archivos.
 * Todas las operaciones de disco ocurren en [Dispatchers.IO].
 */
object FileRepository {

    /** Directorio raíz del almacenamiento externo principal. */
    fun getPrimaryStorageRoot(): File =
        Environment.getExternalStorageDirectory()

    /**
     * Lista el contenido de [directory].
     * Los resultados se ordenan: carpetas primero, luego archivos, ambos alfabéticamente.
     *
     * @param showHidden Si false, los archivos cuyo nombre empieza por "." no se incluyen.
     */
    suspend fun listDirectory(
        directory: File,
        showHidden: Boolean = false,
    ): DirectoryResult = withContext(Dispatchers.IO) {
        when {
            !directory.exists()    ->
                DirectoryResult.Error("La ruta no existe:\n${directory.path}")
            !directory.isDirectory ->
                DirectoryResult.Error("No es una carpeta:\n${directory.path}")
            !directory.canRead()   ->
                DirectoryResult.Error("Sin acceso de lectura en:\n${directory.path}")
            else -> {
                val raw = directory.listFiles()
                    ?: return@withContext DirectoryResult.Error(
                        "No se pudo leer el contenido de:\n${directory.path}"
                    )

                val filtered = if (showHidden) raw.toList()
                               else raw.filter { !it.name.startsWith('.') }

                if (filtered.isEmpty()) return@withContext DirectoryResult.Empty

                val sorted = filtered
                    .map { FileItem(it) }
                    .sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

                DirectoryResult.Success(sorted)
            }
        }
    }
}
