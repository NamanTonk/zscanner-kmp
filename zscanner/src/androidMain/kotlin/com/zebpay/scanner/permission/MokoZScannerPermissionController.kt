package com.zebpay.scanner.permission

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.camera.CAMERA
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory

internal class MokoZScannerPermissionController(
    private val mokoController: PermissionsController,
    private val openSettings: () -> Unit,
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
        openSettings()
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
    val context = LocalContext.current
    val factory = rememberPermissionsControllerFactory()
    val mokoController = remember(factory) { factory.createPermissionsController() }
    BindEffect(mokoController)
    return remember(mokoController, context) {
        MokoZScannerPermissionController(
            mokoController = mokoController,
            openSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
        )
    }
}
