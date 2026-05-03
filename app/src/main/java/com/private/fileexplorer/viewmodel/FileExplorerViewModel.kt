package com.private.fileexplorer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.private.fileexplorer.data.DirectoryResult
import com.private.fileexplorer.data.FileItem
import com.private.fileexplorer.data.FileRepository
import com.private.fileexplorer.permissions.PermissionHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Estado completo de la UI del explorador de archivos.
 */
data class ExplorerUiState(
    val currentPath: File?       = null,
    val items: List<FileItem>    = emptyList(),
    val isLoading: Boolean       = false,
    val error: String?           = null,
    val hasStorageAccess: Boolean = false,
    val backStack: List<File>    = emptyList(),
    val showHidden: Boolean      = false,
)

class FileExplorerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ExplorerUiState())
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()

    /** Job activo de carga — se cancela si llega uno nuevo antes de que termine. */
    private var loadingJob: Job? = null

    // ─── Permisos ─────────────────────────────────────────────────────────────

    /**
     * Revalúa el estado del permiso.
     * Llamar desde onResume de la Activity.
     */
    fun refreshPermissionState() {
        val hasAccess = PermissionHelper.hasAnyStorageAccess(getApplication())
        _uiState.update { it.copy(hasStorageAccess = hasAccess) }
        if (hasAccess && _uiState.value.currentPath == null) {
            navigateTo(FileRepository.getPrimaryStorageRoot())
        }
    }

    // ─── Navegación ───────────────────────────────────────────────────────────

    /** Entra en [directory], guardando el directorio actual en el back stack. */
    fun navigateTo(directory: File) {
        val current = _uiState.value.currentPath
        val newStack = if (current != null) _uiState.value.backStack + current
                       else _uiState.value.backStack
        _uiState.update {
            it.copy(
                currentPath = directory,
                backStack   = newStack,
                isLoading   = true,
                error       = null,
                items       = emptyList(),
            )
        }
        startLoading(directory)
    }

    /** Retrocede al directorio anterior del back stack. No hace nada si estamos en la raíz. */
    fun navigateUp() {
        val stack = _uiState.value.backStack
        if (stack.isEmpty()) return
        val parent   = stack.last()
        val newStack = stack.dropLast(1)
        _uiState.update {
            it.copy(
                currentPath = parent,
                backStack   = newStack,
                isLoading   = true,
                error       = null,
                items       = emptyList(),
            )
        }
        startLoading(parent)
    }

    /** Vuelve al directorio raíz de almacenamiento y limpia el back stack. */
    fun navigateHome() {
        val home = FileRepository.getPrimaryStorageRoot()
        _uiState.update {
            it.copy(
                currentPath = home,
                backStack   = emptyList(),
                isLoading   = true,
                error       = null,
                items       = emptyList(),
            )
        }
        startLoading(home)
    }

    /** True si hay al menos un directorio en el back stack. */
    fun canGoUp(): Boolean = _uiState.value.backStack.isNotEmpty()

    /** Recarga el directorio actual sin cambiar la ruta ni el back stack. */
    fun refresh() {
        val current = _uiState.value.currentPath ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        startLoading(current)
    }

    /** Muestra u oculta archivos cuyo nombre empieza por punto y recarga. */
    fun toggleHiddenFiles() {
        _uiState.update { it.copy(showHidden = !it.showHidden, isLoading = true) }
        val current = _uiState.value.currentPath ?: return
        startLoading(current)
    }

    // ─── Carga ────────────────────────────────────────────────────────────────

    /**
     * Cancela cualquier carga previa y lanza una nueva para [directory].
     * El resultado se descarta si el job fue cancelado antes de terminar.
     */
    private fun startLoading(directory: File) {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            val showHidden = _uiState.value.showHidden
            val result     = FileRepository.listDirectory(directory, showHidden)
            if (!isActive) return@launch          // descartado por cancel()
            _uiState.update { state ->
                when (result) {
                    is DirectoryResult.Success -> state.copy(
                        items     = result.items,
                        isLoading = false,
                        error     = null,
                    )
                    DirectoryResult.Empty -> state.copy(
                        items     = emptyList(),
                        isLoading = false,
                        error     = null,
                    )
                    is DirectoryResult.Error -> state.copy(
                        items     = emptyList(),
                        isLoading = false,
                        error     = result.message,
                    )
                }
            }
        }
    }
}
