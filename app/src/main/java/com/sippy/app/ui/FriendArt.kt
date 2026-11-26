package com.sippy.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
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
 * One Canvas, one infinite phase from the caller. Richer 2.5D shading is done with gradients;
 * no bitmaps, no Lottie and no allocations in the repeated draw loop.
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
    val body = remember(colors) {
        Brush.linearGradient(listOf(colors.light, colors.secondary, colors.primary))
    }
    val aura = remember(colors) {
        Brush.radialGradient(listOf(colors.glow.copy(alpha = 0.4f), Color.Transparent))
    }
    val glass = remember(colors) {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0f)))
    }
    val scale = when (stage) {
        GrowthStage.BABY -> 0.74f
        GrowthStage.YOUNG -> 0.88f
        GrowthStage.GROWN -> 1f
        GrowthStage.ELDER -> 1.12f
    }
    val t = if (animationsEnabled) phase * 2f * PI.toFloat() else 0f
    val sleepy = mood == Mood.SLEEPY && animation != AnimationKind.BOB
    val bob = if (animationsEnabled && !sleepy) sin(t + 0.4f) * 7f else 0f
    val dancing = mood == Mood.DANCING || animation == AnimationKind.DANCE
    val rotation = if (dancing && animationsEnabled) sin(t * 3f) * 10f else 0f
    val giggle = if (animation == AnimationKind.GIGGLE && animationsEnabled)
        1f + sin(animationProgress * PI.toFloat() * 4f) * 0.08f else 1f
    val squish = if (animation == AnimationKind.LEVEL_UP && animationsEnabled)
        1f + sin(animationProgress * PI.toFloat()) * 0.12f else 1f
    val blink = (animation == AnimationKind.BLINK && animationProgress < 0.16f) ||
        (animationsEnabled && (phase * 9f) % 1f < 0.05f)
    val triangle = remember { Path() }
    val interaction = remember { MutableInteractionSource() }

    Canvas(
        modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable(interactionSource = interaction,
                indication = null, onClick = onClick) else Modifier)
            .graphicsLayer(
                translationY = bob,
                rotationZ = rotation,
                scaleX = scale * giggle / squish,
                scaleY = scale * giggle * squish
            )
            .semantics(mergeDescendants = true) { description?.let { contentDescription = it } }
    ) {
        val cx = center.x
        val cy = center.y
        val r = size.toPx() * 0.30f
        drawCircle(brush = aura, radius = r * 1.75f, center = Offset(cx, cy + r * 0.15f))
        when (species) {
            Species.DROPLET -> drawDroplet(cx, cy, r, mood, blink, dancing, stage, body, glass, colors, triangle, t)
            Species.SPROUT -> drawSprout(cx, cy, r, mood, blink, dancing, stage, body, glass, colors, t)
            Species.KITTY -> drawKitty(cx, cy, r, mood, blink, dancing, stage, body, glass, colors, t)
            Species.BLOBBY -> drawBlobby(cx, cy, r, mood, blink, dancing, stage, body, glass, colors, t)
        }
        if (mood == Mood.HAPPY || dancing) {
            val sparkle = if (animationsEnabled) 0.45f + 0.45f * sin(t * 2f) else 0.8f
            for (i in 0 until 4) {
                val a = t + i * (PI.toFloat() / 2f)
                drawCircle(colors.glow.copy(alpha = sparkle), radius = 3f + (i % 2) * 2f,
                    center = Offset(cx + cos(a) * r * 1.4f, cy + sin(a) * r * 1.3f - r * 0.4f))
            }
        }
        if (dancing || animation == AnimationKind.CONFETTI || animation == AnimationKind.LEVEL_UP) {
            drawParticles(cx, cy, r, colors, phase, animationsEnabled)
        }
    }
}

