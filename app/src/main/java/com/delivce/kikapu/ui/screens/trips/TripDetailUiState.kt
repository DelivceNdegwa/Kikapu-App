package com.delivce.kikapu.ui.screens.trips

import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.Trip

data class TripDetailUiState(
    val trip: Trip? = null,
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val remainingBudget: Double = 0.0,
    val spentSoFar: Double = 0.0,
    val catalogItems: List<Item> = emptyList(),
    val newItemName: String = "",
    val newItemPrice: String = "",
    val newItemPriorityIndex: Int = 3,
    val selectedCatalogItemId: String? = null
)

sealed class TripDetailEvent {
    object StartTrip : TripDetailEvent()
    data class ToggleItem(val item: ShoppingItem) : TripDetailEvent()
    data class UpdateItemPrice(val item: ShoppingItem, val price: Double) : TripDetailEvent()
    data class NewItemNameChanged(val name: String) : TripDetailEvent()
    data class NewItemPriceChanged(val price: String) : TripDetailEvent()
    data class SelectCatalogSuggestion(val item: Item) : TripDetailEvent()
    object AddOutOfBudgetItem : TripDetailEvent()
    object CancelTrip : TripDetailEvent()
    object ClearError : TripDetailEvent()
}
