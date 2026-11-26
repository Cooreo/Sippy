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
            Card(
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text("SIMULATE MODE — no changes are saved to the database",
                    modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
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
        ProgressRing(
            progress = state.progress,
            centerLabel = AmountFormat.display(state.effectiveTotalMl, cfg.units),
            bottomLabel = "of ${AmountFormat.display(cfg.goalMl, cfg.units)} · ${(state.progress * 100).toInt()}%"
        )
        Text(
            "Pace: ${(state.pace * 100).toInt()}% of the day elapsed",
            style = MaterialTheme.typography.labelSmall
        )
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
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
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
