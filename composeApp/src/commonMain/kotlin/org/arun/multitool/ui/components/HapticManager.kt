package org.arun.multitool.ui.components

interface HapticManager {
    fun impact()    // Light tap for button clicks
    fun notification() // Success/Error double tap
    fun selection()    // Subtle tick for scrolling/picking
}