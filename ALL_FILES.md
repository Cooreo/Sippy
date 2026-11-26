# Sippy — complete source listing

Every text file in this repository, in full, with its path as a heading. Binary files (Gradle wrapper JAR and PNG icons) are stored in the repository but cannot be printed as text.

---

## `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Sippy"
include(":app")
```

---

## `build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
```

---

## `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

---

## `gradle/libs.versions.toml`

```toml
[versions]
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.25"
room = "2.6.1"
lifecycle = "2.8.7"
navigation = "2.8.4"
compose = "1.7.5"
material3 = "1.3.1"
work = "2.10.0"
datastore = "1.1.1"
coroutines = "1.9.0"
junit = "4.13.2"
material = "1.12.0"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.13.1" }
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { module = "androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version = "1.9.3" }
androidx-compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview", version.ref = "compose" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3", version.ref = "material3" }
androidx-compose-material-icons-extended = { module = "androidx.compose.material:material-icons-extended", version = "1.7.5" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
androidx-room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
androidx-room-ktx = { module = "androidx.room:room-ktx", version.ref = "room" }
androidx-room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
androidx-work-runtime-ktx = { module = "androidx.work:work-runtime-ktx", version.ref = "work" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
junit = { module = "junit:junit", version.ref = "junit" }
google-material = { module = "com.google.android.material:material", version.ref = "material" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

---

## `app/build.gradle.kts`

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) keystorePropertiesFile.inputStream().use { load(it) }
}

android {
    namespace = "com.sippy.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sippy.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        debug { isMinifyEnabled = false }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropertiesFile.exists()) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.google.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
}
```

---

## `app/proguard-rules.pro`

```
# Room
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**
# Kotlin / coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
# Compose
-keep class android.support.compose.ui.tooling.preview.** { *; }
-dontwarn androidx.compose.**
# Hilt rules (kept for future migration to Hilt)
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**
```

---

## `.gitignore`

```
*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
*.apk
*.aab
/keystore.properties
/app/*.jks
/app/*.keystore
/app/build/
```

---

## `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

---

## `app/src/main/java/com/sippy/app/MainActivity.kt`

```kotlin
package com.sippy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sippy.app.di.AppContainer
import com.sippy.app.ui.AppViewModel
import com.sippy.app.ui.SippyNavigation

class MainActivity : ComponentActivity() {
    private val container: AppContainer by lazy { (application as SippyApp).container }
    private val viewModel: AppViewModel by viewModels { SippyVmFactory(container) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SippyNavigation(viewModel) }
    }
}

class SippyVmFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppViewModel(
            repository = container.repository,
            settings = container.settings,
            clock = container.clock,
            scheduler = container.scheduler,
            notifier = container.notifier
        ) as T
    }
}
```

---

## `app/src/main/java/com/sippy/app/SippyApp.kt`

```kotlin
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
```

---

## `app/src/main/java/com/sippy/app/ai/SmartCoach.kt`

```kotlin
package com.sippy.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class CoachContext(
    val friendName: String,
    val totalMl: Int,
    val goalMl: Int,
    val streak: Int,
    val mood: String,
    val average7DaysMl: Int
)

data class CoachState(
    val busy: Boolean = false,
    val answer: String? = null,
    val error: String? = null
)

/** Local-first coaching: deterministic insights need no network. AI answers are opt-in. */
object SmartCoach {
    private const val ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"
    private const val MODEL = "openai/gpt-4o-mini"

    fun localInsights(c: CoachContext): List<String> {
        val remaining = (c.goalMl - c.totalMl).coerceAtLeast(0)
        val result = mutableListOf<String>()
        when {
            remaining == 0 -> result.add("${c.friendName} is dancing — target reached. Protect the streak with a small evening sip.")
            c.totalMl == 0 -> result.add("No water yet today. A 250 ml sip is the easiest way to wake ${c.friendName} up.")
            else -> result.add("$remaining ml to go. Two 250 ml glasses would almost finish the day.")
        }
        if (c.average7DaysMl > 0) {
            val delta = c.totalMl - c.average7DaysMl
            result.add(if (delta >= 0) "You are $delta ml ahead of your 7-day average at this point." else
                "You are ${-delta} ml behind your 7-day average — same day, last week.")
        }
        if (c.streak > 0) result.add("Streak: $c.streak day(s). Consistency matters more than a huge single day.")
        result.add("Current mood: ${c.mood}. ${c.friendName} reacts to every entry you log.")
        return result
    }

    suspend fun ask(apiKey: String, question: String, context: CoachContext): String =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) error("Add your OpenRouter API key in Settings first")
            val system = """
                You are the hydration coach inside Sippy, a friendly water-tracking app.
                Be brief, warm and practical. Never diagnose medical conditions.
                Local app state: friend=${context.friendName}, today=${context.totalMl}ml of ${context.goalMl}ml,
                streak=${context.streak} days, mood=${context.mood}, 7-day average=${context.average7DaysMl}ml.
            """.trimIndent()
            val payload = JSONObject()
                .put("model", MODEL)
                .put("max_tokens", 180)
                .put("messages", JSONArray()
                    .put(JSONObject().put("role", "system").put("content", system))
                    .put(JSONObject().put("role", "user").put("content", question)))

            val connection = URL(ENDPOINT).openConnection() as HttpsURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.doOutput = true
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("HTTP-Referer", "https://cooreo.github.io/Sippy/")
                connection.setRequestProperty("X-Title", "Sippy")
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                if (code !in 200..299) error("OpenRouter HTTP $code: ${response.take(160)}")
                val choices = JSONObject(response).getJSONArray("choices")
                if (choices.length() == 0) error("OpenRouter returned no choices")
                choices.getJSONObject(0).getJSONObject("message").optString("content").ifBlank {
                    error("OpenRouter returned an empty reply")
                }
            } finally {
                connection.disconnect()
            }
        }
}
```

---

## `app/src/main/java/com/sippy/app/data/Entities.kt`

```kotlin
package com.sippy.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val species: String,
    val growthPoints: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)

@Entity(tableName = "drinks")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dayKey: String,
    val amountMl: Int,
    val timestamp: Long,
    val friendId: Long
)

@Entity(tableName = "day_stats")
data class DayStatsEntity(
    @PrimaryKey val dayKey: String,
    val totalMl: Int,
    val targetHit: Boolean
)

@Dao
interface SippyDao {
    @Query("SELECT * FROM friends ORDER BY sortOrder ASC, id ASC")
    fun observeFriends(): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE id = :id")
    suspend fun friend(id: Long): FriendEntity?

    @Query("SELECT COUNT(*) FROM friends")
    suspend fun friendCount(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM friends")
    suspend fun nextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Query("DELETE FROM friends WHERE id = :id")
    suspend fun deleteFriend(id: Long)

    @Query("SELECT * FROM drinks WHERE dayKey = :day ORDER BY timestamp DESC, id DESC")
    fun drinksForDay(day: String): Flow<List<DrinkEntity>>

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drinks WHERE dayKey = :day")
    fun totalForDay(day: String): Flow<Int>

    @Query("SELECT * FROM drinks WHERE dayKey = :day ORDER BY timestamp DESC, id DESC LIMIT 1")
    suspend fun lastDrink(day: String): DrinkEntity?

    @Insert
    suspend fun insertDrink(drink: DrinkEntity): Long

    @Query("DELETE FROM drinks WHERE id = :id")
    suspend fun deleteDrink(id: Long)

    @Query("SELECT * FROM day_stats WHERE dayKey = :day")
    suspend fun dayStats(day: String): DayStatsEntity?

    @Query("SELECT * FROM day_stats ORDER BY dayKey DESC LIMIT :limit")
    fun recentStats(limit: Int): Flow<List<DayStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDayStats(stats: DayStatsEntity)

    @Query("SELECT COALESCE(SUM(amountMl), 0) FROM drinks WHERE dayKey = :day")
    suspend fun sumForDay(day: String): Int

    @Query("DELETE FROM day_stats")
    suspend fun clearStats()

    @Query("DELETE FROM drinks")
    suspend fun clearDrinks()

    @Query("DELETE FROM friends")
    suspend fun clearFriends()
}

/** Version 1 is the stable baseline; exportSchema keeps migrations explicit in future releases. */
@Database(
    entities = [FriendEntity::class, DrinkEntity::class, DayStatsEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SippyDatabase : RoomDatabase() {
    abstract fun dao(): SippyDao
}
```

---

## `app/src/main/java/com/sippy/app/data/Preferences.kt`

```kotlin
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
    val debugOverlay: Boolean = false,
    val lastUpdateCheckDay: String = "",
    val openRouterKey: String = "",
    val reduceMotion: Boolean = false
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
    val UPDATE_CHECK = stringPreferencesKey("last_update_check_day")
    val OPENROUTER_KEY = stringPreferencesKey("openrouter_api_key")
    val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
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
                debugOverlay = p[Keys.DEBUG] ?: false,
                lastUpdateCheckDay = p[Keys.UPDATE_CHECK] ?: "",
                openRouterKey = p[Keys.OPENROUTER_KEY] ?: "",
                reduceMotion = p[Keys.REDUCE_MOTION] ?: false
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
    suspend fun setUpdateCheckDay(day: String) = set(Keys.UPDATE_CHECK, day)
    suspend fun setOpenRouterKey(key: String) = set(Keys.OPENROUTER_KEY, key.trim())
    suspend fun setReduceMotion(enabled: Boolean) = set(Keys.REDUCE_MOTION, enabled)

    suspend fun clearAll() = context.settingsStore.edit { it.clear() }
}

private object RulesClamp {
    fun goal(ml: Int) = ml.coerceIn(250, 10000)
}
```

---

## `app/src/main/java/com/sippy/app/data/Repository.kt`

```kotlin
package com.sippy.app.data

import com.sippy.app.domain.AppClock
import com.sippy.app.domain.DayKeys
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.Mood
import com.sippy.app.domain.Rules
import com.sippy.app.domain.Species
import com.sippy.app.domain.StreakLogic
import com.sippy.app.domain.stageFor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

sealed interface AppEvent {
    data class Celebrate(val friendName: String) : AppEvent
    data class LevelUp(val friendName: String, val stage: GrowthStage) : AppEvent
    data class Streak(val days: Int) : AppEvent
    data class Message(val text: String) : AppEvent
}

class WaterRepository(
    private val dao: SippyDao,
    private val settings: SettingsRepository,
    private val clock: AppClock
) {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    val friends = dao.observeFriends()
    fun drinks(day: String) = dao.drinksForDay(day)
    fun total(day: String) = dao.totalForDay(day)
    fun history(limit: Int) = dao.recentStats(limit)

    suspend fun onboard(name: String, species: Species, goalMl: Int) {
        val id = dao.insertFriend(
            FriendEntity(
                name = name.trim().ifBlank { "Sippy" },
                species = species.name,
                createdAt = clock.now(),
                lastActiveAt = clock.now(),
                sortOrder = dao.nextSortOrder()
            )
        )
        settings.setGoal(Rules.clampGoal(goalMl))
        settings.setActiveFriend(id)
        settings.setOnboarding(true)
        settings.setProcessedDay(today())
    }

    suspend fun addFriend(name: String, species: Species): String? {
        if (!Rules.canAddFriend(dao.friendCount())) return "You can have up to ${Rules.MAX_FRIENDS} friends."
        dao.insertFriend(
            FriendEntity(
                name = name.trim().ifBlank { "Friend" },
                species = species.name,
                createdAt = clock.now(),
                lastActiveAt = clock.now(),
                sortOrder = dao.nextSortOrder()
            )
        )
        return null
    }

    suspend fun renameFriend(id: Long, name: String) {
        val friend = dao.friend(id) ?: return
        dao.updateFriend(friend.copy(name = name.trim().ifBlank { friend.name }))
    }

    suspend fun deleteFriend(id: Long): String? {
        if (!Rules.canDeleteFriend(dao.friendCount())) return "Keep at least one friend."
        dao.deleteFriend(id)
        val current = settings.snapshot()
        if (current.activeFriendId == id) {
            settings.setActiveFriend(allFriends().firstOrNull()?.id ?: -1L)
        }
        return null
    }

    suspend fun setActiveFriend(id: Long) {
        val friend = dao.friend(id) ?: return
        dao.updateFriend(friend.copy(lastActiveAt = clock.now()))
        settings.setActiveFriend(id)
    }

    suspend fun logWater(amountMl: Int): LogOutcome {
        val ml = amountMl.coerceAtLeast(1)
        val cfg = settings.snapshot()
        val day = today()
        val active = dao.friend(cfg.activeFriendId) ?: allFriends().firstOrNull()
        val friendId = active?.id ?: -1L
        val before = dao.dayStats(day)?.totalMl ?: dao.sumForDay(day)
        val hitBefore = before >= cfg.goalMl
        val after = before + ml
        val hitAfter = after >= cfg.goalMl

        dao.insertDrink(DrinkEntity(dayKey = day, amountMl = ml, timestamp = clock.now(), friendId = friendId))
        dao.upsertDayStats(DayStatsEntity(day, after, hitAfter))

        var levelUp: GrowthStage? = null
        if (active != null) {
            val stageBefore = stageFor(active.growthPoints)
            val gained = Rules.pointsFor(ml, after, cfg.goalMl, hitBefore)
            val updated = active.copy(growthPoints = active.growthPoints + gained)
            dao.updateFriend(updated)
            val stageAfter = stageFor(updated.growthPoints)
            if (stageAfter != stageBefore) {
                levelUp = stageAfter
                _events.tryEmit(AppEvent.LevelUp(updated.name, stageAfter))
            }
        }

        var streak = cfg.currentStreak
        if (hitAfter && !hitBefore) {
            val yesterdayHit = dao.dayStats(DayKeys.shift(day, -1))?.targetHit == true
            val result = StreakLogic.afterDay(true, yesterdayHit, cfg.currentStreak, cfg.bestStreak)
            streak = result.first
            settings.setStreaks(result.first, result.second)
            _events.tryEmit(AppEvent.Streak(result.first))
        }
        if (hitAfter && cfg.lastCelebratedDay != day) {
            settings.setCelebratedDay(day)
            _events.tryEmit(AppEvent.Celebrate(active?.name ?: "Your friend"))
        }
        _events.tryEmit(AppEvent.Message("Logged $ml ml"))
        return LogOutcome(celebrate = hitAfter && !hitBefore, levelUp = levelUp, streak = streak)
    }

    data class LogOutcome(val celebrate: Boolean, val levelUp: GrowthStage?, val streak: Int)

    suspend fun undoLast(): Boolean {
        val day = today()
        val last = dao.lastDrink(day) ?: return false
        dao.deleteDrink(last.id)
        recomputeDay(day)
        _events.tryEmit(AppEvent.Message("Last drink removed"))
        return true
    }

    suspend fun deleteDrink(id: Long) {
        val drink = drinksOnce(today()).firstOrNull { it.id == id } ?: return
        dao.deleteDrink(id)
        recomputeDay(drink.dayKey)
        _events.tryEmit(AppEvent.Message("Entry removed"))
    }

    suspend fun ensureDayRollover() {
        val day = today()
        val cfg = settings.snapshot()
        if (cfg.lastProcessedDay == day) return
        val yesterday = DayKeys.shift(day, -1)
        val hitYesterday = dao.dayStats(yesterday)?.targetHit == true
        val result = StreakLogic.onRollover(hitYesterday, cfg.currentStreak, cfg.bestStreak)
        settings.setStreaks(result.first, result.second)
        settings.setProcessedDay(day)
        recomputeDay(day)
        _events.tryEmit(AppEvent.Message("New day: ${day}"))
    }

    suspend fun replayCelebration() {
        val cfg = settings.snapshot()
        _events.tryEmit(AppEvent.Celebrate(activeFriendName(cfg.activeFriendId)))
    }

    /** Dev-only, explicit writes below. */
    suspend fun devSetGrowth(friendId: Long, points: Int, stage: GrowthStage? = null): GrowthStage? {
        val friend = dao.friend(friendId) ?: return null
        val newPoints = stage?.minPoints ?: points.coerceAtLeast(0)
        val before = stageFor(friend.growthPoints)
        dao.updateFriend(friend.copy(growthPoints = newPoints))
        val after = stageFor(newPoints)
        if (after != before) _events.tryEmit(AppEvent.LevelUp(friend.name, after))
        return after
    }

    suspend fun devSetStreaks(current: Int, best: Int) = settings.setStreaks(current, best)

    suspend fun devAddOneOfEachSpecies(): String {
        var added = 0
        for (species in Species.entries) {
            if (dao.friendCount() >= Rules.MAX_FRIENDS) break
            if (allFriends().any { it.species == species.name }) continue
            addFriend("Test ${species.label}", species)
            added++
        }
        return "Added $added test friend(s)"
    }

    suspend fun devRemoveTestFriends(): String {
        val tests = allFriends().filter { it.name.startsWith("Test ") }
        for (friend in tests) {
            if (dao.friendCount() <= 1) break
            dao.deleteFriend(friend.id)
        }
        return "Removed ${tests.size} test friend(s)"
    }

    suspend fun devSeedHistory(days: Int): String {
        val cfg = settings.snapshot()
        val today = today()
        var written = 0
        for (back in days - 1 downTo 1) {
            val day = DayKeys.shift(today, -back.toLong())
            if (dao.dayStats(day) != null) continue
            val total = Random.nextInt(700, 3200)
            val friendId = cfg.activeFriendId
            val base = DayKeys.millisAtStartOfDay(day, clock.zone())
            var remaining = total
            var part = 0
            while (remaining > 0) {
                val amount = minOf(remaining, listOf(150, 250, 500).random())
                dao.insertDrink(
                    DrinkEntity(
                        dayKey = day,
                        amountMl = amount,
                        timestamp = base + (9 + part) * 3_600_000L,
                        friendId = friendId
                    )
                )
                remaining -= amount
                part++
            }
            dao.upsertDayStats(DayStatsEntity(day, total, total >= cfg.goalMl))
            written++
        }
        return "Seeded $written day(s)"
    }

    suspend fun devClearAll() {
        dao.clearDrinks()
        dao.clearStats()
        dao.clearFriends()
        settings.clearAll()
        settings.setProcessedDay(today())
        _events.tryEmit(AppEvent.Message("All data cleared"))
    }

    suspend fun activeFriendName(): String = activeFriendName(settings.snapshot().activeFriendId)

    suspend fun activeFriendName(id: Long): String =
        dao.friend(id)?.name ?: allFriends().firstOrNull()?.name ?: "Your friend"

    private suspend fun recomputeDay(day: String) {
        val total = dao.sumForDay(day)
        val cfg = settings.snapshot()
        dao.upsertDayStats(DayStatsEntity(day, total, total >= cfg.goalMl))
    }

    private fun today() = DayKeys.key(clock.now(), clock.zone())

    private suspend fun allFriends(): List<FriendEntity> = dao.observeFriends().first()

    private suspend fun drinksOnce(day: String): List<DrinkEntity> = dao.drinksForDay(day).first()
}
```

---

## `app/src/main/java/com/sippy/app/di/AppContainer.kt`

```kotlin
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
```

---

## `app/src/main/java/com/sippy/app/domain/Logic.kt`

