package org.arun.multitool

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
data object UserListScreen: Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<TimerViewModel>()
        val users by viewModel.usersList.collectAsState()

        UsersListContent(
            users = users,
            onUserClick = { user ->
                navigator.push(UserDetailScreen(user.id, user.name))
            }
        )
    }

}