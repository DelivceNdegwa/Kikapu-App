package com.delivce.kikapu.ui.screens.progress

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.usecase.savingsPercent
import com.delivce.kikapu.ui.components.AnimatedFillButton
import com.delivce.kikapu.ui.components.BudgetVsSpentChart
import com.delivce.kikapu.ui.components.ChartLegend
import com.delivce.kikapu.ui.components.EmptyStateIllustration
import com.delivce.kikapu.ui.components.ErrorRed
import com.delivce.kikapu.ui.components.PopInItem
import com.delivce.kikapu.ui.components.SavingsBarChart
import com.delivce.kikapu.ui.components.SuccessGreen
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import com.delivce.kikapu.ui.util.formatKes
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showRangeDialog by remember { mutableStateOf(false) }
    var showComparisonDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.BackgroundColor)
    ) {
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Coral)
                }
            }
            uiState.completedTrips.isEmpty() -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Header()
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        EmptyStateIllustration(caption = "[ COMPLETE YOUR FIRST TRIP TO START A STREAK ]")
                    }
                }
            }
            else -> {
                val windowed = uiState.windowedTrips()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    Header()
                    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PopInItem(index = 0) { StreakCard(streak = uiState.streak, streakTrips = uiState.streakTrips) }
                        PopInItem(index = 1) {
                            ImprovementCard(
                                improvementPercent = uiState.improvementPercent,
                                onClick = { if (uiState.completedTrips.size >= 2) showComparisonDialog = true }
                            )
                        }
                        PopInItem(index = 2) {
                            GoalCard(successRate = uiState.successRate, target = uiState.successRateTarget)
                        }

                        Text(
                            text = "SAVINGS TREND",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = RetroTheme.TextColor.copy(alpha = 0.6f)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ChartWindow.presets.forEach { window ->
                                FilterPill(
                                    label = window.label,
                                    selected = uiState.chartWindow == window,
                                    onClick = { viewModel.onEvent(ProgressEvent.WindowChanged(window)) }
                                )
                            }
                            FilterPill(
                                label = "CUSTOM",
                                selected = uiState.chartWindow is ChartWindow.Custom,
                                icon = Icons.Filled.DateRange,
                                onClick = { showRangeDialog = true }
                            )
                            OutcomeFilter.entries.forEach { outcome ->
                                FilterPill(
                                    label = outcome.label,
                                    selected = uiState.outcomeFilter == outcome,
                                    onClick = { viewModel.onEvent(ProgressEvent.OutcomeFilterChanged(outcome)) }
                                )
                            }
                        }

                        if (windowed.isEmpty()) {
                            Text(
                                text = "No completed trips in this range.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RetroTheme.TextColor.copy(alpha = 0.6f)
                            )
                        } else {
                            PopInItem(index = 3) {
                                ChartCard {
                                    SavingsBarChart(
                                        trips = windowed,
                                        selectedTripId = uiState.selectedTripId,
                                        onBarSelected = { viewModel.onEvent(ProgressEvent.TripSelected(it)) }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    ChartLegend(listOf(SuccessGreen to "SAVED", ErrorRed to "OVERSPENT"))
                                }
                            }

                            val selectedTrip = uiState.completedTrips.find { it.id == uiState.selectedTripId }
                            if (selectedTrip != null) {
                                PopInItem(index = 4) { SelectedTripDetail(trip = selectedTrip) }
                            }

                            Text(
                                text = "BUDGET VS. SPENT",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = RetroTheme.TextColor.copy(alpha = 0.6f)
                            )
                            PopInItem(index = 5) {
                                ChartCard {
                                    BudgetVsSpentChart(
                                        trips = windowed,
                                        selectedTripId = uiState.selectedTripId,
                                        onBarSelected = { viewModel.onEvent(ProgressEvent.TripSelected(it)) }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    ChartLegend(listOf(AppColors.InfoBlue to "BUDGET", AppColors.Coral to "SPENT"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRangeDialog) {
        CustomRangeDialog(
            onDismiss = { showRangeDialog = false },
            onConfirm = { start, end ->
                viewModel.onEvent(ProgressEvent.WindowChanged(ChartWindow.Custom(start, end)))
                showRangeDialog = false
            }
        )
    }

    if (showComparisonDialog) {
        val latest = uiState.completedTrips.getOrNull(0)
        val previous = uiState.completedTrips.getOrNull(1)
        if (latest != null && previous != null) {
            TripComparisonDialog(current = latest, previous = previous, onDismiss = { showComparisonDialog = false })
        }
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Progress",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = RetroTheme.TextColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your shopping trends, at a glance",
            style = MaterialTheme.typography.labelMedium,
            color = RetroTheme.TextColor.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ChartCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
            .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .padding(16.dp)
    ) {
        Column { content() }
    }
}

@Composable
private fun SelectedTripDetail(trip: Trip) {
    val difference = trip.budget - trip.totalSpent
    val isSaved = difference >= 0
    val percent = trip.savingsPercent()
    val animatedPercent by animateIntAsState(
        targetValue = percent?.roundToInt() ?: 0,
        animationSpec = tween(500),
        label = "selectedTripPercent"
    )
    // Stacked, not side-by-side: a long trip name and a big percent number were fighting for the
    // same row and the percent kept losing. Full card width for each now, name free to wrap onto
    // as many lines as it needs before the numbers.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
            .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = trip.name.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor
            )
            Text(
                text = "${formatKes(trip.budget)} budget · ${formatKes(trip.totalSpent)} spent",
                style = MaterialTheme.typography.labelSmall,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSaved) "SAVED" else "OVERSPENT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.5f)
                )
                Text(
                    text = if (percent != null) "${if (isSaved) "+" else ""}$animatedPercent%" else "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (isSaved) SuccessGreen else ErrorRed
                )
            }
        }
    }
}

@Composable
private fun StreakCard(streak: Int, streakTrips: List<Trip>) {
    // The hotter the streak, the hotter the flame — a dim outline at 0, warming through amber and
    // into coral the longer it runs, plus a slow breathing pulse so it never looks static (the
    // same "alive" treatment Duolingo gives its flame icon).
    val cardColor by animateColorAsState(
        targetValue = when {
            streak <= 0 -> RetroTheme.TextColor.copy(alpha = 0.25f)
            streak < 5 -> AppColors.Amber
            else -> AppColors.Coral
        },
        animationSpec = tween(500),
        label = "streakCardColor"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "flamePulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (streak > 0) 1.12f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )
    val animatedStreak by animateIntAsState(targetValue = streak, animationSpec = tween(700), label = "streakCount")
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
            .background(cardColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .clickable(enabled = streakTrips.isNotEmpty()) { expanded = !expanded }
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .width(40.dp)
                        .graphicsLayer {
                            scaleX = flameScale
                            scaleY = flameScale
                        }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (streak > 0) "$animatedStreak TRIP STREAK" else "NO STREAK YET",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = if (streak > 0) "Trips in a row within budget — tap to see them" else "Stay within budget on your next trip to start one",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                if (streakTrips.isNotEmpty()) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
            if (expanded && streakTrips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Newest first in state; show oldest-to-newest so the streak reads top-to-bottom
                    // the way it was built, most recent trip landing last.
                    streakTrips.reversed().forEach { trip -> StreakTripRow(trip = trip) }
                }
            }
        }
    }
}