```kotlin
package com.sippy.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone
import kotlin.math.max
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.delay

enum class Species(val label: String) {
    DROPLET("Droplet"), SPROUT("Sprout"), KITTY("Kitty"), BLOBBY("Blobby")
}

enum class Mood(val label: String) {
    SLEEPY("Sleepy"), THIRSTY("Thirsty"), CONTENT("Content"), HAPPY("Happy"), DANCING("Dancing")
}

enum class GrowthStage(val label: String, val minPoints: Int) {
    BABY("Baby", 0), YOUNG("Young", 250), GROWN("Grown", 1000), ELDER("Elder", 3000)
}

fun stageFor(points: Int): GrowthStage = GrowthStage.entries
    .sortedByDescending { it.minPoints }
    .first { points >= it.minPoints }

object Rules {
    const val MAX_FRIENDS = 8
    const val TARGET_BONUS_POINTS = 250
    const val MIN_GOAL_ML = 250
    const val MAX_GOAL_ML = 10000

    fun canAddFriend(currentCount: Int) = currentCount < MAX_FRIENDS
    fun canDeleteFriend(currentCount: Int) = currentCount > 1

    fun clampGoal(goal: Int) = goal.coerceIn(MIN_GOAL_ML, MAX_GOAL_ML)

    /** Active friend earns one point per ml plus a once-per-day target bonus. */
    fun pointsFor(ml: Int, totalAfter: Int, goal: Int, targetAlreadyHit: Boolean): Int =
        maxOf(0, ml) + if (!targetAlreadyHit && totalAfter >= goal) TARGET_BONUS_POINTS else 0
}

object MoodCalculator {
    /**
     * Pace assumes a waking window; before it begins there is no expected progress.
     * Sleepy wins only before the user has had time to wake up and nothing is logged.
     */
    fun mood(
        totalMl: Int, goalMl: Int, nowMillis: Long, zone: TimeZone,
        wakeStartHour: Int = 7, wakeEndHour: Int = 22
    ): Mood {
        if (goalMl <= 0) return Mood.CONTENT
        val hour = hourOfDay(nowMillis, zone)
        if (totalMl <= 0 && hour < wakeStartHour + 1) return Mood.SLEEPY
        if (totalMl >= goalMl) return Mood.DANCING
        val diff = progress(totalMl, goalMl) - pace(hour, wakeStartHour, wakeEndHour)
        return when {
            diff < -0.15f -> Mood.THIRSTY
            diff > 0.15f -> Mood.HAPPY
            else -> Mood.CONTENT
        }
    }

    fun progress(totalMl: Int, goalMl: Int): Float =
        if (goalMl <= 0) 0f else (totalMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1.5f)

    fun pace(nowMillis: Long, zone: TimeZone, wakeStartHour: Int = 7, wakeEndHour: Int = 22): Float =
        pace(hourOfDay(nowMillis, zone), wakeStartHour, wakeEndHour)

    private fun pace(hour: Float, wakeStartHour: Int, wakeEndHour: Int): Float {
        val start = wakeStartHour.toFloat().coerceIn(0f, 23f)
        val end = wakeEndHour.toFloat().coerceIn((start + 1).coerceAtMost(23f), 24f)
        return ((hour - start) / (end - start)).coerceIn(0f, 1f)
    }

    private fun hourOfDay(nowMillis: Long, zone: TimeZone): Float {
        val t = Instant.ofEpochMilli(nowMillis).atZone(zone.toZoneId()).toLocalDateTime()
        return t.hour + t.minute / 60f
    }
}

object StreakLogic {
    /** A hit extends the streak only when yesterday was also a hit; otherwise it restarts at 1. */
    fun afterDay(hitToday: Boolean, hitYesterday: Boolean, current: Int, best: Int): Pair<Int, Int> {
        if (!hitToday) return 0 to best
        val next = if (hitYesterday) max(1, current) + 1 else 1
        return next to max(best, next)
    }

    /** Called on a new day: a missed yesterday breaks the chain, a hit keeps it pending. */
    fun onRollover(hitYesterday: Boolean, current: Int, best: Int): Pair<Int, Int> =
        if (!hitYesterday) 0 to best else current to best
}

object DayKeys {
    fun key(nowMillis: Long, zone: TimeZone): String =
        Instant.ofEpochMilli(nowMillis).atZone(zone.toZoneId()).toLocalDate().toString()

    fun shift(key: String, days: Long): String = LocalDate.parse(key).plusDays(days).toString()

    fun millisAtStartOfDay(key: String, zone: TimeZone): Long =
        LocalDate.parse(key).atStartOfDay(zone.toZoneId()).toInstant().toEpochMilli()

    fun nowInZone(nowMillis: Long, zone: TimeZone) =
        Instant.ofEpochMilli(nowMillis).atZone(zone.toZoneId()).toLocalDateTime()
}

/** Abstraction that lets Dev Options fake the clock without touching real data. */
interface AppClock {
    fun now(): Long
    fun zone(): TimeZone
    val tick: Flow<Long>
}

class RealClock : AppClock {
    override fun now() = System.currentTimeMillis()
    override fun zone() = TimeZone.getDefault()
    override val tick: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000L)
        }
    }
}

class FakeClock(private val delegate: AppClock = RealClock()) : AppClock {
    private val offset = java.util.concurrent.atomic.AtomicLong(0L)
    private val enabled = java.util.concurrent.atomic.AtomicBoolean(false)
    private val _tick = kotlinx.coroutines.flow.MutableStateFlow(delegate.now())
    override val tick: Flow<Long> = merge(delegate.tick, _tick)
    override fun now(): Long = delegate.now() + if (enabled.get()) offset.get() else 0L
    override fun zone(): TimeZone = delegate.zone()
    fun setEnabled(value: Boolean) { enabled.set(value); _tick.value = now() }
    fun isEnabled(): Boolean = enabled.get()
    fun setOffsetHours(hours: Int) {
        offset.set(hours * 3_600_000L)
        _tick.value = now()
    }
    fun offsetHours(): Int = (offset.get() / 3_600_000L).toInt()
}

/** Formatting helpers shared by screens; strings are templates resolved in the UI. */
object AmountFormat {
    fun display(ml: Int, units: String): String = when (units) {
        "oz" -> {
            val oz = ml / 29.5735f
            if (oz >= 10f) "%.0f fl oz".format(oz) else "%.1f fl oz".format(oz)
        }
        else -> "$ml ml"
    }

    fun mlFromDisplayOz(oz: Double): Int = (oz * 29.5735).toInt()
}
```

---

## `app/src/main/java/com/sippy/app/notifications/BootReceiver.kt`

```kotlin
package com.sippy.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as com.sippy.app.SippyApp
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.container.repository.ensureDayRollover()
                app.container.scheduler.refresh(app.container.scheduler.canScheduleExact())
            } finally {
                pending.finish()
            }
        }
    }
}
```

---

## `app/src/main/java/com/sippy/app/notifications/Notifier.kt`

```kotlin
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
```

---

## `app/src/main/java/com/sippy/app/notifications/ReminderReceiver.kt`

```kotlin
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
```

---

## `app/src/main/java/com/sippy/app/notifications/ReminderScheduler.kt`

```kotlin
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
    private val workManager by lazy { WorkManager.getInstance(context) }
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
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, request)
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
```

---

## `app/src/main/java/com/sippy/app/notifications/ReminderWorker.kt`

```kotlin
package com.sippy.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
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
) : CoroutineWorker(context, params) {
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
```

---

## `app/src/main/java/com/sippy/app/ui/AppViewModel.kt`

```kotlin
package com.sippy.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sippy.app.ai.CoachContext
import com.sippy.app.ai.CoachState
import com.sippy.app.ai.SmartCoach
import com.sippy.app.data.AppEvent
import com.sippy.app.data.DayStatsEntity
import com.sippy.app.data.DrinkEntity
import com.sippy.app.data.FriendEntity
import com.sippy.app.data.Settings
import com.sippy.app.data.SettingsRepository
import com.sippy.app.data.WaterRepository
import com.sippy.app.domain.DayKeys
import com.sippy.app.domain.FakeClock
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.Mood
import com.sippy.app.domain.MoodCalculator
import com.sippy.app.domain.Species
import com.sippy.app.notifications.Notifier
import com.sippy.app.notifications.ReminderScheduler
import com.sippy.app.updates.UpdateChecker
import com.sippy.app.updates.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DevState(
    val simulate: Boolean = false,
    val simulatePercent: Float = 1f,
    val forcedMood: Mood? = null,
    val previewSpecies: Species? = null,
    val forcedAnimation: AnimationKind? = null,
    val loopAnimation: Boolean = false,
    val animationSerial: Long = 0L,
    val fakeTime: Boolean = false,
    val fakeHours: Int = 0
)

data class UpdateState(
    val busy: Boolean = false,
    val info: UpdateInfo? = null,
    val available: Boolean = false,
    val message: String? = null
)

data class Feedback(
    val serial: Long = 0L,
    val celebrateName: String? = null,
    val levelUp: GrowthStage? = null,
    val levelUpName: String = "",
    val streak: Int? = null,
    val toast: String? = null,
    val lastEvent: String = "Ready"
)

data class UiState(
    val settings: Settings? = null,
    val friends: List<FriendEntity> = emptyList(),
    val activeFriend: FriendEntity? = null,
    val day: String = "",
    val totalMl: Int = 0,
    val effectiveTotalMl: Int = 0,
    val drinks: List<DrinkEntity> = emptyList(),
    val history: List<DayStatsEntity> = emptyList(),
    val now: Long = System.currentTimeMillis(),
    val progress: Float = 0f,
    val pace: Float = 0f,
    val mood: Mood = Mood.CONTENT,
    val dev: DevState = DevState(),
    val feedback: Feedback = Feedback(),
    val update: UpdateState = UpdateState(),
    val coach: CoachState = CoachState(),
    val apiKey: String = ""
)

sealed interface UiAction {
    data class Onboard(val name: String, val species: Species, val goal: Int) : UiAction
    data class Log(val ml: Int) : UiAction
    data object Undo : UiAction
    data object ReplayCelebration : UiAction
    data class DeleteDrink(val id: Long) : UiAction
    data class SelectFriend(val id: Long) : UiAction
    data class AddFriend(val name: String, val species: Species) : UiAction
    data class RenameFriend(val id: Long, val name: String) : UiAction
    data class DeleteFriend(val id: Long) : UiAction
    data class Goal(val ml: Int) : UiAction
    data class Units(val units: String) : UiAction
    data class Theme(val mode: String) : UiAction
    data class Accent(val accent: String) : UiAction
    data class Reminder(val enabled: Boolean) : UiAction
    data class Interval(val minutes: Int) : UiAction
    data class WakeHours(val start: Int, val end: Int) : UiAction
    data class DebugOverlay(val enabled: Boolean) : UiAction
    data object UnlockDev : UiAction
    data object LockDev : UiAction
    data object ClearToast : UiAction
    data class CheckUpdate(val manual: Boolean) : UiAction
    data class SaveApiKey(val key: String) : UiAction
    data class AskCoach(val question: String) : UiAction
    data class ReduceMotion(val enabled: Boolean) : UiAction
    data class Simulate(val enabled: Boolean) : UiAction
    data class SimulatePercent(val percent: Float) : UiAction
    data class ForceMood(val mood: Mood?) : UiAction
    data class PreviewSpecies(val species: Species?) : UiAction
    data class Animation(val kind: AnimationKind?) : UiAction
    data class Loop(val enabled: Boolean) : UiAction
    data class TriggerAnimation(val kind: AnimationKind) : UiAction
    data class FakeTime(val enabled: Boolean) : UiAction
    data class FakeHours(val hours: Int) : UiAction
    data class Growth(val friendId: Long, val points: Int, val stage: GrowthStage?) : UiAction
    data class Streaks(val current: Int, val best: Int) : UiAction
    data object TestNotification : UiAction
    data class FakeHistory(val days: Int) : UiAction
    data object ClearAll : UiAction
    data object AddTestFriends : UiAction
    data object RemoveTestFriends : UiAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(
    private val repository: WaterRepository,
    private val settings: SettingsRepository,
    private val clock: FakeClock,
    private val scheduler: ReminderScheduler,
    private val notifier: Notifier
) : ViewModel() {

    private val _dev = MutableStateFlow(DevState())
    private val _feedback = MutableStateFlow(Feedback())
    private val _update = MutableStateFlow(UpdateState())
    private val _coach = MutableStateFlow(CoachState())

    private val dayFlow: Flow<String> = clock.tick
        .map { DayKeys.key(it, clock.zone()) }
        .distinctUntilChanged()

    private val daily: Flow<Triple<String, Int, List<DrinkEntity>>> = dayFlow.flatMapLatest { day ->
        combine(repository.total(day), repository.drinks(day)) { total, drinks -> Triple(day, total, drinks) }
    }

    private val configAndFriends = combine(settings.settings, repository.friends) { cfg, friends -> cfg to friends }

    private val core = combine(configAndFriends, daily) { pair, dayData ->
        Triple(pair.first, pair.second, dayData)
    }

    private val loaded = combine(core, repository.history(31), _dev) { c, history, dev ->
        Five(c.first, c.second, c.third, history, dev)
    }

    private val withClock = combine(loaded, clock.tick) { l, now -> l to now }

    private val withFeedback = combine(withClock, _feedback) { pair, feedback ->
        Triple(pair.first, pair.second, feedback)
    }

    private data class LoadedState(
        val five: Five, val now: Long, val feedback: Feedback, val update: UpdateState
    )

    private val withUpdate = combine(withFeedback, _update) { triple, update ->
        LoadedState(triple.first, triple.second, triple.third, update)
    }

    val state = combine(withUpdate, _coach) { loaded, coach ->
        assemble(
            loaded.five, loaded.now, loaded.feedback, loaded.update, coach,
            loaded.five.settings.openRouterKey
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), UiState())

    private data class Five(
        val settings: Settings,
        val friends: List<FriendEntity>,
        val daily: Triple<String, Int, List<DrinkEntity>>,
        val history: List<DayStatsEntity>,
        val dev: DevState
    )

    private fun assemble(
        l: Five, now: Long, feedback: Feedback, update: UpdateState,
        coach: CoachState, apiKey: String
    ): UiState {
        val cfg = l.settings
        val active = l.friends.firstOrNull { it.id == cfg.activeFriendId } ?: l.friends.firstOrNull()
        val effective = if (l.dev.simulate)
            (cfg.goalMl * l.dev.simulatePercent).toInt().coerceAtLeast(0)
        else l.daily.second
        return UiState(
            settings = cfg,
            friends = l.friends,
            activeFriend = active,
            day = l.daily.first,
            totalMl = l.daily.second,
            effectiveTotalMl = effective,
            drinks = l.daily.third,
            history = l.history,
            now = now,
            progress = MoodCalculator.progress(effective, cfg.goalMl),
            pace = MoodCalculator.pace(now, clock.zone(), cfg.wakeStartHour, cfg.wakeEndHour),
            mood = l.dev.forcedMood ?: MoodCalculator.mood(
                effective, cfg.goalMl, now, clock.zone(), cfg.wakeStartHour, cfg.wakeEndHour
            ),
            dev = l.dev,
            feedback = feedback,
            update = update,
            coach = coach,
            apiKey = apiKey
        )
    }

    init {
        viewModelScope.launch {
            repository.events.collect { event ->
                _feedback.update { old ->
                    val next = old.serial + 1
                    when (event) {
                        is AppEvent.Celebrate -> old.copy(serial = next, celebrateName = event.friendName,
                            lastEvent = "Dance: ${event.friendName}")
                        is AppEvent.LevelUp -> old.copy(serial = next, levelUp = event.stage,
                            levelUpName = event.friendName, lastEvent = "Level up: ${event.stage.label}")
                        is AppEvent.Streak -> old.copy(serial = next, streak = event.days,
                            lastEvent = "Streak: ${event.days} days")
                        is AppEvent.Message -> old.copy(serial = next, toast = event.text,
                            lastEvent = event.text)
                    }
                }
            }
        }
        viewModelScope.launch {
            clock.tick.collect { repository.ensureDayRollover() }
        }
        viewModelScope.launch {
            val cfg = settings.snapshot()
            val day = DayKeys.key(clock.now(), clock.zone())
            if (cfg.lastUpdateCheckDay != day) {
                settings.setUpdateCheckDay(day)
                checkUpdate(manual = false)
            }
        }
    }

    fun act(action: UiAction) {
        viewModelScope.launch { handle(action) }
    }

    private suspend fun handle(a: UiAction) {
        when (a) {
            is UiAction.Onboard -> repository.onboard(a.name, a.species, a.goal)
            is UiAction.Log -> {
                if (state.value.dev.simulate) toast("Simulation on — nothing was saved")
                else repository.logWater(a.ml)
            }
            UiAction.ReplayCelebration -> repository.replayCelebration()
            UiAction.Undo -> if (state.value.dev.simulate) toast("Simulation on — nothing was saved")
            else repository.undoLast()
            is UiAction.DeleteDrink -> repository.deleteDrink(a.id)
            is UiAction.SelectFriend -> repository.setActiveFriend(a.id)
            is UiAction.AddFriend -> repository.addFriend(a.name, a.species)?.let { toast(it) }
            is UiAction.RenameFriend -> repository.renameFriend(a.id, a.name)
            is UiAction.DeleteFriend -> repository.deleteFriend(a.id)?.let { toast(it) }
            is UiAction.Goal -> settings.setGoal(a.ml)
            is UiAction.Units -> settings.setUnits(a.units)
            is UiAction.Theme -> settings.setThemeMode(a.mode)
            is UiAction.Accent -> settings.setAccent(a.accent)
            is UiAction.Reminder -> {
                settings.setReminder(a.enabled)
                scheduler.refresh(scheduler.canScheduleExact())
            }
            is UiAction.Interval -> {
                settings.setInterval(a.minutes)
                scheduler.refresh(scheduler.canScheduleExact())
            }
            is UiAction.WakeHours -> {
                settings.setWakeHours(a.start, a.end)
                scheduler.refresh(scheduler.canScheduleExact())
            }
            is UiAction.DebugOverlay -> settings.setDebugOverlay(a.enabled)
            UiAction.UnlockDev -> {
                settings.setDevUnlocked(true)
                toast("Developer options unlocked")
            }
            UiAction.LockDev -> {
                settings.setDevUnlocked(false)
                toast("Developer options locked")
            }
            UiAction.ClearToast -> _feedback.update { it.copy(toast = null) }
            is UiAction.CheckUpdate -> checkUpdate(a.manual)
            is UiAction.SaveApiKey -> {
                settings.setOpenRouterKey(a.key)
                toast("API key saved on this device")
            }
            is UiAction.AskCoach -> askCoach(a.question)
            is UiAction.ReduceMotion -> settings.setReduceMotion(a.enabled)
            is UiAction.Simulate -> _dev.update { it.copy(simulate = a.enabled) }
            is UiAction.SimulatePercent -> _dev.update { it.copy(simulatePercent = a.percent) }
            is UiAction.ForceMood -> _dev.update { it.copy(forcedMood = a.mood) }
            is UiAction.PreviewSpecies -> _dev.update { it.copy(previewSpecies = a.species) }
            is UiAction.Animation -> _dev.update { it.copy(forcedAnimation = a.kind) }
            is UiAction.Loop -> _dev.update { it.copy(loopAnimation = a.enabled) }
            is UiAction.TriggerAnimation -> _dev.update {
                it.copy(forcedAnimation = a.kind, animationSerial = it.animationSerial + 1)
            }
            is UiAction.FakeTime -> {
                clock.setEnabled(a.enabled)
                _dev.update { it.copy(fakeTime = a.enabled) }
            }
            is UiAction.FakeHours -> {
                clock.setOffsetHours(a.hours)
                _dev.update { it.copy(fakeHours = a.hours) }
            }
            is UiAction.Growth -> {
                val stage = repository.devSetGrowth(a.friendId, a.points, a.stage)
                toast("Growth set to ${stage?.label ?: "unknown"}")
            }
            is UiAction.Streaks -> {
                repository.devSetStreaks(a.current, a.best)
                toast("Streaks updated")
            }
            UiAction.TestNotification -> notifier.show(repository.activeFriendName(), test = true)
            is UiAction.FakeHistory -> toast(repository.devSeedHistory(a.days))
            UiAction.ClearAll -> {
                repository.devClearAll()
                _dev.update { DevState() }
            }
            UiAction.AddTestFriends -> toast(repository.devAddOneOfEachSpecies())
            UiAction.RemoveTestFriends -> toast(repository.devRemoveTestFriends())
        }
    }

    private suspend fun askCoach(question: String) {
        _coach.update { it.copy(busy = true, error = null) }
        try {
            val cfg = settings.snapshot()
            val recent = repository.history(7).first()
            val average = if (recent.isEmpty()) 0 else recent.sumOf { it.totalMl } / recent.size
            val context = CoachContext(
                friendName = repository.activeFriendName(),
                totalMl = state.value.totalMl,
                goalMl = cfg.goalMl,
                streak = cfg.currentStreak,
                mood = state.value.mood.label,
                average7DaysMl = average
            )
            val answer = SmartCoach.ask(cfg.openRouterKey, question, context)
            _coach.update { CoachState(false, answer, null) }
        } catch (e: Exception) {
            _coach.update { CoachState(false, null, e.message ?: "Could not reach OpenRouter") }
        }
    }

    private suspend fun checkUpdate(manual: Boolean) {
        _update.update { it.copy(busy = true, message = null) }
        try {
            val info = withContext(Dispatchers.IO) { UpdateChecker.check() }
            val available = UpdateChecker.isNewer(info.version, com.sippy.app.BuildConfig.VERSION_NAME)
            val message = if (available) "Update ${info.version} is available"
            else "You are up to date (${info.version})"
            _update.update { UpdateState(false, info, available, message) }
            if (manual) toast(message)
        } catch (e: Exception) {
            val message = "Could not check for updates (${e.message ?: "network error"})"
            _update.update { UpdateState(false, null, false, message) }
            if (manual) toast(message)
        }
    }

    private fun toast(text: String) {
        _feedback.update { it.copy(serial = it.serial + 1, toast = text, lastEvent = text) }
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/Common.kt`

