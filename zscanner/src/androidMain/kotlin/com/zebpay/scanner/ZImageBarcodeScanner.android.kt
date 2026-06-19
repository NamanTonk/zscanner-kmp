package com.zebpay.scanner

import android.graphics.BitmapFactory
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

actual object ZImageBarcodeScanner {

    private val scanner = BarcodeScanning.getClient(
        com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )

    actual fun scanQrCode(
        imageData: ByteArray,
        onResult: (String?) -> Unit,
    ) {
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        if (bitmap == null) {
            onResult(null)
            return
        }
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                onResult(barcodes.firstOrNull { it.rawValue != null }?.rawValue)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
