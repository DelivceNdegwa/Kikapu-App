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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.ui.components.AnimatedFillButton
import com.delivce.kikapu.ui.components.CatalogSuggestionRow
import com.delivce.kikapu.ui.components.NavigationButton
import com.delivce.kikapu.ui.components.PriorityDots
import com.delivce.kikapu.ui.components.RetroTextField
import com.delivce.kikapu.ui.components.SuccessOverlay
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import com.delivce.kikapu.ui.util.formatKes

private const val STAGE_COUNT = 3

@Composable
fun CompleteTripScreen(
    modifier: Modifier = Modifier,
    viewModel: CompleteTripViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onCompleted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CompleteTripEvent.ClearError)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(RetroTheme.BackgroundColor)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationButton(isBack = true, onBackClick = onBack)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = "COMPLETE TRIP",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = RetroTheme.TextColor
                    )
                    Text(
                        text = "STEP ${uiState.step + 1}/$STAGE_COUNT",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Coral,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Coral)
                }
            } else {
                when (uiState.step) {
                    0 -> WhatDidYouBuyStep(uiState = uiState, onEvent = viewModel::onEvent, modifier = Modifier.weight(1f))
                    1 -> PriceChangesStep(uiState = uiState, onEvent = viewModel::onEvent, modifier = Modifier.weight(1f))
                    else -> ConfirmStep(uiState = uiState, modifier = Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.step > 0) {
                        AnimatedFillButton(
                            text = "‹ BACK",
                            color = RetroTheme.TextColor,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.onEvent(CompleteTripEvent.PreviousStep) }
                        )
                    }
                    if (uiState.step < STAGE_COUNT - 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor, shape = RoundedCornerShape(10.dp))
                                .background(AppColors.Coral, RoundedCornerShape(10.dp))
                                .clickable { viewModel.onEvent(CompleteTripEvent.NextStep) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NEXT →",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor, shape = RoundedCornerShape(10.dp))
                                .background(com.delivce.kikapu.ui.components.SuccessGreen, RoundedCornerShape(10.dp))
                                .clickable(enabled = !uiState.isSaving) { viewModel.onEvent(CompleteTripEvent.Confirm) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            } else {
                                Text(
                                    text = "✓ CONFIRM",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.isDone) {
            SuccessOverlay(onFinished = onCompleted)
        }
    }
}

@Composable
private fun WhatDidYouBuyStep(uiState: CompleteTripUiState, onEvent: (CompleteTripEvent) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "WHAT DID YOU BUY?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = AppColors.Coral
            )
        }
        items(uiState.items, key = { it.id }) { item ->
            WizardChecklistRow(item = item, onToggle = { onEvent(CompleteTripEvent.ToggleItem(item.id)) })
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                    .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "BOUGHT SOMETHING EXTRA?",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RetroTextField(
                        value = uiState.newItemName,
                        onValueChange = { onEvent(CompleteTripEvent.NewItemNameChanged(it)) },
                        placeholder = "Item name",
                        modifier = Modifier.weight(1f)
                    )
                    RetroTextField(
                        value = uiState.newItemPrice,
                        onValueChange = { onEvent(CompleteTripEvent.NewItemPriceChanged(it)) },
                        placeholder = "Price",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (uiState.selectedCatalogItemId != null) {
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
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            suggestions.forEach { suggestion ->
                                CatalogSuggestionRow(
                                    item = suggestion,
                                    onClick = { onEvent(CompleteTripEvent.SelectCatalogSuggestion(suggestion)) }
                                )
                            }
                        }
                    }
                }
                AnimatedFillButton(
                    text = "+ ADD ITEM",
                    height = 40.dp,
                    onClick = { onEvent(CompleteTripEvent.AddItem) }
                )
            }
        }
    }
}

