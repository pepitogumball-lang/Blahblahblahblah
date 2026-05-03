package com.private.fileexplorer.ui

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.private.fileexplorer.permissions.PermissionHelper

/**
 * Pantalla de permisos.
 *
 * Explica claramente por qué la app necesita acceso al almacenamiento
 * y guía al usuario paso a paso para concederlo según su versión de Android.
 *
 * La Activity llama a [viewModel.refreshPermissionState] en onResume,
 * lo que provoca el cambio automático a la pantalla del explorador cuando
 * el usuario regresa de los Ajustes con el permiso concedido.
 */
@Composable
fun PermissionScreen(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current

    // Lanzador para runtime permissions (API 26–29)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) onPermissionGranted()
    }

    // Lanzador para abrir los Ajustes del sistema (API 30+)
    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Al volver de Ajustes, la Activity llamará onResume → refreshPermissionState
        if (PermissionHelper.hasAnyStorageAccess(context)) onPermissionGranted()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // ── Ícono de la app ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.extraLarge,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = Icons.Filled.Folder,
                    contentDescription = null,
                    modifier           = Modifier.size(56.dp),
                    tint               = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Título ────────────────────────────────────────────────────────
            Text(
                text       = "File Explorer",
                fontWeight = FontWeight.Bold,
                fontSize   = 26.sp,
                textAlign  = TextAlign.Center,
                color      = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text      = "Acceso al almacenamiento requerido",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // ── Por qué se necesita el permiso ────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    modifier          = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Lock,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text  = PermissionHelper.permissionRationale(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Instrucciones paso a paso ─────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = "Cómo conceder el permiso",
                        style      = MaterialTheme.typography.labelLarge,
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        StepRow("1", "Pulsa el botón de abajo")
                        StepRow("2", "Busca «${context.packageName}» en la lista")
                        StepRow("3", "Activa «${PermissionHelper.permissionSettingsLabel()}»")
                        StepRow("4", "Regresa a la app — se abrirá sola")
                    } else {
                        StepRow("1", "Pulsa el botón de abajo")
                        StepRow("2", "Acepta el permiso de almacenamiento cuando el sistema te lo solicite")
                        StepRow("3", "La app se abrirá automáticamente")
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Botón de acción ───────────────────────────────────────────────
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        settingsLauncher.launch(PermissionHelper.buildSettingsIntent(context))
                    } else {
                        val perms = PermissionHelper.getRuntimePermissionsToRequest()
                        if (perms.isNotEmpty()) permissionLauncher.launch(perms)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape  = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector        = Icons.Filled.LockOpen,
                    contentDescription = null,
                    modifier           = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text  = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                                "Abrir ajustes del sistema"
                            else
                                "Conceder acceso",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

// ─── Componente auxiliar: fila de paso ────────────────────────────────────────

@Composable
private fun StepRow(step: String, text: String) {
    Row(
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.Start,
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text       = step,
                style      = MaterialTheme.typography.labelSmall,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text  = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
