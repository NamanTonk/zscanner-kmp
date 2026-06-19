package com.zebpay.scanner.permission

interface ZScannerPermissionController {
    suspend fun currentState(): ZCameraPermissionState
    suspend fun requestPermission(): ZCameraPermissionState
    fun openAppSettings()
}
