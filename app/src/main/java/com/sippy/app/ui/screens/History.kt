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
