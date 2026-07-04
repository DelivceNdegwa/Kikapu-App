package com.delivce.kikapu.ui.screens.items

import com.delivce.kikapu.domain.model.Item

data class ItemsUiState(
    val items: List<Item> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showEditor: Boolean = false,
    val editingItemId: String? = null,
    val editorName: String = "",
    val editorQuantity: String = "1",
    val editorPrice: String = "",
    val editorPriority: Int = 3,
    val editorDurationDays: String = "30"
)

sealed class ItemsEvent {
    data class OpenEditor(val item: Item? = null) : ItemsEvent()
    object DismissEditor : ItemsEvent()
    data class NameChanged(val name: String) : ItemsEvent()
    data class QuantityChanged(val quantity: String) : ItemsEvent()
    data class PriceChanged(val price: String) : ItemsEvent()
    data class PriorityChanged(val priority: Int) : ItemsEvent()
    data class DurationChanged(val durationDays: String) : ItemsEvent()
    object Save : ItemsEvent()
    data class Delete(val itemId: String) : ItemsEvent()
    data class MarkShoppedNow(val itemId: String) : ItemsEvent()
    object ClearError : ItemsEvent()
}
