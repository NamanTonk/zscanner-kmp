@file:OptIn(ExperimentalTime::class)

package com.zscanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.zscanner.frame.ScanFrameSpec
import com.zscanner.frame.effectiveCornerRadiusPx
import com.zscanner.frame.rememberScanFrameBounds
import com.zscanner.ui.defaults.drawRoundedFrameScrim
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val SCAN_DEBOUNCE_MS = 2_000L

@Composable
fun ZScanner(
    controller: ZScannerController,
    onResult: (BarcodeResult) -> Unit,
    modifier: Modifier = Modifier,
    frameSpec: ScanFrameSpec = ScanFrameSpec.Default,
    onScanFromGallery: () -> Unit = {},
    loader: @Composable ZScannerCameraScope.() -> Unit = { DefaultLoader() },
    content: @Composable ZScannerCameraScope.() -> Unit,
) {
    var lastScanAt by remember { mutableLongStateOf(0L) }
    val scope = rememberCoroutineScope()
 
    fun handleBarcode(barcode: Barcode) {
        if (controller.isPaused) return
        val now = Clock.System.now().toEpochMilliseconds()
        if (now - lastScanAt < SCAN_DEBOUNCE_MS) return
        lastScanAt = now
        
        controller.pause()
        controller.isProcessing = true
        
        scope.launch {
            delay(1000) // 1 second simulated processing delay
            controller.isProcessing = false
            onResult(BarcodeResult.Success(barcode))
        }
    }
 
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        controller.pause()
    }

    ZScannerCameraHost(
        controller = controller,
        modifier = modifier,
        frameSpec = frameSpec,
        onBarcodeDetected = ::handleBarcode,
        onScanFromGallery = onScanFromGallery,
        loader = loader,
        content = content,
    )
}

@Composable
internal fun ZScannerCameraHost(
    controller: ZScannerController,
    modifier: Modifier = Modifier,
    frameSpec: ScanFrameSpec = ScanFrameSpec.Default,
    onBarcodeDetected: (Barcode) -> Unit,
    onScanFromGallery: () -> Unit = {},
    loader: @Composable ZScannerCameraScope.() -> Unit = { DefaultLoader() },
    content: @Composable ZScannerCameraScope.() -> Unit,
) {
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val frameBounds = rememberScanFrameBounds(
            spec = frameSpec,
            frameRatio = controller.frameRatio,
            containerWidthPx = constraints.maxWidth.toFloat(),
            containerHeightPx = constraints.maxHeight.toFloat(),
        )
        val cornerRadiusPx = frameSpec.effectiveCornerRadiusPx(frameBounds, density)
        controller.frameBounds = ScanFrameBounds(
            left = frameBounds.left,
            top = frameBounds.top,
            right = frameBounds.right,
            bottom = frameBounds.bottom,
            cornerRadiusPx = cornerRadiusPx,
        )

        val useWindowedCameraHost =
            iosUsesWindowedCameraHost() && controller.cameraMode == ZScannerCameraMode.FullScreen
        val useFrameOnlyLayout =
            controller.cameraMode == ZScannerCameraMode.FrameOnly || useWindowedCameraHost

        if (useFrameOnlyLayout) {
            val cornerRadius = frameSpec.effectiveCornerRadiusPx(frameBounds, density)
            val shape = RoundedCornerShape(with(density) { cornerRadius.toDp() })
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(frameBounds.left.roundToInt(), frameBounds.top.roundToInt())
                    }
                    .size(
                        width = with(density) { frameBounds.width.toDp() },
                        height = with(density) { frameBounds.height.toDp() },
                    )
                    .clip(shape),
            ) {
                ZCameraPreview(
                    controller = controller,
                    modifier = Modifier.fillMaxSize(),
                    onBarcodeDetected = onBarcodeDetected,
                )
            }
            if (controller.cameraMode == ZScannerCameraMode.FrameOnly) {
                FrameOnlyScrim(
                    frameBounds = frameBounds,
                    cornerRadiusPx = cornerRadius,
                )
            }
        } else {
            ZCameraPreview(
                controller = controller,
                modifier = Modifier.fillMaxSize(),
                onBarcodeDetected = onBarcodeDetected,
            )
        }

        val scope = remember(controller, frameSpec, frameBounds) {
            ZScannerCameraScope(
                controller = controller,
                cameraMode = controller.cameraMode,
                frameSpec = frameSpec,
                frameBounds = frameBounds,
                onScanFromGalleryCallback = onScanFromGallery,
            )
        }
        ProvideZScannerCameraScope(scope) {
            scope.content()
        }

        if (controller.isProcessing) {
            ProvideZScannerCameraScope(scope) {
                scope.loader()
            }
        }
    }
}

@Composable
private fun FrameOnlyScrim(
    frameBounds: Rect,
    cornerRadiusPx: Float,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRoundedFrameScrim(
            frameBounds = frameBounds,
            cornerRadiusPx = cornerRadiusPx,
            scrimColor = Color.Black,
        )
    }
}

@Composable
internal fun ZScannerCameraScope.DefaultLoader(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val cornerRadius = frameSpec.effectiveCornerRadiusPx(frameBounds, density)
    val shape = RoundedCornerShape(with(density) { cornerRadius.toDp() })
    Box(
        modifier = modifier
            .offset {
                IntOffset(frameBounds.left.roundToInt(), frameBounds.top.roundToInt())
            }
            .size(
                width = with(density) { frameBounds.width.toDp() },
                height = with(density) { frameBounds.height.toDp() },
            )
            .clip(shape)
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = controller.frameColor,
            strokeWidth = 3.dp,
            trackColor = Color.White.copy(alpha = 0.1f),
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .size(48.dp)
                .drawBehind {
                    val radius = size.minDimension / 2f
                    val glowRadius = radius + 12.dp.toPx()
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                controller.frameColor.copy(alpha = 0.45f),
                                controller.frameColor.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = glowRadius
                        ),
                        radius = glowRadius,
                        center = center
                    )
                }
        )
    }
}
