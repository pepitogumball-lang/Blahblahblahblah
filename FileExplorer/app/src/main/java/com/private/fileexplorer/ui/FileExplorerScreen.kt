package com.private.fileexplorer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.private.fileexplorer.ui.theme.*
import com.private.fileexplorer.util.FileItem
import com.private.fileexplorer.util.FileManager
import com.private.fileexplorer.viewmodel.ExplorerUiState
import com.private.fileexplorer.viewmodel.FileExplorerViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileExplorerScreen(viewModel: FileExplorerViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ExplorerTopBar(
                state = state,
                onNavigateUp = { viewModel.navigateUp() },
                onNavigateHome = { viewModel.navigateHome() },
                onRefresh = { viewModel.refresh() },
                onToggleHidden = { viewModel.toggleHiddenFiles() },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> LoadingIndicator()
                state.error != null -> ErrorMessage(state.error!!)
                state.items.isEmpty() -> EmptyFolder()
                else -> FileList(
                    items = state.items,
                    onItemClick = { item ->
                        if (item.isDirectory) viewModel.navigateTo(item.file)
                    },
                )
            }
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExplorerTopBar(
    state: ExplorerUiState,
    onNavigateUp: () -> Unit,
    onNavigateHome: () -> Unit,
    onRefresh: () -> Unit,
    onToggleHidden: () -> Unit,
) {
    val pathLabel = state.currentPath?.absolutePath ?: "Almacenamiento"
    val shortPath = if (pathLabel.length > 40) "…${pathLabel.takeLast(37)}" else pathLabel

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "File Explorer",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = shortPath,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            if (state.backStack.isNotEmpty()) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Subir nivel",
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onNavigateHome) {
                Icon(Icons.Filled.Home, contentDescription = "Inicio")
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refrescar")
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
            containerColor = MaterialTheme.colorScheme.surface,
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
    val folders = items.filter { it.isDirectory }
    val files   = items.filter { !it.isDirectory }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        if (folders.isNotEmpty()) {
            stickyHeader {
                SectionHeader(
                    label = "${folders.size} ${if (folders.size == 1) "carpeta" else "carpetas"}"
                )
            }
            items(folders, key = { it.file.absolutePath }) { item ->
                FileItemRow(item = item, onClick = { onItemClick(item) })
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        if (files.isNotEmpty()) {
            stickyHeader {
                SectionHeader(
                    label = "${files.size} ${if (files.size == 1) "archivo" else "archivos"}"
                )
            }
            items(files, key = { it.file.absolutePath }) { item ->
                FileItemRow(item = item, onClick = { onItemClick(item) })
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun FileItemRow(item: FileItem, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icono de tipo
        FileTypeIcon(item = item, modifier = Modifier.size(40.dp))

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!item.isDirectory) {
                    Text(
                        text = FileManager.formatSize(item.size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = dateFormat.format(Date(item.lastModified)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (item.isDirectory) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun FileTypeIcon(item: FileItem, modifier: Modifier = Modifier) {
    val (icon, tint) = when {
        item.isDirectory -> Icons.Filled.Folder to ColorFolder
        else -> fileIconFor(item.extension) to fileTypeColor(item.extension)
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = tint.copy(alpha = 0.12f),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

private fun fileIconFor(extension: String): ImageVector = when (extension.lowercase()) {
    "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg" -> Icons.Filled.Image
    "mp4", "mkv", "avi", "mov", "webm", "ts"                  -> Icons.Filled.VideoFile
    "mp3", "aac", "ogg", "flac", "wav", "m4a"                 -> Icons.Filled.AudioFile
    "pdf"                                                       -> Icons.Filled.PictureAsPdf
    "doc", "docx"                                               -> Icons.Filled.Description
    "xls", "xlsx"                                               -> Icons.Filled.TableChart
    "ppt", "pptx"                                               -> Icons.Filled.Slideshow
    "zip", "rar", "7z", "tar", "gz"                             -> Icons.Filled.FolderZip
    "apk"                                                       -> Icons.Filled.Android
    "kt", "java", "py", "js", "ts", "html", "css",
    "json", "xml", "sh", "c", "cpp", "h"                       -> Icons.Filled.Code
    "txt", "md", "log", "csv"                                   -> Icons.Filled.TextSnippet
    else                                                        -> Icons.Filled.InsertDriveFile
}

// ─── Estados de UI ────────────────────────────────────────────────────────────

@Composable
private fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyFolder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Carpeta vacía",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
