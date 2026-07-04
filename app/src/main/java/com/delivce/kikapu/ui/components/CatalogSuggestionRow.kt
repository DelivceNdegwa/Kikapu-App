package com.delivce.kikapu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.util.formatKes

/**
 * A tappable catalog-item suggestion shown under a "name" text field while adding an item —
 * picking one prefills price and priority instead of the field falling through to a brand-new
 * custom item. Shared by the active-trip add-item flow and the completion wizard's "bought
 * something extra" step so both behave identically.
 */
@Composable
fun CatalogSuggestionRow(item: Item, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor, shape = RoundedCornerShape(10.dp), thickness = 1.5.dp)
            .background(RetroTheme.SurfaceColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor
            )
            PriorityDots(priority = item.priorityIndex)
        }
        Text(
            text = formatKes(item.estimatedPrice),
            style = MaterialTheme.typography.labelSmall,
            color = RetroTheme.TextColor.copy(alpha = 0.6f)
        )
    }
}
