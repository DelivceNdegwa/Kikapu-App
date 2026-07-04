package com.delivce.kikapu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import com.delivce.kikapu.ui.util.formatKes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Re-exported from AppColors so existing call sites keep working with one canonical source.
val SuccessGreen = AppColors.SuccessGreen
val ErrorRed = AppColors.ErrorRed
val InfoBlue = AppColors.InfoBlue

fun TripStatus.toColor(): Color = when (this) {
    TripStatus.UPCOMING -> InfoBlue
    TripStatus.ACTIVE -> AppColors.Amber
    TripStatus.COMPLETED -> SuccessGreen
    TripStatus.CANCELLED -> ErrorRed
}

@Composable
fun RetroStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor
            )
            .background(backgroundColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .padding(16.dp)
    ) {
        Column {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun RetroSavingsCard(amount: Double, modifier: Modifier = Modifier) {
    val isSaved = amount >= 0
    val backgroundColor = if (isSaved) SuccessGreen else ErrorRed
    val label = if (isSaved) "SAVED" else "OVERSPENT"
    val sign = if (isSaved) "+" else "-"

    Box(
        modifier = modifier
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor
            )
            .background(backgroundColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .padding(16.dp)
    ) {
        Column {
            Icon(
                imageVector = Icons.Filled.Savings,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$sign${formatKes(kotlin.math.abs(amount))}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun StatusBadge(status: TripStatus, modifier: Modifier = Modifier) {
    val color = status.toColor()
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun TripRowCard(trip: Trip, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor
            )
            .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = trip.name.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormatter.format(Date(trip.date)),
                    style = MaterialTheme.typography.labelSmall,
                    color = RetroTheme.TextColor.copy(alpha = 0.5f)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatKes(trip.budget),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(status = trip.status)
                if (trip.status == TripStatus.COMPLETED) {
                    val difference = trip.budget - trip.totalSpent
                    val isSaved = difference >= 0
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${if (isSaved) "+" else "-"}${formatKes(kotlin.math.abs(difference))} ${if (isSaved) "SAVED" else "OVER"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSaved) SuccessGreen else ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
fun RetroChecklistItem(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onPriceEditStart: () -> Unit,
    isEditingPrice: Boolean,
    priceInput: String,
    onPriceInputChange: (String) -> Unit,
    onPriceCommit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (item.isOutOfBudget) ErrorRed else RetroTheme.BorderColor

    Box(
        modifier = modifier
            .fillMaxWidth()
            .retroFrame(
                borderColor = borderColor,
                shadowColor = RetroTheme.ShadowColor
            )
            .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .retroFrame(
                        borderColor = RetroTheme.BorderColor,
                        shadowColor = RetroTheme.ShadowColor,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .background(
                        if (item.isChecked) AppColors.Coral else RetroTheme.BackgroundColor,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) {
                    Text(text = "✓", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.uppercase(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                    color = if (item.isChecked) RetroTheme.TextColor.copy(alpha = 0.4f) else RetroTheme.TextColor
                )
                if (item.isOutOfBudget) {
                    Text(
                        text = "⚠ OVER BUDGET",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isEditingPrice) {
                val focusRequester = remember { FocusRequester() }
                androidx.compose.foundation.text.BasicTextField(
                    value = priceInput,
                    onValueChange = onPriceInputChange,
                    modifier = Modifier
                        .width(72.dp)
                        .focusRequester(focusRequester)
                        // Committing only on the keyboard's Done action meant tapping away to
                        // edit another item (or anywhere else) silently discarded the typed
                        // price. Committing on focus loss too makes that impossible to lose.
                        .onFocusChanged { focusState -> if (!focusState.isFocused) onPriceCommit() },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = RetroTheme.TextColor),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = { onPriceCommit() }
                    )
                )
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            } else {
                Text(
                    // Always the actual price, not a checked/unchecked-dependent fallback —
                    // actualPrice is initialized to match estimatedPrice at creation, so this is
                    // never blank, and an edit here must be visible regardless of checked state.
                    text = formatKes(item.actualPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor,
                    modifier = Modifier.clickable { onPriceEditStart() }
                )
            }
        }
    }
}
