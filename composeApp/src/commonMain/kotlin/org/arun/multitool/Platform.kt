package org.arun.multitool

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Platform {
    val name: String
}

interface PlatformNotifier {
    fun showToast(message: String)
}

expect fun getPlatform(): Platform

expect fun getDeviceModel(): String

@Composable
expect fun NativeLabel(text: String)

@Composable
expect fun CameraPreview(modifier: Modifier)