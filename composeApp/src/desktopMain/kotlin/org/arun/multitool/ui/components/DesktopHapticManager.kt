package org.arun.multitool.ui.components

/** No-op haptic manager for Desktop — JVM has no haptic hardware. */
class DesktopHapticManager : HapticManager {
    override fun impact() { /* no-op */ }
    override fun notification() { /* no-op */ }
    override fun selection() { /* no-op */ }
}

