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
