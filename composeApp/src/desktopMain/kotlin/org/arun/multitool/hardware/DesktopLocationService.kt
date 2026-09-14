package org.arun.multitool.hardware

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Stub location service for Desktop — no GPS hardware available. */
class DesktopLocationService : LocationService {
    override fun getCurrentLocation(): Flow<GpsCoordinates?> = flowOf(null)
    override suspend fun requestPermissions() { /* no-op on desktop */ }

    @Composable
    override fun ProvidePermissionHandler() { /* no-op on desktop */ }
}

