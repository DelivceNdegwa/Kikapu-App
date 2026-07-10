package com.delivce.kikapu.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.theme.AppColors

/**
 * A grouped bar chart — the recommended form for "tell two distinct series apart" (per the
 * choosing-a-form guide, that's categorical, not diverging: budget and spent aren't two poles of
 * one value, they're two separately-measured quantities). Reading the gap between the two bars is
 * a more literal way to see saved/overspent than a percentage, so this is offered alongside the
 * savings % chart rather than instead of it.
 */
@Composable
fun BudgetVsSpentChart(
    trips: List<Trip>,
    selectedTripId: String?,
    onBarSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chronological = remember(trips) { trips.reversed() }
    val budgetColor = AppColors.InfoBlue
    val spentColor = AppColors.Coral
    val borderColor = RetroTheme.BorderColor

    val growth = remember { Animatable(0f) }
    LaunchedEffect(chronological) {
        growth.snapTo(0f)
        growth.animateTo(1f, animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing))
    }

    Box(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(chronological) {
                    detectTapGestures { offset ->
                        if (chronological.isEmpty()) return@detectTapGestures
                        val slot = size.width / chronological.size.toFloat()
                        val index = (offset.x / slot).toInt().coerceIn(0, chronological.size - 1)
                        onBarSelected(chronological[index].id)
                    }
                }
        ) {
            if (chronological.isEmpty()) return@Canvas

            val maxValue = chronological
                .maxOf { maxOf(it.budget, it.totalSpent) }
                .coerceAtLeast(1.0)
            val slot = size.width / chronological.size
            val groupWidth = slot * 0.7f
            val barWidth = (groupWidth / 2f - 2f).coerceAtLeast(4f)
            val chartHeight = size.height - 4f

            chronological.forEachIndexed { index, trip ->
                val baseX = index * slot + (slot - groupWidth) / 2f
                val isSelected = trip.id == selectedTripId
                val alpha = if (isSelected) 1f else 0.55f

                val budgetHeight = (trip.budget / maxValue * chartHeight * growth.value).toFloat().coerceAtLeast(3f)
                drawRoundRect(
                    color = budgetColor.copy(alpha = alpha),
                    topLeft = Offset(baseX, chartHeight - budgetHeight),
                    size = Size(barWidth, budgetHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                val spentHeight = (trip.totalSpent / maxValue * chartHeight * growth.value).toFloat().coerceAtLeast(3f)
                val spentX = baseX + barWidth + 4f
                drawRoundRect(
                    color = spentColor.copy(alpha = alpha),
                    topLeft = Offset(spentX, chartHeight - spentHeight),
                    size = Size(barWidth, spentHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                if (isSelected) {
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(baseX, 0f),
                        size = Size(barWidth * 2 + 4f, chartHeight),
                        cornerRadius = CornerRadius(4f, 4f),
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}
