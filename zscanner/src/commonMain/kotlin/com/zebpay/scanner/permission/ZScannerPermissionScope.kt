package com.zebpay.scanner.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

@Stable
class ZScannerPermissionScope internal constructor(
    val state: ZCameraPermissionState,
    private val requestPermissionAction: () -> Unit,
    private val openSettingsAction: () -> Unit,
) {
    fun requestPermission() = requestPermissionAction()
    fun openAppSettings() = openSettingsAction()
}

val LocalZScannerPermissionScope = staticCompositionLocalOf<ZScannerPermissionScope> {
    error("ZScannerPermissionScope not provided")
}

@Composable
internal fun ProvideZScannerPermissionScope(
    scope: ZScannerPermissionScope,
    content: @Composable ZScannerPermissionScope.() -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(LocalZScannerPermissionScope provides scope) {
        scope.content()
    }
}
