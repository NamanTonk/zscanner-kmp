package com.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zebpay.scanner.BarcodeResult
import com.zebpay.scanner.ZScannerCameraMode
import com.zebpay.scanner.ZScannerScreen
import com.zebpay.scanner.ZScannerFrameRatio
import com.zebpay.scanner.permission.rememberZScannerPermissionController
import com.zebpay.scanner.rememberZScannerController
import com.zebpay.scanner.ui.defaults.ScanFrameOverlay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.Image
import com.zebpay.scanner.ZImageBarcodeScanner

@Composable
@Preview
fun App() {
    MaterialTheme {
        val permissionController = rememberZScannerPermissionController()
        var showScanner by remember { mutableStateOf(false) }
        var scanResultText by remember { mutableStateOf("") }
        var scanStatusText by remember { mutableStateOf("No active scans") }
        val imagePicker = rememberImagePicker { bytes ->
            ZImageBarcodeScanner.scanQrCode(bytes) { result ->
                if (result != null) {
                    scanResultText = result
                    scanStatusText = "Scan successful (Gallery)"
                } else {
                    scanStatusText = "No QR code found in selected image"
                }
                showScanner = false
            }
        }
        var selectedRatio by remember { mutableStateOf<ZScannerFrameRatio>(ZScannerFrameRatio.Ratio_1_1) }
        var selectedColor by remember { mutableStateOf(Color(0xFF4CAF50)) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (showScanner) {
                ZScannerScreen(
                    onResult = { result ->
                        when (result) {
                            is BarcodeResult.Success -> {
                                scanResultText = result.barcode.data
                                scanStatusText = "Scan successful (Format: ${result.barcode.format})"
                                showScanner = false
                            }
                            is BarcodeResult.Cancelled -> {
                                scanStatusText = "Scan cancelled by user"
                                showScanner = false
                            }
                            is BarcodeResult.Failed -> {
                                scanResultText = ""
                                scanStatusText = "Scan failed: ${result.message ?: "Unknown error"}"
                                showScanner = false
                            }
                        }
                    },
                    onClose = {
                        showScanner = false
                    },
                    camera = {
                        ScanFrameOverlay()
                        if (torchAvailable) {
                            IconButton(
                                onClick = ::toggleTorch,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(24.dp)
                                    .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(50))
                            ) {
                                Icon(
                                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Toggle Flash",
                                    tint = Color.Black
                                )
                            }
                        }
                        // Render a gallery button
                        IconButton(
                            onClick = { onScanFromGallery() }, // Call the callback
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp)
                                .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(50))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Scan from Gallery",
                                tint = Color.Black
                            )
                        }
                    },
                    scannerController = rememberZScannerController(
                        cameraMode = ZScannerCameraMode.FrameOnly,
                        frameRatio = selectedRatio,
                        frameColor = selectedColor
                    ),
                    permissionController = permissionController,
                    onScanFromGallery = { imagePicker() },
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeContentPadding()
                        .padding(24.dp),
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

                            if (scanResultText.isNotEmpty()) {
                                Text(
                                    text = scanResultText,
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
                                text = scanStatusText,
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
                    
                    var customRatioText by remember { mutableStateOf("1.5") }
                    var isCustomRatioSelected by remember { mutableStateOf(false) }

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
                            val isSelected = !isCustomRatioSelected && selectedRatio == preset
                            OutlinedButton(
                                onClick = {
                                    isCustomRatioSelected = false
                                    selectedRatio = preset
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
                                isCustomRatioSelected = true
                                val parsed = customRatioText.toFloatOrNull() ?: 1.0f
                                selectedRatio = ZScannerFrameRatio.Custom(parsed)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isCustomRatioSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isCustomRatioSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCustomRatioSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text("Custom")
                        }
                    }

                    if (isCustomRatioSelected) {
                        OutlinedTextField(
                            value = customRatioText,
                            onValueChange = { newValue ->
                                customRatioText = newValue
                                val parsed = newValue.toFloatOrNull()
                                if (parsed != null && parsed > 0.1f) {
                                    selectedRatio = ZScannerFrameRatio.Custom(parsed)
                                }
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
                    
                    var customColorText by remember { mutableStateOf("#9C27B0") }
                    var isCustomColorSelected by remember { mutableStateOf(false) }

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
                            val isSelected = !isCustomColorSelected && selectedColor == colorVal
                            OutlinedButton(
                                onClick = {
                                    isCustomColorSelected = false
                                    selectedColor = colorVal
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
                        
                        val parsedCustomColor = parseHexColor(customColorText) ?: Color.Gray
                        val isSelected = isCustomColorSelected
                        OutlinedButton(
                            onClick = {
                                isCustomColorSelected = true
                                selectedColor = parsedCustomColor
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

                    if (isCustomColorSelected) {
                        OutlinedTextField(
                            value = customColorText,
                            onValueChange = { newValue ->
                                customColorText = newValue
                                val parsed = parseHexColor(newValue)
                                if (parsed != null) {
                                    selectedColor = parsed
                                }
                            },
                            label = { Text("Enter Hex Color (e.g. #9C27B0)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(bottom = 32.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Scan Trigger Button
                    Button(
                        onClick = { showScanner = true },
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
        }
    }
}

fun parseHexColor(hex: String): Color? {
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