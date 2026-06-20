package com.scanner

import androidx.compose.ui.graphics.Color
import com.zebpay.scanner.ZScannerFrameRatio

sealed interface AppIntent {
    object StartScanning : AppIntent
    object StopScanning : AppIntent
    data class ScanSuccess(val data: String, val format: String) : AppIntent
    object ScanCancelled : AppIntent
    data class ScanFailed(val error: String?) : AppIntent
    data class SelectPresetRatio(val ratio: ZScannerFrameRatio) : AppIntent
    data class SelectCustomRatio(val ratioText: String) : AppIntent
    data class SelectPresetColor(val color: Color) : AppIntent
    data class SelectCustomColor(val colorText: String) : AppIntent
    data class ToggleCustomRatioMode(val enabled: Boolean) : AppIntent
    data class ToggleCustomColorMode(val enabled: Boolean) : AppIntent
    data class ScanImageResult(val qrCode: String?) : AppIntent
}
