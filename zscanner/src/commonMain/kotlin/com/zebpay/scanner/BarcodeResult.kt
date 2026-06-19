package com.zebpay.scanner

sealed class BarcodeResult {
    data class Success(val barcode: Barcode) : BarcodeResult()
    data object Cancelled : BarcodeResult()
    data class Failed(val message: String? = null) : BarcodeResult()
}
