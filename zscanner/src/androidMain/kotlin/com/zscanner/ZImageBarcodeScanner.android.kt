package com.zscanner

import android.graphics.BitmapFactory
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

actual object ZImageBarcodeScanner {

    private val scanner = BarcodeScanning.getClient(
        com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )

    actual fun scanQrCode(
        imageData: ByteArray,
        controller: ZScannerController?,
        onResult: (String?) -> Unit,
    ) {
        controller?.isProcessing = true
        val startTime = System.currentTimeMillis()

        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        if (bitmap == null) {
            controller?.isProcessing = false
            onResult(null)
            return
        }
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val result = barcodes.firstOrNull { it.rawValue != null }?.rawValue
                deliverWithDelay(controller, startTime, result, onResult)
            }
            .addOnFailureListener {
                deliverWithDelay(controller, startTime, null, onResult)
            }
    }

    private fun deliverWithDelay(
        controller: ZScannerController?,
        startTime: Long,
        result: String?,
        onResult: (String?) -> Unit
    ) {
        val elapsed = System.currentTimeMillis() - startTime
        val remainingDelay = 1000 - elapsed
        CoroutineScope(Dispatchers.Main).launch {
            if (remainingDelay > 0) {
                delay(remainingDelay.milliseconds)
            }
            controller?.isProcessing = false
            onResult(result)
        }
    }
}
