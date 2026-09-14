package org.arun.multitool.utils

enum class PermissionStatus {
    Granted,
    Denied,            // Can ask again
    PermanentlyDenied  // Must go to Settings
}

interface PermissionManager {
    fun getStatus(permission: PermissionType): PermissionStatus
    fun openAppSettings()
}

enum class PermissionType { CAMERA, LOCATION }