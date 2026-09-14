package org.arun.multitool.ui.viewmodels

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.arun.multitool.hardware.LocationService

class CheckInViewModel(private val locationService: LocationService) : ScreenModel {
    // Transform the Flow into a StateFlow for the UI
    val userLocation = locationService.getCurrentLocation()
        .stateIn(screenModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun requestLocation() {
        screenModelScope.launch {
            locationService.requestPermissions()
        }
    }
}