private fun DrawScope.drawDroplet(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, body: Brush, glass: Brush, colors: FriendColors, triangle: Path, t: Float
) {
    drawCircle(brush = body, radius = r, center = Offset(cx, cy))
    triangle.rewind()
    triangle.moveTo(cx, cy - r * 1.72f)
    triangle.lineTo(cx - r * 0.55f, cy - r * 0.42f)
    triangle.lineTo(cx + r * 0.55f, cy - r * 0.42f)
    triangle.close()
    drawPath(triangle, brush = body)
    drawOval(brush = glass, topLeft = Offset(cx - r * 0.72f, cy - r * 0.78f),
        size = Size(r * 0.55f, r * 0.95f))
    drawCircle(Color.White.copy(alpha = 0.75f), r * 0.1f, Offset(cx - r * 0.42f, cy - r * 0.42f))
    drawArc(color = colors.glow.copy(alpha = 0.55f), startAngle = 20f, sweepAngle = 140f,
        useCenter = false, topLeft = Offset(cx - r * 0.82f, cy + r * 0.2f),
        size = Size(r * 1.64f, r * 1.3f), style = Stroke(width = r * 0.06f))
    drawFace(cx, cy + r * 0.06f, r * 0.29f, mood, blink, dancing, colors, t)
    if (stage == GrowthStage.ELDER) {
        drawCircle(Color.White.copy(alpha = 0.85f), r * 0.09f, Offset(cx + r * 0.58f, cy + r * 0.56f))
        drawCircle(Color.White.copy(alpha = 0.7f), r * 0.07f, Offset(cx - r * 0.62f, cy + r * 0.46f))
    }
}

private fun DrawScope.drawSprout(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, body: Brush, glass: Brush, colors: FriendColors, t: Float
) {
    val sway = if (dancing) sin(t * 3f) * r * 0.14f else sin(t + 0.7f) * r * 0.035f
    drawOval(brush = glass, topLeft = Offset(cx - r * 0.85f, cy + r * 0.28f),
        size = Size(r * 1.7f, r * 0.95f))
    drawRoundRectBox(colors.primary, Rect(cx - r * 0.78f, cy + r * 0.38f, cx + r * 0.78f, cy + r * 1.18f), r * 0.22f)
    drawRoundRect(brush = body, topLeft = Offset(cx - r * 0.78f, cy + r * 0.38f),
        size = Size(r * 1.56f, r * 0.8f), cornerRadius = CornerRadius(r * 0.22f, r * 0.22f))
    drawLine(colors.ink.copy(alpha = 0.55f), Offset(cx, cy + r * 0.42f),
        Offset(cx + sway, cy - r * 0.95f), r * 0.1f)
    drawOval(brush = body, topLeft = Offset(cx + sway - r * 1.02f, cy - r * 0.62f),
        size = Size(r * 0.98f, r * 0.5f))
    drawOval(brush = body, topLeft = Offset(cx + sway + r * 0.04f, cy - r * 0.86f),
        size = Size(r * 0.98f, r * 0.5f))
    drawLine(Color.White.copy(alpha = 0.5f), Offset(cx + sway - r * 0.85f, cy - r * 0.42f),
        Offset(cx + sway - r * 0.2f, cy - r * 0.3f), r * 0.035f)
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        for (i in 0 until 5) {
            val a = i * (PI.toFloat() * 2f / 5f) + if (dancing) t else 0f
            drawCircle(colors.glow, r * 0.23f,
                Offset(cx + sway + cos(a) * r * 0.4f, cy - r * 1f + sin(a) * r * 0.4f))
        }
        drawCircle(colors.light, r * 0.2f, Offset(cx + sway, cy - r * 1f))
    }
    if (stage == GrowthStage.ELDER) {
        drawOval(brush = body, topLeft = Offset(cx - r * 1.18f, cy + r * 0.02f), size = Size(r * 0.8f, r * 0.38f))
        drawOval(brush = body, topLeft = Offset(cx + r * 0.38f, cy - r * 0.12f), size = Size(r * 0.8f, r * 0.38f))
    }
    drawFace(cx + sway * 0.25f, cy + r * 0.72f, r * 0.22f, mood, blink, dancing, colors, t)
}

