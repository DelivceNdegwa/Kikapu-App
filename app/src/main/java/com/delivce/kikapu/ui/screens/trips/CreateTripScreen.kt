package com.delivce.kikapu.ui.screens.trips

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.domain.usecase.BudgetStrategy
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.model.overdueDays
import com.delivce.kikapu.ui.components.AnimatedFillButton
import com.delivce.kikapu.ui.components.NavigationButton
import com.delivce.kikapu.ui.components.PriorityDots
import com.delivce.kikapu.ui.components.PriorityPicker
import com.delivce.kikapu.ui.components.RetroTextField
import com.delivce.kikapu.ui.components.SuccessOverlay
import com.delivce.kikapu.ui.components.WeekStripCalendar
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors
import com.delivce.kikapu.ui.util.formatKes
import com.delivce.kikapu.ui.util.formatTime
import kotlinx.coroutines.launch
import java.util.Calendar

private const val STAGE_COUNT = 3

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CreateTripScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTripViewModel = hiltViewModel(),
    onTripCreated: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tripCreated by viewModel.tripCreated.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { STAGE_COUNT })
    val coroutineScope = rememberCoroutineScope()
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(CreateTripEvent.ClearError)
        }
    }

    LaunchedEffect(tripCreated) {
        if (tripCreated != null) showSuccess = true
    }

    val detailsValid = uiState.name.isNotBlank() && uiState.budget.toDoubleOrNull() != null

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
                    text = "NEW TRIP",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = RetroTheme.TextColor
                )
                Text(
                    text = "STEP ${pagerState.currentPage + 1}/$STAGE_COUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.Coral,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        StepIndicator(currentPage = pagerState.currentPage, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> DetailsStage(uiState = uiState, viewModel = viewModel)
                1 -> ScheduleStage(uiState = uiState, viewModel = viewModel)
                else -> ItemsStage(uiState = uiState, viewModel = viewModel)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pagerState.currentPage > 0) {
                StageNavButton(
                    text = "‹ BACK",
                    color = RetroTheme.SurfaceColor,
                    textColor = RetroTheme.TextColor,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }
                )
            }
            if (pagerState.currentPage < STAGE_COUNT - 1) {
                StageNavButton(
                    text = "NEXT →",
                    color = AppColors.Coral,
                    textColor = Color.White,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (pagerState.currentPage == 0 && !detailsValid) {
                            Toast.makeText(
                                context,
                                "Please fill in the trip name and a valid budget before continuing",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    }
                )
            } else {
                StageNavButton(
                    text = "CREATE TRIP",
                    color = AppColors.Coral,
                    textColor = Color.White,
                    isLoading = uiState.isLoading,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onEvent(CreateTripEvent.CreateTrip) }
                )
            }
        }
    }

        if (showSuccess) {
            SuccessOverlay(onFinished = { tripCreated?.let { onTripCreated(it) } })
        }
    }
}

