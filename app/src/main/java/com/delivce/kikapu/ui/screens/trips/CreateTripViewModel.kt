package com.delivce.kikapu.ui.screens.trips

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivce.kikapu.domain.BudgetStrategy
import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.model.ShoppingItem
import com.delivce.kikapu.domain.model.Trip
import com.delivce.kikapu.domain.recommendItemsForBudget
import com.delivce.kikapu.domain.repository.ItemRepository
import com.delivce.kikapu.domain.repository.TripRepository
import com.delivce.kikapu.ui.util.formatKes
import com.delivce.kikapu.worker.TripReminderWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTripViewModel @Inject constructor(
    private val repository: TripRepository,
    private val itemRepository: ItemRepository,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

    private val _tripCreated = MutableStateFlow<String?>(null)
    val tripCreated: StateFlow<String?> = _tripCreated.asStateFlow()

    init {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch {
                itemRepository.getItems(userId).collect { catalogItems ->
                    _uiState.update { it.copy(catalogItems = catalogItems) }
                }
            }
        }
    }

    fun onEvent(event: CreateTripEvent) {
        when (event) {
            is CreateTripEvent.NameChanged -> _uiState.update { it.copy(name = event.name) }
            is CreateTripEvent.BudgetChanged -> _uiState.update { it.copy(budget = event.budget) }
            is CreateTripEvent.DateChanged -> _uiState.update { it.copy(date = event.date) }
            is CreateTripEvent.ReminderEnabledChanged -> _uiState.update {
                val reminderTime = when {
                    !event.enabled -> null
                    it.reminderTime != null -> it.reminderTime
                    // No time picked yet — default to the trip's own time so a reminder is
                    // always scheduled the moment the switch is flipped on, not only once the
                    // user also taps a preset/custom time chip.
                    else -> it.date
                }
                it.copy(reminderEnabled = event.enabled, reminderTime = reminderTime)
            }
            is CreateTripEvent.ReminderTimeChanged -> _uiState.update { it.copy(reminderTime = event.reminderTime) }
            is CreateTripEvent.ToggleCatalogItem -> toggleCatalogItem(event.item)
            is CreateTripEvent.CatalogSearchChanged -> _uiState.update { it.copy(catalogSearchQuery = event.query) }
            is CreateTripEvent.StrategyChanged -> _uiState.update { it.copy(budgetStrategy = event.strategy) }
            is CreateTripEvent.IncludeNonDueItemsChanged -> _uiState.update { it.copy(includeNonDueItems = event.include) }
            CreateTripEvent.AutoFillFromBudget -> autoFillFromBudget()
            is CreateTripEvent.NewItemNameChanged -> _uiState.update { it.copy(newItemName = event.name) }
            is CreateTripEvent.NewItemPriceChanged -> _uiState.update { it.copy(newItemPrice = event.price) }
            is CreateTripEvent.NewItemPriorityChanged -> _uiState.update { it.copy(newItemPriority = event.priority) }
            CreateTripEvent.AddItem -> addItem()
            is CreateTripEvent.RemoveItem -> removeItem(event.itemId)
            is CreateTripEvent.UpdateItemPrice -> updateItemPrice(event.itemId, event.price)
            is CreateTripEvent.SortItems -> sortItems(event.order)
            CreateTripEvent.CreateTrip -> createTrip()
            CreateTripEvent.ClearError -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    /** Total cost of everything already on the shopping list, excluding [excludingItemId] if given. */
    private fun currentTotal(state: CreateTripUiState, excludingItemId: String? = null): Double =
        state.items.filterNot { it.id == excludingItemId }.sumOf { it.estimatedPrice * it.quantity }

    private fun toggleCatalogItem(catalogItem: Item) {
        val state = _uiState.value
        val alreadyAdded = state.items.any { it.catalogItemId == catalogItem.id }
        if (alreadyAdded) {
            _uiState.update {
                it.copy(items = it.items.filterNot { item -> item.catalogItemId == catalogItem.id })
            }
            return
        }
        val budget = state.budget.toDoubleOrNull()
        val itemTotal = catalogItem.estimatedPrice * catalogItem.quantity
        val projectedTotal = currentTotal(state) + itemTotal
        if (budget != null && projectedTotal > budget) {
            _uiState.update {
                it.copy(errorMessage = "Adding \"${catalogItem.name}\" would exceed your budget by ${formatKes(projectedTotal - budget)}")
            }
            return
        }
        val item = ShoppingItem(
            id = UUID.randomUUID().toString(),
            name = catalogItem.name,
            quantity = catalogItem.quantity,
            estimatedPrice = catalogItem.estimatedPrice,
            actualPrice = catalogItem.estimatedPrice,
            priorityIndex = catalogItem.priorityIndex,
            catalogItemId = catalogItem.id
        )
        _uiState.update { it.copy(items = sortItemsList(it.items + item, it.itemSortOrder)) }
    }

    private fun autoFillFromBudget() {
        val state = _uiState.value
        val budget = state.budget.toDoubleOrNull()
        if (budget == null) {
            _uiState.update { it.copy(errorMessage = "Enter a valid budget first") }
            return
        }
        val remaining = budget - currentTotal(state)
        if (remaining <= 0) {
            _uiState.update { it.copy(errorMessage = "No budget left to auto-fill") }
            return
        }
        val alreadyIncluded = state.items.mapNotNull { it.catalogItemId }.toSet()
        val candidates = state.catalogItems.filterNot { it.id in alreadyIncluded }
        val recommended = recommendItemsForBudget(
            candidates,
            remaining,
            state.budgetStrategy,
            includeNonDue = state.includeNonDueItems
        )
        if (recommended.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No catalog items are due for restock within your remaining budget") }
            return
        }
        val newItems = recommended.map { catalogItem ->
            ShoppingItem(
                id = UUID.randomUUID().toString(),
                name = catalogItem.name,
                quantity = catalogItem.quantity,
                estimatedPrice = catalogItem.estimatedPrice,
                actualPrice = catalogItem.estimatedPrice,
                priorityIndex = catalogItem.priorityIndex,
                catalogItemId = catalogItem.id
            )
        }
        _uiState.update { it.copy(items = sortItemsList(it.items + newItems, it.itemSortOrder)) }
    }

    private fun addItem() {
        val state = _uiState.value
        if (state.newItemName.isBlank()) return
        val price = state.newItemPrice.toDoubleOrNull() ?: 0.0
        val budget = state.budget.toDoubleOrNull()
        val projectedTotal = currentTotal(state) + price
        if (budget != null && projectedTotal > budget) {
            _uiState.update {
                it.copy(errorMessage = "Adding \"${state.newItemName.trim()}\" would exceed your budget by ${formatKes(projectedTotal - budget)}")
            }
            return
        }
        val item = ShoppingItem(
            id = UUID.randomUUID().toString(),
            name = state.newItemName.trim(),
            estimatedPrice = price,
            actualPrice = price,
            priorityIndex = state.newItemPriority,
            isCustom = true
        )
        _uiState.update {
            it.copy(
                items = sortItemsList(it.items + item, it.itemSortOrder),
                newItemName = "",
                newItemPrice = "",
                newItemPriority = 3
            )
        }
    }

    private fun removeItem(itemId: String) {
        _uiState.update { it.copy(items = it.items.filterNot { item -> item.id == itemId }) }
    }

    private fun updateItemPrice(itemId: String, price: Double) {
        val state = _uiState.value
        val target = state.items.find { it.id == itemId } ?: return
        val budget = state.budget.toDoubleOrNull()
        val projectedTotal = currentTotal(state, excludingItemId = itemId) + (price * target.quantity)
        if (budget != null && projectedTotal > budget) {
            _uiState.update {
                it.copy(errorMessage = "That price would exceed your budget by ${formatKes(projectedTotal - budget)}")
            }
            return
        }
        _uiState.update {
            it.copy(items = it.items.map { item ->
                if (item.id == itemId) item.copy(estimatedPrice = price, actualPrice = price) else item
            })
        }
    }

    private fun sortItems(order: ItemSortOrder) {
        _uiState.update { it.copy(itemSortOrder = order, items = sortItemsList(it.items, order)) }
    }

    private fun sortItemsList(items: List<ShoppingItem>, order: ItemSortOrder): List<ShoppingItem> =
        when (order) {
            ItemSortOrder.PRICE_ASC -> items.sortedBy { it.estimatedPrice }
            ItemSortOrder.PRICE_DESC -> items.sortedByDescending { it.estimatedPrice }
            ItemSortOrder.PRIORITY -> items.sortedByDescending { it.priorityIndex }
        }

    private fun createTrip() {
        val state = _uiState.value
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "You must be signed in to create a trip") }
            return
        }
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Trip name is required") }
            return
        }
        val budget = state.budget.toDoubleOrNull()
        if (budget == null) {
            _uiState.update { it.copy(errorMessage = "Enter a valid budget") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val tripId = UUID.randomUUID().toString()
            val trip = Trip(
                id = tripId,
                userId = userId,
                name = state.name.trim(),
                budget = budget,
                date = state.date,
                reminderTime = state.reminderTime
            )

            repository.createTrip(trip).fold(
                onSuccess = {
                    state.items.forEach { item ->
                        repository.addItem(item.copy(tripId = tripId))
                    }
                    state.reminderTime?.let { reminderTime ->
                        val userName = auth.currentUser?.displayName ?: "there"
                        TripReminderWorker.schedule(context, tripId, trip.name, userName, reminderTime)
                    }
                    _uiState.update { it.copy(isLoading = false) }
                    _tripCreated.update { tripId }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Failed to create trip") }
                }
            )
        }
    }
}
