package com.private.fileexplorer.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.private.fileexplorer.util.PermissionHelper

/**
 * Pantalla que explica al usuario por qué se necesita el permiso de almacenamiento
 * y ofrece el botón correcto según la versión de Android.
 *
 * [onPermissionGranted] se invoca cuando los permisos fueron concedidos
 * (en API 30+ el usuario vuelve de Ajustes y onResume revalúa el estado).
 */
@Composable
fun PermissionScreen(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current

    // Lanzador para permisos en tiempo de ejecución (API < 30)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) onPermissionGranted()
    }

    // Lanzador para abrir Ajustes del sistema (API 30+)
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Al regresar de Ajustes, revaluar el permiso
        if (PermissionHelper.hasAnyStorageAccess(context)) {
            onPermissionGranted()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Acceso al almacenamiento",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = PermissionHelper.permissionExplanation(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Indicador extra para API 30+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Busca \"${context.packageName}\" en la lista y activa el permiso.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        settingsLauncher.launch(PermissionHelper.buildSettingsIntent(context))
                    } else {
                        val perms = PermissionHelper.getRuntimePermissionsToRequest()
                        if (perms.isNotEmpty()) {
                            permissionLauncher.launch(perms)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(
                    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                        "Abrir ajustes del sistema"
                    else
                        "Conceder acceso"
                )
            }
        }
    }
}