private fun DrawScope.drawKitty(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, body: Brush, glass: Brush, colors: FriendColors, t: Float
) {
    val hop = if (dancing) sin(t * 4f) * r * 0.14f else 0f
    val tail = if (dancing) sin(t * 2f) * r * 0.36f else r * 0.2f
    drawLine(colors.primary, Offset(cx + r * 0.82f, cy + r * 0.74f),
        Offset(cx + r * 1.38f + tail, cy + r * 0.16f), r * 0.16f)
    drawCircle(brush = body, radius = r * 0.84f, center = Offset(cx, cy - r * 0.16f + hop))
    drawCircle(colors.primary, r * 0.38f, Offset(cx - r * 0.64f, cy - r * 0.74f + hop))
    drawCircle(colors.primary, r * 0.38f, Offset(cx + r * 0.64f, cy - r * 0.74f + hop))
    drawCircle(colors.glow, r * 0.2f, Offset(cx - r * 0.64f, cy - r * 0.76f + hop))
    drawCircle(colors.glow, r * 0.2f, Offset(cx + r * 0.64f, cy - r * 0.76f + hop))
    drawOval(brush = glass, topLeft = Offset(cx - r * 0.6f, cy - r * 0.82f + hop), size = Size(r * 0.5f, r * 0.72f))
    drawFace(cx, cy - r * 0.16f + hop, r * 0.28f, mood, blink, dancing, colors, t)
    for (i in 0 until 2) {
        val side = if (i == 0) -1f else 1f
        drawLine(colors.ink.copy(alpha = 0.6f), Offset(cx + side * r * 0.5f, cy + hop),
            Offset(cx + side * r * 1f, cy - r * 0.08f + hop), r * 0.035f)
        drawLine(colors.ink.copy(alpha = 0.6f), Offset(cx + side * r * 0.5f, cy + r * 0.15f + hop),
            Offset(cx + side * r * 1f, cy + r * 0.24f + hop), r * 0.035f)
    }
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        drawOvalBox(colors.glow, Rect(cx - r * 1f, cy + r * 0.68f + hop, cx - r * 0.1f, cy + r * 1.04f + hop))
        drawOvalBox(colors.glow, Rect(cx + r * 0.1f, cy + r * 0.68f + hop, cx + r * 1f, cy + r * 1.04f + hop))
        drawCircle(colors.primary, r * 0.18f, Offset(cx, cy + r * 0.86f + hop))
    }
    if (stage == GrowthStage.ELDER) {
        for (i in -1..1) drawCircle(colors.light, r * 0.19f, Offset(cx + i * r * 0.44f, cy - r * 1.14f + hop))
    }
}

private fun DrawScope.drawBlobby(
    cx: Float, cy: Float, r: Float, mood: Mood, blink: Boolean, dancing: Boolean,
    stage: GrowthStage, body: Brush, glass: Brush, colors: FriendColors, t: Float
) {
    val wobble = if (dancing) sin(t * 4f) * r * 0.16f else sin(t) * r * 0.045f
    drawOval(brush = body, topLeft = Offset(cx - r - wobble, cy - r * 0.7f),
        size = Size(r * 2f + wobble * 2f, r * 1.85f))
    drawCircle(brush = body, radius = r * 0.74f, center = Offset(cx - r * 0.62f, cy - r * 0.25f))
    drawCircle(brush = body, radius = r * 0.74f, center = Offset(cx + r * 0.62f, cy - r * 0.25f))
    drawOvalBox(colors.secondary, Rect(cx - r * 0.58f, cy + r * 0.95f, cx - r * 0.05f, cy + r * 1.32f))
    drawOvalBox(colors.secondary, Rect(cx + r * 0.05f, cy + r * 0.95f, cx + r * 0.58f, cy + r * 1.32f))
    drawOval(brush = glass, topLeft = Offset(cx - r * 0.6f, cy - r * 0.62f), size = Size(r * 0.5f, r * 0.7f))
    drawFace(cx, cy + r * 0.1f, r * 0.31f, mood, blink, dancing, colors, t)
    if (stage == GrowthStage.GROWN || stage == GrowthStage.ELDER) {
        drawCircle(colors.glow, r * 0.2f, Offset(cx, cy - r * 1f))
        drawCircle(colors.glow, r * 0.14f, Offset(cx - r * 0.3f, cy - r * 0.84f))
        drawCircle(colors.glow, r * 0.14f, Offset(cx + r * 0.3f, cy - r * 0.84f))
    }
    if (stage == GrowthStage.ELDER) {
        drawLine(colors.ink, Offset(cx - r * 0.3f, cy + r * 1f), Offset(cx - r * 0.46f, cy + r * 1.32f), r * 0.05f)
        drawLine(colors.ink, Offset(cx + r * 0.3f, cy + r * 1f), Offset(cx + r * 0.46f, cy + r * 1.32f), r * 0.05f)
    }
}

