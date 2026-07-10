package com.delivce.kikapu.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Wraps [content] in a staggered scale+fade entrance — a small springy overshoot rather than a
 * flat fade, the same "pop" popular gamification apps (Duolingo, Headspace) use for cards and
 * badges appearing on screen load. Plays once per composition; pass an increasing [index] for a
 * cascading stagger across a list of cards.
 */
@Composable
fun PopInItem(
    index: Int = 0,
    staggerMillis: Int = 70,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay((index * staggerMillis).toLong())
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch { alpha.animateTo(1f, animationSpec = tween(220)) }
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
            this.alpha = alpha.value
        }
    ) {
        content()
    }
}