@Composable
private fun StreakTripRow(trip: Trip) {
    val percent = trip.savingsPercent()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = trip.name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (percent != null) "+${percent.roundToInt()}%" else "—",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}

@Composable
private fun ImprovementCard(improvementPercent: Double?, onClick: () -> Unit) {
    val (headline, subtext, color) = when {
        improvementPercent == null -> Triple(
            "Keep going!",
            "Complete one more trip to see how you're trending",
            AppColors.Teal
        )
        improvementPercent >= 0 -> Triple(
            "▲ ${improvementPercent.roundToInt()}% better than last time!",
            "Tap to compare — you're on a roll.",
            SuccessGreen
        )
        else -> Triple(
            "▼ ${(-improvementPercent).roundToInt()}% down from last time",
            "Tap to compare — let's bounce back.",
            ErrorRed
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
            .background(color, RoundedCornerShape(RetroDefaults.CornerRadius))
            .clickable(enabled = improvementPercent != null, onClick = onClick)
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun GoalCard(successRate: Int, target: Int) {
    val met = successRate >= target
    val barColor by animateColorAsState(
        targetValue = if (met) SuccessGreen else AppColors.Amber,
        animationSpec = tween(500),
        label = "goalBarColor"
    )
    val animatedFraction by animateFloatAsState(
        targetValue = (successRate / 100f).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "goalFraction"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
            .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .padding(16.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "SUCCESS RATE GOAL",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
                Text(
                    text = "$successRate% of $target% target",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (met) SuccessGreen else RetroTheme.TextColor.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(RetroTheme.BorderColor.copy(alpha = 0.12f), RoundedCornerShape(7.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedFraction)
                        .fillMaxHeight()
                        .background(barColor, RoundedCornerShape(7.dp))
                )
                // Target marker — a thin line at the goal percentage so "where you are" and
                // "where you want to be" are both visible on the same bar.
                Box(
                    modifier = Modifier
                        .offset(x = maxWidth * (target / 100f) - 1.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(RetroTheme.TextColor.copy(alpha = 0.5f))
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (met) {
                    "Goal met — keep it up!"
                } else {
                    "${target - successRate}% to go to hit your goal"
                },
                style = MaterialTheme.typography.labelSmall,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Box(
        modifier = Modifier
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor,
                shape = RoundedCornerShape(10.dp),
                thickness = 1.5.dp
            )
            .background(
                if (selected) AppColors.Coral else RetroTheme.SurfaceColor,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else RetroTheme.TextColor,
                    modifier = Modifier.width(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else RetroTheme.TextColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRangeDialog(onDismiss: () -> Unit, onConfirm: (Long, Long) -> Unit) {
    val state = rememberDateRangePickerState()
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
        ) {
            DateRangePicker(state = state, modifier = Modifier.weight(1f, fill = false))
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedFillButton(
                    text = "CANCEL",
                    color = RetroTheme.TextColor,
                    modifier = Modifier.weight(1f),
                    onClick = onDismiss
                )
                AnimatedFillButton(
                    text = "APPLY",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val start = state.selectedStartDateMillis
                        val end = state.selectedEndDateMillis
                        if (start != null && end != null) onConfirm(start, end)
                    }
                )
            }
        }
    }
}

@Composable
private fun TripComparisonDialog(current: Trip, previous: Trip, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "LAST TRIP VS. THIS TRIP",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = AppColors.Coral
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TripComparisonColumn(label = "LAST", trip = previous)
                TripComparisonColumn(label = "THIS TIME", trip = current)
            }
            AnimatedFillButton(text = "CLOSE", modifier = Modifier.fillMaxWidth(), onClick = onDismiss)
        }
    }
}

@Composable
private fun TripComparisonColumn(label: String, trip: Trip) {
    val difference = trip.budget - trip.totalSpent
    val isSaved = difference >= 0
    val percent = trip.savingsPercent()
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.TextColor.copy(alpha = 0.5f)
        )
        Text(
            text = trip.name.uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.TextColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Budget: ${formatKes(trip.budget)}", style = MaterialTheme.typography.labelSmall, color = RetroTheme.TextColor.copy(alpha = 0.7f))
        Text(text = "Spent: ${formatKes(trip.totalSpent)}", style = MaterialTheme.typography.labelSmall, color = RetroTheme.TextColor.copy(alpha = 0.7f))
        Text(
            text = "${if (isSaved) "+" else "-"}${formatKes(kotlin.math.abs(difference))}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = if (isSaved) SuccessGreen else ErrorRed
        )
        if (percent != null) {
            Text(
                text = "${if (isSaved) "+" else ""}${percent.roundToInt()}% vs. budget",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSaved) SuccessGreen else ErrorRed
            )
        }
    }
}
