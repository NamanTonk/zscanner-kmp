package com.zscanner.ui.defaults

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zscanner.generated.resources.Res
import com.zscanner.generated.resources.zscanner_open_settings
import com.zscanner.generated.resources.zscanner_permission_allow
import com.zscanner.generated.resources.zscanner_permission_body
import com.zscanner.generated.resources.zscanner_permission_denied
import com.zscanner.generated.resources.zscanner_permission_denied_always
import com.zscanner.generated.resources.zscanner_permission_title
import com.zscanner.permission.ZCameraPermissionState
import com.zscanner.permission.ZScannerPermissionScope
import org.jetbrains.compose.resources.stringResource

@Composable
fun ZScannerPermissionScope.DefaultPermissionContent() {
    val title = when (state) {
        ZCameraPermissionState.DeniedAlways -> stringResource(Res.string.zscanner_permission_denied_always)
        ZCameraPermissionState.Denied -> stringResource(Res.string.zscanner_permission_denied)
        else -> stringResource(Res.string.zscanner_permission_title)
    }
    val body = when (state) {
        ZCameraPermissionState.DeniedAlways -> stringResource(Res.string.zscanner_permission_denied_always)
        else -> stringResource(Res.string.zscanner_permission_body)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
        )
        if (state == ZCameraPermissionState.DeniedAlways) {
            Button(onClick = ::openAppSettings) {
                Text(stringResource(Res.string.zscanner_open_settings))
            }
        } else {
            Button(onClick = ::requestPermission) {
                Text(stringResource(Res.string.zscanner_permission_allow))
            }
        }
    }
}
