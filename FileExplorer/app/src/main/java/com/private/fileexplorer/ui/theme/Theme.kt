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

private val DarkColorScheme = darkColorScheme(
    primary   = Blue80,
    secondary = BlueGrey80,
    tertiary  = Teal80,
    surface   = SurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary   = Blue40,
    secondary = BlueGrey40,
    tertiary  = Teal40,
    surface   = SurfaceLight,
)

@Composable
fun FileExplorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content,
    )
}

/** Devuelve el color asociado a un tipo de archivo según su extensión. */
fun fileTypeColor(extension: String): Color = when (extension.lowercase()) {
    "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "svg" -> ColorImage
    "mp4", "mkv", "avi", "mov", "webm", "ts"                  -> ColorVideo
    "mp3", "aac", "ogg", "flac", "wav", "m4a"                 -> ColorAudio
    "pdf"                                                       -> ColorPdf
    "doc", "docx", "xls", "xlsx", "ppt", "pptx"               -> ColorDoc
    "zip", "rar", "7z", "tar", "gz"                             -> ColorArchive
    "kt", "java", "py", "js", "ts", "html", "css",
    "json", "xml", "sh", "c", "cpp", "h"                       -> ColorCode
    "apk"                                                       -> ColorApk
    else                                                        -> ColorGeneric
}
