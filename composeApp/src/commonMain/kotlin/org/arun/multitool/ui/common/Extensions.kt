package org.arun.multitool.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.arun.multitool.ui.components.HapticManager

@Composable
fun LazyListState.rememberHapticFeedback(haptic: HapticManager) {
    LaunchedEffect(this) {
        snapshotFlow { Pair(firstVisibleItemIndex, isScrollInProgress) }
            .distinctUntilChanged()
            .collect { (index, isScrolling) ->
                if (isScrolling) {
                    haptic.selection()
                    if (index == 0 && firstVisibleItemScrollOffset == 0) {
                        haptic.impact()
                    }
                }
            }
    }
}