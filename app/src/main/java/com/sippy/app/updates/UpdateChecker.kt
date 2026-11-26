package com.sippy.app.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Metadata for one distributable APK. Published next to the APK as `update.json` so the app,
 * the website and the release itself always agree on the same numbers.
 */
data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val sha256: String,
    val size: Long,
    val notes: String,
    val hasChecksum: Boolean = sha256.matches(Regex("(?i)^[0-9a-f]{64}$"))
)

/**
 * Checks the latest GitHub release. Checking is automatic (once per day) and read-only.
 * Downloading and installing only happen after the user explicitly confirms in Settings.
 */
object UpdateChecker {
    private const val LATEST_RELEASE_API = "https://api.github.com/repos/Cooreo/Sippy/releases/latest"

    fun check(): UpdateInfo {
        val connection = URL(LATEST_RELEASE_API).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "Sippy-Update-Checker")
            val code = connection.responseCode
            if (code !in 200..299) error("Update check failed (HTTP $code)")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })

            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            var manifestUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    val url = asset.optString("browser_download_url", "").ifBlank { null }
                    if (name == "update.json" && url != null) manifestUrl = url
                    if (name.endsWith(".apk") && apkUrl == null && url != null) apkUrl = url
                }
            }

            if (manifestUrl != null) {
                val manifest = JSONObject(fetchText(manifestUrl))
                val url = manifest.optString("apkUrl", "").ifBlank { apkUrl }
                if (!url.isNullOrBlank() && safeUrl(url)) {
                    return UpdateInfo(
                        versionCode = manifest.optInt("versionCode", 0),
                        versionName = manifest.optString("versionName", "").ifBlank {
                            json.optString("tag_name", "").removePrefix("v")
                        },
                        apkUrl = url,
                        sha256 = manifest.optString("sha256", "").trim(),
                        size = manifest.optLong("size", 0L),
                        notes = manifest.optString("notes", "")
                    )
                }
            }

            // Compatibility with older releases that had no update.json asset.
            val version = json.optString("tag_name", "").removePrefix("v").trim()
            if (version.isBlank()) error("Release has no version tag")
            if (apkUrl.isNullOrBlank() || !safeUrl(apkUrl)) error("Release has no usable APK")
            UpdateInfo(
                versionCode = 0,
                versionName = version,
                apkUrl = apkUrl,
                sha256 = "",
                size = 0L,
                notes = json.optString("body", "")
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchText(url: String): String {
        require(safeUrl(url)) { "Unsafe update URL" }
        val c = URL(url).openConnection() as HttpURLConnection
        return try {
            c.requestMethod = "GET"
            c.connectTimeout = 10_000
            c.readTimeout = 10_000
            c.setRequestProperty("User-Agent", "Sippy-Update-Checker")
            if (c.responseCode !in 200..299) error("Manifest download failed (HTTP ${c.responseCode})")
            c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    private fun safeUrl(value: String): Boolean {
        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return false
        return uri.scheme == "https" && uri.host == "github.com"
    }

    fun isNewer(latest: UpdateInfo, currentCode: Int): Boolean =
        latest.versionCode > 0 && latest.versionCode > currentCode

    /** Legacy comparison kept for older callers and unit tests. */
    fun isNewer(latest: String, current: String): Boolean {
        fun parts(v: String) = v.split(".").mapNotNull { it.toIntOrNull() }
        val a = parts(latest)
        val b = parts(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val left = a.getOrElse(i) { 0 }
            val right = b.getOrElse(i) { 0 }
            if (left != right) return left > right
        }
        return false
    }

    fun open(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
