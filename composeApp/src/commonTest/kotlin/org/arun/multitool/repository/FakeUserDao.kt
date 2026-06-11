package org.arun.multitool.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.arun.multitool.data.UserDao
import org.arun.multitool.data.UserEntity

/**
 * In-memory fake of [UserDao] for testing.
 * Emits updates via a [MutableStateFlow] so Turbine can observe them.
 */
class FakeUserDao : UserDao {
    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())

    override fun getAllUsers(): Flow<List<UserEntity>> = _users

    override suspend fun insertUser(users: List<UserEntity>) {
        _users.update { users }
    }
}