private fun DrawScope.drawFace(
    cx: Float, cy: Float, gap: Float, mood: Mood, blink: Boolean,
    dancing: Boolean, colors: FriendColors, t: Float
) {
    val ink = colors.ink
    val eyeR = gap * 0.27f
    val left = cx - gap
    val right = cx + gap
    val browTilt = when (mood) {
        Mood.THIRSTY -> gap * 0.22f
        Mood.HAPPY, Mood.DANCING -> -gap * 0.12f
        else -> 0f
    }
    drawLine(ink.copy(alpha = 0.5f), Offset(left - eyeR, cy - eyeR * 2.1f + browTilt),
        Offset(left + eyeR, cy - eyeR * 2.1f - browTilt), eyeR * 0.18f)
    drawLine(ink.copy(alpha = 0.5f), Offset(right - eyeR, cy - eyeR * 2.1f - browTilt),
        Offset(right + eyeR, cy - eyeR * 2.1f + browTilt), eyeR * 0.18f)
    when {
        mood == Mood.SLEEPY && !blink -> {
            drawArcBox(Rect(left - eyeR, cy - eyeR * 0.6f, left + eyeR, cy + eyeR * 0.6f),
                200f, 140f, ink, eyeR * 0.3f)
            drawArcBox(Rect(right - eyeR, cy - eyeR * 0.6f, right + eyeR, cy + eyeR * 0.6f),
                200f, 140f, ink, eyeR * 0.3f)
            for (i in 0 until 3) {
                val zx = cx + gap * 1.6f + i * eyeR * 0.7f
                val zy = cy - eyeR * 2.4f - i * eyeR * 1.2f + sin(t + i) * 2f
                drawLine(colors.glow, Offset(zx, zy), Offset(zx + eyeR, zy), eyeR * 0.16f)
                drawLine(colors.glow, Offset(zx + eyeR, zy), Offset(zx, zy + eyeR), eyeR * 0.16f)
                drawLine(colors.glow, Offset(zx, zy + eyeR), Offset(zx + eyeR, zy + eyeR), eyeR * 0.16f)
            }
        }
        blink -> {
            drawLine(ink, Offset(left - eyeR, cy), Offset(left + eyeR, cy), eyeR * 0.32f)
            drawLine(ink, Offset(right - eyeR, cy), Offset(right + eyeR, cy), eyeR * 0.32f)
        }
        mood == Mood.HAPPY || dancing -> {
            drawCircle(ink, eyeR, Offset(left, cy))
            drawCircle(ink, eyeR, Offset(right, cy))
            for (i in 0 until 2) {
                val ex = if (i == 0) left else right
                drawLine(Color.White, Offset(ex - eyeR * 0.45f, cy - eyeR * 1.7f),
                    Offset(ex + eyeR * 0.45f, cy - eyeR * 1.7f), eyeR * 0.3f)
                drawLine(Color.White, Offset(ex, cy - eyeR * 2.15f),
                    Offset(ex, cy - eyeR * 1.25f), eyeR * 0.3f)
            }
            drawCircle(colors.glow.copy(alpha = 0.55f), gap * 0.34f, Offset(cx - gap * 1.75f, cy + eyeR))
            drawCircle(colors.glow.copy(alpha = 0.55f), gap * 0.34f, Offset(cx + gap * 1.75f, cy + eyeR))
        }
        else -> {
            drawCircle(ink, eyeR, Offset(left, cy))
            drawCircle(ink, eyeR, Offset(right, cy))
            drawCircle(Color.White.copy(alpha = 0.8f), eyeR * 0.3f, Offset(left - eyeR * 0.3f, cy - eyeR * 0.35f))
            drawCircle(Color.White.copy(alpha = 0.8f), eyeR * 0.3f, Offset(right - eyeR * 0.3f, cy - eyeR * 0.35f))
        }
    }
    if (mood == Mood.THIRSTY) {
        drawCircle(colors.secondary, eyeR * 0.35f, Offset(cx + gap * 2f, cy - eyeR * 1.6f))
    }
    val mouthY = cy + gap * 1.25f
    when (mood) {
        Mood.THIRSTY -> drawArcBox(Rect(cx - gap * 0.62f, mouthY + gap * 0.3f, cx + gap * 0.62f, mouthY + gap),
            180f, 180f, ink, eyeR * 0.28f)
        Mood.SLEEPY -> drawLine(ink, Offset(cx - eyeR, mouthY), Offset(cx + eyeR, mouthY), eyeR * 0.32f)
        Mood.HAPPY, Mood.DANCING -> drawOval(ink, topLeft = Offset(cx - gap * 0.55f, mouthY - eyeR * 0.4f),
            size = Size(gap * 1.1f, eyeR * 1.5f))
        else -> drawArcBox(Rect(cx - gap * 0.62f, mouthY - gap * 0.35f, cx + gap * 0.62f, mouthY + gap * 0.35f),
            0f, 180f, ink, eyeR * 0.28f)
    }
}

