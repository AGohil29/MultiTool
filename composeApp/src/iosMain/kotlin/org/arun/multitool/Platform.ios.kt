package org.arun.multitool

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIColor
import platform.UIKit.UIDevice
import platform.UIKit.UILabel

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
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