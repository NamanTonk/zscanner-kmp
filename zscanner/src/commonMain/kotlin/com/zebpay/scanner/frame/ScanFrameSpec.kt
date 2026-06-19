package com.zebpay.scanner.frame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ScanFrameSpec(
    /** Frame width as a fraction of the container width (0–1). */
    val widthFraction: Float = 0.82f,
    /** Frame height as a fraction of the container height (0–1). */
    val heightFraction: Float = 0.58f,
    val cornerRadius: Dp = 32.dp,
    /**
     * Vertical placement of the frame: 0 = top, 0.5 = centered, 1 = bottom.
     * Values below 0.5 shift the frame upward (e.g. 0.46 leaves room for bottom controls).
     */
    val verticalCenterFraction: Float = 0.46f,
) {
    companion object {
        val Default = ScanFrameSpec()

        /** Legacy square frame (min dimension × [widthFraction]). */
        val Square = ScanFrameSpec(
            widthFraction = 0.78f,
            heightFraction = 0.78f,
            cornerRadius = 24.dp,
            verticalCenterFraction = 0.5f,
        )
    }
}

@Composable
fun rememberScanFrameBounds(
    spec: ScanFrameSpec = ScanFrameSpec.Default,
    containerWidthPx: Float,
    containerHeightPx: Float,
): Rect {
    val density = LocalDensity.current
    return remember(spec, containerWidthPx, containerHeightPx, density) {
        if (spec.widthFraction == spec.heightFraction && spec.verticalCenterFraction == 0.5f) {
            val side = minOf(containerWidthPx, containerHeightPx) * spec.widthFraction
            val left = (containerWidthPx - side) / 2f
            val top = (containerHeightPx - side) / 2f
            Rect(left, top, left + side, top + side)
        } else {
            val frameWidth = containerWidthPx * spec.widthFraction.coerceIn(0.1f, 1f)
            val frameHeight = containerHeightPx * spec.heightFraction.coerceIn(0.1f, 1f)
            val left = (containerWidthPx - frameWidth) / 2f
            val top = (containerHeightPx - frameHeight) * spec.verticalCenterFraction.coerceIn(0f, 1f)
            Rect(left, top, left + frameWidth, top + frameHeight)
        }
    }
}

internal fun ScanFrameSpec.effectiveCornerRadiusPx(frameBounds: Rect, density: Density): Float {
    val requested = with(density) { cornerRadius.toPx() }
    val maxRadius = minOf(frameBounds.width, frameBounds.height) / 2f
    return minOf(requested, maxRadius)
}
