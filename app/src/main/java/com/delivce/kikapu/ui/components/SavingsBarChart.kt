package com.delivce.kikapu.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.usecase.savingsPercent
import com.delivce.kikapu.ui.foundation.RetroTheme
import kotlin.math.abs

/**
 * A diverging bar chart — this is the recommended form for "above/below a baseline" data (per
 * the choosing-a-form guide: polarity data gets a diverging bar, not a line or pie). Each bar is
 * one trip's savings %, green above the zero line / red below it. Legend + a "0%" baseline label
 * make the polarity legible without guessing; tapping a bar selects it (there's no hover on
 * touch), and the screen hosting this shows the tapped trip's exact numbers below — so every
 * value stays reachable, not just implied by bar height.
 */
@Composable
fun SavingsBarChart(
    trips: List<Trip>,
    selectedTripId: String?,
    onBarSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val chronological = remember(trips) { trips.reversed() }
    val positiveColor = SuccessGreen
    val negativeColor = ErrorRed
    val borderColor = RetroTheme.BorderColor
    val axisColor = borderColor.copy(alpha = 0.3f)

    // Bars grow up from the baseline each time the underlying trip window changes (including on
    // first appearance) — a small "the data just landed" cue rather than a static, inert chart.
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
            val zeroY = size.height / 2f
            drawLine(
                color = axisColor,
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 2f
            )
            if (chronological.isEmpty()) return@Canvas

            val percentages = chronological.map { it.savingsPercent() ?: 0.0 }
            val maxAbs = percentages.maxOf { abs(it) }.coerceAtLeast(10.0)
            val slot = size.width / chronological.size
            val barWidth = (slot * 0.55f).coerceAtLeast(6f)
            val halfHeight = zeroY - 6f

            chronological.forEachIndexed { index, trip ->
                val percent = percentages[index]
                val barHeight = (abs(percent) / maxAbs * halfHeight * growth.value).toFloat().coerceAtLeast(3f)
                val x = index * slot + (slot - barWidth) / 2f
                val top = if (percent >= 0) zeroY - barHeight else zeroY
                val baseColor = if (percent >= 0) positiveColor else negativeColor
                val isSelected = trip.id == selectedTripId

                drawRoundRect(
                    color = if (isSelected) baseColor else baseColor.copy(alpha = 0.5f),
                    topLeft = Offset(x, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                if (isSelected) {
                    drawRoundRect(
                        color = borderColor,
                        topLeft = Offset(x, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4f, 4f),
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
        Text(
            text = "0%",
            style = MaterialTheme.typography.labelSmall,
            color = RetroTheme.TextColor.copy(alpha = 0.4f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 2.dp)
        )
    }
}

@Composable
fun ChartLegend(items: List<Pair<Color, String>>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { (color, label) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
