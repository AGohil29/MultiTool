package org.arun.multitool.di

import androidx.room.RoomDatabase
import org.arun.multitool.IosNotifier
import org.arun.multitool.PlatformNotifier
import org.arun.multitool.data.database.AppDatabase
import org.arun.multitool.data.database.getDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single<PlatformNotifier> { IosNotifier() }
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
    }
}