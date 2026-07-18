package com.example.simmr.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrSpacing
import com.example.simmr.core.designsystem.theme.SimmrTextStyles
import kotlinx.coroutines.delay

@Composable
internal fun RecipeGeneratingOverlay(modifier: Modifier = Modifier) {
    var messageIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(GeneratingAnimationConstants.MESSAGE_INTERVAL_MS)
            messageIndex = (messageIndex + 1) % GeneratingCopy.MESSAGES.size
        }
    }

    Box(
        modifier = modifier.fillMaxSize().background(SimmrColors.CreamBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Lg),
        ) {
            SimmeringPotAnimation(Modifier.size(160.dp))
            AnimatedContent(
                targetState = messageIndex,
                transitionSpec = {
                    fadeIn(tween(GeneratingAnimationConstants.MESSAGE_FADE_MS)) togetherWith
                        fadeOut(tween(GeneratingAnimationConstants.MESSAGE_FADE_MS))
                },
                label = "generating-message",
            ) { index ->
                Text(
                    text = GeneratingCopy.MESSAGES[index],
                    style = SimmrTextStyles.Headline,
                    color = SimmrColors.TextDark,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SimmeringPotAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "simmering-pot")
    val breathing by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(GeneratingAnimationConstants.BREATH_MS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pot-breathing",
    )
    val lidRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = GeneratingAnimationConstants.LID_CYCLE_MS
                -3f at 90
                3f at 180
                -2.5f at 270
                2.5f at 360
                -1.5f at 450
                0f at 570
            },
        ),
        label = "lid-rattle",
    )
    val steamProgress = List(GeneratingAnimationConstants.STEAM_LANES.size) { index ->
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(GeneratingAnimationConstants.STEAM_CYCLE_MS, easing = LinearEasing),
                initialStartOffset = StartOffset(
                    offsetMillis = index * GeneratingAnimationConstants.STEAM_STAGGER_MS,
                    offsetType = StartOffsetType.Delay,
                ),
            ),
            label = "steam-$index",
        ).value
    }

    Canvas(modifier) {
        val centerX = size.width / 2f
        val centerY = size.height * .62f
        steamProgress.forEachIndexed { index, progress ->
            val alpha = when {
                progress < .2f -> progress / .2f * .85f
                progress < .65f -> .85f
                else -> ((1f - progress) / .35f).coerceIn(0f, 1f) * .85f
            }
            val x = centerX + GeneratingAnimationConstants.STEAM_LANES[index] * density
            val baseY = centerY - 58.dp.toPx() - progress * 20.dp.toPx()
            val width = 12.dp.toPx()
            val height = 34.dp.toPx()
            val path = Path().apply {
                moveTo(x, baseY + height)
                cubicTo(x - width / 2, baseY + height * .85f, x + width / 2, baseY + height * .7f, x, baseY + height * .55f)
                cubicTo(x - width / 2, baseY + height * .4f, x + width / 2, baseY + height * .15f, x, baseY)
            }
            drawPath(path, GeneratingColors.LightCoral.copy(alpha = alpha), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
        }

        val potWidth = 104.dp.toPx()
        val potHeight = 60.dp.toPx()
        val potTopLeft = Offset(centerX - potWidth / 2, centerY - potHeight / 2)
        drawRoundRect(
            color = SimmrColors.Coral,
            topLeft = Offset(centerX - 64.dp.toPx(), centerY - 5.dp.toPx()),
            size = Size(20.dp.toPx(), 10.dp.toPx()),
            cornerRadius = CornerRadius(5.dp.toPx()),
        )
        drawRoundRect(
            color = SimmrColors.Coral,
            topLeft = Offset(centerX + 44.dp.toPx(), centerY - 5.dp.toPx()),
            size = Size(20.dp.toPx(), 10.dp.toPx()),
            cornerRadius = CornerRadius(5.dp.toPx()),
        )
        scale(breathing, pivot = Offset(centerX, centerY)) {
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(SimmrColors.Coral, GeneratingColors.LightCoral)),
                topLeft = potTopLeft,
                size = Size(potWidth, potHeight),
                cornerRadius = CornerRadius(18.dp.toPx()),
            )
        }
        rotate(lidRotation, pivot = Offset(centerX, centerY - 28.dp.toPx())) {
            drawRoundRect(
                color = GeneratingColors.LightCoral,
                topLeft = Offset(centerX - 56.dp.toPx(), centerY - 33.dp.toPx()),
                size = Size(112.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(5.dp.toPx()),
            )
            drawCircle(SimmrColors.Coral, radius = 6.5.dp.toPx(), center = Offset(centerX, centerY - 41.dp.toPx()))
        }
    }
}

private object GeneratingCopy {
    val MESSAGES = listOf(
        "Analysing your recipe…",
        "Breaking down the steps…",
        "Estimating quantities…",
        "Optimising the cooking order…",
        "Personalising to your taste…",
        "Almost ready…",
    )
}

private object GeneratingAnimationConstants {
    const val MESSAGE_INTERVAL_MS = 1_800L
    const val MESSAGE_FADE_MS = 300
    const val BREATH_MS = 1_300
    const val LID_CYCLE_MS = 2_070
    const val STEAM_CYCLE_MS = 1_800
    const val STEAM_STAGGER_MS = 500
    val STEAM_LANES = listOf(-29f, 0f, 29f)
}

private object GeneratingColors {
    val LightCoral = Color(0xFFFFAD9B)
}
