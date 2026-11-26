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
