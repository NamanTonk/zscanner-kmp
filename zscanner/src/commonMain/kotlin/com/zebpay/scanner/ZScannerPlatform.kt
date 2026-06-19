package com.zebpay.scanner

/**
 * On iOS, [UIKitView] cannot be full-screen transparent (CMP renders an opaque surface).
 * Host the camera preview only inside the scan frame window instead.
 */
internal expect fun iosUsesWindowedCameraHost(): Boolean
