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
