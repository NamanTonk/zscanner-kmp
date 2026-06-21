@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.zscanner

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGImageRef
import platform.CoreGraphics.CGRectMake
import platform.CoreImage.CIDetector
import platform.CoreImage.CIDetectorTypeQRCode
import platform.CoreImage.CIImage
import platform.CoreImage.CIQRCodeFeature
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.UIKit.UIImage
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.Vision.VNBarcodeObservation
import platform.Vision.VNDetectBarcodesRequest
import platform.Vision.VNImageRequestHandler
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue

actual object ZImageBarcodeScanner {

    actual fun scanQrCode(
        imageData: ByteArray,
        onResult: (String?) -> Unit,
    ) {
        if (imageData.isEmpty()) {
            deliver(onResult, null)
            return
        }

        val data = imageData.toNSData()
        val workQueue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)
        dispatch_async(workQueue) {
            val payload = scanWithVision(data) ?: scanWithCiDetector(data)
            deliver(onResult, payload)
        }
    }

    private fun scanWithVision(data: NSData): String? {
        val cgImage = resolveCgImage(data) ?: return null
        var result: String? = null
        val handler = VNImageRequestHandler(cgImage, options = emptyMap<Any?, Any?>())
        val request = VNDetectBarcodesRequest { visionRequest, _ ->
            result = visionRequest?.results
                ?.mapNotNull { it as? VNBarcodeObservation }
                ?.firstOrNull { observation ->
                    observation.payloadStringValue?.isNotEmpty() == true
                }
                ?.payloadStringValue
        }

        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            handler.performRequests(listOf(request), error.ptr)
        }
        return result
    }

    private fun scanWithCiDetector(data: NSData): String? {
        val ciImage = CIImage.imageWithData(data) ?: return null
        val detector = CIDetector.detectorOfType(
            type = CIDetectorTypeQRCode,
            context = null,
            options = null,
        ) ?: return null
        val features = detector.featuresInImage(ciImage) ?: return null
        return features
            .mapNotNull { it as? CIQRCodeFeature }
            .firstOrNull { it.messageString?.isNotEmpty() == true }
            ?.messageString
    }

    private fun resolveCgImage(data: NSData): CGImageRef? {
        val image = UIImage(data = data) ?: return null
        image.CGImage?.let { return it }
        val width = image.size.useContents { width }
        val height = image.size.useContents { height }
        if (width <= 0.0 || height <= 0.0) return null
        UIGraphicsBeginImageContextWithOptions(image.size, opaque = false, scale = image.scale)
        image.drawInRect(CGRectMake(0.0, 0.0, width, height))
        val rendered = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return rendered?.CGImage
    }

    private fun deliver(onResult: (String?) -> Unit, value: String?) {
        dispatch_async(dispatch_get_main_queue()) {
            onResult(value)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData =
    usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
