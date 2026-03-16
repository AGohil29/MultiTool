package org.arun.multitool

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.arun.multitool.data.UserRepository

class TimerViewModel(
    private val userRepository: UserRepository,
    private val notifier: PlatformNotifier
) : ScreenModel {
    private val _seconds = MutableStateFlow(0)
    val seconds: StateFlow<Int> = _seconds.asStateFlow()

    private val _userInfo = MutableStateFlow("Loading...")
    val userInfo = _userInfo.asStateFlow()

    private val _userState = MutableStateFlow<NetworkResult<User>>(NetworkResult.Loading)
    val userState = _userState.asStateFlow()

    // UI observes this. It updates automatically whenever the DB changes.
    val usersList: StateFlow<List<User>> = userRepository.getAllUsers()
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        refreshData(false)
    }

//    private fun startTimer() {
//        viewModelScope.launch {
//            while (true) {
//                delay(1000)
//                _seconds.value++
//            }
//        }
//    }

    fun onButtonClicked() {
        notifier.showToast("Hello from Shared ViewModel!")
    }

    fun refreshData(forceRefresh: Boolean) {
        screenModelScope.launch {
            userRepository.refreshUsersIfNecessary(forceRefresh)
        }
    }
}