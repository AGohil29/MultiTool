package org.arun.multitool.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AndroidPermissionManager(private val activity: ComponentActivity) : PermissionManager {
    override fun getStatus(permission: PermissionType): PermissionStatus {
        val androidPerm = when (permission) {
            PermissionType.CAMERA -> Manifest.permission.CAMERA
            PermissionType.LOCATION -> Manifest.permission.ACCESS_FINE_LOCATION
        }

        val isGranted = ContextCompat.checkSelfPermission(
            activity,
            androidPerm
        ) == PackageManager.PERMISSION_GRANTED
        if (isGranted) return PermissionStatus.Granted

        // Senior logic: Check if the user permanently denied
        val shouldShowRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, androidPerm)

        return if (!shouldShowRationale) {
            PermissionStatus.PermanentlyDenied
        } else {
            PermissionStatus.Denied
        }
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
    }
}