```kotlin
package com.sippy.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sippy.app.data.DayStatsEntity
import com.sippy.app.domain.AmountFormat
import com.sippy.app.domain.DayKeys
import com.sippy.app.ui.theme.LocalFriendColors

val LocalAnimationsEnabled = compositionLocalOf { true }

@Composable
fun ProgressRing(
    progress: Float,
    centerLabel: String,
    bottomLabel: String,
    modifier: Modifier = Modifier,
    size: Int = 240
) {
    val colors = LocalFriendColors.current
    val clamped = progress.coerceIn(0f, 1f)
    Box(modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize().semantics {
            contentDescription = "Daily progress ${(clamped * 100).toInt()} percent"
        }) {
            val stroke = this.size.minDimension * 0.085f
            val inset = stroke / 2f + 2f
            val topLeft = Offset(inset, inset)
            val arcSize = Size(this.size.width - inset * 2f, this.size.height - inset * 2f)
            drawArc(
                color = colors.light, startAngle = -90f, sweepAngle = 360f, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke)
            )
            drawArc(
                color = colors.primary, startAngle = -90f, sweepAngle = 360f * clamped, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerLabel, style = MaterialTheme.typography.headlineMedium, color = colors.ink)
            Text(bottomLabel, style = MaterialTheme.typography.bodyMedium, color = colors.primary)
        }
    }
}

@Composable
fun HistoryBars(
    days: List<DayStatsEntity>,
    goalMl: Int,
    daysToShow: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalFriendColors.current
    val shown = days.take(daysToShow).asReversed()
    val max = (listOf(goalMl.toFloat()) + shown.map { it.totalMl.toFloat() }).maxOrNull() ?: goalMl.toFloat()
    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .semantics { contentDescription = "Water history bar chart for $daysToShow days" }
        ) {
            val barWidth = size.width / shown.size.coerceAtLeast(1) * 0.62f
            val step = size.width / shown.size.coerceAtLeast(1)
            val goalY = size.height - (goalMl / max) * size.height * 0.9f
            drawLine(
                color = colors.glow, start = Offset(0f, goalY), end = Offset(size.width, goalY),
                strokeWidth = 2f
            )
            shown.forEachIndexed { index, day ->
                val h = (day.totalMl / max) * size.height * 0.9f
                val x = index * step + (step - barWidth) / 2f
                drawRect(
                    color = if (day.targetHit) colors.primary else colors.secondary,
                    topLeft = Offset(x, size.height - h),
                    size = Size(barWidth, h)
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val labelStep = if (daysToShow == 7) 1 else 5
            shown.forEachIndexed { index, day ->
                if (index % labelStep == 0 || index == shown.lastIndex) {
                    Text(
                        text = day.dayKey.takeLast(5).replace("-", "/"),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
        Text("Goal line: ${AmountFormat.display(goalMl, "ml")}", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun NumberDialog(
    title: String,
    initial: String,
    label: String,
    min: Int,
    max: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    val value = text.toIntOrNull()
    val valid = value != null && value in min..max
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() } },
                    label = { Text(label) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (!valid) Text("Enter a whole number from $min to $max", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { value?.let(onConfirm) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDialog(title: String, body: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { Button(onClick = onConfirm) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}
```

---

## `app/src/main/java/com/sippy/app/ui/FriendArt.kt`

```kotlin
package com.sippy.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.Mood
import com.sippy.app.domain.Species
import com.sippy.app.ui.theme.FriendColors
import com.sippy.app.ui.theme.LocalFriendColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class AnimationKind(val label: String) {
    BLINK("Blink"), BOB("Bob"), GIGGLE("Giggle"), DANCE("Dance"), CONFETTI("Confetti"), LEVEL_UP("Level-up")
}

/** Precomputed translucent colors so the draw loop never allocates. */
private data class ArtPaint(
    val white: Color,
    val white75: Color,
    val white70: Color,
    val white50: Color,
    val white85: Color,
    val shine80: Color,
    val ink50: Color,
    val ink55: Color,
    val ink60: Color,
    val glow55: Color,
    val particles: List<Color>
)

/**
 * One Canvas, one infinite phase from the caller. Richer depth shading is done with gradients;
 * no bitmaps, no Lottie and no allocations in the repeated draw loop.
 */
@Composable
fun FriendCanvas(
    species: Species,
    mood: Mood,
    stage: GrowthStage,
    phase: Float,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    animation: AnimationKind? = null,
    animationProgress: Float = 0f,
    animationsEnabled: Boolean = true,
    description: String? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalFriendColors.current
    val body = remember(colors) {
        Brush.linearGradient(listOf(colors.light, colors.secondary, colors.primary))
    }
    val aura = remember(colors) {
        Brush.radialGradient(listOf(colors.glow.copy(alpha = 0.4f), Color.Transparent))
    }
    val glass = remember(colors) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0f)))
    }
    val paint = remember(colors) {
        ArtPaint(
            white = Color.White,
            white75 = Color.White.copy(alpha = 0.75f),
            white70 = Color.White.copy(alpha = 0.7f),
            white50 = Color.White.copy(alpha = 0.5f),
            white85 = Color.White.copy(alpha = 0.85f),
            shine80 = Color.White.copy(alpha = 0.8f),
            ink50 = colors.ink.copy(alpha = 0.5f),
            ink55 = colors.ink.copy(alpha = 0.55f),
            ink60 = colors.ink.copy(alpha = 0.6f),
            glow55 = colors.glow.copy(alpha = 0.55f),
            particles = listOf(
                colors.primary.copy(alpha = 0.85f),
                colors.glow.copy(alpha = 0.75f),
                colors.secondary.copy(alpha = 0.75f)
            )
        )
    }
    val scale = when (stage) {
        GrowthStage.BABY -> 0.74f
        GrowthStage.YOUNG -> 0.88f
        GrowthStage.GROWN -> 1f
        GrowthStage.ELDER -> 1.12f
    }
    val t = if (animationsEnabled) phase * 2f * PI.toFloat() else 0f
    val sleepy = mood == Mood.SLEEPY && animation != AnimationKind.BOB
    val bob = if (animationsEnabled && !sleepy) sin(t + 0.4f) * 7f else 0f
    val dancing = mood == Mood.DANCING || animation == AnimationKind.DANCE
    val rotation = if (dancing && animationsEnabled) sin(t * 3f) * 10f else 0f
    val giggle = if (animation == AnimationKind.GIGGLE && animationsEnabled)
        1f + sin(animationProgress * PI.toFloat() * 4f) * 0.08f else 1f
    val squish = if (animation == AnimationKind.LEVEL_UP && animationsEnabled)
        1f + sin(animationProgress * PI.toFloat()) * 0.12f else 1f
    val blink = (animation == AnimationKind.BLINK && animationProgress < 0.16f) ||
        (animationsEnabled && (phase * 9f) % 1f < 0.05f)
    val triangle = remember { Path() }
    val interaction = remember { MutableInteractionSource() }

    Canvas(
        modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable(interactionSource = interaction,
                indication = null, onClick = onClick) else Modifier)
            .graphicsLayer(
                translationY = bob,
                rotationZ = rotation,
                scaleX = scale * giggle / squish,
                scaleY = scale * giggle * squish
            )
            .semantics(mergeDescendants = true) { description?.let { contentDescription = it } }
    ) {
        val cx = center.x
        val cy = center.y
        val r = size.toPx() * 0.30f
        drawCircle(brush = aura, radius = r * 1.75f, center = Offset(cx, cy + r * 0.15f))
        when (species) {
            Species.DROPLET -> drawDroplet(cx, cy, r, mood, blink, dancing, stage, body, glass, colors, paint, triangle, t)
            Species.SPROUT -> drawSprout(cx, cy, r, mood, blink, dancing, stage, body, glass, colors, paint, t)
            Species.KITTY -> drawKitty(cx, cy, r, mood, blink, dancing, stage, body, glass, colors, paint, t)
            Species.BLOBBY -> drawBlobby(cx, cy, r, mood, blink, dancing, stage, body, glass, colors, paint, t)
        }
        if (mood == Mood.HAPPY || dancing) {
            val sparkle = if (animationsEnabled) 0.45f + 0.45f * sin(t * 2f) else 0.8f
            for (i in 0 until 4) {
                val a = t + i * (PI.toFloat() / 2f)
                drawCircle(colors.glow, radius = 3f + (i % 2) * 2f, alpha = sparkle,
                    center = Offset(cx + cos(a) * r * 1.4f, cy + sin(a) * r * 1.3f - r * 0.4f))
            }
        }
        if (dancing || animation == AnimationKind.CONFETTI || animation == AnimationKind.LEVEL_UP) {
            drawParticles(cx, cy, r, paint.particles, phase, animationsEnabled)
        }
    }
}

private fun DrawScope.drawDroplet(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, body: Brush, glass: Brush, colors: FriendColors, paint: ArtPaint, triangle: Path, t: Float
) {
    drawCircle(brush = body, radius = r, center = Offset(cx, cy))
    triangle.rewind()
    triangle.moveTo(cx, cy - r * 1.72f)
    triangle.lineTo(cx - r * 0.55f, cy - r * 0.42f)
    triangle.lineTo(cx + r * 0.55f, cy - r * 0.42f)
    triangle.close()
    drawPath(triangle, brush = body)
    drawOval(brush = glass, topLeft = Offset(cx - r * 0.72f, cy - r * 0.78f),
        size = Size(r * 0.55f, r * 0.95f))
    drawCircle(paint.white75, r * 0.1f, Offset(cx - r * 0.42f, cy - r * 0.42f))
    drawArc(color = paint.glow55, startAngle = 20f, sweepAngle = 140f,
        useCenter = false, topLeft = Offset(cx - r * 0.82f, cy + r * 0.2f),
        size = Size(r * 1.64f, r * 1.3f), style = Stroke(width = r * 0.06f))
    drawFace(cx, cy + r * 0.06f, r * 0.29f, mood, blink, dancing, colors, paint, t)
    if (stage == GrowthStage.ELDER) {
        drawCircle(paint.white85, r * 0.09f, Offset(cx + r * 0.58f, cy + r * 0.56f))
        drawCircle(paint.white70, r * 0.07f, Offset(cx - r * 0.62f, cy + r * 0.46f))
    }
}

private fun DrawScope.drawSprout(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, body: Brush, glass: Brush, colors: FriendColors, paint: ArtPaint, t: Float
) {
    val sway = if (dancing) sin(t * 3f) * r * 0.14f else sin(t + 0.7f) * r * 0.035f
    drawOval(brush = glass, topLeft = Offset(cx - r * 0.85f, cy + r * 0.28f),
        size = Size(r * 1.7f, r * 0.95f))
    drawRoundRectBox(colors.primary, Rect(cx - r * 0.78f, cy + r * 0.38f, cx + r * 0.78f, cy + r * 1.18f), r * 0.22f)
    drawRoundRect(brush = body, topLeft = Offset(cx - r * 0.78f, cy + r * 0.38f),
        size = Size(r * 1.56f, r * 0.8f), cornerRadius = CornerRadius(r * 0.22f, r * 0.22f))
    drawLine(paint.ink55, Offset(cx, cy + r * 0.42f),
        Offset(cx + sway, cy - r * 0.95f), r * 0.1f)
    drawOval(brush = body, topLeft = Offset(cx + sway - r * 1.02f, cy - r * 0.62f),
        size = Size(r * 0.98f, r * 0.5f))
    drawOval(brush = body, topLeft = Offset(cx + sway + r * 0.04f, cy - r * 0.86f),
        size = Size(r * 0.98f, r * 0.5f))
    drawLine(paint.white50, Offset(cx + sway - r * 0.85f, cy - r * 0.42f),
        Offset(cx + sway - r * 0.2f, cy - r * 0.3f), r * 0.035f)
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        for (i in 0 until 5) {
            val a = i * (PI.toFloat() * 2f / 5f) + if (dancing) t else 0f
            drawCircle(colors.glow, r * 0.23f,
                Offset(cx + sway + cos(a) * r * 0.4f, cy - r * 1f + sin(a) * r * 0.4f))
        }
        drawCircle(colors.light, r * 0.2f, Offset(cx + sway, cy - r * 1f))
    }
    if (stage == GrowthStage.ELDER) {
        drawOval(brush = body, topLeft = Offset(cx - r * 1.18f, cy + r * 0.02f), size = Size(r * 0.8f, r * 0.38f))
        drawOval(brush = body, topLeft = Offset(cx + r * 0.38f, cy - r * 0.12f), size = Size(r * 0.8f, r * 0.38f))
    }
    drawFace(cx + sway * 0.25f, cy + r * 0.72f, r * 0.22f, mood, blink, dancing, colors, paint, t)
}

private fun DrawScope.drawKitty(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, body: Brush, glass: Brush, colors: FriendColors, paint: ArtPaint, t: Float
) {
    val hop = if (dancing) sin(t * 4f) * r * 0.14f else 0f
    val tail = if (dancing) sin(t * 2f) * r * 0.36f else r * 0.2f
    drawLine(colors.primary, Offset(cx + r * 0.82f, cy + r * 0.74f),
        Offset(cx + r * 1.38f + tail, cy + r * 0.16f), r * 0.16f)
    drawCircle(brush = body, radius = r * 0.84f, center = Offset(cx, cy - r * 0.16f + hop))
    drawCircle(colors.primary, r * 0.38f, Offset(cx - r * 0.64f, cy - r * 0.74f + hop))
    drawCircle(colors.primary, r * 0.38f, Offset(cx + r * 0.64f, cy - r * 0.74f + hop))
    drawCircle(colors.glow, r * 0.2f, Offset(cx - r * 0.64f, cy - r * 0.76f + hop))
    drawCircle(colors.glow, r * 0.2f, Offset(cx + r * 0.64f, cy - r * 0.76f + hop))
    drawOval(brush = glass, topLeft = Offset(cx - r * 0.6f, cy - r * 0.82f + hop), size = Size(r * 0.5f, r * 0.72f))
    drawFace(cx, cy - r * 0.16f + hop, r * 0.28f, mood, blink, dancing, colors, paint, t)
    for (i in 0 until 2) {
        val side = if (i == 0) -1f else 1f
        drawLine(paint.ink60, Offset(cx + side * r * 0.5f, cy + hop),
            Offset(cx + side * r * 1f, cy - r * 0.08f + hop), r * 0.035f)
        drawLine(paint.ink60, Offset(cx + side * r * 0.5f, cy + r * 0.15f + hop),
            Offset(cx + side * r * 1f, cy + r * 0.24f + hop), r * 0.035f)
    }
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        drawOvalBox(colors.glow, Rect(cx - r * 1f, cy + r * 0.68f + hop, cx - r * 0.1f, cy + r * 1.04f + hop))
        drawOvalBox(colors.glow, Rect(cx + r * 0.1f, cy + r * 0.68f + hop, cx + r * 1f, cy + r * 1.04f + hop))
        drawCircle(colors.primary, r * 0.18f, Offset(cx, cy + r * 0.86f + hop))
    }
    if (stage == GrowthStage.ELDER) {
        for (i in -1..1) drawCircle(colors.light, r * 0.19f, Offset(cx + i * r * 0.44f, cy - r * 1.14f + hop))
    }
}

private fun DrawScope.drawBlobby(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, body: Brush, glass: Brush, colors: FriendColors, paint: ArtPaint, t: Float
) {
    val wobble = if (dancing) sin(t * 4f) * r * 0.16f else sin(t) * r * 0.045f
    drawOval(brush = body, topLeft = Offset(cx - r - wobble, cy - r * 0.7f),
        size = Size(r * 2f + wobble * 2f, r * 1.85f))
    drawCircle(brush = body, radius = r * 0.74f, center = Offset(cx - r * 0.62f, cy - r * 0.25f))
    drawCircle(brush = body, radius = r * 0.74f, center = Offset(cx + r * 0.62f, cy - r * 0.25f))
    drawOvalBox(colors.secondary, Rect(cx - r * 0.58f, cy + r * 0.95f, cx - r * 0.05f, cy + r * 1.32f))
    drawOvalBox(colors.secondary, Rect(cx + r * 0.05f, cy + r * 0.95f, cx + r * 0.58f, cy + r * 1.32f))
    drawOval(brush = glass, topLeft = Offset(cx - r * 0.6f, cy - r * 0.62f), size = Size(r * 0.5f, r * 0.7f))
    drawFace(cx, cy + r * 0.1f, r * 0.31f, mood, blink, dancing, colors, paint, t)
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        drawCircle(colors.glow, r * 0.2f, Offset(cx, cy - r * 1f))
        drawCircle(colors.glow, r * 0.14f, Offset(cx - r * 0.3f, cy - r * 0.84f))
        drawCircle(colors.glow, r * 0.14f, Offset(cx + r * 0.3f, cy - r * 0.84f))
    }
    if (stage == GrowthStage.ELDER) {
        drawLine(colors.ink, Offset(cx - r * 0.3f, cy + r * 1f), Offset(cx - r * 0.46f, cy + r * 1.32f), r * 0.05f)
        drawLine(colors.ink, Offset(cx + r * 0.3f, cy + r * 1f), Offset(cx + r * 0.46f, cy + r * 1.32f), r * 0.05f)
    }
}

private fun DrawScope.drawFace(
    cx: Float, cy: Float, gap: Float, mood: Mood, blink: Boolean,
    dancing: Boolean, colors: FriendColors, paint: ArtPaint, t: Float
) {
    val ink = colors.ink
    val eyeR = gap * 0.27f
    val left = cx - gap
    val right = cx + gap
    val browTilt = when (mood) {
        Mood.THIRSTY -> gap * 0.22f
        Mood.HAPPY, Mood.DANCING -> -gap * 0.12f
        else -> 0f
    }
    drawLine(paint.ink50, Offset(left - eyeR, cy - eyeR * 2.1f + browTilt),
        Offset(left + eyeR, cy - eyeR * 2.1f - browTilt), eyeR * 0.18f)
    drawLine(paint.ink50, Offset(right - eyeR, cy - eyeR * 2.1f - browTilt),
        Offset(right + eyeR, cy - eyeR * 2.1f + browTilt), eyeR * 0.18f)
    when {
        mood == Mood.SLEEPY && !blink -> {
            drawArcBox(Rect(left - eyeR, cy - eyeR * 0.6f, left + eyeR, cy + eyeR * 0.6f),
                200f, 140f, ink, eyeR * 0.3f)
            drawArcBox(Rect(right - eyeR, cy - eyeR * 0.6f, right + eyeR, cy + eyeR * 0.6f),
                200f, 140f, ink, eyeR * 0.3f)
            for (i in 0 until 3) {
                val zx = cx + gap * 1.6f + i * eyeR * 0.7f
                val zy = cy - eyeR * 2.4f - i * eyeR * 1.2f + sin(t + i) * 2f
                drawLine(colors.glow, Offset(zx, zy), Offset(zx + eyeR, zy), eyeR * 0.16f)
                drawLine(colors.glow, Offset(zx + eyeR, zy), Offset(zx, zy + eyeR), eyeR * 0.16f)
                drawLine(colors.glow, Offset(zx, zy + eyeR), Offset(zx + eyeR, zy + eyeR), eyeR * 0.16f)
            }
        }
        blink -> {
            drawLine(ink, Offset(left - eyeR, cy), Offset(left + eyeR, cy), eyeR * 0.32f)
            drawLine(ink, Offset(right - eyeR, cy), Offset(right + eyeR, cy), eyeR * 0.32f)
        }
        mood == Mood.HAPPY || dancing -> {
            drawCircle(ink, eyeR, Offset(left, cy))
            drawCircle(ink, eyeR, Offset(right, cy))
            for (i in 0 until 2) {
                val ex = if (i == 0) left else right
                drawLine(paint.white, Offset(ex - eyeR * 0.45f, cy - eyeR * 1.7f),
                    Offset(ex + eyeR * 0.45f, cy - eyeR * 1.7f), eyeR * 0.3f)
                drawLine(paint.white, Offset(ex, cy - eyeR * 2.15f),
                    Offset(ex, cy - eyeR * 1.25f), eyeR * 0.3f)
            }
            drawCircle(paint.glow55, gap * 0.34f, Offset(cx - gap * 1.75f, cy + eyeR))
            drawCircle(paint.glow55, gap * 0.34f, Offset(cx + gap * 1.75f, cy + eyeR))
        }
        else -> {
            drawCircle(ink, eyeR, Offset(left, cy))
            drawCircle(ink, eyeR, Offset(right, cy))
            drawCircle(paint.shine80, eyeR * 0.3f, Offset(left - eyeR * 0.3f, cy - eyeR * 0.35f))
            drawCircle(paint.shine80, eyeR * 0.3f, Offset(right - eyeR * 0.3f, cy - eyeR * 0.35f))
        }
    }
    if (mood == Mood.THIRSTY) {
        drawCircle(colors.secondary, eyeR * 0.35f, Offset(cx + gap * 2f, cy - eyeR * 1.6f))
    }
    val mouthY = cy + gap * 1.25f
    when (mood) {
        Mood.THIRSTY -> drawArcBox(Rect(cx - gap * 0.62f, mouthY + gap * 0.3f, cx + gap * 0.62f, mouthY + gap),
            180f, 180f, ink, eyeR * 0.28f)
        Mood.SLEEPY -> drawLine(ink, Offset(cx - eyeR, mouthY), Offset(cx + eyeR, mouthY), eyeR * 0.32f)
        Mood.HAPPY, Mood.DANCING -> drawOval(ink, topLeft = Offset(cx - gap * 0.55f, mouthY - eyeR * 0.4f),
            size = Size(gap * 1.1f, eyeR * 1.5f))
        else -> drawArcBox(Rect(cx - gap * 0.62f, mouthY - gap * 0.35f, cx + gap * 0.62f, mouthY + gap * 0.35f),
            0f, 180f, ink, eyeR * 0.28f)
    }
}

private fun DrawScope.drawParticles(
    cx: Float, cy: Float, r: Float, particleColors: List<Color>, phase: Float, enabled: Boolean
) {
    val p = if (enabled) phase % 1f else 0.35f
    for (i in 0 until 18) {
        val seed = (i * 37 % 100) / 100f
        val x = cx + (seed - 0.5f) * r * 3.4f
        val y = cy - r * 1.8f + ((p + seed) % 1f) * r * 3.4f
        val color = particleColors[i % 3]
        if (i % 2 == 0) drawCircle(color, r * 0.07f, Offset(x, y))
        else drawRect(color, Offset(x, y), Size(r * 0.12f, r * 0.18f))
    }
}

private fun DrawScope.drawOvalBox(color: Color, rect: Rect, alpha: Float = 1f) = drawOval(
    color = color, topLeft = Offset(rect.left, rect.top), size = Size(rect.width, rect.height), alpha = alpha
)

private fun DrawScope.drawRoundRectBox(color: Color, rect: Rect, corner: Float, alpha: Float = 1f) =
    drawRoundRect(
        color = color, topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height), cornerRadius = CornerRadius(corner, corner), alpha = alpha
    )

private fun DrawScope.drawArcBox(
    rect: Rect, start: Float, sweep: Float, color: Color, strokeWidth: Float
) = drawArc(
    color = color, startAngle = start, sweepAngle = sweep, useCenter = false,
    topLeft = Offset(rect.left, rect.top), size = Size(rect.width, rect.height),
    style = Stroke(width = strokeWidth), alpha = 0.85f
)
```

