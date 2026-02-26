package org.arun.multitool

import javax.swing.JOptionPane

class DesktopNotifier: PlatformNotifier {
    override fun showToast(message: String) {
        // This launches a standard system message dialog
        JOptionPane.showMessageDialog(
            null,
            message,
            "Notification",
            JOptionPane.INFORMATION_MESSAGE
        )
    }
}