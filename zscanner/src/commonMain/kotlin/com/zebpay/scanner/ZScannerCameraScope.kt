package com.zebpay.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Rect
import com.zebpay.scanner.frame.ScanFrameSpec

@Stable
class ZScannerCameraScope internal constructor(
    val controller: ZScannerController,
    val cameraMode: ZScannerCameraMode,
    val frameSpec: ScanFrameSpec,
    val frameBounds: Rect,
    private val onScanFromGalleryCallback: () -> Unit,
) {
    val torchEnabled: Boolean get() = controller.torchEnabled
    val torchAvailable: Boolean get() = controller.torchAvailable

    fun toggleTorch() = controller.toggleTorch()
    fun setTorch(enabled: Boolean) = controller.setTorchEnabled(enabled)

    /** v2 — no-op in v1 */
    fun onScanFromGallery() = onScanFromGalleryCallback()
}

val LocalZScannerCameraScope = staticCompositionLocalOf<ZScannerCameraScope> {
    error("ZScannerCameraScope not provided")
}

@Composable
internal fun ProvideZScannerCameraScope(
    scope: ZScannerCameraScope,
    content: @Composable ZScannerCameraScope.() -> Unit,
) {
    androidx.compose.runtime.CompositionLocalProvider(LocalZScannerCameraScope provides scope) {
        scope.content()
    }
}
