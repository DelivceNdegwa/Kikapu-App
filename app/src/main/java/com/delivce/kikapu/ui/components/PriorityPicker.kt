package com.delivce.kikapu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors

/** 1-5 priority picker, higher = more important. Shared by the trip and item item-entry forms. */
@Composable
fun PriorityPicker(
    priority: Int,
    onPriorityChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (level in 1..5) {
            val isSelected = level <= priority
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .retroFrame(
                        borderColor = RetroTheme.BorderColor,
                        shadowColor = RetroTheme.ShadowColor,
                        shape = RoundedCornerShape(6.dp),
                        thickness = 1.5.dp
                    )
                    .background(
                        if (isSelected) AppColors.Coral else RetroTheme.BackgroundColor,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onPriorityChanged(level) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = level.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else RetroTheme.TextColor
                )
            }
        }
    }
}

/** Compact read-only rendering of a 1-5 priority as filled/outline dots, for list rows. */
@Composable
fun PriorityDots(priority: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (level in 1..5) {
            val isFilled = level <= priority
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        if (isFilled) AppColors.Coral else RetroTheme.BorderColor.copy(alpha = 0.2f),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}
