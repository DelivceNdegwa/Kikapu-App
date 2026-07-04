package com.delivce.kikapu.ui.screens.trips

import com.delivce.kikapu.domain.BudgetStrategy
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.model.ShoppingItem

data class CreateTripUiState(
    val name: String = "",
    val budget: String = "",
    val date: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = false,
    val reminderTime: Long? = null,
    val catalogItems: List<Item> = emptyList(),
    val catalogSearchQuery: String = "",
    val budgetStrategy: BudgetStrategy = BudgetStrategy.PRIORITIZE_PRIORITY,
    val includeNonDueItems: Boolean = false,
    val items: List<ShoppingItem> = emptyList(),
    val newItemName: String = "",
    val newItemPrice: String = "",
    val newItemPriority: Int = 3,
    val itemSortOrder: ItemSortOrder = ItemSortOrder.PRIORITY,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class ItemSortOrder { PRICE_ASC, PRICE_DESC, PRIORITY }

sealed class CreateTripEvent {
    data class NameChanged(val name: String) : CreateTripEvent()
    data class BudgetChanged(val budget: String) : CreateTripEvent()
    data class DateChanged(val date: Long) : CreateTripEvent()
    data class ReminderEnabledChanged(val enabled: Boolean) : CreateTripEvent()
    data class ReminderTimeChanged(val reminderTime: Long?) : CreateTripEvent()
    data class ToggleCatalogItem(val item: Item) : CreateTripEvent()
    data class CatalogSearchChanged(val query: String) : CreateTripEvent()
    data class StrategyChanged(val strategy: BudgetStrategy) : CreateTripEvent()
    data class IncludeNonDueItemsChanged(val include: Boolean) : CreateTripEvent()
    object AutoFillFromBudget : CreateTripEvent()
    data class NewItemNameChanged(val name: String) : CreateTripEvent()
    data class NewItemPriceChanged(val price: String) : CreateTripEvent()
    data class NewItemPriorityChanged(val priority: Int) : CreateTripEvent()
    object AddItem : CreateTripEvent()
    data class RemoveItem(val itemId: String) : CreateTripEvent()
    data class UpdateItemPrice(val itemId: String, val price: Double) : CreateTripEvent()
    data class SortItems(val order: ItemSortOrder) : CreateTripEvent()
    object CreateTrip : CreateTripEvent()
    object ClearError : CreateTripEvent()
}