---

## `app/src/main/java/com/sippy/app/ui/Navigation.kt`

```kotlin
package com.sippy.app.ui

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sippy.app.R
import com.sippy.app.ui.screens.AboutScreen
import com.sippy.app.ui.screens.CoachScreen
import com.sippy.app.ui.screens.DevOptionsScreen
import com.sippy.app.ui.screens.FriendsScreen
import com.sippy.app.ui.screens.HistoryScreen
import com.sippy.app.ui.screens.HomeScreen
import com.sippy.app.ui.screens.OnboardingScreen
import com.sippy.app.ui.screens.SettingsScreen
import com.sippy.app.ui.theme.AppBackdrop
import com.sippy.app.ui.theme.SippyTheme

private data class Tab(val route: String, val label: String, val icon: Int)

private val tabs = listOf(
    Tab("home", "Home", R.drawable.ic_home),
    Tab("friends", "Friends", R.drawable.ic_friends),
    Tab("history", "History", R.drawable.ic_history),
    Tab("coach", "Coach", R.drawable.ic_coach),
    Tab("settings", "Settings", R.drawable.ic_settings)
)

@Composable
fun SippyNavigation(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val systemReduceMotion = remember {
        try {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        } catch (e: Exception) { false }
    }
    val reduceMotion = systemReduceMotion || (state.settings?.reduceMotion ?: false)
    CompositionLocalProvider(LocalAnimationsEnabled provides !reduceMotion) {
        SippyTheme(state.settings?.themeMode ?: "system", state.settings?.accent ?: "aqua") {
            val nav = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }
            val cfg = state.settings
            LaunchedEffect(state.feedback.toast) {
                state.feedback.toast?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.act(UiAction.ClearToast)
                }
            }
            if (cfg == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@SippyTheme
            }
            val start = if (cfg.onboardingDone) "home" else "onboarding"
            LaunchedEffect(cfg.onboardingDone) {
                if (!cfg.onboardingDone && nav.currentDestination?.route != "onboarding") {
                    nav.navigate("onboarding") {
                        popUpTo(nav.graph.startDestinationId) { inclusive = true }
                    }
                }
            }
            val backStack by nav.currentBackStackEntryAsState()
            val route = backStack?.destination?.route
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (route in tabs.map { it.route }) {
                        NavigationBar {
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = route == tab.route,
                                    onClick = { nav.navigate(tab.route) { launchSingleTop = true } },
                                    icon = { Icon(painterResource(tab.icon), contentDescription = tab.label) },
                                    label = { Text(tab.label) }
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.padding(padding).fillMaxSize()) {
                    AppBackdrop(animationsEnabled = !reduceMotion)
                    NavHost(nav, startDestination = start, modifier = Modifier.fillMaxSize()) {
                    composable("onboarding") { OnboardingScreen(viewModel::act) }
                    composable("home") { HomeScreen(state, viewModel::act) }
                    composable("friends") { FriendsScreen(state, viewModel::act) }
                    composable("history") { HistoryScreen(state, viewModel::act) }
                    composable("coach") { CoachScreen(state, viewModel::act) { nav.navigate("settings") } }
                    composable("settings") { SettingsScreen(state, viewModel::act, nav::navigate) }
                    composable("about") { AboutScreen(viewModel::act) { nav.popBackStack() } }
                    composable("dev") { DevOptionsScreen(state, viewModel::act) { nav.popBackStack() } }
                }
                }
            }
        }
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/screens/Coach.kt`

```kotlin
package com.sippy.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sippy.app.ai.CoachContext
import com.sippy.app.ai.SmartCoach
import com.sippy.app.ui.SectionTitle
import com.sippy.app.ui.UiAction
import com.sippy.app.ui.UiState
import com.sippy.app.ui.theme.GlassCard

@Composable
fun CoachScreen(state: UiState, onAction: (UiAction) -> Unit, onOpenSettings: () -> Unit) {
    val cfg = state.settings ?: return
    var question by remember { mutableStateOf("") }
    val recent = state.history.take(7)
    val average = if (recent.isEmpty()) 0 else recent.sumOf { it.totalMl } / recent.size
    val context = CoachContext(
        friendName = state.activeFriend?.name ?: "your friend",
        totalMl = state.totalMl,
        goalMl = cfg.goalMl,
        streak = cfg.currentStreak,
        mood = state.mood.label,
        average7DaysMl = average
    )

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Smart Coach", style = MaterialTheme.typography.headlineSmall)
        Text("Local insights work offline. AI answers use your OpenRouter key only when you ask.")
        Spacer(Modifier.height(12.dp))
        SectionTitle("What the app notices (on device)")
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                SmartCoach.localInsights(context).forEach { insight ->
                    Text("• $insight", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        SectionTitle("Ask the coach")
        if (state.apiKey.isBlank()) {
            Text("Add an OpenRouter API key in Settings to enable AI answers.")
            TextButton(onClick = onOpenSettings) { Text("Open Settings") }
        }
        OutlinedTextField(
            value = question,
            onValueChange = { question = it.take(300) },
            label = { Text("Your question") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = state.apiKey.isNotBlank()
        )
        Spacer(Modifier.height(8.dp))
        Row {
            Button(
                onClick = { onAction(UiAction.AskCoach(question)); question = "" },
                enabled = state.apiKey.isNotBlank() && question.isNotBlank() && !state.coach.busy
            ) { Text(if (state.coach.busy) "Thinking..." else "Ask") }
        }
        state.coach.answer?.let { answer ->
            Spacer(Modifier.height(12.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Text(answer, modifier = Modifier.padding(16.dp))
            }
        }
        state.coach.error?.let { error ->
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(80.dp))
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/screens/DevOptions.kt`

```kotlin
package com.sippy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.Mood
import com.sippy.app.domain.Rules
import com.sippy.app.domain.Species
import com.sippy.app.ui.AnimationKind
import com.sippy.app.ui.ConfirmDialog
import com.sippy.app.ui.NumberDialog
import com.sippy.app.ui.SectionTitle
import com.sippy.app.ui.UiAction
import com.sippy.app.ui.UiState

@Composable
fun DevOptionsScreen(state: UiState, onAction: (UiAction) -> Unit, onBack: () -> Unit) {
    val cfg = state.settings ?: return
    var growthFriend by remember { mutableStateOf(state.activeFriend?.id ?: -1L) }
    var points by remember { mutableStateOf((state.activeFriend?.growthPoints ?: 0).toFloat()) }
    var streakDialog by remember { mutableStateOf(false) }
    var bestDialog by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Developer options", style = MaterialTheme.typography.headlineSmall)
        Text("Simulate and preview never change real data. Buttons that write data are labelled explicitly.")

        SectionTitle("Mood previewer (preview only)")
        SpeciesPicker(state.dev.previewSpecies ?: Species.DROPLET) { onAction(UiAction.PreviewSpecies(it)) }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Mood.entries.forEach { mood ->
                FilterChip(selected = state.dev.forcedMood == mood,
                    onClick = { onAction(UiAction.ForceMood(mood)) },
                    label = { Text(mood.label) })
            }
        }
        TextButton(onClick = { onAction(UiAction.ForceMood(null)) }) { Text("Return to real mood") }

        SectionTitle("Animation tester (preview only)")
        Text("Species: ${state.dev.previewSpecies?.label ?: "active friend's species"}")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AnimationKind.entries.forEach { kind ->
                OutlinedButton(
                    onClick = { onAction(UiAction.TriggerAnimation(kind)) },
                    modifier = Modifier.height(48.dp).fillMaxWidth()
                ) { Text("Play ${kind.label}") }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Loop animation")
            Switch(checked = state.dev.loopAnimation, onCheckedChange = { onAction(UiAction.Loop(it)) })
        }
        TextButton(onClick = { onAction(UiAction.Animation(null)) }) { Text("Clear forced animation") }

        SectionTitle("Growth tester (writes real data)")
        Text("Apply a stage or point value to a real friend.")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            state.friends.forEach { friend ->
                FilterChip(selected = growthFriend == friend.id,
                    onClick = { growthFriend = friend.id; points = friend.growthPoints.toFloat() },
                    label = { Text(friend.name) })
            }
        }
        Slider(
            value = points, onValueChange = { points = it }, valueRange = 0f..5000f,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Points: ${points.toInt()} · current stage ${GrowthStage.entries.last { points >= it.minPoints }.label}")
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            GrowthStage.entries.forEach { stage ->
                OutlinedButton(onClick = {
                    onAction(UiAction.Growth(growthFriend, points.toInt(), stage))
                }) { Text(stage.label) }
            }
        }
        OutlinedButton(onClick = { onAction(UiAction.Growth(growthFriend, points.toInt(), null)) }) {
            Text("Apply exact points")
        }
        OutlinedButton(onClick = { onAction(UiAction.TriggerAnimation(AnimationKind.LEVEL_UP)) }) {
            Text("Play level-up animation")
        }

        SectionTitle("Test friends (writes real data)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAction(UiAction.AddTestFriends) }, enabled = Rules.canAddFriend(state.friends.size)) {
                Text("Add one of each species")
            }
            OutlinedButton(onClick = { onAction(UiAction.RemoveTestFriends) }) { Text("Remove test friends") }
        }

        SectionTitle("Progress simulator (preview only)")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Simulate mode")
            Switch(checked = state.dev.simulate, onCheckedChange = { onAction(UiAction.Simulate(it)) })
        }
        Text("Today's intake shown: ${(state.dev.simulatePercent * 100).toInt()}% of goal")
        Slider(
            value = state.dev.simulatePercent,
            onValueChange = { onAction(UiAction.SimulatePercent(it)) },
            valueRange = 0f..1.5f,
            modifier = Modifier.fillMaxWidth()
        )
        Text("A visible SIMULATE banner appears on Home. Log/Undo buttons do not write data while active.")

        SectionTitle("Time-of-day override (preview only)")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Fake time")
            Switch(checked = state.dev.fakeTime, onCheckedChange = { onAction(UiAction.FakeTime(it)) })
        }
        Slider(
            value = state.dev.fakeHours.toFloat(),
            onValueChange = { onAction(UiAction.FakeHours(it.toInt())) },
            valueRange = -12f..12f,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Offset: ${state.dev.fakeHours} hours · simulated time: " +
            java.time.Instant.ofEpochMilli(state.now).toString())

        SectionTitle("Streak editor (writes real data)")
        Text("Current ${cfg.currentStreak} · best ${cfg.bestStreak}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { streakDialog = true }) { Text("Set current") }
            OutlinedButton(onClick = { bestDialog = true }) { Text("Set best") }
        }

        SectionTitle("Notifications and data (writes real data)")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAction(UiAction.TestNotification) }) { Text("Fire test notification") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onAction(UiAction.FakeHistory(7)) }) { Text("Add 7 fake days") }
            OutlinedButton(onClick = { onAction(UiAction.FakeHistory(30)) }) { Text("Add 30 fake days") }
        }
        OutlinedButton(onClick = { confirmClear = true }) { Text("Clear all data") }

        SectionTitle("Overlay and access")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Debug overlay on Home")
            Switch(checked = cfg.debugOverlay, onCheckedChange = { onAction(UiAction.DebugOverlay(it)) })
        }
        Button(onClick = { onAction(UiAction.LockDev); onBack() }) { Text("Lock dev options") }
        Spacer(Modifier.height(80.dp))
    }

    if (streakDialog) NumberDialog("Current streak", cfg.currentStreak.toString(), "Days", 0, 3650,
        { streakDialog = false },
        { onAction(UiAction.Streaks(it, cfg.bestStreak)); streakDialog = false })
    if (bestDialog) NumberDialog("Best streak", cfg.bestStreak.toString(), "Days", 0, 3650,
        { bestDialog = false },
        { onAction(UiAction.Streaks(cfg.currentStreak, it.coerceAtLeast(cfg.currentStreak))); bestDialog = false })
    if (confirmClear) ConfirmDialog(
        title = "Clear all data?",
        body = "This permanently deletes all friends, drink entries, streaks and settings from this device.",
        onDismiss = { confirmClear = false },
        onConfirm = { onAction(UiAction.ClearAll); confirmClear = false }
    )
}
```

---

## `app/src/main/java/com/sippy/app/ui/screens/Friends.kt`

