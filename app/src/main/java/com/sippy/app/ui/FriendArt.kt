package com.sippy.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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

/**
 * One Canvas, one infinite phase supplied by the caller. All moods and species are drawn here,
 * with no Path/Color allocation inside the repeated draw loop (the triangle Path is remembered).
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
    val scale = when (stage) {
        GrowthStage.BABY -> 0.72f
        GrowthStage.YOUNG -> 0.86f
        GrowthStage.GROWN -> 1f
        GrowthStage.ELDER -> 1.1f
    }
    val t = if (animationsEnabled) phase * 2f * PI.toFloat() else 0f
    val bob = if (animationsEnabled && (mood != Mood.SLEEPY || animation == AnimationKind.BOB))
        sin(t) * 6f else 0f
    val dancing = mood == Mood.DANCING || animation == AnimationKind.DANCE
    val rotation = if (dancing && animationsEnabled) sin(t * 3f) * 9f else 0f
    val giggle = if (animation == AnimationKind.GIGGLE && animationsEnabled)
        1f + sin(animationProgress * PI.toFloat() * 4f) * 0.07f else 1f
    val squish = if (animation == AnimationKind.LEVEL_UP && animationsEnabled)
        1f + sin(animationProgress * PI.toFloat()) * 0.12f else 1f
    val blink = (animation == AnimationKind.BLINK && animationProgress < 0.16f) ||
        (animationsEnabled && (phase * 9f) % 1f < 0.045f)
    val triangle = remember { Path() }

    val clickable = if (onClick != null) Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    ) else Modifier

    Canvas(
        modifier
            .size(size)
            .then(clickable)
            .graphicsLayer(
                translationY = bob,
                rotationZ = rotation,
                scaleX = scale * giggle / squish,
                scaleY = scale * giggle * squish
            )
            .semantics(mergeDescendants = true) {
                description?.let { contentDescription = it }
            }
    ) {
        val cx = center.x
        val cy = center.y
        val r = size.toPx() * 0.30f
        when (species) {
            Species.DROPLET -> drawDroplet(cx, cy, r, mood, blink, dancing, stage, colors, triangle, t)
            Species.SPROUT -> drawSprout(cx, cy, r, mood, blink, dancing, stage, colors, t)
            Species.KITTY -> drawKitty(cx, cy, r, mood, blink, dancing, stage, colors, t)
            Species.BLOBBY -> drawBlobby(cx, cy, r, mood, blink, dancing, stage, colors, t)
        }
        if (mood == Mood.HAPPY || dancing) {
            val sparkleAlpha = if (animationsEnabled) 0.45f + 0.45f * sin(t * 2f) else 0.8f
            for (i in 0 until 4) {
                val a = t + i * (PI.toFloat() / 2f)
                drawCircle(
                    colors.glow.copy(alpha = sparkleAlpha),
                    radius = 3f + i % 2 * 2f,
                    center = Offset(cx + cos(a) * r * 1.35f, cy + sin(a) * r * 1.25f - r * 0.4f)
                )
            }
        }
        if (mood == Mood.DANCING || animation == AnimationKind.CONFETTI || animation == AnimationKind.LEVEL_UP) {
            drawParticles(cx, cy, r, colors, phase, animationsEnabled)
        }
    }
}

private fun DrawScope.drawDroplet(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, colors: FriendColors, triangle: Path, t: Float
) {
    drawCircle(colors.glow.copy(alpha = 0.22f), r * 1.18f, Offset(cx, cy + r * 0.12f))
    drawCircle(colors.primary, r, Offset(cx, cy))
    triangle.rewind()
    triangle.moveTo(cx, cy - r * 1.65f)
    triangle.lineTo(cx - r * 0.52f, cy - r * 0.45f)
    triangle.lineTo(cx + r * 0.52f, cy - r * 0.45f)
    triangle.close()
    drawPath(triangle, colors.primary)
    drawCircle(Color.White.copy(alpha = 0.35f), r * 0.24f, Offset(cx - r * 0.38f, cy - r * 0.3f))
    drawFace(cx, cy + r * 0.08f, r * 0.28f, mood, blink, dancing, colors.ink)
    if (stage == GrowthStage.ELDER) {
        drawCircle(Color.White.copy(alpha = 0.8f), r * 0.09f, Offset(cx + r * 0.55f, cy + r * 0.55f))
        drawCircle(Color.White.copy(alpha = 0.8f), r * 0.07f, Offset(cx - r * 0.6f, cy + r * 0.45f))
    }
}

private fun DrawScope.drawSprout(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, colors: FriendColors, t: Float
) {
    val sway = if (dancing) sin(t * 3f) * r * 0.12f else sin(t) * r * 0.03f
    drawRoundRectBox(colors.primary, Rect(cx - r * 0.75f, cy + r * 0.35f, cx + r * 0.75f, cy + r * 1.15f), r * 0.2f)
    drawLine(colors.ink.copy(alpha = 0.55f), Offset(cx, cy + r * 0.4f), Offset(cx + sway, cy - r * 0.9f), r * 0.1f)
    drawOvalBox(colors.secondary, Rect(cx + sway - r * 0.95f, cy - r * 0.55f, cx + sway - r * 0.05f, cy - r * 0.05f))
    drawOvalBox(colors.secondary, Rect(cx + sway + r * 0.05f, cy - r * 0.8f, cx + sway + r * 0.95f, cy - r * 0.3f))
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        for (i in 0 until 5) {
            val a = i * (PI.toFloat() * 2f / 5f) + if (dancing) t else 0f
            drawCircle(colors.glow, r * 0.22f, Offset(cx + sway + cos(a) * r * 0.38f, cy - r * 0.95f + sin(a) * r * 0.38f))
        }
        drawCircle(colors.light, r * 0.2f, Offset(cx + sway, cy - r * 0.95f))
    }
    if (stage == GrowthStage.ELDER) {
        drawOvalBox(colors.secondary, Rect(cx - r * 1.1f, cy + r * 0.05f, cx - r * 0.35f, cy + r * 0.4f))
        drawOvalBox(colors.secondary, Rect(cx + r * 0.35f, cy - r * 0.1f, cx + r * 1.1f, cy + r * 0.25f))
    }
    drawFace(cx + sway * 0.3f, cy + r * 0.7f, r * 0.22f, mood, blink, dancing, colors.ink)
}

private fun DrawScope.drawKitty(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, colors: FriendColors, t: Float
) {
    val hop = if (dancing) sin(t * 4f) * r * 0.12f else 0f
    val tail = if (dancing) sin(t * 2f) * r * 0.3f else r * 0.2f
    drawLine(colors.primary, Offset(cx + r * 0.8f, cy + r * 0.75f),
        Offset(cx + r * 1.35f + tail, cy + r * 0.2f), r * 0.16f)
    drawCircle(colors.primary, r * 0.82f, Offset(cx, cy - r * 0.15f + hop))
    drawCircle(colors.primary, r * 0.38f, Offset(cx - r * 0.62f, cy - r * 0.72f + hop))
    drawCircle(colors.primary, r * 0.38f, Offset(cx + r * 0.62f, cy - r * 0.72f + hop))
    drawCircle(colors.glow, r * 0.19f, Offset(cx - r * 0.62f, cy - r * 0.74f + hop))
    drawCircle(colors.glow, r * 0.19f, Offset(cx + r * 0.62f, cy - r * 0.74f + hop))
    drawFace(cx, cy - r * 0.16f + hop, r * 0.27f, mood, blink, dancing, colors.ink)
    for (i in 0 until 2) {
        val side = if (i == 0) -1f else 1f
        drawLine(colors.ink.copy(alpha = 0.6f), Offset(cx + side * r * 0.5f, cy + hop),
            Offset(cx + side * r * 0.98f, cy - r * 0.08f + hop), r * 0.035f)
        drawLine(colors.ink.copy(alpha = 0.6f), Offset(cx + side * r * 0.5f, cy + r * 0.14f + hop),
            Offset(cx + side * r * 0.98f, cy + r * 0.22f + hop), r * 0.035f)
    }
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        drawOvalBox(colors.glow, Rect(cx - r * 0.95f, cy + r * 0.7f + hop, cx - r * 0.1f, cy + r * 1.02f + hop))
        drawOvalBox(colors.glow, Rect(cx + r * 0.1f, cy + r * 0.7f + hop, cx + r * 0.95f, cy + r * 1.02f + hop))
        drawCircle(colors.primary, r * 0.17f, Offset(cx, cy + r * 0.86f + hop))
    }
    if (stage == GrowthStage.ELDER) {
        for (i in -1..1) drawCircle(colors.light, r * 0.18f, Offset(cx + i * r * 0.42f, cy - r * 1.12f + hop))
    }
}

private fun DrawScope.drawBlobby(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, colors: FriendColors, t: Float
) {
    val wobble = if (dancing) sin(t * 4f) * r * 0.14f else sin(t) * r * 0.04f
    drawOvalBox(colors.primary, Rect(cx - r - wobble, cy - r * 0.65f, cx + r + wobble, cy + r * 1.1f))
    drawCircle(colors.primary, r * 0.72f, Offset(cx - r * 0.6f, cy - r * 0.25f))
    drawCircle(colors.primary, r * 0.72f, Offset(cx + r * 0.6f, cy - r * 0.25f))
    drawOvalBox(colors.secondary, Rect(cx - r * 0.55f, cy + r * 0.95f, cx - r * 0.05f, cy + r * 1.3f))
    drawOvalBox(colors.secondary, Rect(cx + r * 0.05f, cy + r * 0.95f, cx + r * 0.55f, cy + r * 1.3f))
    drawCircle(Color.White.copy(alpha = 0.28f), r * 0.22f, Offset(cx - r * 0.35f, cy - r * 0.35f))
    drawFace(cx, cy + r * 0.12f, r * 0.3f, mood, blink, dancing, colors.ink)
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        drawCircle(colors.glow, r * 0.2f, Offset(cx, cy - r * 0.95f))
        drawCircle(colors.glow, r * 0.14f, Offset(cx - r * 0.28f, cy - r * 0.8f))
        drawCircle(colors.glow, r * 0.14f, Offset(cx + r * 0.28f, cy - r * 0.8f))
    }
    if (stage == GrowthStage.ELDER) {
        drawLine(colors.ink, Offset(cx - r * 0.3f, cy + r * 0.95f), Offset(cx - r * 0.45f, cy + r * 1.3f), r * 0.05f)
        drawLine(colors.ink, Offset(cx + r * 0.3f, cy + r * 0.95f), Offset(cx + r * 0.45f, cy + r * 1.3f), r * 0.05f)
    }
}

private fun DrawScope.drawFace(
    cx: Float, cy: Float, eyeGap: Float, mood: Mood, blink: Boolean, dancing: Boolean, ink: Color
) {
    val eyeR = eyeGap * 0.28f
    val left = cx - eyeGap
    val right = cx + eyeGap
    when {
        mood == Mood.SLEEPY && !blink -> {
            drawLine(ink, Offset(left - eyeR, cy), Offset(left + eyeR, cy), eyeR * 0.35f)
            drawLine(ink, Offset(right - eyeR, cy), Offset(right + eyeR, cy), eyeR * 0.35f)
        }
        blink -> {
            drawLine(ink, Offset(left - eyeR, cy), Offset(left + eyeR, cy), eyeR * 0.35f)
            drawLine(ink, Offset(right - eyeR, cy), Offset(right + eyeR, cy), eyeR * 0.35f)
        }
        mood == Mood.HAPPY -> {
            drawCircle(ink, eyeR, Offset(left, cy))
            drawCircle(ink, eyeR, Offset(right, cy))
            for (eye in 0 until 2) {
                val ex = if (eye == 0) left else right
                drawLine(Color.White, Offset(ex - eyeR * 0.5f, cy - eyeR * 1.6f), Offset(ex + eyeR * 0.5f, cy - eyeR * 1.6f), eyeR * 0.3f)
                drawLine(Color.White, Offset(ex, cy - eyeR * 2.1f), Offset(ex, cy - eyeR * 1.1f), eyeR * 0.3f)
            }
        }
        else -> {
            drawCircle(ink, eyeR, Offset(left, cy))
            drawCircle(ink, eyeR, Offset(right, cy))
        }
    }
    val mouthY = cy + eyeGap * 1.25f
    when (mood) {
        Mood.THIRSTY -> drawArcBox(
            Rect(cx - eyeGap * 0.6f, mouthY + eyeGap * 0.3f, cx + eyeGap * 0.6f, mouthY + eyeGap),
            180f, 180f, ink, eyeR * 0.28f, Stroke
        )
        Mood.SLEEPY -> drawLine(ink, Offset(cx - eyeR, mouthY), Offset(cx + eyeR, mouthY), eyeR * 0.32f)
        Mood.HAPPY, Mood.DANCING -> drawOvalBox(ink, Rect(cx - eyeGap * 0.55f, mouthY - eyeR * 0.4f,
            cx + eyeGap * 0.55f, mouthY + eyeR * 1.1f))
        else -> drawArcBox(
            Rect(cx - eyeGap * 0.6f, mouthY - eyeGap * 0.35f, cx + eyeGap * 0.6f, mouthY + eyeGap * 0.35f),
            0f, 180f, ink, eyeR * 0.28f, Stroke
        )
    }
}

private fun DrawScope.drawParticles(
    cx: Float, cy: Float, r: Float, colors: FriendColors, phase: Float, enabled: Boolean
) {
    val p = if (enabled) phase % 1f else 0.35f
    for (i in 0 until 18) {
        val seed = (i * 37 % 100) / 100f
        val x = cx + (seed - 0.5f) * r * 3.4f
        val fall = ((p + seed) % 1f)
        val y = cy - r * 1.8f + fall * r * 3.4f
        val color = when (i % 3) {
            0 -> colors.primary
            1 -> colors.glow
            else -> colors.secondary
        }
        if (i % 2 == 0) {
            drawCircle(color.copy(alpha = 0.85f), r * 0.07f, Offset(x, y))
        } else {
            drawRect(color.copy(alpha = 0.75f), Offset(x, y), androidx.compose.ui.geometry.Size(r * 0.12f, r * 0.18f))
        }
    }
}


private fun DrawScope.drawOvalBox(color: Color, rect: Rect, alpha: Float = 1f) = drawOval(
    color = color, topLeft = Offset(rect.left, rect.top),
    size = androidx.compose.ui.geometry.Size(rect.width, rect.height), alpha = alpha
)

private fun DrawScope.drawRoundRectBox(
    color: Color, rect: Rect, corner: Float, alpha: Float = 1f
) = drawRoundRect(
    color = color, topLeft = Offset(rect.left, rect.top),
    size = androidx.compose.ui.geometry.Size(rect.width, rect.height),
    cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner), alpha = alpha
)

private fun DrawScope.drawArcBox(
    rect: Rect, start: Float, sweep: Float, color: Color, strokeWidth: Float,
    style: androidx.compose.ui.graphics.drawscope.DrawStyle
) = drawArc(
    color = color, startAngle = start, sweepAngle = sweep, useCenter = false,
    topLeft = Offset(rect.left, rect.top),
    size = androidx.compose.ui.geometry.Size(rect.width, rect.height),
    style = style, alpha = 0.85f
)
