package com.zscanner

/**
 * Scans a QR code from an image picked by the host app (gallery / photo library).
 */
expect object ZImageBarcodeScanner {
    fun scanQrCode(
        imageData: ByteArray,
        controller: ZScannerController? = null,
        onResult: (String?) -> Unit,
    )
}
