package com.zscanner

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.zscanner.frame.ScanFrameSpec
import com.zscanner.permission.ZCameraPermissionState
import com.zscanner.permission.ZScannerPermissionController
import com.zscanner.permission.ZScannerPermissionEvent
import com.zscanner.permission.ZScannerPermissionScope
import com.zscanner.permission.ProvideZScannerPermissionScope
import com.zscanner.ui.defaults.DefaultCameraContent
import com.zscanner.ui.defaults.DefaultChrome
import com.zscanner.ui.defaults.DefaultPermissionContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ZScannerScreen(
    onResult: (BarcodeResult) -> Unit,
    onClose: () -> Unit,
    permissionController: ZScannerPermissionController,
    modifier: Modifier = Modifier,
    frameSpec: ScanFrameSpec = ScanFrameSpec.Default,
    onPermissionEvent: (ZScannerPermissionEvent) -> Unit = {},
    chrome: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
        DefaultChrome(onClose = onClose, content = content)
    },
    permission: @Composable ZScannerPermissionScope.() -> Unit = { DefaultPermissionContent() },
    camera: @Composable ZScannerCameraScope.() -> Unit = { DefaultCameraContent() },
    scannerController: ZScannerController = rememberZScannerController(
        formats = BarcodeFormat.QR_CODE,
        cameraMode = ZScannerCameraMode.FullScreen
    ),
    onScanFromGallery: () -> Unit = {},
    loader: @Composable ZScannerCameraScope.() -> Unit = { DefaultLoader() },
) {
    val scope = rememberCoroutineScope()
    var permissionState by remember { mutableStateOf(ZCameraPermissionState.NotDetermined) }

    suspend fun refreshPermission() {
        permissionState = permissionController.currentState()
    }

    LaunchedEffect(Unit) {
        refreshPermission()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        scope.launch { refreshPermission() }
    }

    BackHandler {
        onResult(BarcodeResult.Cancelled)
    }

    chrome {
        when (permissionState) {
            ZCameraPermissionState.Granted -> {
                ZScanner(
                    controller = scannerController,
                    onResult = onResult,
                    modifier = modifier.fillMaxSize(),
                    frameSpec = frameSpec,
                    onScanFromGallery = {
                        scope.launch {
                            val galleryState = permissionController.currentGalleryState()
                            if (galleryState == ZCameraPermissionState.Granted) {
                                onScanFromGallery()
                            } else {
                                val newState = permissionController.requestGalleryPermission()
                                if (newState == ZCameraPermissionState.Granted) {
                                    onScanFromGallery()
                                } else if (newState == ZCameraPermissionState.DeniedAlways) {
                                    permissionController.openAppSettings()
                                }
                            }
                        }
                    },
                    loader = loader,
                    content = camera,
                )
            }

            else -> {
                val permissionScope = ZScannerPermissionScope(
                    state = permissionState,
                    requestPermissionAction = {
                        scope.launch {
                            val newState = permissionController.requestPermission()
                            permissionState = newState
                            when (newState) {
                                ZCameraPermissionState.Granted -> onPermissionEvent(
                                    ZScannerPermissionEvent.Granted
                                )

                                ZCameraPermissionState.Denied -> onPermissionEvent(
                                    ZScannerPermissionEvent.Denied
                                )

                                ZCameraPermissionState.DeniedAlways -> onPermissionEvent(
                                    ZScannerPermissionEvent.DeniedAlways
                                )

                                ZCameraPermissionState.NotDetermined -> Unit
                            }
                        }
                    },
                    openSettingsAction = {
                        permissionController.openAppSettings()
                        onPermissionEvent(ZScannerPermissionEvent.OpenedSettings)
                    },
                )
                ProvideZScannerPermissionScope(permissionScope) {
                    permissionScope.permission()
                }
            }
        }
    }
}
