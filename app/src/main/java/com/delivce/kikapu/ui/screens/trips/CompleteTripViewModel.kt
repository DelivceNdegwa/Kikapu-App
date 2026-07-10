package com.delivce.kikapu.ui.screens.trips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.domain.repository.ItemRepository
import com.delivce.kikapu.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val LAST_STEP = 2

@HiltViewModel
class CompleteTripViewModel @Inject constructor(
    private val repository: TripRepository,
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    private val _uiState = MutableStateFlow(CompleteTripUiState())
    val uiState: StateFlow<CompleteTripUiState> = _uiState.asStateFlow()

    init {
        // One-shot load, not a continuous collection — everything from here on is a local working
        // copy that only reaches Room on Confirm, so it must not get clobbered by later emissions.
        viewModelScope.launch {
            val trip = repository.getTripById(tripId).first()
            val items = repository.getItemsForTrip(tripId).first()
            val catalog = trip?.userId?.let { userId -> itemRepository.getItems(userId).first() } ?: emptyList()
            _uiState.update {
                it.copy(
                    trip = trip,
                    items = items,
                    originalItemIds = items.map { item -> item.id }.toSet(),
                    catalogItems = catalog,
                    isLoading = false
                )
            }
        }
    }

    fun onEvent(event: CompleteTripEvent) {
        when (event) {
            is CompleteTripEvent.ToggleItem -> _uiState.update { state ->
                state.copy(items = state.items.map {
                    if (it.id == event.itemId) it.copy(isChecked = !it.isChecked) else it
                })
            }
            is CompleteTripEvent.NewItemNameChanged -> _uiState.update {
                it.copy(newItemName = event.name, selectedCatalogItemId = null)
            }
            is CompleteTripEvent.NewItemPriceChanged -> _uiState.update { it.copy(newItemPrice = event.price) }
            is CompleteTripEvent.SelectCatalogSuggestion -> selectCatalogSuggestion(event.item)
            CompleteTripEvent.AddItem -> addItem()
            is CompleteTripEvent.TogglePriceEdit -> togglePriceEdit(event.itemId)
            is CompleteTripEvent.PriceInputChanged -> _uiState.update {
                it.copy(priceEdits = it.priceEdits + (event.itemId to event.price))
            }
            CompleteTripEvent.NextStep -> _uiState.update { it.copy(step = (it.step + 1).coerceAtMost(LAST_STEP)) }
            CompleteTripEvent.PreviousStep -> _uiState.update { it.copy(step = (it.step - 1).coerceAtLeast(0)) }
            CompleteTripEvent.Confirm -> confirm()
            CompleteTripEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun selectCatalogSuggestion(item: Item) {
        _uiState.update {
            it.copy(
                newItemName = item.name,
                newItemPrice = item.estimatedPrice.toString(),
                newItemPriorityIndex = item.priorityIndex,
                selectedCatalogItemId = item.id
            )
        }
    }

    private fun addItem() {
        val state = _uiState.value
        if (state.newItemName.isBlank()) return
        val price = state.newItemPrice.toDoubleOrNull() ?: 0.0
        val item = ShoppingItem(
            id = UUID.randomUUID().toString(),
            tripId = tripId,
            name = state.newItemName.trim(),
            estimatedPrice = price,
            actualPrice = price,
            priorityIndex = state.newItemPriorityIndex,
            catalogItemId = state.selectedCatalogItemId,
            isChecked = true,
            isCustom = state.selectedCatalogItemId == null
        )
        _uiState.update {
            it.copy(
                items = it.items + item,
                newItemName = "",
                newItemPrice = "",
                newItemPriorityIndex = 3,
                selectedCatalogItemId = null
            )
        }
    }

    private fun togglePriceEdit(itemId: String) {
        _uiState.update { state ->
            val expanded = state.expandedPriceItemIds.toMutableSet()
            val nowExpanded = expanded.add(itemId)
            if (!nowExpanded) expanded.remove(itemId)
            val priceEdits = if (nowExpanded && itemId !in state.priceEdits) {
                val current = state.items.find { it.id == itemId }?.estimatedPrice ?: 0.0
                state.priceEdits + (itemId to current.toString())
            } else {
                state.priceEdits
            }
            state.copy(expandedPriceItemIds = expanded, priceEdits = priceEdits)
        }
    }

    private fun confirm() {
        val state = _uiState.value
        val trip = state.trip ?: return
        _uiState.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val finalItems = state.items.map { item ->
                val editedPrice = state.priceEdits[item.id]?.toDoubleOrNull()
                if (editedPrice != null && editedPrice != item.estimatedPrice) {
                    item.copy(estimatedPrice = editedPrice, actualPrice = editedPrice)
                } else {
                    item
                }
            }

            finalItems.forEach { item ->
                if (item.id in state.originalItemIds) {
                    repository.updateItem(item)
                } else {
                    repository.addItem(item)
                }
            }

            finalItems.filter { it.catalogItemId != null && state.priceEdits[it.id]?.toDoubleOrNull() != null }
                .forEach { itemRepository.updateItemPrice(it.catalogItemId!!, it.estimatedPrice) }

            finalItems.filter { it.isChecked && it.catalogItemId != null }
                .forEach { itemRepository.markShopped(it.catalogItemId!!) }

            val totalSpent = finalItems.filter { it.isChecked }.sumOf { it.actualPrice }
            repository.updateTrip(
                trip.copy(status = TripStatus.COMPLETED, totalSpent = totalSpent, updatedAt = System.currentTimeMillis())
            ).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, isDone = true) } },
                onFailure = { e ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = e.message ?: "Failed to complete trip") }
                }
            )
        }
    }
}
