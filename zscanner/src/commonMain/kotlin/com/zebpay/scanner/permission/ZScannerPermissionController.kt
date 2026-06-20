package com.zebpay.scanner.permission

interface ZScannerPermissionController {
    suspend fun currentState(): ZCameraPermissionState
    suspend fun requestPermission(): ZCameraPermissionState
    suspend fun currentGalleryState(): ZCameraPermissionState
    suspend fun requestGalleryPermission(): ZCameraPermissionState
    fun openAppSettings()
}
