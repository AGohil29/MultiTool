package org.arun.multitool.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.arun.multitool.data.User

class MainRepository {
    private val _userState = MutableStateFlow(User(id = 0, name = "Initial", email = "user@gmail.com"))
    val userFlow: StateFlow<User> = _userState.asStateFlow()

    suspend fun startUpdating() {
        // Simulate a network stream or sensor update
        var count = 1
        while (true) {
            delay(2000)
            _userState.value = User(id = 0, name = "User Update #$count", email = "user$count@gmail.com")
            count++
        }
    }
}