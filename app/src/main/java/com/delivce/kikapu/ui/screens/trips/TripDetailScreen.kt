package com.delivce.kikapu.ui.screens.trips

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.domain.usecase.savingsPercent
import com.delivce.kikapu.ui.components.AnimatedFillButton
import com.delivce.kikapu.ui.components.CatalogSuggestionRow
import com.delivce.kikapu.ui.components.ErrorRed
import com.delivce.kikapu.ui.components.NavigationButton
import com.delivce.kikapu.ui.components.PriorityDots
import com.delivce.kikapu.ui.components.RetroChecklistItem
import com.delivce.kikapu.ui.components.RetroTextField
import com.delivce.kikapu.ui.components.SuccessGreen
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import com.delivce.kikapu.ui.util.formatKes
import kotlin.math.roundToInt

@Composable
fun TripDetailScreen(
    modifier: Modifier = Modifier,
    viewModel: TripDetailViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onTripCancelled: () -> Unit = {},
    onStartCompleteWizard: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editingItemId by remember { mutableStateOf<String?>(null) }
    var priceInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(TripDetailEvent.ClearError)
        }
    }

    // Cancelling has no summary view of its own — just leave.
    LaunchedEffect(uiState.trip?.status) {
        if (uiState.trip?.status == TripStatus.CANCELLED) onTripCancelled()
    }

    val trip = uiState.trip

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.BackgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NavigationButton(isBack = true, onBackClick = onBack)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = (trip?.name ?: "").uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = RetroTheme.TextColor
                    )
                    Text(
                        text = "[ ${trip?.status?.name ?: ""} ]",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Coral
                    )
                }
            }

            if (trip?.status == TripStatus.ACTIVE) {
                Spacer(modifier = Modifier.height(16.dp))

                val budget = trip.budget
                val spent = uiState.spentSoFar
                val progress = if (budget > 0) (spent / budget).coerceIn(0.0, 1.0) else 0.0
                val progressColor = if (progress > 0.8) ErrorRed else AppColors.Coral

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(RetroTheme.BorderColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.toFloat())
                            .height(8.dp)
                            .background(progressColor, RoundedCornerShape(4.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val remainingColor = if (uiState.remainingBudget >= 0) SuccessGreen else ErrorRed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BudgetStat(label = "BUDGET", value = formatKes(budget), color = RetroTheme.TextColor)
                    BudgetStat(label = "SPENT", value = formatKes(spent), color = RetroTheme.TextColor)
                    BudgetStat(label = "REMAINING", value = formatKes(uiState.remainingBudget), color = remainingColor)
                }
            } else if (trip?.status == TripStatus.UPCOMING) {
                // ACTIVE gets the full progress/stats row above; COMPLETED has its own summary
                // card below with budget-vs-spent already in it — this one-liner is just for
                // UPCOMING, before either of those apply.
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "BUDGET: ${formatKes(trip.budget)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
            }
        }

        when (trip?.status) {
            TripStatus.UPCOMING -> UpcomingTripContent(
                items = uiState.items,
                onStartTrip = { viewModel.onEvent(TripDetailEvent.StartTrip) },
                onMarkCompleted = { onStartCompleteWizard(trip.id) }
            )
            TripStatus.ACTIVE -> ActiveTripContent(
                uiState = uiState,
                editingItemId = editingItemId,
                priceInput = priceInput,
                onEditingItemIdChange = { editingItemId = it },
                onPriceInputChange = { priceInput = it },
                onEvent = viewModel::onEvent,
                onComplete = { onStartCompleteWizard(trip!!.id) }
            )
            TripStatus.COMPLETED -> CompletedTripContent(trip = trip, items = uiState.items)
            else -> Unit
        }
    }
}

