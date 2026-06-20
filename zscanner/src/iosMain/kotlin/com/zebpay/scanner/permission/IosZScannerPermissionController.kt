package com.zebpay.scanner.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusDenied
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHAuthorizationStatusRestricted
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHPhotoLibrary
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class IosZScannerPermissionController : ZScannerPermissionController {

    override suspend fun currentState(): ZCameraPermissionState {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return when (status) {
            AVAuthorizationStatusAuthorized -> ZCameraPermissionState.Granted
            AVAuthorizationStatusDenied -> ZCameraPermissionState.DeniedAlways
            AVAuthorizationStatusRestricted -> ZCameraPermissionState.DeniedAlways
            AVAuthorizationStatusNotDetermined -> ZCameraPermissionState.NotDetermined
            else -> ZCameraPermissionState.NotDetermined
        }
    }

    override suspend fun requestPermission(): ZCameraPermissionState = suspendCancellableCoroutine { continuation ->
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
            val state = if (granted) ZCameraPermissionState.Granted else ZCameraPermissionState.DeniedAlways
            continuation.resume(state)
        }
    }

    override suspend fun currentGalleryState(): ZCameraPermissionState {
        val status = PHPhotoLibrary.authorizationStatus()
        return when (status) {
            PHAuthorizationStatusAuthorized -> ZCameraPermissionState.Granted
            PHAuthorizationStatusLimited -> ZCameraPermissionState.Granted
            PHAuthorizationStatusDenied -> ZCameraPermissionState.DeniedAlways
            PHAuthorizationStatusRestricted -> ZCameraPermissionState.DeniedAlways
            PHAuthorizationStatusNotDetermined -> ZCameraPermissionState.NotDetermined
            else -> ZCameraPermissionState.NotDetermined
        }
    }

    override suspend fun requestGalleryPermission(): ZCameraPermissionState = suspendCancellableCoroutine { continuation ->
        PHPhotoLibrary.requestAuthorization { status ->
            val state = when (status) {
                PHAuthorizationStatusAuthorized -> ZCameraPermissionState.Granted
                PHAuthorizationStatusLimited -> ZCameraPermissionState.Granted
                PHAuthorizationStatusDenied -> ZCameraPermissionState.DeniedAlways
                PHAuthorizationStatusRestricted -> ZCameraPermissionState.DeniedAlways
                else -> ZCameraPermissionState.Denied
            }
            continuation.resume(state)
        }
    }

    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }
}

@Composable
actual fun rememberZScannerPermissionController(): ZScannerPermissionController {
    return remember { IosZScannerPermissionController() }
}
