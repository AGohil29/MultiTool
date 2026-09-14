package org.arun.multitool.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.arun.multitool.CameraPreview

@Composable
fun CameraCaptureScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // This calls the platform-specific implementation we wrote earlier
        CameraPreview(
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI (Buttons, Scoping brackets, etc.)
        IconButton(
            onClick = { /* Handle Capture */ },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Icon(Icons.Default.Face, contentDescription = "Capture", tint = Color.White)
        }
    }
}