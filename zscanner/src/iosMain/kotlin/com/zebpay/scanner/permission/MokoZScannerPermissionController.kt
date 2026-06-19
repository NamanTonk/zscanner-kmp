package com.zebpay.scanner.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

internal class MokoZScannerPermissionController(
    private val mokoController: PermissionsController,
) : ZScannerPermissionController {

    override suspend fun currentState(): ZCameraPermissionState = readState()

    override suspend fun requestPermission(): ZCameraPermissionState {
        return try {
            if (!mokoController.isPermissionGranted(Permission.CAMERA)) {
                mokoController.providePermission(Permission.CAMERA)
            }
            ZCameraPermissionState.Granted
        } catch (_: DeniedAlwaysException) {
            ZCameraPermissionState.DeniedAlways
        } catch (_: DeniedException) {
            ZCameraPermissionState.Denied
        } catch (_: Exception) {
            ZCameraPermissionState.Denied
        }
    }

    override fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
        UIApplication.sharedApplication.openURL(url)
    }

    private suspend fun readState(): ZCameraPermissionState {
        return try {
            if (mokoController.isPermissionGranted(Permission.CAMERA)) {
                ZCameraPermissionState.Granted
            } else {
                ZCameraPermissionState.NotDetermined
            }
        } catch (_: DeniedAlwaysException) {
            ZCameraPermissionState.DeniedAlways
        } catch (_: Exception) {
            ZCameraPermissionState.NotDetermined
        }
    }
}

@Composable
actual fun rememberMokoCameraPermissionController(): ZScannerPermissionController {
    val factory = rememberPermissionsControllerFactory()
    val mokoController = remember(factory) { factory.createPermissionsController() }
    BindEffect(mokoController)
    return remember(mokoController) {
        MokoZScannerPermissionController(mokoController = mokoController)
    }
}
