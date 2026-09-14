package org.arun.multitool.library

interface PaymentGateway {
    suspend fun processPayment(amount: Double): PaymentResult
}

sealed class PaymentResult {
    object Success : PaymentResult()
    data class Failure(val error: String) : PaymentResult()
}