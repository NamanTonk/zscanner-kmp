package com.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zscanner.ZImageBarcodeScanner
import com.zscanner.permission.rememberZScannerPermissionController

@Composable
fun App() {
    val viewModel: AppViewModel = viewModel { AppViewModel() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissionController = rememberZScannerPermissionController()

    val imagePicker = rememberImagePicker { bytes ->
        ZImageBarcodeScanner.scanQrCode(bytes) { result ->
            viewModel.handleIntent(AppIntent.ScanImageResult(result))
        }
    }

    AppContent(
        state = state,
        onIntent = { viewModel.handleIntent(it) },
        permissionController = permissionController,
        imagePickerLauncher = { imagePicker() }
    )
}