package org.arun.multitool.di

import androidx.room.RoomDatabase
import org.arun.multitool.AndroidNotifier
import org.arun.multitool.PlatformNotifier
import org.arun.multitool.data.database.AppDatabase
import org.arun.multitool.data.database.getAndroidDatabaseBuilder
import org.arun.multitool.ui.components.AndroidHapticManager
import org.arun.multitool.ui.components.HapticManager
import org.koin.dsl.module

actual val platformModule = module {
    // Provide the Android implementation of the interface
    single<PlatformNotifier> { AndroidNotifier(get()) }
    single<RoomDatabase.Builder<AppDatabase >> { getAndroidDatabaseBuilder(get()) }
    single<HapticManager> { AndroidHapticManager(get()) }
}