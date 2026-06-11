package org.arun.multitool.utils

actual object GarbageCollector {
    actual fun forceCollect() {
        System.gc()
    }
}

