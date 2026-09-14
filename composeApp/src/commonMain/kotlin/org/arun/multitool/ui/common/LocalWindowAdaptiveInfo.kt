package org.arun.multitool.ui.common

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf

/**
 * A [CompositionLocal] that holds the current [WindowAdaptiveInfo].
 *
 * Seeded once at the top of the composition tree inside App by calling
 * [currentWindowAdaptiveInfo] from the Material 3 adaptive library.
 *
 * Every child composable – shared across Android, iOS, and Desktop – can read the
 * value through [LocalWindowAdaptiveInfo.current] or the [currentAdaptiveInfo]
 * shorthand, without any custom expect/actual bridges.
 *
 * Example:
 * ```kotlin
 * val widthClass = currentAdaptiveInfo.windowSizeClass.windowWidthSizeClass
 * ```
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val LocalWindowAdaptiveInfo: ProvidableCompositionLocal<WindowAdaptiveInfo> =
    compositionLocalOf {
        error(
            "No WindowAdaptiveInfo provided. " +
                "Make sure App() seeds LocalWindowAdaptiveInfo via CompositionLocalProvider."
        )
    }

/**
 * Convenience read-only property – reads [LocalWindowAdaptiveInfo.current] from
 * any composable scope without requiring an explicit local reference.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val currentAdaptiveInfo: WindowAdaptiveInfo
    @Composable
    @ReadOnlyComposable
    get() = LocalWindowAdaptiveInfo.current

// ---------------------------------------------------------------------------
// ScreenSize – clean enum representing Material 3 window-width breakpoints
// ---------------------------------------------------------------------------

/**
 * Represents the three Material 3 window-width size classes with their
 * dp breakpoints:
 *
 * | ScreenSize   | Width Range  | Typical Devices                              |
 * |-------------|-------------|----------------------------------------------|
 * | [COMPACT]   | < 600 dp    | Phone portrait, split-screen mobile          |
 * | [MEDIUM]    | 600–839 dp  | Foldables unfolded, mini-tablets             |
 * | [EXPANDED]  | ≥ 840 dp    | iPads, desktop windows, landscape tablets    |
 */
enum class ScreenSize {
    /** < 600 dp – Phone portrait or split-screen mobile. */
    COMPACT,
    /** 600–839 dp – Foldables unfolded or mini-tablets. */
    MEDIUM,
    /** ≥ 840 dp – iPads, desktop windows, or landscape tablets. */
    EXPANDED,
}

// ---------------------------------------------------------------------------
// Convenience extension helpers on WindowAdaptiveInfo
// (Uses the Material 3 adaptive 1.2.x API: isWidthAtLeastBreakpoint)
// ---------------------------------------------------------------------------

/** Returns the current [ScreenSize] derived from the window width breakpoints. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val WindowAdaptiveInfo.screenSize: ScreenSize
    get() = when {
        windowSizeClass.isWidthAtLeastBreakpoint(840) -> ScreenSize.EXPANDED
        windowSizeClass.isWidthAtLeastBreakpoint(600) -> ScreenSize.MEDIUM
        else -> ScreenSize.COMPACT
    }

/** `true` when the window width class is **Compact** (< 600 dp – e.g. phone portrait). */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val WindowAdaptiveInfo.isCompact: Boolean
    get() = screenSize == ScreenSize.COMPACT

/** `true` when the window width class is **Medium** (600–839 dp – e.g. foldable / small tablet). */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val WindowAdaptiveInfo.isMedium: Boolean
    get() = screenSize == ScreenSize.MEDIUM

/** `true` when the window width class is **Expanded** (≥ 840 dp – e.g. large tablet / desktop). */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
val WindowAdaptiveInfo.isExpanded: Boolean
    get() = screenSize == ScreenSize.EXPANDED
