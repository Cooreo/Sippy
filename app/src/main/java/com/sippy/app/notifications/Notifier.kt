package com.sippy.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sippy.app.MainActivity
import com.sippy.app.R

class Notifier(private val context: Context) {
    companion object {
        const val CHANNEL_ID = "sippy_reminders"
        const val NOTIFICATION_ID = 1001
        const val ACTION_REMIND = "com.sippy.app.REMIND"
        const val ACTION_LOG = "com.sippy.app.LOG_250"
        const val ACTION_TEST = "com.sippy.app.TEST"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.notification_channel) }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    fun show(friendName: String, test: Boolean = false) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val actionIntent = Intent(context, ReminderReceiver::class.java).setAction(ACTION_LOG)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val action = PendingIntent.getBroadcast(context, 2, actionIntent, flags)
        val content = Intent(context, MainActivity::class.java).apply { this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val title = if (test) context.getString(R.string.notification_test, friendName)
        else context.getString(R.string.notification_title, friendName)
        val body = context.getString(R.string.notification_text, friendName)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_notification, context.getString(R.string.notification_action), action)
            .setContentIntent(
                PendingIntent.getActivity(
                    context, 3, content, flags
                )
            )
            .build()
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancel() = NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
}
