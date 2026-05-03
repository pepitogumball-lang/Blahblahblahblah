package com.private.fileexplorer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary              = Blue40,
    onPrimary            = Color.White,
    primaryContainer     = Blue90,
    onPrimaryContainer   = Blue10,
    secondary            = Teal40,
    onSecondary          = Color.White,
    secondaryContainer   = Teal80,
    onSecondaryContainer = Color(0xFF002018),
    tertiary             = Grey40,
    onTertiary           = Color.White,
    background           = Color(0xFFF8F9FA),
    onBackground         = Grey10,
    surface              = Color.White,
    onSurface            = Grey10,
    surfaceVariant       = Grey90,
    onSurfaceVariant     = Grey40,
    outline              = Color(0xFF90A4AE),
    outlineVariant       = Color(0xFFCFD8DC),
    error                = Color(0xFFD32F2F),
    onError              = Color.White,
    errorContainer       = Color(0xFFFFCDD2),
    onErrorContainer     = Color(0xFF7F0000),
)

private val DarkColors = darkColorScheme(
    primary              = Blue80,
    onPrimary            = Blue10,
    primaryContainer     = Blue40,
    onPrimaryContainer   = Blue90,
    secondary            = Teal80,
    onSecondary          = Color(0xFF002018),
    secondaryContainer   = Teal40,
    onSecondaryContainer = Teal80,
    tertiary             = Grey80,
    onTertiary           = Grey10,
    background           = Color(0xFF121212),
    onBackground         = Color(0xFFE1E2E5),
    surface              = Color(0xFF1E1E1E),
    onSurface            = Color(0xFFE1E2E5),
    surfaceVariant       = Color(0xFF263238),
    onSurfaceVariant     = Grey80,
    outline              = Grey40,
    outlineVariant       = Color(0xFF37474F),
    error                = Color(0xFFEF9A9A),
    onError              = Color(0xFF7F0000),
    errorContainer       = Color(0xFFB71C1C),
    onErrorContainer     = Color(0xFFFFCDD2),
)

@Composable
fun FileExplorerTheme(
    darkTheme: Boolean    = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}

/** Color asociado a la extensión del archivo para su ícono. */
fun fileTypeColor(extension: String): Color = when (extension.lowercase()) {
    "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg" -> ColorImage
    "mp4", "mkv", "avi", "mov", "webm", "ts"                  -> ColorVideo
    "mp3", "aac", "ogg", "flac", "wav", "m4a"                 -> ColorAudio
    "pdf"                                                       -> ColorPdf
    "doc", "docx", "xls", "xlsx", "ppt", "pptx"               -> ColorDoc
    "zip", "rar", "7z", "tar", "gz"                             -> ColorArchive
    "kt", "java", "py", "js", "ts", "html", "css",
    "json", "xml", "sh", "c", "cpp", "h"                       -> ColorCode
    "txt", "md", "log", "csv"                                   -> ColorText
    else                                                        -> ColorGeneric
}
