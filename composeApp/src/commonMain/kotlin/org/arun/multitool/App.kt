package org.arun.multitool

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.unit.dp
import org.arun.multitool.data.User
import org.arun.multitool.ui.common.LocalWindowAdaptiveInfo
import org.arun.multitool.ui.common.RecompositionTracker
import org.arun.multitool.ui.common.rememberHapticFeedback
import org.arun.multitool.ui.components.HapticManager
import org.arun.multitool.ui.transition.TransitionHandler
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
@Preview
fun App(isIOS: Boolean = false, someText: String = "Default Shared Text") {
    RecompositionTracker("App")
    // Capture the window layout state once, at the top of the composition tree.
    // currentWindowAdaptiveInfo() is the single cross-platform entry point provided
    // by the Material 3 adaptive library — no custom expect/actual sizing bridges needed.
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
//    val themeColor =
//        if (isIOS) Color(0xFF007AFF) else Color(0xFF6200EE) // iOS Blue vs Android Purple
//    // Koin finds the ViewModel and injects the Repository + Client automatically
//    val viewModel = koinViewModel<TimerViewModel>()
//    val seconds by viewModel.seconds.collectAsState()
//    val user by viewModel.userInfo.collectAsState()
//    val userData by viewModel.userState.collectAsState()
//    val users by viewModel.usersList.collectAsState()
//    Surface(
//        modifier = Modifier.fillMaxSize(), // 1. Fill entire screen
//        color = Color.DarkGray // Your background color
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                // 2. Apply padding only to the content to respect the Safe Area
//                .windowInsetsPadding(WindowInsets.systemBars),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Image(
//                painter = painterResource(Res.drawable.ic_timer),
//                contentDescription = "timer icon",
//                modifier = Modifier.size(60.dp)
//            )
//            Text(
//                text = stringResource(Res.string.time_label, seconds),
//                style = TextStyle(fontFamily = getRobotoMonoFontFamily(), fontSize = 22.sp)
//            )
//            NativeLabel(someText)
//            Text(text = user)
//            Button(onClick = { viewModel.onButtonClicked() }) {
//                Text("Trigger Platform Alert")
//            }
//
//            //UserScreen(userData)
//            UsersListContent(users) {}
//
//            // Button to manual sync
//            Button(onClick = { viewModel.refreshData(true) }) { Text("Refresh") }
//        }
//    }

    MaterialTheme {
        // Provide the captured state to the entire subtree through a CompositionLocal.
        // MainContainer reads it via currentAdaptiveInfo to drive NavigationSuiteScaffold.
        CompositionLocalProvider(LocalWindowAdaptiveInfo provides windowAdaptiveInfo) {
            MainContainer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersListContent(users: List<User>, columns: Int = 1, onUserClick: (User) -> Unit) {
    val scrollState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    scrollState.rememberHapticFeedback(koinInject())
//    val isAtTop by remember { derivedStateOf { scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0 } }

//    produceState(initialValue = Unit, scrollState) {
//        // Create a flow that combines the index and the "at top" status
//        snapshotFlow {
//            Pair(scrollState.firstVisibleItemIndex, scrollState.isScrollInProgress)
//        }
//            .distinctUntilChanged()
//            .collect { (index, isScrolling) ->
//                if (isScrolling) {
//                    haptic.selection()
//
//                    if (index == 0 && scrollState.firstVisibleItemScrollOffset == 0) {
//                        haptic.impact()
//                    }
//                }
//            }
//    }

//    LaunchedEffect(scrollState) {
//        snapshotFlow { scrollState.firstVisibleItemIndex }
//            .distinctUntilChanged()    // Only fire if the number actually changes
//            .collect { index ->
//                // Only trigger if the user is actually dragging/scrolling
//                if (scrollState.isScrollInProgress) {
//                    haptic.selection()
//                }
//            }
//    }
//
//    LaunchedEffect(isAtTop) {
//        if (isAtTop && scrollState.isScrollInProgress) {
//            haptic.impact()
//        }
//    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Team Members") })
        }
    ) { paddingValues ->
        if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No users found. Pull to refresh!")
            }
        } else {
            if (columns > 1) {
                // Expanded window (tablet / desktop) — two-column grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = gridState
                ) {
                    items(
                        items = users,
                        key = { it.id }
                    ) { user ->
                        UserCard(user = user, onClick = { onUserClick(user) })
                    }
                }
            } else {
                // Compact / Medium window — single-column list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = scrollState
                ) {
                    items(
                        items = users,
                        key = { it.id }     // Critical for smooth animations and performance
                    ) { user ->
                        UserCard(
                            user = user,
                            onClick = { onUserClick(user) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onClick: () -> Unit,
) {
    val haptic = koinInject<HapticManager>()
    val transitionHandler = koinInject<TransitionHandler>()
    var itemBounds by remember { mutableStateOf(Rect.Zero) }

    Card(
        modifier = Modifier
            .onGloballyPositioned { itemBounds = it.boundsInWindow() }
            .fillMaxWidth()
            .clickable {
                transitionHandler.updateBounds(itemBounds)
                haptic.impact()
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simple Avatar Placeholder
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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