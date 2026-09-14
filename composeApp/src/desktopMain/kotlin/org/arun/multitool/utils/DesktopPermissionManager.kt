package org.arun.multitool.utils

/** Stub permission manager for Desktop — all permissions are granted by default. */
class DesktopPermissionManager : PermissionManager {
    override fun getStatus(permission: PermissionType): PermissionStatus =
        PermissionStatus.Granted

    override fun openAppSettings() { /* no-op on desktop */ }
}

