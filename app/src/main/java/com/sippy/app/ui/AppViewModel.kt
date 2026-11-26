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
