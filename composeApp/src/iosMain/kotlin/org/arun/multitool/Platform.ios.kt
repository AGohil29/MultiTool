package org.arun.multitool

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.Foundation.NSError
import platform.QuartzCore.CALayer
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIColor
import platform.UIKit.UIDevice
import platform.UIKit.UILabel
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getDeviceModel(): String {
    val device = UIDevice.currentDevice
    return "${device.model} ${device.systemName} ${device.systemVersion}"
}

@Composable
actual fun NativeLabel(text: String) {
    UIKitView(
        factory = {
            UILabel().apply {
                this.text = text
                this.backgroundColor = platform.UIKit.UIColor.clearColor
                this.textColor = platform.UIKit.UIColor.whiteColor
                this.opaque = false
            }
        },
        // Force the Compose-side container to also be clear
        modifier = Modifier.background(Color.Transparent),
        update = { label ->
            // Recurse up the tree to clear parent backgrounds
            // that Compose might have made opaque.
            var parent = label.superview
            while (parent != null) {
                parent.backgroundColor = UIColor.clearColor
                parent.opaque = false
                parent = parent.superview
            }
            label.text = text
            // Optional: Re-force transparency on update to catch lifecycle changes
            label.superview?.backgroundColor = UIColor.clearColor
            label.superview?.opaque = false
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraPreview(modifier: Modifier) {
    // Manually check/request permission on launch
    LaunchedEffect(Unit) {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        if (status != AVAuthorizationStatusAuthorized) {
            AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                if (granted) println("Camera Access Granted")
            }
        }
    }

    val isSimulator = remember {
        UIDevice.currentDevice.name.contains("Simulator")
    }

    if (isSimulator) {
        Box(modifier = modifier.background(Color.DarkGray), contentAlignment = Alignment.Center) {
            Text("Camera not available on Simulator", color = Color.White)
        }
    } else {
        val captureSession = remember { AVCaptureSession() }

        UIKitView(
            factory = {
                val container = UIView()
                container.backgroundColor =
                    UIColor.greenColor // Set to black so we know if it's the layer or the view
                // 2. Set up the camera device
                val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
                if (device == null) {
                    println("ERROR: No camera device found")
                }
                val input = device?.let {
                    var error: NSError? = null
                    val input = AVCaptureDeviceInput.deviceInputWithDevice(it, error = null)
                    if (input == null) println("ERROR: Could not create input")
                    input as? AVCaptureDeviceInput
                }

                if (input != null && captureSession.canAddInput(input)) {
                    captureSession.addInput(input)
                } else {
                    println("ERROR: Cannot add input to session")
                }

                // 3. Create the Preview Layer
                val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
                    videoGravity = AVLayerVideoGravityResizeAspectFill
                }

                // IMPORTANT: Give the layer an initial frame
                previewLayer.setFrame(container.bounds)
                container.layer.addSublayer(previewLayer)

                // 4. Start the session on a background thread (Senior Practice)
                dispatch_async(
                    dispatch_get_global_queue(
                        DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(),
                        0u
                    )
                ) {
                    captureSession.startRunning()
                    // Verify if it's running
                    if (captureSession.isRunning()) {
                        println("SUCCESS: Capture session is running")
                    }
                }

                container
            }, modifier = modifier,
            onResize = { view, rect ->
                // 5. Update the layer frame manually on resize/rotation
                CATransaction.begin()
                CATransaction.setValue(true, kCATransactionDisableActions)
                view.layer.sublayers?.forEach { (it as CALayer).frame = rect }
                CATransaction.commit()
            },
            onRelease = {
                captureSession.stopRunning()
            }
        )
    }
}