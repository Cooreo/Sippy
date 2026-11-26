package com.sippy.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as com.sippy.app.SippyApp
        when (intent.action) {
            Notifier.ACTION_REMIND -> {
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        app.container.notifier.show(app.container.repository.activeFriendName())
                        app.container.scheduler.refresh(app.container.scheduler.canScheduleExact())
                    } finally {
                        pending.finish()
                    }
                }
            }
            Notifier.ACTION_LOG -> {
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        app.container.repository.logWater(250)
                        app.container.notifier.cancel()
                    } finally {
                        pending.finish()
                    }
                }
            }
            Notifier.ACTION_TEST -> {
                val pending = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val name = app.container.repository.activeFriendName()
                        app.container.notifier.show(name, test = true)
                    } finally {
                        pending.finish()
                    }
                }
            }
        }
    }
}
