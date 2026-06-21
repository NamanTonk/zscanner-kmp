package com.zscanner.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

internal class AndroidZScannerPermissionController(
    private val context: Context,
    private val checkPermission: (String) -> ZCameraPermissionState,
    private val requestPermissionAction: (String, (ZCameraPermissionState) -> Unit) -> Unit,
    private val openSettings: () -> Unit,
) : ZScannerPermissionController {

    override suspend fun currentState(): ZCameraPermissionState {
        return checkPermission(android.Manifest.permission.CAMERA)
    }

    override suspend fun currentGalleryState(): ZCameraPermissionState {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return checkPermission(permission)
    }

    override suspend fun requestPermission(): ZCameraPermissionState = suspendCancellableCoroutine { continuation ->
        requestPermissionAction(android.Manifest.permission.CAMERA) { state ->
            if (continuation.isActive) {
                continuation.resume(state)
            }
        }
    }

    override suspend fun requestGalleryPermission(): ZCameraPermissionState = suspendCancellableCoroutine { continuation ->
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestPermissionAction(permission) { state ->
            if (continuation.isActive) {
                continuation.resume(state)
            }
        }
    }

    override fun openAppSettings() {
        openSettings()
    }
}

@Composable
actual fun rememberZScannerPermissionController(): ZScannerPermissionController {
    val context = LocalContext.current
    var permissionCallback by remember { mutableStateOf<((ZCameraPermissionState) -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val state = if (isGranted) ZCameraPermissionState.Granted else ZCameraPermissionState.Denied
        permissionCallback?.invoke(state)
        permissionCallback = null
    }

    fun checkPermission(permission: String): ZCameraPermissionState {
        val res = ContextCompat.checkSelfPermission(context, permission)
        if (res == PackageManager.PERMISSION_GRANTED) {
            return ZCameraPermissionState.Granted
        }
        val activity = context as? Activity
        if (activity != null) {
            val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            if (!showRationale) {
                val prefs = context.getSharedPreferences("zscanner_permissions", Context.MODE_PRIVATE)
                val requestedBefore = prefs.getBoolean(permission, false)
                if (requestedBefore) {
                    return ZCameraPermissionState.DeniedAlways
                }
            }
        }
        return ZCameraPermissionState.Denied
    }

    return remember(context, launcher) {
        AndroidZScannerPermissionController(
            context = context,
            checkPermission = ::checkPermission,
            requestPermissionAction = { permission, callback ->
                val activity = context as? Activity
                val prefs = context.getSharedPreferences("zscanner_permissions", Context.MODE_PRIVATE)
                prefs.edit().putBoolean(permission, true).apply()

                permissionCallback = { state ->
                    if (state == ZCameraPermissionState.Denied && activity != null) {
                        val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
                        if (!showRationale) {
                            callback(ZCameraPermissionState.DeniedAlways)
                        } else {
                            callback(state)
                        }
                    } else {
                        callback(state)
                    }
                }
                launcher.launch(permission)
            },
            openSettings = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        )
    }
}
