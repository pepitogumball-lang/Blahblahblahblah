package com.private.fileexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.private.fileexplorer.ui.FileExplorerScreen
import com.private.fileexplorer.ui.PermissionScreen
import com.private.fileexplorer.ui.theme.FileExplorerTheme
import com.private.fileexplorer.util.PermissionHelper
import com.private.fileexplorer.viewmodel.FileExplorerViewModel
import kotlinx.coroutines.launch

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
                        onPermissionGranted = {
                            viewModel.refreshPermissionState()
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Cada vez que la app vuelve a primer plano (p.ej. desde Ajustes)
        // revalidamos el estado de permisos.
        viewModel.refreshPermissionState()
    }
}
