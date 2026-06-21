package com.zscanner.internal

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.zscanner.BarcodeFormat

internal class BarcodeAnalyzer(
    private val onBarcode: (com.zscanner.Barcode) -> Unit,
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )

    @Volatile
    private var isProcessing = false

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        if (isProcessing) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }
        isProcessing = true
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees,
        )
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                val raw = barcodes.firstOrNull { it.rawValue != null }?.rawValue
                if (raw != null) {
                    onBarcode(
                        com.zscanner.Barcode(
                            data = raw,
                            format = BarcodeFormat.QR_CODE,
                        ),
                    )
                }
            }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }
}
