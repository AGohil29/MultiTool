package org.arun.multitool.ui.transition

import androidx.compose.ui.geometry.Rect

data class SharedElementData(
    val initialBounds: Rect = Rect.Zero,
    val isTransitioning: Boolean = false
)
