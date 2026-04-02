package org.arun.multitool.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.arun.multitool.ui.components.AnimatedProfileHeader
import org.arun.multitool.ui.components.AnimatedWaveBackground
import org.arun.multitool.ui.components.CommonAlertDialog
import org.arun.multitool.ui.components.HapticManager
import org.koin.compose.koinInject

data class UserDetailScreen(val userId: Int, val name: String) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var showDeleteDialog by remember { mutableStateOf(false) }
        val haptic = koinInject<HapticManager>()
        var isExpanded by remember { mutableStateOf(false) }

        if (showDeleteDialog) {
            CommonAlertDialog(
                title = "Delete User",
                message = "Are you sure you want to remove this team member?",
                onConfirm = {
                    haptic.notification()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedWaveBackground()

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        ),
                        title = { Text("Profile") },
                        navigationIcon = {
                            IconButton(onClick = { navigator.pop() }) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedProfileHeader(
                        name = name,
                        isExpanded = isExpanded,
                        onClick = {
                            isExpanded = !isExpanded
                            haptic.selection() // Tactile feedback on toggle
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "User ID: $userId",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Remove Member")
                    }
                }
            }
        }
    }
}