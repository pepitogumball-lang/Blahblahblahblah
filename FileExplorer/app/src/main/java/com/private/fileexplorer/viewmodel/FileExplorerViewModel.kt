package com.private.fileexplorer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.private.fileexplorer.util.DirectoryResult
import com.private.fileexplorer.util.FileItem
import com.private.fileexplorer.util.FileManager
import com.private.fileexplorer.util.PermissionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Estado de la UI del explorador de archivos.
 */
data class ExplorerUiState(
    val currentPath: File? = null,
    val items: List<FileItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasStorageAccess: Boolean = false,
    val backStack: List<File> = emptyList(),
    val showHidden: Boolean = false,
)

class FileExplorerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ExplorerUiState())
    val uiState: StateFlow<ExplorerUiState> = _uiState.asStateFlow()

    // ─── Permisos ────────────────────────────────────────────────────────────

    /** Revalúa el estado de permisos y actualiza el estado de la UI. */
    fun refreshPermissionState() {
        val hasAccess = PermissionHelper.hasAnyStorageAccess(getApplication())
        _uiState.update { it.copy(hasStorageAccess = hasAccess) }
        if (hasAccess && _uiState.value.currentPath == null) {
            navigateTo(FileManager.getPrimaryStorageRoot())
        }
    }

    // ─── Navegación ───────────────────────────────────────────────────────────

    /**
     * Navega a [directory], empujando el directorio actual en el back stack.
     */
    fun navigateTo(directory: File) {
        viewModelScope.launch {
            val current = _uiState.value.currentPath
            val newStack = if (current != null) {
                _uiState.value.backStack + current
            } else {
                _uiState.value.backStack
            }
            _uiState.update {
                it.copy(
                    currentPath = directory,
                    backStack = newStack,
                    isLoading = true,
                    error = null,
                )
            }
            loadCurrentDirectory()
        }
    }

    /**
     * Sube un nivel (retrocede en el back stack).
     * No hace nada si ya estamos en la raíz.
     */
    fun navigateUp() {
        val stack = _uiState.value.backStack
        if (stack.isEmpty()) return
        val parent = stack.last()
        val newStack = stack.dropLast(1)
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentPath = parent,
                    backStack = newStack,
                    isLoading = true,
                    error = null,
                )
            }
            loadCurrentDirectory()
        }
    }

    /**
     * Vuelve al directorio raíz de almacenamiento y limpia el back stack.
     */
    fun navigateHome() {
        val home = FileManager.getPrimaryStorageRoot()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentPath = home,
                    backStack = emptyList(),
                    isLoading = true,
                    error = null,
                )
            }
            loadCurrentDirectory()
        }
    }

    /** True si podemos retroceder. */
    fun canGoUp(): Boolean = _uiState.value.backStack.isNotEmpty()

    /** Recarga el contenido del directorio actual. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            loadCurrentDirectory()
        }
    }

    /** Alterna la visibilidad de archivos ocultos. */
    fun toggleHiddenFiles() {
        _uiState.update { it.copy(showHidden = !it.showHidden) }
        refresh()
    }

    // ─── Carga de datos ───────────────────────────────────────────────────────

    private suspend fun loadCurrentDirectory() {
        val directory = _uiState.value.currentPath ?: return

        val result = withContext(Dispatchers.IO) {
            if (_uiState.value.showHidden) {
                FileManager.listDirectoryAll(directory)
            } else {
                FileManager.listDirectory(directory)
            }
        }

        _uiState.update { state ->
            when (result) {
                is DirectoryResult.Success -> state.copy(
                    items = result.items,
                    isLoading = false,
                    error = null,
                )
                is DirectoryResult.Empty -> state.copy(
                    items = emptyList(),
                    isLoading = false,
                    error = null,
                )
                is DirectoryResult.Error -> state.copy(
                    items = emptyList(),
                    isLoading = false,
                    error = result.message,
                )
            }
        }
    }
}
