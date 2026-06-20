@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.zebpay.scanner.internal

import com.zebpay.scanner.Barcode
import com.zebpay.scanner.BarcodeFormat
import com.zebpay.scanner.PlatformScannerHandle
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.hasTorch
import platform.AVFoundation.torchMode
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import kotlin.concurrent.Volatile

private const val TORCH_MODE_OFF = 0L
private const val TORCH_MODE_ON = 1L

internal class ZScannerCaptureSession(
    private val onBarcode: (Barcode) -> Unit,
) : PlatformScannerHandle {

    val captureSession = AVCaptureSession()
    val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)
    private var device: AVCaptureDevice? = null
    private val sessionQueue = dispatch_queue_create("com.zebpay.scanner.session", null)
    private var isRunning = false
    private val metadataDelegate = MetadataDelegate { value ->
        onBarcode(Barcode(data = value, format = BarcodeFormat.QR_CODE))
    }

    fun start() {
        dispatch_async(sessionQueue) {
            if (isRunning) return@dispatch_async
            if (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) != AVAuthorizationStatusAuthorized) {
                return@dispatch_async
            }
            captureSession.beginConfiguration()
            captureSession.sessionPreset = AVCaptureSessionPresetHigh

            val camera = AVCaptureDevice.defaultDeviceWithDeviceType(
                AVCaptureDeviceTypeBuiltInWideAngleCamera,
                AVMediaTypeVideo,
                AVCaptureDevicePositionBack,
            ) ?: run {
                captureSession.commitConfiguration()
                return@dispatch_async
            }
            device = camera

            val input = AVCaptureDeviceInput.deviceInputWithDevice(camera, error = null)
            if (input != null && captureSession.canAddInput(input)) {
                captureSession.addInput(input)
            }

            val metadataOutput = AVCaptureMetadataOutput()
            if (captureSession.canAddOutput(metadataOutput)) {
                captureSession.addOutput(metadataOutput)
                metadataOutput.setMetadataObjectsDelegate(metadataDelegate, dispatch_get_main_queue())
                metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
            }

            captureSession.commitConfiguration()
            captureSession.startRunning()
            isRunning = true
        }
    }

    fun stop() {
        dispatch_async(sessionQueue) {
            if (!isRunning) return@dispatch_async
            captureSession.stopRunning()
            isRunning = false
            setTorch(false)
            metadataDelegate.reset()
        }
    }

    override fun setTorch(enabled: Boolean) {
        val cam = device ?: return
        if (!cam.hasTorch) return
        runCatching {
            cam.lockForConfiguration(null)
            cam.torchMode = if (enabled) TORCH_MODE_ON else TORCH_MODE_OFF
            cam.unlockForConfiguration()
        }
    }

    override fun isTorchAvailable(): Boolean {
        val cam = device ?: AVCaptureDevice.defaultDeviceWithDeviceType(
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
            AVMediaTypeVideo,
            AVCaptureDevicePositionBack,
        )
        return cam?.hasTorch == true
    }

    override fun pause() = stop()

    override fun resume() = start()

    override fun release() = stop()

    private class MetadataDelegate(
        private val onDetected: (String) -> Unit,
    ) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

        @Volatile
        private var delivered = false

        fun reset() {
            delivered = false
        }

        @ObjCSignatureOverride
        override fun captureOutput(
            output: platform.AVFoundation.AVCaptureOutput,
            didOutputMetadataObjects: List<*>,
            fromConnection: platform.AVFoundation.AVCaptureConnection,
        ) {
            if (delivered) return
            val readable = didOutputMetadataObjects
                .filterIsInstance<AVMetadataMachineReadableCodeObject>()
                .firstOrNull()
                ?.stringValue
            if (readable != null) {
                delivered = true
                onDetected(readable)
            }
        }
    }
}
