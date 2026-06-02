package org.arun.multitool.di

import androidx.room.RoomDatabase
import org.arun.multitool.DesktopNotifier
import org.arun.multitool.PlatformNotifier
import org.arun.multitool.data.database.AppDatabase
import org.arun.multitool.data.database.getDatabaseBuilder
import org.arun.multitool.hardware.DesktopLocationService
import org.arun.multitool.hardware.LocationService
import org.arun.multitool.ui.components.DesktopHapticManager
import org.arun.multitool.ui.components.HapticManager
import org.arun.multitool.utils.DesktopPermissionManager
import org.arun.multitool.utils.PermissionManager
import org.koin.dsl.module

actual val platformModule = module {
    single<PlatformNotifier> { DesktopNotifier() }
    single<RoomDatabase.Builder<AppDatabase>> { getDatabaseBuilder() }
    single<HapticManager> { DesktopHapticManager() }
    single<LocationService> { DesktopLocationService() }
    single<PermissionManager> { DesktopPermissionManager() }
}