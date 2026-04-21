package org.arun.multitool.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.arun.multitool.ui.common.PermissionErrorUI

@Composable
fun PermissionGatekeeper(
    permissionType: PermissionType,
    manager: PermissionManager,
    content: @Composable () -> Unit,
) {
    var status by remember { mutableStateOf(manager.getStatus(permissionType)) }

    // Re-check status when the user returns to the app from Settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                status = manager.getStatus(permissionType)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (status) {
        PermissionStatus.Granted -> content()

        PermissionStatus.Denied -> {
            PermissionErrorUI(
                title = "Camera Access Required",
                description = "We need the camera to verify your check-in location.",
                buttonText = "Grant Permission",
                onAction = { /* Call your existing permission launcher here */ }
            )
        }

        PermissionStatus.PermanentlyDenied -> {
            PermissionErrorUI(
                title = "Camera Access Blocked",
                description = "You've disabled camera access in system settings. Please enable it to continue.",
                buttonText = "Open App Settings",
                onAction = { manager.openAppSettings() }
            )
        }
    }
}