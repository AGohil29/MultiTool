package org.arun.multitool.hardware

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.darwin.NSObject

class IOSLocationService : LocationService {
    private val locationManager = CLLocationManager()

    // Define the delegate at the class level or ensure it's held strongly
    private var currentDelegate: CLLocationManagerDelegateProtocol? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun getCurrentLocation(): Flow<GpsCoordinates?> = callbackFlow {
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(
                manager: CLLocationManager,
                didUpdateLocations: List<*>,
            ) {
                val locations = didUpdateLocations as List<CLLocation>
                locations.lastOrNull()?.let {
                    val coords = it.coordinate.useContents {
                        GpsCoordinates(latitude, longitude)
                    }
                    trySend(coords)
                }
            }
        }

        currentDelegate = delegate
        locationManager.delegate = currentDelegate
        locationManager.startUpdatingLocation()

        awaitClose {
            locationManager.stopUpdatingLocation()
            locationManager.delegate = null
            currentDelegate = null
        }
    }

    override suspend fun requestPermissions() {
        val status = locationManager.authorizationStatus
        if (status == kCLAuthorizationStatusNotDetermined) {
            locationManager.requestWhenInUseAuthorization()
        }
    }

    @Composable
    override fun ProvidePermissionHandler() {

    }
}