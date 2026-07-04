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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TripDetailViewModel @Inject constructor(
    private val repository: TripRepository,
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    private val _uiState = MutableStateFlow(TripDetailUiState())
    val uiState: StateFlow<TripDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getTripById(tripId),
                repository.getItemsForTrip(tripId)
            ) { trip, items -> trip to items }.collect { (trip, items) ->
                val spent = items.filter { it.isChecked }.sumOf { it.actualPrice }
                _uiState.update {
                    it.copy(
                        trip = trip,
                        items = items,
                        isLoading = false,
                        remainingBudget = (trip?.budget ?: 0.0) - spent,
                        spentSoFar = spent
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.getTripById(tripId)
                .map { it?.userId }
                .filterNotNull()
                .flatMapLatest { userId -> itemRepository.getItems(userId) }
                .collect { catalog -> _uiState.update { it.copy(catalogItems = catalog) } }
        }
    }

    fun onEvent(event: TripDetailEvent) {
        when (event) {
            TripDetailEvent.StartTrip -> startTrip()
            is TripDetailEvent.ToggleItem -> toggleItem(event.item)
            is TripDetailEvent.UpdateItemPrice -> updateItemPrice(event.item, event.price)
            is TripDetailEvent.NewItemNameChanged -> _uiState.update {
                // Retyping the name by hand breaks the link to whatever suggestion was tapped —
                // the price/priority prefill was a starting point, not a binding choice.
                it.copy(newItemName = event.name, selectedCatalogItemId = null)
            }
            is TripDetailEvent.NewItemPriceChanged -> _uiState.update { it.copy(newItemPrice = event.price) }
            is TripDetailEvent.SelectCatalogSuggestion -> selectCatalogSuggestion(event.item)
            TripDetailEvent.AddOutOfBudgetItem -> addOutOfBudgetItem()
            TripDetailEvent.CancelTrip -> cancelTrip()
            TripDetailEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun startTrip() {
        val trip = _uiState.value.trip ?: return
        if (trip.status != TripStatus.UPCOMING) return
        viewModelScope.launch {
            repository.updateTrip(trip.copy(status = TripStatus.ACTIVE, updatedAt = System.currentTimeMillis())).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to start trip") }
            }
        }
    }

    private fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.updateItem(item.copy(isChecked = !item.isChecked)).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update item") }
            }
        }
    }

    private fun updateItemPrice(item: ShoppingItem, price: Double) {
        viewModelScope.launch {
            val isOutOfBudget = price > _uiState.value.remainingBudget + item.actualPrice
            repository.updateItem(item.copy(actualPrice = price, isOutOfBudget = isOutOfBudget)).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update item") }
            }
            // Keep the catalog price in sync so future trips/budgeting reflect what it actually costs.
            item.catalogItemId?.let { catalogItemId ->
                itemRepository.updateItemPrice(catalogItemId, price)
            }
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

    private fun addOutOfBudgetItem() {
        val state = _uiState.value
        if (state.newItemName.isBlank()) return
        val price = state.newItemPrice.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            val item = ShoppingItem(
                id = UUID.randomUUID().toString(),
                tripId = tripId,
                name = state.newItemName.trim(),
                estimatedPrice = price,
                actualPrice = price,
                priorityIndex = state.newItemPriorityIndex,
                catalogItemId = state.selectedCatalogItemId,
                isChecked = true,
                isCustom = state.selectedCatalogItemId == null,
                isOutOfBudget = price > state.remainingBudget
            )
            repository.addItem(item).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            newItemName = "",
                            newItemPrice = "",
                            newItemPriorityIndex = 3,
                            selectedCatalogItemId = null
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to add item") }
                }
            )
        }
    }

    private fun cancelTrip() {
        val trip = _uiState.value.trip ?: return
        viewModelScope.launch {
            repository.updateTrip(trip.copy(status = TripStatus.CANCELLED, updatedAt = System.currentTimeMillis())).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to cancel trip") }
            }
        }
    }
}
