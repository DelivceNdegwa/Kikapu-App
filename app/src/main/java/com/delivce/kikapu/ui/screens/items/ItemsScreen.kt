package com.delivce.kikapu.ui.screens.items

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.delivce.kikapu.domain.model.isDueForRestock
import com.delivce.kikapu.ui.components.EmptyStateIllustration
import com.delivce.kikapu.ui.components.PriorityDots
import com.delivce.kikapu.ui.components.PriorityPicker
import com.delivce.kikapu.ui.components.RetroTextField
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import com.delivce.kikapu.ui.util.formatDate
import com.delivce.kikapu.ui.util.formatKes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    modifier: Modifier = Modifier,
    viewModel: ItemsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(ItemsEvent.ClearError)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.BackgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = RetroTheme.TextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "[ ${uiState.items.size} IN CATALOG ]",
                    style = MaterialTheme.typography.labelMedium,
                    color = RetroTheme.TextColor.copy(alpha = 0.5f)
                )
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Coral)
                    }
                }
                uiState.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateIllustration(caption = "[ NO ITEMS YET — ADD WHAT YOU RESTOCK OFTEN ]")
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.items, key = { it.id }) { item ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = {
                                    if (it == SwipeToDismissBoxValue.EndToStart) {
                                        viewModel.onEvent(ItemsEvent.Delete(item.id))
                                    }
                                    true
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                AppColors.ErrorRed,
                                                RoundedCornerShape(RetroDefaults.CornerRadius)
                                            )
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(text = "DELETE ×", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            ) {
                                ItemRowCard(
                                    item = item,
                                    onClick = { viewModel.onEvent(ItemsEvent.OpenEditor(item)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .retroFrame(
                    borderColor = RetroTheme.BorderColor,
                    shadowColor = RetroTheme.ShadowColor,
                    shape = RoundedCornerShape(28.dp)
                )
                .background(AppColors.Coral, RoundedCornerShape(28.dp))
                .clickable { viewModel.onEvent(ItemsEvent.OpenEditor(null)) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        if (uiState.showEditor) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { viewModel.onEvent(ItemsEvent.DismissEditor) },
                sheetState = sheetState,
                containerColor = RetroTheme.BackgroundColor
            ) {
                ItemEditorContent(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ItemRowCard(item: Item, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDue = item.isDueForRestock()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .retroFrame(
                borderColor = if (isDue) AppColors.ErrorRed else RetroTheme.BorderColor,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(AppColors.Amber.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "QTY ×${item.quantity}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Earth
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isDue -> "⚠ RESTOCK DUE"
                            item.lastShoppedAt != null -> "LAST ${formatDate(item.lastShoppedAt)}"
                            else -> "NOT BOUGHT YET"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDue) AppColors.ErrorRed else RetroTheme.TextColor.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                PriorityDots(priority = item.priorityIndex)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = formatKes(item.estimatedPrice),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ItemEditorContent(uiState: ItemsUiState, viewModel: ItemsViewModel) {
    val isEditing = uiState.editingItemId != null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
            Text(
                text = if (isEditing) "EDIT ITEM" else "NEW ITEM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = AppColors.Coral
            )
        }
        RetroTextField(
            value = uiState.editorName,
            onValueChange = { viewModel.onEvent(ItemsEvent.NameChanged(it)) },
            label = "Item name",
            placeholder = "Eg Fruits",
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RetroTextField(
                value = uiState.editorQuantity,
                onValueChange = { viewModel.onEvent(ItemsEvent.QuantityChanged(it)) },
                placeholder = "Quantity",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            RetroTextField(
                value = uiState.editorPrice,
                onValueChange = { viewModel.onEvent(ItemsEvent.PriceChanged(it)) },
                placeholder = "Eg 300",
                label = "Estimated Price",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
        RetroTextField(
            value = uiState.editorDurationDays,
            onValueChange = { viewModel.onEvent(ItemsEvent.DurationChanged(it)) },
            placeholder = "Restock every (days)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "PRIORITY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.TextColor.copy(alpha = 0.6f)
        )
        PriorityPicker(
            priority = uiState.editorPriority,
            onPriorityChanged = { viewModel.onEvent(ItemsEvent.PriorityChanged(it)) }
        )

        if (isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .retroFrame(
                        borderColor = RetroTheme.BorderColor,
                        shadowColor = RetroTheme.ShadowColor,
                        shape = RoundedCornerShape(10.dp),
                        thickness = 1.5.dp
                    )
                    .background(AppColors.Teal, RoundedCornerShape(10.dp))
                    .clickable {
                        viewModel.onEvent(ItemsEvent.MarkShoppedNow(uiState.editingItemId!!))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓ MARK SHOPPED TODAY",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .retroFrame(
                    borderColor = RetroTheme.BorderColor,
                    shadowColor = RetroTheme.ShadowColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(AppColors.Coral, RoundedCornerShape(12.dp))
                .clickable { viewModel.onEvent(ItemsEvent.Save) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SAVE ITEM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
