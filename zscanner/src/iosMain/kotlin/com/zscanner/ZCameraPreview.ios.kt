@file:OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)

package com.zscanner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIColor
import platform.UIKit.UIView

@Composable
internal actual fun ZCameraPreview(
    controller: ZScannerController,
    modifier: Modifier,
    onBarcodeDetected: (Barcode) -> Unit,
) {
    val session = remember(controller) {
        com.zscanner.internal.ZScannerCaptureSession(onBarcode = onBarcodeDetected)
    }

    DisposableEffect(controller, session) {
        controller.bind(session)
        if (!controller.isPaused) {
            session.start()
        }
        onDispose {
            controller.unbind()
        }
    }

    DisposableEffect(controller.isPaused) {
        if (controller.isPaused) {
            session.pause()
        } else {
            session.resume()
        }
        onDispose { }
    }

    // Parent sizes this host to the scan frame on iOS (see iosUsesWindowedCameraHost).
    Box(modifier = modifier) {
        UIKitView(
            factory = {
                ZScannerPreviewContainer(session.previewLayer)
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.layoutPreviewLayer()
            },
            onRelease = {
                session.release()
            },
            properties = UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = false,
            ),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ZScannerPreviewContainer(
    private val previewLayer: platform.AVFoundation.AVCaptureVideoPreviewLayer,
) : UIView(cValue { CGRectZero.readValue() }) {

    init {
        clipsToBounds = true
        setOpaque(true)
        backgroundColor = UIColor.blackColor
        previewLayer.videoGravity = platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(previewLayer)
        layoutPreviewLayer()
    }

    fun layoutPreviewLayer() {
        CATransaction.begin()
        CATransaction.setValue(true, kCATransactionDisableActions)
        previewLayer.frame = layer.bounds
        CATransaction.commit()
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        layoutPreviewLayer()
    }
}
