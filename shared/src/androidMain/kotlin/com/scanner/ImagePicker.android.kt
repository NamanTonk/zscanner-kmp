package com.scanner

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnImagePicked = rememberUpdatedState(onImagePicked)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    currentOnImagePicked.value(bytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return remember {
        {
            launcher.launch("image/*")
        }
    }
}