```kotlin
package com.sippy.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sippy.app.R
import com.sippy.app.data.FriendEntity
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.Mood
import com.sippy.app.domain.Rules
import com.sippy.app.domain.Species
import com.sippy.app.domain.stageFor
import com.sippy.app.ui.ConfirmDialog
import com.sippy.app.ui.FriendCanvas
import com.sippy.app.ui.LocalAnimationsEnabled
import com.sippy.app.ui.UiAction
import com.sippy.app.ui.UiState
import com.sippy.app.ui.theme.GlassCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FriendsScreen(state: UiState, onAction: (UiAction) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newSpecies by remember { mutableStateOf(Species.DROPLET) }
    var menuFor by remember { mutableStateOf<Long?>(null) }
    var renameFor by remember { mutableStateOf<FriendEntity?>(null) }
    var renameText by remember { mutableStateOf("") }
    var deleteFor by remember { mutableStateOf<FriendEntity?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Your friends", style = MaterialTheme.typography.headlineSmall)
            Button(
                onClick = { showAdd = true },
                enabled = Rules.canAddFriend(state.friends.size)
            ) { Text("Add friend") }
        }
        Text("${state.friends.size}/${Rules.MAX_FRIENDS} friends · tap to activate, long-press for options")
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.friends, key = { it.id }) { friend ->
                val species = runCatching { Species.valueOf(friend.species) }.getOrDefault(Species.DROPLET)
                val stage = stageFor(friend.growthPoints)
                GlassCard(
                    modifier = Modifier.combinedClickable(
                        onClick = { onAction(UiAction.SelectFriend(friend.id)) },
                        onLongClick = { menuFor = friend.id }
                    ),
                    alpha = if (friend.id == state.activeFriend?.id) 0.3f else 0.12f
                ) {
                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            FriendCanvas(
                                species = species, mood = Mood.CONTENT, stage = stage,
                                phase = 0.1f, size = 92.dp,
                                animationsEnabled = LocalAnimationsEnabled.current,
                                description = "${friend.name}, ${species.label}, ${stage.label}"
                            )
                            IconButton(
                                onClick = { menuFor = friend.id },
                                modifier = Modifier.align(Alignment.TopEnd).size(44.dp)
                            ) {
                                Icon(painterResource(R.drawable.ic_more), contentDescription = "Options for ${friend.name}", tint = MaterialTheme.colorScheme.onSurface)
                            }
                            DropdownMenu(expanded = menuFor == friend.id, onDismissRequest = { menuFor = null }) {
                                DropdownMenuItem(text = { Text("Make active") }, onClick = {
                                    onAction(UiAction.SelectFriend(friend.id)); menuFor = null
                                })
                                DropdownMenuItem(text = { Text("Rename") }, onClick = {
                                    renameFor = friend; renameText = friend.name; menuFor = null
                                })
                                DropdownMenuItem(text = { Text("Delete") }, enabled = Rules.canDeleteFriend(state.friends.size),
                                    onClick = { deleteFor = friend; menuFor = null })
                            }
                        }
                        Text(friend.name, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center,
                            maxLines = 1)
                        Text("${species.label} · ${stage.label}", style = MaterialTheme.typography.labelSmall)
                        Text("${friend.growthPoints} growth pts", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    if (showAdd) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Add a friend") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName, onValueChange = { newName = it.take(24) },
                        label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    SpeciesPicker(newSpecies) { newSpecies = it }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onAction(UiAction.AddFriend(newName, newSpecies)); showAdd = false; newName = ""
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    renameFor?.let { friend ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { renameFor = null },
            title = { Text("Rename ${friend.name}") },
            text = {
                OutlinedTextField(
                    value = renameText, onValueChange = { renameText = it.take(24) },
                    label = { Text("New name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onAction(UiAction.RenameFriend(friend.id, renameText)); renameFor = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { renameFor = null }) { Text("Cancel") } }
        )
    }

    deleteFor?.let { friend ->
        ConfirmDialog(
            title = "Delete ${friend.name}?",
            body = "This removes the friend and its growth. Drink entries stay in history. You cannot delete your last friend.",
            onDismiss = { deleteFor = null },
            onConfirm = { onAction(UiAction.DeleteFriend(friend.id)); deleteFor = null }
        )
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/screens/History.kt`

```kotlin
package com.sippy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sippy.app.R
import com.sippy.app.domain.AmountFormat
import com.sippy.app.domain.DayKeys
import com.sippy.app.ui.HistoryBars
import com.sippy.app.ui.SectionTitle
import com.sippy.app.ui.UiAction
import com.sippy.app.ui.UiState
import com.sippy.app.ui.theme.GlassCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(state: UiState, onAction: (UiAction) -> Unit) {
    val cfg = state.settings ?: return
    var days by remember { mutableStateOf(7) }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("History", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7, 30).forEach { n ->
                FilterChip(selected = days == n, onClick = { days = n }, label = { Text("$n days") })
            }
        }
        Spacer(Modifier.height(12.dp))
        GlassCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                HistoryBars(state.history, cfg.goalMl, days, Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.height(16.dp))
        SectionTitle("Today's entries")
        if (state.drinks.isEmpty()) {
            Text("Nothing logged today yet.")
        } else {
            state.drinks.forEach { drink ->
                GlassCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), alpha = 0.1f) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val time = Instant.ofEpochMilli(drink.timestamp)
                            .atZone(ZoneId.systemDefault()).toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                        Text("$time · ${AmountFormat.display(drink.amountMl, cfg.units)}")
                        IconButton(
                            onClick = { onAction(UiAction.DeleteDrink(drink.id)) },
                            modifier = androidx.compose.ui.Modifier.size(48.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_delete),
                                contentDescription = "Delete entry of ${drink.amountMl} ml",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Daily total: ${AmountFormat.display(state.totalMl, cfg.units)} · day ${state.day}",
            style = MaterialTheme.typography.bodyMedium)
        Text("Day key derived from device time zone (${DayKeys.key(state.now, java.util.TimeZone.getDefault())})",
            style = MaterialTheme.typography.labelSmall)
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/screens/Home.kt`

```kotlin
package com.sippy.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sippy.app.domain.AmountFormat
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.stageFor
import com.sippy.app.ui.FriendCanvas
import com.sippy.app.ui.LocalAnimationsEnabled
import com.sippy.app.ui.NumberDialog
import com.sippy.app.ui.ProgressRing
import com.sippy.app.ui.SectionTitle
import com.sippy.app.ui.UiAction
import com.sippy.app.ui.UiState
import com.sippy.app.ui.theme.GlassCard
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(state: UiState, onAction: (UiAction) -> Unit) {
    val cfg = state.settings ?: return
    val friend = state.activeFriend
    val animations = LocalAnimationsEnabled.current
    val haptics = LocalHapticFeedback.current
    val transition = rememberInfiniteTransition(label = "friend-phase")
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "phase"
    )
    val testerProgress = remember { Animatable(0f) }
    LaunchedEffect(state.dev.animationSerial) {
        if (state.dev.animationSerial > 0L && !state.dev.loopAnimation) {
            testerProgress.snapTo(0f)
            testerProgress.animateTo(1f, animationSpec = tween<Float>(900))
        }
    }
    var showCustom by remember { mutableStateOf(false) }
    var celebrate by remember { mutableStateOf(false) }

    LaunchedEffect(state.feedback.serial) {
        if (!state.feedback.lastEvent.startsWith("Dance")) return@LaunchedEffect
        if (animations) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        celebrate = true
        delay(3_200L)
        celebrate = false
    }
    LaunchedEffect(state.feedback.serial) {
        if (!state.feedback.lastEvent.startsWith("Level up") && !state.feedback.lastEvent.startsWith("Streak")) return@LaunchedEffect
        if (animations) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (state.dev.simulate) {
            GlassCard { Text("SIMULATE MODE — no changes are saved to the database",
                modifier = Modifier.padding(14.dp), color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Streak ${cfg.currentStreak} 🔥 / best ${cfg.bestStreak}",
                style = MaterialTheme.typography.titleSmall)
            Text(state.day, style = MaterialTheme.typography.labelMedium)
        }
        if (state.friends.size > 1) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.friends.forEach { f ->
                    FilterChip(
                        selected = f.id == friend?.id,
                        onClick = { onAction(UiAction.SelectFriend(f.id)) },
                        label = { Text(f.name) }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        val shownSpecies = state.dev.previewSpecies ?: friend?.species?.let { runCatching {
            com.sippy.app.domain.Species.valueOf(it)
        }.getOrNull() } ?: com.sippy.app.domain.Species.DROPLET
        val stage: GrowthStage = friend?.growthPoints?.let { stageFor(it) } ?: GrowthStage.BABY
        val animationKind = state.dev.forcedAnimation
        val testerValue = if (state.dev.loopAnimation) phase else testerProgress.value
        FriendCanvas(
            species = shownSpecies,
            mood = state.mood,
            stage = stage,
            phase = phase,
            size = 240.dp,
            animationsEnabled = animations,
            animation = animationKind,
            animationProgress = testerValue,
            description = "${friend?.name ?: "Friend"}, ${state.mood.label}, ${stage.label}",
            onClick = { onAction(UiAction.TriggerAnimation(com.sippy.app.ui.AnimationKind.GIGGLE)) }
        )
        Text(friend?.name ?: "Friend", style = MaterialTheme.typography.headlineSmall)
        Text("${shownSpecies.label} · ${stage.label} · ${state.mood.label}",
            style = MaterialTheme.typography.bodySmall)

        Spacer(Modifier.height(12.dp))
        GlassCard(alpha = 0.12f) {
            Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                ProgressRing(
                    progress = state.progress,
                    centerLabel = AmountFormat.display(state.effectiveTotalMl, cfg.units),
                    bottomLabel = "of ${AmountFormat.display(cfg.goalMl, cfg.units)} · ${(state.progress * 100).toInt()}%"
                )
                Text("Pace: ${(state.pace * 100).toInt()}% of the day elapsed",
                    style = MaterialTheme.typography.labelSmall)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(150, 250, 500).forEach { ml ->
                Button(onClick = { onAction(UiAction.Log(ml)) }, modifier = Modifier.size(width = 92.dp, height = 56.dp)) {
                    Text("+$ml")
                }
            }
            OutlinedButton(onClick = { showCustom = true }, modifier = Modifier.size(width = 92.dp, height = 56.dp)) {
                Text("Custom")
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onAction(UiAction.Undo) }) { Text("Undo last") }
            if (state.effectiveTotalMl >= cfg.goalMl) {
                OutlinedButton(onClick = { onAction(com.sippy.app.ui.UiAction.ReplayCelebration) }) { Text("Replay dance") }
            }
        }
        if (celebrate) {
            Spacer(Modifier.height(8.dp))
            Text("🎉 ${state.feedback.celebrateName ?: "Your friend"} reached the goal!",
                style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        }
        if (cfg.devUnlocked && cfg.debugOverlay) {
            Spacer(Modifier.height(16.dp))
            SectionTitle("Debug overlay")
            GlassCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Mood: ${state.mood.label}")
                    Text("Pace: ${(state.pace * 100).toInt()}% · Progress: ${(state.progress * 100).toInt()}%")
                    Text("Target: ${cfg.goalMl} ml · Today: ${state.totalMl} ml (shown ${state.effectiveTotalMl})")
                    Text("Active friend: ${friend?.name ?: "none"} · Points: ${friend?.growthPoints ?: 0}")
                    Text("Day: ${state.day} · Last event: ${state.feedback.lastEvent}")
                    Text("Animation: ${animationKind?.label ?: "idle"} · Simulate: ${state.dev.simulate}")
                }
            }
        }
        Spacer(Modifier.height(80.dp))
    }

    if (showCustom) {
        NumberDialog(
            title = "Custom amount", initial = "250", label = "Millilitres", min = 1, max = 5000,
            onDismiss = { showCustom = false },
            onConfirm = { onAction(UiAction.Log(it)); showCustom = false }
        )
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/screens/Onboarding.kt`

```kotlin
package com.sippy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sippy.app.domain.GrowthStage
import com.sippy.app.domain.Mood
import com.sippy.app.domain.Species
import com.sippy.app.ui.FriendCanvas
import com.sippy.app.ui.LocalAnimationsEnabled
import com.sippy.app.ui.UiAction

@Composable
fun SpeciesPicker(selected: Species, onSelect: (Species) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Species.entries.forEach { species ->
            val chosen = species == selected
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(112.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (chosen) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = { onSelect(species) }
            ) {
                Column(
                    Modifier.fillMaxSize().padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    FriendCanvas(
                        species = species, mood = Mood.HAPPY, stage = GrowthStage.YOUNG,
                        phase = 0.12f, size = 62.dp, animationsEnabled = LocalAnimationsEnabled.current,
                        description = species.label
                    )
                    Text(species.label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(onAction: (UiAction) -> Unit) {
    var species by remember { mutableStateOf(Species.DROPLET) }
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("2000") }
    val goalValue = goal.toIntOrNull()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Meet your first water friend", style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center)
        Text("Pick a friend, give it a name, and choose a daily target.",
            style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        SpeciesPicker(species) { species = it }
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(24) },
            label = { Text("Friend's name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it.filter { c -> c.isDigit() }.take(5) },
            label = { Text("Daily goal (ml)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Minimum 250 ml. You can change this any time in Settings.",
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val amount = goalValue?.coerceIn(250, 10000) ?: 2000
                onAction(UiAction.Onboard(name.ifBlank { "Sippy" }, species, amount))
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) { Text("Let's go") }
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/screens/Settings.kt`

```kotlin
package com.sippy.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sippy.app.BuildConfig
import com.sippy.app.domain.AmountFormat
import com.sippy.app.ui.NumberDialog
import com.sippy.app.ui.SectionTitle
import com.sippy.app.ui.UiAction
import com.sippy.app.ui.UiState
import com.sippy.app.updates.UpdateChecker
import com.sippy.app.ui.theme.Accents
import com.sippy.app.ui.theme.LocalFriendColors

@Composable
fun SettingsScreen(state: UiState, onAction: (UiAction) -> Unit, onNavigate: (String) -> Unit) {
    val cfg = state.settings ?: return
    val context = LocalContext.current
    val palette = LocalFriendColors.current
    var goalDialog by remember { mutableStateOf(false) }
    var intervalDialog by remember { mutableStateOf(false) }
    var wakeStartDialog by remember { mutableStateOf(false) }
    var wakeEndDialog by remember { mutableStateOf(false) }
    var apiKeyText by remember(cfg.openRouterKey) { mutableStateOf(cfg.openRouterKey) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) Toast.makeText(context, "Notifications are disabled", Toast.LENGTH_SHORT).show()
    }

    fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) return
        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        SectionTitle("Friends")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Manage friends (${state.friends.size})")
            TextButton(onClick = { onNavigate("friends") }) { Text("Open") }
        }

        SectionTitle("Daily goal")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(AmountFormat.display(cfg.goalMl, cfg.units))
            TextButton(onClick = { goalDialog = true }) { Text("Change") }
        }

        SectionTitle("Units")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ml", "oz").forEach { unit ->
                FilterChip(selected = cfg.units == unit, onClick = { onAction(UiAction.Units(unit)) },
                    label = { Text(if (unit == "ml") "Millilitres" else "US fl oz") })
            }
        }

        SectionTitle("Reminders")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Water reminders")
            Switch(checked = cfg.reminderEnabled, onCheckedChange = {
                if (it) ensureNotificationPermission()
                onAction(UiAction.Reminder(it))
            })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Every ${cfg.reminderIntervalMin} minutes")
            TextButton(onClick = { intervalDialog = true }) { Text("Change") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Waking hours ${cfg.wakeStartHour}:00–${cfg.wakeEndHour}:00")
            Row {
                TextButton(onClick = { wakeStartDialog = true }) { Text("Start") }
                TextButton(onClick = { wakeEndDialog = true }) { Text("End") }
            }
        }
        Text("Exact alarms are used when Android allows them; otherwise WorkManager schedules an inexact reminder.",
            style = MaterialTheme.typography.bodySmall)
        if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(context)) {
            TextButton(onClick = { ensureNotificationPermission() }) { Text("Grant notification permission") }
        }

        SectionTitle("Appearance")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("Reduce motion")
            Switch(checked = cfg.reduceMotion, onCheckedChange = { onAction(UiAction.ReduceMotion(it)) })
        }
        Text("Turns off bobbing, dancing and particle effects. The system animation setting is always respected too.",
            style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("system", "light", "dark").forEach { mode ->
                FilterChip(
                    selected = cfg.themeMode == mode,
                    onClick = { onAction(UiAction.Theme(mode)) },
                    label = { Text(mode.replaceFirstChar { it.uppercase() }) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Accents.names.forEachIndexed { index, accent ->
                val color = when (accent) {
                    "coral" -> Color(0xFFEF4444)
                    "violet" -> Color(0xFF7C3AED)
                    "mint" -> Color(0xFF059669)
                    else -> Color(0xFF0284C7)
                }
 androidx.compose.material3.IconButton(
                    onClick = { onAction(UiAction.Accent(accent)) },
                    modifier = Modifier.size(52.dp)
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(36.dp)) {
                        drawCircle(color, radius = size.minDimension / 2f)
                        if (cfg.accent == accent) drawCircle(
                            palette.ink, radius = size.minDimension / 5f
                        )
                    }
                }
                Text(Accents.labels[index], style = MaterialTheme.typography.labelSmall)
            }
        }

        SectionTitle("Smart features (OpenRouter)")
        OutlinedTextField(
            value = apiKeyText,
            onValueChange = { apiKeyText = it.trim() },
            label = { Text("OpenRouter API key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAction(UiAction.SaveApiKey(apiKeyText)) }) { Text("Save key") }
            TextButton(onClick = { onNavigate("coach") }) { Text("Open Coach") }
        }
        Text("Stored only on this device. Sent to OpenRouter only when you ask the coach a question.",
            style = MaterialTheme.typography.bodySmall)

        SectionTitle("Updates")
        Text("Installed: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onAction(UiAction.CheckUpdate(true)) },
                enabled = !state.update.busy
            ) { Text(if (state.update.busy) "Checking..." else "Check for updates") }
            state.update.info?.apkUrl?.takeIf { state.update.available }?.let { url ->
                Button(onClick = { UpdateChecker.open(context, url) }) { Text("Download update") }
            }
        }
        state.update.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Text("Sippy checks for a new release once per day. Nothing is installed automatically.",
            style = MaterialTheme.typography.bodySmall)

        SectionTitle("About")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text("About Sippy")
            TextButton(onClick = { onNavigate("about") }) { Text("Open") }
        }
        if (cfg.devUnlocked) {
            SectionTitle("Developer options")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Testing tools")
                TextButton(onClick = { onNavigate("dev") }) { Text("Open") }
            }
        }
        Spacer(Modifier.height(80.dp))
    }

    if (goalDialog) NumberDialog("Daily goal", cfg.goalMl.toString(), "Millilitres", 250, 10000,
        { goalDialog = false }, { onAction(UiAction.Goal(it)); goalDialog = false })
    if (intervalDialog) NumberDialog("Reminder interval", cfg.reminderIntervalMin.toString(),
        "Minutes (15–720)", 15, 720, { intervalDialog = false },
        { onAction(UiAction.Interval(it)); intervalDialog = false })
    if (wakeStartDialog) NumberDialog("Waking hours start", cfg.wakeStartHour.toString(),
        "Hour (0–22)", 0, 22, { wakeStartDialog = false },
        { onAction(UiAction.WakeHours(it, cfg.wakeEndHour)); wakeStartDialog = false })
    if (wakeEndDialog) NumberDialog("Waking hours end", cfg.wakeEndHour.toString(),
        "Hour (1–24)", 1, 24, { wakeEndDialog = false },
        { onAction(UiAction.WakeHours(cfg.wakeStartHour, it)); wakeEndDialog = false })
}

private fun hasNotificationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

@Composable
fun AboutScreen(onAction: (UiAction) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    var taps by remember { mutableStateOf(0) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("About Sippy", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Sippy helps you drink water with little friends who live on your home screen. "
                + "All of your data stays on this device.")
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Version 1.0.0 (1)", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = {
                    taps++
                    if (taps == 7) {
                        onAction(UiAction.UnlockDev)
                        Toast.makeText(context, "Developer options unlocked", Toast.LENGTH_SHORT).show()
                    } else if (taps in 2..6) {
                        Toast.makeText(context, "${7 - taps} more taps", Toast.LENGTH_SHORT).show()
                    }
                }, modifier = Modifier.height(48.dp)) { Text("Tap version 7 times for developer options") }
            }
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("Back") }
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/theme/Glass.kt`

