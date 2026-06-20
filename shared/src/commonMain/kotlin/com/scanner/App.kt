package com.scanner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.zebpay.scanner.ZScannerController
import com.zebpay.scanner.ZScannerScreen
import com.zebpay.scanner.permission.rememberMokoCameraPermissionController
import com.zebpay.scanner.rememberZScannerController

@Composable
@Preview
fun App() {
    MaterialTheme {
        val permissionController = rememberMokoCameraPermissionController()
        var showScanner by remember { mutableStateOf(false) }
        var scanResultText by remember { mutableStateOf("") }
        var scanStatusText by remember { mutableStateOf("No active scans") }

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
                    scannerController = rememberZScannerController(cameraMode = ZScannerCameraMode.FrameOnly),
                    permissionController = permissionController,
                    modifier = Modifier.fillMaxSize()
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