@Composable
private fun UpcomingTripContent(
    items: List<ShoppingItem>,
    onStartTrip: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "[ ${items.size} ITEMS PLANNED ]",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
            }
            items(items, key = { it.id }) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                        .background(RetroTheme.SurfaceColor, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = item.name.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RetroTheme.TextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            PriorityDots(priority = item.priorityIndex)
                        }
                        Text(
                            text = formatKes(item.estimatedPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = RetroTheme.TextColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedFillButton(
                text = "✓ MARK AS COMPLETED",
                color = SuccessGreen,
                height = 56.dp,
                modifier = Modifier.weight(1f),
                onClick = onMarkCompleted
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor, shape = RoundedCornerShape(12.dp))
                    .background(AppColors.Coral, RoundedCornerShape(12.dp))
                    .clickable { onStartTrip() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶ START TRIP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ActiveTripContent(
    uiState: TripDetailUiState,
    editingItemId: String?,
    priceInput: String,
    onEditingItemIdChange: (String?) -> Unit,
    onPriceInputChange: (String) -> Unit,
    onEvent: (TripDetailEvent) -> Unit,
    onComplete: () -> Unit
) {
    val checkedCount = uiState.items.count { it.isChecked }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "[ $checkedCount/${uiState.items.size} CHECKED ]",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
            }

            items(uiState.items, key = { it.id }) { item ->
                RetroChecklistItem(
                    item = item,
                    onToggle = { onEvent(TripDetailEvent.ToggleItem(item)) },
                    onPriceEditStart = {
                        onEditingItemIdChange(item.id)
                        onPriceInputChange(if (item.actualPrice > 0) item.actualPrice.toString() else "")
                    },
                    isEditingPrice = editingItemId == item.id,
                    priceInput = priceInput,
                    onPriceInputChange = onPriceInputChange,
                    onPriceCommit = {
                        val price = priceInput.toDoubleOrNull() ?: item.actualPrice
                        onEvent(TripDetailEvent.UpdateItemPrice(item, price))
                        onEditingItemIdChange(null)
                    }
                )
            }

            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RetroTextField(
                            value = uiState.newItemName,
                            onValueChange = { onEvent(TripDetailEvent.NewItemNameChanged(it)) },
                            placeholder = "Item name",
                            modifier = Modifier.weight(1f)
                        )
                        RetroTextField(
                            value = uiState.newItemPrice,
                            onValueChange = { onEvent(TripDetailEvent.NewItemPriceChanged(it)) },
                            placeholder = "Price",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(110.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor, shape = RoundedCornerShape(10.dp))
                                .background(AppColors.Coral, RoundedCornerShape(10.dp))
                                .clickable { onEvent(TripDetailEvent.AddOutOfBudgetItem) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                    if (uiState.selectedCatalogItemId != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "From your items · priority",
                                style = MaterialTheme.typography.labelSmall,
                                color = RetroTheme.TextColor.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            PriorityDots(priority = uiState.newItemPriorityIndex)
                        }
                    } else {
                        val suggestions = remember(uiState.newItemName, uiState.catalogItems) {
                            if (uiState.newItemName.isBlank()) {
                                emptyList()
                            } else {
                                uiState.catalogItems
                                    .filter { it.name.contains(uiState.newItemName, ignoreCase = true) }
                                    .take(4)
                            }
                        }
                        if (suggestions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                suggestions.forEach { suggestion ->
                                    CatalogSuggestionRow(
                                        item = suggestion,
                                        onClick = { onEvent(TripDetailEvent.SelectCatalogSuggestion(suggestion)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedFillButton(
                text = "CANCEL TRIP",
                color = ErrorRed,
                height = 56.dp,
                modifier = Modifier.weight(1f),
                onClick = { onEvent(TripDetailEvent.CancelTrip) }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor, shape = RoundedCornerShape(12.dp))
                    .background(SuccessGreen, RoundedCornerShape(12.dp))
                    .clickable { onComplete() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓ COMPLETE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CompletedTripContent(trip: com.delivce.kikapu.domain.model.Trip, items: List<ShoppingItem>) {
    val bought = items.filter { it.isChecked }
    val difference = trip.budget - trip.totalSpent
    val isSaved = difference >= 0
    val percent = trip.savingsPercent()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                    .background(if (isSaved) SuccessGreen else ErrorRed, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${if (isSaved) "+" else "-"}${formatKes(kotlin.math.abs(difference))}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = if (isSaved) "SAVED" else "OVER BUDGET",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    if (percent != null) {
                        Text(
                            text = "${if (isSaved) "+" else ""}${percent.roundToInt()}% vs. budget",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                BudgetStat(label = "BUDGET", value = formatKes(trip.budget), color = RetroTheme.TextColor)
                BudgetStat(label = "SPENT", value = formatKes(trip.totalSpent), color = RetroTheme.TextColor)
                BudgetStat(label = "ITEMS", value = bought.size.toString(), color = RetroTheme.TextColor)
            }
        }
        item {
            Text(
                text = "[ WHAT YOU BOUGHT ]",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )
        }
        items(bought, key = { it.id }) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                    .background(RetroTheme.SurfaceColor, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RetroTheme.TextColor
                    )
                    Text(
                        text = formatKes(item.actualPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = RetroTheme.TextColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetStat(label: String, value: String, color: Color) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = RetroTheme.TextColor.copy(alpha = 0.5f)
        )
    }
}
