package org.arun.multitool.di

import androidx.room.RoomDatabase
import org.arun.multitool.IosNotifier
import org.arun.multitool.PlatformNotifier
import org.arun.multitool.data.database.AppDatabase
import org.arun.multitool.data.database.getDatabaseBuilder
import org.arun.multitool.hardware.IOSLocationService
import org.arun.multitool.hardware.LocationService
import org.arun.multitool.ui.components.HapticManager
import org.arun.multitool.ui.components.IOSHapticManager
import org.koin.dsl.module

actual val platformModule = module {
    single<PlatformNotifier> { IosNotifier() }
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
    }
    single<HapticManager> { IOSHapticManager() }
    single<LocationService> { IOSLocationService() }
}