private fun DrawScope.drawParticles(
    cx: Float, cy: Float, r: Float, colors: FriendColors, phase: Float, enabled: Boolean
) {
    val p = if (enabled) phase % 1f else 0.35f
    for (i in 0 until 18) {
        val seed = (i * 37 % 100) / 100f
        val x = cx + (seed - 0.5f) * r * 3.4f
        val y = cy - r * 1.8f + ((p + seed) % 1f) * r * 3.4f
        val color = when (i % 3) {
            0 -> colors.primary
            1 -> colors.glow
            else -> colors.secondary
        }
        if (i % 2 == 0) drawCircle(color.copy(alpha = 0.85f), r * 0.07f, Offset(x, y))
        else drawRect(color.copy(alpha = 0.75f), Offset(x, y), Size(r * 0.12f, r * 0.18f))
    }
}

private fun DrawScope.drawOvalBox(color: Color, rect: Rect, alpha: Float = 1f) = drawOval(
    color = color, topLeft = Offset(rect.left, rect.top), size = Size(rect.width, rect.height), alpha = alpha
)

private fun DrawScope.drawRoundRectBox(color: Color, rect: Rect, corner: Float, alpha: Float = 1f) =
    drawRoundRect(
        color = color, topLeft = Offset(rect.left, rect.top),
        size = Size(rect.width, rect.height), cornerRadius = CornerRadius(corner, corner), alpha = alpha
    )

private fun DrawScope.drawArcBox(
    rect: Rect, start: Float, sweep: Float, color: Color, strokeWidth: Float
) = drawArc(
    color = color, startAngle = start, sweepAngle = sweep, useCenter = false,
    topLeft = Offset(rect.left, rect.top), size = Size(rect.width, rect.height),
    style = Stroke(width = strokeWidth), alpha = 0.85f
)