```kotlin
package com.sippy.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Frosted surface built from gradients: no bitmap, no external asset, works on API 26+. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Int = 24,
    alpha: Float = 0.16f,
    content: @Composable BoxScope.() -> Unit
) {
    val palette = LocalFriendColors.current
    val shape = RoundedCornerShape(corner.dp)
    val fill = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = alpha + 0.12f),
            Color.White.copy(alpha = alpha * 0.6f),
            palette.glow.copy(alpha = alpha * 0.7f)
        )
    )
    val stroke = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.75f),
            palette.glow.copy(alpha = 0.5f),
            Color.White.copy(alpha = 0.12f)
        )
    )
    val glint = remember {
        Brush.linearGradient(
            listOf(Color.Transparent, Color.White.copy(alpha = 0.5f), Color.Transparent)
        )
    }
    Box(
        modifier
            .shadow(14.dp, shape, ambientColor = palette.primary.copy(alpha = 0.25f))
            .clip(shape)
            .background(fill)
            .border(1.dp, stroke, shape)
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawLine(
                brush = glint,
                start = Offset(size.width * 0.12f, 1f),
                end = Offset(size.width * 0.88f, 1f),
                strokeWidth = 1.5f
            )
        }
        content()
    }
}

/** Soft ambient field behind screens, inspired by the orbital reference. */
@Composable
fun AppBackdrop(modifier: Modifier = Modifier, animationsEnabled: Boolean = true) {
    val palette = LocalFriendColors.current
    val orb = Brush.radialGradient(
        listOf(palette.glow.copy(alpha = 0.32f), Color.Transparent)
    )
    val deep = Brush.radialGradient(
        listOf(palette.primary.copy(alpha = 0.18f), Color.Transparent)
    )
    Canvas(modifier.fillMaxSize()) {
        drawRect(brush = deep)
        drawCircle(brush = orb, radius = size.minDimension * 0.75f,
            center = Offset(size.width * 0.82f, size.height * 0.14f))
        drawCircle(brush = orb, radius = size.minDimension * 0.6f,
            center = Offset(size.width * 0.08f, size.height * 0.82f))
    }
}
```

---

## `app/src/main/java/com/sippy/app/ui/theme/Theme.kt`

```kotlin
package com.sippy.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

data class FriendColors(
    val primary: Color,
    val secondary: Color,
    val light: Color,
    val ink: Color,
    val glow: Color
)

val AquaColors = FriendColors(
    Color(0xFF0284C7), Color(0xFF38BDF8), Color(0xFFE0F2FE), Color(0xFF0C4A6E), Color(0xFF7DD3FC)
)
val CoralColors = FriendColors(
    Color(0xFFEF4444), Color(0xFFFB923C), Color(0xFFFFEDD5), Color(0xFF7F1D1D), Color(0xFFFCA5A5)
)
val VioletColors = FriendColors(
    Color(0xFF7C3AED), Color(0xFFC084FC), Color(0xFFEDE9FE), Color(0xFF4C1D95), Color(0xFFD8B4FE)
)
val MintColors = FriendColors(
    Color(0xFF059669), Color(0xFF34D399), Color(0xFFD1FAE5), Color(0xFF064E3B), Color(0xFF6EE7B7)
)

val LocalFriendColors = compositionLocalOf { AquaColors }

object Accents {
    val names = listOf("aqua", "coral", "violet", "mint")
    val labels = listOf("Aqua", "Coral", "Violet", "Mint")
    fun colors(name: String) = when (name) {
        "coral" -> CoralColors
        "violet" -> VioletColors
        "mint" -> MintColors
        else -> AquaColors
    }
    fun scheme(name: String, dark: Boolean): ColorScheme {
        val c = colors(name)
        return if (dark) darkColorScheme(
            primary = c.secondary,
            onPrimary = Color(0xFF0B1220),
            primaryContainer = c.primary.copy(alpha = 0.55f),
            onPrimaryContainer = Color.White,
            secondary = c.glow,
            tertiary = c.light,
            background = Color(0xFF0B1220),
            surface = Color(0xFF111C2E),
            surfaceVariant = Color(0xFF1D2A3D),
            onSurface = Color(0xFFE8F1FA)
        ) else lightColorScheme(
            primary = c.primary,
            onPrimary = Color.White,
            primaryContainer = c.light,
            onPrimaryContainer = c.ink,
            secondary = c.secondary,
            tertiary = c.glow,
            background = Color(0xFFF7FBFF),
            surface = Color.White,
            surfaceVariant = c.light,
            onSurface = Color(0xFF10243A)
        )
    }
}

@Composable
fun SippyTheme(mode: String = "system", accent: String = "aqua", content: @Composable () -> Unit) {
    val dark = when (mode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalFriendColors provides Accents.colors(accent)) {
        MaterialTheme(colorScheme = Accents.scheme(accent, dark), content = content)
    }
}
```

---

## `app/src/main/java/com/sippy/app/updates/UpdateChecker.kt`

```kotlin
package com.sippy.app.updates

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val version: String, val apkUrl: String?, val notes: String)

/**
 * Checks the repository's latest GitHub release. The app never installs anything by itself:
 * checking is automatic (once per day), downloading/installing is an explicit user action.
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
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val version = json.optString("tag_name", "").removePrefix("v").trim()
            if (version.isBlank()) error("Release has no version tag")
            var apkUrl: String? = null
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.optString("name", "").endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url").ifBlank { null }
                        break
                    }
                }
            }
            UpdateInfo(version, apkUrl, json.optString("body", ""))
        } finally {
            connection.disconnect()
        }
    }

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
```

---

## `app/src/main/res/drawable/ic_coach.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M12,2 l1.8,6.2 L20,10 l-6.2,1.8 L12,18 l-1.8,-6.2 L4,10 l6.2,-1.8 Z M19,16 l0.9,2.1 L22,19 l-2.1,0.9 L19,22 l-0.9,-2.1 L16,19 l2.1,-0.9 Z"/>
</vector>
```

---

## `app/src/main/res/drawable/ic_delete.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFF44336" android:pathData="M6,19 c0,1.1 0.9,2 2,2 h8 c1.1,0 2,-0.9 2,-2 V7 H6 v12 Z M19,4 h-3.5 l-1,-1 h-5 l-1,1 H5 v2 h14 V4 Z"/>
</vector>
```

---

## `app/src/main/res/drawable/ic_friends.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M8,8 a3,3 0 1,0 0,-6 a3,3 0 1,0 0,6 Z M16,9 a2.5,2.5 0 1,0 0,-5 a2.5,2.5 0 1,0 0,5 Z M8,10 c-4,0 -6,2 -6,5 v3 h12 v-3 c0,-3 -2,-5 -6,-5 Z M16,11 c-0.5,0 -1,0 -1.5,0.1 c1.5,1 2.5,2.7 2.5,4.9 v3 h5 v-3 c0,-3 -2.5,-5 -6,-5 Z"/>
</vector>
```

---

## `app/src/main/res/drawable/ic_history.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M13,3 a9,9 0 1,0 8,14 l-2,-2 a6.5,6.5 0 1,1 -6,-10 Z M12,7 v5 l4,2"/>
</vector>
```

---

## `app/src/main/res/drawable/ic_home.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M12,3 L2,12 h3 v9 h6 v-6 h2 v6 h6 v-9 h3 Z"/>
</vector>
```

---

## `app/src/main/res/drawable/ic_launcher_foreground.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:width="72dp" android:height="72dp" android:gravity="center">
        <bitmap android:src="@drawable/app_icon_cute" android:gravity="center" />
    </item>
</layer-list>
```

---

## `app/src/main/res/drawable/ic_more.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M12,8 m-1.5,0 a1.5,1.5 0 1,0 3,0 a1.5,1.5 0 1,0 -3,0 M12,12 m-1.5,0 a1.5,1.5 0 1,0 3,0 a1.5,1.5 0 1,0 -3,0 M12,16 m-1.5,0 a1.5,1.5 0 1,0 3,0 a1.5,1.5 0 1,0 -3,0"/>
</vector>
```

---

## `app/src/main/res/drawable/ic_notification.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M12,2 C12,2 5,11 5,16 C5,20 8,23 12,23 C16,23 19,20 19,16 C19,11 12,2 12,2 Z"/>
</vector>
```

---

## `app/src/main/res/drawable/ic_settings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path android:fillColor="#FFFFFFFF" android:pathData="M12,15.5 a3.5,3.5 0 1,0 0,-7 a3.5,3.5 0 1,0 0,7 Z M21,12 l-2,-1.2 l0,-2.2 l-2.2,-1.3 l-1.3,-2.2 l-2.2,0 L12,3 l-1.3,2.1 l-2.2,0 L7.2,6.4 L5,7.7 l0,2.2 L3,12 l2,1.2 l0,2.2 l2.2,1.3 l1.3,2.2 l2.2,0 L12,21 l1.3,-2.1 l2.2,0 l1.3,-2.2 l2.2,-1.3 l0,-2.2 Z"/>
</vector>
```

---

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

---

## `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

---

## `app/src/main/res/values/ic_launcher_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources><color name="ic_launcher_background">#FF0284C7</color></resources>
```

---

## `app/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Sippy</string>
    <string name="notification_channel">Water reminders</string>
    <string name="notification_title">Time for a sip, %1$s!</string>
    <string name="notification_text">%1$s is waiting for you. Log a quick 250 ml.</string>
    <string name="notification_test">Test reminder from %1$s</string>
    <string name="notification_action">Log 250 ml</string>
</resources>
```

---

## `app/src/main/res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="SippyTheme" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="android:windowBackground">@color/sippy_background</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
    </style>
    <color name="sippy_background">#FFF7FBFF</color>
</resources>
```

---

## `app/src/main/res/xml/backup_rules.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="database" path="sippy.db"/>
</full-backup-content>
```

---

## `app/src/main/res/xml/data_extraction_rules.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="database" path="sippy.db"/>
    </cloud-backup>
    <device-transfer>
        <exclude domain="database" path="sippy.db"/>
    </device-transfer>