@Composable
private fun WizardChecklistRow(item: ShoppingItem, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
            .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
            .clickable { onToggle() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor, shape = RoundedCornerShape(6.dp))
                    .background(if (item.isChecked) AppColors.Coral else RetroTheme.BackgroundColor, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.isChecked) Text(text = "✓", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.name.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatKes(item.estimatedPrice),
                style = MaterialTheme.typography.bodyMedium,
                color = RetroTheme.TextColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PriceChangesStep(uiState: CompleteTripUiState, onEvent: (CompleteTripEvent) -> Unit, modifier: Modifier = Modifier) {
    val eligible = uiState.items.filter { it.id in uiState.originalItemIds && it.isChecked }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "DID ANY PRICES CHANGE?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = AppColors.Coral
            )
        }
        if (eligible.isEmpty()) {
            item {
                Text(
                    text = "No planned items were marked as bought, so there's nothing to price-check.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
            }
        }
        items(eligible, key = { it.id }) { item ->
            val isExpanded = item.id in uiState.expandedPriceItemIds
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                    .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                    .clickable { onEvent(CompleteTripEvent.TogglePriceEdit(item.id)) }
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = item.name.uppercase(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = RetroTheme.TextColor
                    )
                    Text(
                        text = if (isExpanded) "TAP TO COLLAPSE" else "TAP TO CHANGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Coral,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isExpanded) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Current: ${formatKes(item.estimatedPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RetroTheme.TextColor.copy(alpha = 0.6f)
                        )
                        Text(text = "→", color = RetroTheme.TextColor.copy(alpha = 0.4f))
                        RetroTextField(
                            value = uiState.priceEdits[item.id] ?: item.estimatedPrice.toString(),
                            onValueChange = { onEvent(CompleteTripEvent.PriceInputChanged(item.id, it)) },
                            placeholder = "New price",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfirmStep(uiState: CompleteTripUiState, modifier: Modifier = Modifier) {
    val bought = uiState.items.filter { it.isChecked }
    fun finalPrice(item: ShoppingItem) = uiState.priceEdits[item.id]?.toDoubleOrNull() ?: item.estimatedPrice
    val changed = bought.filter {
        it.id in uiState.originalItemIds && uiState.priceEdits[it.id]?.toDoubleOrNull()?.let { p -> p != it.estimatedPrice } == true
    }
    val plannedBudget = uiState.trip?.budget ?: 0.0
    val newTotal = bought.sumOf { finalPrice(it) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "CONFIRM",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = AppColors.Coral
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "BUDGET", style = MaterialTheme.typography.labelSmall, color = RetroTheme.TextColor.copy(alpha = 0.5f))
                Text(text = "SPENT", style = MaterialTheme.typography.labelSmall, color = RetroTheme.TextColor.copy(alpha = 0.5f))
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = formatKes(plannedBudget), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = RetroTheme.TextColor)
                Text(
                    text = formatKes(newTotal),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = if (newTotal <= plannedBudget) com.delivce.kikapu.ui.components.SuccessGreen else com.delivce.kikapu.ui.components.ErrorRed
                )
            }
        }
        item {
            Text(
                text = "[ BOUGHT (${bought.size}) ]",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )
        }
        items(bought, key = { it.id }) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                    .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = item.name.uppercase(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = RetroTheme.TextColor)
                Text(text = formatKes(finalPrice(item)), style = MaterialTheme.typography.bodyMedium, color = RetroTheme.TextColor.copy(alpha = 0.7f))
            }
        }
        if (changed.isNotEmpty()) {
            item {
                Text(
                    text = "[ PRICE CHANGES ]",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
            }
            items(changed, key = { "changed_${it.id}" }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = item.name.uppercase(), style = MaterialTheme.typography.bodySmall, color = RetroTheme.TextColor.copy(alpha = 0.7f))
                    Text(
                        text = "${formatKes(item.estimatedPrice)} → ${formatKes(finalPrice(item))}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Coral
                    )
                }
            }
        }
    }
}
