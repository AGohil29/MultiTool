package org.arun.multitool.di

import org.arun.multitool.IosNotifier
import org.arun.multitool.PlatformNotifier
import org.koin.dsl.module

actual val platformModule = module {
    single<PlatformNotifier> { IosNotifier() }
}