package com.sippy.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.sippy.app.data.SippyDatabase
import com.sippy.app.data.SettingsRepository
import com.sippy.app.data.WaterRepository
import com.sippy.app.domain.FakeClock
import com.sippy.app.notifications.Notifier
import com.sippy.app.notifications.ReminderScheduler
import com.sippy.app.notifications.ReminderWorker

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val settings = SettingsRepository(appContext)
    private val database = Room.databaseBuilder(appContext, SippyDatabase::class.java, "sippy.db").build()
    val dao = database.dao()
    val clock = FakeClock()
    val repository = WaterRepository(dao, settings, clock)
    val notifier = Notifier(appContext)
    val scheduler = ReminderScheduler(appContext, settings, notifier)
    val workerFactory: WorkerFactory = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker? = if (workerClassName == ReminderWorker::class.java.name) {
            ReminderWorker(appContext, workerParameters, repository, settings, notifier)
        } else null
    }
}
