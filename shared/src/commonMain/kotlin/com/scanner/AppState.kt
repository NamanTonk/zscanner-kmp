package com.scanner

import androidx.compose.ui.graphics.Color
import com.zscanner.ZScannerFrameRatio
import com.zscanner.ZScannerCameraMode

data class AppState(
    val showScanner: Boolean = false,
    val scanResultText: String = "",
    val scanStatusText: String = "No active scans",
    val selectedRatio: ZScannerFrameRatio = ZScannerFrameRatio.Ratio_1_1,
    val selectedColor: Color = Color(0xFF4CAF50),
    val customRatioText: String = "1.5",
    val isCustomRatioSelected: Boolean = false,
    val customColorText: String = "#9C27B0",
    val isCustomColorSelected: Boolean = false,
    val selectedCameraMode: ZScannerCameraMode = ZScannerCameraMode.FullScreen,
)
