package org.arun.multitool.ui.transition

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Rect

class TransitionHandler {
    // We use a private MutableState and a public State to follow
    // Encapsulation principles
    private val _state = mutableStateOf(SharedElementData())
    val state: State<SharedElementData> = _state

    fun updateBounds(bounds: Rect) {
        _state.value = SharedElementData(initialBounds = bounds, isTransitioning = true)
    }

    fun reset() {
        _state.value = SharedElementData(initialBounds = Rect.Zero, isTransitioning = false)
    }

    fun completeTransition() {
        _state.value = _state.value.copy(isTransitioning = false)
    }
}