package org.arun.multitool

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import org.arun.multitool.ui.common.RecompositionTracker
import org.arun.multitool.ui.common.ScreenSize
import org.arun.multitool.ui.common.currentAdaptiveInfo
import org.arun.multitool.ui.common.screenSize
import org.arun.multitool.ui.screens.CheckInScreen
import org.arun.multitool.ui.screens.SettingsScreen
import org.arun.multitool.ui.screens.UserListScreen

// ---------------------------------------------------------------------------
// Top-level navigation tabs
// ---------------------------------------------------------------------------

/** Sealed hierarchy representing each root destination in the app. */
sealed class AppTab(
    val label: String,
    val contentDescription: String,
    val rootScreen: Screen,
) {
    data object Directory : AppTab("Directory", "Users", UserListScreen)
    data object CheckIn : AppTab("Check In", "Check In", CheckInScreen)
    data object Config : AppTab("Config", "Settings", SettingsScreen)
}

private val ALL_TABS: List<AppTab> = listOf(
    AppTab.Directory,
    AppTab.CheckIn,
    AppTab.Config,
)

// ---------------------------------------------------------------------------
// MainContainer
// ---------------------------------------------------------------------------

/**
 * Top-level shell that wraps every root destination inside a
 * [NavigationSuiteScaffold].
 *
 * The scaffold automatically chooses the right navigation chrome at runtime:
 * - **Compact** windows  → bottom navigation bar  (phone portrait)
 * - **Medium** windows   → navigation rail        (foldable / small tablet)
 * - **Expanded** windows → persistent nav drawer  (large tablet / desktop)
 *
 * The layout type is derived from [currentAdaptiveInfo] and [ScreenSize] —
 * the same [WindowAdaptiveInfo] captured once in [App] and pushed down through
 * [LocalWindowAdaptiveInfo] — so no expect/actual sizing code is needed.
 *
 * Screen-size breakpoints:
 * - **Compact**  (< 600 dp)  → bottom navigation bar  (phone portrait)
 * - **Medium**   (600–839 dp) → navigation rail        (foldable / small tablet)
 * - **Expanded** (≥ 840 dp)  → persistent nav drawer  (large tablet / desktop)
 *
 * Each tab owns an isolated Voyager [Navigator] (created fresh via [key] when
 * the tab changes), preserving full deep-navigation within a tab while keeping
 * the back stack clean on tab switches.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainContainer() {
    RecompositionTracker("MainContainer")
    var selectedTab by remember { mutableStateOf<AppTab>(AppTab.Directory) }

    // Read the WindowAdaptiveInfo that was provided by App().
    val adaptiveInfo = currentAdaptiveInfo

    // Determine the current screen size class (Compact / Medium / Expanded).
    // This mirrors the Material 3 WindowWidthSizeClass breakpoints:
    //   COMPACT  → < 600 dp  (phones)
    //   MEDIUM   → 600–839 dp (foldables, mini-tablets)
    //   EXPANDED → ≥ 840 dp  (iPads, desktops)
    val currentScreenSize = adaptiveInfo.screenSize

    NavigationSuiteScaffold(
        // Derives the chrome type (bottom bar / rail / drawer) from the current
        // WindowAdaptiveInfo — the single cross-platform breakpoint engine.
        layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo),
        navigationSuiteItems = {
            ALL_TABS.forEach { tab ->
                item(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    icon = {
                        Icon(
                            imageVector = when (tab) {
                                AppTab.Directory -> Icons.AutoMirrored.Filled.List
                                AppTab.CheckIn -> Icons.Default.LocationOn
                                AppTab.Config -> Icons.Default.Settings
                            },
                            contentDescription = tab.contentDescription,
                        )
                    },
                    label = { Text(tab.label) },
                )
            }
        },
    ) {
        // Isolate each tab's back stack: re-create the Navigator when the tab changes.
        key(selectedTab) {
            Navigator(screen = selectedTab.rootScreen) { navigator ->
                SlideTransition(navigator) { screen ->
                    screen.Content()
                }
            }
        }
    }
}
