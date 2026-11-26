package com.sippy.app.updates

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.updateStore: DataStore<Preferences> by preferencesDataStore(name = "sippy_update")

data class PendingUpdate(
    val id: Long = -1L,
    val sha256: String = "",
    val version: String = "",
    val size: Long = 0L
)

/** Transient state for a download that must survive the receiver that completes it. */
class UpdateStore(context: Context) {
    private val store = context.applicationContext.updateStore

    val pending: Flow<PendingUpdate> = store.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            PendingUpdate(
                id = p[Keys.ID] ?: -1L,
                sha256 = p[Keys.SHA] ?: "",
                version = p[Keys.VERSION] ?: "",
                size = p[Keys.SIZE] ?: 0L
            )
        }

    suspend fun save(id: Long, sha256: String, version: String, size: Long) {
        store.edit {
            it[Keys.ID] = id
            it[Keys.SHA] = sha256
            it[Keys.VERSION] = version
            it[Keys.SIZE] = size
        }
    }

    suspend fun snapshot(): PendingUpdate = pending.first()

    suspend fun clear() = store.edit {
        it.remove(Keys.ID)
        it.remove(Keys.SHA)
        it.remove(Keys.VERSION)
        it.remove(Keys.SIZE)
    }

    private object Keys {
        val ID = longPreferencesKey("pending_download_id")
        val SHA = stringPreferencesKey("pending_download_sha")
        val VERSION = stringPreferencesKey("pending_download_version")
        val SIZE = longPreferencesKey("pending_download_size")
    }
}
