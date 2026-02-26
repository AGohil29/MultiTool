package org.arun.multitool

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import multitool.composeapp.generated.resources.Res
import multitool.composeapp.generated.resources.ic_timer
import multitool.composeapp.generated.resources.time_label
import org.arun.multitool.data.UserEntity
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(isIOS: Boolean = false, someText: String = "Default Shared Text") {
    val themeColor =
        if (isIOS) Color(0xFF007AFF) else Color(0xFF6200EE) // iOS Blue vs Android Purple
    // Koin finds the ViewModel and injects the Repository + Client automatically
    val viewModel = koinViewModel<TimerViewModel>()
    val seconds by viewModel.seconds.collectAsState()
    val user by viewModel.userInfo.collectAsState()
    val userData by viewModel.userState.collectAsState()
    val users by viewModel.usersList.collectAsState()
    Surface(
        modifier = Modifier.fillMaxSize(), // 1. Fill entire screen
        color = Color.DarkGray // Your background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 2. Apply padding only to the content to respect the Safe Area
                .windowInsetsPadding(WindowInsets.systemBars),
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_timer),
                contentDescription = "timer icon",
                modifier = Modifier.size(60.dp)
            )
            Text(
                text = stringResource(Res.string.time_label, seconds),
                style = TextStyle(fontFamily = getRobotoMonoFontFamily(), fontSize = 22.sp)
            )
            NativeLabel(someText)
            Text(text = user)
            Button(onClick = { viewModel.onButtonClicked() }) {
                Text("Trigger Platform Alert")
            }

            //UserScreen(userData)
            UsersListScreen(users)
        }
    }
}

@Composable
fun UsersListScreen(users: List<UserEntity>) {
    LazyColumn {
        items(users) { user ->
            Text("Welcome, ${user.name}")
        }
    }
}

@Composable
fun UserScreen(state: NetworkResult<User>) {
    when (state) {
        is NetworkResult.Error -> Text("Error: ${state.message}", color = Color.Red)
        is NetworkResult.Loading -> CircularProgressIndicator()
        is NetworkResult.Success -> Text("Welcome, ${state.data.name}")
    }
}