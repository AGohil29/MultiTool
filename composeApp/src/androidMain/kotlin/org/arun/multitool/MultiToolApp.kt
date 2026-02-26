package org.arun.multitool

import android.app.Application
import org.arun.multitool.di.initKoin
import org.koin.android.ext.koin.androidContext

class MultiToolApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@MultiToolApp)
        }
    }
}