package com.psm2.inva

import android.app.Application
import com.psm2.inva.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class InvaTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@InvaTrackApp)
            modules(appModule)
        }
    }
}
