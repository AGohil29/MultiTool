package org.arun.multitool.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class AndroidLocationService(private val context: Context) : LocationService {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private var permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>? =
        null

    // A simple way to force a flow restart if needed
    private val permissionGrantedTrigger = MutableSharedFlow<Unit>(replay = 1)

    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(): Flow<GpsCoordinates?> = callbackFlow {
        // Function to start updates
        val startUpdates = {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()
            val callback = object : LocationCallback() {
                override fun onLocationResult(res: LocationResult) {
                    res.lastLocation?.let { trySend(GpsCoordinates(it.latitude, it.longitude)) }
                }
            }
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            callback
        }

        var currentCallback: LocationCallback? = null

        // Check permissions immediately
        if (checkPermissions()) {
            currentCallback = startUpdates()
        }

        // Also listen for the trigger in case permissions were just granted
        val job = CoroutineScope(Dispatchers.Main).launch {
            permissionGrantedTrigger.collect {
                if (currentCallback == null) currentCallback = startUpdates()
            }
        }

        awaitClose {
            currentCallback?.let { client.removeLocationUpdates(it) }
            job.cancel()
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestPermissions() {
        permissionLauncher?.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @Composable
    override fun ProvidePermissionHandler() {
        permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.values.any { it }) {
                permissionGrantedTrigger.tryEmit(Unit)
            }
        }
    }
}