package com.zebpay.scanner

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.zebpay.scanner.internal.BarcodeAnalyzer
import java.util.concurrent.Executors

@Composable
internal actual fun ZCameraPreview(
    controller: ZScannerController,
    modifier: Modifier,
    onBarcodeDetected: (Barcode) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    fun bindCamera(provider: ProcessCameraProvider) {
        provider.unbindAll()
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, BarcodeAnalyzer(onBarcode = onBarcodeDetected))
            }
        val camera = provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageAnalysis,
        )
        controller.bind(AndroidScannerHandle(camera))
    }

    DisposableEffect(lifecycleOwner, controller) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = future.get()
            cameraProvider = provider
            if (!controller.isPaused) {
                bindCamera(provider)
            }
        }, ContextCompat.getMainExecutor(context))
        onDispose {
            controller.unbind()
            cameraProvider?.unbindAll()
            cameraProvider = null
            executor.shutdown()
        }
    }

    LaunchedEffect(controller.isPaused, cameraProvider) {
        val provider = cameraProvider ?: return@LaunchedEffect
        if (controller.isPaused) {
            controller.setTorchEnabled(false)
            provider.unbindAll()
        } else {
            bindCamera(provider)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}

private class AndroidScannerHandle(
    private val camera: Camera,
) : PlatformScannerHandle {

    override fun setTorch(enabled: Boolean) {
        runCatching { camera.cameraControl.enableTorch(enabled) }
    }

    override fun isTorchAvailable(): Boolean = camera.cameraInfo.hasFlashUnit()

    override fun pause() {
        setTorch(false)
    }

    override fun resume() {
    }

    override fun release() {
        setTorch(false)
    }
}
