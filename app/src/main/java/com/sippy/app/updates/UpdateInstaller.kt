package com.sippy.app.updates

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import com.sippy.app.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.security.MessageDigest

/**
 * Sideload update installation. The user always confirms: this class never starts a download
 * or an install without an explicit action from Settings, and it verifies the SHA-256 of the
 * downloaded APK before handing it to Android's installer.
 */
object UpdateInstaller {
    const val ACTION_INSTALL_RESULT = "com.sippy.app.UPDATE_INSTALL_RESULT"
    private const val CHANNEL = "sippy_updates"
    private const val NOTIFICATION_ID = 4207
    private const val FILE_NAME = "sippy-update.apk"

    private fun store(context: Context) = UpdateStore(context)

    fun pending(context: Context): Flow<PendingUpdate> = store(context).pending

    fun canInstall(context: Context): Boolean =
        Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls()

    fun openUnknownSources(context: Context) {
        val packageUri = Uri.parse("package:${context.packageName}")
        val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            context.startActivity(
                Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    suspend fun enqueue(context: Context, info: UpdateInfo): Long {
        require(info.hasChecksum) { "This release has no verified APK checksum" }
        require(canInstall(context)) { "Install unknown apps is not allowed for Sippy" }
        val manager = downloadManager(context)

        store(context).snapshot().id.takeIf { it != -1L }?.let { manager.remove(it) }

        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("Sippy ${info.versionName}")
            .setDescription("Downloading update")
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, FILE_NAME)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val id = manager.enqueue(request)
        store(context).save(id, info.sha256.lowercase(), info.versionName, info.size)
        notify(context, "Updating Sippy", "Downloading ${info.versionName}…")
        return id
    }

    suspend fun finishDownload(context: Context, downloadId: Long) {
        val store = store(context)
        val pending = store.snapshot()
        if (pending.id != downloadId) return

        val manager = downloadManager(context)
        val query = DownloadManager.Query().setFilterById(downloadId)
        val status = manager.query(query)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            } else DownloadManager.STATUS_FAILED
        } ?: DownloadManager.STATUS_FAILED

        if (status != DownloadManager.STATUS_SUCCESSFUL) {
            store.clear()
            notify(context, "Update failed", "The APK download did not finish. Please try again.")
            return
        }

        val uri = manager.getUriForDownloadedFile(downloadId)
        if (uri == null) {
            store.clear()
            notify(context, "Update failed", "Downloaded file could not be opened.")
            return
        }

        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (pending.size > 0L) params.setSize(pending.size)
        val sessionId = installer.createSession(params)
        val session = installer.openSession(sessionId)

        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val input = context.contentResolver.openInputStream(uri)
                ?: error("Downloaded file could not be read")
            input.use { source ->
                session.openWrite(FILE_NAME, 0, pending.size.takeIf { it > 0 } ?: -1L).use { out ->
                    copyWithHash(source, out, digest, pending.size, session)
                    session.fsync(out)
                }
            }

            val actual = digestHex(digest.digest())
            if (!pending.sha256.equals(actual, ignoreCase = true)) {
                error("APK checksum did not match the signed release manifest")
            }

            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val receiver = PendingIntent.getBroadcast(
                context, 0, Intent(ACTION_INSTALL_RESULT).setPackage(context.packageName), flags
            )
            session.commit(receiver.intentSender)
            notify(context, "Update ready", "Finishing installation of ${pending.version}…")
        } catch (e: Exception) {
            runCatching { session.abandon() }
            store.clear()
            notify(context, "Update failed", e.message ?: "Installation could not be prepared.")
        }
    }

    private fun copyWithHash(
        source: InputStream,
        out: java.io.OutputStream,
        digest: MessageDigest,
        size: Long,
        session: PackageInstaller.Session
    ) {
        val buffer = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            val read = source.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
            out.write(buffer, 0, read)
            total += read
            if (size > 0L) session.setProgress((total.toFloat() / size).coerceIn(0f, 1f))
        }
    }

    private fun digestHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it.toInt() and 0xff) }

    @Suppress("DEPRECATION")
    fun installResult(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_SUCCESS -> {
                notify(context, "Sippy updated", "Restart Sippy to use the new version.")
            }
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                if (confirm != null) {
                    context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } else {
                    notify(context, "Update waiting", "Open Sippy and approve the installation.")
                }
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                notify(context, "Update failed", message ?: "Android could not install the APK.")
            }
        }
        if (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE) !=
            PackageInstaller.STATUS_PENDING_USER_ACTION
        ) {
            kotlinx.coroutines.CoroutineScope(
                kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO
            ).launch { store(context).clear() }
        }
    }

    suspend fun cancel(context: Context) {
        val store = store(context)
        val id = store.snapshot().id
        if (id != -1L) downloadManager(context).remove(id)
        store.clear()
        notify(context, "Update cancelled", "No changes were made.")
    }

    private fun downloadManager(context: Context): DownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private fun notify(context: Context, title: String, text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "Updates", NotificationManager.IMPORTANCE_LOW)
            )
        }
        manager.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        )
    }
}
