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
