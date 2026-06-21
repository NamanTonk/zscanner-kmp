package com.zscanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun rememberZScannerController(
    formats: BarcodeFormat = BarcodeFormat.QR_CODE,
    cameraMode: ZScannerCameraMode = ZScannerCameraMode.FullScreen,
    frameRatio: ZScannerFrameRatio = ZScannerFrameRatio.Ratio_1_1,
    frameColor: Color = Color(0xFF4CAF50),
    showTorchButton: Boolean = true,
    showGalleryButton: Boolean = true,
): ZScannerController {
    val controller = remember(formats, cameraMode, frameRatio, frameColor, showTorchButton, showGalleryButton) {
        ZScannerController(
            formats = formats,
            cameraMode = cameraMode,
            frameRatio = frameRatio,
            frameColor = frameColor,
            showTorchButton = showTorchButton,
            showGalleryButton = showGalleryButton
        )
    }
    return controller
}

class ZScannerController internal constructor(
    val formats: BarcodeFormat,
    val cameraMode: ZScannerCameraMode,
    val frameRatio: ZScannerFrameRatio,
    val frameColor: Color,
    val showTorchButton: Boolean,
    val showGalleryButton: Boolean,
) {
    internal var platformHandle: PlatformScannerHandle? = null

    private var _torchEnabled by mutableStateOf(false)
    val torchEnabled: Boolean get() = _torchEnabled

    private var _torchAvailable by mutableStateOf(false)
    val torchAvailable: Boolean get() = _torchAvailable

    var isPaused by mutableStateOf(false)
        private set

    var isProcessing by mutableStateOf(false)

    internal var frameBounds: ScanFrameBounds? = null

    internal fun updateTorchState(enabled: Boolean, available: Boolean) {
        _torchEnabled = enabled
        _torchAvailable = available
    }

    fun setTorchEnabled(enabled: Boolean) {
        if (!_torchAvailable) return
        platformHandle?.setTorch(enabled)
        _torchEnabled = enabled
    }

    fun toggleTorch() {
        setTorchEnabled(!torchEnabled)
    }

    fun pause() {
        isPaused = true
        setTorchEnabled(false)
        platformHandle?.pause()
    }

    fun resume() {
        isPaused = false
        platformHandle?.resume()
    }

    internal fun bind(handle: PlatformScannerHandle) {
        platformHandle = handle
        _torchAvailable = handle.isTorchAvailable()
        _torchEnabled = false
    }

    internal fun unbind() {
        setTorchEnabled(false)
        platformHandle?.release()
        platformHandle = null
    }
}

internal interface PlatformScannerHandle {
    fun setTorch(enabled: Boolean)
    fun isTorchAvailable(): Boolean
    fun pause()
    fun resume()
    fun release()
}

data class ScanFrameBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val cornerRadiusPx: Float = 0f,
)
