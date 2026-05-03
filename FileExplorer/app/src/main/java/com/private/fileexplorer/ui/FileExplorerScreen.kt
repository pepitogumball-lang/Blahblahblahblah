package com.private.fileexplorer.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.private.fileexplorer.data.FileItem
import com.private.fileexplorer.ui.theme.ColorFolder
import com.private.fileexplorer.ui.theme.fileTypeColor
import com.private.fileexplorer.utils.FileUtils
import com.private.fileexplorer.viewmodel.ExplorerUiState
import com.private.fileexplorer.viewmodel.FileExplorerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─── Pantalla principal ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileExplorerScreen(viewModel: FileExplorerViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ExplorerTopBar(
                state          = state,
                onNavigateUp   = { viewModel.navigateUp() },
                onNavigateHome = { viewModel.navigateHome() },
                onRefresh      = { viewModel.refresh() },
                onToggleHidden = { viewModel.toggleHiddenFiles() },
            )
        },
    ) { innerPadding ->
        // Crossfade suaviza la transición entre los estados de pantalla
        val screenKey = when {
            state.isLoading       -> "loading"
            state.error != null   -> "error"
            state.items.isEmpty() -> "empty"
            else                  -> "content"
        }

        Crossfade(
            targetState  = screenKey,
            animationSpec = tween(durationMillis = 200),
            label        = "explorer_state",
            modifier     = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { key ->
            when (key) {
                "loading" -> LoadingIndicator()
                "error"   -> ErrorMessage(
                    message   = state.error ?: "Error desconocido",
                    onRetry   = { viewModel.refresh() },
                )
                "empty"   -> EmptyFolder()
                else      -> FileList(
                    items      = state.items,
                    onItemClick = { item ->
                        if (item.isDirectory) viewModel.navigateTo(item.file)
                    },
                )
            }
        }
    }
}

// ─── Top Bar ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplorerTopBar(
    state: ExplorerUiState,
    onNavigateUp: () -> Unit,
    onNavigateHome: () -> Unit,
    onRefresh: () -> Unit,
    onToggleHidden: () -> Unit,
) {
    val folderName = state.currentPath?.name?.ifEmpty { "Almacenamiento" } ?: "Almacenamiento"
    val fullPath   = state.currentPath?.absolutePath ?: ""
    val shortPath  = if (fullPath.length > 48) "…${fullPath.takeLast(45)}" else fullPath

    TopAppBar(
        title = {
            Column {
                Text(
                    text     = folderName,
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (shortPath.isNotEmpty()) {
                    Text(
                        text     = shortPath,
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        navigationIcon = {
            if (state.backStack.isNotEmpty()) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector     = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Subir un nivel",
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onNavigateHome) {
                Icon(
                    imageVector        = Icons.Filled.Home,
                    contentDescription = "Ir al inicio",
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector        = Icons.Filled.Refresh,
                    contentDescription = "Refrescar",
                )
            }
            IconButton(onClick = onToggleHidden) {
                Icon(
                    imageVector = if (state.showHidden) Icons.Filled.VisibilityOff
                                  else Icons.Filled.Visibility,
                    contentDescription = if (state.showHidden) "Ocultar archivos ocultos"
                                         else "Mostrar archivos ocultos",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

// ─── Lista de archivos ────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileList(
    items: List<FileItem>,
    onItemClick: (FileItem) -> Unit,
) {
    val folders = remember(items) { items.filter { it.isDirectory } }
    val files   = remember(items) { items.filter { !it.isDirectory } }

    LazyColumn(
        modifier        = Modifier.fillMaxSize(),
        contentPadding  = PaddingValues(bottom = 24.dp),
    ) {
        if (folders.isNotEmpty()) {
            stickyHeader(key = "header_folders") {
                SectionHeader(
                    label = "${folders.size} ${if (folders.size == 1) "carpeta" else "carpetas"}"
                )
            }
            items(folders, key = { it.file.absolutePath }) { item ->
                FileItemRow(item = item, onClick = { onItemClick(item) })
                HorizontalDivider(
                    modifier  = Modifier.padding(start = 70.dp),
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }

        if (files.isNotEmpty()) {
            stickyHeader(key = "header_files") {
                SectionHeader(
                    label = "${files.size} ${if (files.size == 1) "archivo" else "archivos"}"
                )
            }
            items(files, key = { it.file.absolutePath }) { item ->
                FileItemRow(item = item, onClick = { onItemClick(item) })
                HorizontalDivider(
                    modifier  = Modifier.padding(start = 70.dp),
                    thickness = 0.5.dp,
                    color     = MaterialTheme.colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text     = label.uppercase(),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun FileItemRow(item: FileItem, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Ícono con fondo de color semitransparente
        FileTypeIcon(item = item, modifier = Modifier.size(42.dp))

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text     = item.name,
                style    = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!item.isDirectory) {
                    Text(
                        text  = FileUtils.formatSize(item.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text  = dateFormat.format(Date(item.lastModified)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (item.isDirectory) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector        = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun FileTypeIcon(item: FileItem, modifier: Modifier = Modifier) {
    val icon = if (item.isDirectory) Icons.Filled.Folder else iconForExtension(item.extension)
    val tint = if (item.isDirectory) ColorFolder else fileTypeColor(item.extension)

    Surface(
        modifier = modifier,
        shape    = MaterialTheme.shapes.small,
        color    = tint.copy(alpha = 0.13f),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = tint,
                modifier           = Modifier.size(24.dp),
            )
        }
    }
}

private fun iconForExtension(ext: String): ImageVector = when (ext.lowercase()) {
    "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg" -> Icons.Filled.Image
    "mp4", "mkv", "avi", "mov", "webm", "ts"                  -> Icons.Filled.VideoFile
    "mp3", "aac", "ogg", "flac", "wav", "m4a"                 -> Icons.Filled.AudioFile
    "pdf"                                                       -> Icons.Filled.PictureAsPdf
    "doc", "docx"                                               -> Icons.Filled.Description
    "xls", "xlsx"                                               -> Icons.Filled.TableChart
    "ppt", "pptx"                                               -> Icons.Filled.Slideshow
    "zip", "rar", "7z", "tar", "gz"                             -> Icons.Filled.FolderZip
    "kt", "java", "py", "js", "ts", "html", "css",
    "json", "xml", "sh", "c", "cpp", "h"                       -> Icons.Filled.Code
    "txt", "md", "log", "csv"                                   -> Icons.Filled.TextSnippet
    else                                                        -> Icons.Filled.InsertDriveFile
}

// ─── Estados de la pantalla ───────────────────────────────────────────────────

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            color     = MaterialTheme.colorScheme.primary,
            modifier  = Modifier.size(48.dp),
        )
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector        = Icons.Filled.Error,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.error,
                modifier           = Modifier.size(56.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text  = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(
                    imageVector        = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyFolder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector        = Icons.Filled.FolderOpen,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.outline,
                modifier           = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text  = "No hay archivos en esta carpeta",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
