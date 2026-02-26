package org.arun.multitool

import androidx.compose.runtime.Composable

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
