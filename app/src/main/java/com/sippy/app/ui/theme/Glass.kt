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
