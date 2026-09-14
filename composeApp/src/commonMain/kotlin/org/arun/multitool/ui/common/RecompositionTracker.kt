package org.arun.multitool.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * Debug utility that logs every recomposition of the composable it's placed in.
 *
 * Usage — drop this at the top of any @Composable function:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     RecompositionTracker("MyScreen")
 *     // ... rest of the composable
 * }
 * ```
 *
 * On each recomposition the Logcat / console output will show:
 * ```
 * 🔄 RECOMPOSITION #3 → MyScreen
 * ```
 *
 * **Important:** Remove or guard behind a `BuildConfig.DEBUG` flag before release!
 */
@Composable
fun RecompositionTracker(tag: String) {
    val counter = remember { RecompositionCounter() }
    counter.count++  // Increment during composition (not in SideEffect)
    SideEffect {
        // SideEffect runs after every successful recomposition —
        // perfect for logging without triggering further recomposition.
        println("🔄 RECOMPOSITION #${counter.count} → $tag")
    }
}

/** Simple holder to survive recompositions (remember keeps the same instance). */
private class RecompositionCounter {
    var count = 0
}

