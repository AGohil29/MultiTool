package org.arun.multitool

import androidx.compose.runtime.Composable

class DesktopPlatform : Platform {
    override val name: String = "Desktop (JVM)"
}

@Composable
actual fun NativeLabel(text: String) {
}

actual fun getDeviceModel(): String {
   return "Desktop iOS"
}

actual fun getPlatform(): Platform {
    return DesktopPlatform()
}