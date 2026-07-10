package com.delivce.kikapu.ui.screens.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.repository.ItemRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val repository: ItemRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val userId: String get() = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getItems(userId).collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
        }
    }

    fun onEvent(event: ItemsEvent) {
        when (event) {
            is ItemsEvent.OpenEditor -> openEditor(event.item)
            is ItemsEvent.SearchQueryChanged -> _uiState.update { it.copy(searchQuery = event.query) }
            ItemsEvent.DismissEditor -> _uiState.update { it.copy(showEditor = false) }
            is ItemsEvent.NameChanged -> _uiState.update { it.copy(editorName = event.name) }
            is ItemsEvent.QuantityChanged -> _uiState.update { it.copy(editorQuantity = event.quantity) }
            is ItemsEvent.PriceChanged -> _uiState.update { it.copy(editorPrice = event.price) }
            is ItemsEvent.PriorityChanged -> _uiState.update { it.copy(editorPriority = event.priority) }
            is ItemsEvent.DurationChanged -> _uiState.update { it.copy(editorDurationDays = event.durationDays) }
            ItemsEvent.Save -> save()
            is ItemsEvent.Delete -> delete(event.itemId)
            is ItemsEvent.MarkShoppedNow -> markShoppedNow(event.itemId)
            ItemsEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun openEditor(item: Item?) {
        _uiState.update {
            it.copy(
                showEditor = true,
                editingItemId = item?.id,
                editorName = item?.name ?: "",
                editorQuantity = (item?.quantity ?: 1).toString(),
                editorPrice = item?.estimatedPrice?.takeIf { price -> price > 0 }?.toString() ?: "",
                editorPriority = item?.priorityIndex ?: 3,
                editorDurationDays = (item?.durationDays ?: 30).toString()
            )
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.editorName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Item name is required") }
            return
        }
        val quantity = state.editorQuantity.toIntOrNull()?.coerceAtLeast(1)
        val durationDays = state.editorDurationDays.toIntOrNull()?.coerceAtLeast(1)
        if (quantity == null || durationDays == null) {
            _uiState.update { it.copy(errorMessage = "Enter a valid quantity and duration") }
            return
        }
        val userId = userId
        if (userId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "You must be signed in") }
            return
        }

        viewModelScope.launch {
            val existing = state.items.find { it.id == state.editingItemId }
            val item = Item(
                id = existing?.id ?: UUID.randomUUID().toString(),
                userId = userId,
                name = state.editorName.trim(),
                quantity = quantity,
                estimatedPrice = state.editorPrice.toDoubleOrNull() ?: 0.0,
                priorityIndex = state.editorPriority,
                durationDays = durationDays,
                lastShoppedAt = existing?.lastShoppedAt,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.upsertItem(item).fold(
                onSuccess = { _uiState.update { it.copy(showEditor = false, editingItemId = null) } },
                onFailure = { e -> _uiState.update { it.copy(errorMessage = e.message ?: "Failed to save item") } }
            )
        }
    }

    private fun delete(itemId: String) {
        viewModelScope.launch {
            repository.deleteItem(itemId).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to delete item") }
            }
        }
    }

    private fun markShoppedNow(itemId: String) {
        viewModelScope.launch {
            repository.markShopped(itemId).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update item") }
            }
        }
    }
}
