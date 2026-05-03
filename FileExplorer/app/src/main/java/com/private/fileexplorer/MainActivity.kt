package com.private.fileexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.private.fileexplorer.ui.FileExplorerScreen
import com.private.fileexplorer.ui.PermissionScreen
import com.private.fileexplorer.ui.theme.FileExplorerTheme
import com.private.fileexplorer.viewmodel.FileExplorerViewModel

/**
 * Única Activity de la app.
 *
 * Responsabilidades:
 *  - Aplicar el tema
 *  - Decidir qué pantalla mostrar según el estado de permisos
 *  - Revalidar el permiso cada vez que la app vuelve a primer plano
 */
class MainActivity : ComponentActivity() {

    private val viewModel: FileExplorerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FileExplorerTheme {
                val uiState by viewModel.uiState.collectAsState()

                if (uiState.hasStorageAccess) {
                    FileExplorerScreen(viewModel = viewModel)
                } else {
                    PermissionScreen(
                        onPermissionGranted = { viewModel.refreshPermissionState() },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Se llama al volver de los Ajustes del sistema, de otra app, etc.
        // Revalida el permiso sin que el usuario tenga que hacer nada más.
        viewModel.refreshPermissionState()
    }
}
