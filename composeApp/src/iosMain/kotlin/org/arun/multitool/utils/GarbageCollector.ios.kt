package org.arun.multitool.utils

import kotlin.native.runtime.NativeRuntimeApi

actual object GarbageCollector {
    @OptIn(NativeRuntimeApi::class)
    actual fun forceCollect() {
        println("KMP Debug: Forcing synchronous Garbage Collection sweep...")

        // Triggers an immediate, full garbage collection pass on the Kotlin side
        kotlin.native.runtime.GC.collect()
    }
}