package com.zebpay.scanner.ui.defaults

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zebpay.scanner.ZScannerCameraScope

@Composable
fun ZScannerCameraScope.DefaultCameraContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        ScanFrameOverlay()
        if (torchAvailable) {
            IconButton(
                onClick = ::toggleTorch,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp),
            ) {
                Icon(
                    imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = null,
                )
            }
        }
    }
}
