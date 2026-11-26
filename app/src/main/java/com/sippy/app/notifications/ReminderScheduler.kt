package com.sippy.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sippy.app.data.SettingsRepository
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

class ReminderScheduler(
    private val context: Context,
    private val settings: SettingsRepository,
    private val notifier: Notifier
) {
    private val workManager = WorkManager.getInstance(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun refresh(exactAllowed: Boolean) {
        val cfg = settings.snapshot()
        cancel()
        if (!cfg.reminderEnabled) return
        val interval = max(15, cfg.reminderIntervalMin)
        if (exactAllowed && canScheduleExact()) {
            val trigger = nextTrigger(interval, cfg.wakeStartHour, cfg.wakeEndHour)
            val intent = Intent(context, ReminderReceiver::class.java).setAction(Notifier.ACTION_REMIND)
            val pi = PendingIntent.getBroadcast(
                context, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, trigger, pi)
            }
        } else {
            val delay = nextTrigger(interval, cfg.wakeStartHour, cfg.wakeEndHour) - System.currentTimeMillis()
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(
                interval.toLong(), TimeUnit.MINUTES
            )
                .setInitialDelay(maxOf(0L, delay), TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().build())
                .build()
            workManager.enqueueUniquePeriodic(WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, request)
        }
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
        val intent = Intent(context, ReminderReceiver::class.java).setAction(Notifier.ACTION_REMIND)
        PendingIntent.getBroadcast(
            context, 11, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        ).cancel()
    }

    fun canScheduleExact(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else true

    fun nextTrigger(intervalMinutes: Int, wakeStart: Int, wakeEnd: Int): Long {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, wakeStart.coerceIn(0, 23))
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        var guard = 0
        while (candidate.timeInMillis <= now.timeInMillis + 30_000L && guard++ < 500) {
            candidate.add(Calendar.MINUTE, max(15, intervalMinutes))
        }
        if (candidate.get(Calendar.HOUR_OF_DAY) >= wakeEnd.coerceIn(wakeStart + 1, 24)) {
            candidate.set(Calendar.HOUR_OF_DAY, wakeStart.coerceIn(0, 23))
            candidate.set(Calendar.MINUTE, 0)
            candidate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return candidate.timeInMillis
    }

    fun fireNow(name: String) = notifier.show(name, test = true)

    companion object { const val WORK_NAME = "sippy_reminder" }
}
