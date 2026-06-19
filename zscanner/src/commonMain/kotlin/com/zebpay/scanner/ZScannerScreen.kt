package com.zebpay.scanner

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
import com.zebpay.scanner.frame.ScanFrameSpec
import com.zebpay.scanner.permission.ZCameraPermissionState
import com.zebpay.scanner.permission.ZScannerPermissionController
import com.zebpay.scanner.permission.ZScannerPermissionEvent
import com.zebpay.scanner.permission.ZScannerPermissionScope
import com.zebpay.scanner.permission.ProvideZScannerPermissionScope
import com.zebpay.scanner.ui.defaults.DefaultCameraContent
import com.zebpay.scanner.ui.defaults.DefaultChrome
import com.zebpay.scanner.ui.defaults.DefaultPermissionContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ZScannerScreen(
    onResult: (BarcodeResult) -> Unit,
    onClose: () -> Unit,
    permissionController: ZScannerPermissionController,
    modifier: Modifier = Modifier,
    formats: Set<BarcodeFormat> = setOf(BarcodeFormat.QR_CODE),
    cameraMode: ZScannerCameraMode = ZScannerCameraMode.FullScreen,
    frameSpec: ScanFrameSpec = ScanFrameSpec.Default,
    onPermissionEvent: (ZScannerPermissionEvent) -> Unit = {},
    chrome: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
        DefaultChrome(onClose = onClose, content = content)
    },
    permission: @Composable ZScannerPermissionScope.() -> Unit = { DefaultPermissionContent() },
    camera: @Composable ZScannerCameraScope.() -> Unit = { DefaultCameraContent() },
    scannerController: ZScannerController? = null,
    onScanFromGallery: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    var permissionState by remember { mutableStateOf(ZCameraPermissionState.NotDetermined) }
    val defaultController = rememberZScannerController(formats = formats, cameraMode = cameraMode)
    val activeController = scannerController ?: defaultController

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
                    controller = activeController,
                    onResult = onResult,
                    modifier = modifier.fillMaxSize(),
                    frameSpec = frameSpec,
                    onScanFromGallery = onScanFromGallery,
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
                                ZCameraPermissionState.Granted -> onPermissionEvent(ZScannerPermissionEvent.Granted)
                                ZCameraPermissionState.Denied -> onPermissionEvent(ZScannerPermissionEvent.Denied)
                                ZCameraPermissionState.DeniedAlways -> onPermissionEvent(ZScannerPermissionEvent.DeniedAlways)
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
