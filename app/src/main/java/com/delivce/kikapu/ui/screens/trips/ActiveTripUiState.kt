package com.delivce.kikapu.ui.screens.trips

import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.Trip

data class ActiveTripUiState(
    val trip: Trip? = null,
    val items: List<ShoppingItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val remainingBudget: Double = 0.0,
    val newItemName: String = "",
    val newItemPrice: String = ""
)

sealed class ActiveTripEvent {
    data class ToggleItem(val item: ShoppingItem) : ActiveTripEvent()
    data class UpdateItemPrice(val item: ShoppingItem, val price: Double) : ActiveTripEvent()
    data class NewItemNameChanged(val name: String) : ActiveTripEvent()
    data class NewItemPriceChanged(val price: String) : ActiveTripEvent()
    object AddOutOfBudgetItem : ActiveTripEvent()
    object CompleteTrip : ActiveTripEvent()
    object ClearError : ActiveTripEvent()
}
