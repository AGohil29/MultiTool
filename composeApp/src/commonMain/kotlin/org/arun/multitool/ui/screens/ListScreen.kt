package org.arun.multitool.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.arun.multitool.ui.viewmodels.TimerViewModel
import org.arun.multitool.UsersListContent

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