</data-extraction-rules>
```

---

## `app/src/test/java/com/sippy/app/domain/LogicTest.kt`

```kotlin
package com.sippy.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class LogicTest {
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    private fun at(hour: Int, minute: Int = 0): Long {
        val cal = java.util.Calendar.getInstance(utc)
        cal.set(2026, java.util.Calendar.JUNE, 15, hour, minute, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    @Test
    fun sleepyBeforeWakeWithNoDrinks() {
        assertEquals(Mood.SLEEPY, MoodCalculator.mood(0, 2000, at(6), utc))
    }

    @Test
    fun thirstyWhenBehindPace() {
        // 18:00 is ~79% of a 07:00-22:00 window; 20% progress is far behind.
        assertEquals(Mood.THIRSTY, MoodCalculator.mood(400, 2000, at(18), utc))
    }

    @Test
    fun contentWhenOnPace() {
        // 13:00 is 40% of the window; 800/2000 is exactly 40%.
        assertEquals(Mood.CONTENT, MoodCalculator.mood(800, 2000, at(13), utc))
    }

    @Test
    fun happyWhenAheadOfPace() {
        assertEquals(Mood.HAPPY, MoodCalculator.mood(1200, 2000, at(10), utc))
    }

    @Test
    fun dancingWhenGoalReached() {
        assertEquals(Mood.DANCING, MoodCalculator.mood(2000, 2000, at(9), utc))
    }

    @Test
    fun streakExtendsOnlyFromConsecutiveDays() {
        assertEquals(Pair(4, 5), StreakLogic.afterDay(true, true, 3, 5))
        assertEquals(Pair(1, 5), StreakLogic.afterDay(true, false, 3, 5))
        assertEquals(Pair(0, 5), StreakLogic.afterDay(false, true, 3, 5))
    }

    @Test
    fun rolloverBreaksStreakAfterMissedDay() {
        assertEquals(Pair(0, 7), StreakLogic.onRollover(false, 5, 7))
        assertEquals(Pair(5, 7), StreakLogic.onRollover(true, 5, 7))
    }

    @Test
    fun dayKeysRollOverAndShiftAcrossMonthBoundary() {
        val day = DayKeys.key(at(23, 59), utc)
        assertEquals("2026-06-15", day)
        assertEquals("2026-06-16", DayKeys.shift(day, 1))
        assertEquals("2026-07-01", DayKeys.shift("2026-06-30", 1))
    }

    @Test
    fun growthStageThresholds() {
        assertEquals(GrowthStage.BABY, stageFor(0))
        assertEquals(GrowthStage.BABY, stageFor(249))
        assertEquals(GrowthStage.YOUNG, stageFor(250))
        assertEquals(GrowthStage.GROWN, stageFor(1000))
        assertEquals(GrowthStage.ELDER, stageFor(3000))
        assertEquals(GrowthStage.ELDER, stageFor(99999))
    }

    @Test
    fun friendLimitAndLastFriendProtection() {
        assertTrue(Rules.canAddFriend(7))
        assertFalse(Rules.canAddFriend(8))
        assertTrue(Rules.canDeleteFriend(2))
        assertFalse(Rules.canDeleteFriend(1))
    }

    @Test
    fun pointsIncludeOncePerDayTargetBonus() {
        assertEquals(250, Rules.pointsFor(250, 250, 2000, false))
        assertEquals(500, Rules.pointsFor(250, 2000, 2000, false))
        assertEquals(250, Rules.pointsFor(250, 2250, 2000, true))
    }

    @Test
    fun goalIsClamped() {
        assertEquals(250, Rules.clampGoal(1))
        assertEquals(10000, Rules.clampGoal(50000))
        assertEquals(2000, Rules.clampGoal(2000))
    }
}
```

---

## `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

    <application
        android:name=".SippyApp"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/SippyTheme">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:windowSoftInputMode="adjustResize"
            android:theme="@style/SippyTheme">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver android:name=".notifications.ReminderReceiver" android:exported="false" />
        <receiver android:name=".notifications.BootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
                <action android:name="android.intent.action.TIME_SET" />
            </intent-filter>
        </receiver>

        <provider
            android:name="androidx.startup.InitializationProvider"
            android:authorities="${applicationId}.androidx-startup"
            android:exported="false"
            tools:node="merge"
            xmlns:tools="http://schemas.android.com/tools">
            <meta-data
                android:name="androidx.work.WorkManagerInitializer"
                tools:node="remove" />
        </provider>
    </application>
</manifest>
```

---

## `.github/workflows/android-ci.yml`

```yaml
name: Android CI

on:
  push:
    branches:
      - 'arena/**'
    paths-ignore:
      - 'releases/**'
      - 'ALL_FILES.md'
      - '.github/workflows/deploy-pages.yml'
  workflow_dispatch:

permissions:
  contents: write
  issues: write

jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 45
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Prepare Android SDK 35
        run: |
          echo "ANDROID_HOME=$ANDROID_HOME"
          export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
          yes | sdkmanager --licenses > /dev/null 2>&1 || true
          sdkmanager "platforms;android-35" "build-tools;35.0.0"
          sdkmanager --list_installed | head -30
      - uses: gradle/actions/setup-gradle@v4
      - name: Make Gradle wrapper executable
        run: chmod +x gradlew
      - name: Unit tests and builds
        run: |
          set -o pipefail
          ./gradlew --no-daemon --stacktrace testDebugUnitTest assembleDebug bundleRelease 2>&1 | tee build-ci.log
      - name: Report build failure
        if: failure()
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          {
            echo 'Build log:'
            echo '```'
            tail -c 60000 build-ci.log
            echo '```'
          } > issue-body.md
          gh issue create --repo "$GITHUB_REPOSITORY" \
            --title "CI build failure run $GITHUB_RUN_ID" \
            --body-file issue-body.md
      - name: Publish APK to releases folder
        if: success()
        run: |
          mkdir -p releases
          cp app/build/outputs/apk/debug/app-debug.apk releases/sippy-debug.apk
          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
          git checkout -B "$GITHUB_REF_NAME"
          git add -f releases/sippy-debug.apk
          if ! git diff --cached --quiet; then
            git commit -m "ci: publish debug APK to releases/"
            git push origin HEAD:"$GITHUB_REF_NAME"
          fi
      - name: Publish GitHub release with APK
        if: success()
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          if ! gh release view 1.2.0 --repo "$GITHUB_REPOSITORY" > /dev/null 2>&1; then
            gh release create 1.2.0 releases/sippy-debug.apk --repo "$GITHUB_REPOSITORY" \
              --target "$GITHUB_REF_NAME" \
              --title "Sippy 1.2.0" \
              --notes "CI-built debug APK for Android 8.0+ (API 26). Settings -> Check for updates reads this release."
          fi
      - name: Upload APK
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: sippy-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: ignore
      - name: Upload AAB
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: sippy-release-aab
          path: app/build/outputs/bundle/release/*.aab
          if-no-files-found: ignore
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: sippy-test-reports
          path: app/build/reports/tests/
          if-no-files-found: ignore
```

---

## `.github/workflows/android-build.yml`

```yaml
name: Android Build

on:
  push:
    branches:
      - 'arena/**'
  workflow_dispatch:

permissions:
  contents: write
  issues: write

jobs:
  build:
    if: ${{ !contains(github.event.head_commit.message, '[skip ci]') }}
    runs-on: ubuntu-latest
    timeout-minutes: 45
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Prepare Android SDK 35
        run: |
          echo "ANDROID_HOME=$ANDROID_HOME"
          export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
          yes | sdkmanager --licenses > /dev/null 2>&1 || true
          sdkmanager "platforms;android-35" "build-tools;35.0.0"
      - uses: gradle/actions/setup-gradle@v4
      - name: Make Gradle wrapper executable
        run: chmod +x gradlew
      - name: Unit tests and builds
        run: |
          set -o pipefail
          ./gradlew --no-daemon --stacktrace testDebugUnitTest assembleDebug bundleRelease 2>&1 | tee build-ci.log
      - name: Report build failure
        if: failure()
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          {
            echo 'Build log:'
            echo '```'
            tail -c 60000 build-ci.log
            echo '```'
          } > issue-body.md
          gh issue create --repo "$GITHUB_REPOSITORY" \
            --title "Android Build failure run $GITHUB_RUN_ID" \
            --body-file issue-body.md
      - name: Publish APK to releases folder
        if: success()
        run: |
          mkdir -p releases
          cp app/build/outputs/apk/debug/app-debug.apk releases/sippy-debug.apk
          git config user.name "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
          git checkout -B "$GITHUB_REF_NAME"
          git add -f releases/sippy-debug.apk
          if ! git diff --cached --quiet; then
            git commit -m "ci: publish debug APK to releases/"
            git push origin HEAD:"$GITHUB_REF_NAME"
          fi
      - name: Publish GitHub release with APK
        if: success()
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          if ! gh release view 1.2.0 --repo "$GITHUB_REPOSITORY" > /dev/null 2>&1; then
            gh release create 1.2.0 releases/sippy-debug.apk --repo "$GITHUB_REPOSITORY" \
              --target "$GITHUB_REF_NAME" \
              --title "Sippy 1.2.0" \
              --notes "CI-built debug APK for Android 8.0+ (API 26). Settings -> Check for updates reads this release."
          fi
      - name: Upload APK
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: sippy-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: ignore
      - name: Upload AAB
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: sippy-release-aab
          path: app/build/outputs/bundle/release/*.aab
          if-no-files-found: ignore
      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: sippy-test-reports
          path: app/build/reports/tests/
          if-no-files-found: ignore
```

---

## `.github/workflows/deploy-pages.yml`

```yaml
name: Deploy website

on:
  push:
    paths:
      - 'docs/**'
      - '.github/workflows/deploy-pages.yml'
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: pages
  cancel-in-progress: true

jobs:
  package:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/configure-pages@v5
      - uses: actions/upload-pages-artifact@v5
        with:
          path: docs

  deploy:
    needs: package
    runs-on: ubuntu-latest
    steps:
      # GitHub's artifact metadata API can lag a second after upload. Wait, then retry once.
      - name: Wait for artifact metadata
        run: sleep 20
      - id: first
        uses: actions/deploy-pages@v5
        continue-on-error: true
      - name: Wait before retry
        if: steps.first.outcome == 'failure'
        run: sleep 45
      - id: retry
        if: steps.first.outcome == 'failure'
        uses: actions/deploy-pages@v5
      - name: Fail if both attempts failed
        if: steps.first.outcome == 'failure' && steps.retry.outcome == 'failure'
        run: exit 1
```

---

## `.github/workflows/release.yml`

```yaml
name: Release bundle

on:
  workflow_dispatch:
  push:
    tags:
      - 'v*'

jobs:
  bundle:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
      - name: Decode signing keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.SIPPY_KEYSTORE_BASE64 }}
          KEYSTORE_PASSWORD: ${{ secrets.SIPPY_KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.SIPPY_KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.SIPPY_KEY_PASSWORD }}
        run: |
          if [ -n "$KEYSTORE_BASE64" ]; then
            echo "$KEYSTORE_BASE64" | base64 -d > app/release.jks
            cat > keystore.properties <<PROPS
          storeFile=app/release.jks
          storePassword=$KEYSTORE_PASSWORD
          keyAlias=$KEY_ALIAS
          keyPassword=$KEY_PASSWORD
          PROPS
          fi
      - name: Grant Gradle wrapper
        run: chmod +x gradlew
      - name: Unit tests
        run: ./gradlew testDebugUnitTest
      - name: Build signed release bundle
        run: ./gradlew bundleRelease
      - uses: actions/upload-artifact@v4
        with:
          name: sippy-release-aab
          path: app/build/outputs/bundle/release/*.aab
```

---

## `README.md`

```markdown
# Sippy

**Distribution:** this project ships as a sideloaded Android APK from a GitHub Pages site (`docs/`) and GitHub Releases. The Play Store guide in `docs/RELEASE_GUIDE.md` is kept for reference only.

An offline-first water tracking app with animated water friends, built with Kotlin, Jetpack Compose (Material 3), Room, Preferences DataStore, WorkManager and manual dependency injection.

## Project structure

```
Sippy/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/libs.versions.toml
├── gradle/wrapper/{gradle-wrapper.jar,gradle-wrapper.properties}
├── gradlew, gradlew.bat
├── keystore.properties            # created locally, git-ignored
├── .github/workflows/release.yml
├── docs/
│   ├── PRIVACY_POLICY.md
│   └── RELEASE_GUIDE.md
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/
        │   │   ├── drawable/ (ic_launcher_foreground, ic_notification, ic_more, ic_delete, nav icons)
        │   │   ├── mipmap-anydpi-v26/{ic_launcher.xml,ic_launcher_round.xml}
        │   │   ├── values/{strings.xml,themes.xml,ic_launcher_background.xml}
        │   │   └── xml/{backup_rules.xml,data_extraction_rules.xml}
        │   └── java/com/sippy/app/
        │       ├── SippyApp.kt, MainActivity.kt
        │       ├── data/{Entities.kt,Preferences.kt,Repository.kt}
        │       ├── domain/Logic.kt
        │       ├── di/AppContainer.kt
        │       ├── notifications/{Notifier.kt,ReminderWorker.kt,ReminderScheduler.kt,ReminderReceiver.kt,BootReceiver.kt}
        │       └── ui/{AppViewModel.kt,Navigation.kt,Common.kt,FriendArt.kt}
        │           ├── theme/Theme.kt
        │           └── screens/{Onboarding.kt,Home.kt,Friends.kt,History.kt,Settings.kt,DevOptions.kt}
        └── test/java/com/sippy/app/domain/LogicTest.kt
```

## Design

The UI uses a reusable frosted-glass surface (`ui/theme/Glass.kt`) and an ambient orbital backdrop. Settings has a **Reduce motion** toggle that combines with the Android animation setting. Pets stay 2D Canvas art with gradient shading, extra expressions and smoother easing.

## Website

`docs/` is the GitHub Pages source (enable Pages → Deploy from a branch → current branch → `/docs`). It has a animated intro, floating particles and a download button pointing at the release APK.

## Build and run

Requirements: Android Studio Ladybug or newer (or JDK 17 + Android SDK Platform 35), Android device/emulator on API 26+.

1. Open the folder in Android Studio, let Gradle sync, then Run → app. Or from a terminal:

```bash
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

2. Tests: `./gradlew testDebugUnitTest` (reports: `app/build/reports/tests/testDebugUnitTest/index.html`).
3. Lint: `./gradlew lintDebug`.

## Icons

The launcher icon is a vector drawable plus an adaptive icon XML in `app/src/main/res`; no binary assets are needed. To generate PNG densities from the vector in Android Studio: right-click `ic_launcher_foreground.xml` → Generate → Image Asset, or use `mipmap` density folders produced by the Asset Studio wizard. For Play, export a 512×512 PNG from the same vector (e.g. Android Studio's Image Asset preview or a vector → PNG converter).

## Updates

Settings has a **Check for updates** button. The app also checks the latest GitHub release once per day using `https://api.github.com/repos/Cooreo/Sippy/releases/latest`. If a newer tag exists, a **Download update** button opens the release APK in the browser. Nothing is downloaded or installed automatically. Built APKs are kept in `releases/`.

## Developer options

Settings → About → tap "Version 1.0.0 (1)" seven times. The unlocked flag is stored in DataStore. Developer options include mood/animation previews, growth and streak editors, progress simulation (never writes), time override (preview only), test friends, fake history, a test notification, debug overlay, and a lock button.

## Privacy

All data stays on device; see [`docs/PRIVACY_POLICY.md`](docs/PRIVACY_POLICY.md).
```

---

## `docs/PRIVACY_POLICY.md`

```markdown
# Sippy Privacy Policy

Last updated: 26 November 2026

Sippy is an offline-first water tracking app. We do not operate servers for the app and we do not collect personal data.

**What the app stores.** On your device only: your friends' names and species, growth points, water entries, daily goal, streaks, reminder preferences, theme and developer-tool settings. These are stored in a local SQLite database (Room) and Preferences DataStore.

**What we do not do.** We do not upload data, show ads, use analytics or crash-reporting services, sell data, or share data with third parties. Notification reminders and the "Log 250 ml" action work entirely on the device.

**Permissions.** POST_NOTIFICATIONS (Android 13+) is used only to show water reminders. Exact alarm scheduling (Android 12+) is used only if you enable reminders and your device allows it. No other sensitive permission is requested.

**Backup.** Cloud backup of the app database is excluded; data stays on the device.

**Your choices.** You can delete individual entries in History, delete friends in Friends, or use Settings → Developer options → Clear all data to erase everything stored by the app. Uninstalling the app also removes its data.

**Children.** The app is not directed at children under 13 and we do not knowingly collect data from them.

**Optional AI coach.** If you add your own OpenRouter API key in Settings, the app stores that key on your device. When you explicitly ask the coach a question, the app sends your question plus basic local hydration statistics (friend name, today's intake, goal, streak, mood and 7-day average) directly to OpenRouter to generate a reply. Without a key, no AI request is made. Local insights never leave the device.

**Contact.** For privacy questions, contact the developer through the Google Play Store listing's developer contact email.

This policy may be updated; the current version is always the one published with the app listing.
```

---

## `docs/RELEASE_GUIDE.md`

```markdown
# Shipping Sippy to Google Play

1. **Finalise identity and version.** Choose the final applicationId (e.g. `com.yourcompany.sippy`) in `app/build.gradle.kts` (`namespace` must match). Set `versionCode` (start at 1, increment for every upload) and `versionName` (e.g. `1.0.0`). Never reuse a versionCode.

2. **Create an upload keystore.**

```bash
keytool -genkeypair -v -keystore ~/sippy-upload.jks -keyalg RSA -keysize 2048 -validity 10000 -alias sippy-upload
```

Create `keystore.properties` in the repository root (git-ignored):

```
storeFile=/absolute/path/to/sippy-upload.jks
storePassword=YOUR_PASSWORD
keyAlias=sippy-upload
keyPassword=YOUR_PASSWORD
```

Gradle already reads this file in `app/build.gradle.kts` and signs `release`. Back up the keystore and passwords outside the repository: Play App Signing cannot recover a lost upload key.

3. **R8 and resource shrinking.** `app/build.gradle.kts` sets `isMinifyEnabled = true` and `isShrinkResources = true` for release, with `app/proguard-rules.pro` keeping Room entities/daos/database subclasses and Hilt/Dagger classes (useful if you migrate DI later). Run `./gradlew :app:dependencies` and test the release variant on a device before upload.

4. **Build and verify the bundle.**

```bash
./gradlew clean testDebugUnitTest bundleRelease
ls -lh app/build/outputs/bundle/release/app-release.aab
jarsigner -verify -verbose app/build/outputs/bundle/release/app-release.aab   # verifies signature metadata
```

Install the release APK variant locally (`./gradlew assembleRelease`) and smoke-test onboarding, logging, reminders, history and restore-after-restart.

5. **Play Console setup.** Create a personal or organisation developer account and pay the one-time fee; add a payment profile if the app will ever be paid. Create app → default language English (US) → Free. Store listing draft:

   - **Title:** Sippy — Water Friend Tracker
   - **Short description (80 chars):** "Track water with tiny friends who cheer every sip."
   - **Full description:** "Sippy turns hydration into a tiny daily ritual. Pick a Droplet, Sprout, Kitty or Blobby, name it, and watch it grow as you log water. Big animated reactions, a daily goal ring, streaks, seven- and thirty-day history, and friendly reminders make it easy to keep going. Everything is stored on your device — no account, no ads, no tracking. Choose your own goal, units, reminder interval, waking hours and accent theme. Friends gain growth points and evolve through Baby, Young, Grown and Elder stages. Reach your goal and they dance!"
   - **Screenshots:** at least 2, up to 8 per device type, PNG/JPEG, 320–3840 px, 16:9 for phone; capture Home (friend + ring), Friends grid, History chart, Settings. Feature graphic: **1024 × 500 px** JPG/PNG (make it from the vector icon plus a short tagline). App icon: 512×512 PNG 32-bit.
   - **Content rating:** complete the questionnaire (no violence, no user-generated content, no ads; "Family" / Everyone is the likely result).
   - **Data safety:** answer that **no data is collected and no data is shared**. On-device data (friends, logs, settings) is stored only locally and never transmitted. Declare no encryption in transit (nothing is transmitted). Data deletion is in-app via Settings → Developer options → Clear all data (after unlocking) and by uninstalling; for a production build consider adding a visible Settings entry for deletion.
   - **Target audience:** not directed at children; select "18+" or an adult audience if asked, or "Families" with no child-directed content. Do not mark as "Designed for families" unless you complete the families policy requirements.
   - **Privacy policy:** host `docs/PRIVACY_POLICY.md` at a public HTTPS URL and paste it into the listing.

6. **App signing and testing.** In Play Console → App signing, enrol in Play App Signing (generate/upload the app signing key with `pepk` or let Google generate it). Create Internal testing, upload the `.aab`, add tester email lists, and install via the Play Store test link. Then create a Closed testing track: **new personal developer accounts must run a closed test with at least 12 testers continuously enrolled for 14 days** before they can request production access (requirements can change — check Play Console). Once approved, promote the same release to Production with a staged rollout.

7. **Pre-launch checklist.**

   - Test on API 26, 28, 33 and 35 devices/emulator images, including dark mode, system font scaling and "Remove animations".
   - Grant/deny POST_NOTIFICATIONS; verify exact-alarm fallback and the "Log 250 ml" notification action.
   - Verify onboarding, one-friend flow, eight-friend limit, last-friend protection, midnight/time-zone rollover, process death (Don't keep activities), and backup/restore.
   - Check that private data is not logged, permissions match the manifest, screenshots are honest, and the privacy URL resolves.
   - Upload a fresh build after every versionCode bump; never reuse a versionCode.

   Common rejections: misleading screenshots or descriptions, undeclared permissions/SDKs, broken notification permission flow, crashes on older Android, missing privacy policy, child-directed design without a Families declaration, and a private or unreachable privacy policy URL.

8. **Optional GitHub Actions.** `.github/workflows/release.yml` decodes a base64 keystore from repository secrets (`SIPPY_KEYSTORE_BASE64`, `SIPPY_KEYSTORE_PASSWORD`, `SIPPY_KEY_ALIAS`, `SIPPY_KEY_PASSWORD`), runs unit tests, builds `bundleRelease` and uploads the AAB as an artifact on tag `v*` or manual dispatch. Create those secrets in Settings → Secrets and variables → Actions.
```

---

## `docs/index.html`

```html
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="theme-color" content="#0b0716">
<title>Sippy — hydrate in your orbit</title>
<meta name="description" content="A local-first water tracker with animated friends. Download the Android APK.">
<link rel="stylesheet" href="styles.css">
</head>
<body>
<div class="intro" id="intro" aria-hidden="true">
  <div class="intro-inner">
    <div class="intro-line i1">WELCOME TO</div>
    <div class="intro-line i2">SIPPY'S</div>
    <div class="intro-line i3">ORBIT</div>
    <div class="intro-orbit"></div>
  </div>
</div>
<canvas id="stars" aria-hidden="true"></canvas>

<header class="topbar">
  <a class="brand" href="#top"><img src="app-icon.png" alt=""> SIPPY</a>
  <nav><a href="#why">Why</a><a href="#friends">Friends</a><a href="#download">Download</a>
    <a class="pill" href="#download">GET APK ↓</a></nav>
</header>

<main id="top">
<section class="hero">
  <div class="hero-copy">
    <p class="eyebrow">LOCAL-FIRST · ANDROID 8+ · NO ACCOUNT</p>
    <h1><span>HYDRATE</span><span class="script">in your</span><span class="outline">ORBIT.</span></h1>
    <p class="lede">Little water friends who blink, bounce, giggle and dance when you hit your goal. Your data stays on your device. Your key, your call.</p>
    <div class="cta-row">
      <a class="big-button" data-download href="https://github.com/Cooreo/Sippy/releases/download/1.2.0/sippy-debug.apk">DOWNLOAD SIPPY <span data-version>1.2.0</span> <b>↓</b></a>
      <a class="text-button" href="#friends">Meet the crew →</a>
    </div>
  </div>

  <div class="universe" aria-label="Sippy droplet friend inside orbital rings">
    <div class="orbit orbit-a"></div><div class="orbit orbit-b"></div><div class="orbit orbit-c"></div>
    <div class="planet"><img src="app-icon.png" alt="Sippy water droplet friend"></div>
    <div class="glass-float f1"><small>WE GROW</small><span>Every sip counts.</span></div>
    <div class="glass-float f2"><small>WE CELEBRATE</small><span>Goal hit? Dance time.</span></div>
    <div class="glass-float f3"><small>WE STAY LOCAL</small><span>No cloud. No ads.</span></div>
    <div class="dust d1"></div><div class="dust d2"></div><div class="dust d3"></div>
  </div>
</section>

<div class="marquee"><div class="marquee-track">ONE MORE SIP · ONE MORE SIP · ONE MORE SIP · ONE MORE SIP · ONE MORE SIP ·&nbsp;</div></div>

<section class="dark" id="why">
  <div class="section-head"><span>01 / WHY</span><h2>You always know<br><em>where things stand.</em></h2></div>
  <div class="cards">
    <article class="glass"><h3>We notice</h3><p>Moods follow your pace, your streak and the time of day. Blobby knows when you're behind.</p></article>
    <article class="glass"><h3>We play</h3><p>Four friends, four personalities. Blink, bob, giggle, dance — all drawn live in code.</p></article>
    <article class="glass"><h3>We remember</h3><p>Growth stages, streaks and seven or thirty days of history, stored only on your device.</p></article>
    <article class="glass"><h3>We respect you</h3><p>Offline insights always work. Add your OpenRouter key only if you want AI coaching.</p></article>
  </div>
</section>

<section class="crew-section" id="friends">
  <div class="section-head"><span>02 / THE CREW</span><h2>Four friends.<br><em>One daily ritual.</em></h2></div>
  <div class="crew">
    <div class="crew-card glass"><div class="crew-art droplet"></div><h3>Droplet</h3><p>The original sip buddy.</p></div>
    <div class="crew-card glass"><div class="crew-art sprout"></div><h3>Sprout</h3><p>Leaves first, flowers later.</p></div>
    <div class="crew-card glass"><div class="crew-art kitty"></div><h3>Kitty</h3><p>Bows, tail swishes, big hops.</p></div>
    <div class="crew-card glass"><div class="crew-art blobby"></div><h3>Blobby</h3><p>Wobbles with pure joy.</p></div>
  </div>
</section>

<section class="download" id="download">
  <div class="section-head"><span>03 / GET IT</span><h2>Make a friend.<br><em>Keep a promise.</em></h2></div>
  <p>Android 8.0 (API 26) or newer. Download the APK, allow installation from your browser, and open it. No account, no sign-in, no tracking.</p>
  <a class="big-button" data-download href="https://github.com/Cooreo/Sippy/releases/download/1.2.0/sippy-debug.apk">DOWNLOAD SIPPY <span data-version>1.2.0</span> <b>↓</b></a>
  <p class="small">Build it yourself from <a href="https://github.com/Cooreo/Sippy">source</a>. Read the <a href="PRIVACY_POLICY.md">privacy policy</a>.</p>
</section>
</main>
<footer><span>SIPPY · HYDRATE IN YOUR ORBIT</span><span>NO CLOUD. NO ADS. JUST WATER.</span></footer>
<script src="script.js"></script>
</body>
</html>
```

---

## `docs/script.js`

```javascript
(function () {
  var intro = document.getElementById('intro');
  var reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (!reduce) setTimeout(function () { intro.classList.add('hidden'); }, 1550);

  // Dynamic download: always use the newest GitHub release APK asset.
  var links = document.querySelectorAll('[data-download]');
  if (links.length) {
    fetch('https://api.github.com/repos/Cooreo/Sippy/releases/latest', {
      headers: { Accept: 'application/vnd.github+json' }
    }).then(function (r) { return r.ok ? r.json() : Promise.reject(); })
      .then(function (release) {
        var apk = (release.assets || []).find(function (a) { return a.name.toLowerCase().endsWith('.apk'); });
        if (!apk) return;
        links.forEach(function (a) { a.href = apk.browser_download_url; });
        document.querySelectorAll('[data-version]').forEach(function (el) {
          el.textContent = String(release.tag_name || '').replace(/^v/, '');
        });
      }).catch(function () { /* static fallback stays in place */ });
  }

  var canvas = document.getElementById('stars');
  if (reduce || !canvas) return;
  var ctx = canvas.getContext('2d');
  var stars = [];
  function resize() {
    canvas.width = innerWidth * devicePixelRatio;
    canvas.height = innerHeight * devicePixelRatio;
    ctx.setTransform(devicePixelRatio, 0, 0, devicePixelRatio, 0, 0);
  }
  resize();
  addEventListener('resize', resize);
  for (var i = 0; i < 60; i++) {
    stars.push({ x: Math.random() * innerWidth, y: Math.random() * innerHeight,
      r: Math.random() * 1.6 + .3, s: Math.random() * .25 + .05,
      c: Math.random() > .6 ? '102,230,255' : '255,255,255' });
  }
  function frame() {
    ctx.clearRect(0, 0, innerWidth, innerHeight);
    for (var j = 0; j < stars.length; j++) {
      var s = stars[j];
      s.y -= s.s;
      if (s.y < -2) { s.y = innerHeight + 2; s.x = Math.random() * innerWidth; }
      ctx.beginPath();
      ctx.fillStyle = 'rgba(' + s.c + ',' + (.25 + s.r * .25) + ')';
      ctx.arc(s.x, s.y, s.r, 0, Math.PI * 2);
      ctx.fill();
    }
    requestAnimationFrame(frame);
  }
  frame();
})();
```

---

## `docs/styles.css`

```css
:root{--ink:#0b0716;--violet:#7b2ff7;--glow:#ff77d4;--cyan:#66e6ff;--lime:#c8ff2e;--paper:#f6f1ff}
*{box-sizing:border-box}html{scroll-behavior:smooth}html,body{max-width:100%;overflow-x:hidden}
body{margin:0;background:var(--ink);color:#fff;font-family:Inter,Arial,Helvetica,sans-serif}
a{color:inherit}
.intro{position:fixed;inset:0;z-index:100;background:radial-gradient(circle at 50% 50%,#2b0f54,#0b0716 70%);display:grid;place-items:center;transition:transform .9s cubic-bezier(.7,0,.2,1),opacity .6s}
.intro.hidden{transform:translateY(-100%);opacity:0;pointer-events:none}
.intro-inner{text-align:center;font-weight:900;font-size:clamp(46px,11vw,120px);line-height:.82;letter-spacing:-6px}
.intro-line{opacity:0;transform:translateY(50px) rotate(-4deg);animation:introIn .55s forwards}
.i1{color:var(--cyan);animation-delay:.1s}.i2{animation-delay:.35s}.i3{color:var(--glow);animation-delay:.6s}
.intro-orbit{width:0;height:3px;margin:38px auto 0;border-radius:9px;background:linear-gradient(90deg,var(--cyan),var(--glow));animation:bar 1s .3s forwards}
@keyframes introIn{to{opacity:1;transform:none}}@keyframes bar{to{width:min(420px,72vw)}}
#stars{position:fixed;inset:0;z-index:0;pointer-events:none}
.topbar{position:sticky;top:0;z-index:20;display:flex;justify-content:space-between;align-items:center;padding:14px 4vw;background:rgba(11,7,22,.72);backdrop-filter:blur(18px);border-bottom:1px solid rgba(255,255,255,.12)}
.brand{display:flex;gap:10px;align-items:center;font-weight:900;letter-spacing:2px;text-decoration:none}.brand img{width:32px;height:32px}
nav{display:flex;gap:22px;align-items:center;font-size:12px;font-weight:800;text-transform:uppercase;letter-spacing:1px}
nav a{text-decoration:none;opacity:.85}nav a:hover{opacity:1}
nav .pill{padding:10px 16px;border-radius:999px;background:var(--lime);color:var(--ink);opacity:1}
.hero{position:relative;z-index:2;display:grid;grid-template-columns:1.05fr .95fr;gap:3vw;align-items:center;min-height:94vh;padding:8vh 4vw 10vh}
.eyebrow{font-size:11px;letter-spacing:3px;font-weight:900;color:var(--cyan)}
h1{font-size:clamp(54px,8.6vw,132px);line-height:.82;letter-spacing:-7px;margin:12px 0 24px;text-transform:uppercase}
h1 span{display:block}h1 .script{font-family:Georgia,serif;font-style:italic;text-transform:none;font-weight:400;letter-spacing:-3px;color:var(--glow);font-size:.86em;margin:2px 0 6px 8vw}
h1 .outline{-webkit-text-stroke:2px rgba(255,255,255,.8);color:transparent}
.lede{max-width:540px;line-height:1.55;opacity:.82;font-size:17px}
.cta-row{display:flex;gap:18px;align-items:center;flex-wrap:wrap;margin-top:30px}
.big-button{display:inline-flex;gap:18px;align-items:center;padding:18px 24px;border-radius:999px;text-decoration:none;font-weight:900;letter-spacing:.5px;color:var(--ink);background:linear-gradient(100deg,var(--lime),#eaff9a);box-shadow:0 14px 40px rgba(200,255,46,.25);transition:transform .25s,box-shadow .25s}
.big-button:hover{transform:translateY(-3px);box-shadow:0 20px 50px rgba(200,255,46,.38)}
.big-button b{font-size:22px}.text-button{font-weight:800;text-decoration:none;border-bottom:2px solid var(--glow)}
.universe{position:relative;height:min(62vw,620px);display:grid;place-items:center}
.planet{position:relative;width:min(44vw,340px);height:min(44vw,340px);border-radius:50%;display:grid;place-items:center;background:radial-gradient(circle at 35% 30%,rgba(255,255,255,.18),rgba(123,47,247,.22) 55%,transparent 70%);box-shadow:0 0 90px rgba(123,47,247,.5),inset 0 0 40px rgba(255,255,255,.06);animation:float 6s ease-in-out infinite}
.planet img{width:74%;height:74%;object-fit:contain;filter:drop-shadow(0 18px 30px rgba(0,0,0,.45))}
.orbit{position:absolute;border:1px solid rgba(255,255,255,.24);border-radius:50%;transform-style:preserve-3d}
.orbit-a{width:min(58vw,520px);height:min(58vw,520px);border-top-color:var(--glow);animation:spin 18s linear infinite}
.orbit-b{width:min(72vw,650px);height:min(36vw,320px);transform:rotateX(64deg) rotate(-18deg);border-color:rgba(102,230,255,.45);animation:spin2 24s linear infinite}
.orbit-c{width:min(80vw,720px);height:min(42vw,380px);transform:rotateX(70deg) rotate(24deg);border-color:rgba(255,119,212,.35);animation:spin3 30s linear infinite}
.glass-float{position:absolute;padding:12px 16px;border-radius:18px;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.24);backdrop-filter:blur(14px);box-shadow:0 16px 40px rgba(0,0,0,.35);animation:float 5s ease-in-out infinite}
.glass-float small{display:block;font-size:9px;letter-spacing:2px;color:var(--cyan);font-weight:900}.glass-float span{font-size:13px;font-weight:700}
.f1{top:10%;left:-2%;animation-delay:.2s}.f2{top:48%;right:-6%;animation-delay:1s}.f3{bottom:8%;left:8%;animation-delay:1.8s}
.dust{position:absolute;border-radius:50%;background:var(--glow);filter:blur(1px);animation:twinkle 2.6s infinite}
.d1{width:6px;height:6px;top:18%;right:12%}.d2{width:4px;height:4px;bottom:24%;right:20%;background:var(--cyan);animation-delay:.7s}.d3{width:8px;height:8px;top:60%;left:4%;background:var(--lime);animation-delay:1.3s}
.marquee{position:relative;z-index:2;overflow:hidden;background:linear-gradient(90deg,#2b0f54,var(--violet),#2b0f54);padding:12px 0;font-weight:900;letter-spacing:2px;white-space:nowrap}
.marquee-track{display:inline-block;animation:scroll 18s linear infinite}
.dark,.crew-section,.download{position:relative;z-index:2;padding:10vh 4vw}
.dark{background:linear-gradient(160deg,#170a2e,#0b0716)}
.section-head{display:flex;flex-direction:column;gap:14px;margin-bottom:36px}
.section-head span{font-size:11px;letter-spacing:3px;font-weight:900;color:var(--lime)}
.section-head h2{font-size:clamp(36px,6vw,76px);line-height:.95;letter-spacing:-4px;margin:0;text-transform:uppercase}
.section-head h2 em{font-family:Georgia,serif;font-weight:400;text-transform:none;color:var(--glow);letter-spacing:-2px}
.glass{background:rgba(255,255,255,.06);border:1px solid rgba(255,255,255,.16);backdrop-filter:blur(14px);box-shadow:0 20px 50px rgba(0,0,0,.3)}
.cards,.crew{display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:18px}
.cards article,.crew-card{padding:24px;border-radius:22px;transition:transform .25s,border-color .25s}
.cards article:hover,.crew-card:hover{transform:translateY(-8px);border-color:var(--glow)}
.cards h3,.crew-card h3{margin:0 0 10px;color:var(--cyan)}.cards p,.crew-card p{margin:0;line-height:1.5;opacity:.8}
.crew-section{background:radial-gradient(circle at 20% 30%,rgba(123,47,247,.25),transparent 50%),#120a26}
.crew-card{text-align:center}.crew-card h3{color:#fff}
.crew-art{width:100px;height:100px;margin:0 auto 18px;position:relative;animation:float 3s ease-in-out infinite;box-shadow:inset -12px -16px 30px rgba(0,0,0,.25),inset 10px 10px 25px rgba(255,255,255,.35)}
.crew-art:after{content:"";position:absolute;left:30px;top:44px;width:12px;height:12px;border-radius:50%;background:#0b0716;box-shadow:28px 0 0 #0b0716}
.droplet{border-radius:50% 50% 50% 50%/60% 60% 40% 40%;background:linear-gradient(140deg,#b6f0ff,#079cc9 60%,#075985)}
.sprout{border-radius:45% 55% 50% 50%;background:linear-gradient(140deg,#bbf7d0,#34d399,#047857)}
.kitty{border-radius:46%;background:linear-gradient(140deg,#ddd6fe,#a78bfa,#6d28d9)}
.blobby{border-radius:55% 45% 50% 50%;background:linear-gradient(140deg,#fed7aa,#fb923c,#c2410c)}
.download{background:radial-gradient(circle at 80% 20%,rgba(200,255,46,.14),transparent 45%),#0b0716}
.download p{max-width:620px;line-height:1.6;opacity:.85}.small{font-size:13px}.small a{color:var(--cyan)}
footer{position:relative;z-index:2;display:flex;justify-content:space-between;gap:20px;flex-wrap:wrap;padding:24px 4vw;background:#07040f;font-size:11px;letter-spacing:2px;font-weight:900;opacity:.85}
@keyframes float{0%,100%{transform:translateY(0)}50%{transform:translateY(-12px)}}
@keyframes spin{to{transform:rotate(360deg)}}
@keyframes spin2{to{transform:rotateX(64deg) rotate(342deg)}}
@keyframes spin3{to{transform:rotateX(70deg) rotate(-336deg)}}
@keyframes twinkle{0%,100%{opacity:.2;transform:scale(.7)}50%{opacity:1;transform:scale(1.2)}}
@keyframes scroll{to{transform:translateX(-50%)}}
@media(max-width:860px){.hero{grid-template-columns:1fr;padding-top:7vh}.universe{height:460px;margin-top:20px}h1{letter-spacing:-4px}.glass-float{display:none}.orbit-b,.orbit-c{width:90vw;height:45vw}}
@media(max-width:600px){.topbar{padding:10px 16px}.brand{font-size:18px}.brand img{width:26px;height:26px}nav{gap:0}nav a:not(.pill){display:none}nav .pill{padding:8px 12px;font-size:10px}.hero{padding:5vh 16px 8vh}h1{font-size:clamp(42px,13vw,72px);letter-spacing:-3px}h1 .script{margin-left:12vw}.lede{font-size:15px}.cta-row{flex-direction:column;align-items:stretch}.big-button{width:100%;justify-content:space-between;padding:16px 18px}.universe{height:360px}.planet{width:230px;height:230px}.orbit-a{width:320px;height:320px}.dark,.crew-section,.download{padding:8vh 16px}.section-head h2{font-size:clamp(30px,9vw,48px);letter-spacing:-2px}.marquee{font-size:18px}footer{padding:20px 16px}}
@media(prefers-reduced-motion:reduce){*{animation:none!important;transition:none!important}.intro{display:none}#stars{display:none}}
```

---

## `gradlew`

```
#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
#

##############################################################################
#
#   Gradle start up script for POSIX generated by Gradle.
#
#   Important for running:
#
#   (1) You need a POSIX-compliant shell to run this script. If your /bin/sh is
#       noncompliant, but you have some other compliant shell such as ksh or
#       bash, then to run this script, type that shell name before the whole
#       command line, like:
#
#           ksh Gradle
#
#       Busybox and similar reduced shells will NOT work, because this script
#       requires all of these POSIX shell features:
#         * functions;
#         * expansions «$var», «${var}», «${var:-default}», «${var+SET}»,
#           «${var#prefix}», «${var%suffix}», and «$( cmd )»;
#         * compound commands having a testable exit status, especially «case»;
#         * various built-in commands including «command», «set», and «ulimit».
#
#   Important for patching:
#
#   (2) This script targets any POSIX shell, so it avoids extensions provided
#       by Bash, Ksh, etc; in particular arrays are avoided.
#
#       The "traditional" practice of packing multiple parameters into a
#       space-separated string is a well documented source of bugs and security
#       problems, so this is (mostly) avoided, by progressively accumulating
#       options in "$@", and eventually passing that to Java.
#
#       Where the inherited environment variables (DEFAULT_JVM_OPTS, JAVA_OPTS,
#       and GRADLE_OPTS) rely on word-splitting, this is performed explicitly;
#       see the in-line comments for details.
#
#       There are tweaks for specific operating systems such as AIX, CygWin,
#       Darwin, MinGW, and NonStop.
#
#   (3) This script is generated from the Groovy template
#       https://github.com/gradle/gradle/blob/HEAD/platforms/jvm/plugins-application/src/main/resources/org/gradle/api/internal/plugins/unixStartScript.txt
#       within the Gradle project.
#
#       You can find Gradle at https://github.com/gradle/gradle/.
#
##############################################################################

# Attempt to set APP_HOME

# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}  # leaves a trailing /; empty if no leading path
    [ -h "$app_path" ]
do
    ls=$( ls -ld "$app_path" )
    link=${ls#*' -> '}
    case $link in             #(
      /*)   app_path=$link ;; #(
      *)    app_path=$APP_HOME$link ;;
    esac
done

# This is normally unused
# shellcheck disable=SC2034
APP_BASE_NAME=${0##*/}
# Discard cd standard output in case $CDPATH is set (https://github.com/gradle/gradle/issues/25036)
APP_HOME=$( cd -P "${APP_HOME:-./}" > /dev/null && printf '%s
' "$PWD" ) || exit

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD=maximum

warn () {
    echo "$*"
} >&2

die () {
    echo
    echo "$*"
    echo
    exit 1
} >&2

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$( uname )" in                #(
  CYGWIN* )         cygwin=true  ;; #(
  Darwin* )         darwin=true  ;; #(
  MSYS* | MINGW* )  msys=true    ;; #(
  NONSTOP* )        nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD=$JAVA_HOME/jre/sh/java
    else
        JAVACMD=$JAVA_HOME/bin/java
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD=java
    if ! command -v java >/dev/null 2>&1
    then
        die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
fi

# Increase the maximum file descriptors if we can.
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    case $MAX_FD in #(
      max*)
        # In POSIX sh, ulimit -H is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        MAX_FD=$( ulimit -H -n ) ||
            warn "Could not query maximum file descriptor limit"
    esac
    case $MAX_FD in  #(
      '' | soft) :;; #(
      *)
        # In POSIX sh, ulimit -n is undefined. That's why the result is checked to see if it worked.
        # shellcheck disable=SC2039,SC3045
        ulimit -n "$MAX_FD" ||
            warn "Could not set maximum file descriptor limit to $MAX_FD"
    esac
fi

# Collect all arguments for the java command, stacking in reverse order:
#   * args from the command line
#   * the main class name
#   * -classpath
#   * -D...appname settings
#   * --module-path (only if needed)
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, and GRADLE_OPTS environment variables.

# For Cygwin or MSYS, switch paths to Windows format before running java
if "$cygwin" || "$msys" ; then
    APP_HOME=$( cygpath --path --mixed "$APP_HOME" )
    CLASSPATH=$( cygpath --path --mixed "$CLASSPATH" )

    JAVACMD=$( cygpath --unix "$JAVACMD" )

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if
            case $arg in                                #(
              -*)   false ;;                            # don't mess with options #(
              /?*)  t=${arg#/} t=/${t%%/*}              # looks like a POSIX filepath
                    [ -e "$t" ] ;;                      #(
              *)    false ;;
            esac
        then
            arg=$( cygpath --path --ignore --mixed "$arg" )
        fi
        # Roll the args list around exactly as many times as the number of
        # args, so each arg winds up back in the position where it started, but
        # possibly modified.
        #
        # NB: a `for` loop captures its iteration list before it begins, so
        # changing the positional parameters here affects neither the number of
        # iterations, nor the values presented in `arg`.
        shift                   # remove old arg
        set -- "$@" "$arg"      # push replacement arg
    done
fi


# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='-Dfile.encoding=UTF-8 "-Xmx64m" "-Xms64m"'

# Collect all arguments for the java command:
#   * DEFAULT_JVM_OPTS, JAVA_OPTS, JAVA_OPTS, and optsEnvironmentVar are not allowed to contain shell fragments,
#     and any embedded shellness will be escaped.
#   * For example: A user cannot expect ${Hostname} to be expanded, as it is an environment variable and will be
#     treated as '${Hostname}' itself on the command line.

set -- \
        "-Dorg.gradle.appname=$APP_BASE_NAME" \
        -classpath "$CLASSPATH" \
        org.gradle.wrapper.GradleWrapperMain \
        "$@"

# Stop when "xargs" is not available.
if ! command -v xargs >/dev/null 2>&1
then
    die "xargs is not available"
fi

# Use "xargs" to parse quoted args.
#
# With -n1 it outputs one arg per line, with the quotes and backslashes removed.
#
# In Bash we could simply go:
#
#   readarray ARGS < <( xargs -n1 <<<"$var" ) &&
#   set -- "${ARGS[@]}" "$@"
#
# but POSIX shell has neither arrays nor command substitution, so instead we
# post-process each arg (as a line of input to sed) to backslash-escape any
# character that might be a shell metacharacter, then use eval to reverse
# that process (while maintaining the separation between arguments), and wrap
# the whole thing up as a single "set" statement.
#
# This will of course break if any of these variables contains a newline or
# an unmatched quote.
#

eval "set -- $(
        printf '%s\n' "$DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS" |
        xargs -n1 |
        sed ' s~[^-[:alnum:]+,./:=@_]~\\&~g; ' |
        tr '\n' ' '
    )" '"$@"'

exec "$JAVACMD" "$@"
```

---

## `gradlew.bat`

```bat
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=-Dfile.encoding=UTF-8 "-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%GRADLE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
```
