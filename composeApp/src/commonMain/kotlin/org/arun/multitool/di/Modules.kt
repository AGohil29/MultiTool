package org.arun.multitool.di

import org.arun.multitool.TimerViewModel
import org.arun.multitool.data.UserRepository
import org.arun.multitool.data.database.AppDatabase
import org.arun.multitool.data.database.getRoomDatabase
import org.arun.multitool.httpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
    // Single instance of HttpClient
    single { httpClient }
    single { getRoomDatabase(get()) }
    single { get<AppDatabase>().userDao() }

    // Repository: Create a new instance when needed, but inject the client
    factoryOf(::UserRepository)

    // ViewModel: Koin 4.0 handles CMP ViewModels natively
    viewModelOf(::TimerViewModel)
}

expect val platformModule: Module