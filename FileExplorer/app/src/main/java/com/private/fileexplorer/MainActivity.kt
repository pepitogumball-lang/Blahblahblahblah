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
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Revalida el permiso cada vez que el usuario vuelve a la app
        // (por ejemplo, después de activarlo en los Ajustes del sistema).
        viewModel.refreshPermissionState()
    }
}
