package com.zebpay.scanner.ui.defaults

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zebpay.scanner.ZScannerCameraMode
import com.zebpay.scanner.ZScannerCameraScope
import com.zebpay.scanner.frame.ScanFrameSpec
import com.zebpay.scanner.frame.effectiveCornerRadiusPx

@Composable
fun ZScannerCameraScope.ScanFrameOverlay(
    spec: ScanFrameSpec = frameSpec,
    mode: ZScannerCameraMode = cameraMode,
    scrimColor: Color = Color.Black.copy(alpha = 0.5f),
    borderColor: Color = Color.White,
    borderWidth: Dp = 3.dp,
    innerBorderColor: Color? = null,
    innerBorderWidth: Dp = 2.dp,
) {
    val density = LocalDensity.current
    val cornerRadiusPx = spec.effectiveCornerRadiusPx(frameBounds, density)
    val borderWidthPx = with(density) { borderWidth.toPx() }
    val innerBorderWidthPx = with(density) { innerBorderWidth.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        if (mode == ZScannerCameraMode.FullScreen) {
            drawRoundedFrameScrim(
                frameBounds = frameBounds,
                cornerRadiusPx = cornerRadiusPx,
                scrimColor = scrimColor,
            )
        }
        drawRoundRect(
            color = borderColor,
            topLeft = Offset(frameBounds.left, frameBounds.top),
            size = Size(frameBounds.width, frameBounds.height),
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            style = Stroke(width = borderWidthPx),
        )
        innerBorderColor?.let { innerColor ->
            drawInnerFrameBorder(
                frameBounds = frameBounds,
                cornerRadiusPx = cornerRadiusPx,
                insetPx = borderWidthPx,
                color = innerColor,
                strokeWidthPx = innerBorderWidthPx,
            )
        }
    }
}

private fun DrawScope.drawInnerFrameBorder(
    frameBounds: Rect,
    cornerRadiusPx: Float,
    insetPx: Float,
    color: Color,
    strokeWidthPx: Float,
) {
    if (frameBounds.width <= insetPx * 2f || frameBounds.height <= insetPx * 2f) return
    val innerBounds = Rect(
        left = frameBounds.left + insetPx,
        top = frameBounds.top + insetPx,
        right = frameBounds.right - insetPx,
        bottom = frameBounds.bottom - insetPx,
    )
    val innerCornerRadius = (cornerRadiusPx - insetPx).coerceAtLeast(0f)
    drawRoundRect(
        color = color,
        topLeft = Offset(innerBounds.left, innerBounds.top),
        size = Size(innerBounds.width, innerBounds.height),
        cornerRadius = CornerRadius(innerCornerRadius, innerCornerRadius),
        style = Stroke(width = strokeWidthPx),
    )
}

internal fun DrawScope.drawRoundedFrameScrim(
    frameBounds: Rect,
    cornerRadiusPx: Float,
    scrimColor: Color,
) {
    val scrimPath = Path().apply {
        addRect(Rect(0f, 0f, size.width, size.height))
        addRoundRect(
            RoundRect(
                rect = frameBounds,
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
            ),
        )
        fillType = PathFillType.EvenOdd
    }
    drawPath(scrimPath, scrimColor)
}
