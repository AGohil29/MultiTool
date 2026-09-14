package org.arun.multitool.hardware

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

data class GpsCoordinates(val latitude: Double, val longitude: Double)

interface LocationService {
    fun getCurrentLocation(): Flow<GpsCoordinates?>
    suspend fun requestPermissions()

    @Composable
    fun ProvidePermissionHandler() // This will host the launcher on Android
}