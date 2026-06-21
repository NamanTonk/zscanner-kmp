@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.scanner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import platform.UIKit.*
import platform.Foundation.*
import platform.posix.memcpy
import platform.darwin.NSObject
import kotlinx.cinterop.*

@Composable
actual fun rememberImagePicker(onImagePicked: (ByteArray) -> Unit): () -> Unit {
    val currentOnImagePicked = rememberUpdatedState(onImagePicked)
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                if (image != null) {
                    val data = UIImageJPEGRepresentation(image, 0.8)
                    if (data != null) {
                        val bytes = ByteArray(data.length.toInt())
                        if (bytes.isNotEmpty()) {
                            val pinned = bytes.pin()
                            try {
                                memcpy(pinned.addressOf(0), data.bytes, data.length)
                            } finally {
                                pinned.unpin()
                            }
                        }
                        currentOnImagePicked.value(bytes)
                    }
                }
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    return remember {
        {
            val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            if (rootViewController != null) {
                val picker = UIImagePickerController()
                picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                picker.delegate = delegate
                rootViewController.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
