package org.arun.multitool.library

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IOSPaymentGateway : PaymentGateway {
    // This holds the reference to the Swift logic
    private var callback: ((Double, (PaymentResult) -> Unit) -> Unit)? = null

    // This function is called from Swift to "plug in" the logic
    fun provideImplementation(block: (Double, (PaymentResult) -> Unit) -> Unit) {
        this.callback = block
    }

    override suspend fun processPayment(amount: Double): PaymentResult =
        suspendCancellableCoroutine { continuation ->
            callback?.invoke(amount) { result ->
                continuation.resume(result)
            } ?: continuation.resume(PaymentResult.Failure("Swift Logic missing"))
        }
}