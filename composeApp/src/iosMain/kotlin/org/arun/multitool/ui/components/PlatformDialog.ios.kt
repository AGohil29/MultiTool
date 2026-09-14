package org.arun.multitool.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.uikit.LocalUIViewController
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert

@Composable
actual fun CommonAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String,
    dismissText: String,
) {
    // We access the root ViewController of the iOS App
    val viewController = LocalUIViewController.current

    SideEffect {
        val alert = UIAlertController.alertControllerWithTitle(
            title = title,
            message = message,
            preferredStyle = UIAlertControllerStyleAlert
        )

        alert.addAction(
            UIAlertAction.actionWithTitle(
                title = confirmText,
                style = UIAlertActionStyleDefault,
                handler = {
                    onConfirm()
                }
            )
        )

        alert.addAction(
            UIAlertAction.actionWithTitle(
                title = dismissText,
                style = UIAlertActionStyleCancel,
                handler = {
                    onDismiss()
                }
            )
        )

        viewController.presentViewController(alert, animated = true, completion = null)
    }
}