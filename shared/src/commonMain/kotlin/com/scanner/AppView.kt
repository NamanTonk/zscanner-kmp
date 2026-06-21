package com.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zscanner.*
import com.zscanner.permission.ZScannerPermissionController
import com.zscanner.ui.defaults.ScanFrameOverlay

@Composable
fun AppContent(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    permissionController: ZScannerPermissionController,
    imagePickerLauncher: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.showScanner) {
            ZScannerScreenLayout(
                state = state,
                onIntent = onIntent,
                permissionController = permissionController,
                imagePickerLauncher = imagePickerLauncher
            )
        } else {
            MainScreen(
                state = state,
                onIntent = onIntent
            )
        }
    }
}

@Composable
fun MainScreen(
    state: AppState,
    onIntent: (AppIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Vertical))
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            text = "ZScanner Integration",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Kotlin Multiplatform Barcode Scanner",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Results Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Scan Result",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (state.scanResultText.isNotEmpty()) {
                    Text(
                        text = state.scanResultText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    Text(
                        text = "Ready to Scan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Spacer(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                )

                Text(
                    text = state.scanStatusText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Frame Ratio Selection
        Text(
            text = "Frame Aspect Ratio",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp)
        ) {
            val presets = listOf(
                ZScannerFrameRatio.Ratio_1_1 to "1:1",
                ZScannerFrameRatio.Ratio_1_2 to "1:2",
                ZScannerFrameRatio.Ratio_2_1 to "2:1"
            )
            presets.forEach { (preset, label) ->
                val isSelected = !state.isCustomRatioSelected && state.selectedRatio == preset
                OutlinedButton(
                    onClick = {
                        onIntent(AppIntent.SelectPresetRatio(preset))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text(label)
                }
            }

            OutlinedButton(
                onClick = {
                    onIntent(AppIntent.ToggleCustomRatioMode(true))
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (state.isCustomRatioSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (state.isCustomRatioSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.isCustomRatioSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            ) {
                Text("Custom")
            }
        }

        if (state.isCustomRatioSelected) {
            OutlinedTextField(
                value = state.customRatioText,
                onValueChange = { newValue ->
                    onIntent(AppIntent.SelectCustomRatio(newValue))
                },
                label = { Text("Enter Ratio (e.g. 1.5)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Frame Color Selection
        Text(
            text = "Frame Border Color",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp)
        ) {
            val colorPresets = listOf(
                Color(0xFFEF5350) to "Red",
                Color(0xFF4CAF50) to "Green",
                Color(0xFFFF9800) to "Orange"
            )
            colorPresets.forEach { (colorVal, name) ->
                val isSelected = !state.isCustomColorSelected && state.selectedColor == colorVal
                OutlinedButton(
                    onClick = {
                        onIntent(AppIntent.SelectPresetColor(colorVal))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) colorVal else Color.Transparent,
                        contentColor = if (isSelected) Color.White else colorVal
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) colorVal else colorVal.copy(alpha = 0.5f)
                    )
                ) {
                    Text(name)
                }
            }

            val parsedCustomColor = parseHexColor(state.customColorText) ?: Color.Gray
            val isSelected = state.isCustomColorSelected
            OutlinedButton(
                onClick = {
                    onIntent(AppIntent.ToggleCustomColorMode(true))
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) parsedCustomColor else Color.Transparent,
                    contentColor = if (isSelected) Color.White else parsedCustomColor
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) parsedCustomColor else parsedCustomColor.copy(alpha = 0.5f)
                )
            ) {
                Text("Custom")
            }
        }

        if (state.isCustomColorSelected) {
            OutlinedTextField(
                value = state.customColorText,
                onValueChange = { newValue ->
                    onIntent(AppIntent.SelectCustomColor(newValue))
                },
                label = { Text("Enter Hex Color (e.g. #9C27B0)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 24.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Camera Mode Selection
        Text(
            text = "Camera Mode",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            val modes = listOf(
                ZScannerCameraMode.FullScreen to "Full Screen",
                ZScannerCameraMode.FrameOnly to "Frame Only"
            )
            modes.forEach { (mode, label) ->
                val isSelected = state.selectedCameraMode == mode
                OutlinedButton(
                    onClick = {
                        onIntent(AppIntent.SelectCameraMode(mode))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(label)
                }
            }
        }

        // Scan Trigger Button
        Button(
            onClick = { onIntent(AppIntent.StartScanning) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Scan Barcode / QR Code",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ZScannerScreenLayout(
    state: AppState,
    onIntent: (AppIntent) -> Unit,
    permissionController: ZScannerPermissionController,
    imagePickerLauncher: () -> Unit
) {
    ZScannerScreen(
        onResult = { result ->
            when (result) {
                is BarcodeResult.Success -> {
                    onIntent(AppIntent.ScanSuccess(result.barcode.data, result.barcode.format.name))
                }
                is BarcodeResult.Cancelled -> {
                    onIntent(AppIntent.ScanCancelled)
                }
                is BarcodeResult.Failed -> {
                    onIntent(AppIntent.ScanFailed(result.message))
                }
            }
        },
        onClose = {
            onIntent(AppIntent.StopScanning)
        },
        scannerController = rememberZScannerController(
            cameraMode = state.selectedCameraMode,
            frameRatio = state.selectedRatio,
            frameColor = state.selectedColor
        ),
        permissionController = permissionController,
        onScanFromGallery = imagePickerLauncher,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    )
}

// Local helper to parse preview/outline colors in the View layer if necessary
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
