package org.arun.multitool.di

import com.russhwolf.settings.Settings
import org.arun.multitool.ui.viewmodels.TimerViewModel
import org.arun.multitool.data.UserDao
import org.arun.multitool.repository.UserRepository
import org.arun.multitool.data.database.AppDatabase
import org.arun.multitool.data.database.getRoomDatabase
import org.arun.multitool.data.httpClient
import org.arun.multitool.ui.transition.TransitionHandler
import org.koin.core.module.Module
import org.koin.dsl.module

val sharedModule = module {
    // Single instance of HttpClient
    single { httpClient }
    single<AppDatabase> { getRoomDatabase(get()) }
    single<UserDao> { get<AppDatabase>().userDao() }

    single { Settings() }   // Simple no-arg factory for Android/iOS

    single { TransitionHandler() }

    // Repository: Create a new instance when needed, but inject the client
    factory { UserRepository(get(), get(), get()) }

    // ViewModel: Koin 4.0 handles CMP ViewModels natively
//    viewModelOf(::TimerViewModel)
    factory { TimerViewModel(get(), get()) }
}

expect val platformModule: Module