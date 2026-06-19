package com.zebpay.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
@Composable
fun rememberZScannerController(
    formats: Set<BarcodeFormat> = setOf(BarcodeFormat.QR_CODE),
    cameraMode: ZScannerCameraMode = ZScannerCameraMode.FullScreen,
): ZScannerController {
    val controller = remember(formats, cameraMode) {
        ZScannerController(formats = formats, cameraMode = cameraMode)
    }
    return controller
}

class ZScannerController internal constructor(
    val formats: Set<BarcodeFormat>,
    val cameraMode: ZScannerCameraMode,
) {
    internal var platformHandle: PlatformScannerHandle? = null

    private var _torchEnabled by mutableStateOf(false)
    val torchEnabled: Boolean get() = _torchEnabled

    private var _torchAvailable by mutableStateOf(false)
    val torchAvailable: Boolean get() = _torchAvailable

    var isPaused by mutableStateOf(false)
        private set

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