@Composable
private fun StepIndicator(currentPage: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (step in 0 until STAGE_COUNT) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .background(
                        if (step <= currentPage) AppColors.Coral else RetroTheme.BorderColor.copy(alpha = 0.15f),
                        RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@Composable
private fun StageNavButton(
    text: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(if (enabled) color else color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = textColor, modifier = Modifier.size(22.dp))
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun DetailsStage(uiState: CreateTripUiState, viewModel: CreateTripViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionHeader("01. DETAILS") }
        item {
            RetroTextField(
                value = uiState.name,
                onValueChange = { viewModel.onEvent(CreateTripEvent.NameChanged(it)) },
                label = "Short description",
                placeholder = "My shopping",
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            RetroTextField(
                value = uiState.budget,
                onValueChange = { viewModel.onEvent(CreateTripEvent.BudgetChanged(it)) },
                label = "Budget (KES)",
                placeholder = "1000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ScheduleStage(uiState: CreateTripUiState, viewModel: CreateTripViewModel) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                context,
                "Notifications are off, so the reminder won't show. Enable them in system settings if you'd like a heads-up.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SectionHeader("02. SCHEDULE") }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                    .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                    .padding(16.dp)
            ) {
                WeekStripCalendar(
                    selectedDate = uiState.date,
                    onDateSelected = { viewModel.onEvent(CreateTripEvent.DateChanged(it)) }
                )
            }
        }
        item {
            Text(
                text = "TRIP TIME",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )
        }
        item {
            TimeChipsRow(
                baseDateMillis = uiState.date,
                selectedMillis = uiState.date,
                onTimeSelected = { viewModel.onEvent(CreateTripEvent.DateChanged(it)) },
                onCustomClick = {
                    val calendar = Calendar.getInstance().apply { timeInMillis = uiState.date }
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hour)
                            calendar.set(Calendar.MINUTE, minute)
                            viewModel.onEvent(CreateTripEvent.DateChanged(calendar.timeInMillis))
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    ).show()
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SET REMINDER",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
                Switch(
                    checked = uiState.reminderEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.onEvent(CreateTripEvent.ReminderEnabledChanged(enabled))
                        if (!enabled || NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            return@Switch
                        }
                        val canRequestRuntimePermission =
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                        if (canRequestRuntimePermission) {
                            // First-time ask (or the user hasn't permanently denied it yet).
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            // Either pre-API 33 (no runtime prompt exists) or the permission was
                            // already denied earlier and the system won't show the dialog again —
                            // notifications are disabled for the app at the OS level either way,
                            // so send the user straight to the app's notification settings.
                            Toast.makeText(
                                context,
                                "Notifications are off for Kikapu, so the reminder won't show. Opening notification settings…",
                                Toast.LENGTH_LONG
                            ).show()
                            val settingsIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            context.startActivity(settingsIntent)
                        }
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = AppColors.Coral)
                )
            }
        }
        if (uiState.reminderEnabled) {
            item {
                TimeChipsRow(
                    baseDateMillis = uiState.date,
                    selectedMillis = uiState.reminderTime,
                    onTimeSelected = { viewModel.onEvent(CreateTripEvent.ReminderTimeChanged(it)) },
                    onCustomClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = uiState.reminderTime ?: uiState.date
                        }
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                viewModel.onEvent(CreateTripEvent.ReminderTimeChanged(calendar.timeInMillis))
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            false
                        ).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun ItemsStage(uiState: CreateTripUiState, viewModel: CreateTripViewModel) {
    val budget = uiState.budget.toDoubleOrNull() ?: 0.0
    val spent = uiState.items.sumOf { it.estimatedPrice }
    val remaining = budget - spent
    var editingItemId by remember { mutableStateOf<String?>(null) }
    var priceInput by remember { mutableStateOf("") }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SectionHeader("03. ITEMS") }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BudgetStatText(label = "BUDGET", value = formatKes(budget))
                BudgetStatText(label = "PLANNED", value = formatKes(spent))
                BudgetStatText(
                    label = "LEFT",
                    value = formatKes(remaining),
                    color = if (remaining >= 0) RetroTheme.TextColor else AppColors.ErrorRed
                )
            }
        }

        if (uiState.catalogItems.isNotEmpty()) {
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
                        text = "SMART FILL",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Coral
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BudgetStrategy.entries.forEach { strategy ->
                            SortChip(
                                label = strategy.label,
                                selected = uiState.budgetStrategy == strategy,
                                onClick = { viewModel.onEvent(CreateTripEvent.StrategyChanged(strategy)) }
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.onEvent(CreateTripEvent.IncludeNonDueItemsChanged(!uiState.includeNonDueItems))
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .retroFrame(
                                    borderColor = RetroTheme.BorderColor,
                                    shadowColor = RetroTheme.ShadowColor,
                                    shape = RoundedCornerShape(4.dp),
                                    thickness = 1.5.dp
                                )
                                .background(
                                    if (uiState.includeNonDueItems) AppColors.Coral else RetroTheme.BackgroundColor,
                                    RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.includeNonDueItems) {
                                Text(text = "✓", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INCLUDE NON-DUE ITEMS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = RetroTheme.TextColor.copy(alpha = 0.6f)
                        )
                    }
                    AnimatedFillButton(
                        text = "AUTO-FILL FROM BUDGET",
                        icon = Icons.Filled.SmartToy,
                        height = 40.dp,
                        onClick = { viewModel.onEvent(CreateTripEvent.AutoFillFromBudget) }
                    )
                }
            }

            item {
                Text(
                    text = "FROM YOUR CATALOG",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor.copy(alpha = 0.6f)
                )
            }
            item {
                RetroTextField(
                    value = uiState.catalogSearchQuery,
                    onValueChange = { viewModel.onEvent(CreateTripEvent.CatalogSearchChanged(it)) },
                    label = "Search catalog",
                    placeholder = "Eg: milk",
                    leadingIcon = Icons.Filled.Search,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val filteredCatalog = uiState.catalogItems
                .filter { it.name.contains(uiState.catalogSearchQuery, ignoreCase = true) }
                .sortedByDescending { it.overdueDays() }
            items(filteredCatalog, key = { "catalog_${it.id}" }) { catalogItem ->
                val isSelected = uiState.items.any { it.catalogItemId == catalogItem.id }
                CatalogItemRow(
                    item = catalogItem,
                    isSelected = isSelected,
                    onClick = { viewModel.onEvent(CreateTripEvent.ToggleCatalogItem(catalogItem)) }
                )
            }
        }

        item {
            Text(
                text = "SHOPPING LIST",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SortChip(
                    label = "PRIORITY",
                    selected = uiState.itemSortOrder == ItemSortOrder.PRIORITY,
                    onClick = { viewModel.onEvent(CreateTripEvent.SortItems(ItemSortOrder.PRIORITY)) }
                )
                SortChip(
                    label = "PRICE ↑",
                    selected = uiState.itemSortOrder == ItemSortOrder.PRICE_ASC,
                    onClick = { viewModel.onEvent(CreateTripEvent.SortItems(ItemSortOrder.PRICE_ASC)) }
                )
                SortChip(
                    label = "PRICE ↓",
                    selected = uiState.itemSortOrder == ItemSortOrder.PRICE_DESC,
                    onClick = { viewModel.onEvent(CreateTripEvent.SortItems(ItemSortOrder.PRICE_DESC)) }
                )
            }
        }

        items(uiState.items, key = { it.id }) { shoppingItem ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    if (it == SwipeToDismissBoxValue.EndToStart) {
                        viewModel.onEvent(CreateTripEvent.RemoveItem(shoppingItem.id))
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .retroFrame(
                            borderColor = RetroTheme.BorderColor,
                            shadowColor = RetroTheme.ShadowColor
                        )
                        .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = shoppingItem.name.uppercase(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = RetroTheme.TextColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            PriorityDots(priority = shoppingItem.priorityIndex)
                        }
                        if (editingItemId == shoppingItem.id) {
                            val focusRequester = remember { FocusRequester() }
                            val commitPrice = {
                                val price = priceInput.toDoubleOrNull() ?: shoppingItem.estimatedPrice
                                viewModel.onEvent(CreateTripEvent.UpdateItemPrice(shoppingItem.id, price))
                                editingItemId = null
                            }
                            BasicTextField(
                                value = priceInput,
                                onValueChange = { priceInput = it },
                                modifier = Modifier
                                    .width(72.dp)
                                    .focusRequester(focusRequester)
                                    // Committing only on the keyboard's Done action meant tapping
                                    // away to edit another item silently discarded the price.
                                    .onFocusChanged { focusState -> if (!focusState.isFocused) commitPrice() },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = RetroTheme.TextColor),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { commitPrice() })
                            )
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }
                        } else {
                            Text(
                                text = formatKes(shoppingItem.estimatedPrice),
                                style = MaterialTheme.typography.bodyMedium,
                                color = RetroTheme.TextColor.copy(alpha = 0.7f),
                                modifier = Modifier.clickable {
                                    editingItemId = shoppingItem.id
                                    priceInput = shoppingItem.estimatedPrice.toString()
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(
                        borderColor = RetroTheme.BorderColor,
                        shadowColor = RetroTheme.ShadowColor
                    )
                    .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RetroTextField(
                    value = uiState.newItemName,
                    onValueChange = { viewModel.onEvent(CreateTripEvent.NewItemNameChanged(it)) },
                    placeholder = "Item name",
                    modifier = Modifier.fillMaxWidth()
                )
                RetroTextField(
                    value = uiState.newItemPrice,
                    onValueChange = { viewModel.onEvent(CreateTripEvent.NewItemPriceChanged(it)) },
                    placeholder = "Estimated price",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                PriorityPicker(
                    priority = uiState.newItemPriority,
                    onPriorityChanged = { viewModel.onEvent(CreateTripEvent.NewItemPriorityChanged(it)) }
                )
                AnimatedFillButton(
                    text = "+ ADD TO LIST",
                    height = 44.dp,
                    onClick = { viewModel.onEvent(CreateTripEvent.AddItem) }
                )
            }
        }
    }
}

@Composable
private fun CatalogItemRow(item: Item, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor,
                thickness = 1.5.dp
            )
            .background(
                if (isSelected) AppColors.Coral else RetroTheme.SurfaceColor,
                RoundedCornerShape(RetroDefaults.CornerRadius)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                    color = if (isSelected) Color.White else RetroTheme.TextColor
                )
                Text(
                    text = "QTY ×${item.quantity} · ${formatKes(item.estimatedPrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else RetroTheme.TextColor.copy(alpha = 0.5f)
                )
            }
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeChipsRow(
    baseDateMillis: Long,
    selectedMillis: Long?,
    onTimeSelected: (Long) -> Unit,
    onCustomClick: () -> Unit
) {
    val presets = listOf(9 to 0, 12 to 0, 15 to 0, 18 to 0)
    val presetLabels = listOf("9:00 AM", "12:00 PM", "3:00 PM", "6:00 PM")
    val selectedCal = selectedMillis?.let { Calendar.getInstance().apply { timeInMillis = it } }
    val isPresetSelected = selectedCal != null && presets.any { (hour, minute) ->
        selectedCal.get(Calendar.HOUR_OF_DAY) == hour && selectedCal.get(Calendar.MINUTE) == minute
    }
    val isCustomSelected = selectedCal != null && !isPresetSelected

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEachIndexed { index, (hour, minute) ->
                val isSelected = selectedCal != null &&
                    selectedCal.get(Calendar.HOUR_OF_DAY) == hour &&
                    selectedCal.get(Calendar.MINUTE) == minute
                TimeChip(
                    label = presetLabels[index],
                    selected = isSelected,
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = baseDateMillis }
                        cal.set(Calendar.HOUR_OF_DAY, hour)
                        cal.set(Calendar.MINUTE, minute)
                        onTimeSelected(cal.timeInMillis)
                    }
                )
            }
        }
        TimeChip(
            label = if (isCustomSelected) "CUSTOM: ${formatTime(selectedMillis!!)}" else "CUSTOM TIME…",
            selected = isCustomSelected,
            onClick = onCustomClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TimeChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else RetroTheme.TextColor
        )
    }
}

@Composable
private fun BudgetStatText(label: String, value: String, color: Color = RetroTheme.TextColor) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.TextColor.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Black,
        color = AppColors.Coral
    )
}

@Composable
private fun SortChip(label: String, selected: Boolean, onClick: () -> Unit) {
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
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else RetroTheme.TextColor
        )
    }
}
