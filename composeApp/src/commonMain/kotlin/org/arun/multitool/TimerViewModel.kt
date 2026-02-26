package org.arun.multitool

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.arun.multitool.data.UserEntity
import org.arun.multitool.data.UserRepository

class TimerViewModel(
    private val userRepository: UserRepository,
    private val notifier: PlatformNotifier
) : ViewModel() {
    private val _seconds = MutableStateFlow(0)
    val seconds: StateFlow<Int> = _seconds.asStateFlow()

    private val _userInfo = MutableStateFlow("Loading...")
    val userInfo = _userInfo.asStateFlow()

    private val _userState = MutableStateFlow<NetworkResult<User>>(NetworkResult.Loading)
    val userState = _userState.asStateFlow()

    // UI observes this. It updates automatically whenever the DB changes.
    val usersList: StateFlow<List<UserEntity>> = userRepository.getAllUsers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshData()
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _seconds.value++
            }
        }
    }

    fun onButtonClicked() {
        notifier.showToast("Hello from Shared ViewModel!")
    }

    fun refreshData() {
        viewModelScope.launch {
            userRepository.refreshUsers()
        }
    }
}