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
