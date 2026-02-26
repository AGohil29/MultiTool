package org.arun.multitool

import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication

class IosNotifier: PlatformNotifier {
    override fun showToast(message: String) {
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        val alert = UIAlertController.alertControllerWithTitle(
            title = null,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )
        alert.addAction(UIAlertAction.actionWithTitle("OK", style = UIAlertActionStyleDefault, handler = null))
        rootViewController?.presentViewController(alert, animated = true, completion = null)
    }
}