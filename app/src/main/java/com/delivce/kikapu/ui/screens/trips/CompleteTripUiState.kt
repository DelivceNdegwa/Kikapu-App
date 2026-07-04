package com.delivce.kikapu.ui.screens.trips

import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.Trip

data class CompleteTripUiState(
    val trip: Trip? = null,
    // Working copy — nothing here is persisted to Room until Confirm.
    val items: List<ShoppingItem> = emptyList(),
    // Ids present before the wizard opened; step 2 only offers a price edit for these.
    val originalItemIds: Set<String> = emptySet(),
    val step: Int = 0, // 0 = what did you buy, 1 = price changes, 2 = confirm
    val catalogItems: List<Item> = emptyList(),
    val newItemName: String = "",
    val newItemPrice: String = "",
    val newItemPriorityIndex: Int = 3,
    val selectedCatalogItemId: String? = null,
    val expandedPriceItemIds: Set<String> = emptySet(),
    val priceEdits: Map<String, String> = emptyMap(), // itemId -> new price text
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val errorMessage: String? = null
)

sealed class CompleteTripEvent {
    data class ToggleItem(val itemId: String) : CompleteTripEvent()
    data class NewItemNameChanged(val name: String) : CompleteTripEvent()
    data class NewItemPriceChanged(val price: String) : CompleteTripEvent()
    data class SelectCatalogSuggestion(val item: Item) : CompleteTripEvent()
    object AddItem : CompleteTripEvent()
    data class TogglePriceEdit(val itemId: String) : CompleteTripEvent()
    data class PriceInputChanged(val itemId: String, val price: String) : CompleteTripEvent()
    object NextStep : CompleteTripEvent()
    object PreviousStep : CompleteTripEvent()
    object Confirm : CompleteTripEvent()
    object ClearError : CompleteTripEvent()
}
