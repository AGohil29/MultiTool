package utils

import org.arun.multitool.utils.PermissionManager
import org.arun.multitool.utils.PermissionStatus
import org.arun.multitool.utils.PermissionType
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

class IOSPermissionManager : PermissionManager {
    override fun getStatus(permission: PermissionType): PermissionStatus {
        return when (permission) {
            PermissionType.CAMERA -> {
                val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
                when (status) {
                    AVAuthorizationStatusAuthorized -> PermissionStatus.Granted
                    AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> PermissionStatus.PermanentlyDenied
                    else -> PermissionStatus.Denied
                }
            }

            PermissionType.LOCATION -> { /* Similar logic for CLLocationManager */ PermissionStatus.Denied
            }
        }
    }

    override fun openAppSettings() {
        val settingsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (settingsUrl != null) {
            UIApplication.sharedApplication.openURL(settingsUrl)
        }
    }
}