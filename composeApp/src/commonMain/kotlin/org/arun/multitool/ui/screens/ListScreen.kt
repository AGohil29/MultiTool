package org.arun.multitool.ui.screens

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.arun.multitool.ui.viewmodels.TimerViewModel
import org.arun.multitool.UsersListContent
import org.arun.multitool.ui.common.currentAdaptiveInfo
import org.arun.multitool.ui.common.RecompositionTracker
import org.arun.multitool.ui.common.ScreenSize
import org.arun.multitool.ui.common.screenSize
import org.arun.multitool.ui.transition.TransitionHandler
import org.koin.compose.koinInject

data object UserListScreen : Screen {
    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    @Composable
    override fun Content() {
        RecompositionTracker("UserListScreen")
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<TimerViewModel>()
        val users by viewModel.usersList.collectAsState()
        val transitionHandler = koinInject<TransitionHandler>()

        // Read the window layout state that was captured once in App() and
        // pushed down through LocalWindowAdaptiveInfo — no expect/actual needed.
        val adaptiveInfo = currentAdaptiveInfo
        val columns = when (adaptiveInfo.screenSize) {
            ScreenSize.COMPACT -> 1  // < 600 dp – single column for phones
            ScreenSize.MEDIUM  -> 1  // 600–839 dp – single column for foldables/mini-tablets
            ScreenSize.EXPANDED -> 2 // ≥ 840 dp – two columns for tablets/desktop
        }

        // When this screen is destroyed (Navigator.pop), reset the bounds
        DisposableEffect(Unit) {
            onDispose {
                transitionHandler.reset()
            }
        }

        UsersListContent(
            users = users,
            columns = columns,
            onUserClick = { user ->
                navigator.push(UserDetailScreen(user.id, user.name))
            }
        )
    }

}