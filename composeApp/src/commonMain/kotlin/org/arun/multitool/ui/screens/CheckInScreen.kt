package org.arun.multitool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.arun.multitool.CameraPreview
import org.arun.multitool.hardware.LocationService
import org.arun.multitool.ui.viewmodels.CheckInViewModel
import org.arun.multitool.utils.PermissionGatekeeper
import org.arun.multitool.utils.PermissionManager
import org.arun.multitool.utils.PermissionType
import org.koin.compose.koinInject

data object CheckInScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<CheckInViewModel>()
        val location by viewModel.userLocation.collectAsState()
        val locationService = koinInject<LocationService>()
        val permissionManager = koinInject<PermissionManager>()

        // Identify if this screen is currently at the top of the stack.
        // During a pop transition, navigator.lastItem will already be the PREVIOUS screen,
        // so isTop will be false, allowing us to hide the native view immediately.
        val isTop = navigator.lastItem == CheckInScreen

        // 1. This "mounts" the platform-specific launcher
        // On iOS, this might do nothing. On Android, it creates the launcher.
        locationService.ProvidePermissionHandler()

        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            // LAYER 0: The Camera (Hardware Bridge)
            PermissionGatekeeper(
                permissionType = PermissionType.CAMERA,
                manager = permissionManager
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    if (isTop) {
                        CameraPreview(modifier = Modifier.fillMaxSize())
                    }
                }

                // LAYER 1: Location Overlay
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Current Location:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        // Displaying coordinates or a loading state
                        Text(
                            text = location?.let { "${it.latitude}, ${it.longitude}" }
                                ?: "Fetching GPS...",
                            style = MaterialTheme.typography.bodyMedium)

                        Button(onClick = { viewModel.requestLocation() }) {
                            Text("Allow Location Access")
                        }
                    }
                }
            }
        }
    }
}