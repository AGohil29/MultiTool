package org.arun.multitool.di

import org.arun.multitool.DesktopNotifier
import org.arun.multitool.PlatformNotifier
import org.koin.dsl.module

actual val platformModule = module {
    single<PlatformNotifier> { DesktopNotifier() }
}