package org.arun.multitool

import android.os.Build
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getDeviceModel(): String {
    return "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"
}

@Composable
actual fun NativeLabel(text: String) {
    Text(text = "Android view: $text")
}