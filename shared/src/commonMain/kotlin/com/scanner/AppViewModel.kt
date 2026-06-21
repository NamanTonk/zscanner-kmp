package com.scanner

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.zscanner.ZScannerFrameRatio
import com.zscanner.ZScannerCameraMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel() {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun handleIntent(intent: AppIntent) {
        when (intent) {
            AppIntent.StartScanning -> {
                _state.update { it.copy(showScanner = true) }
            }
            AppIntent.StopScanning -> {
                _state.update { it.copy(showScanner = false) }
            }
            is AppIntent.ScanSuccess -> {
                _state.update {
                    it.copy(
                        scanResultText = intent.data,
                        scanStatusText = "Scan successful (Format: ${intent.format})",
                        showScanner = false
                    )
                }
            }
            AppIntent.ScanCancelled -> {
                _state.update {
                    it.copy(
                        scanStatusText = "Scan cancelled by user",
                        showScanner = false
                    )
                }
            }
            is AppIntent.ScanFailed -> {
                _state.update {
                    it.copy(
                        scanResultText = "",
                        scanStatusText = "Scan failed: ${intent.error ?: "Unknown error"}",
                        showScanner = false
                    )
                }
            }
            is AppIntent.SelectPresetRatio -> {
                _state.update {
                    it.copy(
                        selectedRatio = intent.ratio,
                        isCustomRatioSelected = false
                    )
                }
            }
            is AppIntent.SelectCustomRatio -> {
                val parsed = intent.ratioText.toFloatOrNull()
                _state.update {
                    it.copy(
                        customRatioText = intent.ratioText,
                        selectedRatio = if (parsed != null && parsed > 0.1f) ZScannerFrameRatio.Custom(parsed) else it.selectedRatio
                    )
                }
            }
            is AppIntent.SelectPresetColor -> {
                _state.update {
                    it.copy(
                        selectedColor = intent.color,
                        isCustomColorSelected = false
                    )
                }
            }
            is AppIntent.SelectCustomColor -> {
                val parsed = parseHexColor(intent.colorText)
                _state.update {
                    it.copy(
                        customColorText = intent.colorText,
                        selectedColor = parsed ?: it.selectedColor
                    )
                }
            }
            is AppIntent.ToggleCustomRatioMode -> {
                _state.update {
                    val parsed = it.customRatioText.toFloatOrNull() ?: 1.0f
                    it.copy(
                        isCustomRatioSelected = intent.enabled,
                        selectedRatio = if (intent.enabled) ZScannerFrameRatio.Custom(parsed) else ZScannerFrameRatio.Ratio_1_1
                    )
                }
            }
            is AppIntent.ToggleCustomColorMode -> {
                _state.update {
                    val parsed = parseHexColor(it.customColorText) ?: Color.Gray
                    it.copy(
                        isCustomColorSelected = intent.enabled,
                        selectedColor = if (intent.enabled) parsed else Color(0xFF4CAF50)
                    )
                }
            }
            is AppIntent.ScanImageResult -> {
                if (intent.qrCode != null) {
                    _state.update {
                        it.copy(
                            scanResultText = intent.qrCode,
                            scanStatusText = "Scan successful (Gallery)",
                            showScanner = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            scanStatusText = "No QR code found in selected image",
                            showScanner = false
                        )
                    }
                }
            }
            is AppIntent.SelectCameraMode -> {
                _state.update {
                    it.copy(selectedCameraMode = intent.mode)
                }
            }
        }
    }

    private fun parseHexColor(hex: String): Color? {
        val cleanHex = hex.trim().removePrefix("#")
        return try {
            when (cleanHex.length) {
                6 -> {
                    val colorLong = ("FF$cleanHex").toLong(16)
                    Color(colorLong)
                }
                8 -> {
                    val colorLong = cleanHex.toLong(16)
                    Color(colorLong)
                }
                else -> {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
