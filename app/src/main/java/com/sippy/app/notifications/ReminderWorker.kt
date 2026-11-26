package com.sippy.app.notifications

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.sippy.app.data.SettingsRepository
import com.sippy.app.data.WaterRepository
import com.sippy.app.domain.DayKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: WaterRepository,
    private val settings: SettingsRepository,
    private val notifier: Notifier
) : ListenableWorker(context, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val cfg = settings.snapshot()
            if (!cfg.reminderEnabled) return@withContext Result.success()
            val now = DayKeys.nowInZone(System.currentTimeMillis(), java.util.TimeZone.getDefault())
            val hour = now.hour
            if (hour < cfg.wakeStartHour || hour >= cfg.wakeEndHour) return@withContext Result.success()
            notifier.show(repository.activeFriendName())
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
