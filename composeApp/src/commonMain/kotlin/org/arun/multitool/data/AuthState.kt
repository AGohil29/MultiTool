package org.arun.multitool.data

sealed class AuthState {
    data object Loading : AuthState()

    data class Authenticated(
        val userName: String,
        val token: String,
    ) : AuthState()

    data class Error(
        val message: String,
    ) : AuthState()
}