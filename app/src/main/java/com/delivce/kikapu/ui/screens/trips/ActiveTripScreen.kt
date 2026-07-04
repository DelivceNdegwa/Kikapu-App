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
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.ui.components.AnimatedFillButton
import com.delivce.kikapu.ui.components.ErrorRed
import com.delivce.kikapu.ui.components.NavigationButton
import com.delivce.kikapu.ui.components.RetroChecklistItem
import com.delivce.kikapu.ui.components.RetroTextField
import com.delivce.kikapu.ui.components.SuccessGreen
import com.delivce.kikapu.ui.components.SuccessOverlay
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import com.delivce.kikapu.ui.util.formatKes

@Composable
fun ActiveTripScreen(
    modifier: Modifier = Modifier,
    viewModel: ActiveTripViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onTripCompleted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editingItemId by remember { mutableStateOf<String?>(null) }
    var priceInput by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(ActiveTripEvent.ClearError)
        }
    }

    LaunchedEffect(uiState.trip?.status) {
        when (uiState.trip?.status) {
            TripStatus.COMPLETED -> showSuccess = true
            TripStatus.CANCELLED -> onTripCompleted()
            else -> Unit
        }
    }

    val trip = uiState.trip

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
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
                        text = "[ ACTIVE MODE ]",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Coral
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val budget = trip?.budget ?: 0.0
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
        }

        val checkedCount = uiState.items.count { it.isChecked }

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
                    onToggle = { viewModel.onEvent(ActiveTripEvent.ToggleItem(item)) },
                    onPriceEditStart = {
                        editingItemId = item.id
                        priceInput = if (item.actualPrice > 0) item.actualPrice.toString() else ""
                    },
                    isEditingPrice = editingItemId == item.id,
                    priceInput = priceInput,
                    onPriceInputChange = { priceInput = it },
                    onPriceCommit = {
                        val price = priceInput.toDoubleOrNull() ?: item.actualPrice
                        viewModel.onEvent(ActiveTripEvent.UpdateItemPrice(item, price))
                        editingItemId = null
                    }
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RetroTextField(
                        value = uiState.newItemName,
                        onValueChange = { viewModel.onEvent(ActiveTripEvent.NewItemNameChanged(it)) },
                        placeholder = "Item name",
                        modifier = Modifier.weight(1f)
                    )
                    RetroTextField(
                        value = uiState.newItemPrice,
                        onValueChange = { viewModel.onEvent(ActiveTripEvent.NewItemPriceChanged(it)) },
                        placeholder = "Price",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(110.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .retroFrame(
                                borderColor = RetroTheme.BorderColor,
                                shadowColor = RetroTheme.ShadowColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(AppColors.Coral, RoundedCornerShape(10.dp))
                            .clickable { viewModel.onEvent(ActiveTripEvent.AddOutOfBudgetItem) },
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
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedFillButton(
                text = "CANCEL TRIP",
                color = ErrorRed,
                height = 56.dp,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.onEvent(ActiveTripEvent.CancelTrip) }
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .retroFrame(
                        borderColor = RetroTheme.BorderColor,
                        shadowColor = RetroTheme.ShadowColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(SuccessGreen, RoundedCornerShape(12.dp))
                    .clickable { viewModel.onEvent(ActiveTripEvent.CompleteTrip) },
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

        if (showSuccess) {
            SuccessOverlay(onFinished = onTripCompleted)
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
