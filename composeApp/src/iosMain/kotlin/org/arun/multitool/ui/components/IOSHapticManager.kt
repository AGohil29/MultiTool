package org.arun.multitool.ui.components

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType
import platform.UIKit.UISelectionFeedbackGenerator

class IOSHapticManager: HapticManager {
    private val impactGen by lazy { UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium) }

    override fun impact() {
        impactGen.prepare()
        impactGen.impactOccurred()
    }

    override fun notification() {
        UINotificationFeedbackGenerator().apply {
            prepare()
            notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
        }
    }

    override fun selection() {
        UISelectionFeedbackGenerator().apply {
            prepare()
            selectionChanged()
        }
    }
}