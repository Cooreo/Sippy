package com.sippy.app

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.sippy.app.di.AppContainer

class SippyApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        WorkManager.initialize(
            this,
            Configuration.Builder().setWorkerFactory(container.workerFactory).build()
        )
    }
}
