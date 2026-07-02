package com.delivce.kikapu.ui.screens.trips

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.domain.repository.ItemRepository
import com.delivce.kikapu.domain.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ActiveTripViewModel @Inject constructor(
    private val repository: TripRepository,
    private val itemRepository: ItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: String = checkNotNull(savedStateHandle["tripId"])

    private val _uiState = MutableStateFlow(ActiveTripUiState())
    val uiState: StateFlow<ActiveTripUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.getTripById(tripId),
                repository.getItemsForTrip(tripId)
            ) { trip, items -> trip to items }.collect { (trip, items) ->
                val spent = items.filter { it.isChecked }
                    .sumOf { it.actualPrice * it.quantity }
                _uiState.update {
                    it.copy(
                        trip = trip,
                        items = items,
                        isLoading = false,
                        remainingBudget = (trip?.budget ?: 0.0) - spent
                    )
                }
            }
        }
    }

    fun onEvent(event: ActiveTripEvent) {
        when (event) {
            is ActiveTripEvent.ToggleItem -> toggleItem(event.item)
            is ActiveTripEvent.UpdateItemPrice -> updateItemPrice(event.item, event.price)
            is ActiveTripEvent.NewItemNameChanged -> _uiState.update { it.copy(newItemName = event.name) }
            is ActiveTripEvent.NewItemPriceChanged -> _uiState.update { it.copy(newItemPrice = event.price) }
            ActiveTripEvent.AddOutOfBudgetItem -> addOutOfBudgetItem()
            ActiveTripEvent.CompleteTrip -> completeTrip()
            ActiveTripEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
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
            val isOutOfBudget = price > _uiState.value.remainingBudget + (item.actualPrice * item.quantity)
            repository.updateItem(item.copy(actualPrice = price, isOutOfBudget = isOutOfBudget)).onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to update item") }
            }
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
                isChecked = true,
                isCustom = true,
                isOutOfBudget = price > state.remainingBudget
            )
            repository.addItem(item).fold(
                onSuccess = {
                    _uiState.update { it.copy(newItemName = "", newItemPrice = "") }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to add item") }
                }
            )
        }
    }

    private fun completeTrip() {
        val trip = _uiState.value.trip ?: return
        val spent = _uiState.value.items.filter { it.isChecked }
            .sumOf { it.actualPrice * it.quantity }

        viewModelScope.launch {
            repository.updateTrip(trip.copy(status = TripStatus.COMPLETED, totalSpent = spent)).fold(
                onSuccess = {
                    _uiState.value.items
                        .filter { it.isChecked && it.catalogItemId != null }
                        .forEach { itemRepository.markShopped(it.catalogItemId!!) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(errorMessage = e.message ?: "Failed to complete trip") }
                }
            )
        }
    }
}
