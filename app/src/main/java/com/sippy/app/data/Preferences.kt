package com.sippy.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.io.IOException

private val Context.settingsStore: DataStore<Preferences> by preferencesDataStore(name = "sippy_settings")

data class Settings(
    val onboardingDone: Boolean = false,
    val goalMl: Int = 2000,
    val units: String = "ml",
    val themeMode: String = "system",
    val accent: String = "aqua",
    val reminderEnabled: Boolean = false,
    val reminderIntervalMin: Int = 120,
    val wakeStartHour: Int = 7,
    val wakeEndHour: Int = 22,
    val activeFriendId: Long = -1L,
    val devUnlocked: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCelebratedDay: String = "",
    val lastProcessedDay: String = "",
    val debugOverlay: Boolean = false
)

private object Keys {
    val ONBOARDING = booleanPreferencesKey("onboarding_done")
    val GOAL = intPreferencesKey("goal_ml")
    val UNITS = stringPreferencesKey("units")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val ACCENT = stringPreferencesKey("accent")
    val REMINDER = booleanPreferencesKey("reminder_enabled")
    val INTERVAL = intPreferencesKey("reminder_interval_min")
    val WAKE_START = intPreferencesKey("wake_start_hour")
    val WAKE_END = intPreferencesKey("wake_end_hour")
    val ACTIVE_FRIEND = longPreferencesKey("active_friend_id")
    val DEV_UNLOCKED = booleanPreferencesKey("dev_unlocked")
    val STREAK = intPreferencesKey("streak_current")
    val BEST = intPreferencesKey("streak_best")
    val CELEBRATED = stringPreferencesKey("last_celebrated_day")
    val PROCESSED = stringPreferencesKey("last_processed_day")
    val DEBUG = booleanPreferencesKey("debug_overlay")
}

class SettingsRepository(private val context: Context) {
    val settings: Flow<Settings> = context.settingsStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            Settings(
                onboardingDone = p[Keys.ONBOARDING] ?: false,
                goalMl = p[Keys.GOAL] ?: 2000,
                units = p[Keys.UNITS] ?: "ml",
                themeMode = p[Keys.THEME_MODE] ?: "system",
                accent = p[Keys.ACCENT] ?: "aqua",
                reminderEnabled = p[Keys.REMINDER] ?: false,
                reminderIntervalMin = p[Keys.INTERVAL] ?: 120,
                wakeStartHour = p[Keys.WAKE_START] ?: 7,
                wakeEndHour = p[Keys.WAKE_END] ?: 22,
                activeFriendId = p[Keys.ACTIVE_FRIEND] ?: -1L,
                devUnlocked = p[Keys.DEV_UNLOCKED] ?: false,
                currentStreak = p[Keys.STREAK] ?: 0,
                bestStreak = p[Keys.BEST] ?: 0,
                lastCelebratedDay = p[Keys.CELEBRATED] ?: "",
                lastProcessedDay = p[Keys.PROCESSED] ?: "",
                debugOverlay = p[Keys.DEBUG] ?: false
            )
        }

    suspend fun snapshot(): Settings = settings.first()

    private suspend fun set(key: Preferences.Key<*>, value: Any) {
        context.settingsStore.edit { it[key as Preferences.Key<Any>] = value as Any }
    }

    suspend fun setOnboarding(done: Boolean) = set(Keys.ONBOARDING, done)
    suspend fun setGoal(ml: Int) = set(Keys.GOAL, RulesClamp.goal(ml))
    suspend fun setUnits(units: String) = set(Keys.UNITS, if (units == "oz") "oz" else "ml")
    suspend fun setThemeMode(mode: String) = set(Keys.THEME_MODE, mode)
    suspend fun setAccent(accent: String) = set(Keys.ACCENT, accent)
    suspend fun setReminder(enabled: Boolean) = set(Keys.REMINDER, enabled)
    suspend fun setInterval(minutes: Int) = set(Keys.INTERVAL, minutes.coerceIn(15, 720))
    suspend fun setWakeHours(start: Int, end: Int) {
        context.settingsStore.edit {
            it[Keys.WAKE_START] = start.coerceIn(0, 22)
            it[Keys.WAKE_END] = end.coerceIn(start + 1, 24)
        }
    }
    suspend fun setActiveFriend(id: Long) = set(Keys.ACTIVE_FRIEND, id)
    suspend fun setDevUnlocked(unlocked: Boolean) = set(Keys.DEV_UNLOCKED, unlocked)
    suspend fun setStreaks(current: Int, best: Int) = context.settingsStore.edit {
        it[Keys.STREAK] = current.coerceAtLeast(0)
        it[Keys.BEST] = best.coerceAtLeast(current)
    }
    suspend fun setCelebratedDay(day: String) = set(Keys.CELEBRATED, day)
    suspend fun setProcessedDay(day: String) = set(Keys.PROCESSED, day)
    suspend fun setDebugOverlay(enabled: Boolean) = set(Keys.DEBUG, enabled)

    suspend fun clearAll() = context.settingsStore.edit { it.clear() }
}

private object RulesClamp {
    fun goal(ml: Int) = ml.coerceIn(250, 10000)
}
